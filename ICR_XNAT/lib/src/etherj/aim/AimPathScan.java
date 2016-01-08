/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.AbstractPathScan;
import etherj.XmlException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jamesd
 */
public class AimPathScan extends AbstractPathScan<ImageAnnotationCollection>
{

	@Override
	public ImageAnnotationCollection scanFile(File file) throws IOException
	{
		try
		{
			return XmlParser.parse(file);
		}
		catch (XmlException ex)
		{
			throw new IOException(ex.getCause());
		}
	}
	
}
