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

import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import etherj.aim.Markup;
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
	public String callerName;
	public String callerVersion;
	public String callerManufacturer;
	
	public RtStructBuilder()
	{
		callerName         = "";
		callerVersion      = "";
		callerManufacturer = "";
	}
	
	public RtStructBuilder(String callerName, String callerVersion, String callerManufacturer)
	{
		this.callerName         = callerName;
		this.callerVersion      = callerVersion;
		this.callerManufacturer = callerManufacturer;
	}
	
	
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
                                    Map<String, DicomObject>  sopDoMap,
												Map<String, String>       markupRegionMap)
			          throws DataFormatException
   {
		RtStruct rts = new RtStruct();
		rts.sopCommon            = iacBuildSopCommon(iac);
		rts.patient              = iacBuildPatient(iac, sopDoMap);
		rts.generalStudy         = iacBuildGeneralStudy(rts.sopCommon);
		rts.generalEquipment     = iacBuildGeneralEquipment(iac, sopDoMap);
		rts.rtSeries             = iacBuildRtSeries(iac, rts.sopCommon);
		rts.rtRoiObservationList = iacBuildRtRoiObservationList(iac);
		
		// We need to build the RoiContourList, StructureSet and RtRoiObservations
		// with the same method, because the information needed to build
		// the StructureSetRois is actually parsed at the time of extracting
		// the contour lists.
		iacBuildRoiContourListAndStructureSet(iac, sopDoMap, markupRegionMap, rts);
		
		return rts;
   }

   
   private SopCommon iacBuildSopCommon(ImageAnnotationCollection iac)
			  throws DataFormatException
	{
		SopCommon sc = new SopCommon();
      String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      sc.instanceCreationDate    = DicomXnatDateTime.convertXnatDateTimeToDicomDate(dateTime);
      sc.instanceCreationTime    = DicomXnatDateTime.convertXnatDateTimeToDicomTime(dateTime);
      sc.sopClassUid             = UID.RTStructureSetStorage;
      sc.sopInstanceUid          = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                            UidGenerator.RT_STRUCT,
                                                                  UidGenerator.SOPInstanceUID);
		sc.mediaStorageSopClassUid = UID.RTStructureSetStorage;
		sc.specificCharacterSet    = "ISO_IR 100";
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
	
	
	private String getDefaultIfEmpty(String src)
	{
		final String DEFAULT = "Unknown";
		
		if (src == null)   return DEFAULT;
		if (src.isEmpty()) return DEFAULT;
		return src;
	}
	
	
	private String createStringFromSet(Set<String> set)
	{
		final String SEP = " | ";	
		StringBuilder sb = new StringBuilder();
		
		for (String s : set)
		{
			if (!(sb.toString().contains(s))) 
			{
			   if (sb.length() != 0) sb.append(SEP);
				if (s != null) sb.append(s);
			}
		}
      return getDefaultIfEmpty(sb.toString());

	}
	
	private GeneralEquipment iacBuildGeneralEquipment(ImageAnnotationCollection iac,
			                                            Map<String, DicomObject>  sopDoMap)
	{
		GeneralEquipment ge = new GeneralEquipment();
	
		// Potentially, an AIM file could have several annotations from different
		// studies, which might be on different machines. We need to capture this.
		Set<String> instAddressSet  = new LinkedHashSet<>();
		Set<String> instNameSet     = new LinkedHashSet<>();
		Set<String> manufacturerSet = new LinkedHashSet<>();
		Set<String> modelNameSet    = new LinkedHashSet<>();
		Set<String> stationNameSet  = new LinkedHashSet<>();
		for (String sop : sopDoMap.keySet())
		{
			DicomObject      bdo   = sopDoMap.get(sop);
			GeneralEquipment geSop = new GeneralEquipment(bdo);
			instAddressSet.add(geSop.institutionAddress);
			instNameSet.add(geSop.institutionName);
			manufacturerSet.add(geSop.manufacturer);
			modelNameSet.add(geSop.modelName);
			stationNameSet.add(geSop.stationName);
		}
				
      ge.institutionAddress = createStringFromSet(instAddressSet);
      ge.institutionName    = createStringFromSet(instNameSet);
      ge.manufacturer       = createStringFromSet(manufacturerSet);
      ge.modelName          = createStringFromSet(modelNameSet);
		ge.stationName        = createStringFromSet(stationNameSet);
      List<String> sv = new ArrayList<>();
      sv.add(iac.getAimVersion());
      sv.add("Converted to RT-STRUCT by " + callerName + " version " + callerVersion);
      ge.softwareVersions   = sv;
		
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
									  Map<String, String>       markupRegionMap,
									  RtStruct                  rts)
			  throws DataFormatException
	{
		final float[] DUMMY_F2 = new float[] {0f, 0f};
      final float[] DUMMY_F3 = new float[] {0f, 0f, 0f};
		final float[] DUMMY_F6 = new float[] {0f, 0f, 0f, 0f, 0f, 0f};
      
		StructureSet ss = new StructureSet();
		
      ss.structureSetLabel           = "Auto-created structure set";
      ss.structureSetName            =  "RT-STRUCT_" + iac.getUid();
      ss.structureSetDescription     = "Image markup from AIM instance document with UID "
                                        + iac.getUid() + " converted to RT-STRUCT by ICR XNAT DataUploader";
      ss.instanceNumber              = "1";
		ss.structureSetDate            = rts.sopCommon.instanceCreationDate;
		ss.structureSetTime            = rts.sopCommon.instanceCreationTime;
      ss.referencedFrameOfReferenceList
                                     = iacBuildRforList(iac, sopDoMap);
		ss.predecessorStructureSetList = new ArrayList<>();
		
		
		// Build the list of structure set ROIs from the AIM data.
		// When we have a 2-D shape, there is only one Contour object in the
		// RoiContour and there is also only one ContourImage in the list.
		// Furthermore, there is a one-to-one mapping in this application
		// between RoiContours and StructureSetROIs.
		List<StructureSetRoi> ssrl = new ArrayList<>();
		List<RoiContour>      rcl  = new ArrayList<>();
		int roiCount = 0;
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (Markup mku : ia.getMarkupList())
         {
            if (mku instanceof TwoDimensionGeometricShape)
            {
               TwoDimensionGeometricShape shape;
               shape = (TwoDimensionGeometricShape) mku;
               StructureSetRoi ssr = new StructureSetRoi();              
					RoiContour      rc  = new RoiContour(); 
               rc.referencedRoiNumber = roiCount;
					ssr.roiNumber = roiCount;
					markupRegionMap.put(shape.getUid(), "ROI_" + UidGenerator.createShortUnique());
					roiCount++;
					
               //rc.roiDisplayColour = shape.getLineColour(); Not yet implemented in EtherJ
               List<TwoDimensionCoordinate> d2l = shape.getCoordinateList();
               
               DicomObject        bdo = null;
               List<Contour>      cl  = new ArrayList<>();
               Contour            c   = new Contour();
               List<ContourImage> cil = new ArrayList<>();
               ContourImage       ci  = new ContourImage();
               ArrayList<Integer> fnl = new ArrayList<>();
               List<List<Float>>  cd  = new ArrayList<List<Float>>();
               float[]            position    = DUMMY_F3;
               float[]            orientation = DUMMY_F6;
               float[]            pixelSize   = DUMMY_F2; 
               
               ci.referencedSopInstanceUid = shape.getImageReferenceUid();
               fnl.add(shape.getReferencedFrameNumber());
               ci.referencedFrameNumberList = fnl;
               
               // AIM doesn't store all the information that we need. To get it,
					// we need to find the corresponding DICOM object.
               bdo = sopDoMap.get(shape.getImageReferenceUid());
               if (bdo == null) throw new DataFormatException(DataFormatException.BUILDER);
               ci.referencedSopClassUid = bdo.getString(Tag.SOPClassUID);
					ssr.referencedFrameOfReferenceUid = bdo.getString(Tag.FrameOfReferenceUID);
               
					cil.add(ci);
         
               
               c.contourNumber        = 0;
               c.attachedContours     = new ArrayList<>();
               c.contourImageList     = cil;
               c.contourGeometricType = getDicomContourTypeFromAimMarkup(mku);
               c.contourSlabThickness = bdo.getFloat(Tag.SliceThickness);
               c.contourOffsetVector  = new ArrayList<Float>();
               c.nContourPoints       = d2l.size();
               c.contourData          = cd;
               
					position    = bdo.getFloats(Tag.ImagePositionPatient);
               orientation = bdo.getFloats(Tag.ImageOrientationPatient);
               pixelSize   = bdo.getFloats(Tag.PixelSpacing);					
               
					for (TwoDimensionCoordinate d2 : d2l)
					{
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
	
					ssr.roiName                = shape.getLabel();
					ssr.roiDescription         = shape.getDescription();
					ssr.roiGenerationAlgorithm = ""; // Required field but can be empty.
					ssr.derivationCodeList     = new ArrayList<>();
					
					rcl.add(rc);
					ssrl.add(ssr);
            }
         }
			ss.structureSetRoiList = ssrl;
			rts.structureSet       = ss;
			rts.roiContourList     = rcl;
      }

		
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
		
      
		// Now loop over all the frames of reference and create the underlying structure.
		for (ReferencedFrameOfReference rfor : rforList)
		{
			List<RtReferencedStudy> rtrsl = new ArrayList<>();
			rfor.rtReferencedStudyList = rtrsl;
			
			for (ImageAnnotation ia : iac.getAnnotationList())
			{
				for (ImageReference ir : ia.getReferenceList())
				{
				  if (ir instanceof DicomImageReference)
					{
						DicomImageReference dir    = (DicomImageReference) ir;
						ImageStudy          study  = dir.getStudy();
						ImageSeries         series = study.getSeries();
					
						// Without loss of generality, we can just pick the first
						// image in the series, as all will have the same FoR.
						String      firstSop = series.getImageList().get(0).getInstanceUid();
						DicomObject firstDo  = sopDoMap.get(firstSop);
						if (rfor.frameOfReferenceUid.equals(firstDo.getString(Tag.FrameOfReferenceUID)))
						{
							// At least one of the images in this study belongs in this
							// frame of reference. Now check whether this is the first time
							// we have encountered this particular study for this reference frame.
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
								rtrs.referencedSopClassUid    = firstDo.getString(Tag.SOPClassUID);
								rtrs.rtReferencedSeriesList   = new ArrayList<>();                 
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
         }
      }
            
      return rforList;
   }
	
   

	private List<RtRoiObservation> iacBuildRtRoiObservationList(ImageAnnotationCollection iac)
	{
		List<RtRoiObservation> rrol = new ArrayList<>();
		
		// It is mandatory that this object exists in the DICOM standard, but there is
		// no equivalent requirement in AIM. Thus, we need to include a dummy term here.
		// It is anticipated that future versions of the software will look at the
		// AIM Inference Entities to populate this field. However, to date, we have
		// no examples that contain any of these types of data.
		RtRoiObservation rro     = new RtRoiObservation();
		rro.observationNumber    = 0;
		rro.referencedRoiNumber  = 0;
		rro.roiObservationLabel  = "Dummy entry";
		rro.rtRoiInterpretedType = ""; // Mandatory category 2, can be empty
		rro.roiInterpreter       = ""; // Mandatory category 2, can be empty
		
		rrol.add(rro);
		return rrol;
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
