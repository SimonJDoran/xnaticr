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
* Java class: SingletonListFromTwoLevelXML.java
* First created on April 10, 2010, 00:02 AM
* 
* Utility for reading and parsing a simple "two-level" XML file into
* a LinkedHashMap. The form of the source XML will be:
*
* <root>
*    <outer outAttr="outAttrText">
*       <inner inAttr="inAttrText">inText</inner>
*       <inner inAttr="inAttrText">inText</inner>
*       .
*       .
*       .
*    </outer>
*
*    <outer outAttr="outAttrText">
*       <inner inAttr="inAttrText">inText</inner>
*       <inner inAttr="inAttrText">inText</inner>
*       .
*       .
*       .
*    </outer>
*    .
*    .
*    .
* </root>
*
* and the output will go into a LinkedHashMap (linked so that the order
* of the original XML document is maintained).
*
* LinkedHashMap<String,        LinkedHashMap<String, String>>
*              <outerAttrText, <innerAttrText, innerText>>
*
**********************************************************************/

package xmlUtilities;

import exceptions.XMLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xnatDAO.XNATDAO;



/**
 *
 * @author Simon J Doran
 */
public abstract class SingletonListFromTwoLevelXML
{
	/* SingletonListFromTwoLevelXML implements the Singleton design pattern and the
    * constructor is not called directly.
	 */
   protected static Logger logger = Logger.getLogger(SingletonListFromTwoLevelXML.class);
   protected String errorMessageIOE;
   protected String errorMessageLog;
   protected String XMLResourceName;
   protected String rootName;
   protected String outer;
   protected String outAttr;
   protected String inner;
   protected String inAttr;

   protected LinkedHashMap<String, LinkedHashMap<String, String>> resultMap;


   /** Creates a new instance of DAOSearchableElementList
	 */
	protected SingletonListFromTwoLevelXML() throws IOException
	{
      // Concrete classes will provide this method, which sets the names of the
      // outer and inner elements and attributes, together with the source filename
      // and the error messages.
      setVariables();

      resultMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
      Document XMLDoc = getXMLDoc();

      NodeList nodes  = XMLDoc.getChildNodes();

      // Check that document contains what we are expecting.
      Node root = nodes.item(1);
      if ((!root.getNodeName().equals(rootName)) ||
          (nodes == null))
      {
         logger.error(errorMessageLog + " - root element name should be <" + rootName + ">.");
         throw new IOException(errorMessageIOE);
      }

      NodeList outerNodes = root.getChildNodes();
      if (outerNodes.getLength() < 1)
      {
         logger.error(errorMessageLog + " - no <" + outer + "> elements.");
         throw new IOException(errorMessageIOE);
      }

      // Scan the outer nodes for occurrences of inner nodes.
      // Note the += 2. Every entry in the XML file corresponds to an
      // element node followed by a text node.
      for (int i=1; i<outerNodes.getLength(); i+=2)
      {
         Node outerNode = outerNodes.item(i);
         if (outerNode.getNodeName().equals("#comment"))
         {
            i += 2;
            outerNode = outerNodes.item(i);
         }

         if (!outerNode.getNodeName().equals(outer))
         {
            logger.error(errorMessageLog + " - outer elements should be <" + outer + ">"
                    + " not <" + outerNode.getNodeName() + ">.");
            throw new IOException(errorMessageIOE);
         }

         if (!outerNode.hasAttributes())
         {
            logger.error(errorMessageLog + " - no " + outAttr +
                                             " attribute for <" + outer + ">.");
            throw new IOException(errorMessageIOE);
         }

         Node outAttrNode = outerNode.getAttributes().item(0);
         if (!outAttrNode.getNodeName().equals(outAttr))
         {
            logger.error(errorMessageLog + " - incorrect attribute for <" + outer
                           + ">, was expecting " + outAttr + ".");
            throw new IOException(errorMessageIOE);
         }
         String outAttrText = outAttrNode.getTextContent();

         NodeList innerNodes = outerNode.getChildNodes();
         if (innerNodes.getLength() < 1)
         {
            logger.error(errorMessageLog + " - no <" + inner
                           + "> elements for <" + outer + ">." );
            throw new IOException(errorMessageIOE);
         }


         LinkedHashMap<String, String> innerMap = new LinkedHashMap<String, String>();

         for (int j=1; j<innerNodes.getLength(); j+=2)
         {
            Node   innerNode = innerNodes.item(j);
            String inText    = innerNode.getTextContent();
            if (!innerNode.hasAttributes())
            {
               logger.error(errorMessageLog + " - no " + inAttr +
                       " attribute for <" + inner + "> = " + inText);
               throw new IOException(errorMessageIOE);
            }

            Node inAttrNode = innerNode.getAttributes().item(0);
            if (!inAttrNode.getNodeName().equals(inAttr))
            {
               logger.error(errorMessageLog + " - attributes list for <" + inner
                            + "> has incorrect format.");
               throw new IOException(errorMessageIOE);
            }

            String inAttrText = inAttrNode.getTextContent();

            innerMap.put(inAttrText, inText);
         }

         // Finally, we get to the point of all this!
         resultMap.put(outAttrText, innerMap);
      }

   }

