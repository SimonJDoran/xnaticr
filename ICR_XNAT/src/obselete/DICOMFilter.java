/*****************************************************************************
 *
 * DICOMfilter.java
 *
 * Simon J Doran
 *
 * First created on August 3, 2007, 2:53 PM
 *
 *****************************************************************************/

package obselete;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class DICOMFilter extends FileFilter
{
	
	/** Creates a new instance of SiemensFilter */
	public DICOMFilter()
	{		
	}
	
	public boolean accept(File f)
	{
		if (f.isDirectory()) return true;
		
		return (f.getName()).endsWith(".dcm");		
	}
	
	public String getDescription()
	{
		return  "DICOM image files";
	}
	
}
