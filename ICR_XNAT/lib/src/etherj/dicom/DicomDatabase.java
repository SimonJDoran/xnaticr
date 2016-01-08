/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import etherj.db.DatabaseException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface DicomDatabase extends Displayable
{
	/**
	 *
	 * @param path
	 * @throws java.io.IOException
	 */
	public void importDirectory(String path) throws IOException;

	/**
	 *
	 * @param path
	 * @param recurse
	 * @throws java.io.IOException
	 */
	public void importDirectory(String path, boolean recurse) throws IOException;

	/**
	 *
	 * @param specification
	 * @return
	 */
	public PatientRoot search(SearchSpecification specification);

	/**
	 *
	 * @param query
	 * @return
	 */
	public PatientRoot search(String query);

	/**
	 *
	 * @param uid
	 * @return
	 * @throws etherj.db.DatabaseException
	 */
	public SopInstance searchInstance(String uid) throws DatabaseException;

	/**
	 *
	 * @param specification
	 * @return
	 * @throws DatabaseException
	 */
	public List<SopInstance> searchInstance(SearchSpecification specification)
		throws DatabaseException;

	/**
	 *
	 * @param specification
	 * @param referencedUids
	 * @return
	 * @throws DatabaseException
	 */
	public List<SopInstance> searchInstance(SearchSpecification specification,
		Collection<String> referencedUids) throws DatabaseException;

	/**
	 *
	 * @throws DatabaseException
	 */
	public void shutdown() throws DatabaseException;

	/**
	 *
	 * @param sopInst
	 * @throws etherj.db.DatabaseException
	 */
	public void storeInstance(SopInstance sopInst) throws DatabaseException;

	/**
	 *
	 * @param patient
	 * @throws etherj.db.DatabaseException
	 */
	public void storePatient(Patient patient) throws DatabaseException;
}
