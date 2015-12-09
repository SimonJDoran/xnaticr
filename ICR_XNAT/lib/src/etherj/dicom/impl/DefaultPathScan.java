/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.AbstractPathScan;
import etherj.dicom.DicomUtils;
import java.io.File;
import java.io.IOException;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * $Id$
 * 
 * Searches paths for DICOM image data
 *
 * @author James d'Arcy
 */
class DefaultPathScan extends AbstractPathScan<DicomObject>
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultPathScan.class);

	/**
	 * Constructor
	 *
	 */
	public DefaultPathScan()
	{
	}

	@Override
	public DicomObject scanFile(File file) throws IOException
	{
		if (!file.isFile() || !file.canRead())
		{
			logger.warn("Not a file or cannot be read: {}", file.getPath());
			return null;
		}

		DicomObject dcm = null;
		try
		{
			dcm = DicomUtils.readDicomFile(file);
		}
		catch (IOException exIO)
		{
			logger.warn("Cannot scan file: "+file.getPath(), exIO);
			throw exIO;
		}
		// Ignore null or presentation state
		if (dcm != null)
		{ 
			String uid = dcm.getString(Tag.SOPClassUID);
			if ((uid == null) || uid.startsWith("1.2.840.10008.5.1.4.1.1.11"))
			{
				return null;
			}
		}

		return dcm;
	}

}
