package utilities;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

/**
 * ImageUtils class which converts a canvas to a byte array that can be 
 * sent through a socket and convert back to a Writeable image
 * @author Takemitsu Yamanaka 757037
 *
 */
public class ImageUtils {
	
	private final static String jpg = "jpg";
	private final static String png = "png";
	
	/**
	 * Method to return the byte array of canvas
	 * @param canvas
	 * @return
	 */
	public static byte[] toByteArray(Canvas canvas)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			WritableImage image = canvas.snapshot(null, null);
			BufferedImage byteImage = SwingFXUtils.fromFXImage(image, null);
			ImageIO.write(byteImage, "png", byteOut);
	        byte[] bytes = byteOut.toByteArray();
	        return bytes;	
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Method to return the Writable image of canvas from byte array
	 * @param canvas
	 * @return
	 */
	public static WritableImage toWritableImage(byte[] bytes)
	{
        try
		{
        	InputStream byteIn = new ByteArrayInputStream(bytes);
			BufferedImage byteImage = ImageIO.read(byteIn);
			WritableImage image = SwingFXUtils.toFXImage(byteImage, null);
			return image;
		} 
        catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
	/**
	 * Method to open a file and return the Writable Image
	 * @param file			file to open
	 * @return				Writable image to load to the canvas
	 * @throws IOException	exceptions
	 */
	public static WritableImage openImageFromFile(File file) throws IOException
	{
		BufferedImage byteImage = ImageIO.read(file);
		WritableImage image = SwingFXUtils.toFXImage(byteImage, null);
		return image;
	}
	
	
	/**
	 * Method to save the current canvas into a file into disk
	 * @param canvas
	 * @param file
	 * @throws IOException
	 */
	public static void saveImageToFile(Canvas canvas, File file) 
		throws IOException
	{
		WritableImage image = canvas.snapshot(null, null);
		BufferedImage byteImage;
		
		// For jpg specific Buffered image must be defined first since 
		// JPG have no alpha channel, ImageIO.write() will fail silently
		if(file.getName().endsWith(jpg))
		{
			byteImage = new BufferedImage((int)image.getWidth(), 
					(int)image.getHeight(), BufferedImage.TYPE_INT_RGB);
			// copy the image instead of returning it
			SwingFXUtils.fromFXImage(image, byteImage);
			
			ImageIO.write(byteImage, jpg, file);
			return;
		}
			
		byteImage = SwingFXUtils.fromFXImage(image, null);
		if(file.getName().endsWith(png))
			ImageIO.write(byteImage, png, file);
	}
}
