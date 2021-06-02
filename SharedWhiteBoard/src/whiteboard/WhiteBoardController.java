package whiteboard;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import network.ServerHandler;
import utilities.ExchangePayload;
import utilities.SerialByteCanvas;
import utilities.SerialMessage;


/**
 * WhiteBoardController class that displays a canvas for the user to draw on
 * @author Take Yamanaka 757037.
 */
public class WhiteBoardController implements Initializable {
	
    protected GraphicsContext mainGraphics;
    protected GraphicsContext drawGraphics;
    
    private boolean line = true;
    private boolean circle = false;
    private boolean oval = false;
    private boolean rectangle = false;
    private boolean text = false;
    private boolean eraser = false;
    private boolean pencil = false;
    
    private double startX;
    private double startY;
    private double lastX;
    private double lastY;
    private double oldX;
    private double oldY;
    
    private String tempString;
    
    // FXML Variables
    @FXML // fx:id="anchorPane"
    protected AnchorPane anchorPane; // Value injected by FXMLLoader
    
    @FXML // fx:id="lineBtn"
    private Button lineBtn; // Value injected by FXMLLoader

    @FXML // fx:id="circleBtn"
    private Button circleBtn; // Value injected by FXMLLoader

    @FXML // fx:id="ovalBtn"
    private Button ovalBtn; // Value injected by FXMLLoader

    @FXML // fx:id="rectBtn"
    private Button rectBtn; // Value injected by FXMLLoader

    @FXML // fx:id="textBtn"
    private Button textBtn; // Value injected by FXMLLoader

    @FXML // fx:id="eraserBtn"
    private Button eraserBtn; // Value injected by FXMLLoader

    @FXML // fx:id="pencilBtn"
    private Button pencilBtn; // Value injected by FXMLLoader

    @FXML // fx:id="colourPick"
    private ColorPicker colourPick; // Value injected by FXMLLoader

    @FXML // fx:id="strokeRadioBtn"
    private RadioButton strokeRadioBtn; // Value injected by FXMLLoader

    @FXML // fx:id="fillRadioBtn"
    private RadioButton fillRadioBtn; // Value injected by FXMLLoader

    @FXML // fx:id="sizeSlider"
    private Slider sizeSlider; // Value injected by FXMLLoader

    @FXML // fx:id="mainCanvas"
	protected Canvas mainCanvas; // Value injected by FXMLLoader

    @FXML // fx:id="tempCanvas"
    private Canvas tempCanvas; // Value injected by FXMLLoader
    
    @FXML // fx:id="scrollPane"
    private ScrollPane scrollPane;	// Value injected by FXMLLoader
    
    @FXML // fx:id="messagePane"
    private TextFlow messagePane; // Value injected by FXMLLoader
    
    @FXML // fx:id="connectedUsersView"
	private ListView<String> connectedUsersView; // Value injected by FXMLLoader
    
    @FXML // fx:id="sendTextBtn"
    private Button sendTextBtn; // Value injected by FXMLLoader

    @FXML // fx:id="textField"
    private TextArea textField; // Value injected by FXMLLoader

    
    ///// Server Handler for white board to process actions to server
    protected ServerHandler server;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainGraphics = mainCanvas.getGraphicsContext2D();
        drawGraphics = tempCanvas.getGraphicsContext2D();

        tempCanvas.getParent().requestFocus();
        sizeSlider.setMin(1);
        sizeSlider.setMax(30);
        
        tempString = "";
        
