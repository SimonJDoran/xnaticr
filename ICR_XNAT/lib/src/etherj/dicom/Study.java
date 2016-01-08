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
public interface Study extends Displayable
{

	/**
	 *
	 * @param series
	 * @return 
	 */
	public Series addSeries(Series series);

	/**
	 *
	 * @return
	 */
	public String getAccession();

	/**
	 *
	 * @return
	 */
	public String getDate();

	/**
	 *
	 * @return
	 */
	public String getDescription();

	/**
	 *
	 * @return
	 */
	public String getId();

	/**
	 *
	 * @return
	 */
	public String getModality();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Series getSeries(String uid);

	/**
	 *
	 * @return
	 */
	public List<Series> getSeriesList();

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
	public boolean hasSeries(String uid);

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Series removeSeries(String uid);

	/**
	 *
	 * @param accession
	 */
	public void setAccession(String accession);

	/**
	 *
	 * @param date
	 */
	public void setDate(String date);

	/**
	 *
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 *
	 * @param id
	 */
	public void setId(String id);

	/**
	 *
	 * @param uid
	 */
	public void setUid(String uid);

}
