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
* Java class: RtStructBuilder.java
* First created on May 4, 2016 at 12:10:14 PM
* 
* Builds RtStruct objects from a variety of different sources. This
* class removes the heavy lifting from the class RtStruct.
*********************************************************************/

package dataRepresentations.dicom;

import dataRepresentations.dicom.RtStruct;
import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import etherj.aim.Markup;
import static etherj.aim.Markup.TwoDimensionPolyline;
import etherj.aim.TwoDimensionCircle;
import etherj.aim.TwoDimensionCoordinate;
import etherj.aim.TwoDimensionEllipse;
import etherj.aim.TwoDimensionGeometricShape;
import etherj.aim.TwoDimensionMultiPoint;
import etherj.aim.TwoDimensionPoint;
import etherj.aim.TwoDimensionPolyline;
import exceptions.DataFormatException;
import generalUtilities.DicomXnatDateTime;
import generalUtilities.UidGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import xnatUploader.ContourRendererHelper;

public class RtStructBuilder
{	
	/**
    * Create a new RtStruct object and populate it with data from a DicomObject -
    * typically representing a file being uploaded.
    * @param rtsDo Source DICOM object
    * @throws DataFormatException 
    */
	public RtStruct buildNewInstance(DicomObject rtsDo)
				       throws DataFormatException
	{
		// Before we start, check that this really is a structure set!
		if (!rtsDo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}
		
		RtStruct rts = new RtStruct();

		rts.sopCommon        = new SopCommon(rtsDo);
		rts.patient          = new Patient(rtsDo);
		rts.generalStudy     = new GeneralStudy(rtsDo);
		rts.rtSeries         = new RtSeries(rtsDo);
		rts.generalEquipment = new GeneralEquipment(rtsDo);
		rts.structureSet     = new StructureSet(rtsDo);
		
		rts.roiContourList   = rts.readSequence(RoiContour.class, rtsDo,
				                                         Tag.ROIContourSequence, 1);
		rts.rtRoiObservationList = rts.readSequence(RtRoiObservation.class, rtsDo,
				                                  Tag.RTROIObservationsSequence, 1); 
		
		return rts;
   }
	
	
	/**
    * Create a new RtStruct object and populate it with data from an existing
    * RtStruct object, but including only a subset of the ROIs defined in the
	 * original RT-STRUCT file.
    * @param src Source RtStruct object
    * @param rois Set of region numbers to put into the output RT-STRUCT.
    * @return a new RtStruct generated from the input information
	 * @throws DataFormatException 
	 */
   public RtStruct buildNewInstance(RtStruct src, Set<Integer> rois)
			          throws DataFormatException
   {
      RtStruct dest = src.deepCopy();
      
      // Make the modifications that come about because of selecting a single ROI.
		dest.sopCommon.sopInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                          UidGenerator.RT_STRUCT,
                                                                UidGenerator.SOPInstanceUID);

		dest.structureSet.structureSetLabel        = src.structureSet.structureSetLabel + " - ROI subset";
		dest.structureSet.structureSetName         = src.structureSet.structureSetName + "_ROIsubset";
		dest.structureSet.structureSetDescription  = src.structureSet.structureSetDescription + "(ROI subset generated by ICR_XNATDataUploader)";
		dest.structureSet.referencedFrameOfReferenceList
		                                           = buildNewRforListFromSubset(src, rois);
		dest.structureSet.structureSetRoiList      = buildNewSsrListFromSubset(src, rois);
		dest.roiContourList                        = buildNewRcListFromSubset(src, rois);
		dest.rtRoiObservationList                  = buildNewRroListFromSubset(src, rois);
		
