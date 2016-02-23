/********************************************************************
* Copyright (c) 2016, Institute of Cancer Research
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

/********************************************************************
* @author Simon J Doran
* Java class: RtStructDataUploader.java
* First created on Feb 23, 2016 at 9:36:36 AM
* 
* Object for uploading files to XNAT that conform to DICOM's
* RT-STRUCT format
*********************************************************************/

package xnatUploader;

import dataRepresentations.ROI_old;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.UIDGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import org.w3c.dom.Document;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xnatMetadataCreators.IcrRoiSetDataMdComplexType;

public class RtStructDataUploader extends DataUploader
{
	@Override
   public void clearFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("Label",                            "", true);
      mdsp.populateJTextField("Note",                             "", true);
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
      errorOccurred = false;
      
      // The icr:roiSetData and icr:roiData XML files are mutually dependent
      // inasmuch as the ROI_old Set needs to know the IDs of the contained ROIs,
      // whereas each ROI_old needs to know the ID's of all the ROI_old Sets that contain
      // it. In this case, it makes sense to pre-calculate the IDs for all the
      // ROI_old's to be uploaded.
      // N.B. The only reason for instantiating the following uploader is to
      // access the method ru.getRootElement() below.
      ROI_old ru = new ROI_old(xnprf);     
      for (int i=0; i<rts.roiList.length; i++)
      {
         rts.roiList[i].roiXNATID = ru.getRootElement()
                                    + '_' + UIDGenerator.createShortUnique();
      }
      
      
      // -------------------------------------------
      // Step 1: Upload the icr:roiSetData metadata.
      // -------------------------------------------
      
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
         RESTCommand = getMetadataUploadCommand();
         
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
         
         
         rts.roiSetID    = XNATAccessionID;
         rts.roiSetLabel = getStringField("Label"); // TODO: This won't work for batch mode. 
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
     
      
      // ----------------------------------------------------------
      // Step 2: Upload the icr:roiData metadata and data files for
      //         each ROI_old referred to by the structure set.
      // ----------------------------------------------------------    
      
		if (errorOccurred)
		{
			throw new XNATException(XNATException.FILE_UPLOAD, errorMessage);
		}
		
      for (int i=0; i<rts.roiList.length; i++)
      {
         ru = new ROI_old(rts, i, labelPrefix, uploadStructure);
         try
         {
            ru.XNATAccessionID = rts.roiList[i].roiXNATID;
            ru.associatedRoiSetIDs = new ArrayList<String>();
            ru.associatedRoiSetIDs.add(XNATAccessionID);
            ru.uploadMetadata();
            ru.uploadFilesToRepository();
         }
         catch (Exception ex)
         {
            errorOccurred = true;
            errorMessage = "Problem uploading ROI data to XNAT.\n"
                           + ex.getMessage();
            throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
         }
      }
         
   }
	
	
	/**
	 * Get the list of files containing the input data used in the creation of this
	 * XNAT assessor. 
	 * @return ArrayList of String file names
	 */
	@Override
   protected ArrayList<String> getInputCatEntries()
	{
		ArrayList<String>	fileURIs	= new ArrayList<>();
		Set<String>			ks			= rts.fileSOPMap.keySet();
      for (String s : ks) fileURIs.add(rts.fileSOPMap.get(s));
		
		return fileURIs;
	}
	
	
	
	@Override
	public void createPrimaryResourceFile()
	{
		primaryFile					= new XNATResourceFile();
		primaryFile.content		= "EXTERNAL";
		primaryFile.description	= "DICOM RT-STRUCT file created in an external application";
		primaryFile.format		= "DICOM";
		primaryFile.file			= uploadFile;
		primaryFile.name			= "RT-STRUCT";
		primaryFile.inOut			= "out";
	}
   
   
   
   /**
    * Create additional thumbnail files for upload with the DICOM-RT structure set.
    */
   @Override
   public void createAuxiliaryResourceFiles()
   {
      // In the first instance, the only auxiliary file needed is the
		// input catalogue, since the referenced ROI_old objects already contain
		// the required thumbnails.
      // TODO: Consider whether some composite visualisation is needed to
      // summarise all the ROI_old's making up the ROISet object.
		createInputCatalogueFile("DICOM", "RAW", "referenced contour image");
   }
	
	@Override
	public Document createMetadataXML()
	{
		IcrRoiSetDataMdComplexType roiSet  = new IcrRoiSetDataMdComplexType();
		
		Document metadoc = null;
		try
		{
			metadoc = roiSet.createXmlAsRootElement();
		}
		catch (IOException | XMLException ex){}
		
		return metadoc;
		
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
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }


}
