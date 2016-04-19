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
* Object for uploading ROI metadata and generated thumbnails to XNAT
* extracted from source files that conform to DICOM RT-STRUCT format
* Note that there are no primary data files associated with these
* ROIs, as the primary file will have been uploaded as part of an
* ROIset. Because of the use to which this will be put, many of the
* abstract methods in DataUploader do not need implementing.
*********************************************************************/

package xnatUploader;

import dataRepresentations.dicom.Code;
import dataRepresentations.dicom.ContourImage;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import dataRepresentations.dicom.RoiContour;
import dataRepresentations.dicom.RtReferencedSeries;
import dataRepresentations.dicom.RtReferencedStudy;
import dataRepresentations.dicom.RtRoiObservation;
import dataRepresentations.dicom.RtStruct;
import dataRepresentations.dicom.StructureSet;
import dataRepresentations.dicom.StructureSetRoi;
import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.InvestigatorList;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Scan;
import exceptions.XMLException;
import generalUtilities.UIDGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrRegionDataMdComplexType;

public class RegionFromRtStructDataUploader extends DataUploader
{
	// Capture all the data from the class supervising the uploading.
	private RtStructDataUploader rtdsu;
   private int                  roiPos;
	
	public RegionFromRtStructDataUploader(XNATProfile xnprf, RtStructDataUploader rtsdu)
	{
		super(xnprf);
		this.rtdsu = rtdsu;
	}
	
@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrRegionDataMdComplexType region = new IcrRegionDataMdComplexType();
		
		List<String> arsl = new ArrayList<>();
		arsl.add(rtdsu.XNATAccessionID);
		region.setAssociatedRegionSetIdList(arsl);
		
		region.setOriginalUid(rtdsu.rts.sopCommon.sopInstanceUid);
		region.setOriginalDataType("RT-STRUCT");
		
      Set<Integer>    singleRoi = new HashSet<>();
      singleRoi.add(rtdsu.rts.structureSet.structureSetRoiList.get(roiPos).roiNumber);
      RtStruct        rtsSingle = new RtStruct(rtdsu.rts, singleRoi);
		
		StructureSet    ss  = rtsSingle.structureSet;
      assert (ss.structureSetRoiList.size() == 1);
      StructureSetRoi ssr = ss.structureSetRoiList.get(0);
      
      region.setOriginalLabel(ss.structureSetLabel);
      region.setOriginalDescription(ss.structureSetDescription);
		region.setOriginatingApplicationName(rtdsu.rts.generalEquipment.modelName);
		
		final String sep = " | ";
		StringBuilder sb = new StringBuilder();
		for (String s : rtdsu.rts.generalEquipment.softwareVersions) sb.append(s).append(sep);
		region.setOriginatingApplicationVersion(sb.toString());
		
      region.setOriginalContainsMultipleRois((rtdsu.nRois > 1) ? "true" : "false");
      region.setRoiNumberInOriginal(Integer.toString(ssr.roiNumber));
		
		region.setRoiName(ssr.roiName);
		region.setRoiDescription(ssr.roiDescription);
		
		// In order to get to the ROI colour and contour type, we need to iterate
      // through the contents of roiContour. The model implemented here does not
      // completely match the DICOM information model in this respect, because,
      // theoretically, there seems nothing to stop there being more than one
      // different RoiContour (with different colour) for any given ROI.
      // Similarly, there could be many different Contours composing each
      // RoiContour, each of which might have a different geometric type.
      for (RoiContour rc : rtdsu.rts.roiContourList)
		{
			if (rc.referencedRoiNumber == ssr.roiNumber)
			{
				int[] col = rc.roiDisplayColour;
				String s = "[" + col[0] + ", " + col[1] + ", " + col[2] + "]";
				region.setLineColour(s);
            region.setRoiGeometricType(rc.contourList.get(0).contourGeometricType);
            region.setNDicomContours(rc.contourList.size());
            break;
			}
		}
      
      region.setRoiVolume(Float.toString(ssr.roiVolume));
      region.setRoiGenerationAlgorithm(ssr.roiGenerationAlgorithm);
      region.setRoiGenerationDescription(ssr.roiGenerationDescription);
      
      StringBuilder dcsb = new StringBuilder();
      for (Code dc : ssr.derivationCodeList)
      {
         dcsb.append(dc.getAsSingleString()).append(sep);
      }
      region.setDerivationCode(dcsb.toString());
      
      List<RtRoiObservation> rrol = new ArrayList<>();
      for (RtRoiObservation rro : rtdsu.rts.rtRoiObservationList)
      {
         if (rro.referencedRoiNumber == ssr.roiNumber) rrol.add(rro);
      }
      region.setRtRoiObservationList(rrol);
      
      // IcrRegionDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// regionSet.setType();  Not currently sure what should go here.
		region.setXnatSubjId(rtdsu.XNATSubjectID);
		region.setDicomSubjName(rtsSingle.patient.patientName);
		
