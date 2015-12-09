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
public class ConversionException extends Exception implements Displayable
{
	/**
	 * Creates a new instance of
	 * <code>ConversionException</code> without detail message.
	 */
	public ConversionException()
	{
	}

	/**
	 * Constructs an instance of
	 * <code>ConversionException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ConversionException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructs an instance of
	 * <code>ConversionException</code> with the specified detail message and
	 * cause.
	 *
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public ConversionException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	/**
	 * Constructs an instance of
	 * <code>ConversionException</code> with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public ConversionException(Throwable cause)
	{
		super(cause);
	}

	@Override
	public void display()
	{
		display("", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display("", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Message: "+getMessage());
		Throwable cause = getCause();
		if (cause != null)
		{
			System.out.println(pad+"Cause: "+cause.getClass().getName());
		}
		StackTraceElement[] stackTrace = getStackTrace();
		for (StackTraceElement element : stackTrace)
		{
			System.out.println(indent+"      "+element.toString());
		}
	}

}
