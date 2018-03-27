/*******************************************************************
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
********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: RESTSearchWorker.java
* First created on Apr 29, 2010, 10:55:36 AM
* 
* Wrapper routine to allow a potentially long XNAT search to be
* conducted in a worker thread. 
*********************************************************************/


package xnatRestToolkit;

import exceptions.FailedToConnectException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.SwingWorker;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATDAO;


public class RESTSearchWorker extends SwingWorker<Vector2D<String>, Void>
{
   static  Logger                      logger = Logger.getLogger(RESTSearchWorker.class);
   private final XNATDAO               xnd;
   private final XNATServerConnection  xnsc;
   private final String                rootElement;
   private final XNATReturnedField[]   returnedFields;
   private final String                combinationOperator;
   private final XNATSearchCriterion[] searchCriteria;
   private final ArrayList<String>     projectList;
   private final TreeNode[]            expansionNodePath;
   private Vector2D                    RESTResult;
   private Vector<String>              columnHeadings;;

   
   public RESTSearchWorker(
           XNATDAO               xnd,
           XNATServerConnection  xnsc,
           String                rootElement,
           XNATReturnedField[]   returnedFields,
           String                combinationOperator,
           XNATSearchCriterion[] searchCriteria,
           ArrayList<String>     projectList,
           TreeNode[]            expansionNodePath)
   {
      super();
      this.xnd                 = xnd;
      this.xnsc                = xnsc;
      this.rootElement         = rootElement;
      this.returnedFields      = returnedFields;
      this.combinationOperator = combinationOperator;
      this.searchCriteria      = searchCriteria;
      this.projectList         = projectList;
      this.expansionNodePath   = expansionNodePath;
      this.RESTResult          = null;
   }


   
   @Override
   protected Vector2D<String> doInBackground() throws Exception
   {
      logger.debug("RESTSearchWorker started");
      
      String RESTGetCommand = "/data/search?format=xml";
      InputStream is;
      try
      {
         XNATRESTToolkit xnrt       = new XNATRESTToolkit(xnsc);
         
         // Temporary code for calculating stats.
         if (expansionNodePath.length == 4)
         {
            TreeNode[] expPath2 = new TreeNode[2];
            expPath2[0] = expansionNodePath[0];
            expPath2[1] = expansionNodePath[1];
            
            Document statsSearchDoc = xnrt.createSearchXML(rootElement,
                                                           returnedFields,
                                                           combinationOperator,
                                                           searchCriteria,
                                                           projectList,
                                                           expPath2);
            // This is going to be a big file!
            is = xnsc.doRESTPost(RESTGetCommand, statsSearchDoc);
            
            try
            {
               parseOutputSAX(new BufferedInputStream(is), new XNATSearchSAXAdapter());
            }

            catch (XNATException exXNAT){throw exXNAT;}

            catch (IOException exIO){throw exIO;}

            finally
            {
               try {is.close();} catch (IOException exIOignore) {}
            }

            
            Map<String, Map<String, List<String>>> statsTree = new HashMap<>();
            
            for (int j=0; j<RESTResult.size(); j++)
            {
               String manufacturer = (String) RESTResult.atom(1, j);
               String scanType     = (String) RESTResult.atom(2, j);
               String patientName  = (String) RESTResult.atom(3, j);
               
               if (!statsTree.containsKey(manufacturer))
                  statsTree.put(manufacturer, new HashMap<String, List<String>>());
               
               Map<String, List<String>> m = statsTree.get(manufacturer);
               if (!m.containsKey(scanType))
                  m.put(scanType, new ArrayList<String>());
               
               List<String> l = m.get(scanType);
               l.add(patientName);       
            }
            
            System.out.println("Output for stats here.");
         }
         Document        searchDoc  = xnrt.createSearchXML(rootElement,
                                                           returnedFields,
                                                           combinationOperator,
                                                           searchCriteria,
                                                           projectList,
                                                           expansionNodePath);
         is = xnsc.doRESTPost(RESTGetCommand, searchDoc);
      }
      catch (FailedToConnectException exFTC)
      {
         throw new XNATException(XNATException.SEARCH_CREATE, exFTC.getMessage());
      }
      catch (XMLException exXML)
      {
         throw new XNATException(XNATException.PARSE, exXML.getMessage());
      }

      try
      {
         parseOutputSAX(new BufferedInputStream(is), new XNATSearchSAXAdapter());
      }

      catch (XNATException exXNAT){throw exXNAT;}

      catch (IOException exIO){throw exIO;}

      finally
      {
         try {is.close();} catch (IOException exIOignore) {}
      }
      
      return RESTResult;
   }
   
   
   @Override
   public void done()
   {
      logger.debug("RESTSearchWorker exited via done().");
   }


