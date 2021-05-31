package whiteboardserver;

import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import network.NetworkConstants;
import network.Utilities;
import utilities.DialogMessage;


/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Server Main class
 */
public class ServerMain extends Application{
	
	private static int serverPort;
    
	@Override
    public void start(Stage stage) throws Exception {
    	parseArgs();
    	
    	// create white board server
		WhiteBoardServer server = new WhiteBoardServer(serverPort);
		
		// start the server
		server.start();
		
		System.out.println("Server started, listening on port " + serverPort);
    }
    
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	
    	try 
 		{ 	
    		launch(args);
        } 
 		catch (Exception e) 
 		{
             e.printStackTrace();
        }
    }
    
    
    /**
     * @param
     */
    private void parseArgs() {
    	List<String> arugments = this.getParameters().getUnnamed();
        if (arugments.size() < 1) {
        	new DialogMessage().displayError(NetworkConstants.SERVER_ARG_ERROR);
            System.exit(1);
        }
        
        if(Utilities.isPortFormatCorrect(arugments.get(0)))
        {
        	serverPort = Integer.parseInt(arugments.get(0));
        }
        else
        {
        	new DialogMessage().displayError(NetworkConstants.INVALID_PORT);
        	System.exit(1);
        }
    }
}
