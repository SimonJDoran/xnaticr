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
* Builds RtStruct objects from a variety of different sources 
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

public class RtStructBuilder
{
	DicomObject               rtsDo       = null;
	ImageAnnotationCollection iac         = null;
	Map<String, DicomObject>  seriesDoMap = null;
   Set<Integer>              rois        = null;
	
	public void setConstructorPars(DicomObject rtsDo)
	{
		this.rtsDo = rtsDo;
	}
	
	public void setConstructorPars(DicomObject rtsDo, Set<Integer> rois)
	{
		this.rtsDo = rtsDo;
      this.rois  = rois;
	}
   
   public void setConstructorPars(ImageAnnotationCollection iac,
                                  Map<String, DicomObject>  seriesDoMap)
   {
      this.iac         = iac;
      this.seriesDoMap = seriesDoMap;
   }
   
   public RtStruct getNewInstance() throws DataFormatException
   {
      // Check for presence of variables for more than one of the constructor
      // parameters.
      int count = 0;
      
      // RtStruct from uploaded RT-STRUCT file
      if ((rtsDo != null) && (rois == null)) count++; 
      
      // RtStruct from subset of uploaded RT-STRUCT file
      if ((rtsDo != null) && (rois != null)) count++; 
      
      // RtStruct from ImageAnnotationCollection
      if ((iac != null)   && (seriesDoMap != null)) count++;
              
      if (count != 1) throw new DataFormatException(DataFormatException.BUILDER);
           
      RtStruct rts = new RtStruct();
      
      if ((rtsDo != null) && (rois == null))        populateRtStruct(rts, rtsDo);
      if ((rtsDo != null) && (rois != null))        populateRtStruct(rts, src, rois);
      if ((iac != null)   && (seriesDoMap != null)) populateRtStruct(rts, iac, seriesDoMap);
      
      return rts;
   }
   
   
   
