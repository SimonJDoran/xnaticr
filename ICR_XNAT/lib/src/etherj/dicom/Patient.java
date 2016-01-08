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
public interface Patient extends Displayable
{

	/**
	 *
	 * @param study
	 * @return 
	 */
	public Study addStudy(Study study);

	/**
	 *
	 * @return
	 */
	public String getBirthDate();

	/**
	 *
	 * @return
	 */
	public String getComments();

	/**
	 *
	 * @return
	 */
	public String getId();

	/**
	 *
	 * @return
	 */
	public String getName();

	/**
	 *
	 * @return
	 */
	public String getOtherId();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Study getStudy(String uid);

	/**
	 *
	 * @return
	 */
	public List<Study> getStudyList();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public boolean hasStudy(String uid);

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Study removeStudy(String uid);

	/**
	 *
	 * @param birthDate
	 */
	public void setBirthDate(String birthDate);

	/**
	 *
	 * @param comments
	 */
	public void setComments(String comments);

	/**
	 *
	 * @param id
	 */
	public void setId(String id);

	/**
	 *
	 * @param name
	 */
	public void setName(String name);

	/**
	 *
	 * @param otherId
	 */
	public void setOtherId(String otherId);
}
