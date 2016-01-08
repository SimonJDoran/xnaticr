/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.io.File;
import java.util.Set;
import org.dcm4che2.data.DicomObject;

/**
 * $Id$
 *
 * @author adminjdarcy
 */
public interface SopInstance extends Displayable
{

	/**
	 *
	 */
	public void compact();

	/**
	 *
	 * @return
	 */
	public DicomObject getDicomObject();

	/**
	 *
	 * @return
	 */
	public File getFile();

	/**
	 *
	 * @return
	 */
	public int getInstanceNumber();

	/**
	 *
	 * @return
	 */
	public String getModality();

	/**
	 *
	 * @return
	 */
	public int getNumberOfFrames();

	/**
	 *
	 * @return
	 */
	public String getPath();

	/**
	 *
	 * @return
	 */
	public Set<String> getReferencedSopInstanceUidSet();

	/**
	 *
	 * @return
	 */
	public String getSeriesUid();

	/**
	 *
	 * @return
	 */
	public String getSopClassUid();

	/**
	 *
	 * @return
	 */
	public String getStudyUid();

	/**
	 *
	 * @return
	 */
	public String getUid();

	/**
	 *
	 * @param number
	 */
	public void setInstanceNumber(int number);

	/**
	 *
	 * @param modality
	 */
	public void setModality(String modality);

	/**
	 *
	 * @param frameCount
	 */
	public void setNumberOfFrames(int frameCount);

	/**
	 *
	 * @param uid
	 */
	public void setSeriesUid(String uid);

	/**
	 *
	 * @param uid
	 */
	public void setSopClassUid(String uid);

	/**
	 *
	 * @param uid
	 */
	public void setStudyUid(String uid);

	/**
	 *
	 * @param uid
	 */
	public void setUid(String uid);
	
}