   /**
    * Populate the supplied RtStruct object with data from a DicomObject -
    * typically representing a file being uploaded.
    * @param rts Destination RtStruct
    * @param rtsDo Source DICOM object
    * @throws DataFormatException 
    */
   private void populateRtStruct(RtStruct rts, DicomObject rtsDo)
           throws DataFormatException
   {
      // Before we start, check that this really is a structure set!
		if (!rtsDo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

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
   }
   
   
   /**
    * Populate the supplied RtStruct object with data from a DicomObject -
    * typically representing a file being uploaded - but including only a
    * subset of the ROIs definied in the original RT-STRUCT file.
    * @param rts Destination RtStruct
    * @param rtsDo Source DICOM object
    * @param rois Set of regions to put into the output RT-STRUCT.
    */
   private void populateRtStruct(RtStruct src, Set<Integer> rois)
           throws DataFormatException
   {
      RtStruct dest = src.deepCopy();
      
      sopCommon            = dest.sopCommon;
		patient              = dest.patient;
		generalStudy         = dest.generalStudy;
		rtSeries             = dest.rtSeries;
		generalEquipment     = dest.generalEquipment;
		structureSet         = dest.structureSet;
		roiContourList       = dest.roiContourList;
      rtRoiObservationList = dest.rtRoiObservationList;
      
      // Make the modifications that come about because of selecting a single ROI.
		sopCommon.sopInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                          UidGenerator.RT_STRUCT,
                                                                UidGenerator.SOPInstanceUID);

		structureSet.structureSetLabel        = src.structureSet.structureSetLabel + " - ROI subset";
		structureSet.structureSetName         = src.structureSet.structureSetName + "_ROIsubset";
		structureSet.structureSetDescription  = src.structureSet.structureSetDescription + "(ROI subset generated by ICR_XNATDataUploader)";
		structureSet.referencedFrameOfReferenceList
		                                      = buildNewRforListFromSubset(src, rois);
		structureSet.structureSetRoiList      = buildNewSsrListFromSubset(src, rois);
		roiContourList                        = buildNewRcListFromSubset(src, rois);
		rtRoiObservationList                  = buildNewRroListFromSubset(src, rois);
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
							 
			
   }
   
   
   /**
    * Populate the supplied RtStruct object with data from an AIM image
    * annotation collection (normally derived from an AIM XML instance file).
    * Note that most of the data needed for the RT-STRUCT is not contained
    * within the AIM source file and we have to seek it out from some of the
    * original image source DICOM files.
    * @param iac an AIM ImageAnnotationCollection parsed from the source XML
    * by James d'Arcy's Etherj package.
	 * @param seriesDoMap a Map that links a DICOM series to one representative
	 * image from that series, thus allowing us to extract the various header
    * parameters to supplement the information in the image annotation.
	 * @throws exceptions.DataFormatException 
    * @param rts
    * @param iac
    * @param seriesDoMap 
    */
   private void populateRtStruct(RtStruct rts, ImageAnnotationCollection iac,
                             Map<String, DicomObject>  seriesDoMap)
   {
      /**
    * Constructor with data from an AIM image annotation collection (normally
    * derived from an AIM XML instance file). Note that most of the data needed
    * for the RT-STRUCT is not contained within the AIM source file and we have
    * to seek it out from on of the original image source DICOM files.
    * @param iac an AIM ImageAnnotationCollection parsed from the source XML
    * by James d'Arcy's Etherj package.
	 * @param seriesDoMap a Map that links a DICOM series to one representative
	 * image from that series, thus allowing us to extract the various header
    * parameters to supplement the information in the image annotation.
	 * @throws exceptions.DataFormatException 
    */
   public RtStruct(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
                  throws DataFormatException
   {
      iacCreateSopCommon(iac);
		iacCreatePatient(iac, seriesDoMap);
		iacCreateGeneralStudy();
		iacCreateGeneralEquipment(iac);
		iacCreateRtSeries(iac);
		iacCreateStructureSet(iac, seriesDoMap);
      
		

		
		
		
		
		
     
      
      // Create the specific structure set items from information in the AIM
      // image annotation.
      
      
      
      ReferencedFrameOfReference rfor = new ReferencedFrameOfReference();
      
      // Note: At present, the case of an annotation collection with images
      // from DICOM studies with different frames of reference is not supported.
      // This is because of the expense involved in downloading lots of files
      // from the repository and checking the frame of reference UID.
      rfor.frameOfReferenceUid = rfor.readString(iacDo, Tag.FrameOfReferenceUID, 1);
      
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
      List<ReferencedFrameOfReference> rforList   = new ArrayList<>();
      rforList.add(rfor);
      
      structureSet.referencedFrameOfReferenceList = rforList;
      
   }
   
   private void iacCreateSopCommon(ImageAnnotationCollection iac)
			  throws DataFormatException
	{
		sopCommon = new SopCommon();
      String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      sopCommon.instanceCreationDate = DicomXnatDateTime.convertXnatDateTimeToDicomDate(dateTime);
      sopCommon.instanceCreationTime = DicomXnatDateTime.convertXnatDateTimeToDicomTime(dateTime);
      sopCommon.sopClassUid = UID.RTStructureSetStorage;
      sopCommon.sopInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                          UidGenerator.RT_STRUCT,
                                                                UidGenerator.SOPInstanceUID);  
	}
	
	
	private void iacCreatePatient(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
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
		for (String series : seriesDoMap.keySet())
		{
			DicomObject bdo   = seriesDoMap.get(series);
			String      name2 = readString(bdo, Tag.PatientName, 1);
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
		
		patient          = new Patient(templateDo);
	}
	
	
	private void iacCreateGeneralStudy()
	{
		generalStudy     = new GeneralStudy();
		generalStudy.studyInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                         UidGenerator.RT_STRUCT,
																							UidGenerator.StudyInstanceUID);
		generalStudy.studyDate = sopCommon.instanceCreationDate;
		generalStudy.studyTime = sopCommon.instanceCreationTime;
	}
	
	
	private void iacCreateGeneralEquipment(ImageAnnotationCollection iac)
	{
		final String DEFAULT = "Unknown";
		generalEquipment = new GeneralEquipment();
      generalEquipment.institutionAddress = DEFAULT;
      generalEquipment.institutionName    = DEFAULT;
      generalEquipment.manufacturer       = "Institute of Cancer Research";
      generalEquipment.modelName          = "ICR XNAT DataUploader";
      List<String> sv = new ArrayList<>();
      sv.add(iac.getAimVersion());
      sv.add("Converted to RT-STRUCT by ICR XNAT DataUploader");
      generalEquipment.softwareVersions   = sv;
      try
		{
			generalEquipment.stationName = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException exUH)
		{
			generalEquipment.stationName = DEFAULT;
		}
	}
	
	
	private void iacCreateRtSeries(ImageAnnotationCollection iac)
	{
		rtSeries                   = new RtSeries();
		rtSeries.modality          = "RTSTRUCT";
		rtSeries.seriesInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                      UidGenerator.RT_STRUCT,
																						UidGenerator.SeriesInstanceUID);
		rtSeries.seriesNumber      = 1;
		rtSeries.seriesDate        = sopCommon.instanceCreationDate;
		rtSeries.seriesTime        = sopCommon.instanceCreationTime;
		rtSeries.seriesDescription = "Image markup from AIM instance with UID " + iac.getUid()
				                       + " converted RT-STRUCT by XNAT DataUploader";
		rtSeries.operatorName      = System.getProperty("user.name");
	}
	
	
	private void iacStructureSet(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
			  throws DataFormatException
	{
		structureSet                         = new StructureSet();
      structureSet.structureSetLabel       = "Auto-created structure set";
      structureSet.structureSetName        = iac.getUid() + "_RT-STRUCT";
      structureSet.structureSetDescription = "Image markup from AIM instance document with UID "
                                             + iac.getUid() + " converted RT-STRUCT by ICR XNAT DataUploader";
      structureSet.instanceNumber          = "1";
		structureSet.structureSetDate        = sopCommon.instanceCreationDate;
		structureSet.structureSetTime        = sopCommon.instanceCreationTime;
		
	
   }
}
