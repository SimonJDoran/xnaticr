/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat;

import etherj.Displayable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Document;

/**
 *
 * @author jamesd
 */
public interface XnatServerConnection extends Closeable, Displayable
{
	@Override
	public void close();

	/**
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public InputStream get(String command) throws IOException;

	/**
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws XnatException
	 */
	public Document getDocument(String command) throws IOException, XnatException;

	/**
	 *
	 * @param command
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws XnatException
	 */
	public Document getDocument(String command, String xml) throws IOException,
		XnatException;

	/**
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws XnatException
	 */
	public XnatResultSet getResultSet(String command) throws IOException,
		XnatException;

	/**
	 *
	 * @param command
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws XnatException
	 */
	public XnatResultSet getResultSet(String command, String xml) throws
		IOException, XnatException;

	public boolean isOpen();

	/**
	 *
	 * @throws IOException
	 */
	public void open() throws IOException;

	/**
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public InputStream post(String command) throws IOException;

	/**
	 *
	 * @param command
	 * @param xml
	 * @return
	 * @throws IOException
	 */
	public InputStream post(String command, String xml) throws IOException;

}
