package utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import network.Protocols;
import network.Session;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Serialised Message Exchange class that can be sent through sockets
 */

@SuppressWarnings("serial")
public class ExchangePayload implements Serializable{
	
	private String protocol;
	private String username;
	
	// content within the pay load
	private SerialByteCanvas graphics;
	private SerialStrArray users;
	private SerialMessage message;
	private String kickUser;
	private String selectedHost;
	
	
	/**
	 * Constructor for basic responses
	 * @param protocol	Protocol header
	 */
	public ExchangePayload(String protocol)
	{
		this.protocol = protocol;
	}
	
	
	/**
	 * Constructor for a basic connection
	 * @param protocol
	 * @param username
	 */
	public ExchangePayload(String protocol, String username)
	{
		this.protocol = protocol;
		this.username = username;
	}
	
	/**
	 * Constructor for updating graphics
	 * @param protocol
	 * @param username
	 * @param bytes
	 */
	public ExchangePayload(String protocol, String username, 
							Canvas canvas)
	{	
		this.protocol = protocol;
		this.username = username;
		
		this.graphics = new SerialByteCanvas(canvas);
	}
	
	/**
	 * Constructor for updating graphics
	 * @param protocol
	 * @param username
	 * @param bytes
	 */
	public ExchangePayload(String protocol, String username, 
			SerialByteCanvas byteCanvas)
	{	
		this.protocol = protocol;
		this.username = username;
		
		this.graphics = byteCanvas;
	}
	
	/**
	 * Constructor for sending array of user or hosts
	 * @param protocol
	 * @param username
	 * @param sessions
	 */
	public ExchangePayload(String protocol, String username, 
			HashMap<String, Session> sessions )
	{	
		this.protocol = protocol;
		this.username = username;
		
		this.users = new SerialStrArray(sessions);
	}
	
	/**
	 * Constructor for sending array of user or hosts
	 * @param protocol
	 * @param username
	 * @param sessions
	 */
	public ExchangePayload(String protocol, String username, 
			ArrayList<String> users )
	{	
		this.protocol = protocol;
		this.username = username;
		
		this.users = new SerialStrArray(users);
	}
	
	/**
	 * Constructor for sending new chat message
	 * @param protocol
	 * @param username
	 * @param message
	 */
	public ExchangePayload(String protocol, String username, 
			SerialMessage message)
	{	
		this.protocol = protocol;
		this.username = username;
		
		this.message = message;
	}
	
	/**
	 * Constructor for host selection
	 * @param protocol
	 * @param username
	 * @param input
	 */
	public ExchangePayload(String protocol, String username, 
			String input)
	{	
		this.protocol = protocol;
		this.username = username;
		
		if(protocol.equals(Protocols.Client.Request.KICK))
			this.kickUser = input;
		if(protocol.equals(Protocols.Client.Request.JOIN))
			this.selectedHost = input;
	}

	
	////////// Getters and Setters ///////////
	
	/**
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return the graphics
	 */
	public SerialByteCanvas getGraphics()
	{
		return graphics;
	}

	/**
	 * @param graphics the graphics to set
	 */
	public void setGraphics(SerialByteCanvas graphics)
	{
		this.graphics = graphics;
	}

	/**
	 * @return the users
	 */
	public SerialStrArray getUsers()
	{
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(SerialStrArray users)
	{
		this.users = users;
	}

	/**
	 * @return the message
	 */
	public SerialMessage getMessage()
	{
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(SerialMessage message)
	{
		this.message = message;
	}

	/**
	 * @return the kickUser
	 */
	public String getKickUsername()
	{
		return kickUser;
	}

	/**
	 * @param kickUser= the kickUser to set
	 */
	public void setKickUsername(String kickUser)
	{
		this.kickUser = kickUser;
	}
	
	/**
	 * @return the selectedHost
	 */
	public String getSelectedHost()
	{
		return selectedHost;
	}

	/**
	 * @param selectedHost the selectedHost to set
	 */
	public void setSelectedHost(String selectedHost)
	{
		this.selectedHost = selectedHost;
	}
}
