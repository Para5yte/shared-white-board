package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;

import utilities.DialogMessage;
import utilities.ExchangePayload;
import utilities.SerialByteCanvas;
import whiteboard.WhiteBoardController;

/**
 * 
 * Server Listener class that keeps a out put stream open
 * updates on chat/graphics
 * @author Takemitsu Yamanaka 757038
 *
 */
public class ServerListener implements Runnable {
	
	private SocketAddress socketAddress;
	private Socket socket;
	private String username;
	
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private boolean isHost;
	
	private SerialByteCanvas byteCanvas;
	
	private String selectedHost;
	private Boolean accepted;
	private boolean connected = false;
	
	private WhiteBoardController controller;
	private ArrayList<String> connectedUsers;
	
	/**
	 * ServerListener class constructor
	 * @param payload		pay load that includes information of the user
	 * @param socketAddress	Socket address of the client
	 */
	public ServerListener(ExchangePayload payload, SocketAddress socketAddress)
	{
		socket = new Socket();
		this.socketAddress = socketAddress;
		
		this.username = payload.getUsername();
		
		String request = payload.getProtocol();
		
		isHost = false;
		// when you create a new session, you're automatically a host
		if(request.equals(Protocols.Client.KeepAlive.CREATE))
		{
			isHost = true;
			byteCanvas = payload.getGraphics();
		}
		this.connectedUsers = new ArrayList<String>();
	}


	/**
	 * Method to start a keep alive connection to the server
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void keepAliveConnection() throws IOException, ClassNotFoundException
	{
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new ObjectOutputStream(socket.getOutputStream());		
		
		if(isHost)
			createWhiteBoard();
		else
			joinHost();
	}

	
	/**
	 * Method to create a new white board session to the server
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private void createWhiteBoard() throws IOException, ClassNotFoundException
	{
		// send a create request to the server
		out.writeObject(new ExchangePayload(Protocols.Client.KeepAlive.CREATE,
				username, byteCanvas));
		out.reset();
		
		ExchangePayload response = (ExchangePayload) in.readObject();
		
		String protocol = response.getProtocol();
		
		if(!(protocol.equals(Protocols.Server.Response.SUCCESS)))
		{
			socket.close();
			in.close();
			out.close();
			displayError(protocol);
			System.exit(1);
		}
		displayInfo(NetworkConstants.CREATED);
		this.connected = true;
	}
	
	
	/**
	 * Method to join the host
	 * @throws ClassNotFoundException 
	 */
	private void joinHost() throws IOException, ClassNotFoundException
	{
		out.writeObject(new ExchangePayload(
				Protocols.Client.KeepAlive.JOIN, username));
		out.reset();
		
		ExchangePayload response = (ExchangePayload) in.readObject();
		
		String protocol = response.getProtocol();
		
		if(protocol.equals(Protocols.Server.Response.HOSTS))
		{
			selectHost(response.getUsers().getUsers());
			
			// send the request to join the selected host
			out.writeObject(new ExchangePayload(
					Protocols.Client.Request.JOIN, username, selectedHost));
			out.reset();
			
			// wait for a response on the join response
			response = (ExchangePayload) in.readObject();
			
			protocol = response.getProtocol();
			if(protocol.equals(Protocols.Server.Response.OK))
			{
				//set current graphics to what ever sent in pay load
				controller.updateGraphics(response.getGraphics());
				//exit out of loading screen
				Platform.runLater(() -> DialogMessage.closeLoadingScreen());
				displayInfo(NetworkConstants.HOST_ACCEPT);
				this.connected = true;
			}
			else if(protocol.equals(Protocols.Server.Response.NO))
			{
				displayError(NetworkConstants.HOST_REJECT);
			}
			else if(protocol.equals(Protocols.Server.Response.NOTFOUND))
			{
				displayError(NetworkConstants.HOST_EXIT);
			}
		}
		else
		{
			displayError(NetworkConstants.NO_SESSION);			
		}
	}

