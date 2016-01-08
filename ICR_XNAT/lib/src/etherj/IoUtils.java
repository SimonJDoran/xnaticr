/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author jamesd
 */
public class IoUtils
{
	public static String toString(InputStream is)
	{
		return toString(is, "UTF-8");
	}

	public static String toString(InputStream is, String encoding)
	{
		Scanner scanner = new Scanner(is, encoding).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : null;
	}

	/**
	 *	Safely close Closeable c without throwing.
	 * @param c
	 */
	public static void safeClose(Closeable c)
	{
		try
		{
			if (c != null)
			{
				c.close();
			}
		}
		catch (IOException exIgnore)
		{}
	}

	private IoUtils()
	{}
}
