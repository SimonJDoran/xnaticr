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

import exceptions.SJDException;


public class XNATDicomImportRuleException extends SJDException
{
   public static final int IO      = 0;
   public static final int PARSING = 1;


	private static final String[] errorMessages =
	{
		"Unable to open XNAT DICOM import rule XML file.",
      "Parsing error on XNAT DICOM import rule XML file."
	};


	public XNATDicomImportRuleException(int errorCode)
	{
		super(errorCode);
	}

   public XNATDicomImportRuleException(int errorCode, String upstreamMessage)
   {
      super(errorCode, upstreamMessage);
   }

   @Override
	public String[] getMessageList()
	{
		return errorMessages;
	}
}