   public LinkedHashMap<String, LinkedHashMap<String, String>> getMap()
   {
      return resultMap;
   }


   public LinkedHashMap<String, Vector<String>> getInnerTextMap()
   {
      LinkedHashMap<String, Vector<String>> innerTextMap = new LinkedHashMap<String, Vector<String>>();

      Set<Map.Entry<String, LinkedHashMap<String, String>>> mapEntries
              = resultMap.entrySet();

      for (Iterator<Map.Entry<String, LinkedHashMap<String, String>>> ime
              = mapEntries.iterator(); ime.hasNext();)
      {
         Vector<String> innerTextVector = new Vector<String>();

         Map.Entry<String, LinkedHashMap<String, String>> mapEntry = ime.next();

         LinkedHashMap<String, String> innerMap = mapEntry.getValue();
         Set<Map.Entry<String, String>> innerMapEntries = innerMap.entrySet();

         for (Iterator<Map.Entry<String, String>> jme = innerMapEntries.iterator(); jme.hasNext();)
         {
            Map.Entry<String, String> innerMapEntry = jme.next();
            innerTextVector.add(innerMapEntry.getValue());
         }

         innerTextMap.put(mapEntry.getKey(), innerTextVector);
      }

      return innerTextMap;
   }


   public LinkedHashMap<String, Vector<String>> getInnerAttrTextMap()
   {
      LinkedHashMap<String, Vector<String>> innerAttrTextMap
                                              = new LinkedHashMap<String, Vector<String>>();

      Set<Map.Entry<String, LinkedHashMap<String, String>>> mapEntries
              = resultMap.entrySet();

      for (Iterator<Map.Entry<String, LinkedHashMap<String, String>>> ime
              = mapEntries.iterator(); ime.hasNext();)
      {
         Vector<String> innerAttrTextVector = new Vector<String>();

         Map.Entry<String, LinkedHashMap<String, String>> mapEntry = ime.next();

         LinkedHashMap<String, String> innerMap = mapEntry.getValue();
         Set<Map.Entry<String, String>> innerMapEntries = innerMap.entrySet();

         for (Iterator<Map.Entry<String, String>> jme = innerMapEntries.iterator(); jme.hasNext();)
         {
            Map.Entry<String, String> innerMapEntry = jme.next();
            innerAttrTextVector.add(innerMapEntry.getKey());
         }

         innerAttrTextMap.put(mapEntry.getKey(), innerAttrTextVector);
      }

      return innerAttrTextMap;

   }

   protected Document getXMLDoc() throws IOException
   {
      InputStream resourceIs  = XNATDAO.class.getResourceAsStream(XMLResourceName);

      if (resourceIs == null)
      {
         logger.error("Couldn't find the XML resource. "
                    + "This shouldn't happen as it is supposed to be packaged with the application jar!");
         throw new IOException(errorMessageIOE);
      }

      Document          xmlDoc;
      XMLUtilities      xmlUtil          = new XMLUtilities();

      try
      {
         xmlDoc = (xmlUtil.getDOMDocument(resourceIs));
		}
		
      catch (XMLException exXML)
      {
         logger.error("Resource " + XMLResourceName + " could not be parsed. This shouldn't happen!");
         throw new IOException(errorMessageIOE);
      }

      return xmlDoc;
   }


   public abstract void setVariables();


}
