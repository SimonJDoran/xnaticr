/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat;

import etherj.DisplayableException;

/**
 *
 * @author adminjamesd
 */
public class XnatException extends DisplayableException
{
	/**
	 * Creates a new instance of
	 * <code>XnatException</code> without detail message.
	 */
	public XnatException()
	{
	}

	/**
	 * Constructs an instance of
	 * <code>XnatException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public XnatException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructs an instance of
	 * <code>XnatException</code> with the specified detail message and
	 * cause.
	 *
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public XnatException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	/**
	 * Constructs an instance of
	 * <code>XnatException</code> with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public XnatException(Throwable cause)
	{
		super(cause);
	}

}
