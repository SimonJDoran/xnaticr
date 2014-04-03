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
* Java class: AdeptDataUploader.java
* First created on October 18, 2010 at 9:10 PM
* 
* Object for uploading files to XNAT that have been generated by the
* in-house ICR application ADEPT.
*********************************************************************/

package xnatUploader;

import com.generationjava.io.xml.SimpleXmlWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.imageio.ImageIO;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xnatDAO.XNATProfile;


public class AdeptDataUploader extends QCAssessmentDataUploader
{
   public AdeptDataUploader(XNATProfile xnprf)
   {
      super(xnprf);
   }

   @Override
   public boolean parseFile()
   {
      // Find information on all the studies in the database.
      try
      {
         studyUIDs       = (XMLUtil.getAttributeA(doc, XNATns, "input",
                                 "study-uid"));
         seriesUIDs      = XMLUtil.getAttributeA(doc, XNATns, "image",
                                 "series-uid");
         SOPInstanceUIDs = XMLUtil.getAttributeA(doc, XNATns, "image",
                                 "sop-instance-uid");

         RESTCommand = "/REST/projects/" + XNATProject + "/experiments"
                       + "?xsiType=xnat:imageSessionData"
                       + "&columns=xnat:imageSessionData/UID,xnat:imageSessionData/subject_ID"
                       + "&format=xml";
         result      = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (Exception ex)
      {
         errorOccurred = true;
         errorMessage  = "Unable to open selected file.\n"
                    + "It appears not to be a valid ADEPT result set file.\n\n";
                    
         if (ex.getMessage() != null)
            errorMessage += "The detailed error message was:\n" + ex.getMessage();
         return false;
      }


      // Does the DICOM study from the uploaded ADEPT file match any of the
      // studies in the database? At present, we expect the ADEPT file to be
      // based on only one study. This might change in future.
      if (studyUIDs.size() != 1)
      {
         errorOccurred = true;
         errorMessage  = "Unable to open selected file.\n"
           + "The ADEPT result set file appears to refer to more than one study.\n\n";
         return false;
      }

      boolean allPresent = false;
      if (result.columnContains(1, studyUIDs.get(0)))
      {
         // If so, then are the relevant series present?
         // For simplicity, I currently assume that if the series is loaded
         // then so are the individual images.
         try
         {
            int row = result.indexOfForCol(1, studyUIDs.get(0));
            XNATExperimentID = result.atom(0, row);
            XNATSubjectID    = result.atom(2, row);

            RESTCommand = "/REST/projects/" + XNATProject
                          + "/subjects/"    + XNATSubjectID
                          + "/experiments/" + XNATExperimentID
                          + "?format=xml";
            resultDoc   = xnrt.RESTGetDoc(RESTCommand);
            parseResult = XMLUtil.getAttributes(resultDoc, XNATns, "xnat:scan",
                                                   new String[] {"ID", "UID"});
         }
         catch (Exception ex)
         {
            errorOccurred = true;
            errorMessage  = "Error retrieving data from XNAT\n\n" + ex.getMessage();
            return false;
         }

         // Iterate only over unique series, so that we repeat the various
         // checks only as often as we need to.
         HashSet<String> uniqueSeries = new HashSet<String>();
         for (int i=0; i<seriesUIDs.size(); i++)
            if (!uniqueSeries.contains(seriesUIDs.get(i)))
               uniqueSeries.add(seriesUIDs.get(i));

         allPresent = true;
         XNATScanID = new ArrayList<String>();
         for (String seriesUID : uniqueSeries)
         {
            boolean seriesPresent = false;
            for (int i=0; i<parseResult.length; i++)
            {
               if (parseResult[i][1].equals(seriesUID))
               {
                  seriesPresent = true;
                  XNATScanID.add(parseResult[i][0]);
               }
            }
            allPresent = allPresent && seriesPresent;
         }
      }

      if (!allPresent)
      {
         errorOccurred = true;
         errorMessage  = "The DICOM series associated with the Adept result set file \n"
            + "are not all present in the chosen XNAT database.\n\n"
            + "Please archive the dataset to XNAT before continuing.";
         return false;
      }
      return true;
   }
   
   
   @Override
   public void updateParseFile()
   {
      //TODO
   }

   
   @Override
   public void populateFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("XNAT assessment ID", "Generated by XNAT on data upload");
      mdsp.populateJTextField("Associated project", XNATProject);
      mdsp.populateJTextField("Label", "", true);
 
      // Provenance information not yet implemented in Adept.
      //mdsp.populateJTextField("Creation date", xnu.getXNATDate());
      //mdsp.populateJTextField("Creation time", xnu.getXNATTime());
      mdsp.populateJTextField("Associated XNAT image session ID", XNATExperimentID);
      
      mdsp.populateJTextField("Note", "", true);
   }



