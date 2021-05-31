package utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import network.Session;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Serialised ArrayList class that can be sent through sockets
 */

@SuppressWarnings("serial")
public class SerialStrArray implements Serializable{
	
	private ArrayList<String> users;
	
	/**
	 * UserArray for constructor when given a hash map of sessions
	 * sets users for every key of sessions
	 * @param sessions
	 */
	public SerialStrArray(HashMap<String, Session> sessions)
	{
		users = new ArrayList<>(sessions.keySet());
	}
	

	/**
	 * UserArray for constructor when given a hash map of sessions
	 * sets users for every key of sessions
	 * @param sessions
	 */
	public SerialStrArray(ArrayList<String> users)
	{
		this.users = users;
	}
	
	public ArrayList<String> getUsers()
	{
		return this.users;
	}
}