   /**
    * Parse the XML output of an XNAT REST query. The main function of this
    * method is as a wrapper around the SAXParser API. It is called by both
    * {@link XNATRESTToolkit#RESTGetResultSet(java.lang.String)} and
    * {@link XNATRESTToolkit#search(java.lang.String, xnat_experiments.XNATReturnedField[], java.lang.String, xnat_experiments.XNATSearchCriterion[])}.
    *
    * @param XNATOutput
    * @param SAXAdapter
    * @throws exceptions.XNATException
    * @throws java.io.IOException
    */
   private void parseOutputSAX(
                     BufferedInputStream XNATOutput,
                     DefaultHandler SAXAdapter)
                throws XNATException, IOException
   {
      // Use a standard XML parser to extract the elements. Refer to methods
      // startElement and endElement for further details.
      try
      {
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
         parser.parse(XNATOutput, SAXAdapter);
      }
      catch (Exception ex)
      {
         throw new XNATException(XNATException.PARSE, ex.getMessage());
      }
      finally
      {
         try
         {
            XNATOutput.close();
         }
         catch (IOException exIO)
         {
            throw new XNATException(XNATException.STREAM_CLOSE);
         }
      }
   }



   /**
    * An extension of the {@link DefaultHandler} class to provide the context-
    * specific parsing of XML input streams that result from XNAT searches.
    */
   private class XNATSearchSAXAdapter extends DefaultHandler
	{
      protected  int            nRecords      = 0;
      protected  int            nColumns      = 0;
      protected  int            row           = -1;
      protected  int            cell          = -1;
      protected  boolean        cellStarted   = false;
      protected  boolean        columnStarted = false;
      protected  StringBuffer   textBuffer    = null;
      protected  Locator        locator;


      @Override
      public void setDocumentLocator(Locator locator)
      {
         this.locator = locator;
      }

		@Override
		public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
		{
         int         col   = -1;
         int         line  = -1;

         if (locator != null)
         {
            col  = locator.getColumnNumber();
            line = locator.getLineNumber();
         }

			if (qName.equals("ResultSet"))
			{
            nRecords = (int) Integer.parseInt(attributes.getValue("totalRecords"));
         }

         if (qName.equals("columns"))
         {
            columnHeadings = new Vector<String>();
         }

         if (qName.equals("column"))
         {
            nColumns     += 1;
            columnStarted = true;
         }

         if (qName.equals("row"))
         {
            row += 1;
            cell = -1;
            if (row >= nRecords)
               throw new SAXException("Incorrect number of records at " + line
                                       + " of search output.");
            RESTResult.add(new Vector<String>());
         }

         if (qName.equals("cell"))
         {
            cell       += 1;
            cellStarted = true;
            if (cell > nColumns)
               throw new SAXException("Incorrect number of cells (columns) at " + line
                                       + " of search output.");
         }

	   }


      @Override
      public void characters(char[] ch, int start, int length)
      {
         /* The characters is slightly tricky as there is no guarantee that it
          * will be called only once per element! Sometimes, SAXParser builds up
          * the element value by multiple calls. I'm sure there is a good
          * reason ...? Hence, we use a string buffer and "read it out" when the
          * endElement method for cell is closed.
          */
         if (cellStarted || columnStarted )
         {
             String s = new String(ch, start, length);
             if (textBuffer == null) textBuffer = new StringBuffer(s);
             else textBuffer.append(s);
         }
      }



		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException
		{
			if (qName.equals("columns"))
         {
            RESTResult = new Vector2D<String>();
         }

         if (qName.equals("column"))
         {
            columnStarted = false;
            if (textBuffer != null) columnHeadings.add(textBuffer.toString());
            textBuffer = null;
         }

         if (qName.equals("cell"))
         {
            cellStarted             = false;
            RESTResult.elementAt(row)
               .add(textBuffer == null ? "" : textBuffer.toString());
            textBuffer              = null;
         }
      }

   }  // End of class XNATSearchSAXAdapter
 
}
