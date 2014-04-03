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
* Note that much of this methodology is based on a custom class
* Vector2D. Since Vector itself has now long been deprecated, this
* whole methodology is much overdue for refactoring, but it has
* permeated so much of the codebase that this is a major task.
********************************************************************/

package obselete;

import xnatRestToolkit.*;
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
import xmlUtilities.DelayedPrettyPrinterXmlWriter;


/**
 * This class contains a number of utility functions to allow easier interaction
 * with the XNAT REST client. Note that all anticipate exceptions are handled
 * within the class and rethrown as an XNATException to simplify the calling
 * mechanism.
 */
public class OldVersionOfXNATRESTToolkit
{
   static  Logger                logger = Logger.getLogger(OldVersionOfXNATRESTToolkit.class);
   private XNATServerConnection  xnsc;
   private Vector2D<String>      RESTResult;
   private boolean               isLocked = false;
   private Vector<String>        columnHeadings;
   private String                thumbnailFilename;


   private String createScanXMLFilename = System.getProperty("user.home")
                                            + System.getProperty("file.separator")
                                            + ".XNATDAO/createScan.xml";

   


   /**
    * Create a new instance of the XNATToolkit, which will 
    * @param xnsc the currently selected connection to the XNAT database in use
    */
   public OldVersionOfXNATRESTToolkit(XNATServerConnection xnsc)
   {
      this.xnsc = xnsc;
   }