   @Override
   public void clearFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("XNAT assessment ID",               "");
      mdsp.populateJTextField("Associated project",               "");
      mdsp.populateJTextField("Associated XNAT image session ID", "");
      mdsp.populateJTextField("Note",                             "", true);
      mdsp.populateJTextField("Label",                            "", true);
   }
   
   
   @Override
   public String getFileFormat()
   {
      return "ADEPT_XML";
   }
   


  /**
    * Create an XML representation of the metadata relating to the input files
    * referred to and output files created by the application whose data we are
    * uploading.
    */
   @Override
   protected void createInputCatalogFile()
   {      
      String homeDir        = System.getProperty("user.home");
      String fileSep        = System.getProperty("file.separator");
      String XNAT_DAO_HOME  = homeDir + fileSep + ".XNAT_DAO" + fileSep;
      String catFilename    = XNAT_DAO_HOME + "temp" + fileSep 
                              + XNATAccessionID + "_input_catalog.xml";
      File   catFile        = new File(catFilename);
      
      try
      {
         DelayedPrettyPrinterXmlWriter ppXML
              = new DelayedPrettyPrinterXmlWriter(
                   new SimpleXmlWriter(
                      new FileWriter(catFile)));
         
         ppXML.setIndent("   ")
               .writeXmlVersion()
               .writeEntity("cat:Catalog")
               .writeAttribute("xmlns:cat", "http://nrg.wustl.edu/catalog")
               .writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
               
              
         // Now add the files, using the "delayed" method so that an empty
         // "cat:entries" section is not written if there are not files.
         ppXML.delayedWriteEntity("cat:entries");
         
         // TODO Implement the input catalog for ADEPT.
         
         ppXML.delayedEndEntity();
         ppXML.close();
      }
      catch (Exception ex)
      {
         reportError(ex, "create input catalog file");
      }
      
      auxiliaryFiles.add(catFile);
      auxFileFormats.add("INPUT_CATALOG_XML");
   }
   
   
   
   /**
    * Create additional thumbnail files for upload with the ADEPT object set.
    */
   @Override
   public void createAuxiliaryFiles()
   {
      // TODO Create ADEPT renderer.
      // MRIWRenderer r = new MRIWRenderer();
      //BufferedImage[] thumbnails     = r.createImages();
      BufferedImage[] thumbnails = null;
      
      String          homeDir        = System.getProperty("user.home");
      String          fileSep        = System.getProperty("file.separator");
      String          XNAT_DAO_HOME  = homeDir + fileSep + ".XNAT_DAO" + fileSep;
      String          filePrefix     = XNAT_DAO_HOME + "temp" + fileSep 
                                       + XNATAccessionID + "_ADEPT_thumbnail_";

      try
      {
         for (int i=0; i<thumbnails.length; i++)
         {
            File outputFile = new File(filePrefix + i);
            ImageIO.write(thumbnails[i], "png", outputFile);
            auxiliaryFiles.add(outputFile);
            auxFileFormats.add("ADEPT_THUMBNAIL_PNG");
         }
      }
      catch (IOException exIO)
      {
         reportError(exIO, "create ADEPT thumbnail file");
      }
   }
   
   
   @Override
   public String getRootElement()
   {
      return "ADEPTOutput";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:adeptOutputData";
   }

   
}
