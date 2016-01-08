/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

/**
 *
 * @author adminjamesd
 */
public class AsyncTaskException extends Exception
{

	/**
	 * Creates a new instance of
	 * <code>AsyncTaskException</code> without detail message.
	 */
	public AsyncTaskException()
	{
	}

	/**
	 * Constructs an instance of
	 * <code>AsyncTaskException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public AsyncTaskException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructs an instance of
	 * <code>AsyncTaskException</code> with the specified detail message
	 * and cause.
	 *
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public AsyncTaskException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	/**
	 * Constructs an instance of
	 * <code>AsyncTaskException</code> with the specified detail message
	 * and cause.
	 *
	 * @param cause the cause.
	 */
	public AsyncTaskException(Throwable cause)
	{
		super(cause);
	}

}
