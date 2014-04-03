/*****************************************************************************
 *
 * DICOMDictionaryException.java
 *
 * Simon J Doran
 *
 * First created on August 9, 2007, 2:08 PM
 *
 *****************************************************************************/

package obselete;

public class DICOMDictionaryException extends Exception
{
	public static final int FILE_NOT_FOUND			= 0;
	public static final int PARSING_ERROR			= 1;
	public static final int UNEXPECTED_ERROR     = 2;
	
	private int dictionaryStatus;
	
	public static final String[] errorMessages =
	{
		"The dictionary definition file DICOM_dictionary.xml was not found.",
		"There was a parsing error in dictionary definition file.",
		"There was an unexpected error in parsing the Siemens dictionary."
	}; 
	
	
	
	/** Creates a new instance of SiemensImageException */
	public DICOMDictionaryException(int errorCode)
	{
		super("<SJD ERROR> " + errorMessages[errorCode]);
		dictionaryStatus = errorCode;
	}
	
	
	/** Get the error code - potentially useful for taking error dependent actions */
	public int getStatus()
	{
		return dictionaryStatus;
	}

	
	/** Get a list of all the possible errors that the object can throw. */
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
