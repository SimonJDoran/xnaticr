/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import java.io.StringWriter;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author jamesd
 */
public class Xml
{
	private static final org.slf4j.Logger logger =
		LoggerFactory.getLogger(Xml.class);

	/**
	 *
	 * @param attrs
	 * @param name
	 * @return
	 */
	public static double getAttrDouble(NamedNodeMap attrs, String name)
	{
		return getAttrDouble(attrs, name, Double.NaN);
	}

	/**
	 *
	 * @param attrs
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static double getAttrDouble(NamedNodeMap attrs, String name,
		double defaultValue)
	{
		double value = defaultValue;
		Node node = attrs.getNamedItem(name);
		if (node != null)
		{
			try
			{
				value = Double.parseDouble(node.getNodeValue());
			}
			catch (NumberFormatException ex)
			{
				logger.warn("Attr: '{}' not a valid double '{}'", name,
					node.getNodeValue());
			}
		}
		return value;
	}

	/**
	 *
	 * @param attrs
	 * @param name
	 * @return
	 */
	public static int getAttrInt(NamedNodeMap attrs, String name)
	{
		return getAttrInt(attrs, name, -1);
	}

	/**
	 *
	 * @param attrs
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static int getAttrInt(NamedNodeMap attrs, String name,
		int defaultValue)
	{
		int value = defaultValue;
		Node node = attrs.getNamedItem(name);
		if (node != null)
		{
			try
			{
				value = Integer.parseInt(node.getNodeValue());
			}
			catch (NumberFormatException ex)
			{
				logger.warn("Attr: '{}' not a valid int '{}'", name,
					node.getNodeValue());
			}
		}
		return value;
	}
	
	/**
	 *
	 * @param attrs
	 * @param name
	 * @return
	 */
	public static String getAttrStr(NamedNodeMap attrs, String name)
	{
		return getAttrStr(attrs, name, null);
	}
	
	/**
	 *
	 * @param attrs
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getAttrStr(NamedNodeMap attrs, String name,
		String defaultValue)
	{
		Node node = attrs.getNamedItem(name);
		return (node != null) ? node.getNodeValue() : defaultValue;
	}

	public static String toString(Document doc)
		throws XmlException
	{
		return toString(doc, "UTF-8");
	}

	public static String toString(Document doc, String encoding)
		throws XmlException
	{
		String output = null;
		try
		{
			DOMImplementationLS ls = (DOMImplementationLS) DOMImplementationRegistry
				.newInstance().getDOMImplementation("LS");

			LSSerializer lsSerialiser = ls.createLSSerializer();
			LSOutput lsOutput = ls.createLSOutput();
			StringWriter sw = new StringWriter();
			lsOutput.setEncoding(encoding);
			lsOutput.setCharacterStream(sw);
			lsSerialiser.write(doc, lsOutput);
			output = sw.toString();
		}
		catch (ClassNotFoundException | InstantiationException |
			IllegalAccessException | ClassCastException | LSException ex)
		{
			throw new XmlException("Cannot conver Document to String", ex);
		}
		return output;
	}

	private Xml()
	{}
}
