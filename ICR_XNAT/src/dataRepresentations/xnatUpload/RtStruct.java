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

package dataRepresentations.xnatUpload;

import dataRepresentations.RtStructWriter;
import dataRepresentations.dicom.StructureSetRoi;
import dataRepresentations.dicom.StructureSet;
import dataRepresentations.dicom.SopCommon;
import dataRepresentations.dicom.RtRoiObservation;
import dataRepresentations.dicom.RtReferencedStudy;
import dataRepresentations.dicom.RtReferencedSeries;
import dataRepresentations.dicom.RoiContour;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import dataRepresentations.dicom.Patient;
import dataRepresentations.dicom.ContourImage;
import dataRepresentations.dicom.Contour;
import dataRepresentations.dicom.DicomEntityRepresentation;
import java.util.List;

public class RtStruct extends DicomEntityRepresentation implements RtStructWriter
{
	/* The aim here is to follow the DICOM model fairly closely.
	   The modules mandatory for an RT-STRUCT file are in Table A.1-4
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
	   specifically ROI-related modules can be found from the referenced
		images, only the modules marked * are implemented fully with the
		optional tags. For the others, only required tags are considered
		here.
	*/
	
	public SopCommon              sopCommon;
	public Patient                patient;
	public GeneralStudy           generalStudy;
	public RtSeries               rtSeries;
	public GeneralEquipment       generalEquipment;
	public StructureSet           structureSet;
	public List<RoiContour>       roiContourList;
	public List<RtRoiObservation> rtRoiObservationList;
	
	
	/**
    * Constructor with data from an RT-STRUCT DICOM.
    * @param bdo a DCM4CHE Basic DICOM object that has already been initialised,
    * typically from an RT-STRUCT file, although it could have been created dynamically.
    * @param xnprf an XNAT profile, already connected to an XNAT database, which
    * we can use to query the databases for image dependencies. 
	 * @throws exceptions.DataFormatException 
	 * @throws DataRepresentationException 
    */
	public RtStruct(DicomObject bdo, XNATProfile xnprf)
          throws DataFormatException, DataRepresentationException
   {
      this.bdo         = bdo;
      this.xnprf       = xnprf;
		
		studyUIDs        = new ArrayList<>();
      seriesUIDs       = new ArrayList<>();
      SOPInstanceUIDs  = new ArrayList<>();
      fileSOPMap       = new TreeMap<>();
      fileScanMap      = new TreeMap<>();
      ambiguousSubjExp = new LinkedHashMap<>();
		dav              = new DicomAssignVariable();
           
		// Before we start, check that this really is a structure set!
		if (!bdo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

		structureSetUID  = dav.assignString(bdo, Tag.SOPInstanceUID, 1);
		studyDate        = dav.assignString(bdo, Tag.StudyDate, 2);
		studyTime        = dav.assignString(bdo, Tag.StudyTime, 2);
		studyDescription = dav.assignString(bdo, Tag.StudyDescription, 3);
		patientName      = dav.assignString(bdo, Tag.PatientName, 2);
		
		structureSet     = new StructureSet(bdo);
		
		roiContourList   = dav.assignSequence(RoiContour.class, bdo,
				                                 Tag.ROIContourSequence, 1);
		
		rtRoiObservationList = dav.assignSequence(RtRoiObservation.class, bdo,
				                                 Tag.RTROIObservationsSequence, 1);
		
		 		
		 
		if (!dav.errors.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String er : dav.errors) sb.append(er).append("\n");
			throw new DataRepresentationException(DataRepresentationException.RTSTRUCT,
			                                       sb.toString());
		}
   }
	

	/**
	 * Constructor with data from a previously constructed RtStruct object,
	 * and choosing a subset of the existing regions-of-interest
	 * @param src a source RtStruct.
	 * @param rois 
	 */
	public RtStruct(RtStruct src, Set<Integer> rois)
	{
		version         = src.version;
		bdo             = src.bdo;
		structureSetUID = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DAO,
		                                                 UIDGenerator.RT_STRUCT,
							  											 UIDGenerator.SOPInstanceUID);
		studyDate                            = src.studyDate;
		studyTime                            = src.studyTime;
		studyDescription                     = src.studyDescription;
		patientName                          = src.patientName;
		
		structureSet.structureSetLabel       = src.structureSet.structureSetLabel + " - ROI subset";
		structureSet.structureSetName        = src.structureSet.structureSetName + "_ROIsubset";
		structureSet.structureSetDescription = src.structureSet.structureSetDescription + " generated by ICR_XNAT";
		structureSet.instanceNumber          = src.structureSet.instanceNumber;
		structureSet.structureSetDate        = src.structureSet.structureSetDate;
		structureSet.structureSetTime        = src.structureSet.structureSetTime;
		structureSet.referencedFrameOfReferenceList
		                                     = buildNewRforListFromSubset(src, rois);
		structureSet.structureSetRoiList     = buildNewSsrListFromSubset(src, rois);
		roiContourList                       = buildNewRcListFromSubset(src, rois);
		rtRoiObservationList                 = buildNewRroListFromSubset(src, rois);
	}
	
		private List<ReferencedFrameOfReference>
	                   buildNewRforListFromSubset(RtStruct src, Set<Integer> rois)
		{
			// In order to put the correct entries in the referencedFrameOfReference
			// and rtReferencedStudy variables, we need to look up only the data
			// used by the subset of ROIs. This is a bit of work.

			// First get a list of all the ContourImages contained in the subset.
			// This will be used directly.
			Set srcCiSet = new HashSet<ContourImage>(); 
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
							if (srcCiSet.contains(ci))
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

