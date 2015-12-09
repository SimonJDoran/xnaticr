/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

/**
 *
 * @author jamesd
 */
public class XmlException extends DisplayableException
{
	/**
	 * Creates a new instance of
	 * <code>XmlException</code> without detail message.
	 */
	public XmlException()
	{
	}

	/**
	 * Constructs an instance of
	 * <code>XmlException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public XmlException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructs an instance of
	 * <code>XmlException</code> with the specified detail message and
	 * cause.
	 *
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public XmlException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	/**
	 * Constructs an instance of
	 * <code>XmlException</code> with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public XmlException(Throwable cause)
	{
		super(cause);
	}

}
