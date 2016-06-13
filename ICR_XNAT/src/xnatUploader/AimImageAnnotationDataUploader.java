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

import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.AimEntitySubclass;
import dataRepresentations.xnatSchema.Catalog;
import dataRepresentations.xnatSchema.CatalogEntry;
import dataRepresentations.xnatSchema.InvestigatorList;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Resource;
import dataRepresentations.xnatSchema.Scan;
import etherj.aim.DicomImageReference;
import etherj.aim.Equipment;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import etherj.aim.Markup;
import etherj.aim.Person;
import etherj.aim.TwoDimensionGeometricShape;
import etherj.aim.User;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.UidGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimImageAnnotationDataMdComplexType;

public class AimImageAnnotationDataUploader extends DataUploader
{
	private ImageAnnotation     ia;
	private User                     userParent;
   private Equipment                equipmentParent;
   private Person                   personParent;
   private String                   labelParent;
	private String                   assocRegionSetId;
	private Map<String, String>      markupRegionMap;
	private List<String>             subclassIdList;
   private String                   dicomSubjNameParent;
   private Map<String, String>      filenameSopMap;
   private Map<String, String>      filenameScanMap;
   private Map<String, String>      sopFilenameMap;
   private Map<String, DicomObject> sopDoMap;
	Set<String>                      sopSet;
	private Map<String, String>      uidIdMap;
	
