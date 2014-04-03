/*****************************************************************************
 *
 * DICOMDictionaryElement.java
 *
 * Simon J Doran
 *
 * First created on August 8, 2007, 3:39 PM
 *
 *****************************************************************************/

package obselete;

public class DICOMDictionaryElement
{
	
	/** Creates a new instance of DICOMDictionaryElement, which consists of:
	 *
	 *  String	alias		-	dcm4che terminology for name with spaces removed
	 *  String	VR			-  DICOM value representation string
	 *  int		VMMin		-	DICOM value multiplicity minimum value
	 *  int     VMMax	   -  DICOM value multiplicity maximum value
	 *  String	name		-  name as per the "Registry of DICOM data elements"
	 *  boolean	retired	-	flag whether the DICOM element is retired
	 *
	 *  Note that these elements are stored in a Hashtable using the DICOM
	 *  tag, i.e., an integer representing the (group, element) description
	 *  as the key.
	 */	 
	private	String	alias;
	private	String	VR;
	private	int		VMMin;
	private	int		VMMax;
	private	String	name;
	private	boolean	retired;
	
	
	public DICOMDictionaryElement(String alias, String VR, int VMMin,
		int VMMax, String name, boolean retired)
	{
		this.alias		= alias;
		this.VR			= VR;
		this.VMMin		= VMMin;
		this.VMMax		= VMMax;
		this.name		= name;
		this.retired	= retired;
	}
	
	
	/** Get the so-called alias of a DICOM field. This is a string used by dcm4che
	 *  that broadly replicates the DICOM name, but without spaces. As such, it
	 *  is good for SQL column names, variable names, etc.
	 */
	public String getAlias()
	{
		return alias;
	}
	

	
	/** Get the DICOM value representation string, i.e., the data type. */
	public String getVR()
	{
		return VR;
	}
	
	
	
	/** Get the minimum value specified in the DICOM "value multiplicity" string.
	 *  This string has form <min>-<max> and it is more convenient to extract
	 *  the minimum and maximum values separately. For many fields, the DICOM
	 *  Registry specifies the maximum multiplicity as "n". This is translated
	 *  here to VMn = -1.
	 */
	public int getVMMin()
	{
		return VMMin;
	}
	
	
		
/** Get the maximum value specified in the DICOM "value multiplicity" string
	 (See above.) */
	public int getVMMax()
	{
		return VMMax;
	}
	
	
	
	/** Get the name of the DICOM field, as specified in the "Registry of DICOM
	 *  data elements".
	 */ 
	public String getName()
	{
		return name;
	}
	
	
	
	/** Get the retirement status of a given element. */
	public boolean getRetired()
	{
		return retired;
	}
	
}
