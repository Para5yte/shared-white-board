package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

import utilities.DialogMessage;
import utilities.ExchangePayload;
import utilities.SerialMessage;
import whiteboard.WhiteBoardController;

/**
 * ServerHandler class that handles all the server connection tasks
 * @author Takemitsu Yamanaka 757037
 *
 */
public class ServerHandler {
	
	private SocketAddress socketAddress;
	private String username;
	
	private ServerListener listener;
	
	private WhiteBoardController controller;
	
	public ServerHandler(String serverAddress, int serverPort, String username)
	{
		this.username = username;
		
		this.socketAddress = new InetSocketAddress(serverAddress, serverPort);
	}
	
	
	/**
	 * method to connect to server to check if there is a server present
	 * @return String	server response
	 */
	public String connect()
	{
		return clientRequest(new ExchangePayload(
				Protocols.Client.Request.CONNECT, username));
	}
	
	
	/**
	 * Method to create a white board session as a host and open a keep alive
	 * connection to the server
	 * @param mainGraphics 
	 * @return	String	Server Response
	 */
	public void createWhiteBoard(Canvas canvas)
	{
		startListener(new ExchangePayload(Protocols.Client.KeepAlive.CREATE, 
				username, canvas));
	}
	
	
	/**
	 * Method to join a white board session of the manager and open a keep alive
	 * connection to the server
	 * @return	String	Server Response
	 */
	public void joinWhiteBoard()
	{
		
		startListener(
			new ExchangePayload(Protocols.Client.KeepAlive.JOIN, username));
	}
	
	
	/**
	 * Method to start Server Listener
	 * @param mainGraphics 
	 */
	public void startListener(ExchangePayload payload)
	{
		this.listener =new ServerListener(payload, socketAddress);
		Thread t = new Thread(this.listener);
		t.start();
	}
	
	
	/**
	 * Method to send graphical update to the server
	 * @return String	server response
	 */
	public String sendGraphicalUpdate(Canvas mainCanvas)
	{
		return clientRequest(new ExchangePayload(
			Protocols.Client.Request.UPDATE, username, mainCanvas));
	}
	
	
	/**
	 * Method to request a user to be kicked from this session
	 */
	public String kickUser(String toKick)
	{
		return clientRequest(new ExchangePayload(
				Protocols.Client.Request.KICK, username, toKick));
	}
	
	/**
	 * Method to request a user to be kicked from this session
	 */
	public String sendChat(String msg)
	{
		return clientRequest(new ExchangePayload(
				Protocols.Client.Request.CHAT, 
				username, new SerialMessage(username, msg)));
	}

	
	/**
	 * Method when exit to send to server
	 */
	public void onExit()
	{
		clientRequest(
				new ExchangePayload(Protocols.Client.Request.EXIT, username));
	}
	
	
	/**
	 * Method to send updates to server on any changes of the UI to the server
	 * @param request
	 * @return
	 */
	private String clientRequest(ExchangePayload clientPayload)
	{
		Socket socket = new Socket();
		ExchangePayload payload = null;
		
		try
		{
			// Create socket bounded to any port and connect it to the server
			socket.connect(socketAddress);
			
			InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			
			ObjectInputStream in = new ObjectInputStream(inStream);
			ObjectOutputStream out = new ObjectOutputStream(outStream);
			
			String request = clientPayload.getProtocol();

			// send a connect request to check if there is an server
			if(request.equals(Protocols.Client.Request.CONNECT))
        	{
				out.writeObject(clientPayload);
        	}
        	
			// Send a graphical update to the server session
        	if(request.equals(Protocols.Client.Request.UPDATE))
        	{
        		out.writeObject(clientPayload);
        	}
        	
        	// Kick a user from the session
    		if(request.equals(Protocols.Client.Request.KICK))
    		{
        		out.writeObject(clientPayload);
    		}
    		
    		// Send a chat message to the server
			if(request.equals(Protocols.Client.Request.CHAT))
			{
        		out.writeObject(clientPayload);
			}
			
			// Send a chat message to the server
			if(request.equals(Protocols.Client.Request.EXIT))
			{
				out.writeObject(clientPayload);
				in.close();
				socket.close();
				return request;
			}
			out.reset();
			
			payload = (ExchangePayload) in.readObject();
			
			if(payload == null)
				return "Something is wrong";
			
			socket.close();
			in.close();
			return payload.getProtocol();
		}
		catch (UnknownHostException | ConnectException | SocketTimeoutException e)
		{
			return NetworkConstants.CONNECT_ERROR;	
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return "Unknown Error";
	}
	
	/**
	 * Method to run dialog in a it's runnable thread since FX does not allow
	 * other threads to make changes
	 * @param msg
	 */
	public void displayError(String msg)
	{	
		Platform.runLater(new Runnable() {
			public void run()
			{
				new DialogMessage().displayError(msg);
			}
		});
	}


	/**
	 * Method to check if the client is connected to active white board session
	 * @return	true	if the client is connected
	 *			false	if the client isn't connected
	 */
	public boolean isConnected()
	{
		return this.listener.isConnected();
	}

	
	//// Getters and Setters
	
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
		this.listener.setController(controller);
	}
}
