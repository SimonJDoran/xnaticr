/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface Series extends Displayable
{
	/**
	 *
	 * @param sopInstance
	 * @return 
	 */
	public SopInstance addSopInstance(SopInstance sopInstance);

	/**
	 *
	 * @return
	 */
	public String getDescription();

	/**
	 *
	 * @return
	 */
	public String getModality();

	/**
	 *
	 * @return
	 */
	public int getNumber();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public SopInstance getSopInstance(String uid);

	/**
	 *
	 * @return
	 */
	public List<SopInstance> getSopInstanceList();

	/**
	 *
	 * @return
	 */
	public String getStudyUid();

	/**
	 *
	 * @return
	 */
	public double getTime();

	/**
	 *
	 * @return
	 */
	public String getUid();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public boolean hasSopInstance(String uid);

	/**
	 *
	 * @param uid
	 * @return
	 */
	public SopInstance removeSopInstance(String uid);

	/**
	 *
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 *
	 * @param modality
	 */
	public void setModality(String modality);

	/**
	 *
	 * @param number
	 */
	public void setNumber(int number);

	/**
	 *
	 * @param studyUid
	 */
	public void setStudyUid(String studyUid);

	/**
	 *
	 * @param time
	 */
	public void setTime(double time);
	/**
	 *
	 * @param uid
	 */
	public void setUid(String uid);
}