		// Although the full version of Scan, including scan and slice image
		// statistics is implemented, this is overkill for the RT-STRUCT and
		// the only part of scan for which information is available is the
		// list of scan IDs. Generate this list for the individual region. Note
      // that what is passed into the object is the complete set for all the ROIs
      // in the structure set.
      // Generate a single Set of all studies referenced for later use
		// and similarly for all series and SOPInstances referenced.
      Set<String> studyUidSet       = new LinkedHashSet<>();
      Set<String> seriesUidSet      = new LinkedHashSet<>();
      Set<String> sopInstanceUidSet = new LinkedHashSet<>();
		for (ReferencedFrameOfReference rfor : rtsSingle.structureSet.referencedFrameOfReferenceList)
		{
			for (RtReferencedStudy rrs : rfor.rtReferencedStudyList)
			{
				studyUidSet.add(rrs.referencedSopInstanceUid);
				for (RtReferencedSeries rrse : rrs.rtReferencedSeriesList)
				{
					seriesUidSet.add(rrse.seriesInstanceUid);
					for (ContourImage ci : rrse.contourImageList)
					{
						sopInstanceUidSet.add(ci.referencedSopInstanceUid);
					}
				}
			}
		}
		List<Scan> lsc = new ArrayList<>();
		for (String id : rtdsu.seriesUidSet)
		{
			Scan sc = new Scan();
			sc.id = id;
			lsc.add(sc);
		}
		region.setScanList(lsc);
		
		// IcrGenericImageAssessmentDataMdComplexType inherits from XnatImageAssessorDataMdComplexType.
		
		// The "in" section of the assessor XML contains all files that were already
		// in the database at the time of upload, whilst the "out" section lists
		// the files that added at the time of upload, including those generated
		// automatically. In this, the only generated files are the snapshots, but
		// this information is already included in the separately uploaded ROI
		// metadata files and need not be duplicated here.
		List<AbstractResource> inList = new ArrayList<>();
		
      // Because Java maps are not one-to-one mappings, i.e., many keys may
      // have the same attached value, one cannot work backwards from a given
      // value to get a unique key. This means that while the map.keyset() call
      // exists, a similar map.valueSet() is (presumable) not seen to be useful
      // and hence not provided. However, in our case, we *do* have a one-to-one
      // mapping between files and SOPInstances.
		List<String> fileList = new ArrayList<>();
      for (String filename : rtdsu.fileSopMap.keySet())
      {
         if (sopInstanceUidSet.contains(rtdsu.fileSopMap.get(filename)))
            fileList.add(filename);
      }
      
      for (String filename : fileList)
		{
			AbstractResource ar  = new AbstractResource();
			List<MetaField>  mfl = new ArrayList<>();
			mfl.add(new MetaField("filename",       filename));
			mfl.add(new MetaField("format",         "DICOM"));
			mfl.add(new MetaField("SOPInstanceUID", rtdsu.fileSopMap.get(filename)));
			ar.tagList = mfl;
			inList.add(ar);
		}
		
		List<AbstractResource> outList = new ArrayList<>();
		AbstractResource       ar      = new AbstractResource();
		List<MetaField>        mfl     = new ArrayList<>();
		mfl.add(new MetaField("filename", rtdsu.uploadFile.getName()));
		mfl.add(new MetaField("format",   "RT-STRUCT"));
		ar.tagList = mfl;
		outList.add(ar);
		
		region.setInList(inList);
		region.setOutList(outList);
		
		region.setImageSessionId(rtdsu.XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		region.setParamList(new ArrayList<AdditionalField>());
		
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		
 
		Provenance prov = (rtdsu.retrieveProvenance());
      prov.stepList.get(1).program.name = "Auto-extracted from RT-STRUCT file by ICR XNAT DataUploader";
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      region.setId(XNATAccessionID);
      region.setProject(rtdsu.XNATProject);
      
      //StringBuilder versions = new StringBuilder();
		//for (String s : rts.generalEquipment.softwareVersions) versions.append(s);
      //roiSet.setVersion(versions.toString());
      
		// Apparently the version XML element has to be an integer, so my ideal
		// version above is no use.
		region.setVersion("1");
		
      label = rtdsu.label + " " + getRootElement() + "_" + ssr.roiNumber;
		region.setLabel(label);
      
		region.setDate(rtdsu.date);
      region.setTime(rtdsu.time);
      region.setNote(rtdsu.note);
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator. However, we need to set an
      // empty Investigator object, rather than null.
		region.setInvestigator(new InvestigatorList.Investigator());      
      
      // Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = region.createXmlAsRootElement();
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
	
	public void setRoiPositionInSSRoiSequence(int n)
   {
      roiPos = n; 
   }
	
	@Override
   public String getRootElement()
   {
      return "Region";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:regionData";
   }

   // All abstract methods need to be implemented.
   
   @Override
   public boolean parseFile()
   {
      // Method never called.
      return true;
   }

   @Override
   public void updateParseFile()
   {
      // Method never called.
   }

   @Override
   protected ArrayList<String> getInputCatEntries()
   {
      return new ArrayList<String>();
   }
   

   @Override
   public void clearFields(MetadataPanel mdsp)
   {
      // This routine is never called as Region entities are not loaded as individual files
      // but created dynamically from RegionSets.
   }
   
   
   @Override
   protected void createPrimaryResource()
   {
      // There is no primary resource associated with a Region entity.
   }

   @Override
   protected void createAuxiliaryResources()
   {  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void updateVariablesForEditableFields(MetadataPanel mdsp)
   {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public List<String> getEditableFields()
   {
      return new ArrayList<String>();
   }

   @Override
   public List<String> getRequiredFields()
   {
      return new ArrayList<String>();
   }

   @Override
   public boolean rightMetadataPresent()
   {
      return true;
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
