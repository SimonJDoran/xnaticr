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
* THIS SOFTWARE IS PROVIDED BY XXHE COPYRIGHT HOLDERS AND CONTRIBUTORS
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
* Java class: MRIWDataUploader.java
* First created on Nov 15, 2010 at 3:00:38 PM
* 
* Object for uploading files to XNAT that have been generated by the
* in-house ICR application MRIW.
*********************************************************************/

package xnatUploader;

/**
 *
 * @author Simon J Doran
 *
 * Java class: MRIWDataUploader.java
 *
 * First created on Nov 15, 2010 at 3:00:38 PM
 *
 */
import com.generationjava.io.xml.SimpleXmlWriter;
import dataRepresentations.ContourRenderer;
import dataRepresentations.MRIWOutput;
import dataRepresentations.RTStruct;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.DicomXnatDateTime;
import generalUtilities.UIDGenerator;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomOutputStream;
import org.w3c.dom.Document;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xnatDAO.XNATGUI;
import xnatDAO.XNATProfile;


public class MRIWDataUploader extends QCAssessmentDataUploader
{
   static  Logger                        logger = Logger.getLogger(MRIWDataUploader.class);
   private MRIWOutput                    mriw;
   private LinkedHashMap<String, String> simpleEntries;

   public MRIWDataUploader(XNATProfile xnprf)
   {
      super(xnprf);
      
      // "Simple" in the sense that no special processing needs to be performed
      // to set these in the user interface once their values are available. I'm
      // unhappy that there is duplication (and hence the need to maintain
      // consistency) here of data in searchableXNATElements.xml,
      // but it is not obvious how to select just the elements we want without
      // typing all the names anyway!
      simpleEntries = new LinkedHashMap<String, String>();
      simpleEntries.put("Modality name",         "modality");
      simpleEntries.put("Modality subtype",      "subtype");
      simpleEntries.put("Converter class",       "converterClass");
      simpleEntries.put("Converter keys",        "converterKeys");
      simpleEntries.put("Converter values",      "converterValues");
      simpleEntries.put("Model class",           "modelClass");
      simpleEntries.put("Model keys",            "modelKeys");
      simpleEntries.put("Model values",          "modelValues");
      simpleEntries.put("Solver class",          "solverClass");
      simpleEntries.put("Solver keys",           "solverKeys");
      simpleEntries.put("Solver values",         "solverValues");
      simpleEntries.put("Curve extractor",       "curveExtractor");
      simpleEntries.put("Onset locator",         "onsetLocator");
      simpleEntries.put("ROI curve extractor",   "roiCurveExtractor");
      simpleEntries.put("Contrast agent",        "contrastAgent");
      simpleEntries.put("Contrast agent volume", "contrastAgentVolume");
      simpleEntries.put("Patient weight",        "patientWeight");
      simpleEntries.put("Patient code",          "patientCode");
      simpleEntries.put("Measurement code",      "measurementCode");
      simpleEntries.put("Imaging session date",  "sessionDate");
      simpleEntries.put("Imaging session time",  "sessionTime");
      simpleEntries.put("Slice location",        "sliceLocation");
      simpleEntries.put("Computed maps present", "computedMapsPresent");
      simpleEntries.put("Aggregate keys",        "aggregateKeys");
      simpleEntries.put("Aggregate values",      "aggregateValues");
   }

