/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat.impl;

import etherj.IoUtils;
import etherj.codec.Base64;
import etherj.xnat.XnatException;
import etherj.xnat.XnatResultSet;
import etherj.xnat.XnatServerConnection;
import etherj.xnat.XnatToolkit;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jamesd
 */
public class DefaultXnatServerConnection implements XnatServerConnection
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultXnatServerConnection.class);
	private static final String DELETE = "DELETE";
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String REST_JSESSION = "/REST/JSESSION";

	private final String password;
	private String sessionId = null;
	private final XnatToolkit toolkit = XnatToolkit.getToolkit();
	private final URL url;
	private final String userId;
	// Hack to allow self-signed certs. Needs proper handling
	private final TrustManager[] trustAllCerts = new TrustManager[]
	{
		new X509TrustManager()
		{
			@Override
			public void checkClientTrusted(X509Certificate[] xcs, String string)
				throws CertificateException
			{}

			@Override
			public void checkServerTrusted(X509Certificate[] xcs, String string)
				throws CertificateException
			{}

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
			
		}
	};

	DefaultXnatServerConnection(URL serverUrl, String userId, String password)
		throws XnatException
	{
		this.url = serverUrl;
		this.userId = userId;
		this.password = password;
		try
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (NoSuchAlgorithmException | KeyManagementException ex)
		{
			throw new XnatException(ex);
		}
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
		System.out.println(pad+"Url: "+url.toString());
		System.out.println(pad+"UserId: "+userId);
		System.out.println(pad+"Password: !");
		System.out.println(pad+"SessionId: "+(sessionId != null ? sessionId : null));
	}

	@Override
	public void close()
	{
		try
		{
			delete(REST_JSESSION);
		}
		catch (IOException ex)
		{
			logger.warn("Error closing connection:", ex);
		}
		finally
		{
			sessionId = null;
		}
	}

	@Override
	public InputStream get(String command) throws IOException
	{
		HttpsURLConnection conn;
		String rest = url.toString()+command;
		conn = getHttpsConnection(new URL(rest), GET);
		conn.connect();

		logger.debug("GET "+command+" - Code: {}, Message: {}",
			conn.getResponseCode(), conn.getResponseMessage());

		InputStream is = new BufferedInputStream(conn.getInputStream());

		return is;
	}

	@Override
	public Document getDocument(String command) throws IOException, XnatException
	{
		return streamToDoc(get(command));
	}

	@Override
	public Document getDocument(String command, String xml) throws IOException,
		XnatException
	{
		return streamToDoc(post(command, xml));
	}

	@Override
	public XnatResultSet getResultSet(String command) throws IOException,
		XnatException
	{
		return toolkit.createResultSet(streamToDoc(get(command)));
	}

	@Override
	public XnatResultSet getResultSet(String command, String xml) throws
		IOException, XnatException
	{
		return toolkit.createResultSet(streamToDoc(post(command, xml)));
	}

	@Override
	public boolean isOpen()
	{
		return sessionId == null;
	}

	@Override
	public void open() throws IOException
	{
		InputStream is = null;
		try
		{
			sessionId = IoUtils.toString(post(REST_JSESSION));
		}
		catch (IOException ex)
		{
			sessionId = null;
			throw new IOException(ex);
		}
		finally
		{
			IoUtils.safeClose(is);
		}
	}

	/**
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 */
	@Override
	public InputStream post(String command) throws IOException
	{
		return post(command, null);
	}

	@Override
	public InputStream post(String command, String xml) throws IOException
	{
		HttpsURLConnection conn;
		String rest = url.toString()+command;
		conn = getHttpsConnection(new URL(rest), POST);
		if (xml != null)
		{
			conn.setDoOutput(true);
			OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
			osw.write(xml);
			osw.close();
		}
		conn.connect();

		logger.debug("POST "+command+" - Code: {}, Message: {}",
			conn.getResponseCode(), conn.getResponseMessage());

		InputStream is = new BufferedInputStream(conn.getInputStream());

		return is;
	}

	private void delete(String command) throws IOException
	{
		HttpsURLConnection conn;
		String rest = url.toString()+command;
		conn = getHttpsConnection(new URL(rest), DELETE);
		conn.connect();
		logger.debug("DELETE - Code: {}, Message: {}", conn.getResponseCode(),
			conn.getResponseMessage());
	}

	private String getAuthorization()
	{
		return "Basic "+
			new Base64().encodeToString((userId+":"+password).getBytes()).trim();
	}

	private HttpsURLConnection getHttpsConnection(URL restUrl, String type)
		throws IOException
	{
		HttpsURLConnection conn;
		conn = (HttpsURLConnection) restUrl.openConnection();
		conn.setRequestMethod(type);
		if (sessionId == null)
		{
			conn.setRequestProperty("Authorization", getAuthorization());
		}
		else
		{
			conn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
		}
		conn.setConnectTimeout(5000);
		return conn;
	}

	private Document streamToDoc(InputStream is) throws IOException, XnatException
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(is);
		}
		catch (ParserConfigurationException | SAXException ex)
		{
			throw new XnatException("Response cannot be parsed.", ex);
		}
		return doc;
	}

}
