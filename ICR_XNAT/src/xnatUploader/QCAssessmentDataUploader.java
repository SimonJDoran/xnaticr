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
* Java class: QCAssessmentDataUploader.java
* First created on Dec 16, 2010 at 12:56:16 PM
* 
* Abstract subclass of DataUploader that caters specifically for the
* fields required in the xnat:qcAssessmentData schema.
*********************************************************************/

package xnatUploader;

import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;


public abstract class QCAssessmentDataUploader extends DataUploader
{
   static Logger logger = Logger.getLogger(DataUploader.class);
   
   public QCAssessmentDataUploader(XNATProfile xnprf)
   {
      super(xnprf);
   }
   
   
/**
    * Utility function used in createMetadataXML below
    * @param provString String containing processStep entries in a provenance
    * list, separated by the characteristic identifier !PS!
    * @return an ArrayList of the separated values
    */
   private ArrayList<String> splitProvenanceString(String provString)
   {
      ArrayList<String> list = new ArrayList<String>();
      int    ind;
      String s = new String(provString);
      
      if (s != null) do
      {
         ind = s.indexOf("!PS! ", 0);
         if (ind == -1)
         {
            // XNAT objects if the provenance entry is blank.
            if (s.equals("")) s = "Unknown";
            list.add(s);
         }
         else
         {
            list.add(s.substring(0, ind - 1));
            s = s.substring(ind + 5);
         }
      }
      while (ind != -1);
      
      return list;
   }


   /**
    * Create the XML that is uploaded to XNAT and contains all the metadata
    * that will go into the PostgreSQL database.
    * @return a Document to be uploaded into XNAT
    */
   @Override
   public Document createMetadataXML()
   {
      initialiseXML();
      createQCMetadataXML();
      createScanListXML();
      createSpecificMetadataXML();
      finishWritingXML();
      
      return getXMLDocument();
   }
   
 
   
   /**
    * Create the output stream and writer for the metadata XML and write
    * the root element.
    */
   protected void initialiseXML()
   {          
      baos    = new ByteArrayOutputStream();
      dppXML  = new DelayedPrettyPrinterXmlWriter(
                   new SimpleXmlWriter(
                      new OutputStreamWriter(baos)));
      
      String label;
      String note;
      if (isBatchMode)
         currentLabel = batchLabelPrefix + (new Long(System.nanoTime())).toString();
      else
         currentLabel = getStringField("Label");
      
      try
      {
         dppXML.setIndent("   ")
               .writeXmlVersion()
               .writeEntity(getRootElement())
               .writeAttribute("xmlns:xnat", "http://nrg.wustl.edu/xnat")
               .writeAttribute("xmlns:xsi",  "http://www.w3.org/2001/XMLSchema-instance")
               .writeAttribute("xmlns:prov", "http://www.nbirn.net/prov")
               .writeAttribute("xmlns:icr",  "http://www.icr.ac.uk/icr")
               .writeAttribute("ID",         XNATAccessionID)
               .writeAttribute("label",      currentLabel)
               .writeAttribute("project",    XNATProject);
      }
         

      catch (IOException exIO){reportError(exIO, "initialise XML");}
   }

   
   