   @Override
   public boolean parseFile()
   {     
      try
      {
         mriw = MRIWOutput.getInstanceFromXML(doc, xnprf);
         
         date = DicomXnatDateTime.convertMriwToXnatDate(mriw.prov.creationDateTime);
         time = DicomXnatDateTime.convertMriwToXnatTime(mriw.prov.creationDateTime);
         
         XNATProject      = mriw.XNATProjectID; 
         XNATSubjectID    = mriw.XNATSubjectID;
         XNATExperimentID = mriw.XNATExperimentID;
         XNATScanID       = mriw.XNATScanID;
         
         populateStringFields();
         
      }
      catch (XNATException exXNAT)
      {
         errorOccurred = true;
         errorMessage  = "Error interacting with XNAT.\n"
                         + exXNAT.getMessage();
      }
      catch (XMLException exXML)
      {
         errorOccurred = true;
         errorMessage  = "Error validating MRIW XML file.\n"
                         + exXML.getMessage();
      }
      catch (DataFormatException exDF)
      {
         errorOccurred = true;
         errorMessage  = "Error validating extracting date or time from MRIW XML file.\n"
                         + exDF.getMessage();
      }
      
      if (errorOccurred) return false; else return true;
   }
  
   
   @Override
   public void updateParseFile()
   {
      //TODO
   }
   
   
   private void populateStringFields()
   {  
      if (mriw.outputType == MRIWOutput.BATCH_MODE)
         setStringField("Type", "Batch mode processing descriptor");

      if (mriw.outputType == MRIWOutput.RESULT_SET)
         setStringField("Type", "Result set");

      setStringField("Modality name",         mriw.inp.modality);
      setStringField("Modality subtype",      mriw.inp.subtype);
      
      setStringField("Converter class",       mriw.con.converterClass);
      setStringField("Converter keys",
                        mriw.getMRIWKeysAsString(mriw.con.converterKeyValue));
      setStringField("Converter values",
                        mriw.getMRIWValuesAsString(mriw.con.converterKeyValue));
      
      setStringField("Model class",           mriw.con.modelClass);
      setStringField("Model keys",
                        mriw.getMRIWKeysAsString(mriw.con.converterKeyValue));
      setStringField("Model values",
                        mriw.getMRIWValuesAsString(mriw.con.converterKeyValue));
      
      setStringField("Solver class",          mriw.con.solverClass);
      setStringField("Solver keys",
                        mriw.getMRIWKeysAsString(mriw.con.converterKeyValue));
      setStringField("Solver values",
                        mriw.getMRIWValuesAsString(mriw.con.converterKeyValue));
      
      setStringField("Curve extractor",       mriw.con.helpersCurveExtractor);
      setStringField("Onset locator",         mriw.con.helpersOnsetLocator);
      setStringField("ROI curve extractor",   mriw.con.helpersROICurveExtractor);
      setStringField("Contrast agent",        mriw.con.agentName);
      setStringField("Contrast agent volume", String.valueOf(mriw.con.agentVolume));
      setStringField("Patient weight",        String.valueOf(mriw.con.patientWeight));
      setStringField("Patient code"  ,        mriw.patientCode);
      setStringField("Measurement code",      mriw.measurementCode);
      setStringField("Imaging session date",  mriw.sessionDate);
      setStringField("Imaging session time",  mriw.sessionTime);
      setStringField("Slice location",        mriw.sliceLocation);
      
      setStringField("Computed maps present",
                        mriw.getMRIWMapNamesAsString(mriw.res.computedMaps));
      setStringField("Aggregate keys",
                        mriw.getMRIWKeysAsString(mriw.res.aggregateKeyValue));
      setStringField("Aggregate values",
                        mriw.getMRIWValuesAsString(mriw.res.aggregateKeyValue));
      
      setStringField("Provenance: program",   mriw.prov.programName);
      setStringField("Provenance: version",   mriw.prov.programVersion
                        + " build-id:"      + mriw.prov.programBuildID);
      setStringField("Provenance: arguments", "As specified in XML document");
      setStringField("Provenance: machine",   mriw.prov.platformRuntimeName
                        + " version:"       + mriw.prov.platformRuntimeVersion);
      setStringField("Provenance: platform",  mriw.prov.platformMachineOSName
                        + " version:"       + mriw.prov.platformMachineOSType
                        + " architecture:"  + mriw.prov.platformMachineArchitecture);
      setStringField("Provenance: user",      mriw.prov.creationUser);
   }
     
   
   @Override
   public void populateFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("XNAT assessment ID", "Generated by XNAT on data upload");
      mdsp.populateJTextField("Associated project", XNATProject);
      mdsp.populateJTextField("MRIW output date",   date);
      mdsp.populateJTextField("MRIW output time",   time);
      mdsp.populateJTextField("Associated XNAT image session ID", XNATExperimentID);
      
      mdsp.populateJTextField("Note", "",  true);      
      mdsp.populateJTextField("Label","", true);
      
      mdsp.populateJTextField("Type", getStringField("Type"));
      
      
      for (String key : simpleEntries.keySet())
         mdsp.populateJTextField(key, getStringField(key));
      