        // set a listener to auto scroll to bottom
        // only issue is it would be annoying if someone is spamming messages
        messagePane.heightProperty().addListener(
        		observable -> scrollPane.setVvalue(1D));
    }
    
    
    /**
     * updates the current canvas
     * @param byteCanvas Serialisable bytes of the canvas
     * @return 
     */
    public void updateGraphics(SerialByteCanvas byteCanvas)
    {
    	Platform.runLater(new Runnable() {
			public void run()
			{
				WritableImage image = byteCanvas.toWriteableImage();
				mainGraphics.drawImage(image, 0, 0);
			}
		});
    }
    
    
    //////// Advanced Features - start /////////
    
    /**
     * method to initialise the customer list view of the screen
     */
    protected void setListView(boolean isHost)
	{
		connectedUsersView.setCellFactory(
	        	new Callback<ListView<String>, ListCell<String>>() {
			        @Override
			        public ListCell<String> call(ListView<String> param) {
			            return new ClientCell(isHost);
			        }
	        	}
	    );
	}
    
    /**
     * Method to add a new connected client to the UI
     * @param username
     */
    public void addNewClient(String username){
        Platform.runLater(new Runnable() {
            public void run(){
            	connectedUsersView.getItems().add(username);
            }
        });
    }
    
    /**
     * Method to remove connected client from the UI
     * @param username
     */
    public void removeClient(String username){
        Platform.runLater(new Runnable() {
            public void run(){
            	connectedUsersView.getItems().remove(username);
            }
        });
    }
    
    /// Chat methods
    
    @FXML
    private void sendChatBtn(ActionEvent e)
    {
    	sendChat();
    }
    
    @FXML
    private void sendChatKey(KeyEvent e)
    {
    	if (e.getCode() == KeyCode.ENTER)
    		sendChat();
    }
    
    /**
     * Method to send chat message to server when the send button or "Enter" 
     * is pressed or enterd.
     */
    private void sendChat()
    {
    	String msg = textField.getText().trim();
    	if(msg.length() == 0)
    	{	
    		//do nothing
    	}
    	else
    	{
    		textField.clear();
    		Text chat = new Text("me: " + msg + "\n");
    		chat.setFont(new Font(16));
    		messagePane.getChildren().add(chat);
    		server.sendChat(msg);
    	}
    }
    
    
    /**
     * Append new chat message to the messagePane
     * @param payload
     */
    public void appendChatMessage(ExchangePayload payload)
    {
    	SerialMessage msg = payload.getMessage();
    	System.out.println("new message received");
    	Platform.runLater(new Runnable() {
            public void run(){
            	Text chat = new Text(
            			msg.getChatUser() + ": " + msg.getChatMessage() + "\n");
        		chat.setFont(new Font(16));
      			messagePane.getChildren().add(chat);
            }
        });
    }

    
    //////// Advanced Features - Finish /////////
    
	//////// Nested custom list cell class - Start /////////
    
    /***
     *  Nested Custom ClientCell class for the Connected Clients ListView
     * @author Take
     *
     */
    private class ClientCell extends ListCell<String>{
        
    	GridPane grid;
    	Circle circle;
        Label usernameLabel;
        Button button;
        
        /**
         * ClientCell constructor
         * @param ishost 
         */
        public ClientCell(boolean isHost) {
            super();
            
            //initialise all nodes
            init();
            
            //set the property of each child
            setProperties();
            
            //add property of all nodes
            grid.add(circle, 0, 0);
            grid.add(usernameLabel, 1, 0);
            grid.add(button, 2, 0);
            
            //set the visibility of the "KICK" button for host only
            setButtonVisibility(isHost);

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    
                	//confirm with host to kick or not using dialog
                	boolean kickConfirmed = selectHost(usernameLabel.getText());
                	System.out.println(kickConfirmed);
                	
                	//kick user once confirmed
                	if(kickConfirmed)
                		server.kickUser(usernameLabel.getText());
                }
            });
        }

        
        @Override
        public void updateItem(String username, boolean empty) {
        	super.updateItem(username, empty);

            if (empty || username == null) {
                setText(null);
                setGraphic(null);
            } 
            else 
            {
            	setUsername(username);
                setGraphic(grid);
            }
        }
        
        /**
         * Initialise all the nodes
         */
        public void init()
        {
        	grid = new GridPane();
        	circle = new Circle();
            usernameLabel = new Label();
            button = new Button();
        }
        
        /**
         * Method to set the node properties
         */
        public void setProperties()
        {
        	/*
        	 * new Insets(top, right, bottom, left)
        	 */
        	
       	 	//circle
        	circle.setFill(Color.LIME);
        	circle.setRadius(5.0);
        	GridPane.setHalignment(circle, HPos.CENTER);
        	GridPane.setValignment(circle, VPos.CENTER);
        	
        	//label of username
        	usernameLabel.setPadding(new Insets(2,2,2,0));
        	usernameLabel.setFont(new Font(15));
        	GridPane.setHalignment(usernameLabel, HPos.LEFT);
        	GridPane.setValignment(usernameLabel, VPos.CENTER);
        	
        	// by default set button visibility as false;
        	button.setVisible(false);
        	button.setText("Kick");
        	GridPane.setHalignment(button, HPos.RIGHT);
        	GridPane.setValignment(button, VPos.CENTER);
        	
        	grid.setPrefHeight(30);
       	 	grid.setPadding(new Insets(2));
       	 	
            ColumnConstraints circleCol = new ColumnConstraints();
            circleCol.setPrefWidth(20);
            
            ColumnConstraints labelCol = new ColumnConstraints();
            labelCol.setHgrow(Priority.ALWAYS);
            
            ColumnConstraints buttonCol = new ColumnConstraints();
            buttonCol.setPrefWidth(50);
            
            grid.getColumnConstraints().addAll(circleCol, labelCol, buttonCol);
        }
        
        /**
         * Method to display dialog confirming with the host if they would like to
         * kick the user or not
         * @param hosts
         * @return
         */
        public boolean selectHost(String username)
    	{	
    		ButtonType yes = new ButtonType("Yes", ButtonData.OK_DONE);
    		ButtonType no = new ButtonType("No", ButtonData.CANCEL_CLOSE);
    		
    		Alert alert = new Alert(AlertType.CONFIRMATION,
    				"", yes, no);
    		alert.setTitle("Confirmation");
    		alert.setHeaderText("Kick User Confirmation");
    		alert.setContentText("Are you sure you would like to kick " 
    				+ username + " from this session.");

    		Optional<ButtonType> result = alert.showAndWait();
    		
    		if(!result.isPresent())
    			return false;
    		
    		if (result.get() == yes)
    			return true;
    		else
    			return false;
    	}
        
        
        /**
         * method to set the user name label
         * @param username
         */
        private void setUsername(String username)
        {
        	usernameLabel.setText(username);
        }
        
        /**
         * sets the button visibility
         * @param visible
         */
        private void setButtonVisibility(boolean visible)
        {
        	button.setVisible(visible);
        }
    }
    
    //////// Nested List Cell Class - Finished ////////
    
	//////// On Mouse/Key Action Methods - Start /////////
    
    @FXML
    private void appendText(KeyEvent e)
    {
    	this.tempString += e.getCharacter();
    	addText();
    	server.sendGraphicalUpdate(mainCanvas);
    }
    
    @FXML
    private void onMousePressedListener(MouseEvent e){
        this.startX = e.getX();
        this.startY = e.getY();
        this.oldX = e.getX();
        this.oldY = e.getY();
        
        if(text)	
        	tempString = "";
    }

    
    @FXML
    private void onMouseDraggedListener(MouseEvent e){
        this.lastX = e.getX();
        this.lastY = e.getY();
        
        clearTempCanvas();
        
        if(line)
            drawLineEffect();
        if(circle)
        	drawCircleOvalEffect("circle");
        if(oval)
        	drawCircleOvalEffect("oval");
        if(rectangle)
            drawRectEffect();
        if(eraser)
        	erase();
        if(pencil)
            freeDrawing();
    }

    
    @FXML
    private void onMouseReleaseListener(MouseEvent e){
    	if(line)
            drawLine();
    	if(circle)
    		drawCircleOrOval("circle");
    	if(oval)
    		drawCircleOrOval("oval");
    	if(rectangle)
            drawRect();
    	
    	// send tcp packet here when any update happens update
    	server.sendGraphicalUpdate(mainCanvas);
    }
    
    @FXML
    private void onMouseExitedListener(MouseEvent e)
    {
    	
    }
	    
	//////// On Mouse/Key  Action Methods - Finish /////////
    
	//////// Canvas Draw Methods - Start /////////
    
    /**
     *  method to draw the line onto screen
     */
    private void drawLine()
    {
        mainGraphics.setLineWidth(sizeSlider.getValue());
        mainGraphics.setStroke(colourPick.getValue());
        mainGraphics.strokeLine(startX, startY, lastX, lastY);
    }
    
    /**
     *  method to draw either a circle or oval
     */
    private void drawCircleOrOval(String shape)
    {
    	double width = lastX - startX;
        double height = lastY - startY;
         
        if(shape.equals("circle"))
     	{
             if(width > height)
             	width = height;
             if(height > width)
             	height = width;
     	}
         
         mainGraphics.setLineWidth(sizeSlider.getValue());

         if(fillRadioBtn.isSelected())
         {
             mainGraphics.setFill(colourPick.getValue());
             mainGraphics.fillOval(startX, startY, width, height);
         }
         else
         {
             mainGraphics.setStroke(colourPick.getValue());
             mainGraphics.strokeOval(startX, startY, width, height);
         }
    }
    
    
    /**
     *  method to draw either a draw a rectangle
     */
    private void drawRect()
    {
        double wh = lastX - startX;
        double hg = lastY - startY;
        mainGraphics.setLineWidth(sizeSlider.getValue());

        if(fillRadioBtn.isSelected())
        {
            mainGraphics.setFill(colourPick.getValue());
            mainGraphics.fillRect(startX, startY, wh, hg);
        }
        else
        {
            mainGraphics.setStroke(colourPick.getValue());
            mainGraphics.strokeRect(startX, startY, wh, hg);
        }
    }

    
    /**
     *  method to write text onto screen
     */
    private void addText()
    {
    	mainGraphics.setFont(new Font(sizeSlider.getValue() * 15.0));
        mainGraphics.setFill(colourPick.getValue());
        mainGraphics.fillText(tempString, startX, startY);
    }
    
    /**
     *  method to start erasing
     */
    private void erase()
    {
    	double lineSize = sizeSlider.getValue();
    	
    	if(lineSize < 5.0)
    		lineSize = 5.0;
    	
    	mainGraphics.setLineWidth(lineSize);
        mainGraphics.setStroke(Color.WHITE);
        mainGraphics.strokeLine(lastX, lastY, lastX, lastY);
    }

    /**
     *  method to free draw with pencil
     */
    private void freeDrawing()
    {
        mainGraphics.setLineWidth(sizeSlider.getValue());
        mainGraphics.setStroke(colourPick.getValue());
        mainGraphics.strokeLine(oldX, oldY, lastX, lastY);
        oldX = lastX;
        oldY = lastY;
    }
    
    //////// Canvas Draw Methods - Finish /////////
	    
	//////// Temporary Canvas Draw Methods - Start /////////
    
    /**
     * Method to draw line
     */
    private void drawLineEffect()
    {
        drawGraphics.setLineWidth(sizeSlider.getValue());
        drawGraphics.setStroke(colourPick.getValue());
        drawGraphics.clearRect(0, 0, tempCanvas.getWidth() , tempCanvas.getHeight());
        drawGraphics.strokeLine(startX, startY, lastX, lastY);
    }
    
    /**
     * Method to draw either circle or oval
     * @param shape
     */
    private void drawCircleOvalEffect(String shape)
    {
    	double width = lastX - startX;
        double height = lastY - startY;
    	
        if(shape.equals("circle"))
        {
            if(width > height)
            	width = height;
            if(height > width)
            	height = width;
    	}
    	
    	drawGraphics.setLineWidth(sizeSlider.getValue());

        if(fillRadioBtn.isSelected())
        {
            drawGraphics.setFill(colourPick.getValue());
            drawGraphics.fillOval(startX, startY, width, height);
        }
        else
        {
            drawGraphics.setStroke(colourPick.getValue());
            drawGraphics.strokeOval(startX, startY, width, height);
        }
    }
    
    /**
     * Method to draw rectangle
     */
    private void drawRectEffect()
    {
        double wh = lastX - startX;
        double hg = lastY - startY;
        drawGraphics.setLineWidth(sizeSlider.getValue());

        if(fillRadioBtn.isSelected())
        {
            drawGraphics.setFill(colourPick.getValue());
            drawGraphics.fillRect(startX, startY, wh, hg);
        }
        else
        {
            drawGraphics.setStroke(colourPick.getValue());
            drawGraphics.strokeRect(startX, startY, wh, hg);
        }
    }
    
    
    /**
     * Method to clear the temporary canvas 
     */
    private void clearTempCanvas()
    {
    	drawGraphics.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    }
    
    
    @FXML 
    private void setStroke(ActionEvent e)
    {
        fillRadioBtn.setSelected(false);
    }
    
    
    @FXML 
    private void setFill(ActionEvent e)
    {
        strokeRadioBtn.setSelected(false);
    }
    	
	//////// Temporary Canvas Draw Methods - Finish /////////

    /// Button control
    
    @FXML
    private void setLine(ActionEvent e)
    {
        line = true;
        circle = false;
        oval = false;
        rectangle = false;
        text = false;
        pencil = false;
        eraser = false;
    }
    
    @FXML
    private void setCircle(ActionEvent e)
    {
    	line = false;
        circle = true;
        oval = false;
        rectangle = false;
        text = false;
        pencil = false;
        eraser = false;
    }
    
    @FXML
    private void setOval(ActionEvent e)
    {
    	line = false;
        circle = false;
        oval = true;
        rectangle = false;
        text = false;
        pencil = false;
        eraser = false;
    }

     
    @FXML
    private void setRectangle(ActionEvent e)
    {
    	line = false;
        circle = false;
        oval = false;
        rectangle = true;
        text = false;
        pencil = false;
        eraser = false;
    }

    
    @FXML
    private void setText(ActionEvent e)
    {
    	line = false;
        circle = false;
        oval = false;
        rectangle = false;
        text = true;
        pencil = false;
        eraser = false;
    }


    @FXML
    private void setPencil(ActionEvent e)
    {
    	line = false;
        circle = false;
        oval = false;
        rectangle = false;
        text = false;
        pencil = true;
        eraser = false;
    }
    
    
    @FXML
    private void setEraser(ActionEvent e)
    {
    	line = false;
        circle = false;
        oval = false;
        rectangle = false;
        text = false;
        pencil = false;
        eraser = true;
    }
}