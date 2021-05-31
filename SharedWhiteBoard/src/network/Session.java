package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import utilities.ExchangePayload;
import utilities.SerialByteCanvas;
import utilities.SerialMessage;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Session class to store information on an open session of white board
 */
public class Session {
		
	private Client host;
	private HashMap<String, Client> connectedClients;
	
	private SerialByteCanvas byteCanvas;
	
	// private Chat chat;
	public Session(String username, 
			ObjectOutputStream out, ObjectInputStream in, 
			SerialByteCanvas byteCanvas)
	{
		this.host = new Client(username, out, in);
		this.connectedClients = new HashMap<String, Client>();
		this.connectedClients.put(username, this.host);
		this.byteCanvas = byteCanvas;
	}
	
	
	/**
	 * Method to add a new client to this session
	 * @param user
	 * @param clientSocket
	 * @return
	 * @throws IOException 
	 */
	public void addNewClient(String user, 
			ObjectOutputStream out, ObjectInputStream in) throws IOException
	{
		this.connectedClients.put(user, new Client(user, out, in));
		this.updateUserArray();
	}
	
	
	/**
	 * Send a join request to host
	 * @param user		username
	 * @param out 		ObjectOutputStream
	 * @param in 		ObjectInputStream
	 */
	public boolean joinRequest(String user, 
			ObjectOutputStream out, ObjectInputStream in) throws Exception
	{
		ObjectOutputStream objectOut;
		ObjectInputStream objectIn;
		objectOut = this.host.getOutputStream();
		objectIn = this.host.getInputStream();
		
		// send join request to the host
		objectOut.writeObject(new ExchangePayload(
				Protocols.Server.Request.JOIN, user));
		
		objectOut.reset();
		
		ExchangePayload response = (ExchangePayload) objectIn.readObject();
		
		ExchangePayload send;
		boolean accepted = 
			response.getProtocol().equals(Protocols.Client.Response.ACCEPT);
		
		// the payload to send to the client
		if(accepted)
		{
			send = new ExchangePayload(Protocols.Server.Response.OK,
					user, this.byteCanvas);
		}
		else
		{
			send = new ExchangePayload(Protocols.Server.Response.NO);
		}
		
		// if the client close the connection before the host replies
		// catch SocketException here and end this request safely
		try {
			out.writeObject(send);
			out.reset();
		}
		catch (SocketException e)
		{
			return false;
		}
		
		// once client's join request is sent safely, add client to this session
		if(accepted)
		{
			addNewClient(user, out, in);
			return true;
		}
		else
			return false;
	}
	
	
	/**
	 * Write to all output stream with a updated graphics
	 * Since we want the updates to be flowing with changes, we want this 
	 * method to be synchroised
	 * @param user
	 * @param serialG
	 */
	public synchronized void updateGraphics(String user, 
			SerialByteCanvas serialG) throws Exception
	{
		// set the current white board to be this
		this.byteCanvas = serialG;
		
		ObjectOutputStream objectOut;
		
		// for each client connected to this session, update their canvas
		for (Client client : connectedClients.values()) 
		{
			if(client.getUsername().equals(user))
				continue;
			
			System.out.println("Updating canvas of user :" +client.getUsername());
			objectOut = client.getOutputStream();
			objectOut.writeObject(new ExchangePayload(
					Protocols.Server.Request.UPDATE, user, this.byteCanvas));
			objectOut.reset();
        }
	}
	
	
	/**
	 * remove user from current session
	 * @param username
	 * @throws IOException 
	 */
	public void removeUser(String username) throws IOException
	{
		this.connectedClients.remove(username);
		this.updateUserArray();
	}
	
	
	/**
	 * Update connected clients on client exit
	 * @param user	user name of the user to remove
	 * @throws IOException 
	 */
	public void kickUser(String user) throws IOException{
		
		Client client = this.connectedClients.remove(user);
		this.updateUserArray();
		
		ObjectOutputStream out = client.getOutputStream();
		
		out.writeObject(new ExchangePayload(Protocols.Server.Request.KICK));
		out.reset();
	}
	
	
	/**
	 * on Host exit, stop all client connection
	 * @param user
	 * @throws IOException 
	 */
	public void onHostExit() throws IOException
	{
		ObjectOutputStream objectOut;
		
		// for each client connected to this session, send EXIT request to each
		for (Client client : connectedClients.values()) 
		{
			if(client.getUsername().equals(host.getUsername()))
				continue;
			objectOut = client.getOutputStream();
			objectOut.writeObject(new ExchangePayload(
					Protocols.Server.Request.EXIT));
			
			objectOut.close();
		}
	}
	
	
	/**
	 * Update the user list upon kick/join/exit of a client
	 * @param user	user name of the user to remove
	 * @throws IOException 
	 */
	public synchronized void updateUserArray() throws IOException
	{
		ArrayList<String> clients = 
				new ArrayList<>(this.connectedClients.keySet());
		
		ObjectOutputStream objectOut;
		
		ExchangePayload payload = new ExchangePayload(
			Protocols.Server.Request.USER, host.getUsername(), clients);
				
		//update everyone's connected user list
		for (Client client : connectedClients.values()) 
		{
			objectOut = client.getOutputStream();
			objectOut.writeObject(payload);
			objectOut.reset();
		}
	}
	
	
	/**
	 * Update connected clients on client exit
	 * @param user	user name of the user to remove
	 * @throws IOException 
	 */
	public synchronized void updateChat(SerialMessage msg) throws IOException
	{
		ObjectOutputStream objectOut;
		
		ExchangePayload payload = new ExchangePayload(
			Protocols.Server.Request.CHAT, msg.getChatUser(), msg);
				
		// Send everyone's new chat list
		for (Client client : connectedClients.values()) 
		{
			// skip the user that sent this message
			if(client.getUsername().equals(msg.getChatUser()))
				continue;
			
			objectOut = client.getOutputStream();
			objectOut.writeObject(payload);
			objectOut.reset();
		}
	}
	
	
	/////// Getters and Setters ///////

	/**
	 * @return the host
	 */
	public Client getHost()
	{
		return host;
	}


	/**
	 * @param host the host to set
	 */
	public void setHost(Client host)
	{
		this.host = host;
	}


	/**
	 * @return the connectedClients
	 */
	public HashMap<String, Client> getConnectedClients()
	{
		return connectedClients;
	}


	/**
	 * @param connectedClients the connectedClients to set
	 */
	public void setConnectedClients(HashMap<String, Client> connectedClients)
	{
		this.connectedClients = connectedClients;
	}


	/**
	 * @return the byteCanvas
	 */
	public SerialByteCanvas getByteCanvas()
	{
		return byteCanvas;
	}


	/**
	 * @param byteCanvas the byteCanvas to set
	 */
	public void setByteCanvas(SerialByteCanvas byteCanvas)
	{
		this.byteCanvas = byteCanvas;
	}
}