   /**
    * Create the generic QCAssessmentData element.
    * This method does not return anything, because the XML that it is building
    * is a work in progress that will be completed by another method.
    */
   protected void createQCMetadataXML()
   {
      XMLUtilities xmlUtil = new XMLUtilities();
      
      // Each document needs a label unique within the project(?), which is
      // used when creating lists in various display contexts. So, we need to
      // check that the specified label has not been used.
      try
      {
         RESTCommand = "/data/archive/projects/" + XNATProject
                       + "/subjects/"            + XNATSubjectID
                       + "/experiments/"         + XNATExperimentID
                       + "/assessors"
                       + "?format=xml";
         result      = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (Exception ex)
      {
         errorOccurred = true;
         errorMessage  = "Error retrieving data from XNAT while preparing\n"
                         + "metadata for upload\n\n" + ex.getMessage();
         logger.error(errorMessage);
         return;
      }
      
      if (result.columnContains(5, getStringField("Label")))
      {
         errorOccurred = true;
         errorMessage  = "You must ensure that the label you enter is unique\n"
                         + "so that all assessments can be correctly listed.\n"
                         + "Please try again with a different label.";
         logger.error(errorMessage);
         return;
      }
      
      
      // Check for the presence of illegal characters. Note that XNAT will use
      // the label to form a UNIX directory name and this should not contain
      // spaces.
      if (!Pattern.matches("\\w+", getStringField("Label")))
      {
         errorOccurred = true;
         errorMessage  = "The label name must contain only alphanumeric characters\n"
                         + "or an underscore. No other characters are permitted.\n"
                         + "Please try again with a different label.";
         logger.error(errorMessage);
         return;
      }   
      
      
      // The next step is a bit of a kludge to allow a straightforward
      // display and archiving of multiple process steps. If these are
      // present, we separate them with the characteristic
      // identifier !PS! If this is not found, then there is only one step.
      ArrayList<String> provProgs     = splitProvenanceString(
                                        getStringField("Provenance: program"));
      ArrayList<String> provVersions  = splitProvenanceString(
                                        getStringField("Provenance: version"));
      ArrayList<String> provArguments = splitProvenanceString(
                                        getStringField("Provenance: arguments"));
      ArrayList<String> provUsers     = splitProvenanceString(
                                        getStringField("Provenance: user"));
      ArrayList<String> provMachines  = splitProvenanceString(
                                        getStringField("Provenance: machine"));
      ArrayList<String> provPlatforms = splitProvenanceString(
                                        getStringField("Provenance: platform"));

      if ((provProgs.size() != provVersions.size())  ||
          (provProgs.size() != provArguments.size()) ||
          (provProgs.size() != provUsers.size())     ||
          (provProgs.size() != provMachines.size())  ||
          (provProgs.size() != provPlatforms.size()))
         {
            reportError(null, "creating qcAssessmentData: provenance");
            return;
         }
      
      InvestigatorList.Investigator xninv   = getXNATInvestigators()
                                                 .getChosenInvestigator();

      
      // Write the data out.
      try
      {
         dppXML.delayedWriteEntity("date")
                  .delayedWriteText(date)
               .delayedEndEntity()
               
               .delayedWriteEntity("time")
                  .delayedWriteText(time)
               .delayedEndEntity()
               
               .writeEntity("note")
                  .writeText(isBatchMode ? batchNote : getStringField("Note"))
               .endEntity()
               
               .writeEntity("investigator")
                  .writeEntity("title")
                     .writeText(xninv.title)
                  .endEntity()
                  .writeEntity("firstname")
                     .writeText(xninv.firstName)
                  .endEntity()
                  .writeEntity("lastname")
                     .writeText(xninv.lastName)
                  .endEntity()
                  .writeEntity("institution")
                     .writeText(xninv.institution)
                  .endEntity()
                  .writeEntity("department")
                     .writeText(xninv.department)
                  .endEntity()
                  .writeEntity("email")
                     .writeText(xninv.email)
                  .endEntity()
                  .writeEntity("phone")
                     .writeText(xninv.phoneNumber)
                  .endEntity()
               .endEntity();

         if (!provProgs.isEmpty())
         {
            dppXML.writeEntity("provenance");
            for (int i=0; i<provProgs.size(); i++)
            {
               dppXML.writeEntity("processStep")
                  .delayedWriteEntity("program")
                     .delayedWriteAttribute("version", provVersions.get(i))
                     .delayedWriteAttribute("arguments", provArguments.get(i))
                     .delayedWriteText(provProgs.get(i))
                  .delayedEndEntity()
                  .delayedWriteEntity("timestamp")
                     .delayedWriteText(date + " " + time)
                  .delayedEndEntity()
                  .delayedWriteEntity("cvs")
                     .delayedWriteText("not specified")
                  .delayedEndEntity()
                  .delayedWriteEntity("user")
                     .delayedWriteText(provUsers.get(i))
                  .delayedEndEntity()
                  .delayedWriteEntity("machine")
                     .delayedWriteText(provMachines.get(i))
                  .delayedEndEntity()
                  .delayedWriteEntity("platform")
                     .delayedWriteText(provPlatforms.get(i))
                  .delayedEndEntity()
               .endEntity();
            }
            dppXML.endEntity();
         }
         
         // Note that I write out only an "in" section. The philosophy of the
         // uploader is that all files used to create the qcAssessment must
         // already be present in the database and the catalog file created
         // here records them. By contrast, the upload procedure itself may
         // create new files (such as thumbnails) and these are placed in the
         // repository by the XNAT Uploader. XNAT automatically records all
         // new files uploaded in its own catalog file.
         createInputCatalogFile();
         dppXML.writeEntity("in")
                  .writeEntity("file")
                     .writeEntity("tags")
                        .writeEntity("tag")
                           .writeAttribute("name", "URI")
                           .writeText("input_file_catalog.xml")
                        .endEntity()
                        .writeEntity("tag")
                           .writeAttribute("name", "format")
                           .writeText("ICR_XNAT_UPLOADER_INPUT_CATALOG_XML")
                        .endEntity()
                     .endEntity()
                  .endEntity()
               .endEntity()
               .writeEntity("imageSession_ID")
                  .writeText(XNATExperimentID)
               .endEntity();
                                
      } 
   
      catch (IOException exIO) {reportError(exIO, "creating qcAssessmentData");}
   }
             
           
   /**
    * Allow subclasses of QCAssessmentData to write out data on the input files used
    * by the application whose data we are uploading.
    */
   protected abstract void createInputCatalogFile();
   
   
   
   /**
    *  Add the list of scans (which might potentially be empty).
    */
   protected void createScanListXML()
   {
      try
      {
         dppXML.delayedWriteEntity("scans");

         for (String scan : XNATScanID)
         {
            dppXML.delayedWriteEntity("scan")
                  .delayedWriteAttribute("id", scan)
            .delayedEndEntity();
         }
         dppXML.delayedEndEntity();
      }
      catch (IOException exIO) {reportError(exIO, "create scan list");}
   }
   
   
   
   /**
    * Allow subclasses of QCAssessmentDataUploader to record data specific to
    * the application whose data we are uploading.
    */
   protected void createSpecificMetadataXML()
   {
      // For the xnat:qcAssessmentData there are not specific additional fields.
   }
  
   
   
   /**
    * Close open elements and finish writing XML.
    */
   protected void finishWritingXML()
   {
      try
      {
         dppXML.endEntity();
         dppXML.close();
      }
      catch (IOException exIO) {reportError(exIO, "finish writing XML");}
   }
   
  
   
   /**
    * Retrieve the XML describing the metadata from the output stream.
    * @return a Document containing the information
    */
   protected Document getXMLDocument()
   {
      Document metaDoc = null;
      try
      {
         ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());     
         metaDoc = (new XMLUtilities()).getDOMDocument(bis);
      }
      catch (XMLException exXML)
      {
         reportError(exXML, "convert OutputStream to Document");
      }
      
      return metaDoc;
   }

   
   
