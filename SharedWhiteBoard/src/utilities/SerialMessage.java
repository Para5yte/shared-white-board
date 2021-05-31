package utilities;

import java.io.Serializable;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Serialised Message class that can be sent through sockets
 */
@SuppressWarnings("serial")
public class SerialMessage implements Serializable{

	private String chatUser;
	private String chatMessage;
	
	/**
	 * SerialMessage Constructor
	 * @param chatMessage
	 * @param chatUser
	 */
	public SerialMessage(String chatUser, String chatMessage)
	{
		this.chatUser = chatUser;
		this.chatMessage = chatMessage;
	}

	/**
	 * @return the chatMessage
	 */
	public String getChatMessage()
	{
		return chatMessage;
	}

	/**
	 * @param chatMessage the chatMessage to set
	 */
	public void setChatMessage(String chatMessage)
	{
		this.chatMessage = chatMessage;
	}

	/**
	 * @return the chatUser
	 */
	public String getChatUser()
	{
		return chatUser;
	}

	/**
	 * @param chatUser the chatUser to set
	 */
	public void setChatUser(String chatUser)
	{
		this.chatUser = chatUser;
	}
	
}
