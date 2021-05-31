package whiteboard;

import network.ServerHandler;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * White board client controller class
 */
public class WhiteBoardClientController extends WhiteBoardController{
	
	/**
	 * Method to pass server handler from main to the controller
	 * @param server
	 */
	public void initClient(ServerHandler server)
	{
		super.server = server;
		server.joinWhiteBoard();
		server.setController(this);
		
		boolean isHost = false;
		
		//set list view once host boolean is established
		super.setListView(isHost);
	}
}
