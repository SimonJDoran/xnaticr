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
* Java class: RTStructureSetDataUploader.java
* First created in October 2010
* 
* Object for uploading files to XNAT that conform to DICOM's
* RT-STRUCT format
*********************************************************************/

package xnatUploader;

import dataRepresentations.ROI_old;
import java.util.ArrayList;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import exceptions.XNATException;
import exceptions.DataFormatException;
import java.io.*;
import java.util.Set;
import org.dcm4che2.io.DicomOutputStream;
import dataRepresentations.RtStructWriter;
import dataRepresentations.RTStruct_old;
import generalUtilities.UIDGenerator;
import org.apache.log4j.Logger;
import xnatDAO.XNATGUI;


public class RTStructureSetUploader extends QCAssessmentDataUploader
{
   static    Logger      logger = Logger.getLogger(RTStructureSetUploader.class);
   protected DicomObject bdo;
   protected RTStruct_old    rts;
   
   
   /**
    * Constructor for the RTStructureSetUploader. Note that the XNAT profile
    * xnprf is not used here, but needed in the abstract class DataUploader.
    * @param xnprf 
    */
   public RTStructureSetUploader(XNATProfile xnprf)
   {
      super(xnprf);
   }
   
      
   /**
    * Open and read the specified file.
    * This method will be overrides the default (which reads XML), to allow
    * us to open an RT Structure Set DICOM file.
    * @return a boolean variable with true if the file was opened successfully
    *         and false otherwise.
    */
   @Override
   public boolean readFile()
   {
      bdo = new BasicDicomObject();
      try
      {
         BufferedInputStream bis
            = new BufferedInputStream(new FileInputStream(uploadFile));
         DicomInputStream dis = new DicomInputStream(bis);
         dis.readDicomObject(bdo, -1);
      }
      catch (IOException exIO)
      {
         errorOccurred = true;
         errorMessage  = "Unable to open selected file. \n\n" + exIO.getMessage();
         return false;
      }

      return true;
   }
   
   
   /**
    * Parse a DICOM RT-Structure Set file to extract the relevant metadata.
	 * The parsing to create the RTStruct_old DataRepresentation occurs in the
 relevant constructor. This routine extracts the variables that are
 needed by the uploader.
	 * @return true if the parsing is successful, false otherwise.
    */
   @Override
   public boolean parseFile()
   {  
		try
      {
         rts                  = new RTStruct_old(bdo, xnprf);
         rts.version          = version;
         date                 = rts.convertToXNATDate(rts.structureSetDate);
         time                 = rts.convertToXNATTime(rts.structureSetTime);
         XNATProject          = rts.XNATProjectID;
         XNATSubjectID        = rts.XNATSubjectID;
         XNATExperimentID     = rts.XNATExperimentID;
         XNATScanID           = rts.XNATScanID;
         ambiguousSubjExp     = rts.ambiguousSubjExp;
                
         // Note that only a small minority of the fields in the structure set
         // are of interest to the user directly. So whilst we read in the whole
         // structure set, for later output to an XNAT XML upload file, we place
         // very little of the information into the string fields of the user
         // interface.       
         setStringField("Provenance: platform",       bdo.getString(Tag.Manufacturer) + " TPS");
         setStringField("Provenance: machine",        bdo.getString(Tag.StationName));
         setStringField("Provenance: program",        bdo.getString(Tag.ManufacturerModelName) + " RTSTRUCT");
         setStringField("Provenance: timestamp",
            convertToDateTime(bdo.getString(Tag.StructureSetDate), bdo.getString(Tag.StructureSetTime)));
         
         // There is no obvious information in the DICOM StructureSet file about
         // the user who created the file. The nearest we have (if present) is the
         // person who "interpreted" the ROIs. However, user is a required element
         // so set "Unknown" and if we find any additional information, we over-write it.
         setStringField("Provenance: user",           "Unknown user");         
         for (int i=0; i<rts.roiObsList.length; i++)
            if (rts.roiObsList[i].roiInterpreter != null)
               setStringField("Provenance: user", rts.roiObsList[i].roiInterpreter);
         
         setStringField("Number of ROIs contained", (new Integer(rts.roiList.length)).toString());
         
         //debugRTStructWriter();
      }   

      catch (Exception ex)
      {
         errorOccurred = true;
         errorMessage  = "Unable to load selected RT-STRUCT data.\n"
                         + ex.getMessage() + "\n\n";
         return false;
      }
      
      return true;
   }
      
      
   /**
    * Update the parsing of the file to take into account the most
    * recent selection of either subject or experiment labels from the
    * JCombo boxes in the user interface.
    */
   @Override
   public void updateParseFile()
   {
      rts.XNATExperimentID = XNATExperimentID;
      rts.XNATSubjectID    = XNATSubjectID;
      
		ArrayList<String> issues = new ArrayList<>();
		rts.checkForScansInDatabase(issues);
		
		// This error should not happen, since both XNATExperimentID and
		// XNATSubjectID should have been the result of a previous scan of
		// database for appropriately matching files.
      if (!issues.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String issue : issues) sb.append(issue).append("\n");
			
         throw new RuntimeException("Error in updateParseFile: " + sb.toString() );
      }
   }
           
   
   @Override
   public void populateFields(MetadataPanel mdsp)
   {
      super.populateFields(mdsp);
      mdsp.populateJTextField("Patient name",               rts.patientName);
      mdsp.populateJTextField("XNAT subject ID",            XNATSubjectID);
      mdsp.populateJTextField("XNAT assessment ID",         "Generated by XNAT on data upload");
      mdsp.populateJTextField("Associated project",         XNATProject);
      
      mdsp.populateJTextField("Label", "", true);
      mdsp.populateJTextField("Associated XNAT image session ID", XNATExperimentID);
      mdsp.populateJTextField("Note", "", true);
      mdsp.populateJTextField("Number of ROIs contained",   getStringField("Number of ROIs contained"));
      mdsp.populateJTextField("Structure set label",        rts.structureSetLabel);
      mdsp.populateJTextField("Structure set name",         rts.structureSetName);
      mdsp.populateJTextField("Structure set description",  rts.structureSetDescription);
     
      try
      {
         mdsp.populateJTextField("Data acquisition date",   rts.convertToXNATDate(rts.studyDate));
         mdsp.populateJTextField("Data acquisition time",   rts.convertToXNATTime(rts.studyTime));    
         mdsp.populateJTextField("Structure set date",      rts.convertToXNATDate(rts.structureSetDate));
         mdsp.populateJTextField("Structure set time",      rts.convertToXNATDate(rts.structureSetTime));
      }
      catch (DataFormatException exIgnore){}
      
      mdsp.populateJTextField("Provenance: program",        getStringField("Provenance: program"));
      mdsp.populateJTextField("Provenance: machine",        getStringField("Provenance: machine"));
      mdsp.populateJTextField("Provenance: timestamp",      getStringField("Provenance: timestamp"));
      mdsp.populateJTextField("Provenance: user",           getStringField("Provenance: user"));
      mdsp.populateJTextField("Provenance: platform",       getStringField("Provenance: platform"));
   }


   @Override
   public void clearFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("Patient name",                     "");
      mdsp.populateJTextField("XNAT subject ID",                  "");
      mdsp.populateJTextField("XNAT assessment ID",               "");
      mdsp.populateJTextField("Associated project",               "");
      mdsp.populateJTextField("Associated XNAT image session ID", "");
      mdsp.populateJTextField("Data acquisition date",            "");
      mdsp.populateJTextField("Data acquisition time",            "");
      mdsp.populateJTextField("Label",                            "", true);
      mdsp.populateJTextField("Note",                             "", true);
      mdsp.populateJTextField("Number of ROIs contained",         "");
      mdsp.populateJTextField("Structure set label",              "");
      mdsp.populateJTextField("Structure set name",               "");
      mdsp.populateJTextField("Structure set description",        "");
      mdsp.populateJTextField("Structure set date",               "");
      mdsp.populateJTextField("Structure set time",               "");
      mdsp.populateJTextField("Provenance: program",              "");
      mdsp.populateJTextField("Provenance: machine",              "");
      mdsp.populateJTextField("Provenance: timestamp",            "");
      mdsp.populateJTextField("Provenance: user",                 "");
      mdsp.populateJTextField("Provenance: platform",             "");
   }


   
   /**
    * Uploading data to XNAT is a two-stage process. First the data file
    * is placed in the repository, then the metadata are placed in the SQL
    * tables of the PostgreSQL database. This method attempts the repository
    * upload.
    * 
    * Note that we have to override the method in the parent class DataUploader.
    * Loading an RT-STRUCT file is special because not only do we create a
    * set-of-ROIs element in the database (icr:roiSetData), but we also create
    * all the individual ROIs as separate icr:roiData objects.
    * @throws Exception
    */
   @Override
   public void uploadMetadata() throws Exception
   {
//      errorOccurred = false;
//      
//      // The icr:roiSetData and icr:roiData XML files are mutually dependent
//      // inasmuch as the ROI_old Set needs to know the IDs of the contained ROIs,
//      // whereas each ROI_old needs to know the ID's of all the ROI_old Sets that contain
//      // it. In this case, it makes sense to pre-calculate the IDs for all the
//      // ROI_old's to be uploaded.
//      // N.B. The only reason for instantiating the following uploader is to
//      // access the method ru.getRootElement() below.
//      ROI_old ru = new ROI_old(xnprf);     
//      for (int i=0; i<rts.roiList.length; i++)
//      {
//         rts.roiList[i].roiXNATID = ru.getRootElement()
//                                    + '_' + UIDGenerator.createShortUnique();
//      }
//      
//      
//      // -------------------------------------------
//      // Step 1: Upload the icr:roiSetData metadata.
//      // -------------------------------------------
//      
//      if (XNATAccessionID == null)
//         XNATAccessionID = getRootElement() + '_' + UIDGenerator.createShortUnique();
//      
//      String labelPrefix = getStringField("Label");
//      
//      Document metaDoc = createMetadataXML();
//      if (errorOccurred) throw new XNATException(XNATException.FILE_UPLOAD,
//                          "There was a problem in creating the metadata to "
//                          + "metadata to describe the uploaded DICOM-RT "
//                          + "structure set file.\n"
//                          + getErrorMessage());
//            
//      try
//      {
//         RESTCommand = getMetadataUploadCommand();
//         
//         InputStream is = xnprf.doRESTPut(RESTCommand, metaDoc);
//         int         n  = is.available();
//         byte[]      b  = new byte[n];
//         is.read(b, 0, n);
//         String XNATUploadMessage = new String(b);
//         
//         if ((xnrt.XNATRespondsWithError(XNATUploadMessage)) ||
//             (!XNATUploadMessage.equals(XNATAccessionID)))
//         {
//            errorOccurred = true;
//            errorMessage  = XNATUploadMessage;
//            throw new XNATException(XNATException.FILE_UPLOAD,
//                          "XNAT generated the message:\n" + XNATUploadMessage);
//         }
//         
//         
//         rts.roiSetID    = XNATAccessionID;
//         rts.roiSetLabel = getStringField("Label"); // TODO: This won't work for batch mode. 
//      }
//      catch (Exception ex)
//      {
//         // Here we cater both for reporting the error by throwing an exception
//         // and by setting the error variables. When performing the upload via
//         // a SwingWorker, it is not easy to retrieve an Exception.
//         errorOccurred = true;
//         errorMessage = ex.getMessage();
//         throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
//      }
//     
//      
//      // ----------------------------------------------------------
//      // Step 2: Upload the icr:roiData metadata and data files for
//      //         each ROI_old referred to by the structure set.
//      // ----------------------------------------------------------    
//      
//		if (errorOccurred)
//		{
//			throw new XNATException(XNATException.FILE_UPLOAD, errorMessage);
//		}
//		
//      for (int i=0; i<rts.roiList.length; i++)
//      {
//         ru = new ROI_old(rts, i, labelPrefix, uploadStructure);
//         try
//         {
//            ru.XNATAccessionID = rts.roiList[i].roiXNATID;
//            ru.associatedRoiSetIDs = new ArrayList<String>();
//            ru.associatedRoiSetIDs.add(XNATAccessionID);
//            ru.uploadMetadata();
//            ru.uploadFilesToRepository();
//         }
//         catch (Exception ex)
//         {
//            errorOccurred = true;
//            errorMessage = "Problem uploading ROI data to XNAT.\n"
//                           + ex.getMessage();
//            throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
//         }
//      }
//         
//   }
//	
//	
//	/**
//	 * Get the list of files containing the input data used in the creation of this
//	 * XNAT assessor. 
//	 * @return ArrayList of String file names
//	 */
//	@Override
//   protected ArrayList<String> getInputCatEntries()
//	{
//		ArrayList<String>	fileURIs	= new ArrayList<>();
//		Set<String>			ks			= rts.fileSOPMap.keySet();
//      for (String s : ks) fileURIs.add(rts.fileSOPMap.get(s));
//		
//		return fileURIs;
//	}
//	
//	
//	
//	@Override
//	public void createPrimaryResourceFile()
//	{
//		primaryFile					= new XNATResourceFile();
//		primaryFile.content		= "EXTERNAL";
//		primaryFile.description	= "DICOM RT-STRUCT file created in an external application";
//		primaryFile.format		= "DICOM";
//		primaryFile.file			= uploadFile;
//		primaryFile.name			= "RT-STRUCT";
//		primaryFile.inOut			= "out";
//	}
//   
//   
//   
//   /**
//    * Create additional thumbnail files for upload with the DICOM-RT structure set.
//    */
//   @Override
//   public void createAuxiliaryResourceFiles()
//   {
//      // In the first instance, the only auxiliary file needed is the
//		// input catalogue, since the referenced ROI_old objects already contain
//		// the required thumbnails.
//      // TODO: Consider whether some composite visualisation is needed to
//      // summarise all the ROI_old's making up the ROISet object.
//		createInputCatalogueFile("DICOM", "RAW", "referenced contour image");
   }
   
   
   
   /**
    * Create parts of the metadata XML file that are specific to this
    * particular subclass. Common parts of the XML file are handled by
    * various methods in the parent QCAssessmentUploader.
    */
   @Override
   public void createSpecificMetadataXML()
   {
//      try
//      {
//         dppXML.delayedWriteEntity("subjectID")
//                  .delayedWriteText(XNATSubjectID)
//               .delayedEndEntity() 
//                                 
//               .delayedWriteEntity("dcmPatientName")
//                  .delayedWriteText(rts.patientName)
//               .delayedEndEntity()
//                 
//               .delayedWriteEntity("nROIs")
//                  .delayedWriteText(getStringField("Number of ROIs contained"))
//               .delayedEndEntity()
//
//               
//               .delayedWriteEntity("associatedStructureSetUID")
//                  .delayedWriteText(rts.structureSetUID)
//               .delayedEndEntity()
//
//               .delayedWriteEntity("structureSetLabel")
//                  .delayedWriteText(getStringField("Structure set label"))
//               .delayedEndEntity()
//
//               .delayedWriteEntity("structureSetName")
//                  .delayedWriteText(getStringField("Structure set name"))
//               .delayedEndEntity()
//
//               .delayedWriteEntity("structureSetDescription")
//                  .delayedWriteText(getStringField("Structure set description"))
//               .delayedEndEntity()
//
//               .delayedWriteEntity("structureSetDate")
//                  .delayedWriteText(getStringField("Structure set date"))
//               .delayedEndEntity()
//
//               .delayedWriteEntity("structureSetTime")
//                  .delayedWriteText(getStringField("Structure set time"))
//               .delayedEndEntity();
//         
//         
//         dppXML.delayedWriteEntity("roiDisplays");
//         for (int i=0; i<rts.roiList.length; i++)
//         {
//            // Most of the elements of icr:roiDisplayData cannot be
//            // filled in. The ROI_old line colour is present in
//            // the structure set, but the other properties are
//            // extensions not found in the structure set and the intention is
//            // to let future applications have the option of assigning non-
//            // DICOM properties such as ROI_old shading to the roiSet.
//            int[] c = rts.roiContourList[rts.roiList[i].correspondingROIContour].roiDisplayColour;
//            dppXML.delayedWriteEntity("roiDisplay")
//               
//               .delayedWriteEntity("roiID")
//                  .delayedWriteText(rts.roiList[i].roiXNATID)
//               .delayedEndEntity()
//                    
//               .delayedWriteEntity("lineColour")
//                  .delayedWriteText(c[0] + "/" + c[1] + "/" + c[2])
//               .delayedEndEntity()
//            
//            .delayedEndEntity();
//         }
//         dppXML.delayedEndEntity();
//         
//         
//         dppXML.delayedWriteEntity("referencedFramesOfReference");
//         for (int i=0; i<rts.fORList.length; i++)
//         {
//            dppXML.delayedWriteEntity("referencedFrameOfReference")
//                    
//               .delayedWriteEntity("frameOfReferenceUID")
//                  .delayedWriteText(rts.fORList[i].UID)
//               .delayedEndEntity()
//                    
//               .delayedWriteEntity("frameOfReferenceRelationships");
//               
//               for (int j=0; j<rts.fORList[i].nRelatedFOR; j++)
//               {
//                  dppXML.delayedWriteEntity("frameOfReferenceRelationship")
//                          
//                     .delayedWriteEntity("relatedFrameOfReferenceUID")
//                        .delayedWriteText(rts.fORList[i].relatedFOR[j].UID)
//                     .delayedEndEntity()
//                          
//                     .delayedWriteEntity("frameOfReferenceTransformationMatrix")
//                        .delayedWriteText(rts.fORList[i].relatedFOR[j].transformationMatrix)
//                     .delayedEndEntity()
//                          
//                     .delayedWriteEntity("frameOfReferenceTransformationComment")
//                        .delayedWriteText(rts.fORList[i].relatedFOR[j].transformationComment)
//                     .delayedEndEntity()
//                          
//                 .delayedEndEntity();
//               }
//               
//               dppXML.delayedEndEntity();
//            dppXML.delayedEndEntity();
//         }
//         dppXML.delayedEndEntity();
//         
//      }
//      catch (IOException exIO){{reportError(exIO, "write RT-STRUCT specific elements");}}
   }
   
   
   
   
   
   
   public String convertToDateTime(String date, String time) throws DataFormatException
   {
      String month;
      String day;
      String year;
      String hour;
      String minute;
      String second;
      
      try
      {
         month  = date.substring(4, 6);
         day    = date.substring(6, 8);
         year   = date.substring(0, 4);
         hour   = time.substring(0, 2);
         minute = time.substring(2, 4);
         second = time.substring(4, 6);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return new String(year + "-" + month + "-" + day
                        + "T" + hour + ":" + minute + ":" + second);
   }
   
   
   
   @Override
   public String[] getRequiredFields()
   {
      return new String[]{"Label", "Note"};
   }
   
   
   @Override
   public boolean rightMetadataPresent()
   {
      return (!getStringField("Label").equals("")) &&
             (!getStringField("Note").equals(""))  &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanID.equals(""));
   }
   
   
   @Override
   public String getRootElement()
   {
      return "ROISet";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:roiSetData";
   }
   
   