   /**
    * Error reporting method that takes some often repeated lines out of the
    * other methods
    * @param ex the Exception that gave rise to this call
    * @param operation a String giving details of what the program was doing
    */
   protected void reportError(Exception ex, String operation)
   {
      logger.error("Error during operation " + operation + " of metadata XML creation.");
      
      errorOccurred = true;
      errorMessage  = "Unable to create XML document required for uploading the data.\n";
      errorMessage  = "Error during operation " + operation + "\n\n";
      
      if (ex == null) return;
      if (ex.getMessage() != null) errorMessage += "The detailed error message was:\n"
                                                     + ex.getMessage();
   }
   
   
   
   @Override
   public boolean rightMetadataPresent()
   {
      return (!getStringField("Label").equals("")) &&
             (!getStringField("Note").equals(""))  &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanID.get(0).equals(""));
   }
   
   
   @Override
   public String[] getRequiredFields()
   {
      return new String[]{"Label", "Note"};
   }
   
   
   @Override
   public String createMetadataUploadCommand()
   {
      return "/data/archive/projects" + XNATProject
             + "/experiments/"        + XNATExperimentID
             + "/assessors/"          + currentLabel;
   }


   @Override
   public String getRootElement()
   {
      return "QCAssessment";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "xnat:qcAssessmentData";
   }

}
