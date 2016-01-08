/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import etherj.concurrent.TaskMonitor;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author jamesd
 * @param <T>
 */
public interface PathScan<T extends Object>
{

	/**
	 *
	 * @param context
	 * @return 
	 */
	boolean addContext(PathScanContext<T> context);

	/**
	 * Returns the current PathScanContext or null if using the no-op context
	 * @return
	 */
	List<PathScanContext<T>> getContextList();

	/**
	 *
	 * @param context
	 * @return 
	 */
	boolean removeContext(PathScanContext<T> context);

	/**
	 *
	 * @param path
	 * @throws IOException
	 */
	void scan(String path) throws IOException;

	/**
	 *
	 * @param path
	 * @param recurse
	 * @throws IOException
	 */
	void scan(String path, boolean recurse) throws IOException;

	/**
	 *
	 * @param path
	 * @param taskMonitor
	 * @throws IOException
	 */
	void scan(String path, TaskMonitor taskMonitor) throws IOException;

	/**
	 *
	 * @param path
	 * @param recurse
	 * @param taskMonitor
	 * @throws IOException
	 */
	void scan(String path, boolean recurse, TaskMonitor taskMonitor) throws IOException;

	/**
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	T scanFile(File file) throws IOException;

}
