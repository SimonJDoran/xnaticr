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
import dataRepresentations.dicom.Contour;
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
import exceptions.DataFormatException;
import exceptions.ImageUtilitiesException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.UIDGenerator;
import generalUtilities.Vector2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrRegionDataMdComplexType;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XnatResource;
import xnatUploader.ContourRendererHelper.RenderContour;

public class RegionFromRtStructDataUploader extends DataUploader implements ContourRenderer
{
	public  RtStruct             rtsSingle;
	
	// Capture all the data from the class supervising the uploading.
	private RtStructDataUploader rtdsu;
   private int                  roiPos;
	public Set<String>           sopInstanceUidSet;
	public Set<String>           filenameSet;
	
	public RegionFromRtStructDataUploader(XNATProfile xnprf, RtStructDataUploader rtdsu)
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
		
      Set<Integer> singleRoi = new HashSet<>();
      singleRoi.add(rtdsu.rts.structureSet.structureSetRoiList.get(roiPos).roiNumber);
      rtsSingle = new RtStruct(rtdsu.rts, singleRoi);
		
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
				List<Integer> col = rc.roiDisplayColour;
				String s = "[" + col.get(0) + ", " + col.get(1) + ", " + col.get(2) + "]";
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
      
      // The RT-STRUCT does noit provide any statistics, but we still need to
      // set the variable to an empty list.
      region.setAssociatedRegionParStatsIdList(new ArrayList<String>());
      
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
		sopInstanceUidSet = new LinkedHashSet<>();
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
		
		filenameSet   = new LinkedHashSet<>();
		XNATScanIdSet = new LinkedHashSet<>();
		for (String sopUid : sopInstanceUidSet )
		{
			// Does the ROI-specific set of SOP Instances contain the SOP Instance
			// corresponding to the file name. If yes, add the scan ID corresponding
			// to this filename. Of course, many files will have the same scan number
			// (unless we are dealing with multi-frame DICOM), hence the use of a Set.
			String filename = rtdsu.sopFileMap.get(sopUid);
			filenameSet.add(filename);
			XNATScanIdSet.add(rtdsu.fileScanMap.get(filename));
		}
		
		List<Scan> lsc = new ArrayList<>();
		for (String scId : XNATScanIdSet)
		{
				Scan sc = new Scan();
				sc.id = scId;
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
      
      for (String filename : filenameSet)
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
      region.setProvenance(prov);
		
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
      // This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
      return true;
   }

   @Override
   public void updateParseFile()
   {
      // This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
   }

   @Override
   protected ArrayList<String> getInputCatEntries()
   {
      return new ArrayList<String>();
   }
   

   @Override
   public void clearFields(MetadataPanel mdsp)
   {
      // This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
   }
   
   
   @Override
   protected void createPrimaryResource()
   {
      // There is no primary resource associated with a Region entity.
   }

   @Override
   protected void createAuxiliaryResources()
   {
      //createInputCatalogueFile("DICOM", "RAW", "referenced contour image");
		
      ContourRendererHelper crh;
      Map<String, File>     fileMap;
      try
      {
         crh = createContourRendererHelper();
         crh.retrieveBaseImagesToCache();
      }
      catch (DataFormatException | XNATException ex)
      {
          reportError(ex, "create thumbnail images");
          return;
      }   
      
		try
      {
         ArrayList<BufferedImage> thumbnails = crh.createImages();
         String filePrefix = XNATAccessionID + "_ROI_thumbnail_";
         
         for (int i=0; i<thumbnails.size(); i++)
         {
            StringBuilder description = new StringBuilder();
            description.append("ROI thumbnail rendered by ICR DataUploader ")
                       .append(version)
                       .append("extracted from original RT-STRUCT file ")
                       .append(rtdsu.uploadFile.getName());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnails.get(i), "png", baos);
            InputStream bais = new ByteArrayInputStream(baos.toByteArray());
            
            XnatResource xr = new XnatResource(bais,
		                                         "out",
		                                         filePrefix + i,
				                                   "PNG",
		                                         "ROI_THUMBNAIL",
		                                         description.toString(),
				                                   filePrefix + i + ".png");
				
            auxiliaryResources.add(xr);
         }
      }
      catch (IOException | ImageUtilitiesException ex)
      {
         reportError(ex, "create RT thumbnail file");
      }      
   }
	
	@Override
	public ContourRendererHelper createContourRendererHelper()
                                throws DataFormatException
	{
		ContourRendererHelper crh = new ContourRendererHelper();
		
		// An RtStruct object corresponding to a single ROI has only one element
		// in its roiContourList.
		assert (rtsSingle.roiContourList.size() == 1);
		RoiContour rc     = rtsSingle.roiContourList.get(0);
		crh.displayColour = rc.roiDisplayColour;
		
		// The frame of reference in which the ROI is defined is in a separate DICOM
		// IOD from the contour list!
		for (StructureSetRoi ssr : rtsSingle.structureSet.structureSetRoiList)
		{
			if (ssr.roiNumber == rc.referencedRoiNumber) crh.frameOfReferenceUid = ssr.referencedFrameOfReferenceUid;
		}
		
		crh.coordsAsPixel = false;
      crh.rndCList      = new ArrayList<>();
      
		for (Contour c : rc.contourList)
      {
         if (c.contourImageList.size() != 1)
         {
            String msg = "This type of contour cannot yet be rendered."
                         + "More than one base image for a single contour.";
            logger.error(msg);
            throw new DataFormatException(DataFormatException.RTSTRUCT, msg);
         }
         
         RenderContour rndC       = new RenderContour();
         String baseSop           = c.contourImageList.get(0).referencedSopInstanceUid;
         rndC.baseImageFilename   = rtdsu.sopFileMap.get(baseSop);
			rndC.baseFrameNumberList = c.contourImageList.get(0).referencedFrameNumber;
         rndC.nContourPoints      = c.nContourPoints;
         rndC.contourPoints       = new float[c.nContourPoints][3];

         for (int j=0; j<c.nContourPoints; j++)
            for (int i=0; i<3; i++)
               rndC.contourPoints[j][i] = (c.contourData.get(j)).get(i);
         
         crh.rndCList.add(rndC);
			
			crh.caller = this;
      }
			
		return crh;
	}
	
	
	@Override
	public Set<String> getFilenameSet()
	{
		return filenameSet;
	}
	
	
	@Override
	public Set<String> getXnatScanIdSet()
	{
		return XNATScanIdSet;
	}
	
	
	@Override
	public String getXnatExperimentId()
	{
		return XNATExperimentID;
	}
	
	
	@Override
	public XNATProfile getXnatProfile()
	{
		return xnprf;
	}
	
	
 

   @Override
   public void updateVariablesForEditableFields(MetadataPanel mdsp)
   {
      // This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
   }

   @Override
   public List<String> getEditableFields()
   {
		// This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
      return new ArrayList<String>();
   }

   @Override
   public List<String> getRequiredFields()
   {
		// This routine is never called as Region entities are not loaded as individual
      // files via the UI but created dynamically from RegionSets.
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
