/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

/**
 *	$Id$
 *
 * @author adminjamesd
 */
public interface AsyncTaskResult<T>
{
	public void failure(AsyncTaskException ex);

	public void success(T t);
}