   /**
    * Return the result of using the XNAT REST mechanism to retrieve information
    * using the URI given by RESTGetCommand.
    * @param RESTGetCommand - an appropriate REST URI (e.g., <code>
    * /REST/projects/TESTPROJ1/subjects/XNAT_S00004/experiments/XNAT_E00004?format=xml
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
    * @param rootElement - This is the type of element (as defined in xnat.xsd)
    * that the returned fields will come from.
    * @param returnedFields - These can be either elements from the xnat.xsd
    * schema (presumably lying lower down than <code>root_element</code> or
    * elements from the XNAT display.xsd document associated with <code>root_element</code>
    * and are created with the {@link XNATReturnedField} constructor.
    * @param combinationOperator - For string values the following are permissible:
    * 'LIKE' (use '%' as wildcard), =, 'IS', 'IS NOT'. For numeric & date values:
    * =,<,<=,>=,>, != (not yet added as of 11.5.09.
    * @param searchCriteria - These are built up using the
    * {@link XNATSearchCriterion} constructor.
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
                     ArrayList<String>     projectList)
                     throws XNATException, IOException, Exception
   {
      // Reset the search output to null so that we don't accidentally return
      // the result of a prior search.
      if (isLocked) throw new XNATException(XNATException.LOCKED);
      isLocked   = true;
      RESTResult = null;

      String RESTGetCommand = "/REST/search?format=xml";
      InputStream is;
      try
      {
         Document searchDoc = createSearchXML(rootElement,
                                              returnedFields,
                                              combinationOperator,
                                              searchCriteria,
                                              projectList);
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


//   public RESTSearchWorker searchInBackground(
//                     String                rootElement,
//                     XNATReturnedField[]   returnedFields,
//                     String                combinationOperator,
//                     XNATSearchCriterion[] searchCriteria,
//                     ArrayList<String>     projectList)
//                     throws XNATException, IOException
//   {
//      RESTSearchWorker worker = new RESTSearchWorker(xnsc,
//                                                     rootElement,
//                                                     returnedFields,
//                                                     combinationOperator,
//                                                     searchCriteria,
//                                                     projectList);
//      try
//      {
//         worker.execute();
//      }
//      catch (Exception ex)
//      {
//         if (ex instanceof IOException)   throw (IOException)   ex;
//         if (ex instanceof XNATException) throw (XNATException) ex;
//
//         else throw new XNATException(XNATException.SEARCH, ex.getMessage());
//      }
//
//      return worker;
//   }



   /**
    * Low-tech method for creating the XML file required for running an
    * XNAT REST API search
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
                     ArrayList<String>     projectList)
                throws XNATException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream           ps   = new PrintStream(baos);


      // The search XML contents is taken from the REST API usage documentation
      // at http://nrg.wikispaces.com/XNAT+REST+API+Usage.
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
         if ((Float.parseFloat(xnsc.getVersion().substring(0, 3)) < 1.6) &&
             (returnedFields[i].fieldID.equals("xnat:mrScanData/parameters/startTime")))
            fieldID = "xnat:mrScanData/parameters/scanTime";
         ps.println("   <xdat:search_field>");
         ps.println("      <xdat:element_name>" + returnedFields[i].name     + "</xdat:element_name>" );
         ps.println("      <xdat:field_ID>"     + fieldID                    + "</xdat:field_ID>");
         ps.println("      <xdat:sequence>"     + Integer.toString(i)        + "</xdat:sequence>");
         ps.println("      <xdat:type>"         + returnedFields[i].type     + "</xdat:type>");
         ps.println("      <xdat:header>"       + "Subject"     + "</xdat:header>");
         ps.println("   </xdat:search_field>");
      }


      // The search condition section of the XML file needs to reflect both the
      // search that the user has selected and the available projects.
      ps.println("   <xdat:search_where method=\"AND\">");
      ps.println("      <xdat:child_set method=\"" + combinationOperator + "\">");
      for (int i=0; i<searchCriteria.length; i++)
      {
         ps.println("         <xdat:criteria override_value_formatting=\"0\">");
         ps.println("         <xdat:schema_field>"    + searchCriteria[i].schemaField    + "</xdat:schema_field>");
         ps.println("         <xdat:comparison_type>" + searchCriteria[i].comparisonType + "</xdat:comparison_type>");
         ps.println("         <xdat:value>"           + searchCriteria[i].value          + "</xdat:value>");
         ps.println("      </xdat:criteria>");
      }
      ps.println("      </xdat:child_set>");
      
      ps.println("      <xdat:child_set method=\"OR\">");
      for (int i=0; i<projectList.size(); i++)
      {
         ps.println("         <xdat:criteria override_value_formatting=\"0\">");
         ps.println("         <xdat:schema_field>xnat:mrSessionData.PROJECT</xdat:schema_field>"); 
         ps.println("         <xdat:comparison_type>=</xdat:comparison_type>");
         ps.println("         <xdat:value>" + projectList.get(i) + "</xdat:value>");
         ps.println("      </xdat:criteria>");
      }
      ps.println("      </xdat:child_set>");
      ps.println("   </xdat:search_where>");

      ps.println("</xdat:search>");


      Document searchDoc = null;
      try
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         searchDoc = (new XMLUtilities()).getDOMDocument(bais);
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
    * Parse the output of the XNAT REST Client OS call for an error condition.
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



//   /**
//    * Given a subject name and an XNAT project to put it in, create the new
//    * subject in the XNAT database.
//    *
//    * @param XNATSubjectLabel
//    * @param XNATProjectName
//    * @throws exceptions.XNATException
//    * @return the XNAT subject ID as a String
//    */
//   public String createSubject(
//                     String XNATProjectName,
//                     String XNATSubjectLabel)
//			        throws XNATException
//   {
//      int      L = commonCommandArgs.length;
//      String[] command = new String[L+6];
//      String   output  = null;
//
//      System.arraycopy(commonCommandArgs, 0, command , 0, commonCommandArgs.length);
//      command[L]   = "-user_session";
//      command[L+1] = JSessionID;
//      command[L+2] = "-m";
//      command[L+3] = "PUT";
//      command[L+4] = "-remote";
//      command[L+5] = "/REST/projects/" + XNATProjectName
//                     + "/subjects/"      + XNATSubjectLabel;
//
//
//      try
//      {
//         OSProcessManager osp = new OSProcessManager();
//         osp.initiateProcessAndWait(command);
//         output = osp.getProcessOutputAsString();
//      }
//      catch (OSProcessManagerException exOS)
//      {
//         throw new XNATException(XNATException.SUBJECT_CREATE,
//                 ": " + exOS.getMessage());
//      }
//
//      if (XNATRespondsWithError(output))
//         throw new XNATException(XNATException.SUBJECT_CREATE, ": " + output);
//
//      return output;
//   }
//
//
//
//
//   /**
//    * Use the XNAT REST API to create a new XNAT session under an existing
//    * project and subject.
//    * @param project
//    * @param XNATSubjectID
//    * @param DICOMPatientName
//    * @param XNATExperimentLabel
//    * @param DICOMStudyUID
//    * @param modality
//    * @param date
//    * @return the XNAT experiment ID as a String
//    * @throws exceptions.XNATException
//    */
//   public String createSession(
//                     String XNATProjectName,
//                     String XNATSubjectID,
//                     String DICOMPatientName,
//                     String XNATExperimentLabel,
//                     String DICOMStudyUID,
//                     String modality,
//                     String date)
//                 throws XNATException
//   {
//      int      L       = commonCommandArgs.length;
//      String[] command = new String[L+6];
//      String   output  = null;
//
//      System.arraycopy(commonCommandArgs, 0, command , 0, commonCommandArgs.length);
//      command[L]   = "-user_session";
//      command[L+1] = JSessionID;
//      command[L+2] = "-m";
//      command[L+3] = "PUT";
//      command[L+4] = "-remote";
//
//      // Start with support for MR, PET and CT - add more as and when data available.
//      String sessionType;
//      sessionType = "xnat:imageSessionData";
//      if (modality.equals("CT")) sessionType = "xnat:ctSessionData";
//      if (modality.equals("MR")) sessionType = "xnat:mrSessionData";
//      if (modality.equals("PT")) sessionType = "xnat:petSessionData";
//
//      command[L+5] = "/REST/projects/"                        + XNATProjectName
//                   + "/subjects/"                             + XNATSubjectID
//                   + "/experiments/"                          + XNATExperimentLabel
//                   + "?xsiType="                              + sessionType
//                   + "&xnat:imageSessionData/dcmPatientName=" + DICOMPatientName
//                   + "&xnat:imageSessionData/UID="            + DICOMStudyUID
//                   + "&xnat:imageSessionData/modality="       + modality;
//
//
//      try
//      {
//         OSProcessManager osp = new OSProcessManager();
//         osp.initiateProcessAndWait(command);
//         output = osp.getProcessOutputAsString();
//      }
//      catch (OSProcessManagerException exOS)
//      {
//         throw new XNATException(XNATException.SESSION_CREATE,
//                 ": " + exOS.getMessage());
//      }
//
//      if (XNATRespondsWithError(output))
//         throw new XNATException(XNATException.SESSION_CREATE, ": " + output);
//
//      return output;
//   }
//
//
//
//   /**
//    * Use the XNAT REST API to create a new XNAT scan under an existing
//    * project, subject and experiment.
//    * @param XNATProjectName
//    * @param XNATSubjectID
//    * @param XNATExperimentID
//    * @param XNATScanLabel
//    * @param xndp
//    * @return
//    * @throws exceptions.XNATException
//    */
//	public String createScan(
//                     String XNATProjectName,
//                     String XNATSubjectID,
//                     String XNATExperimentID,
//                     String XNATScanLabel,
//                     XNATDicomParameters xndp)
//					  throws XNATException
//   {
//      int      L       = commonCommandArgs.length;
//      String[] command = new String[L+8];
//      String   output  = null;
//
//      System.arraycopy(commonCommandArgs, 0, command , 0, commonCommandArgs.length);
//      command[L]   = "-user_session";
//      command[L+1] = JSessionID;
//      command[L+2] = "-m";
//      command[L+3] = "PUT";
//      command[L+4] = "-remote";
//
//
//
//      command[L+5] = "/REST/projects/"            + XNATProjectName
//                   + "/subjects/"                 + XNATSubjectID
//                   + "/experiments/"              + XNATExperimentID
//				  	 	 + "/scans/"                    + XNATScanLabel
//				 		 + "?xsiType="                  + XNATScanDataType(xndp);
//
//      command[L+6] = "-local";
//
//      command[L+7] = createScanXMLFilename;
//
//      try
//      {
//         createScanXML(xndp);
//
//         OSProcessManager osp = new OSProcessManager();
//         osp.initiateProcessAndWait(command);
//         output = osp.getProcessOutputAsString();
//      }
//      catch (Exception ex)
//      {
//         throw new XNATException(XNATException.SCAN_CREATE,
//                 ": " + ex.getMessage());
//      }
//
//
//      if (XNATRespondsWithError(output))
//         throw new XNATException(XNATException.SCAN_CREATE, ": " + output);
//
//      /* For some reason, XNAT does not appear to return the scan ID here,
//		 * but it would appear that the scan ID is, in fact, the user-generated
//		 * label.
//		 */
//		return XNATScanLabel;
//   }
//
//
//
//	/**
//    * Use the XNAT REST API to create a new resource, with relevant tags.
//    * E.g., for a single scan, we can create a DICOM resource with a UID tag
//    * and a THUMBNAIL resource to contain JPEG thumbnails.
//    * @param XNATProjectName
//    * @param XNATSubjectID
//    * @param XNATExperimentID
//    * @param XNATScanLabel
//    * @param XNATResourceName
//    * @param XNATTags
//    * @throws XNATException
//    */
//   public void createTaggedResource(
//                     String XNATProjectName,
//                     String XNATSubjectID,
//                     String XNATExperimentID,
//                     String XNATScanLabel,
//                     String XNATResourceName,
//                     String XNATTags)
//					  throws XNATException
//   {
//      int      L       = commonCommandArgs.length;
//      String[] command = new String[L+6];
//      String   output  = null;
//
//      System.arraycopy(commonCommandArgs, 0, command , 0, commonCommandArgs.length);
//      command[L]   = "-user_session";
//      command[L+1] = JSessionID;
//      command[L+2] = "-m";
//      command[L+3] = "PUT";
//      command[L+4] = "-remote";
//
//      command[L+5] = "/REST/projects/" + XNATProjectName
//                   + "/subjects/"      + XNATSubjectID
//                   + "/experiments/"   + XNATExperimentID
//				  	 	 + "/scans/"         + XNATScanLabel
//				 		 + "/resources/"     + XNATResourceName
//                   + ((XNATTags != null) ? "?tags="+XNATTags : "");
//
//      try
//      {
//         OSProcessManager osp = new OSProcessManager();
//         osp.initiateProcessAndWait(command);
//         output = osp.getProcessOutputAsString();
//      }
//      catch (Exception ex)
//      {
//         throw new XNATException(XNATException.RESOURCE_CREATE,
//                 ": " + ex.getMessage());
//      }
//
//
//      if (XNATRespondsWithError(output))
//         throw new XNATException(XNATException.RESOURCE_CREATE, ": " + output);
//   }
//
//
//
//
//   /**
//    * Insert a DICOM file into the XNAT database.
//    * @param dcm the data to be inserted
//    * @param xndp the object giving easy access to common DICOM parameters
//    * @param XNATProjectName
//    * @param DICOMStudyUID
//    * @param DICOMInstanceUID
//    * @param catalogCache a hashtable caching useful data required during upload.
//    * This saves having to call the XNAT REST API too many times and should speed
//    * up the process.
//    * @throws exceptions.XNATException
//    */
//   public void uploadDicomFile(
//                     String XNATProjectName,
//                     XNATDicomParameters xndp,
//                     Hashtable<String, XNATCacheElement> catalogCache,
//                     File   dcmFile)
//				   throws XNATException
//   {
//		String   XNATExperimentID;
//		Vector<String>	XNATSubjectIDLabel;
//		String   XNATScanLabel;
//
//		String   DICOMStudyUID       = xndp.getParameter("StudyUID");
//      String   DICOMSeriesUID      = xndp.getParameter("SeriesUID");
//      String   DICOMSOPInstanceUID = xndp.getParameter("SOPInstanceUID");
//
//      String[] XNATResourceNames   = {"DICOM", "THUMBNAIL"};
//      String[] XNATResourceTags    = {"SOPInstanceUID", "JPEG"};
//
//		try
//		{
//         XNATSubjectIDLabel = createSubjIfNecessary(XNATProjectName,
//                                                    xndp,
//                                                    catalogCache);
//
//			XNATExperimentID   = createExptIfNecessary(XNATProjectName,
//																    XNATSubjectIDLabel,
//                                                    xndp,
//																	 catalogCache);
//
//         XNATScanLabel      = createScanIfNecessary(XNATProjectName,
//                                                    xndp,
//                                                    catalogCache);
//
//         createResourcesIfNecessary(
//            XNATProjectName,
//            XNATSubjectIDLabel.elementAt(0),
//            XNATExperimentID,
//            XNATScanLabel,
//            XNATResourceNames,
//            XNATResourceTags);
//
//         BufferedImage thumbnail = xndp.getThumbnail(64, 64);
//
//         uploadDCM(
//            XNATProjectName,
//            XNATSubjectIDLabel.elementAt(0),
//            XNATExperimentID,
//            XNATScanLabel,
//            removeIllegalCharacters(xndp.getParameter("Scan_Type")),
//            dcmFile.getAbsolutePath(),
//            dcmFile.getName(),
//            DICOMStudyUID,
//            DICOMSeriesUID,
//            DICOMSOPInstanceUID,
//            catalogCache,
//            thumbnail);
//		}
//		catch (XNATException exXNAT)
//		{
//			throw exXNAT;
//		}
////      catch (ImageUtilitiesException exIU)
////      {
////         throw new XNATException(XNATException.THUMB_CREATE);
////      }
//   }
//
//
//
//
///**
// * This is the method that actually does the uploading. It is called by
// * uploadDicomFile, which does the groundwork of assembling all the parameters
// * below.
// * @param XNATProjectName
// * @param XNATSubjectID
// * @param XNATExperimentID
// * @param XNATScanID
// * @param scanType
// * @param dcmFullPath
// * @param dcmFilename
// * @param DICOMStudyUID
// * @param DICOMSeriesUID
// * @param DICOMSOPInstanceUID
// * @param catalogCache
// * @throws exceptions.XNATException
// */
//   private void uploadDCM(
//                     String XNATProjectName,
//                     String XNATSubjectID,
//                     String XNATExperimentID,
//                     String XNATScanLabel,
//                     String scanType,
//                     String dcmFullPath,
//                     String dcmFilename,
//                     String DICOMStudyUID,
//                     String DICOMSeriesUID,
//                     String DICOMSOPInstanceUID,
//                     Hashtable<String, XNATCacheElement> catalogCache,
//                     BufferedImage thumbnail)
//                throws XNATException
//   {
//      int      L       = commonCommandArgs.length;
//      String[] command = new String[L+8];
//      String   output  = null;
//
//      System.arraycopy(commonCommandArgs, 0, command , 0, commonCommandArgs.length);
//      command[L]   = "-user_session";
//      command[L+1] = JSessionID;
//      command[L+2] = "-m";
//      command[L+3] = "PUT";
//      command[L+4] = "-remote";
//      command[L+5] = "/REST/projects/"       + XNATProjectName
//                   + "/subjects/"            + XNATSubjectID
//                   + "/experiments/"         + XNATExperimentID
//						 + "/scans/"               + XNATScanLabel
//                   + "/files/"               + dcmFilename
//						 + "?format=DICOM"
//                   + "&content="             + scanType
//                   + "&tags=SOPInstanceUID:" + DICOMSOPInstanceUID;
//      command[L+6] = "-local";
//      command[L+7] = dcmFullPath;
//
//
//      try
//      {
//         OSProcessManager osp = new OSProcessManager();
//         osp.initiateProcessAndWait(command);
//         output = osp.getProcessOutputAsString();
//      }
//      catch (OSProcessManagerException exOS)
//      {
//         throw new XNATException(XNATException.FILE_UPLOAD,
//                 ": " + exOS.getMessage());
//      }
//
//      if (XNATRespondsWithError(output))
//         throw new XNATException(XNATException.FILE_UPLOAD, ": " + output);
//
//
//      // Having uploaded the DICOM, we now need to update the internal cache.
//      XNATCacheElement ce = catalogCache.get(DICOMStudyUID);
//      if (ce == null) throw new XNATException(XNATException.CACHE);  // Shouldn't happen
//
//      int index = ce.DICOMSeriesUIDs.indexOf(DICOMSeriesUID);
//      if (index == -1) throw new XNATException(XNATException.CACHE); // Shouldn't happen
//
//		ce.DICOMSOPInstanceUIDs.elementAt(index).add(DICOMSOPInstanceUID);
//
//
//      /* Now upload the thumbnail image. Note that since XNAT is expecting to
//       * deal with data in the form of a file, we need to save the data in JPEG
//       * format to a temporary file, before XNAT copies it into the repository.
//       */
//      try
//      {
//  //       ImageIO.write(thumbnail, "jpg", new File());
//      }
//      catch (Exception ex)
//      {
//         throw new XNATException(XNATException.THUMB_CREATE, ex.getMessage());
//      }
//   }
//
//
//
//
//
//
//
//	/**
//	 * Private method called by uploadDicomFile, which returns the XNAT subject
//	 * ID corresponding to the DICOM file being loaded, creating a new subject
//	 * if necessary.
//	 * @param XNATProjectName
//	 * @param xndp
//	 * @param catalogCache
//	 * @return two-element array, consisting of the XNAT subject ID and XNAT
//	 * subject label
//	 * @throws exceptions.XNATException
//	 */
//	private Vector<String> createSubjIfNecessary(
//                     String XNATProjectName,
//						   XNATDicomParameters xndp,
//						   Hashtable<String, XNATCacheElement> catalogCache)
//			           throws XNATException
//	{
//		String XNATSubjectLabel = removeIllegalCharacters(xndp.getParameter("Subject"));
//
//		/* Before calling on the XNAT REST API, use the cache to try and find
//		 * the subject, as this is quicker.
//		 */
//		XNATCacheElement ce = null;
//		boolean found = false;
//		for (Enumeration<XNATCacheElement> e = catalogCache.elements();
//				e.hasMoreElements();)
//		{
//			ce = e.nextElement();
//			if (ce.XNATSubjectLabel.equals(XNATSubjectLabel))
//			{
//				found = true;
//				break;
//			}
//		}
//
//		if (found)
//      {
//         Vector<String> v = new Vector<String>();
//         v.add(ce.XNATSubjectID);
//         v.add(ce.XNATSubjectLabel);
//         return v;
//      }
//
//
//		/* The subject of this DICOM file is not in the cache, so has not yet
//		 * been encountered during this run of XNAT DICOMLoader. Now check
//		 * the XNAT database to see if it is there.
//		 */
//		String RESTURI = "/REST/projects/" + XNATProjectName + "/subjects?format=xml";
//		Vector2D<String> subjectList;
//		try
//		{
//			subjectList = RESTGetResultSet(RESTURI);
//		}
//		catch (XNATException exXNAT)
//		{
//			throw new XNATException(XNATException.SUBJ_LIST);
//		}
//
//		int index = subjectList.indexOfForCol(0, XNATSubjectLabel);
//		if (index != -1) return subjectList.getRow(index);
//
//
//
//		/* The subject of this DICOM file is not in this project in the
//		 * XNAT database either, so go ahead and create a new subject.
//		 */
//      String XNATSubjectID = null;
//		try
//		{
//			XNATSubjectID = createSubject(XNATProjectName, XNATSubjectLabel);
//		}
//		catch (XNATException exXNAT)
//		{
//			throw exXNAT;
//		}
//
//      Vector<String> v = new Vector<String>();
//      v.add(XNATSubjectID);
//      v.add(XNATSubjectLabel);
//      return v;
//	}
//
//
//
//
//	/**
//	 * Private method called by uploadDicomFile, which returns the XNAT
//	 * Experiment ID corresponding to the DICOM file being loaded,
//	 * creating a new experiment if necessary.
//	 * @param XNATProjectName
//	 * @param XNATSubjectID
//	 * @param xndp
//	 * @param catalogCache
//	 * @return
//	 * @throws exceptions.XNATException
//	 */
//	private String createExptIfNecessary(
//                     String XNATProjectName,
//							Vector<String> XNATSubjectIDLabel,
//                     XNATDicomParameters xndp,
//							Hashtable<String, XNATCacheElement> catalogCache)
//			         throws XNATException
//	{
//		String DICOMStudyUID    = xndp.getParameter("StudyUID");
//		XNATCacheElement ce     = catalogCache.get(DICOMStudyUID);
//		if (ce != null) return ce.XNATExperimentID;
//
//      String modality         = xndp.getParameter("Modality");
//      String DICOMPatientName = xndp.getParameter("Subject");
//
//      String XNATExperimentID = null;
//      try
//		{
//       /* In order to create a new experiment/session, we need to supply a label.
//       * Ideally, this would be a meaningful piece of text entered by the user,
//       * but this is not possible when the upload is happening in batch mode.
//       * Hence, we create a pseudo-label out of two pieces of information that
//       * we can access from the DICOM.
//       */
//         String date                = formatDate(xndp.getParameter("Date"));
//         String experimentType      = xndp.getParameter("Experiment");
//         String XNATExperimentLabel = removeIllegalCharacters(
//													experimentType + "_" + date);
//
//			XNATExperimentID = createSession(XNATProjectName,
//                                          XNATSubjectIDLabel.elementAt(0),
//                                          DICOMPatientName,
//                                          XNATExperimentLabel,
//                                          DICOMStudyUID,
//                                          modality,
//                                          date);
//		}
//      catch (NumberFormatException exNF)
//      {
//         throw new XNATException(XNATException.DATE);
//      }
//      catch (XNATException exXNAT)
//      {
//         throw exXNAT;
//      }
//
//		// Having created a new study, we need to record its details in the cache.
//      ce = new XNATCacheElement(DICOMStudyUID,
//										  XNATExperimentID,
//										  XNATSubjectIDLabel.elementAt(0),
//										  XNATSubjectIDLabel.elementAt(1),
//										  null,
//										  null,
//										  null); // Individual scan details not yet known.
//		catalogCache.put(DICOMStudyUID, ce);
//
//      return XNATExperimentID;
//	}
//
//
//
//
//	/**
//	 * Private method called by uploadDicomFile, which returns the XNAT
//	 * Scan ID corresponding to the DICOM file being loaded,
//	 * creating a new experiment if necessary.
//	 * @param XNATProjectName
//	 * @param xndp
//	 * @param catalogCache
//	 * @return
//	 * @throws exceptions.XNATException
//	 */
//	private String createScanIfNecessary(
//                     String XNATProjectName,
//                     XNATDicomParameters xndp,
//                     Hashtable<String, XNATCacheElement> catalogCache)
//				      throws XNATException
//   {
//		String DICOMStudyUID  = xndp.getParameter("StudyUID");
//		String DICOMSeriesUID = xndp.getParameter("SeriesUID");
//		XNATCacheElement ce   = catalogCache.get(DICOMStudyUID);
//
//      if (ce.DICOMSeriesUIDs != null)
//		{
//			int index = ce.DICOMSeriesUIDs.indexOf(DICOMSeriesUID);
//			if (index != -1) return ce.XNATScanLabels.elementAt(index);
//		}
//
//		/* Create a "unique" scan label for use in XNAT. By preference, this
//		 * will use the type and scan number from the DICOM file. However, if
//		 * the scan number has defaulted to a UID, then do not use this, as it
//		 * is too unwieldy, so try the time instead. At present, the return
//       * value from createScan is the same as the input, i.e., XNATScanLabel
//       * = scanLabel. This formalism is used just in case XNAT changes in such
//       * a way to return something different.
//		 */
//		String XNATScanLabel = null;
//		String scanType      = xndp.getParameter("Scan_Type");
//		String scanNumber    = xndp.getParameter("Scan");
//		String scanTime      = xndp.getParameter("Scan_Time");
//		String scanLabel;
//		scanLabel            = (scanNumber.length() < 10) ? scanType + scanNumber:
//																	    scanType + scanTime;
//		scanLabel            = removeIllegalCharacters(scanLabel);
//		String modality      = xndp.getParameter("Modality");
//
//      try
//      {
//         XNATScanLabel = createScan(XNATProjectName,
//							   				ce.XNATSubjectID,
//								   			ce.XNATExperimentID,
//											   scanLabel,
//                                    xndp);
//      }
//      catch (XNATException exXNAT)
//      {
//         throw exXNAT;
//      }
//
//		/* Having added the new scan to the XNAT database, we now need to
//		 * update the cache to reflect this.
//		 */
//		if (ce.XNATScanLabels == null)
//      {
//         ce.XNATScanLabels     = new Vector<String>();
//         ce.DICOMSeriesUIDs = new Vector<String>();
//
//			if (ce.DICOMSOPInstanceUIDs == null)
//				ce.DICOMSOPInstanceUIDs = new Vector2D<String>();
//
//			ce.DICOMSOPInstanceUIDs.add(new Vector<String>());
//      }
//
//		ce.XNATScanLabels.add(XNATScanLabel);
//		ce.DICOMSeriesUIDs.add(DICOMSeriesUID);
//
//
//		return XNATScanLabel;
//   }
//
//
//
//   private void createResourcesIfNecessary(
//                     String XNATProjectName,
//                     String XNATSubjectID,
//                     String XNATExperimentID,
//                     String XNATScanLabel,
//                     String[] XNATResourceNames,
//                     String[] XNATResourceTags)
//				      throws XNATException
//   {
//      String RESTQuery = "/REST/projects/" + XNATProjectName
//                       + "/subjects/"      + XNATSubjectID
//                       + "/experiments/"   + XNATExperimentID
//                       + "/scans/"         + XNATScanLabel
//                       + "/resources?format=xml";
//
//      Vector2D<String> resourceResult;
//      try
//      {
//         resourceResult = RESTGetResultSet(RESTQuery);
//      }
//      catch (Exception ex)
//      {
//         throw new XNATException(XNATException.GET);
//      }
//
//
//      try
//      {
//         for (int i=0; i<XNATResourceNames.length; i++)
//            if (!resourceResult.columnContains(1, XNATResourceNames[i]))
//               createTaggedResource(XNATProjectName,
//							   			XNATSubjectID,
//								   		XNATExperimentID,
//											XNATScanLabel,
//                                 XNATResourceNames[i],
//                                 XNATResourceTags[i]);
//      }
//      catch (XNATException exXNAT)
//      {
//         throw exXNAT;
//      }
//   }
//
//
//
//
//	/**
//	 * Given an XML document representing an XNAT experiment, parse the data
//	 * to produce an entry in the cache. The cache is used to speed up access
//	 * to the data by reducing the number of REST queries that have to be sent
//	 * to XNAT.
//	 * @param experimentDoc
//	 * @param DICOMStudyUID
//	 * @param XNATExperimentID
//	 * @param XNATSubjectID
//	 * @return
//	 * @throws exceptions.XMLException
//	 * @throws exceptions.XNATException
//	 */
//	public XNATCacheElement parseExperimentXML(
//                     Document experimentDoc,
//							String DICOMStudyUID,
//							String XNATExperimentID,
//							String XNATSubjectID,
//                     String XNATSubjectLabel)
//                  throws XMLException, XNATException
//   {
//      Vector<String>             DICOMSeriesUIDs;
//      Vector<String>             XNATScanIDs;
//      Vector2D<String> DICOMSOPInstanceUIDs;
//      NodeList                   scans;
//
//      XMLUtilities xmlUtils = new XMLUtilities();
//      XNATNamespaceContext XNATns = new XNATNamespaceContext();
//
//
//      try
//      {
//         scans = (NodeList) xmlUtils.getElement(experimentDoc,
//                                                XNATns,
//                                                "xnat:scan");
//      }
//      catch (XMLException exXML)
//      {
//         throw exXML;
//      }
//
//
//      int nScans = scans.getLength();
//
//      if (nScans == 0) return new XNATCacheElement(DICOMStudyUID,
//                                                   XNATExperimentID,
//                                                   XNATSubjectID,
//                                                   XNATSubjectLabel,
//                                                   null, null, null);
//      DICOMSeriesUIDs      = new Vector<String>();
//      XNATScanIDs          = new Vector<String>();
//      DICOMSOPInstanceUIDs = new Vector2D<String>();
//
//      for (int i=0; i<nScans; i++)
//      {
//         /* The next problem is to sort out the right path for the catalog.xml
//          * file for each scan. Each scan element may have more than one file
//          * element, because, as well as DICOM data, XNAT can store snapshots
//          * as well as files of other arbitrary types. We want the URI attribute
//          * for the file element with attribute format="DICOM".
//          */
//         String[][] scanAttrs;
//			try
//		   {
//            scanAttrs = xmlUtils.getAttributes(scans.item(i),
//                                               XNATns,
//                                               "xnat:scan",
//                                               new String[] {"ID", "UID"});
//            XNATScanIDs.add(scanAttrs[0][0]);
//            DICOMSeriesUIDs.add(scanAttrs[0][1]);
//				DICOMSOPInstanceUIDs.add(new Vector<String>());
//
//            scanAttrs = xmlUtils.getAttributes(scans.item(i),
//                                               XNATns,
//                                               "xnat:file",
//                                               new String[] {"format", "URI"});
//	      }
//			catch (XMLException exXML)
//			{
//            throw exXML;
//			}
//
//         File catalogFile = null;
//
//         for (int j=0; j<scanAttrs.length; j++)
//            if (scanAttrs[j][0].equals("DICOM"))
//					catalogFile = new File(scanAttrs[j][1]);
//
//         if (catalogFile == null) throw new XNATException(XNATException.CATALOG);
//
//         try
//         {
//            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(catalogFile));
//				String[]   SOPInstances = xmlUtils.getAttribute(xmlUtils.getDOMDocument(bis),
//															               XNATns,
//                                                            "cat:entry",
//															               "UID");
//				for (int j=0; j<SOPInstances.length; j++)
//				{
//					/* This exception should not be thrown if the catalog.xml file
//					 * has been correctly created by XNAT. */
//					if (SOPInstances[j] == null)
//						throw new XNATException(XNATException.CATALOG);
//
//					DICOMSOPInstanceUIDs.elementAt(i).add(SOPInstances[j]);
//				}
//
//	      }
//			catch (Exception ex)
//			{
//            throw new XNATException(XNATException.CATALOG);
//			}
//
//		} // End of i loop over scans
//
//      return new XNATCacheElement(DICOMStudyUID,
//											 XNATExperimentID,
//											 XNATSubjectID,
//											 XNATSubjectLabel,
//											 DICOMSeriesUIDs,
//											 XNATScanIDs,
//											 DICOMSOPInstanceUIDs);
//   }



   



