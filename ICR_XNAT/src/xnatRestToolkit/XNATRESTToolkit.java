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
* Java class: XNATRESTToolkit.java
* First created on Mar 17, 2009 at 12:00:35 PM
* 
* Set of tools/useful methods for sending requests to XNAT via REST
* and transforming the output that comes back.
* 
* All anticipated exceptions are handled within the class and
* rethrown as an XNATException to simplify the calling mechanism.
* 
* Note that much of this methodology is based on a custom class
* Vector2D. Since Vector itself has now long been deprecated, this
* whole methodology is much overdue for refactoring, but it has
* permeated so much of the codebase that this is a major task.
********************************************************************/

package xnatRestToolkit;

import obselete.XNATDicomParameters;
import exceptions.XMLException;
import exceptions.XNATException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import xmlUtilities.XMLUtilities;
import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.FailedToConnectException;
import generalUtilities.Vector2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import treeTable.DAOMutableTreeNode;
import treeTable.DAOTreeNodeUserObject;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;


public class XNATRESTToolkit
{
   static  Logger                logger = Logger.getLogger(XNATRESTToolkit.class);
   private XNATServerConnection  xnsc;
   private Vector2D<String>      RESTResult;
   private boolean               isLocked = false;
   private Vector<String>        columnHeadings;
   private String                thumbnailFilename;


   private String createScanXMLFilename = System.getProperty("user.home")
                                            + System.getProperty("file.separator")
                                            + ".XNATDAO/createScan.xml";

   
   /**
    * Create a new instance of the XNATToolkit. 
    * @param xnsc the currently selected connection to the XNAT database in use
    */
   public XNATRESTToolkit(XNATServerConnection xnsc)
   {
      this.xnsc = xnsc;
   }


   /**
    * Return the result of using the XNAT REST mechanism to retrieve information
    * using the URI given by RESTGetCommand.
    * Several types of response are returned by XNAT when a REST query is
    * issued, including : (1) result sets; (2) schema elements; (3) plain-text
    * responses (e.g., an ID when a PUT command completes successfully, the
    * project accessibility, the JSESSION ID); (4) file data; (5) error messages.
    * 
    * The result sets are "tables of data" with a set of column headings and a
    * set of rows, one for each returned value from the database.
    * @param RESTGetCommand - an appropriate REST URI (e.g., <code>
    * /data/archive/projects/TESTPROJ1/subjects/XNAT_S00004/experiments/XNAT_E00004?format=xml
    * </code>
    * @return A 2-D string array containing one row for each returned "match" in
    * the XNAT database and with a set of columns giving database information on
    * that match (e.g., XNAT ID, access dates, etc.) The information returned will
    * depend on the specific URI.
    * @throws exceptions.XNATException
    */
   public Vector2D<String> RESTGetResultSet(String RESTGetCommand)
                  throws XNATException
   {
      if (isLocked) throw new XNATException(XNATException.LOCKED);
      isLocked   = true;
      RESTResult = null;

      Document XNATResponseDoc    = xnsc.getDOMDocument(RESTGetCommand);
      String   XNATResponseString = xnsc.getMostRecentOutputAsString();

      if (XNATResponseDoc == null)
      {
         isLocked = false;
         throw new XNATException(XNATException.GET, xnsc.getMostRecentErrorMessage());
      }
      try
      {
         BufferedInputStream bis = new BufferedInputStream(
                                    new ByteArrayInputStream(XNATResponseString.getBytes("UTF-8")));
         parseOutputSAX(bis, new XNATGETSAXAdapter());
      }
      catch (XNATException exXNAT)
      {
         throw exXNAT;
      }
		catch (IOException exIO)
		{
			throw new XNATException(XNATException.PARSE, exIO.getMessage());
		}
      finally
      {
         isLocked = false;
      }

      return RESTResult;
   }



