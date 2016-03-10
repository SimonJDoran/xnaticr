/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: XMLUtilities.java
* First created on May 18, 2009 at 3:21:47 PM
* 
* Wrappers round various XML libraries
*********************************************************************/

package xmlUtilities;

import exceptions.XMLException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import exceptions.DataFormatException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;



public class XMLUtilities
{
   static  Logger logger = Logger.getLogger(XMLUtilities.class);

   /**
    * Wrapper round the DOM API to build a "document tree" from an XML input
    * stream. For some of the operations, it is potentially easier to use a DOM
    * rather than a SAX parser.
    *
    * @param inputStream This is typically the output of some process which then
    * becomes the input to the parser. 
    * @return A DOM "document" containing an in-memory representation of the
    * XML input stream.
    * @throws exceptions.XMLException
    */
   public static Document getDOMDocument(InputStream inputStream)
           throws XMLException
  {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(false);
      dbf.setNamespaceAware(true);
      dbf.setIgnoringElementContentWhitespace(true);

      Document doc = null;
      try
      {
         DocumentBuilder builder = dbf.newDocumentBuilder();
         builder.setErrorHandler(new DOMErrorHandler());
         InputSource is = new InputSource(inputStream);
         doc = builder.parse(is);
      }
      // Exceptions handled separately by the original code I modified this
      // from were SAXException, ParserConfigurationException and IOException.
      // However, there is no real need to handle these separately.
      catch (Exception ex)
      {
         throw new XMLException(XMLException.PARSE);
      }
      
      return doc;
   }


   /**
	 * Analyse a DOM node using XPath.
	 * This method implements a common scenario, where we wish to
	 * find all occurrences of a particular element and from each of them
	 * extract a number of attributes.
	 * @param node the parsed XML as a Node
	 * @param elementName single value required
    * @param attributeNames can contain multiple values, in which case, we
	 * search for elements containing both values.
    * @return A 2-D array, where the columns represent the different
	 * attributes specified and the rows represent occurrences of the element.
    * Note that the returned string will contain <code>null</code> if a given
    * occurrence of the element does not have one of the attributes.
	 */
	public static String[][] getAttributes(
                               Node             node,
                               NamespaceContext namespaceContext,
                               String           elementName,
										 String[]         attributeNames)
							       throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
		try
		{
			XPathExpression xpe    = xpath.compile("//" + elementName);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

      if (nodes.getLength() == 0) return null;

      String[][] attrArray = new String[nodes.getLength()][attributeNames.length];

      for (int i=0; i<nodes.getLength(); i++)
		{
         NamedNodeMap attrs = nodes.item(i).getAttributes();
         int attrLength = attrs.getLength();
         for (int j=0; j<attrLength; j++)
         {
            for (int k=0; k<attributeNames.length; k++)
               if (attributeNames[k].equals(attrs.item(j).getNodeName()))
                  attrArray[i][k] = attrs.item(j).getNodeValue();
         }
		}
		return attrArray;
	}



   /**
    * Wrapper around GetAttributes simplifying the output from a 2-D to a
    * 1-D String array for the case where we only want to get one attribute
    * per call.
    * @param node the parsed XML as a Node
	 * @param elementName single value required
    * @param attributeName single value required
    * @return A 1-D array, whose elements represent all the occurrences of the
    * element. Note that if the element occurs, but the expected attribute does
    * not, then there will be a null at this position in the array.
    */
   public static String[] getAttribute(
                             Node             node,
                             NamespaceContext namespaceContext,
                             String           elementName,
									  String           attributeName)
						        throws XMLException
	{
      String[] attributeNames = new String[1];
      attributeNames[0] = attributeName;

      String[][] attrs = getAttributes(node, namespaceContext, elementName, attributeNames);
      if (attrs == null) return null;

		String[] result = new String[attrs.length];
      for (int i=0; i<attrs.length; i++)
			result[i] = attrs[i][0];

		return result;
   }
   
   
   /**
    * Wrapper around GetAttributes simplifying the output from a 2-D to a
    * 1-D String array for the case where we only want to get one attribute
    * per call. Identical functionality to getAttribute, but returns an
    * ArrayList<String> rather than a String[].
    * @param node the parsed XML as a Node
	 * @param elementName single value required
    * @param attributeName single value required
    * @return A 1-D array, whose elements represent all the occurrences of the
    * element. Note that if the element occurs, but the expected attribute does
    * not, then there will be a null at this position in the array.
    */
   public static ArrayList<String> getAttributeA(
                                      Node             node,
                                      NamespaceContext namespaceContext,
                                      String           elementName,
											     String           attributeName)
						                 throws XMLException
	{
      String[] attributeNames = new String[1];
      attributeNames[0] = attributeName;

      String[][] attrs = getAttributes(node, namespaceContext, elementName, attributeNames);
      if (attrs == null) return null;

		ArrayList<String> result = new ArrayList<String>();
      for (int i=0; i<attrs.length; i++)
			result.add(attrs[i][0]);

		return result;
   }
   
   
   
   
   