   /**
    * Create the XML file required for uploading a DICOM file to XNAT.
    * @param xndp
    * @throws exceptions.XNATException
    */
   private void createScanXML(XNATDicomParameters xndp)
                throws XNATException, IOException
   {
      /* Before embarking on writing the scan XML file, extract a few parameters
       * that can't easily be generated in line, or need to be accessed several
       * times.
       */
      float[] voxelSize  = xndp.getVoxelSize();
      float[] FOV        = xndp.getFOV();
      int[]   matrixSize = xndp.getMatrixSize();


      File                          file;
      FileWriter                    fw;
      DelayedPrettyPrinterXmlWriter dppXML;
      try
      {
         /* I had a problem with the XML file not being written if it was already
          * present. So delete it if it exists.
          */
         file   = new File(createScanXMLFilename);
         file.delete();

         fw     = new FileWriter(createScanXMLFilename);
         dppXML = new DelayedPrettyPrinterXmlWriter(new SimpleXmlWriter(fw));
      }
      catch (Exception ex)
      {
         throw new XNATException(XNATException.SCAN_CREATE,
                 ": can't write createScan.xml file: " + ex.getMessage());
      }

      dppXML.setIndent("   ")
      .writeXmlVersion()
      .writeEntity(XNATScanType(xndp))
         .writeAttribute("xmlns:xnat", "http://nrg.wustl.edu/xnat")
         .writeAttribute("xmlns:xsi",  "http://www.w3.org/2001/XMLSchema-instance")
         .writeAttribute("UID", xndp.getParameter("SeriesUID"))

         .delayedWriteEntity("xnat:series_description")
            .delayedWriteText(xndp.getParameter("Series_Description"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:scanner")
            .delayedWriteAttribute("manufacturer", xndp.getParameter("Scanner_Manufacturer"))
            .delayedWriteAttribute("model", xndp.getParameter("Scanner_Model"))
            .delayedWriteText(xndp.getParameter("Scanner_Name"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:modality")
            .delayedWriteText(xndp.getParameter("Modality"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:operator")
            .delayedWriteText(xndp.getParameter("Operator"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:coil")
            .delayedWriteText(xndp.getParameter("Receive_Coil"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:fieldStrength")
            .delayedWriteText(xndp.getParameter("Field_Strength"))
         .delayedEndEntity()

         .delayedWriteEntity("xnat:parameters")

            .delayedWriteEntity("xnat:voxelRes")
               .delayedWriteAttribute("x", voxelSize[0])
               .delayedWriteAttribute("y", voxelSize[1])
               .delayedWriteAttribute("z", voxelSize[2])
            .delayedEndEntity()

            .delayedWriteEntity("xnat:orientation")
               .delayedWriteText(xndp.getXNATOrientation())
            .delayedEndEntity()

            .delayedWriteEntity("xnat:fov")
               .delayedWriteAttribute("x", FOV[0])
               .delayedWriteAttribute("y", FOV[1])
            .delayedEndEntity()

            .delayedWriteEntity("xnat:matrix")
               .delayedWriteAttribute("x", matrixSize[0])
               .delayedWriteAttribute("y", matrixSize[1])
            .delayedEndEntity()

            .delayedWriteEntity("xnat:partitions")
               .delayedWriteText(xndp.getParameter("Number_of_Planes"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:tr")
               .delayedWriteText(xndp.getParameter("Repetition_Time_TR"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:te")
               .delayedWriteText(xndp.getParameter("Echo_Time_TE"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:ti")
               .delayedWriteText(xndp.getParameter("Inversion_Time_TI"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:flip")
               .delayedWriteText(xndp.getParameter("Flip_Angle"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:sequence")
               .delayedWriteText(xndp.getParameter("Sequence_Name"))
            .delayedEndEntity()

            .delayedWriteEntity("scanTime")
               .delayedWriteText(xndp.getParameter("Series_Time"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:imageType")
               .delayedWriteText(xndp.getParameter("Image_Type"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:scanSequence")
               .delayedWriteText(xndp.getParameter("Scanning_Sequence"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:seqVariant")
               .delayedWriteText(xndp.getParameter("Sequence_Variant"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:scanOptions")
               .delayedWriteText(xndp.getParameter("Scan_Options"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:acqType")
               .delayedWriteText(xndp.getParameter("MR_Acquisition_Type"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:coil")
               .delayedWriteText(xndp.getParameter("Receive_Coil"))
            .delayedEndEntity()

            /* The following elements are not yet supported in the main xnat
             * schema and so (temporarily, I hope) are coded as "addParam"s.
             * Ideally, the diffusion direction should be an element
             * in its own right with attributes for x-, y- and z-
             * components. As it stands, any application using the database will
             * need to extract these. Note that a key problem as yet unresolved
             * is how to handled multi-frame data. Most of the parameters below
             * will change over the course of a single scan.
             

            .delayedWriteEntity("xnat:addParam")
               .delayedWriteAttribute("name", "slice_position")
               .delayedWriteText(xndp.getParameter("Slice_Position"))
            .delayedEndEntity()

            .delayedWriteEntity("xnat:addParam")
               .delayedWriteAttribute("name", "diffusion_directionality")
               .delayedWriteText(xndp.getDiffusionDirectionality())
            .delayedEndEntity()

            .delayedWriteEntity("xnat:addParam")
               .delayedWriteAttribute("name", "diffusion_direction")
               .delayedWriteText(xndp.getDiffusionDirection())
            .delayedEndEntity()

            .delayedWriteEntity("xnat:addParam")
               .delayedWriteAttribute("name", "b-value")
               .delayedWriteText(xndp.getBValue())
            .delayedEndEntity()


            .delayedWriteEntity("xnat:addParam")
               .delayedWriteAttribute("name", "acquisition_time")
               .delayedWriteText(xndp.getParameter("Acquisition_Time"))
            .delayedEndEntity()
            */

         .delayedEndEntity()  // xnat:parameters


      .endEntity();  // xnat:MRScan

      dppXML.close();
      fw.close();
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







   /**
    * In XNAT, only alphanumerics, spaces and the underscore character are
    * permitted. This function removes unwanted characters, replacing them
    * with underscores.
    * @param s
    * @return
    */
   private static String removeIllegalCharacters(String s)
   {
		/* There seems to be a quirk in which sometimes a string is returned
		 * with an extra string termination character at the end. If this is
		 * the case then truncate it.
		 */
		if ((int) s.charAt(s.length()-1) == 0)
			return s.substring(0, s.length()-1).replaceAll("\\W", "_");

      return s.replaceAll("\\W", "_");
   }



   /**
    * XNAT is quite sensitive to the format in which dates are entered.
    * The format yyyymmdd seems to work fine, whilst other input formats
    * do not return errors, but give corrupted values. The conversions here
    * are based on cases encountered to date and will require updating based
    * on further experience.
    * @param date - input string from DICOM file
    * @return
    */
   private static String formatDate(String date)
                         throws NumberFormatException
   {
      Integer yyyy;
      Integer yy;
      Integer mm;
      Integer dd;

      String date1 = date.replaceAll("/", "").replaceAll("\\.", "");

      if (date1.length() == 8)
      {
         try
         {
            yyyy = new Integer(date1.substring(0, 4));
            mm   = new Integer(date1.substring(4, 6));
            dd   = new Integer(date1.substring(6, 8));
         }
         catch (NumberFormatException exNF)
         {
            throw exNF;
         }

         if (yyyy < 1900 || yyyy > 2200 || mm < 1 || mm > 12 || dd < 1 || dd > 31)
            throw new NumberFormatException("Invalid date");

         return date1;
      }

      
      if (date1.length() == 6)
      {
         try
         {
            yy = new Integer(date1.substring(0, 2));
            mm = new Integer(date1.substring(2, 4));
            dd = new Integer(date1.substring(4, 6));
         }
         catch (NumberFormatException exNF)
         {
            throw exNF;
         }

         if (yy < 0) throw new NumberFormatException("Invalid date");

         if (mm < 1 || mm > 12 || dd < 1 || dd > 31)
            throw new NumberFormatException("Invalid date");

         if (yy > 50) yyyy = 1900+yy; else yyyy = 2000+yy;

         return new String( yyyy.toString()+mm.toString()+dd.toString());
      }

      throw new NumberFormatException("Invalid date");
   }



 /**
 * Utility function to take code out of the other methods.
 * @param xndp object allowing us to query the parameters of a DICOM file
 * in a format that XNAT understands.
 * @return the XML element name for the scanData type corresponding to the modality
 * represented by the DICOM file.
 */
   public static String XNATScanDataType(XNATDicomParameters xndp)
   {
      String modality = xndp.getParameter("Modality");

      // Start with support for MR, PET and CT - add more as and when data available.
      String scanType = "xnat:imageScanData";
      if (modality.equals("CT")) scanType = "xnat:ctScanData";
      if (modality.equals("MR")) scanType = "xnat:mrScanData";
      if (modality.equals("PT")) scanType = "xnat:petScanData";

      return scanType;
   }

 /**
 * Utility function to take code out of the other methods.
 * @param xndp object allowing us to query the parameters of a DICOM file
 * in a format that XNAT understands.
 * @return the XML element name for the scan type corresponding to the modality
 * represented by the DICOM file.
 */
   public static String XNATScanType(XNATDicomParameters xndp)
   {
      String modality = xndp.getParameter("Modality");

      // Start with support for MR, PET and CT - add more as and when data available.
      String scanType = "xnat:imageScanData";
      if (modality.equals("CT")) scanType = "xnat:CTScan";
      if (modality.equals("MR")) scanType = "xnat:MRScan";
      if (modality.equals("PT")) scanType = "xnat:PETScan";

      return scanType;
   }
}