		return dest;
   }
	
	private List<ReferencedFrameOfReference>
	                   buildNewRforListFromSubset(RtStruct src, Set<Integer> rois)
		{
			// In order to put the correct entries in the referencedFrameOfReference
			// and rtReferencedStudy variables, we need to look up only the data
			// used by the subset of ROIs. This is a bit of work.

			// First get a list of all the ContourImages contained in the subset.
			// This will be used directly.
			Set<ContourImage> srcCiSet = new HashSet<>(); 
			for (RoiContour rc : src.roiContourList)
			{
				if (rois.contains(rc.referencedRoiNumber))
				{
					for (Contour c : rc.contourList)
					{
						for (ContourImage ci : c.contourImageList) srcCiSet.add(ci);
					}					
				}
			}

			// Now work backwards from the SOPInstanceUIDs of the individual
			// ContourImages to get the corresponding series and study UIDs.
			Map<ReferencedFrameOfReference, Map<RtReferencedStudy, Map<RtReferencedSeries, Set<ContourImage>>>> m1 = new HashMap<>();
			for (ReferencedFrameOfReference rfor : src.structureSet.referencedFrameOfReferenceList)
			{
				for (RtReferencedStudy rrs : rfor.rtReferencedStudyList)
				{
					for (RtReferencedSeries rrse : rrs.rtReferencedSeriesList)
					{
						for (ContourImage ci : rrse.contourImageList)
						{
                     // Naively, the following line is what we want to do. However,
                     // the problem is that there are two ci objects representing
                     // the same contour image and, unlike primitives, they will
                     // not evaluate to the same thing and the test would fail.
							// if (srcCiSet.contains(ci))
                     
                     // Instead, compare the image UIDs.
                     for (ContourImage ciSrc : srcCiSet)
							{
                        if (ciSrc.referencedSopInstanceUid.equals(ci.referencedSopInstanceUid))
                        {
                           Map<RtReferencedStudy, Map<RtReferencedSeries, Set<ContourImage>>> m2;
                           Map<RtReferencedSeries, Set<ContourImage>> m3;
                           Set<ContourImage> s4;

                           if (m1.containsKey(rfor)) m2 = m1.get(rfor);
                           else
                           {
                              m2 = new HashMap<>();
                              m1.put(rfor, m2);
                           }

                           if (m2.containsKey(rrs)) m3 = m2.get(rrs);
                           else
                           {
                              m3 = new HashMap<>();
                              m2.put(rrs, m3);
                           }

                           if (m3.containsKey(rrse)) s4 = m3.get(rrse);
                           else
                           {
                              s4 = new HashSet<>();
                              m3.put(rrse, s4);
                           }

                           s4.add(ci);
                        }
							}
						}
					}
				}
			}
		
		// The contents of the map m1 are now enough to construct all the
		// required objects from the original structure set contents.
		List<ReferencedFrameOfReference> destRforList = new ArrayList<>();	
		for (ReferencedFrameOfReference rfor : m1.keySet())
		{
			// Build up the destination ReferencedFrameOfReferenceObjects,
			// which may have only a selected range of studies and series.
			ReferencedFrameOfReference destRfor = new ReferencedFrameOfReference();
			destRforList.add(destRfor);
			destRfor.frameOfReferenceUid   = rfor.frameOfReferenceUid;
			destRfor.rtReferencedStudyList = new ArrayList<RtReferencedStudy>();
			
			Map<RtReferencedStudy, Map<RtReferencedSeries, Set<ContourImage>>> m2 = m1.get(rfor);				
			for (RtReferencedStudy rrs : m2.keySet())
			{
				RtReferencedStudy destRrs        = new RtReferencedStudy();
				destRfor.rtReferencedStudyList.add(destRrs);
				destRrs.referencedSopClassUid    = rrs.referencedSopClassUid;
				destRrs.referencedSopInstanceUid = rrs.referencedSopInstanceUid;
				destRrs.rtReferencedSeriesList   = new ArrayList<RtReferencedSeries>();
				
				Map<RtReferencedSeries, Set<ContourImage>> m3 = m2.get(rrs);
				for (RtReferencedSeries rrse : m3.keySet())
				{
					RtReferencedSeries destRrse = new RtReferencedSeries();
					destRrs.rtReferencedSeriesList.add(destRrse);
					destRrse.seriesInstanceUid  = rrse.seriesInstanceUid;
					destRrse.contourImageList   = new ArrayList<>(m3.get(rrse));	
				}
			}			
		}		
		return destRforList;
	}

							 
	private List<StructureSetRoi>
	                   buildNewSsrListFromSubset(RtStruct src, Set<Integer> rois)
	{
		List<StructureSetRoi> destSsrList = new ArrayList<>();
		for (StructureSetRoi ssr : src.structureSet.structureSetRoiList)
		{
			if (rois.contains(ssr.roiNumber)) destSsrList.add(ssr);
		}
		
		return destSsrList;	
	}
   
							 
   private List<RoiContour>
	                   buildNewRcListFromSubset(RtStruct src, Set<Integer> rois)
	{
		List<RoiContour> destRcList = new ArrayList<>();
		for (RoiContour rc : src.roiContourList)
		{
			if (rois.contains(rc.referencedRoiNumber)) destRcList.add(rc);
		}
		
		return destRcList;
	}
							 
	
	private List<RtRoiObservation>
	                   buildNewRroListFromSubset(RtStruct src, Set<Integer> rois)
	{
		List<RtRoiObservation> destRroList = new ArrayList<>();
		for (RtRoiObservation rro : src.rtRoiObservationList)
		{
			if (rois.contains(rro.referencedRoiNumber)) destRroList.add(rro);
		}
		
		return destRroList;
	}
	
							 
	
   /**
	 * Create a new RtStruct object and populate it with data from an
    * Annotation and Image Markup (AIM) instance file.
	 * (https://wiki.nci.nih.gov/display/AIM)
    * Note that most of the data needed for the RT-STRUCT is not contained
    * within the AIM source file and we have to seek it out from the original
    * image DICOM files, which are provided by the sopDoMap parameter.
	 * @param iac an AIM ImageAnnotationCollection parsed from the source XML
    * by James d'Arcy's Etherj package.
	 * @param sopDoMap a Map that links a DICOM sopInstanceUid to the actual DicomObject
    * corresponding to the file, thus allowing us to extract the various header
    * parameters to supplement the information in the image annotation.
	 * @return a new RtStruct generated from the input information
	 * @throws DataFormatException 
	 */
   public RtStruct buildNewInstance(ImageAnnotationCollection iac,
                                    Map<String, DicomObject>  sopDoMap)
			          throws DataFormatException
   {
		RtStruct rts = new RtStruct();
		rts.sopCommon        = iacBuildSopCommon(iac);
		rts.patient          = iacBuildPatient(iac, sopDoMap);
		rts.generalStudy     = iacBuildGeneralStudy(rts.sopCommon);
		rts.generalEquipment = iacBuildGeneralEquipment(iac);
		rts.rtSeries         = iacBuildRtSeries(iac, rts.sopCommon);
		
		// We need to build the iacBuildRoiContourList and
		// iacBuildStructureSet methods, because the information needed to build
		// the StructureSetRois is actually parsed at the time of extracting
		// the contour lists. Hence, the name and description parameters.
		iacBuildRoiContourListAndStructureSet(iac, sopDoMap, rts.sopCommon);
		
		return rts;
   }

   
   private SopCommon iacBuildSopCommon(ImageAnnotationCollection iac)
			  throws DataFormatException
	{
		SopCommon sc = new SopCommon();
      String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      sc.instanceCreationDate = DicomXnatDateTime.convertXnatDateTimeToDicomDate(dateTime);
      sc.instanceCreationTime = DicomXnatDateTime.convertXnatDateTimeToDicomTime(dateTime);
      sc.sopClassUid          = UID.RTStructureSetStorage;
      sc.sopInstanceUid       = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                         UidGenerator.RT_STRUCT,
                                                               UidGenerator.SOPInstanceUID);
		return sc;
	}
	
	
	private Patient iacBuildPatient(ImageAnnotationCollection iac, Map<String, DicomObject> sopDoMap)
			  throws DataFormatException
	{
		// Arbitrarily, pick the last series in the following loop to provide
		// the template DICOM file from which to pick various patient parameters.
		DicomObject templateDo = null;
		
		// Check that the same subject is referenced in all the images. This is
		// potentially tricky, given that different variants of the name might
		// have been entered differently by the scanner operators and that the
		// images might have been subject to anonymisation.
		String      name1      = null;
		for (String sop : sopDoMap.keySet())
		{
			DicomObject bdo   = sopDoMap.get(sop);
			
			// Note that readString is not a static method, so we have to create an
			// instance of a DicomEntity to use it. However, there is no need for
			// any further use of the object after the operation completes.
			String      name2 = (new Patient()).readString(bdo, Tag.PatientName, 1);
			if ((!name2.equals(name1)) && (name1 != null))
			{
				String msg = "Trying to construct an RT-STRUCT from two sets of "
						       + "images with different patient names. Ensure that "
						       + "source images are uploaded to XNAT with compatible"
						       + "values in the Patient Name field of the DICOM headers.";
				
				throw new DataFormatException(DataFormatException.RTSTRUCT, msg);
			}
			name1      = name2;
			templateDo = bdo;
		}
		
		return new Patient(templateDo);
	}
	
	
	private GeneralStudy iacBuildGeneralStudy(SopCommon sc)
	{
		GeneralStudy gs = new GeneralStudy();
		gs.studyInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                         UidGenerator.RT_STRUCT,
																							UidGenerator.StudyInstanceUID);
		gs.studyDate = sc.instanceCreationDate;
		gs.studyTime = sc.instanceCreationTime;
		
		return gs;
	}
	
	
	private GeneralEquipment iacBuildGeneralEquipment(ImageAnnotationCollection iac)
	{
		final String DEFAULT = "Unknown";
		
		GeneralEquipment ge = new GeneralEquipment();
		
      ge.institutionAddress = DEFAULT;
      ge.institutionName    = DEFAULT;
      ge.manufacturer       = "Institute of Cancer Research";
      ge.modelName          = "ICR XNAT DataUploader";
      List<String> sv = new ArrayList<>();
      sv.add(iac.getAimVersion());
      sv.add("Converted to RT-STRUCT by ICR XNAT DataUploader");
      ge.softwareVersions   = sv;
      try
		{
			ge.stationName = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException exUH)
		{
			ge.stationName = DEFAULT;
		}
		
		return ge;
	}
	
	
	private RtSeries iacBuildRtSeries(ImageAnnotationCollection iac, SopCommon sc)
	{
		RtSeries rtSeries = new RtSeries();
		rtSeries.modality          = "RTSTRUCT";
		rtSeries.seriesInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                      UidGenerator.RT_STRUCT,
																						UidGenerator.SeriesInstanceUID);
		rtSeries.seriesNumber      = 1;
		rtSeries.seriesDate        = sc.instanceCreationDate;
		rtSeries.seriesTime        = sc.instanceCreationTime;
		rtSeries.seriesDescription = "Image markup from AIM instance with UID " + iac.getUid()
				                       + " converted RT-STRUCT by XNAT DataUploader";
		rtSeries.operatorName      = System.getProperty("user.name");
		
		return rtSeries;
	}
	
	
	private StructureSet iacBuildRoiContourListAndStructureSet
		                      (ImageAnnotationCollection iac,
			                    Map<String, DicomObject>  sopDoMap,
									  SopCommon                 sc)
			  throws DataFormatException
	{
		final float[] DUMMY_F2 = new float[] {0f, 0f};
      final float[] DUMMY_F3 = new float[] {0f, 0f, 0f};
      
		List<RoiContour>      rcl  = new ArrayList<>();
		StructureSet ss = new StructureSet();
		
      ss.structureSetLabel           = "Auto-created structure set";
      ss.structureSetName            = iac.getUid() + "_RT-STRUCT";
      ss.structureSetDescription     = "Image markup from AIM instance document with UID "
                                        + iac.getUid() + " converted RT-STRUCT by ICR XNAT DataUploader";
      ss.instanceNumber              = "1";
		ss.structureSetDate            = sc.instanceCreationDate;
		ss.structureSetTime            = sc.instanceCreationTime;
      ss.referencedFrameOfReferenceList
                                     = iacBuildRforList(iac, sopDoMap);
		ss.predecessorStructureSetList = new ArrayList<>();
		
		
		// Build the list of structure set ROIs from the AIM data.
		List<StructureSetRoi> ssrl = new ArrayList<>();
		ss.structureSetRoiList = ssrl;
		int roiCount = 0;
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (Markup mku : ia.getMarkupList())
         {
            if (mku instanceof TwoDimensionGeometricShape)
            {
               TwoDimensionGeometricShape shape;
               shape = (TwoDimensionGeometricShape) mku;
               
               RoiContour rc = new RoiContour(); 
               rc.referencedRoiNumber = roiCount;
               //rc.roiDisplayColour = shape.getLineColour(); Not yet implemented in EtherJ
               List<TwoDimensionCoordinate> d2l = shape.getCoordinateList();
               
               // When we have a 2-D shape, there is only one Contour object in the
               // RoiContour and there is also only one ContourImage in the list.
               DicomObject        bdo = null;
               List<Contour>      cl  = new ArrayList<>();
               Contour            c   = new Contour();
               List<ContourImage> cil = new ArrayList<>();
               ContourImage       ci  = new ContourImage();
               ArrayList<Integer> fnl = new ArrayList<>();
               List<List<Float>>  cd  = new ArrayList<List<Float>>();
               float[]            position    = DUMMY_F3;
               float[]            orientation = DUMMY_F3;
               float[]            pixelSize   = DUMMY_F2; 
               
               ci.referencedSopInstanceUid = shape.getImageReferenceUid();
               fnl.add(shape.getReferencedFrameNumber());
               ci.referencedFrameNumberList = fnl;
               
               // Retrieving the SOP Class UID is a bit of a pain, because AIM
               // doesn't store it. To get to it, we need to find the corresponding
               // DICOM object and to do this, we have to find the series containing
               // this image.
               for (ImageReference ir : ia.getReferenceList())
               {
                  if (ir instanceof DicomImageReference)
                  {
                     DicomImageReference dir    = (DicomImageReference) ir;
                     ImageStudy          study  = dir.getStudy();
                     ImageSeries         series = study.getSeries();
                     for (Image im : series.getImageList())
                        if (im.getInstanceUid().equals(shape.getImageReferenceUid()))
                        {
                           bdo = sopDoMap.get(im.getInstanceUid());
                           break;
                        }
                  }
               }
               if (bdo == null) throw new DataFormatException(DataFormatException.BUILDER);
               
               cil.add(ci);
               
               ci.referencedSopClassUid = bdo.getString(Tag.SOPClassUID);
               
               
               c.contourNumber        = 0;
               c.attachedContours     = new ArrayList<>();
               c.contourImageList     = cil;
               c.contourGeometricType = getDicomContourTypeFromAimMarkup(mku);
               c.contourSlabThickness = bdo.getFloat(Tag.SliceThickness);
               c.contourOffsetVector  = new ArrayList<Float>();
               c.nContourPoints       = d2l.size();
               c.contourData          = cd;
               
               position    = bdo.getFloats(Tag.PatientPosition);
               orientation = bdo.getFloats(Tag.PatientOrientation);
               pixelSize   = bdo.getFloats(Tag.PixelSpacing);
               
               for (int i=0; i<d2l.size(); i++)
               {
                  TwoDimensionCoordinate d2 = d2l.get(i);
                  float    x  = (float) d2.getX();
                  float    y  = (float) d2.getY();
                  float [] d3 = ContourRendererHelper.convertFromImageToPatientCoords(
                                          x, y, position, orientation, pixelSize);
                  List<Float> lf = new ArrayList<>();
                  lf.add(d3[0]);
                  lf.add(d3[1]);
                  lf.add(d3[2]);
                  cd.add(lf);
               }                              
            }
         }
      }
		return rcl;
		
		return ss;
	}
	
	
   private List<ReferencedFrameOfReference>
	                   iacBuildRforList(ImageAnnotationCollection iac,
			                                     Map<String, DicomObject>  sopDoMap)
   {
      // This method parallels buildNewRForListFromSubset, but sources the
      // image metadata from a combination of AIM file and existing DICOM
      // images rather than a pre-existing RT-STRUCT.
      
      List<ReferencedFrameOfReference> rforList   = new ArrayList<>();
		
      // First work out how many unique frames of reference we are dealing with.
      // A lot of the time, it is only one.
      List<String> rforUidList = new ArrayList<>();
      
      for (String sop : sopDoMap.keySet())
		{
      	DicomObject bdo = sopDoMap.get(sop);
         String rforUid  = bdo.getString(Tag.FrameOfReferenceUID);
         if (!rforUidList.contains(rforUid))
         {
            ReferencedFrameOfReference rfor = new ReferencedFrameOfReference();
            rforUidList.add(rforUid);
            rforList.add(rfor);
            rfor.frameOfReferenceUid = rforUid;     
         }
		}
      
      List<RtReferencedStudy> rtrsl = new ArrayList<>(); 
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (ImageReference ir : ia.getReferenceList())
         {
           if (ir instanceof DicomImageReference)
            {
               DicomImageReference dir    = (DicomImageReference) ir;
               ImageStudy          study  = dir.getStudy();
               ImageSeries         series = study.getSeries();
               Set<String>         sops   = new HashSet<>();
               for (Image im : series.getImageList()) sops.add(im.getInstanceUid());
               
               boolean newStudy = true;
               for (RtReferencedStudy rtrs : rtrsl)
               {
                  if (rtrs.referencedSopInstanceUid.equals(study.getInstanceUid()))
                     newStudy = false;
               }
               
               if (newStudy)
               {
                  RtReferencedStudy rtrs = new RtReferencedStudy();
                  rtrs.referencedSopInstanceUid = study.getInstanceUid();
                  
                  // Find the corresponding DicomObject in order to get the
                  // SOP class UID, which is not supplied by the AIM metadata.
                  String sop = series.getImageList().get(0).getInstanceUid();
                  DicomObject bdo = sopDoMap.get(sop);
                  rtrs.referencedSopClassUid = rtrs.readString(bdo, Tag.SOPClassUID, 1);
                  
                  rtrs.rtReferencedSeriesList = new ArrayList<>();                 
                  rtrsl.add(rtrs);
               }
               
               for (RtReferencedStudy rtrs : rtrsl)
               {
                  if (rtrs.referencedSopInstanceUid.equals(study.getInstanceUid()))
                  {
                     List<RtReferencedSeries> rtrsel = rtrs.rtReferencedSeriesList;
                     boolean newSeries = true;
                     for (RtReferencedSeries rtrse : rtrsel)
                     {
                        if (rtrse.seriesInstanceUid.equals(series.getInstanceUid()))
                           newSeries = false;
                     }
                     if (newSeries)
                     {
                        RtReferencedSeries rtrse = new RtReferencedSeries();
                        rtrse.seriesInstanceUid  = series.getInstanceUid();
                        rtrse.contourImageList   = new ArrayList<>();
                        rtrsel.add(rtrse);
                     }
                     
                     for (RtReferencedSeries rtrse : rtrsel)
                     {
                        if (rtrse.seriesInstanceUid.equals(series.getInstanceUid()))
                        {
                           for (Image im : series.getImageList())
                           {
                              ContourImage ci             = new ContourImage();
                              ci.referencedSopInstanceUid = im.getInstanceUid();
                              ci.referencedSopClassUid    = im.getSopClassUid();
                              rtrse.contourImageList.add(ci);
                           }
                        }
                     }
                  }
               }
            }            
         }
      }
      
      // By the time we get here, we have created a list of RtReferencedStudies.
      // These might be from different modalities and will all likely have
      // different frames of references. So the last step is to assign each
      // RtReferencedStudy to the correct ReferencedFrameOfReference.
      for (ReferencedFrameOfReference rfor : rforList)
      {
         for (RtReferencedStudy rtrs : rtrsl)
         {
            RtReferencedSeries firstSeries = rtrs.rtReferencedSeriesList.get(0);
            ContourImage       firstImage  = firstSeries.contourImageList.get(0);
            String             firstUid    = firstImage.referencedSopInstanceUid;
            DicomObject        firstDo     = sopDoMap.get(firstUid);
            String             firstForUid = rtrs.readString(firstDo, Tag.FrameOfReferenceUID, 1);
            
            if (rfor.frameOfReferenceUid.equals(firstForUid))
               rfor.rtReferencedStudyList.add(rtrs);
         }
      }
      
      return rforList;
   }
	
   

	private List<StructureSetRoi> iacBuildStructureSetRoiList(ImageAnnotationCollection iac,
			                                                    Map<String, DicomObject>  sopDoMap,
																				 List<RoiContour>          rcl,
																				 String                    name,
																				 String                    description)
	{
		List<StructureSetRoi> ssrl = new ArrayList<>();
		
		for (RoiContour rc : rcl)
		{
			StructureSetRoi ssr = new StructureSetRoi();
			
			ssr.roiNumber       = rc.referencedRoiNumber;
			
			String firstSop     = rc.contourList.get(0).contourImageList.get(0).referencedSopInstanceUid;
			DicomObject firstDo = sopDoMap.get(firstSop);
			ssr.referencedFrameOfReferenceUid
					              = firstDo.getString(Tag.FrameOfReferenceUID);
			
			ssr.roiName         = name;
			ssr.roiDescription  = description;
		}
		return ssrl;
	}
   
	
	private List<RoiContour>
                      iacBuildRoiContourList(ImageAnnotationCollection iac,
			                                    Map<String, DicomObject>  sopDoMap,
															String                    name,
															String                    description)
                      throws DataFormatException
   {
      
      List<RoiContour> rcl = new ArrayList<>();
      
      
   }
							 

							 
   private String getDicomContourTypeFromAimMarkup(Markup mku)
			  throws DataFormatException
	{
      if (mku instanceof TwoDimensionGeometricShape)
      {
         TwoDimensionGeometricShape shape;
         shape = (TwoDimensionGeometricShape) mku;
         if ((shape instanceof TwoDimensionPolyline) ||
             (shape instanceof TwoDimensionCircle) ||
             (shape instanceof TwoDimensionEllipse))
         {
            return "CLOSED_PLANAR";
         }
         if ((shape instanceof TwoDimensionPoint) ||
             (shape instanceof TwoDimensionMultiPoint))
         {
            return "POINT";
         }
      }
		
		throw new DataFormatException(DataFormatException.BUILDER,
				    "The only AIM shape types currently supported are those that "
		          + "convert to DICOM CLOSED_PLANAR and POINT contours.");
	}

}
