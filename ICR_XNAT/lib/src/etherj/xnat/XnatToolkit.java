/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat;

import etherj.dicom.DataSource;
import etherj.xnat.impl.DefaultXnatFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author jamesd
 */
public class XnatToolkit
{
	private static final String Default = "default";
	private static final Logger logger = LoggerFactory.getLogger(
		XnatToolkit.class);
	private static final Map<String,XnatToolkit> toolkitMap = new HashMap<>();
	private final XnatFactory xnatFactory = new DefaultXnatFactory();

	static
	{
		toolkitMap.put(Default, new XnatToolkit());
	}

	/**
	 *
	 * @return
	 */
	public static XnatToolkit getDefaultToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @return
	 */
	public static XnatToolkit getToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public static XnatToolkit getToolkit(String key)
	{
		return toolkitMap.get(key);
	}

	/**
	 *
	 * @param key
	 * @param toolkit
	 * @return
	 */
	public static XnatToolkit setToolkit(String key, XnatToolkit toolkit)
	{
		XnatToolkit tk = toolkitMap.put(key, toolkit);
		logger.info(toolkit.getClass().getName()+" set with key '"+key+"'");
		return tk;
	}

	/**
	 *
	 * @param connection
	 * @return
	 */
	public DataSource createDataSource(XnatServerConnection connection)
	{
		return xnatFactory.createDataSource(connection);
	}

	public XnatResultSet createResultSet(Document doc)
	{
		return xnatFactory.createResultSet(doc);
	}

	public XnatServerConnection createServerConnection()
	{
		return xnatFactory.createServerConnection();
	}

	public XnatServerConnection createServerConnection(String serverUrl,
		String userId, String password) throws MalformedURLException, XnatException
	{
		return xnatFactory.createServerConnection(new URL(serverUrl), userId,
			password);
	}

	public XnatServerConnection createServerConnection(URL serverUrl,
		String userId, String password) throws XnatException
	{
		return xnatFactory.createServerConnection(serverUrl, userId, password);
	}

	/*
	 *	Private constructor to prevent direct instantiation
	 */
	private XnatToolkit()
	{}

	/**
	 *
	 */
	public interface XnatFactory
	{

		/**
		 *
		 * @param connection
		 * @return
		 */
		public DataSource createDataSource(XnatServerConnection connection);

		/**
		 *
		 * @param doc
		 * @return
		 */
		public XnatResultSet createResultSet(Document doc);

		/**
		 *
		 * @return
		 */
		public XnatServerConnection createServerConnection();

		/**
		 *
		 * @param serverUrl
		 * @param userId
		 * @param password
		 * @return
		 * @throws etherj.xnat.XnatException
		 */
		public XnatServerConnection createServerConnection(URL serverUrl,
			String userId, String password) throws XnatException;
	}
}