   /**
    * Return matches for the element/attribute combination specified
    * by the arguments as an ArrayList of Boolean. 
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Boolean> getAttributeAsBooleanA(
                                       Node             node,
                                       NamespaceContext namespaceContext,
                                       String           elementName,
								               String           attributeName)
						                  throws XMLException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null) return null;
      
      ArrayList<Boolean> result = new ArrayList<Boolean>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(booleanTrueFalseParser(s[i]));
         }
         catch (DataFormatException exDF)
         {
            logger.warn("Value at position " + i + " does not evaluate to a boolean.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
   
   private static Boolean booleanTrueFalseParser(String s)
           throws DataFormatException
   {
      if (s.equals("yes")   ||
          s.equals("Yes")   ||
          s.equals("y")     ||
          s.equals("Y")     ||
          s.equals("true")  ||
          s.equals("True")  ||
          s.equals("t")     ||
          s.equals("T")     ||
          s.equals("1"))    
         return true;
      
      if (s.equals("no")    ||
          s.equals("No")    ||
          s.equals("n")     ||
          s.equals("N")     ||
          s.equals("false") ||
          s.equals("False") ||
          s.equals("f")     ||
          s.equals("F")     ||
          s.equals("0"))    
         return false;
      
      logger.warn("Not valid true/false value: " + s);
      throw new DataFormatException(DataFormatException.TRUE_FALSE, s);
   }
   
 
   
   /**
    * Extract the first match for the element/attribute combination specified
    * by the arguments and return as a Integer. Call this routine only when
    * entitled to assume that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Integer> getAttributeAsIntegerA(
                                       Node             node,
                                       NamespaceContext namespaceContext,
                                       String           elementName,
									            String           attributeName)
                                    throws XMLException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null) return null;
      
      ArrayList<Integer> result = new ArrayList<Integer>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(new Integer(s[i]));
         }
         catch (NumberFormatException exNF)
         {
            logger.warn("Value at position " + i + " does not evaluate to an integer.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
   
/**
    * Extract the first match for the element/attribute combination specified
    * by the arguments and return as a Float. Call this routine only when
    * entitled to assume that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Float> getAttributeAsFloatA(
                                     Node             node,
                                     NamespaceContext namespaceContext,
                                     String           elementName,
								             String           attributeName)
                                  throws XMLException, NumberFormatException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null) return null;
      
      ArrayList<Float> result = new ArrayList<Float>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(new Float(s[i]));
         }
         catch (NumberFormatException exNF)
         {
            logger.warn("Value at position " + i + " does not evaluate to an integer.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
   
   /**
    * Extract the first match for the element/attribute combination specified
    * by the arguments. Call this routine only when entitled to assume
    * that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return the attribute text or null if the attribute does not exist
    * @throws XMLException 
    */
   public static String getFirstAttribute(
                           Node             node,
                           NamespaceContext namespaceContext,
                           String           elementName,
									String           attributeName)
						      throws XMLException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null) return null;
      else return s[0];
   }


   
   /**
    * Extract the first match for the element/attribute combination specified
    * by the arguments and return as a Boolean. Call this routine only when
    * entitled to assume that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static Boolean getFirstAttributeAsBoolean(
                            Node             node,
                            NamespaceContext namespaceContext,
                            String           elementName,
								    String           attributeName)
						       throws XMLException, DataFormatException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s[0] == null) return null;
      return booleanTrueFalseParser(s[0]);
   }
   
   
   /**
    * Extract the first match for the element/attribute combination specified
    * by the arguments and return as a Integer. Call this routine only when
    * entitled to assume that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static Integer getFirstAttributeAsInteger(
                            Node             node,
                            NamespaceContext namespaceContext,
                            String           elementName,
									 String           attributeName)
           throws XMLException, NumberFormatException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null)    return null;
      if (s[0] == null) return null;
      else return new Integer(s[0]);
   }
   
   
   
/**
    * Extract the first match for the element/attribute combination specified
    * by the arguments and return as a Float. Call this routine only when
    * entitled to assume that there should be only one matching element.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param attributeName
    * @return
    * @throws XMLException
    */
   public static Float getFirstAttributeAsFloat(
                          Node             node,
                          NamespaceContext namespaceContext,
                          String           elementName,
								  String           attributeName)
           throws XMLException, NumberFormatException
   {
      String[] s = getAttribute(node, namespaceContext, elementName, attributeName);
      if (s == null)    return null;
      if (s[0] == null) return null;
      else return new Float(s[0]);
   }
   
   
   
   

   
   /**
    * Given a node containing an element with key-value pairs, extract them into a map.
    * @param node
    * @param namespaceContext
    * @param element the containing element
    * @param keyValueElement the element containing an individual key-value pairs 
    * @param keyName name of the attribute containing the keys
    * @param valueName name of the attribute containing the values
    * @return map containing key-value pairs
    * @throws XMLException 
    */
   public static SortedMap<String, String> getKeyValue(
                                            Node             node,
                                            NamespaceContext namespaceContext,
                                            String           element,
                                            String           keyValueElement,
                                            String           keyName,
                                            String           valueName)
                                   throws XMLException
   {
      TreeMap<String, String> keyValue = new TreeMap<String, String>();
      
      try
      {
         ArrayList<String> keys   = getXPathResultA(node, namespaceContext,
                                            "//" + element + "/" + keyValueElement
                                            + "/@" + keyName);
         
         ArrayList<String> values = getXPathResultA(node, namespaceContext,
                                            "//" + element + "/" + keyValueElement
                                            + "/@" + valueName);
         
         if (keys == null)   return null;
         if (keys.isEmpty()) return null;
         for (int i=0; i<keys.size(); i++)
         {
            keyValue.put(keys.get(i), values.get(i) );
         }
      }
      catch (XMLException exXML)
      {
         throw exXML;
      }     
      
      return keyValue;
   }
   
   
   
   
   /**
    * Given a node containing an element with delimited float values, parse
    * into an ArrayList.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param delimiter
    * @return
    * @throws XMLException
    * @throws NumberFormatException 
    */
   public static ArrayList<Float> getFloatArrayListFromElementCSV(
                                     Node             node,
                                     NamespaceContext namespaceContext,
                                     String           elementName,
                                     String           delimiter)
                                  throws XMLException, NumberFormatException
   {
      // N.B. We assume here that there is at most one matching element
      // in a valid MRIW_RECORD file.
      String[] entries = getElementText(node, namespaceContext, elementName);
      if (entries == null) return null;
      
      String[] sa = entries[0].split(delimiter);
      ArrayList result = new ArrayList<Float>();
      for (String s : sa) result.add(new Float(s));
      
      return result;
   }
   
   
   
