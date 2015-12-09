/**************************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: Mar 24, 2009 at 12:01:06 PM
 *
 * Filename:      XNATDicomImportRuleException.java
 * Package:       xnat_experiments
 **************************************************/


package obselete;

import exceptions.CodedException;
import java.util.HashMap;
import java.util.Map;

public class XNATDicomImportRuleException extends CodedException
{
   public static final int IO      = 0;
   public static final int PARSING = 1;


	private static final String[] errorMessages =
	{
		"Unable to open XNAT DICOM import rule XML file.",
      "Parsing error on XNAT DICOM import rule XML file."
	};
	private static final HashMap<Integer, String> messages;
	
	static
	{
		messages = new HashMap();
	   messages.put(IO,      "Unable to open XNAT DICOM import rule XML file.");
		messages.put(PARSING, "Parsing error on XNAT DICOM import rule XML file.");
	}


	public XNATDicomImportRuleException(int diagnosticCode)
	{
		this(diagnosticCode, null);
	}

	
   public XNATDicomImportRuleException(int diagnosticCode, String upstreamMessage)
   {
      super(diagnosticCode, upstreamMessage);
   }

	
   @Override
	public Map getMessagesForAllCodes()
	{
		return messages;
	}

	
	@Override
	public String getMessageForCode()
	{
		if (!messages.containsKey(code)) return CODE_NOT_FOUND;
		return messages.get(code);
	}
}