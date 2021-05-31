package utilities;

import java.util.ArrayList;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Window;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Dialog creator class
 */
public class DialogMessage{
	
	private static Alert alert;
	private ChoiceDialog<String> dialog;
	
	public DialogMessage()
	{
	}
	
	/**
	 * Method to display an error dialog
	 * @param msg	error message
	 */
	public void displayError(String msg)
	{
		alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText("Error Dialog");
		alert.setContentText(msg);
		alert.showAndWait();
		// exit program for all errors
		System.exit(1);
	}
	
	/**
	 * Method to display an information
	 * @param msg	Information to display
	 */
	public void displayInfo(String msg)
	{
		alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText("Information Dialog");
		alert.setContentText(msg);
		alert.showAndWait();
	}
	
	/**
	 * Method to display to a loading screen
	 */
	public void loadingScreen()
	{
        ButtonType button = new ButtonType("x", ButtonData.CANCEL_CLOSE);

		alert = new Alert(AlertType.INFORMATION,
				"If you close this screen, the program will exit!",
				button);
		alert.setTitle("Waiting for Connection");
		alert.setHeaderText("Waiting for Host to accept you into the session!");
		alert.getDialogPane().lookupButton(button).setVisible(false);
		Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> System.exit(1));
        
		alert.showAndWait();
	}
	
	/**
	 * Method to hide the loading screen
	 */
	public static void closeLoadingScreen()
	{
//		alert.close();
		alert.hide();
	}
	
	public boolean displayJoinRequest(String username)
	{
		ButtonType accept = new ButtonType("Accept", ButtonData.OK_DONE);
		ButtonType decline = new ButtonType("Decline", ButtonData.CANCEL_CLOSE);
		alert = new Alert(AlertType.CONFIRMATION,
				username + " would like to join.", accept, decline);
		alert.setTitle("Join Request");
		alert.setHeaderText("New client would like to join your session"
				+ ", please accept or decline.");

		Optional<ButtonType> result = alert.showAndWait();
		
		// alert is exited, no button has been pressed.
		if(!result.isPresent())
			return false;
		
		if (result.get() == accept)
			return true;
		else
			return false;
	}
	
	public String selectHost(ArrayList<String> hosts)
	{
		String selectedOption;
		
		dialog = new ChoiceDialog<String>(hosts.get(0), hosts);
        dialog.setTitle("Host selection!");
        dialog.setHeaderText("Please select a host to join");
        
        Optional<String> result = dialog.showAndWait();
        selectedOption = "cancelled";
        
        if(result.isPresent()) 
        	selectedOption = result.get();
        
        if(selectedOption.equals("cancelled"))
        	System.exit(1);
        
        return selectedOption;
	}
}
