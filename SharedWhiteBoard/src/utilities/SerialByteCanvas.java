package utilities;

import java.io.Serializable;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Serialised Graphics class that can be sent through sockets
 */

@SuppressWarnings("serial")
public class SerialByteCanvas implements Serializable{

	private byte[] byteImage;
	
	public SerialByteCanvas(Canvas canvas)
	{
		this.byteImage = ImageUtils.toByteArray(canvas);
	}

	/**
	 * @return the byteImage
	 */
	public byte[] getByteImage()
	{
		System.out.println(byteImage.length);
		return byteImage;
	}

	/**
	 * @param byteImage the byteImage to set
	 */
	public void setByteImage(byte[] byteImage)
	{
		this.byteImage = byteImage;
	}
	
	/**
	 * Convert the byte[] into a write able image
	 * @return
	 */
	public WritableImage toWriteableImage()
	{
		return ImageUtils.toWritableImage(byteImage);
	}
	
	
}
