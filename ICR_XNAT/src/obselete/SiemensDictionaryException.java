/*****************************************************************************
 *
 * SiemensDictionaryException.java
 *
 * Simon J Doran
 *
 * First created on June 26, 2007, 9:26 AM
 *
 *****************************************************************************/

package obselete;

public class SiemensDictionaryException extends Exception
{
	public static final int FILE_NOT_FOUND			= 0;
	public static final int PARSING_ERROR			= 1;
	public static final int UNEXPECTED_ERROR     = 2;
	
	private int dictionaryStatus;
	
	private static final String[] errorMessages =
	{
		"The dictionary definition file vis3mr_newNEMA_codes.dat was not found.",
		"There was a parsing error in dictionary definition file.",
		"There was an unexpected error in parsing the Siemens dictionary."
	}; 
	
	
	
	/** Creates a new instance of SiemensImageException */
	public SiemensDictionaryException(int errorCode)
	{
		super("<SJD ERROR> " + errorMessages[errorCode]);
		dictionaryStatus = errorCode;
	}
	
	
	/** Get the error code - potentially useful for taking error dependent actions */
	public int getStatus()
	{
		return dictionaryStatus;
	}
	
	
	
	/** Get a list of all the possible errors that the object can report. */
	public String[] getMessageList()
	{
		return errorMessages;
	}
	
	
	
	/** Get a detailed error message including the source of the error. */
	public String getDetailedMessage()
	{
		StackTraceElement[] st = getStackTrace();
		return "<SJD ERROR>\n" +
			"Method: " + st[0].getMethodName() + " in class " + st[0].getClassName() +
			"\n(Source: line " + st[0].getLineNumber() +
			" in file" + st[0].getFileName() + ")\n" +
			errorMessages[dictionaryStatus];		
	}	
}
