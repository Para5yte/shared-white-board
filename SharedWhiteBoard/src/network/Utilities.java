package network;

/***
 * 
 * @author Takemitsu Yamanaka 757038
 *
 * Network utilities class for checking port
 */
public class Utilities {
	
	/**
	 *  Check if port is formated correctly
	 *  
	 *  @param port port number to check format of
	 *  @return 	true if the port number format is correct
	 *  			false if the port number format is incorrect
	 */
	public static boolean isPortFormatCorrect(String port)
	{
		// handle string exception here
		try 
        {
        	int portNumber = Integer.parseInt (port);
        	
        	if(portNumber < 1 || portNumber > 65535)
        	{
        		return false;
        	}
        	else
        	{
        		return true;
        	}
        }
        catch (Exception e) 
        {
        	return false;
        }
	}
	
	/**
	 *  Method to reformat the word to search/add/update/remove
	 *  
	 *  @param	word	the word to format
	 *  @return			the formated word without trailing whitespaces
	 */
	public static String formatWord(String word)
	{	
		return word.trim();
	}
}
