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
public interface PatientRoot extends Displayable
{
	/**
	 *
	 * @param study
	 * @return 
	 */
	public Patient addPatient(Patient study);

	/**
	 *
	 * @param key
	 * @return
	 */
	public Patient getPatient(String key);

	/**
	 *
	 * @return
	 */
	public List<Patient> getPatientList();

	/**
	 *
	 * @param uid
	 * @return
	 */
	public boolean hasPatient(String uid);

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Patient removePatient(String uid);

}