      mdsp.populateJTextField("Provenance: program",   getStringField("Provenance: program"));
      mdsp.populateJTextField("Provenance: version",   getStringField("Provenance: version"));
      mdsp.populateJTextField("Provenance: arguments", getStringField("Provenance: arguments"));
      mdsp.populateJTextField("Provenance: user",      getStringField("Provenance: user"));
      mdsp.populateJTextField("Provenance: machine",   getStringField("Provenance: machine"));
      mdsp.populateJTextField("Provenance: platform",  getStringField("Provenance: platform"));

   }


   @Override
   public void clearFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("XNAT assessment ID",               "");
      mdsp.populateJTextField("Associated project",               "");
      mdsp.populateJTextField("MRIW output date",                 "");
      mdsp.populateJTextField("MRIW output time",                 "");
      mdsp.populateJTextField("Associated XNAT image session ID", "");
      mdsp.populateJTextField("Note",                             "", true);
      mdsp.populateJTextField("Label",                            "", true);
      mdsp.populateJTextField("Type",                             "");
      mdsp.populateJTextField("Provenance: program",              "");
      mdsp.populateJTextField("Provenance: version",              "");
      mdsp.populateJTextField("Provenance: arguments",            "");
      mdsp.populateJTextField("Provenance: user",                 "");
      mdsp.populateJTextField("Provenance: machine",              "");
      mdsp.populateJTextField("Provenance: platform",             "");
      
      
      for (String key : simpleEntries.keySet())
         mdsp.populateJTextField(key, "");
   }


   
      
   /**
    * Uploading data to XNAT is a two-stage process. First the data file
    * is placed in the repository, then the metadata are placed in the SQL
    * tables of the PostgreSQL database. This method attempts the repository
    * upload.
    * 
    * Note that we have to override the method in the parent class DataUploader.
    * Loading an MRIW output file is special because not only do we create an
    * MRIW element in the database (icr:roiSetData), but we also turn the ROI
    * information into an RT-STRUCT file and upload that and deal with the
    * calculated maps.
    * @throws Exception
    */
   @Override
   public void uploadMetadata() throws Exception
   {
      errorOccurred = false;
          
      // -----------------------------------------------
      // Step 1: Upload the icr:mriwOutputData metadata.
      // -----------------------------------------------  
      
      if (XNATAccessionID == null)
         XNATAccessionID = getRootElement() + '_' + UIDGenerator.createShortUnique();
      
      String labelPrefix = getStringField("Label");
      
      Document metaDoc = createMetadataXML();
      if (errorOccurred) throw new XNATException(XNATException.FILE_UPLOAD,
                          "There was a problem in creating the metadata to "
                          + "metadata to describe the uploaded DICOM-RT "
                          + "structure set file.\n"
                          + getErrorMessage());
            
      try
      {
         RESTCommand = "/data/archive/projects/" + XNATProject
                       + "/subjects/"    + XNATSubjectID
                       + "/experiments/" + XNATExperimentID
                       + "/assessors/"   + XNATAccessionID;
         
         InputStream is = xnprf.doRESTPut(RESTCommand, metaDoc);
         int         n  = is.available();
         byte[]      b  = new byte[n];
         is.read(b, 0, n);
         String XNATUploadMessage = new String(b);
         
         if ((xnrt.XNATRespondsWithError(XNATUploadMessage)) ||
             (!XNATUploadMessage.equals(XNATAccessionID)))
         {
            errorOccurred = true;
            errorMessage  = XNATUploadMessage;
            throw new XNATException(XNATException.FILE_UPLOAD,
                          "XNAT generated the message:\n" + XNATUploadMessage);
         }
      }
      catch (Exception ex)
      {
         // Here we cater both for reporting the error by throwing an exception
         // and by setting the error variables. When performing the upload via
         // a SwingWorker, it is not easy to retrieve an Exception.
         errorOccurred = true;
         errorMessage = ex.getMessage();
         throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
      }
     
      
      // ---------------------------------------------------------------
      // Step 2: Create and upload the RT-STRUCT file.
      //         This is a multi-step process and will modify the XNAT
      //         database to add icr:roiData and icr:roiSetData objects.
      // ---------------------------------------------------------------
      
      RTStructureSetUploader rtsu = new RTStructureSetUploader(xnprf);
      rtsu.createTempRtStructFile(mriw);
      rtsu.setUploadFile(new File(rtsu.getTempRtStructFilename()));
      
      // Note that this step duplicates some of the parsing work that has
      // just been performed on the MRIW result set file, but the overhead is
      // probably worth it for some cleaner coding here.
      rtsu.prepareUpload();
      if (errorOccurred) throw new Exception(errorMessage);
      
      // The icr:roiSetData object that is created requires a label and a
      // description. If the structure set were uploaded manually, these
      // would be added by the user.
      rtsu.setStringField("Label",  this.getStringField("Label")+ "_ROI");
      rtsu.setStringField("Description", "ROI autogenerated from MRIW output file");
      rtsu.XNATAccessionID = getRootElement() + '_' + UIDGenerator.createShortUnique();
      
      try
      {
         rtsu.uploadMetadata();
         rtsu.uploadFilesToRepository();
      }
      catch (Exception ex)
      {
         errorOccurred = true;
         errorMessage = ex.getMessage();
         throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
      }
   }
 
   
   
   /**
    * Create parts of the metadata XML file that are specific to this
    * particular subclass. Common parts of the XML file are handled by
    * various methods in the parent QCAssessmentUploader. Note that there
    * is no method output, because we are just adding to a work in progress.
    */
   @Override
   public void createSpecificMetadataXML()
   {
      try
      {
         dppXML.delayedWriteEntity("outputType")
                  .delayedWriteText(getStringField("Type"))
               .delayedEndEntity();
         
         for (Map.Entry<String, String> entry : simpleEntries.entrySet())
            dppXML.delayedWriteEntity(entry.getValue())
                     .delayedWriteText(getStringField(entry.getKey()))
                  .delayedEndEntity();
      }
      catch (IOException exIO){{reportError(exIO, "write MRIW specific elements");}}
   }
   
   
   
   
	/**
	 * Get the list of files containing the input data used in the creation of this
	 * XNAT assessor. 
	 * @return ArrayList of String file names
	 */
	@Override
   protected ArrayList<String> getInputCatEntries()
	{
		return mriw.inp.dynFilenames;
	}
	
	
	
   @Override
	public void createPrimaryResourceFile()
	{
		primaryFile					= new XNATResourceFile();
		primaryFile.content		= "EXTERNAL";
		primaryFile.description	= "MRIW file created in an external application";
		primaryFile.format		= "XML";
		primaryFile.file			= uploadFile;
		primaryFile.name			= "MRIW_OUTPUT";
	}
   
	
	
	/**
    * Create additional thumbnail files and RTStruct file for upload with the
	 * MRIW object.
    */
   @Override
   public void createAuxiliaryResourceFiles()
   {
      createInputCatalogueFile("DICOM", "RAW", "image referenced by MRIW");
		
		String fileSep    = System.getProperty("file.separator");
      String filePrefix = XNATGUI.getHomeDir() + "temp" + fileSep + XNATAccessionID;
      try
      {
         ContourRenderer cr = new ContourRenderer(mriw);
         ArrayList<BufferedImage> thumbnails = cr.createImages();
			String thumbnailFile = filePrefix + "_MRIW_ROI_thumbnail_";

         for (int i=0; i<thumbnails.size(); i++)
         {
            File outputFile = new File(thumbnailFile + i);
            ImageIO.write(thumbnails.get(i), "png", outputFile);
            XNATResourceFile rf	= new XNATResourceFile();
				rf.content				= "GENERATED";
				rf.description			= "thumbnail image containing ROI contour";
				rf.format				= "PNG";
				rf.file					= outputFile;
				rf.name					= "MRIW_THUMBNAIL";
            auxiliaryFiles.add(rf);
         }
      }
      catch (Exception ex)
      {
         reportError(ex, "create MRIW thumbnail file");
      }
		
		DicomOutputStream dos = null;
		DicomObject       rts;
		try
		{
			dos = new DicomOutputStream(
						new FileOutputStream(
							new File(filePrefix + "MRIW_RT-STRUCT.dcm")));
			rts = mriw.createDICOM();
			dos.writeItem(rts, TransferSyntax.ExplicitVRLittleEndian);
		}
		catch (Exception ex)
		{
			reportError(ex, "create RT-STRUCT from DICOM");
		}
		finally
		{
			try {if (dos != null) dos.close();} catch (IOException exIO) {}
		}
   }
      
   
   
   
   @Override
   public String getRootElement()
   {
      return "MRIWOutput";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:mriwOutputData";
   }
   
   
   
   
}
