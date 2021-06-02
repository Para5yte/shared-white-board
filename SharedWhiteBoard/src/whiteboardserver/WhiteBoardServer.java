package whiteboardserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import network.NetworkConstants;
import network.Protocols;
import network.Session;
import utilities.DialogMessage;
import utilities.ExchangePayload;
import utilities.SerialByteCanvas;
import utilities.SerialMessage;


/**
 * WhiteBoardServer class that can host multiple sessions of a white board
 * @author Takemitsu Yamanaka 757037
 *
 */
public class WhiteBoardServer{
	
    private int serverPort;
	
    private ServerSocket listeningSocket;
    
    // holds every session/white board currently hosted by this server
	private static HashMap<String, Session> sessions;
	
	// holds which session each user is connect to
	// HashMap <username, manager username>
	private static HashMap<String, String> users;
	
	
	/**
	 * Dictionary Server Constructor
	 * @param port			assigned port of the server
	 * @param dictionary	initial dictionary
	 * @throws IOException	throws any exception to GUI to display
	 * 						Noticeable exception is when the port is already
	 * 						binded to another program
	 */
	public WhiteBoardServer(int port) throws IOException
	{
		this.serverPort = port;
		
		sessions = new HashMap<String, Session>();
		users = new HashMap<String, String>();
	}
	
	
	/**
	 *  Method to start server
	 *  
	 */
	public void start()
	{		
		// start a new Thread that tries to run a white board server
		try
		{					
			//Create a server socket listening on port
			listeningSocket = new ServerSocket(serverPort);
					
			System.out.println("Server listening on port " + 
					serverPort + " for requests");
			
			//Listen for incoming connections for ever 
			while (true) 
			{
				// thread by connection, create a new thread per client
				Thread t = new Thread(new ClientHandler(listeningSocket.accept()));
				t.start();
			}
		} 
		catch (BindException e)
		{
			new DialogMessage().displayError(NetworkConstants.PORT_BINDED);;
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	
	
	/**
	 *  Adds new session into the sessions list
	 * @param payload	pay load of message
	 * @param out		out put stream
	 * @return
	 */
	public static boolean addNewSession(ExchangePayload payload, 
			ObjectOutputStream out, ObjectInputStream in)
	{
		String username = payload.getUsername();
		SerialByteCanvas byteCanvas = payload.getGraphics();
		
		users.put(username, username);
		sessions.put(username, new Session(username, out, in, byteCanvas));
		
		return true;
	}
	
	
	/**
	 * Check if user name already exist
	 * @param username
	 * @return	true if user name already taken
	 * 			false if user name isn't taken
	 */
	public static boolean dupUsername(String username)
	{
		return users.containsKey(username);
	}
	
	
	/**
	 * Method to join a new user into a session
	 * @param in 
	 * 
	 * @param username
	 * @return
	 * @throws Exception 
	 */
	public static void joinSession(ExchangePayload payload, 
			ObjectOutputStream out, ObjectInputStream in) throws Exception
	{
		String username = payload.getUsername();
		
		String selectedHost = payload.getSelectedHost();
		
		Session selectedSession = sessions.get(selectedHost);
		
		
		if(selectedSession == null)
		{
			// if selected session was closed during host selected
			// let the client know
			out.writeObject(new ExchangePayload(Protocols.Server.Response.NOTFOUND));
			out.reset();
			return;
		}
		
		if(selectedSession.joinRequest(username, out, in))
		{	
			// if the user was successfully accepted and added to the session
			// add the user to the users list
			users.put(username, selectedHost);
		}
	}
	
	
	/**
	 * Method to find which host is the user connected to
	 * @param username
	 * @return
	 */
	public String connectedSession(String username)
	{
		return users.get(username);
	}
	
	
	/**
	 * Method to kick a user from this session
	 * @param payload
	 * @throws IOException 
	 */
	public static void kickUser(ExchangePayload payload) throws IOException
	{
		String host = payload.getUsername();
		String kickUser = payload.getKickUsername();
		
		sessions.get(host).kickUser(kickUser);
		users.remove(kickUser);
	}
	
	/**
	 * Method to update chat for everyone connected to their session
	 * @param payload
	 * @throws IOException
	 */
	public static void updateChat(ExchangePayload payload) throws IOException
	{
		String user = payload.getUsername();
		SerialMessage msg = payload.getMessage();
		
		sessions.get(users.get(user)).updateChat(msg);
	}
	
	
	
	/**
	 * Method to remove the user from a session, if the user is a host
	 * Boot everyone in the session off
	 * @param username
	 * @throws IOException 
	 */
	public static void onExit(String username) throws IOException
	{
		if(sessions.containsKey(username))
		{
			// for removal ensure synchronised task
			synchronized (sessions)
			{
				// send all connected client a host left message
				sessions.get(username).onHostExit();
				sessions.remove(username);
				users.remove(username);
			}
			
			synchronized (users)
			{
				// remove all users connected to that host
				users.remove(username);
				for(String client : users.keySet()) {
					if(users.get(client).equals(username))
						users.remove(client);
				}
			}
		}
		else
		{
			// remove client from the connected session
			sessions.get(users.get(username)).removeUser(username);
			
			// remove user from the list of users currently in the server
			synchronized (users)
			{
				users.remove(username);
			}
		}
	}
	
	/**
	 * 
	 * Nested ConnectionThread class which implements Runnable interface
	 * this class will handle each request made by client as a task
	 */
	private static class ClientHandler implements Runnable {
		
		private Socket socket;
		
		private ObjectInputStream in;
		private ObjectOutputStream out;
		
		/**
		 * ClientHandler constructor
		 * @param socket	the socket which the client has connected to
		 */
		public ClientHandler(Socket socket) 
		{
	        this.socket = socket;
	    }
		
		/**
		 * Method to update graphics on all connected clients 
		 * 
		 * @param word	word to add or update
		 */
		private void updateGraphics(ExchangePayload message) throws Exception
		{
			String host;
			
			String username = message.getUsername();
			
			// first find which host is the user connected to
			host = users.get(username);		
			
			Session session = sessions.get(host);
			
			// next retrieve the session the user is connected to 
			session.updateGraphics(username, message.getGraphics());
		}
		
		/**
		 * Check if a request was a keep alive request
		 * @param msg request header
		 * @return	true if it's a keep alive request false otherwise
		 */
		private boolean isKeepAliveRequest(String msg) 
		{
			if(msg.equals(Protocols.Client.KeepAlive.CREATE) || 
				msg.equals(Protocols.Client.KeepAlive.JOIN))
				return true;
			
			return false;
		}

		
	    /** (non-Javadoc)
	     * 
	     */
	    @Override
	    public void run() 
	    {	
	    	ExchangePayload payload = null;
            String clientUsername = null;
            String request = null;
            
	        try 
	        {
	        	this.out = new ObjectOutputStream(socket.getOutputStream());
				this.in = new ObjectInputStream(socket.getInputStream());
				
				// wait for message from client
	            while((payload = (ExchangePayload) in.readObject())!= null)
	            {
	            	request = payload.getProtocol();
	            	clientUsername = payload.getUsername();
	            	
	            	/*
	            	 *  Read the protocol of client request
	            	 *  per the request do what the client has requested
	            	 *  respond to the client
	            	 */
	            	if(request.equals(Protocols.Client.Request.CONNECT))
	            	{
	            		if(dupUsername(clientUsername))
	            		{
	            			out.writeObject(new ExchangePayload(
	            					Protocols.Server.Response.DUPLICATE));
	            		}
	            		else
	            		{
	            			out.writeObject(new ExchangePayload(
	            					Protocols.Server.Response.SUCCESS));
	            		}
	            			
	            	}
	            	// an updated graphical request from client
	            	if(request.equals(Protocols.Client.Request.UPDATE))
	            	{
	            		updateGraphics(payload);
	        		
            			out.writeObject(new ExchangePayload(
            					Protocols.Server.Response.SUCCESS));
	            	}
	            	
            		if(request.equals(Protocols.Client.Request.KICK))
            		{
            			kickUser(payload);
            			
            			out.writeObject(new ExchangePayload(
            					Protocols.Server.Response.SUCCESS));
            		}
            		
        			if(request.equals(Protocols.Client.Request.CHAT))
        			{
        				updateChat(payload);
        				
        				out.writeObject(new ExchangePayload(
            					Protocols.Server.Response.SUCCESS));
        			}
        			
        			if(request.equals(Protocols.Client.Request.EXIT))
        			{
        				onExit(clientUsername);
        				
        				out.writeObject(new ExchangePayload(
            					Protocols.Server.Response.SUCCESS));
        			}
	            	out.reset();
	            	
	            	if(!isKeepAliveRequest(request))
	            	{
	            		in.close();
	    				out.close();
	            		break;
	            	}
	            	
	            	// host can request to create a new white board
					if(request.equals(Protocols.Client.KeepAlive.CREATE))
					{	
						// throws exception if failed for whatever reason
						addNewSession(payload, out, in);
						
						out.writeObject(new ExchangePayload(
								Protocols.Server.Response.SUCCESS));
						out.reset();

						break;
					}
					
					if(request.equals(Protocols.Client.KeepAlive.JOIN))
					{
						if(sessions.size()==0)
						{
							out.writeObject(new ExchangePayload(
								Protocols.Server.Response.NOSESSION));
							out.reset();
							break;
						}
						
						// send the current available sessions to join to client
						out.writeObject(new ExchangePayload(
								Protocols.Server.Response.HOSTS,
								clientUsername, sessions));
						
						out.reset();
						
						// once the reply of host selection by client is received
						// process the following
						// 1. ask the host to accept/decline
						// 2.a) add the client to the session if host accept
						// 2.b) reply to client if host decline
						joinSession((ExchangePayload)in.readObject(), out, in);
						
						break;
					}
	            }
				System.out.println("Request from " + clientUsername
					+ socket.getRemoteSocketAddress() + " : " + request);
	        }
	        catch (SocketException e)
	        {
	        	System.out.println("Connection closed from " 
	        		+ socket.getRemoteSocketAddress());
	        }
	        catch (Exception e) 
	        {
	            System.out.println("Error: " + e 
	            	+ socket.getRemoteSocketAddress());
	        } 
	    }
	}
}
