/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat.impl;

import etherj.dicom.DataSource;
import etherj.xnat.XnatException;
import etherj.xnat.XnatResultSet;
import etherj.xnat.XnatServerConnection;
import etherj.xnat.XnatToolkit.XnatFactory;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author jamesd
 */
public class DefaultXnatFactory implements XnatFactory
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultXnatFactory.class);

	@Override
	public DataSource createDataSource(XnatServerConnection xsc)
	{
		return new XnatDataSource(xsc);
	}

	@Override
	public XnatResultSet createResultSet(Document doc)
	{
		return new DefaultXnatResultSet(doc);
	}

	@Override
	public XnatServerConnection createServerConnection()
	{
		return null;
	}

	@Override
	public XnatServerConnection createServerConnection(URL serverUrl,
		String userId, String password) throws XnatException
	{
		return new DefaultXnatServerConnection(serverUrl, userId, password);
	}
	
}
