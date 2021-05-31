package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Client class to store information on an connected client
 */
public class Client {
	
	private SocketAddress socketAddress;
	private String username;
	
	// the stream that is kept alive to send message to the client for updates
	private ObjectOutputStream outputStream;
	
	// the stream that is kept alive to read message to the client for updates
	private ObjectInputStream inputStream;

	
	public Client(String username, ObjectOutputStream out, ObjectInputStream in)
	{
		this.username = username;
		this.outputStream = out;
		this.inputStream = in;
	}

	
	/**
	 * @return the socketAddress
	 */
	public SocketAddress getSocketAddress()
	{
		return socketAddress;
	}

	/**
	 * @param socketAddress the socketAddress to set
	 */
	public void setSocketAddress(SocketAddress socketAddress)
	{
		this.socketAddress = socketAddress;
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
	 * @return the outputStream
	 */
	public ObjectOutputStream getOutputStream()
	{
		return outputStream;
	}

	/**
	 * @param outputStream the outputStream to set
	 */
	public void setOutputStream(ObjectOutputStream outputStream)
	{
		this.outputStream = outputStream;
	}

	/**
	 * @return the inputStream
	 */
	public ObjectInputStream getInputStream()
	{
		return inputStream;
	}


	/**
	 * @param inputStream the inputStream to set
	 */
	public void setInputStream(ObjectInputStream inputStream)
	{
		this.inputStream = inputStream;
	}

}
