/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;

/**
 *
 * @author jamesd
 */
public interface DataSource extends Displayable
{
	public static String DicomDatabase = "DicomDatabase";

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Series getSeries(String uid);

	/**
	 * XNAT doesn't support searching purely by UID, requires modality as well.
	 * This method can be used to avoid brute-force searching all modalities in
	 * XNAT.
	 * @param uid
	 * @param modality
	 * @return
	 */
	public Series getSeries(String uid, String modality);

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Study getStudy(String uid);
}