	public AimImageAnnotationDataUploader(XNATProfile xnprf)
	{
		super(xnprf);  
	}
   
   
   @Override
   public void uploadMetadataAndCascade() throws XNATException, DataFormatException, IOException
   {
      errorOccurred = false;
      
      label = labelParent + "_" + ia.getName();
      
      // Upload the metadata for the icr:aimImageAnnotation.
		// The "cascade" part of the process involves separately uploading metadata
		// for each of the individual bits (icr:aimEntitySubclass) of the image
		// annotation. The ids of all the separate bits are stored in a list that
		// is uploaded with the icr:aimImageAnnotation metadata.
		uidIdMap       = new HashMap<>();
		subclassIdList = new ArrayList<>();
		for (Markup mku : ia.getMarkupList())
		{
			String id = AimEntitySubclass.MARKUP + "_" + UidGenerator.createShortUnique();
			uidIdMap.put(mku.getUid(), id);
			subclassIdList.add(id);
		}
      
      // From the parent ImageAnnotationCollection, we have lists of all the
      // DICOM images contributing to the collection. We now need to break this
      // down into just the set of all images relevant to this annotation.
		sopSet = new HashSet<>();
      for (ImageReference ir : ia.getReferenceList())
         {
            if (ir instanceof DicomImageReference)
            {
               DicomImageReference dir    = (DicomImageReference) ir;
               ImageStudy          study  = dir.getStudy();
               ImageSeries         series = study.getSeries();
               for (Image im : series.getImageList())
                   sopSet.add(im.getInstanceUid());
            }
         }
      
      
      super.uploadMetadataAndCascade();
      
		// This is the "cascade" bit. Now set an upload in train for a separate
		// icr:aimEntitySubclass for each of the bits of the image annotation.		
		
		
		for (Markup mku : ia.getMarkupList())
      {
			AimEntitySubclass es    = new AimEntitySubclass();
			Set<String>       mkSop = new HashSet<>();
			
			es.subclassType                      = es.MARKUP;
			es.associatedRegionSetId             = assocRegionSetId;
			es.associatedRegionId                = markupRegionMap.get(mku.getUid());
			es.associatedAimEntitySubclassIdList = new ArrayList<>();
         for (Map.Entry<String, String> me : uidIdMap.entrySet())
			{
            if (me.getKey().equals(mku.getUid()))
					es.associatedAimEntitySubclassIdList.add(me.getValue());
			}
		
			if (mku instanceof TwoDimensionGeometricShape)
			{
				TwoDimensionGeometricShape shape = (TwoDimensionGeometricShape) mku;
				es.description     = shape.getDescription();
				es.shapeIdentifier = Integer.toString(shape.getShapeId());
				mkSop.add(shape.getImageReferenceUid());
			}
			 
         AimEntitySubclassDataUploader esu = new AimEntitySubclassDataUploader(xnprf);
         
         try
         {
            esu.setAccessionId(uidIdMap.get(mku.getUid()));
				esu.XNATProject      = XNATProject;
            esu.XNATExperimentID = XNATExperimentID;
            esu.XNATSubjectID    = XNATSubjectID;
            esu.date             = date;
            esu.time             = time;
				esu.setSops(sopFilenameMap, filenameScanMap, mkSop);
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
      
		// More here when the Etherj package is ready. For the moment, set all
      // the other N...Entity variables to zero.
      iad.setNTaskContextEntity(0);
      iad.setNInferenceEntity(0);
      iad.setNAnnotationRoleEntity(0);
      iad.setNCalculationEntity(0);
      iad.setNImagingObservationEntity(0);
      iad.setNImagingPhysicalEntity(0);
		
      // IcrAimImageAnnotationDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// iacd.setType();  Not currently sure what should go here.
		iad.setXnatSubjId(XNATSubjectID);
	   iad.setDicomSubjName(dicomSubjNameParent);
		
		// Although the full version of Scan, including scan and slice image
		// statistics is implemented, this is overkill here and
		// the only part of scan for which information is available is the
		// list of scan IDs. 
		Set<String> idSet = new HashSet<>();
		for (String sop : sopSet) idSet.add(filenameScanMap.get(sopFilenameMap.get(sop)));
		
		List<Scan> lsc = new ArrayList<>();
		for (String id : idSet)
		{
			Scan sc = new Scan();
			sc.id = id;
			lsc.add(sc);
		}
		iad.setScanList(lsc);
      
      
      // IcrGenericImageAssessmentDataMdComplexType inherits from XnatImageAssessorDataMdComplexType.
		
		// The "in" section of the assessor XML contains all files that were already
		// in the database at the time of upload, whilst the "out" section lists
		// the files that added at the time of upload, including those generated
		// automatically. In this, the only generated files are the snapshots, but
		// this information is already included in the separately uploaded ROI
		// metadata files and need not be duplicated here.
		inputCat = new Catalog();
      List<CatalogEntry> lce = new ArrayList<>();
      for (String sop : sopSet)
		{
			CatalogEntry ce   = new CatalogEntry();
			ce.name           = sopFilenameMap.get(sop);
         ce.id             = sop;
         ce.format         = "DICOM";
         ce.content        = "IMAGE";
         lce.add(ce);
      }
      CatalogEntry ce      = new CatalogEntry();
      ce.name              = (uploadFile == null) ? "GENERATED" : uploadFile.getName();
      ce.id                = "AIM_Instance_" + XNATAccessionID;
      ce.format            = "AIM";
      ce.content           = "Markup";
      lce.add(ce);
      
      inputCat.entryList   = lce;
      inputCat.id          = "INPUT_FILES";
      inputCat.description = "catalogue of input files for assessor " + XNATAccessionID;
      
      Resource         r   = new Resource();
      List<MetaField>  mfl = new ArrayList<>();
		r.tagList            = mfl;
      r.uri                = XNATAccessionID+"_input.xml";
      r.format             = "XML";
      r.content            = "INPUT_CATALOGUE";
      r.fileCount          = lce.size();
      r.description        = "Input data for assessor " + XNATAccessionID;
      Provenance       p   = new Provenance();
      p.stepList           = new ArrayList<>();
      r.prov               = p;
      
		List<Resource> inList  = new ArrayList<>();
		inList.add(r);
      List<Resource> outList = new ArrayList<>();
				
		iad.setInList(inList);
      iad.setOutList(outList);
      // There is no outList for icr:imageAnnotationData
		
		iad.setImageSessionId(XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		iad.setParamList(new ArrayList<AdditionalField>());
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		iad.setProvenance(prov);
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      iad.setId(XNATAccessionID);
      iad.setProject(XNATProject);
      
     
		// Apparently the version XML element has to be an integer, so it is not
		// really clear what this field signifies.
		iad.setVersion("1");
		
		iad.setLabel(label);
      
		iad.setDate(date);
      iad.setTime(time);
      iad.setNote(note);
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator.
		iad.setInvestigator(new InvestigatorList.Investigator());      

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
   
   
   void setDicomSubjNameParent(String s)
   {
      dicomSubjNameParent = s;
   }
   
   
   void setLabelParent(String s)
   {
      labelParent = s;
   }
   
   
   void setMapsParent(Map<String, String>      fsop,
                      Map<String, String>      sf,
                      Map<String, String>      fscan,
                      Map<String, DicomObject> sdo,
                      Map<String, String>      mr)
   {
      filenameSopMap  = fsop;
      filenameScanMap = fscan;
      sopFilenameMap  = sf;
      sopDoMap        = sdo;
      markupRegionMap = mr;
   }
   
   
   void setProvenanceParent(Provenance p)
   {
      prov = p;
   }
}
