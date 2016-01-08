/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;
import etherj.aim.ImageAnnotationCollection.FileUidPair;
import etherj.db.DatabaseException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface AimDatabase extends Displayable
{

	/**
	 *
	 * @param path
	 * @throws IOException
	 */
	public void importDirectory(String path) throws IOException;

	/**
	 *
	 * @param path
	 * @param recurse
	 * @throws IOException
	 */
	public void importDirectory(String path, boolean recurse) throws IOException;

	/**
	 *
	 * @param uid the Study or Series UID to search for
	 * @return
	 * @throws etherj.db.DatabaseException
	 */
	public List<FileUidPair> searchDicomUid(String uid) throws DatabaseException;

	/**
	 *
	 * @throws DatabaseException
	 */
	public void shutdown() throws DatabaseException;

	/**
	 *
	 * @param collection
	 * @throws etherj.db.DatabaseException
	 */
	public void storeCollection(ImageAnnotationCollection collection) throws DatabaseException;
}
