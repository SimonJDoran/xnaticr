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
* Java class: AimImageAnnotationDataUploader.java
* First created on Apr 26, 2016 at 10:04:04 AM
* 
* Object for uploading Annotation and Image Markup (AIM) data
* to XNAT. Note that a single AIM instance file contains an
* annotation collection, which may be made up of a number of 
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: AimImageAnnotationDataUploader.java
* First created on Apr 26, 2016 at 10:04:04 AM
*********************************************************************/

package xnatUploader;

import dataRepresentations.xnatSchema.AimEntitySubclass;
import etherj.aim.Equipment;
import etherj.aim.ImageAnnotation;
import etherj.aim.Markup;
import etherj.aim.Person;
import etherj.aim.TwoDimensionGeometricShape;
import etherj.aim.User;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimImageAnnotationDataMdComplexType;

public class AimImageAnnotationDataUploader extends DataUploader
{
	private ImageAnnotation     ia;
	private User                userParent;
   private Equipment           equipmentParent;
   private Person              personParent;
	private String              assocRegionSetId;
	private Map<String, String> markupRegionMap;
	private List<String>        subclassIdList;
	
	
	public AimImageAnnotationDataUploader(XNATProfile xnprf)
	{
		super(xnprf);  
	}
   
   
   @Override
   public void uploadMetadataAndCascade() throws XNATException, DataFormatException, IOException
   {
      errorOccurred = false;
      
      // Upload the metadata for the icr:aimImageAnnotation.
		// The "cascade" part of the process involves separately uploading metadata
		// for each of the individual bits (icr:aimEntitySubclass) of the image
		// annotation. The ids of all the separate bits are stored in a list that
		// is uploaded with the icr:aimImageAnnotation metadata.
		subclassIdList = new ArrayList<>();
		for (Markup mku : ia.getMarkupList())
			subclassIdList.add(AimEntitySubclass.MARKUP + "_" + mku.getUid());
		
		XNATAccessionID = ia.getUid();
      super.uploadMetadataAndCascade();
      
		// This is the "cascade" bit. Now set an upload in train for a separate
		// icr:aimEntitySubclass for each of the bits of the image annotation.		
      for (Markup mku : ia.getMarkupList())
      {
			AimEntitySubclass es = new AimEntitySubclass();
			
			es.subclassType          = es.MARKUP;
			es.associatedRegionSetId = assocRegionSetId;
			es.associatedRegionId    = markupRegionMap.get(mku.getUid());
			
			if (mku instanceof TwoDimensionGeometricShape)
			{
				TwoDimensionGeometricShape shape = (TwoDimensionGeometricShape) mku;
				es.description     = shape.getDescription();
				es.shapeIdentifier = Integer.toString(shape.getShapeId());
			}
			 
         AimEntitySubclassDataUploader esu = new AimEntitySubclassDataUploader(xnprf);
         
         try
         {
            esu.setAccessionId(AimEntitySubclass.MARKUP + "_" + mku.getUid());
				esu.setEntitySubclass(es);
				esu.uploadMetadataAndCascade();
         }
         catch (XNATException | DataFormatException | IOException ex)
         {
            errorOccurred = true;
            errorMessage  = ex.getMessage();
            throw ex;
         }
      }         
   }
   
   
	@Override
	protected Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the image annotation to the metadata creator.
		IcrAimImageAnnotationDataMdComplexType iad
				                    = new IcrAimImageAnnotationDataMdComplexType();
				  
		iad.setComment(ia.getComment());
		
		iad.setAimUserName(            userParent.getName());
      iad.setAimUserLoginName(       userParent.getLoginName());
      iad.setAimUserRole(            userParent.getRoleInTrial());
      iad.setAimUserNumberInRole(    userParent.getNumberWithinRoleOfClinicalTrial());
      
      iad.setManufacturerName(       equipmentParent.getManufacturerName());
      iad.setManufacturerModelName(  equipmentParent.getManufacturerModelName());
      iad.setDeviceSerialNumber(     equipmentParent.getDeviceSerialNumber());
      iad.setSoftwareVersion(        equipmentParent.getSoftwareVersion());
      
      iad.setPersonName(             personParent.getName());
      iad.setPersonId(               personParent.getId());
      iad.setPersonBirthDate(        personParent.getBirthDate());
      iad.setPersonSex(              personParent.getSex());
      iad.setPersonEthnicGroup(      personParent.getEthnicGroup());
       
		iad.setAssociatedRegionSetId(  assocRegionSetId);
		iad.setAimEntitySubclassIdList(subclassIdList);
      iad.setNMarkupEntity(          ia.getMarkupList().size());
      
		// More here when the Etherj package is ready.
		
      // Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = iad.createXmlAsRootElement();
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
   public String getRootElement()
   {
      return "AimImageAnnotation";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:aimImageAnnotationData";
   }
   

   @Override
   protected void createPrimaryResource()
   {
      // There is no primary resource associated with an AIM ImageAnnotation
      // entity. The AimImageAnnotationCollectionDataUploader does the upload
      // of the AIM instance XML file. 
   }
	

   @Override
   protected void createAuxiliaryResources()
   {
      // There are no auxiliary resources associated with an AIM ImageAnnotation.
      // The AimImageAnnotationCollectionDataUploader kicks off a separate upload
      // of an RT-STRUCT file, which, in turn archives the ROI objects. Hence,
      // nothing needs to be done here.
   }
	
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }
	
	
	void setImageAnnotation(ImageAnnotation ia)
	{
		this.ia = ia;
	}
	
	
	void setMarkupRegionMap(Map<String, String> map)
	{
		markupRegionMap = map;
	}
   
   
   void setAssociatedRegionSetId(String s)
   {
      assocRegionSetId = s;
   }
	
	
	void setUserParent(User u)
	{
		userParent = u;
	}
   
   
   void setEquipmentParent(Equipment e)
	{
		equipmentParent = e;
	}
   
   
   void setPersonParent(Person p)
	{
		personParent = p;
	}	
}