   /**
    * Perform a search query using the full XNAT <code>/REST/search</code>
    * mechanism.
    * @param rootElement This is the type of element (as defined in xnat.xsd)
    * that the returned fields will come from.
    * @param returnedFields These can be either elements from the xnat.xsd
    * schema (presumably lying lower down than <code>root_element</code> or
    * elements from the XNAT display.xsd document associated with <code>root_element</code>
    * and are created with the {@link XNATReturnedField} constructor.
    * @param combinationOperator - For string values the following are permissible:
    * 'LIKE' (use '%' as wildcard), =, 'IS', 'IS NOT'. For numeric & date values:
    * =,<,<=,>=,>, != (not yet added as of 11.5.09.
    * @param searchCriteria These are built up using the
    * {@link XNATSearchCriterion} constructor.
    * @param projectList List of the XNAT projects in the current profile. We
    * will search only these for data matching the other criteria.
    * @param isLazySearch Set this to true if the search should retrieve only 
    * limited information, pending interaction to expand parts of the associated
    * TreeTable in the GUI.
    * @return - A 2-D string array with one row for each returned result from the
    * XNAT database and one column for each of the desired returned fields. XNAT
    * also returns some additional columns.
    * @throws exceptions.XNATException
    * @throws java.io.IOException
    */
   public Vector2D<String> search(
                     String                rootElement,
                     XNATReturnedField[]   returnedFields,
                     String                combinationOperator,
                     XNATSearchCriterion[] searchCriteria,
                     ArrayList<String>     projectList,
                     TreeNode[]            expansionNodePath)
                     throws XNATException, IOException, Exception
   {
      // Reset the search output to null so that we don't accidentally return
      // the result of a prior search.
      if (isLocked) throw new XNATException(XNATException.LOCKED);
      isLocked   = true;
      RESTResult = null;

      String RESTGetCommand = "/data/search?format=xml";
      InputStream is;
      try
      {
         Document searchDoc = createSearchXML(rootElement,
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
         isLocked = false;
         try {is.close();} catch (IOException exIOignore) {}
      }

      return RESTResult;
   }


   
   /**
    * Low-tech method for creating the XML file required for running an
    * XNAT REST API search. The search parameters are as described for the
    * search method.
    *
    * @param rootElement
    * @param returnedFields
    * @param combinationOperator
    * @param searchCriteria
    * @throws exceptions.XNATException
    */
   public Document createSearchXML(
                     String                rootElement,
                     XNATReturnedField[]   returnedFields,
                     String                combinationOperator,
                     XNATSearchCriterion[] searchCriteria,
                     ArrayList<String>     projectList,
                     TreeNode[]            expansionNodePath)
                   throws XNATException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream           ps   = new PrintStream(baos);


      // The search XML contents is taken from the REST API usage documentation
      // at https://wiki.xnat.org/display/XNAT/Query+the+XNAT+Search+Engine+with+REST+API
      ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      ps.println("<xdat:search ID=\"\""
              +  " allow-diff-columns=\"0\""
              +  " secure=\"false\""
              +  " brief-description=\"MR Sessions\""
              +  " xmlns:xdat=\"http://nrg.wustl.edu/security\""
              +  " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

      ps.println("   <xdat:root_element_name>" + rootElement + "</xdat:root_element_name>");

      for (int i=0; i<returnedFields.length; i++)
      {
         // Kludge: In XNAT 1.6, a key change was made to the schema.
         String fieldID = returnedFields[i].fieldID;
         if ((Float.parseFloat(xnsc.XNATVersion.substring(0, 3)) < 1.6) &&
                  (returnedFields[i].fieldID.contains("startTime")))
            fieldID = returnedFields[i].fieldID.replaceFirst("startTime", "parameters/scanTime");
         ps.println("   <xdat:search_field>");
         ps.println("      <xdat:element_name>" + returnedFields[i].name     + "</xdat:element_name>" );
         ps.println("      <xdat:field_ID>"     + fieldID                    + "</xdat:field_ID>");
         ps.println("      <xdat:sequence>"     + Integer.toString(i)        + "</xdat:sequence>");
         ps.println("      <xdat:type>"         + returnedFields[i].type     + "</xdat:type>");
         ps.println("      <xdat:header>"       + "Subject"     + "</xdat:header>");
         ps.println("   </xdat:search_field>");
      }


      // The search condition section of the XML file needs to reflect three things:
      // 1. the search that the user has selected via the dialog;
      // 2. the subset of projects specified by the profile;
      // 3. the node that is being expanded in a lazy search.
      ps.println("   <xdat:search_where method=\"AND\">");
      ps.println("      <xdat:child_set method=\"" + combinationOperator + "\">");
      for (int i=0; i<searchCriteria.length; i++)
      {
         ps.println("      <xdat:criteria override_value_formatting=\"0\">");
         ps.println("         <xdat:schema_field>"    + searchCriteria[i].schemaField    + "</xdat:schema_field>");
         ps.println("         <xdat:comparison_type>" + searchCriteria[i].comparisonType + "</xdat:comparison_type>");
         ps.println("         <xdat:value>"           + searchCriteria[i].value          + "</xdat:value>");
         ps.println("      </xdat:criteria>");
      }
      ps.println("      </xdat:child_set>");
      
      // Ugly, ugly kludge: Selection by project requires looking at the relevant
      // element in the schema and so currently works only for elements that
      // can be associated with a xxxSessionData schema element. The following
      // code needs revisiting.
      String   modality   = "";
      String[] modalities = {"ct", "mr", "pet", "nm", "us", "image"};
      for (int j=0; j<modalities.length; j++)
         if (rootElement.contains(modalities[j])) modality = modalities[j];
      
      if (!modality.equals(""))
      {
         ps.println("      <xdat:child_set method=\"OR\">");
         for (int i=0; i<projectList.size(); i++)
         {
            ps.println("         <xdat:criteria override_value_formatting=\"0\">");
            ps.println("            <xdat:schema_field>xnat:" + modality + "SessionData/project</xdat:schema_field>");
            ps.println("            <xdat:comparison_type>=</xdat:comparison_type>");
            ps.println("            <xdat:value>" + projectList.get(i) + "</xdat:value>");
            ps.println("         </xdat:criteria>");
         }
         ps.println("      </xdat:child_set>");
      }
      
      if (expansionNodePath.length > 1) // I.e., not just root node.
      {
         ps.println("      <xdat:child_set method=\"AND\">");
         for (int i=1; i<expansionNodePath.length; i++)
         {
            DAOTreeNodeUserObject uo
                  = ((DAOMutableTreeNode) expansionNodePath[i]).getUserObject();
            
            String displayName = uo.getDisplayName();
            displayName = displayName.replaceAll("&", "&amp;")
                                     .replaceAll(">", "&gt;")
                                     .replaceAll("<", "&lt;")
                                     .replaceAll("%", "&#37;");
                                    
         
            ps.println("         <xdat:criteria override_value_formatting=\"0\">");
            ps.println("            <xdat:schema_field>"
                                  + searchCriteria[i-1].schemaField + "</xdat:schema_field>");
            ps.println("            <xdat:comparison_type>=</xdat:comparison_type>");
            ps.println("            <xdat:value>" + displayName + "</xdat:value>");
            ps.println("         </xdat:criteria>");
         }
         ps.println("      </xdat:child_set>");
      }   
         
      ps.println("   </xdat:search_where>");

      ps.println("</xdat:search>");


      Document searchDoc = null;
      try
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         searchDoc = XMLUtilities.getDOMDocument(bais);
      }

      catch (XMLException exXML)
      {
         logger.error("The search XML produced by XNATRESTToolkit was not well formed.");
      }

      return searchDoc;
   }


   
   /**
    * Retrieve the XNAT Server Connection used to create this instance of the toolkit
    * @return an XNAT server connection object
    */
   public XNATServerConnection getXNATServerConnection()
   {
      return xnsc;
   }


