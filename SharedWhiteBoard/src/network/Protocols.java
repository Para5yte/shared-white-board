package network;



/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Message Exchange Protocol Headers
 */
public class Protocols {
	/**
	 * 
	 * Client MESSAGE Headers class
	 */
	public class Client{
		
		public class Request{
			public static final String CONNECT = "CONNECT";
			public static final String JOIN = "JOIN";
			public static final String UPDATE = "UPDATE";
			public static final String KICK = "KICK";
			public static final String CHAT = "CHAT";
			public static final String EXIT = "EXIT";
		}
		
		/**
		 * 
		 * Client response headers class
		 */
		public class Response{
			
			public static final String OK = "OK";
			public static final String ACCEPT = "ACCEPT";
			public static final String DECLINE = "DECLINE";
		}
		
		/**
		 * 
		 * Client KeepAlive request headers
		 * When these request are called, the connection must stay alive
		 */
		public class KeepAlive{
			public static final String CREATE = "CREATE";
			public static final String JOIN = "JOIN";
		}
		
	}
	
	/**
	 * 
	 * Server MESSAGE Headers class
	 */
	public class Server{
		
		/**
		 * Server Request Messages
		 */
		public class Request{
			public static final String UPDATE = "UPDATE";
			public static final String JOIN = "JOIN";
			public static final String EXIT = "EXIT";
			public static final String KICK = "KICK";
			public static final String CHAT = "CHAT";
			public static final String USER = "USER";
		}
		
		/**
		 * 
		 * Server Response Headers class
		 */
		public class Response{
			public static final String SUCCESS = "SUCCESS";
			public static final String USERS = "USERS";
			public static final String OK = "OK";
			public static final String NO = "NO";
			public static final String NOSESSION = "NOSESSION";
			public static final String HOSTS = "HOSTS";
			public static final String NOTFOUND = "NOTFOUND";
			public static final String DUPLICATE = "DUPLICATE";
		}
	}
}
