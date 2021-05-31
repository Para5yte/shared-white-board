package network;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Network constants that includes error messages
 */
public class NetworkConstants{
	
	// Error message
    public static final String CLIENT_ARG_ERROR = 
        "Not enough arguments! should be <serverIPAddress> <serverPort> username";
    
    public static final String SERVER_ARG_ERROR = 
        "Not enough arguments! should be <serverPort>";
    
	public static final String INVALID_PORT = 
		"Please enter a valid port number from 1 to 65535";
	
	public static final String PORT_BINDED = 
		"Port is already used by another program, try another port!";
	
	public static final String HOST_EXIT = 
		"The host has left, please restart program to join another host";
	
	public static final String HOST_ACCEPT = 
		"Succesfully connected to the host whiteboard session!";
	
	public static final String CREATED =
		"Succesfully created a whiteboard session on the server!";
	
	public static final String HOST_REJECT = 
		"The host has did not allow you join, sorry.";
	
	public static final String HOST_KICK = 
		"The host has kicked you out of this session, sorry.";
	
	public static final String NO_SESSION = 
		"No avaliable session currently online";
		
	public static final String WRONG_SERVER =
		"Connected to wrong Server, "
			+ "try another IP or Port Number or check if Server is running";
		
	public static final String CONNECT_ERROR = 
		"Can't connect to Server, try another IP "
			+ "or Port Number, or check if the Server is running";
	
	public static final String SERVER_ERROR = 
		"Server is offline, please restart the server";
	
	public static final String DUPLICATE_USERNAME = 
		"Username already taken, please choice a new username";
}
