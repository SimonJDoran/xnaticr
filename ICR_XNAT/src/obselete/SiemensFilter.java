package obselete;
/*
 * SiemensFilter.java
 *
 * Created on June 19, 2007, 12:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author simond
 */
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SiemensFilter extends FileFilter
{
	
	/** Creates a new instance of SiemensFilter */
	public SiemensFilter()
	{		
	}
	
	public boolean accept(File f)
	{
		if (f.isDirectory()) return true;
		
		return (f.getName()).endsWith(".ima");		
	}
	
	public String getDescription()
	{
		return  "Siemens image files";
	}
	
}
