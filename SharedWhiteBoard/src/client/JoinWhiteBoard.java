package client;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.NetworkConstants;
import network.Protocols;
import network.ServerHandler;
import network.Utilities;
import utilities.DialogMessage;
import whiteboard.WhiteBoardClientController;


/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Join White Board main class
 */
public class JoinWhiteBoard extends Application{
	
	private static String serverAddress;
    private static int serverPort;
    private static String username;
	private static ServerHandler server;
    
	@Override
    public void start(Stage stage) throws Exception {
    	parseArgs();
		
    	server = new ServerHandler(serverAddress, serverPort, username);
    	
    	// Connect to a server first, error message if there is no server
    	String response = server.connect();	
    	
    	if(response.equals(Protocols.Server.Response.SUCCESS))
    	{	
    		FXMLLoader loader = new FXMLLoader(
            		getClass().getResource("WhiteBoardClient.fxml"));
    		
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            WhiteBoardClientController controller = loader.getController();
            // connect to server and request to join a open session
            controller.initClient(server);
            
            stage.setScene(scene);
            stage.setTitle("Shared White Board");
            stage.show();
            
            System.out.println("Whiteboard client created");
    	}
    	else if (response.equals(Protocols.Server.Response.DUPLICATE))
    	{
    		new DialogMessage().displayError(NetworkConstants.DUPLICATE_USERNAME);
    	}
    	else if (response.equals(Protocols.Server.Response.NOTFOUND))
    	{
    		new DialogMessage().displayError(NetworkConstants.HOST_EXIT);
    	}
    	else
    	{
    		new DialogMessage().displayError(response);
    	}
    }
	
	@Override
	public void stop(){
	    server.onExit();
	    System.exit(1);
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
     * parses the arguments of input
     */
    private void parseArgs() {
    	List<String> arugments = this.getParameters().getUnnamed();
        
    	if (arugments.size() < 3)
        	new DialogMessage().displayError(NetworkConstants.CLIENT_ARG_ERROR);
        
        serverAddress = arugments.get(0);
        
        if(Utilities.isPortFormatCorrect(arugments.get(1)))
        	serverPort = Integer.parseInt(arugments.get(1));
        else
        	new DialogMessage().displayError(NetworkConstants.INVALID_PORT);
        
        username = Utilities.formatWord(arugments.get(2));
    }
}
