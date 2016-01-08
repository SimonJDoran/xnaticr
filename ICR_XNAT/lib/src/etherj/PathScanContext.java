/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import java.io.File;

/**
 *
 * @author jamesd
 * @param <T>
 */
public interface PathScanContext<T extends Object>
{
	/**
	 *
	 * @param file
	 * @param item
	 */
	public void notifyItemFound(File file, T item);

	/**
	 *
	 */
	public void notifyScanFinish();

	/**
	 *
	 */
	public void notifyScanStart();
}
