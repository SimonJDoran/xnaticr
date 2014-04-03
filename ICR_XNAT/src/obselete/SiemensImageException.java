/*****************************************************************************
 * SiemensImageException.java
 *
 * Simon J Doran
 *
 * First created on June 21, 2007, 11:34 AM
 *
 *****************************************************************************/

package obselete;


public class SiemensImageException extends Exception
{
	public static final int FILE_NOT_FOUND		= 0;
	public static final int NOT_SIEMENS_FILE	= 1;
	public static final int READ_ERROR			= 2;
	public static final int GT_2_DIMENSIONS	= 3;
	public static final int WRONG_NO_OF_BITS	= 4;
	public static final int WRONG_NO_OF_BYTES = 5;
	public static final int UNEXPECTED_ERROR	= 255;
	
	private int SiemensImageStatus;
	
	public static final String[] errorMessages =
	{
		"File not found",
		"The specified file does not appear to be a Siemens MR image file.",
		"The specified data element could not be read correctly. This is probably a dictionary or coding error.",
		"This format supports for only two-dimensional data.",
		"This format expects an image file with 16-bit data.",
		"There is an inconsistency between the number of data bytes calculated and the number of data bytes specified in the header",
		"There was an unexpected error."
	}; 
	
	
	/** Creates a new instance of SiemensImageException
	 *  int errorCode			- one of the values in the list above
	 *  String errorMethod	- the name of the method in which the error was thrown
	 */
	public SiemensImageException(int errorCode)
	{
		super("<SJD ERROR> " + errorMessages[errorCode]);
		SiemensImageStatus = errorCode;
	}
	
	
	/** Get the error code - potentially useful for taking error dependent actions */
	public int getStatus()
	{
		return SiemensImageStatus;
	}
	
	
	/** Get a list of all the possible errors that the object can report. */
	public String[] getMessageList()
	{
		return errorMessages;
	}
	
	
	/** Get a more detailed error message including the source of the error. */
	public String getDetailedMessage()
	{
		StackTraceElement[] st = getStackTrace();
		return "<SJD ERROR>\n" +
			"Method: " + st[0].getMethodName() + " in class " + st[0].getClassName() +
			"\n(Source: line " + st[0].getLineNumber() +
			" in file" + st[0].getFileName() + ")\n" +
			errorMessages[SiemensImageStatus];		
	}
	
}