   /**
    * Return the result of using the XNAT REST mechanism to retrieve information
    * using the URI given by RESTGetCommand. This is similar to the method
	 * RESTGetResultSet. In that case, the REST client returns an
	 * XML document containing a set of <cell> elements that are formatted into
	 * a String[][] output table. By contrast, here the REST query URI returns an
	 * XML document with a large variety of elements that we might want to read.
	 * This method turns the output of that REST query into a DOM document.
    * @param RESTGetCommand
	 * @return DOM Document object representation of the result of the REST
	 * command.
    */
   public Document RESTGetDoc(String RESTGetCommand)
                   throws XNATException
   {
      if (isLocked) throw new XNATException(XNATException.LOCKED);
      isLocked   = true;
      RESTResult = null;

      Document XNATResponseDoc    = xnsc.getDOMDocument(RESTGetCommand);
      String   XNATResponseString = xnsc.getMostRecentOutputAsString();


      isLocked = false;
      if (XNATResponseDoc == null) throw
         new XNATException(XNATException.GET, xnsc.getMostRecentErrorMessage());

      return XNATResponseDoc;
	}
   
   
   
   /**
    * Return the result of a REST GET command as an input stream. This is the
    * method we will typically call to retrieve a data file from the XNAT
    * repository.
    * @param RESTGetCommand
    * @return
    * @throws XNATException 
    */
   public InputStream RESTGetFileAsStream(String RESTGetCommand)
                      throws XNATException
   {
      if (isLocked) throw new XNATException(XNATException.LOCKED);
      isLocked   = true;

      InputStream dataStream = null;
      try
      {
         dataStream = xnsc.doRESTGet(RESTGetCommand);
      }
      catch (Exception ex)
      {
         throw new XNATException(XNATException.GET, ex.getMessage());
      }
      finally
      {
         isLocked = false;
      }
      
      String   XNATResponseString = xnsc.getMostRecentOutputAsString();
      
      if (dataStream == null)
         throw new XNATException(XNATException.GET, "null response from XNAT");
      
      return dataStream;
      
   }


   /**
    * Parse the output of the XNAT REST call for an error condition.
    * At present, this is somewhat empirical and I am accumulating cases.
    *
    * @param s
    * @return true if XNAT has signalled an error
    */
   public boolean XNATRespondsWithError(String s)
   {
	   if (s.length()<10) return false;

		if (s.substring(0,10).equals("ERROR CODE")) return true;
      if (s.substring(0, 6).equals("<html>")    ) return true;
      else return false;
   }


   /**
    * Get the column headings returned by the most recent call to a GET
    * or search call to the API.
    *
    * @return the set of column headings as a String array
    */
   public Vector<String> getColumnHeadings()
   {
      return columnHeadings;
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
    * The output from, for example, the REST URI
    * <code>/REST/projects/TESTPROJ1/subjects/XNAT_S00004/experiments</code>,
    * which we obtain using {@link XNATRESTToolkit#RESTGetResultSet(java.lang.String)}
    * has almost the same format as that from the full XNAT search. However,
    * crucially, there is no <code>totalRecords</code>
    * attribute, so we don't know in advance how large our results matrix
    * should be. This requires the modification of several methods. This requires
    * an additional implementation of the {@link DefaultHandler} methods. With
    * luck, this is a temporary fix that should be resolved soon.
    */
   private class XNATGETSAXAdapter extends XNATSearchSAXAdapter
	{
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
            cellStarted = false;
            RESTResult.elementAt(row)
               .add(textBuffer == null ? "" : textBuffer.toString());
            textBuffer = null;
         }
      }

     // End of class XNATGETSAXAdapter
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
