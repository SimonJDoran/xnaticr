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
* First created on May 24, 2016 at 23:54:00 AM
*********************************************************************/

package xnatUploader;

import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.AimEntitySubclass;
import dataRepresentations.xnatSchema.Catalog;
import dataRepresentations.xnatSchema.CatalogEntry;
import dataRepresentations.xnatSchema.InvestigatorList;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Resource;
import dataRepresentations.xnatSchema.Scan;
import etherj.aim.Equipment;
import etherj.aim.ImageAnnotation;
import etherj.aim.Markup;
import etherj.aim.Person;
import etherj.aim.User;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimEntitySubclassDataMdComplexType;
import xnatMetadataCreators.IcrAimImageAnnotationDataMdComplexType;

public class AimEntitySubclassDataUploader extends DataUploader
{
	private AimEntitySubclass es;
   private String            dicomSubjNameParent;
	
	public AimEntitySubclassDataUploader(XNATProfile xnprf)
	{
		super(xnprf);  
	}
   
   
   
	@Override
	protected Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method.
		IcrAimEntitySubclassDataMdComplexType
				  esd = new IcrAimEntitySubclassDataMdComplexType();
		
		esd.setEntitySubclass(es);
      
		// IcrAimImageAnnotationDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// iacd.setType();  Not currently sure what should go here.
		esd.setXnatSubjId(XNATSubjectID);
	   esd.setDicomSubjName(dicomSubjNameParent);
		
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
		esd.setScanList(lsc);
      
      
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
				
		esd.setInList(inList);
      esd.setOutList(outList);
      // There is no outList for icr:imageAnnotationData
		
		esd.setImageSessionId(XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		esd.setParamList(new ArrayList<AdditionalField>());
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		esd.setProvenance(prov);
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      esd.setId(XNATAccessionID);
      esd.setProject(XNATProject);
      
     
		// Apparently the version XML element has to be an integer, so it is not
		// really clear what this field signifies.
		esd.setVersion("1");
		
		esd.setLabel(label);
      
		esd.setDate(date);
      esd.setTime(time);
      esd.setNote(note);
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator.
		esd.setInvestigator(new InvestigatorList.Investigator());      
// Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = esd.createXmlAsRootElement();
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
      return "AimEntitySubclass";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:aimEntitySubclassData";
   }
   

   @Override
   protected void createPrimaryResource()
   {
      // There is no primary resource associated with an AIM EntitySubclassData
      // entity. The AimImageAnnotationCollectionDataUploader does the upload
      // of the AIM instance XML file. 
   }
	

   @Override
   protected void createAuxiliaryResources()
   {
      // There are no auxiliary resources associated with an AIM EntitySubclass.
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
	
	
	void setEntitySubclass(AimEntitySubclass es)
	{
		this.es = es;
	}
   
   
   void setDicomSubjNameParent(String s)
   {
      dicomSubjNameParent = s;
   }
   
   

}

