/*****************************************************************************
 *
 * DICOMDictionary.java
 *
 * Simon J Doran
 *
 * First created on August 8, 2007, 4:13 PM
 *
 *****************************************************************************/

package obselete;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.TreeMap;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

	
public class DICOMDictionary extends TreeMap<Integer, DICOMDictionaryElement>
{
	/* Only one instance of the dictionary is every created and is subsequently
	 * referred to by all of the DICOMImage objects.
	 */
	private static DICOMDictionary singletonDict = null;
	
	/* These values substitute in any expression where the DICOM value multiplicity
	 * string has an "n" in it. */	
	public static final int VMn	= 999991;
	public static final int VM2n	= 999992;
	public static final int VM3n	= 999993;	
	
	
	/** Creates a new instance of DicomDictionary
	 *
	 *  Keys to the hash table are four-byte integers (tags), corresponding
	 *  to the 2 x two-byte NEMA codes in the file vis3mr.dat.
	 *
	 *  The value looked up is an object of type DICOMDictionaryElement,
	 *  which consists of:
	 *  String	alias		-	dcm4che terminology for name with spaces removed
	 *  String	VR			-  DICOM value representation string
	 *  String	VMMin		-	DICOM value multiplicity, minimum value
	 *  String  VMMax		-  DICOM value multiplicity, maximum value
	 *  String	name		-  name as per the "Registry of DICOM data elements"
	 *  boolean retired	-	flag if the DICOM element is retired 
	 */
	protected DICOMDictionary() throws DICOMDictionaryException
	{
		super();  // Create a new TreeMap

		
		java.net.URL dictDefnURL = DICOMDictionary.class.getResource("projectResources/minimal_DICOM_dictionary.xml");
		String s = dictDefnURL.getPath();
		// When the path comes back from this call, any spaces (e.g., in Windows
		// "My Documents") come back as "%20". This can be rectified by the call below.
		try
		{
			s = URLDecoder.decode(dictDefnURL.getPath(), "UTF-8");			
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new DICOMDictionaryException(DICOMDictionaryException.UNEXPECTED_ERROR);
		}

		File dictDefnFile = new File(s);



		// The DICOM dictionary data are stored as XML courtesy of dcm4che.
		// Use a standard XML parser to extract the elements. Refer to methods
		// startElement and endElement for further details.
		try
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(dictDefnFile, new DICOMDictionarySAXAdapter());
		}
		catch (SAXException e)
		{
			throw new DICOMDictionaryException(DICOMDictionaryException.PARSING_ERROR);
		}
		catch (IOException e)
		{
			throw new DICOMDictionaryException(DICOMDictionaryException.FILE_NOT_FOUND);
		}
		
		catch (ParserConfigurationException e)
		{
			throw new DICOMDictionaryException(DICOMDictionaryException.PARSING_ERROR);
		}	
		catch (FactoryConfigurationError e)
		{
			throw new DICOMDictionaryException(DICOMDictionaryException.PARSING_ERROR);
		}


	}
	
	
	
	public static DICOMDictionary getDICOMDictionary() throws DICOMDictionaryException
	{
		if ( singletonDict == null )
		{
			try
			{
				singletonDict = new DICOMDictionary();
			}
			catch(DICOMDictionaryException dde)
			{
				throw dde;
			}
		}
		
		return singletonDict;
	}
	
	
	
	
	private final class DICOMDictionarySAXAdapter extends DefaultHandler
	{
		boolean			retired	= false;
		Integer			tag		= -1;
		int				pos		= 0;
		int				VMMin		= 0;
		int				VMMax		= 0;
		String			s1;
		String			s2;
		StringBuffer	VM			= new StringBuffer(10);
		StringBuffer	alias		= new StringBuffer(80);
		StringBuffer	name		= new StringBuffer(80);
		StringBuffer	VR			= new StringBuffer(5);
		

		@Override
		public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
		{
			if (qName.equals("element"))
			{
				tag		= (int) Long.parseLong(attributes.getValue("tag").replace(
									'x', '0'), 16);
				VM.append(attributes.getValue("vm"));				
				alias.append(attributes.getValue("alias"));			
				VR.append(attributes.getValue("vr"));
				retired	= attributes.getValue("ret").equals("RET");
			}
	   }

		
		@Override
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
			name.append(ch, start, length);
		}

		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException
		{
			if (qName.equals("element"))
			{
				/* Parse the value multiplicity string. If it does not contain a "-"
				 * then assume that the VMMin and VMMax are the same. Otherwise, set
				 * VMMin to anything on the left of the string and VMMax to anything
				 * on the right. In the special case that the right-hand string is
				 * "n", rather than a number, then use the value
				 * DICOMDictionaryElement.VMn
				 */
				s1		= VM.toString();
				pos	= s1.indexOf("-");
				if (pos == -1)  // A "-" does not occur in the VM string.
				{
					/* There are a few null fields in the DICOM header, with no
					 * data type VR or VM. */
					if (s1.length() == 0)
					{
						VMMin = 0;
						VMMax = 0;
					}
					else
					{
						try
						{
							VMMax = Integer.parseInt(s1);
						}
						catch (NumberFormatException e)
						{
							throw new SAXException(e);
						}
						VMMin = 1;
					}	
				}

				else				// A "-" does occur in the VM string.
				{	
					try
					{
						VMMin = Integer.parseInt(s1.substring(0, pos));
					}
					catch (NumberFormatException e)
					{
						throw new SAXException(e);
					}

					s2	= s1.substring(pos+1);
					if (s2.equals("n"))	VMMax = VMn;
					else if (s2.equals("2n"))	VMMax = VM2n;
					else if (s2.equals("3n"))	VMMax = VM3n;
					else
					{
						try
						{
							VMMax = Integer.parseInt(s2);
						}
						catch (NumberFormatException e)
						{
							throw new SAXException(e);
						}
					}
					
					if (VMMin > VMMax  &&  VMMax != DICOMDictionary.VMn
							&& VMMax != DICOMDictionary.VM2n && VMMax != DICOMDictionary.VM3n)
						throw new SAXException("<SJD ERROR> Value "
						+ "Multiplicity string in DICOM dictionary XML file has minimum "
						+ "value greater than maximum value");
				}
					
				
				DICOMDictionaryElement dde = new DICOMDictionaryElement(
					alias.toString(), VR.toString(), VMMin, VMMax, name.toString(), retired);

				put(tag, dde);
				
				alias.setLength(0);
				name.setLength(0);
				VR.setLength(0);
				VM.setLength(0);
				tag = -1;
			}
		}
		
	}
}