	/**
	 * Method to display a choice dialog to select a host to connect to
	 * @param hosts
	 */
	private void selectHost(ArrayList<String> hosts)
	{	
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		Runnable hostSelection = new Runnable() {
			public void run()
			{
				selectedHost = new DialogMessage().selectHost(hosts);
			}
		};
		Platform.runLater(hostSelection);

		while(lock.isLocked())
		{
			if(selectedHost!= null)
			{
				loadingScreen();
				lock.unlock();
			}
		}
	}
	
	
	/**
	 * Method to display a choice dialog to select a host to connect to
	 * @param hosts
	 */
	private boolean acceptJoinRequest(String username)
	{	
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		Platform.runLater(new Runnable() {
			public void run()
			{
				accepted = new DialogMessage().displayJoinRequest(username);
			}
		});
		
		while(lock.isLocked())
		{
			if(accepted != null)
			{
				lock.unlock();
				break;
			}
		}
		boolean result = (boolean) accepted;
		accepted = null;
		return result;
	}
	
	
	/**
	 * Method to update the UI's connected users list
	 * @param payload
	 */
	public void updateConnectedUsers(ExchangePayload payload)
	{
		ArrayList<String> payloadUsers = payload.getUsers().getUsers();
		
		// create two new list to check between each other
		List<String> newUserList = new ArrayList<String>(payloadUsers);
		List<String> currentUsers = new ArrayList<String>(connectedUsers);
		
		newUserList.removeAll(connectedUsers);
		currentUsers.removeAll(payloadUsers);
		
		// for new users add to view and this currently connected list
		newUserList.remove(username);
		newUserList.forEach(u -> 
			{
				controller.addNewClient(u);
				connectedUsers.add(u);
			}
		);
		
		// for disconnected users remove from view and currently connected list
		currentUsers.forEach(u -> 
			{
				controller.removeClient(u);
				connectedUsers.remove(u);
			}
		);
	}
	
	
	/** (non-Javadoc)
     * 
     */
    @Override
    public void run() 
    {	
        try 
        {
        	// connect to server socket
        	socket.connect(socketAddress);
        	
        	// start the keep alive streams
        	keepAliveConnection();
			
        	ExchangePayload payload = null;
            
            // listen for server requests for change in the graphics/canvas
            while((payload = (ExchangePayload) in.readObject())!= null)
            {

            	String request = payload.getProtocol();
            	
            	// update the graphical interface when requested
            	if(request.equals(Protocols.Server.Request.UPDATE))
    			{
     				controller.updateGraphics(payload.getGraphics());
     				continue;
    			}
            	
            	// Accept or Decline join request as admin
            	if(request.equals(Protocols.Server.Request.JOIN))
    			{
            		String username = payload.getUsername();
            		boolean accepted = acceptJoinRequest(username);
            		if(accepted)
            		{
            			out.writeObject(new ExchangePayload(
            				Protocols.Client.Response.ACCEPT));
            		}
            		else
            		{
            			out.writeObject(new ExchangePayload(
                				Protocols.Client.Response.DECLINE));
            		}
            		out.reset();
            		continue;
    			}
            	
            	if(request.equals(Protocols.Server.Request.CHAT))
    			{
            		controller.appendChatMessage(payload);
    			}
            	
            	if(request.equals(Protocols.Server.Request.USER))
    			{
            		updateConnectedUsers(payload);
    			}
            	
            	// Accept or Decline join request as admin
            	if(request.equals(Protocols.Server.Request.KICK))
    			{
            		displayError(NetworkConstants.HOST_KICK);
    			}

            	if(request.equals(Protocols.Server.Request.EXIT))
    			{
            		displayError(NetworkConstants.HOST_EXIT);
    			}
            }            
			System.out.println("Request from " 
				+ socket.getRemoteSocketAddress() + " : " + payload);
        }
        catch (SocketException e)
		{
        	displayError(NetworkConstants.SERVER_ERROR);
		}
        catch (Exception e) 
        {
            System.out.println("Error: " + e 
            	+ socket.getRemoteSocketAddress());
        } 
    }
    
    
    /**
	 * Method to display a information dialog
	 * @param hosts
	 */
	private void displayInfo(String msg)
	{	
		Platform.runLater(new Runnable() {
			public void run()
			{
				new DialogMessage().displayInfo(msg);
			}
		});
	}
	
	/**
	 * Method to display a information dialog
	 * @param hosts
	 */
	private void displayError(String msg)
	{	
		Platform.runLater(new Runnable() {
			public void run()
			{
				new DialogMessage().displayError(msg);
			}
		});
	}
	
	/**
	 * Method to display a loading screen
	 * @param hosts
	 */
	private void loadingScreen()
	{	
		Platform.runLater(() -> new DialogMessage().loadingScreen());
	}

	
    
    ////// Getters and Setters ///////
    
    /**
     * returns true if connected, false if not
     * @return
     */
    public boolean isConnected()
    {
    	return connected;
    }


	/**
	 * @return the controller
	 */
	public WhiteBoardController getController()
	{
		return controller;
	}


	/**
	 * @param controller the controller to set
	 */
	public void setController(WhiteBoardController controller)
	{
		this.controller = controller;
	}
}