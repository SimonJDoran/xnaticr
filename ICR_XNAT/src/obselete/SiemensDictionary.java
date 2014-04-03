/*****************************************************************************
 *
 * SiemensDictionary.java
 *
 * Simon J Doran
 *
 * First created on June 22, 2007, 11:48 AM
 *
 *****************************************************************************/

package obselete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Scanner;
import java.net.URL;

public class SiemensDictionary extends Hashtable<Long, SiemensDictionaryElement>
{
	/* Only one instance of the dictionary is ever created and is subsequently
	 * referred to by all of the SiemensImage objects.
	 */
	private static SiemensDictionary singletonDict = null;
	
   public static final int UNDEFINED   = -1;
	public static final int INT			= 1;
	public static final int STRING_cg	= 2;
	public static final int STRING_cj	= 3;
	public static final int STRING_ct	= 4;
	public static final int FLOAT			= 5;
	public static final int DOUBLE		= 6;
	public static final int SPACE			= 7;
	public static final int SHORTINT    = 8;
	
	
	/** Creates a new instance of SiemensDictionary
	 *
	 *  Keys to the hash table are four-byte integers, corresponding
	 *  to the 2 x two-byte NEMA codes in the file vis3mr.dat.
	 *
	 *  The value looked up is an object of type SiemensDictionaryElement,
	 *  which consists of:
	 *  varType			int
	 *  block			int
	 *  startPosition	int
	 *  endPosition	int
	 *  description	String
	 */
	protected SiemensDictionary() throws SiemensDictionaryException
	{
		super(750);  // Create a new hash table with initial capacity 750 elements.
		
		Scanner sc = null;
		char c;
		String s = null;
		
		java.net.URL dictDefnURL = SiemensDictionary.class.getResource("projectResources/vis3mr_newNEMA_codes.dat");
		s = dictDefnURL.getPath();
		// When the path comes back from this call, any spaces (e.g., in Windows
		// "My Documents") come back as "%20". This can be rectified by the call below.
		try
		{
			s = URLDecoder.decode(dictDefnURL.getPath(), "UTF-8");			
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new SiemensDictionaryException(SiemensDictionaryException.UNEXPECTED_ERROR);
		}

		File dictDefnFile = new File(s);

		try
		{
			sc = new Scanner(new BufferedReader(new FileReader(dictDefnFile)));
		}
		catch (FileNotFoundException ex)
		{
			throw new SiemensDictionaryException(SiemensDictionaryException.FILE_NOT_FOUND);
		}
		
		
	// Read the definition file line by line.
		int currentBlock				= 0;
		int blockNum					= 0;
		int blockStart					= 0;
		int blockEnd					= 0;
		int variableType				= -1;
		int nemaGroup					= 0;
		int nemaElement				= 0;
		int siemensIndex           = 0;
		long nemaKey					= 0;;
		String description			= "";
		String[] varTypes				= {"I", "cg", "cj", "ct", "Fs", "Fd", "s"};
		boolean missingNemaElement	= false;
		SiemensDictionaryElement sde;
		
		sc.useDelimiter("\n");
		while (sc.hasNext())
		{
			/* The first character of each line identifies the type of line
			 * C = comment, B = block description, f = field description
			 */
			sc.useDelimiter("\\s+");
			s = sc.next();
			c = s.charAt(0);
			
			switch(c)
			{
				case 'B': // Block: Need to parse this to extract the block
							 //end position for error checking below.					
					currentBlock++;
					sc.useDelimiter("\\s+");
					s = sc.next();
					blockStart = sc.nextInt();
					blockEnd   = sc.nextInt();
					s = sc.next();
					if (!s.equals("Block"))
						throw new SiemensDictionaryException(SiemensDictionaryException.PARSING_ERROR);
					blockNum   = sc.nextInt();
					if (blockNum != currentBlock)
						throw new SiemensDictionaryException(SiemensDictionaryException.PARSING_ERROR);
					break;
					
				case 'C': // Comment: Ignore the rest of the line.
					sc.useDelimiter("\n");
					s = sc.next();
					break;
					
				case 'E': // End of file - do nothing
					break;
					
				case 'f': // Field: Read in data and create new SiemensDictionaryElement.
					s = sc.findInLine("[a-zA-Z\\s]{3}");
					String varTypeString	= s.trim();
					variableType		= UNDEFINED;
					
					for (int i=0; i<7; i++)
					{
						if (varTypeString.equals(varTypes[i]))  variableType=i+1;
					}

					
					if (variableType == UNDEFINED)
						throw new SiemensDictionaryException(SiemensDictionaryException.PARSING_ERROR);

					
					sc.useDelimiter("\\s+");
					int startPosition = sc.nextInt();
					int endPosition   = sc.nextInt();
					if (	(startPosition < blockStart) ||
							(endPosition   > blockEnd)   ||
							(startPosition > endPosition) )
						throw new SiemensDictionaryException(SiemensDictionaryException.PARSING_ERROR);

					
					// The Siemens variable type code does not distinguish between
					// long and short integers, but we need to.
					if ( (variableType == 1) && ((endPosition-startPosition) == 1) )
						variableType = SHORTINT;
						
					
					s = sc.findInLine(".{23}");
					// Sometimes, there is no element on a particular line
					// and the line is just blank after the start and end positions.
					if (s == null)
					{
						description				= "<No data element>";
						missingNemaElement	= true;
					}
					else
					{
						description				= s.trim();
					   missingNemaElement	= false;
						sc.useDelimiter("-");
						String nemaGroupHex	= (sc.next()).substring(1);
						nemaGroup				= Integer.parseInt(nemaGroupHex, 16);

						sc.useDelimiter("\\s");
						String nemaElementHex = (sc.next()).substring(1);
						
						// There are a number of entries that do not have a NEMA element code.
						// This would make the item non-retrievable by
						// SiemensImage.extractNemaElement. Instead, the original file
						// vis3mr.dat has been modified to introduce new (arbitrary)
						// "NEMA" codes, starting at FFFE and working backwards.
						nemaElement = Integer.parseInt(nemaElementHex, 16);
					
						// Some lines now have an additional field entitled "Index".
						// This seems allows several items with the same NEMA reference
						// to be included.
						s = sc.findInLine("\\(");
						if (s == null)
						{							
							siemensIndex = 0xFFFF;
						}
						else
						{
							sc.useDelimiter("\\)");
							s = sc.next();
							siemensIndex = Integer.parseInt(s.trim());
							// Finally, scan and throw away the rest of the line.
							sc.useDelimiter("\\n");
							s = sc.next();
						}
												
					}
					
					sde = new SiemensDictionaryElement(variableType,
							currentBlock, startPosition, endPosition, description);
					
					// Register the new data in the dictionary hash table.
					if (!missingNemaElement)
					{
						nemaKey = ((long) nemaGroup << 32) + ((long) nemaElement << 16) + (long) siemensIndex;
						put(nemaKey, sde);
					}
														
					break;
					
				default:  // This should not happen.
					throw new SiemensDictionaryException(SiemensDictionaryException.UNEXPECTED_ERROR);

			}
				

		}
			
		sc.close();

	}
	
	public static SiemensDictionary getSiemensDictionary() throws SiemensDictionaryException
	{
		if ( singletonDict == null )
		{
			try
			{
				singletonDict = new SiemensDictionary();
			}
			catch(SiemensDictionaryException se)
			{
				throw se;
			}
		}
		
		return singletonDict;
	}
	
}