/**
    * Given a node containing an element with delimited String values, parse
    * into an ArrayList.
    * @param node
    * @param namespaceContext
    * @param elementName
    * @param delimiter
    * @return
    * @throws XMLException
    * @throws NumberFormatException 
    */
   public static ArrayList<String> getStringArrayListFromElementCSV(
                                      Node             node,
                                      NamespaceContext namespaceContext,
                                      String           elementName,
                                      String           delimiter)
                                   throws XMLException, NumberFormatException
   {
      // N.B. We assume here that there is at most one matching element
      // in a valid MRIW_RECORD file.
      String[] entries = getElementText(node, namespaceContext, elementName);
      if (entries == null) return null;
      
      String[] sa = entries[0].split(delimiter);
      ArrayList<String> result = new ArrayList<String>();
      result.addAll(Arrays.asList(sa));
      
      return result;
   }
   
   
   /**
	 * Given a DOM Node, analyse it using XPath.
    * This method implements a common scenario, where we wish to return
	 * find all occurrences of a particular element.
	 * @param node the parsed XML as a Node
	 * @param elementName single value required
    * @return List of nodes containing the successive occurrences.
    * If the element does not occur, then the method returns null.
	 */
	public static NodeList getElement(
                             Node             node,
                             NamespaceContext namespaceContext,
                             String           elementName)
						        throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
		
		try
		{
			XPathExpression xpe    = xpath.compile("//" + elementName);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

      int len = nodes.getLength();
      if (len == 0) return null;
      return nodes;
	}
   
   
   
   /**
	 * Given a DOM Node, analyse it using XPath to obtain element text items.
    * This method implements a common scenario, where we wish to
	 * find all occurrences of a particular element and from each of them
	 * return the text. Note: this will return every occurrence in the whole
    * Node and is retained for compatibility with previous usage.
	 * @param node the parsed XML as a Node
    * @param namespaceContext
	 * @param elementName single value required
    * @return String array containing the text value of successive occurrences
    * of the element, or <code>null</code> if the element does not occur.
	 */
	public static String[] getElementText(
                             Node             node,
                             NamespaceContext namespaceContext,
                             String           elementName)
						        throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
		try
		{
			XPathExpression xpe    = xpath.compile("//" + elementName);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

      int len = nodes.getLength();
      if (len == 0) return null;

      String[] result = new String[len];
      for (int i=0; i<len; i++) result[i] = nodes.item(i).getTextContent();

		return result;
	}
   
   
   /**
	 * Given a DOM Node, analyse it using XPath to obtain element text items.
    * This method implements a common scenario, where we wish to
	 * find all occurrences of a particular element and from each of them
	 * return the text. The only difference from getElementText() above is that
    * the results are returned as an ArrayList.
	 * @param node the parsed XML as a Node
    * @param namespaceContext
	 * @param elementName single value required
    * @return String array containing the text value of successive occurrences
    * of the element, or <code>null</code> if the element does not occur.
	 */
   public static ArrayList<String> getElementTextA(
                                      Node             node,
                                      NamespaceContext namespaceContext,
                                      String           elementName)
						                 throws XMLException
   {
      String[] s = getElementText(node, namespaceContext, elementName);
      if (s == null) return null;
      
      ArrayList<String> result = new ArrayList<String>();
      result.addAll(Arrays.asList(s));
      
      return result;
   }
           
           
           
   /**
    * Return matches for the element specified as an ArrayList of Boolean. 
    * @param node
    * @param namespaceContext
    * @param elementName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Boolean> getElementTextAsBooleanA(
                                       Node             node,
                                       NamespaceContext namespaceContext,
                                       String           elementName,
								               String           attributeName)
						                  throws XMLException
   {
      String[] s = getElementText(node, namespaceContext, elementName);
      if (s == null) return null;
      
      ArrayList<Boolean> result = new ArrayList<Boolean>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(booleanTrueFalseParser(s[i]));
         }
         catch (DataFormatException exDF)
         {
            logger.warn("Value at position " + i + " does not evaluate to a boolean.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
   /**
    * Return matches for the element specified as an ArrayList of Integer. 
    * @param node
    * @param namespaceContext
    * @param elementName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Integer> getElementTextAsIntegerA(
                                       Node             node,
                                       NamespaceContext namespaceContext,
                                       String           elementName,
								               String           attributeName)
						                  throws XMLException
   {
      String[] s = getElementText(node, namespaceContext, elementName);
      if (s == null) return null;
      
      ArrayList<Integer> result = new ArrayList<Integer>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(new Integer(s[i]));
         }
         catch (NumberFormatException exNF)
         {
            logger.warn("Value at position " + i + " does not evaluate to an integer.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
   /**
    * Return matches for the element specified as an ArrayList of Integer. 
    * @param node
    * @param namespaceContext
    * @param elementName
    * @return
    * @throws XMLException
    */
   public static ArrayList<Float> getElementTextAsFloatA(
                                       Node             node,
                                       NamespaceContext namespaceContext,
                                       String           elementName,
								               String           attributeName)
						                  throws XMLException
   {
      String[] s = getElementText(node, namespaceContext, elementName);
      if (s == null) return null;
      
      ArrayList<Float> result = new ArrayList<Float>();
      for (int i=0; i<s.length; i++)
      {
         try
         {
            result.add(new Float(s[i]));
         }
         catch (NumberFormatException exNF)
         {
            logger.warn("Value at position " + i + " does not evaluate to an integer.");
            result.add(null);
         }
      }
      
      return result;
   }
   
   
           
   /**
    * Get elements relative to the current node and return as a Nodelist
    * @param node
    * @param namespaceContext
    * @param elementName
    * @return a Nodelist of the elements with the given name
    * @throws XMLException 
    */
   public static NodeList getElementsRelative(Node             node,
                                              NamespaceContext namespaceContext,
                                              String           elementName)
						        throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
		try
		{
			XPathExpression xpe    = xpath.compile("child::" + elementName);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

      int len = nodes.getLength();
      if (len == 0) return null;
      return nodes;
	}
   
   
   
   /**
    * Analyse the given node using a full XPath expression to produce
    * the resulting matches as text.
    * 
    * @param node
    * @param namespaceContext
    * @param xpathExpr
    * @return a String array containing the text value of successive occurrences
    * of the elements obtained by executing the xpath expression
    * @throws XMLException
    */
   public static String[] getXPathResult(
                             Node             node,
                             NamespaceContext namespaceContext,
                             String           xpathExpr)
						        throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
      
      try
		{
			XPathExpression xpe    = xpath.compile(xpathExpr);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

      int len = nodes.getLength();
      if (len == 0) return null;
      
      String[] result = new String[len];
      for (int i=0; i<len; i++) result[i] = nodes.item(i).getTextContent();

		return result;
	}
   
   
   
   /**
    * Analyse the given node using a full XPath expression to produce
    * the resulting matches as text. Same functionality as getXPathResult, but
    * returns and ArrayList
    * 
    * @param node
    * @param namespaceContext
    * @param xpathExpr
    * @return an ArrayList containing the text value of successive occurrences
    * of the elements obtained by executing the xpath expression
    * @throws XMLException
    */
   public static ArrayList<String> getXPathResultA(
                                      Node         node,
                                      NamespaceContext namespaceContext,
                                      String           xpathExpr)
						                 throws XMLException
   {
      String[] result = getXPathResult(node, namespaceContext, xpathExpr);
      if (result == null) return null;
      
      ArrayList<String> resultList = new ArrayList<String>();
      resultList.addAll(Arrays.asList(result));
      
      return resultList;
   }
   
   
   /**
    * Given a document, analyse it using a full XPath expression to produce
    * the resulting matches as a NodeList.
    * 
    * @param node
    * @param namespaceContext
    * @param xpathExpr
    * @return a String array containing the text value of successive occurrences
    * of the elements obtained by executing the xpath expression
    * @throws XMLException
    */
   public static NodeList getXPathResultAsNodeList(
                             Node             node,
                             NamespaceContext namespaceContext,
                             String           xpathExpr)
						        throws XMLException
	{
		XPathFactory xpf	 = XPathFactory.newInstance();
		XPath			 xpath = xpf.newXPath();
      xpath.setNamespaceContext(namespaceContext);
		NodeList		 nodes;
      
      try
		{
			XPathExpression xpe    = xpath.compile(xpathExpr);
			Object          result = xpe.evaluate(node, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (Exception ex)
		{
			throw new XMLException(XMLException.PARSE);
		}

		return nodes;
	}
   
   
   
   /**
    * Analyse the given node using a full XPath expression and return the first
    * match as a String.
    * 
    * @param node
    * @param namespaceContext
    * @param xpathExpr
    * @return a String array containing the text value of successive occurrences
    * of the elements obtained by executing the xpath expression
    * @throws XMLException
    */
   public static String getFirstXPathResult(
                           Node             node,
                           NamespaceContext namespaceContext,
                           String           xpathExpr)
						      throws XMLException
   {
      String[] s = getXPathResult(node, namespaceContext, xpathExpr);
      if (s == null) return null;
      else return s[0];
   }

 


  
   /**
    * Dump a DOM document to a String, which might then be sent, for example
    * to and OutputStreamWriter.
    *
    * @param doc
    * @param encoding
    * @return String containing entire contents of XML document
    * @throws XMLException
    */
   public static String dumpDOMDocument(Document doc, String encoding)
                        throws XMLException
   {
      String docString = null;

      try
      {
         DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

         DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");

         LSSerializer   lss = impl.createLSSerializer();
         LSOutput       lso = impl.createLSOutput();
         StringWriter   sw  = new StringWriter();
         lso.setEncoding(encoding);
         lso.setCharacterStream(sw);
         lss.write(doc, lso);
         docString = sw.toString();
      }
      catch (Exception ex)
      {
         throw new XMLException(XMLException.OUTPUT, ex.getMessage());
      }

      return docString;
   }


   public static String dumpDOMDocument(Document doc) throws XMLException
   {
      // Default to UTF-8.
      return dumpDOMDocument(doc, "UTF-8");
   }

}