// This routine doesn't now work with the latest definition of ContourRenderer.
//
//   protected void debugContourOutput() throws Exception
//   {
//      for (int i=0; i<rts.roiList.length; i++)
//      {
//         ContourRenderer cr = new ContourRenderer(rts,
//                 rts.roiContourList[rts.roiList[i].correspondingROIContour].referencedRoiNumber);
//         
//         ArrayList<BufferedImage> bi = cr.createImages();
//         for (int j=0; j<bi.size(); j++)
//         {
//            File outputFile = new File("/Users/simond/temp/ROI_test/ROI" + i
//                                       + "/contour_" + j + ".png");
//            outputFile.mkdirs();
//            ImageIO.write(bi.get(j), "png", outputFile);
//         }
//      }
//   }
	
	
   
   protected void createTempRtStructFile(RtStructWriter rtsw) throws Exception
   {
      try
      {
         DicomOutputStream dos = new DicomOutputStream(
                                    new BufferedOutputStream(
                                       new FileOutputStream(
                                          new File(getTempRtStructFilename()))));
         dos.writeDicomFile(rtsw.createDICOM());
         dos.close();
      }
      catch (Exception ex)
      {
         throw ex;
      }
   }
   
   
   protected String getTempRtStructFilename()
   {
      String fs = System.getProperty("file.separator");
      return XNATGUI.getHomeDir() + fs + "temp" + fs + "tempRT-STRUCT.dcm";        
   }

	@Override
	protected ArrayList<String> getInputCatEntries()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void createPrimaryResourceFile()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void createAuxiliaryResourceFiles()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
