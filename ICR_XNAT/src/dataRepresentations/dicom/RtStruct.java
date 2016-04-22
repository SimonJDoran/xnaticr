/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
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

/*********************************************************************
* @author Simon J Doran
* Java class: RtStruct.java
* First created on Jan 25, 2016 at 3:53:12 PM
* 
* Define a representation of the RT-STRUCT data structure, including
* methods to read the data in from a DICOM file and create a new
* DICOM file from an instance. Significantly refactored starting
* 25.1.16.
*********************************************************************/

package dataRepresentations.dicom;

import exceptions.DataFormatException;
import exceptions.DataRepresentationException;
import generalUtilities.UIDGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class RtStruct extends DicomEntity
{
	/* The aim here is to follow the DICOM model fairly closely.
	   The IOD modules mandatory for an RT-STRUCT file are in Table A.1-4
	   of the DICOM standard and are as follows:
	   Patient
	   General Study
	   RT Series
	   General Equipment
	   Structure Set*
	   ROI Contour*
	   RT ROI Obseervations*
	   SOP Common
	  
	   On the assumption that all of the information that is not in the
	   specifically ROI-related IOD modules can be found from the referenced
		images, only the modules marked * are implemented fully with the
		optional tags. For the others, only required tags and a subset of
	   the optional tags are considered here.
	
		There are other IOD modules that are not mandatory for a valid structure
	   set. These are not implemented here to avoid the code becoming too large
	   and unwieldy.
	*/
	
	public SopCommon              sopCommon;
	public Patient                patient;
	public GeneralStudy           generalStudy;
	public RtSeries               rtSeries;
	public GeneralEquipment       generalEquipment;
	public StructureSet           structureSet;
	public List<RoiContour>       roiContourList;
	public List<RtRoiObservation> rtRoiObservationList;

	
	protected RtStruct()
	{
		// The empty constructor is necessary as part of the process for the
		// deepCopy() method.
	}
	
	/**
    * Constructor with data from an RT-STRUCT DICOM.
    * @param rtsDo a DCM4CHE DICOM object that has already been initialised,
    * typically from an RT-STRUCT file, although it could have been created dynamically.
	 * @throws exceptions.DataFormatException 
    */
	public RtStruct(DicomObject rtsDo) throws DataFormatException
   {         
		// Before we start, check that this really is a structure set!
		if (!rtsDo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

		sopCommon        = new SopCommon(rtsDo);
		patient          = new Patient(rtsDo);
		generalStudy     = new GeneralStudy(rtsDo);
		rtSeries         = new RtSeries(rtsDo);
		generalEquipment = new GeneralEquipment(rtsDo);
		structureSet     = new StructureSet(rtsDo);
		
		roiContourList       = readSequence(RoiContour.class, rtsDo,
				                                         Tag.ROIContourSequence, 1);
		rtRoiObservationList = readSequence(RtRoiObservation.class, rtsDo,
				                                  Tag.RTROIObservationsSequence, 1);
   }
	

	/**
	 * Constructor with data from a previously constructed RtStruct object,
	 * and choosing a subset of the existing regions-of-interest
	 * @param src a source RtStruct.
	 * @param rois 
	 */
	public RtStruct(RtStruct src, Set<Integer> rois)
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
		sopCommon.sopInstanceUid = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DAO,
		                                                          UIDGenerator.RT_STRUCT,
                                                                UIDGenerator.SOPInstanceUID);

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
							 
							 
	@Override
	public void writeToDicom(DicomObject rtsDo)
	{
		sopCommon.writeToDicom(rtsDo);
		patient.writeToDicom(rtsDo);
		generalStudy.writeToDicom(rtsDo);
		rtSeries.writeToDicom(rtsDo);
		generalEquipment.writeToDicom(rtsDo);
		structureSet.writeToDicom(rtsDo);
		
		writeSequence(rtsDo, Tag.ROIContourSequence,        VR.SQ, 1, roiContourList);
		writeSequence(rtsDo, Tag.RTROIObservationsSequence, VR.SQ, 1, rtRoiObservationList);
	}
}

