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
* Java class: AimImageAnnotationCollectionDataUploader.java
* First created on 25 April, 2016 at 3:00 PM
* 
* Object for uploading Annotation and Image Markup (AIM) instance
* files to XNAT 
*********************************************************************/

package xnatUploader;

import etherj.XmlException;
import etherj.aim.AimToolkit;
import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import exceptions.DataFormatException;
import exceptions.XMLException;
import generalUtilities.DicomXnatDateTime;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimImageAnnotationCollectionDataMdComplexType;
import xnatRestToolkit.XnatResource;


public class AimImageAnnotationCollectionDataUploader extends DataUploader
{
   private ImageAnnotationCollection iac;
   private Set<String> studyUidSet       = new LinkedHashSet<>();
	private Set<String> seriesUidSet      = new LinkedHashSet<>();
	private Set<String> sopInstanceUidSet = new LinkedHashSet<>();

   public AimImageAnnotationCollectionDataUploader(XNATProfile xnprf)
   {
      super(xnprf);
   }

  /**
    * Open and read the specified file.
    * Note that the default type of file is XML, but this method will be over-
    * ridden in subclasses to allow us to open arbitrary file types, such as
    * DICOM.
    * @return a boolean variable with true if the file was opened successfully
    *         and false otherwise.
    */
	@Override
   public boolean readFile()
   {
		// James d'Arcy's Etherj package opens and reads AIM files as a single
      // method call, so nothing is needed here.
      return true;				
   }
	
	/**
    * Parse an AIM instance to extract the relevant metadata.
	 * Note that because an external library Ether is being used, most of the
	 * parsing of the XML is actually performed in etherj.aim.XmlParser.
	 * @return true if the parsing is successful, false otherwise.
    */
	@Override
   public boolean parseFile()
   {     
		try
		{
			iac = (AimToolkit.getToolkit().createXmlParser()).parse(uploadFile);
		}
		catch (XmlException | IOException ex)
		{
			errorMessage = "Problem reading AIM instance file: " + ex.getMessage();
			logger.error(errorMessage);
			errorOccurred = true;
			return false;
		}
      
      try
      {
         date = DicomXnatDateTime.convertAimToXnatDate(iac.getDateTime());
         time = DicomXnatDateTime.convertAimToXnatTime(iac.getDateTime());
      }
      catch (DataFormatException exDF)
      {
         errorMessage = "Incorrect date-time format in AIM metadata." + exDF.getMessage();
         logger.error(errorMessage);
         errorOccurred = true;
         return false;
      }
      
      // Extract the image reference data from the AIM structure.
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (ImageReference ir : ia.getReferenceList())
         {
            if (ir instanceof DicomImageReference)
            {
               DicomImageReference dir    = (DicomImageReference) ir;
               ImageStudy          study  = dir.getStudy();
               studyUidSet.add(study.getInstanceUid());
               ImageSeries         series = study.getSeries();
               seriesUidSet.add(series.getInstanceUid());
               for (Image im : series.getImageList())
               {
                  sopInstanceUidSet.add(im.getInstanceUid());
               }    
            }
         }
      }

		return true;
   }
  
   
   @Override
   public void updateParseFile()
   {
      //TODO
   }
   
	
	@Override
   public void clearFields(MetadataPanel mdp)
   {
      mdp.populateJTextField("Label", "", true);
      mdp.populateJTextField("Note",  "", true);
   }


   
      
   /**
	 * Uploading data to XNAT is a two-stage process. First the data file
	 * is placed in the repository, then the metadata are placed in the SQL
	 * tables of the PostgreSQL database. This method attempts the repository
    * upload.
    *
	 * The AIM upload is particularly complicated, because a single AIM file
	 * can describe multiple ROIs. In this sense, the situation is a little like
	 * the case for an RT-STRUCT, but it is even more involved, because one also
	 * needs to upload the references to the various radiological observations.
	 * 
	 * THE AIM UML document is rather complex and so the translation into the
	 * XNAT schema necessarily misses out some of the relationships.
	 * 
	 * @throws Exception 
	 */
   @Override
   public void uploadMetadata() throws Exception
   {
      errorOccurred = false;
          
      // -----------------------------------------------------------------
      // Step 1: Upload the icr:aimImageAnnotationCollectionData metadata.
      // -----------------------------------------------------------------
		
		XNATAccessionID = iac.getUid();
      
      // Pre-calculate the associated RT-STRUCT id.
      
	}   
      
     
	@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrAimImageAnnotationCollectionDataMdComplexType
				    iacd = new IcrAimImageAnnotationCollectionDataMdComplexType();
		
		iacd.setVersion(iac.getAimVersion());
      
      iacd.setAimUserName(          iac.getUser().getName());
      iacd.setAimUserLoginName(     iac.getUser().getLoginName());
      iacd.setAimUserRole(          iac.getUser().getRoleInTrial());
      iacd.setAimUserNumberInRole(  iac.getUser().getNumberWithinRoleOfClinicalTrial());
      
      iacd.setManufacturerName(     iac.getEquipment().getManufacturerName());
      iacd.setManufacturerModelName(iac.getEquipment().getManufacturerModelName());
      iacd.setDeviceSerialNumber(   iac.getEquipment().getDeviceSerialNumber());
      iacd.setSoftwareVersion(      iac.getEquipment().getSoftwareVersion());
      
      iacd.setPersonName(           iac.getPerson().getName());
      iacd.setPersonId(             iac.getPerson().getId());
      iacd.setPersonBirthDate(      iac.getPerson().getBirthDate());
      iacd.setPersonSex(            iac.getPerson().getSex());
      iacd.setPersonEthnicGroup(    iac.getPerson().getEthnicGroup());
      
		iacd.setnumImageAnnotations(iac.getAnnotationCount());
      
     
		
		// Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = iacd.createXmlAsRootElement();
		}
		catch (IOException | XMLException ex)
		{
			// This really shouldn't happen, but the mechanism is there to handle
			// it if it does.
			errorOccurred = true;
			errorMessage  = ex.getMessage();
		}
		
		return metaDoc;
	}
	
	
	@Override
	public void createPrimaryResource()
	{
		StringBuilder description = new StringBuilder();
		description.append("AIM instance file");

		primaryResource = new XnatResource(uploadFile,
		                                   "out",
		                                   "AIM-INSTANCE",
				                             "XML",
		                                   "EXTERNAL",
		                                   description.toString(),
				                             uploadFile.getName());
	}
   
   
   
   @Override
   public void createAuxiliaryResources()
   {
      // There are no auxiliary resources associated with an
      // AimImageAnnotationCollection. uploadMetadata() above kicks off a
      // separate upload of an RT-STRUCT, which, in turn archives the ROI objects.
      // Hence, nothing needs to be done here.
   }
   
   
   @Override
   public String getRootElement()
   {
      return "AimImageAnnotationCollection";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:aimImageAnnotationCollectionData";
   }
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }
	
	
	@Override
	public void updateVariablesForEditableFields(MetadataPanel mdp)
	{
		labelPrefix = mdp.getJTextFieldContents("Label");
		note        = mdp.getJTextFieldContents("Note");
	}
	
	
	@Override
   public List<String> getEditableFields()
   {
      List<String> s = new ArrayList<>();
		s.add("Label");
		s.add("Note");
		
		return s;
   }
   
	
	@Override
   public List<String> getRequiredFields()
   {
      List<String> s = new ArrayList<>();
		s.add("Label");
		
		return s;
   }
   
   
	
	
	@Override
   public boolean rightMetadataPresent()
   {
      return (!labelPrefix.equals("")) &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanIdSet.isEmpty());
   }
   
}

