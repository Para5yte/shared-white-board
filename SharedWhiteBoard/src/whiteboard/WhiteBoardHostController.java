package whiteboard;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import network.ServerHandler;
import utilities.DialogMessage;
import utilities.ImageUtils;


/**
 * WhiteBoardHostController class that displays the windows which is displayed
 * to the host of a session
 * @author Takemitsu Yamanaka 757037
 *
 */
public class WhiteBoardHostController extends WhiteBoardController{
	
	//// javafx host only variables
	
	@FXML // fx:id="clearBtn"
    private Button clearBtn; // Value injected by FXMLLoader

    @FXML // fx:id="openBtn"
    private Button openBtn; // Value injected by FXMLLoader

    @FXML // fx:id="saveBtn"
    private Button saveBtn; // Value injected by FXMLLoader

    @FXML // fx:id="saveAsBtn"
    private Button saveAsBtn; // Value injected by FXMLLoader

    @FXML // fx:id="close"
    private Button close; // Value injected by FXMLLoader
    
    
    // directory inputs
    private String filepath;
    private File currentOpenedFile;
	
	/**
	 * Method to pass server handler from main to the controller
	 * @param server
	 */
	public void initServer(ServerHandler server)
	{
		super.server = server;
		server.createWhiteBoard(super.mainCanvas);
		server.setController(this);
		
		boolean isHost = true;

		//set list view once host boolean is established
		super.setListView(isHost);
		filepath = System.getProperty("user.home");
	}
	
	
	@FXML 
    private void clearCanvas(ActionEvent e)
    {
        mainGraphics.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        drawGraphics.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        currentOpenedFile = null;
        server.sendGraphicalUpdate(mainCanvas);
    }
	
	@FXML
	private void save(ActionEvent e) 
	{
		// check if a file is open to be saved into yet
		if(currentOpenedFile == null)
		{
			new DialogMessage().displayInfo(
					"No file currently open, press ok to save as new file");
			
			currentOpenedFile = saveFile("Save White Board");
			filepath = currentOpenedFile.getParent();
		}
		
		try
		{
			ImageUtils.saveImageToFile(mainCanvas, currentOpenedFile);
		} 
		catch (IOException e1)
		{
			new DialogMessage().displayInfo("Error when saving image");
		}
	}
	
	@FXML
	private void saveAs(ActionEvent e)
	{
		File file = saveFile("Save White Board");
		
		if(file == null)
		{
			return;
		}
		
		try
		{
			//once saved successfully change the currently opened file
			ImageUtils.saveImageToFile(mainCanvas, file);
			currentOpenedFile = file;
			filepath = currentOpenedFile.getParent();
		} 
		catch (IOException e1)
		{
			new DialogMessage().displayInfo("Error when saving image");
		}
	}
	
	@FXML
	private void open(ActionEvent e)
	{
		File file = selectFile("Open a image file to load into White Board");
		if(file == null)
		{
			return;
		}
		
		try
		{
			// open the file as a Writable Image
			WritableImage image = ImageUtils.openImageFromFile(file);
			
			// clear the screen
			clearCanvas(e);
			
			// draw onto canvas
			mainGraphics.drawImage(image, 0, 0);
			
			currentOpenedFile = file;
			filepath = currentOpenedFile.getParent();

			// once image is loaded send update to all clients
		    server.sendGraphicalUpdate(mainCanvas);
		} 
		catch (IOException e1)
		{
			new DialogMessage().displayInfo("Error when loading image");
		}		
	}
	
	
	/**
	 * Save the current canvas into a file
	 * @param msg
	 */
	private File saveFile(String msg)
	{
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(filepath));
		
		// allow both jpg and png to save in
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG","*.jpg"));
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG","*.png"));
		fc.setTitle(msg);
		return fc.showSaveDialog(anchorPane.getScene().getWindow());
	}
	
	
	/**
	 * Method to select a file in the disk
	 * @return
	 */
	private File selectFile(String msg)
	{
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(filepath));
		// allow both jpg and png
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG","*.jpg"));
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG","*.png"));
		fc.setTitle(msg);
		return fc.showOpenDialog(anchorPane.getScene().getWindow());
	}
}
