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
* First created on Jul 12, 2012 at 10:53:42 PM
* 
* Define a representation of the RT-STRUCT data structure, including
* methods to read the data in from a DICOM file and create a new
* DICOM file from an instance. Significantly refactored starting
* 25.1.16.
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: RtStruct.java
* First created on Jan 25, 2016 at 3:53:12 PM
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RTStruct_old.DUMMY_INT;
import exceptions.DataFormatException;
import exceptions.DataRepresentationException;
import generalUtilities.DicomUtilities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import xnatDAO.XNATProfile;
import xnatUploader.AmbiguousSubjectAndExperiment;

public class RtStruct extends XnatUploadRepresentation implements RtStructWriter
{
   static    Logger                        logger = Logger.getLogger(RtStruct.class);
   
   protected static final int              DUMMY_INT   = -999;
   protected static final float            DUMMY_FLOAT = -999.9f;
	
	public String                           version;
   public DicomObject                      bdo;
   public String                           structureSetUID;
   public String                           structureSetLabel;
   public String                           structureSetName;
   public String                           structureSetDate;
   public String                           structureSetTime;
   public String                           structureSetDescription;
   public String                           instanceNumber;
   public List<RtReferencedStudy>          refStudyList;
   public List<ReferencedFrameOfReference> rforList;
   public List<StructureSetRoi>            ssrList;
   public List<RoiContour>                 roiContourList;
   public List<RtRoiObservation>           roiObsList;   
   public String                           roiSetID;
   public String                           roiSetLabel;
   public ArrayList<String>                studyUIDs;
   public ArrayList<String>                seriesUIDs;
   public ArrayList<String>                SOPInstanceUIDs;
   public String                           studyDate;
   public String                           studyTime;
   public String                           studyDescription;
   public String                           patientName;
	public String                           XNATDateOfBirth;
   public String                           XNATGender;
   public LinkedHashMap<String,
			   AmbiguousSubjectAndExperiment> ambiguousSubjExp;
	public ArrayList<String>                warnings;
	public ArrayList<String>                errors;
	
	
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
           
		// Before we start, check that this really is a structure set!
		if (!bdo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

		structureSetUID         = assignString(Tag.SOPInstanceUID, 1);
		structureSetLabel       = assignString(Tag.StructureSetLabel, 1);
		structureSetName        = assignString(Tag.StructureSetName, 3);
		structureSetDescription = assignString(Tag.StructureSetDescription, 3);
		instanceNumber          = assignString(Tag.InstanceNumber, 3);
		structureSetDate        = assignString(Tag.StructureSetDate, 2);
		structureSetTime        = assignString(Tag.StructureSetTime, 2);
		studyDate               = assignString(Tag.StudyDate, 2);
		studyTime               = assignString(Tag.StudyTime, 2);
		studyDescription        = assignString(Tag.StudyDescription, 3);
		patientName             = assignString(Tag.PatientName, 2);

		errors   = new ArrayList<>();
		warnings = new ArrayList<>();

		
		
		if (!errors.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String er : errors) sb.append(er).append("\n");
			throw new DataRepresentationException(DataRepresentationException.RTSTRUCT,
			                                       sb.toString());
		}
   }
	
	public final String assignString(int tag, int requirementType)
	{
		return assignString(tag, Integer.toString(requirementType));
	}
	
	public final String assignString(int tag, String requirementType)
	{
		DicomUtilities.AssignStringStatus as = new DicomUtilities.AssignStringStatus();
		String tagValue = DicomUtilities.assignString(bdo, tag, requirementType, as);
		if (as.error   != null) errors.add(as.error);
		if (as.warning != null) warnings.add(as.error);
		return tagValue;
	}
	
	
   
	/** Extract the frame-of-reference-information.
    * This entails not only the SOPInstanceUID of the frame-of-reference in which the
    * structure set contours themselves are defined, but also the SOPInstanceUID's
    * of any related frames of reference, together with their relationship
    * to the frame-of-reference of this structure set. As an added complication
    * if the ROIs were drawn on more than one series, then there are multiple
    * items in this sequence.
    * @param issues ArrayList to which any problems found will be added
    */
   protected void buildReferencedFrameOfReferenceList()
   {   
		int          rforTag = Tag.ReferencedFrameOfReferenceSequence;
		DicomElement rforSeq = bdo.get(rforTag);

		if (rforSeq == null)
		{
			warnings.add("Optional tag " + rforTag + " " + bdo.nameOf(rforTag)
					          + " is not present in input.");
			return;
		}

		int nRfor = rforSeq.countItems();
		rforList  = new ArrayList<>();
		for (int i=0; i<nRfor; i++)
		{
			DicomObject rforDo = rforSeq.getDicomObject(i);
			DicomUtilities.AssignStringStatus ass = new DicomUtilities.AssignStringStatus();
			ReferencedFrameOfReference rfor = new ReferencedFrameOfReference(rforDo, ass);
			errors.addAll(ass.errors);
			warnings.addAll(ass.warnings);
			if (rfor != null) rforList.add(rfor);           
		}
   }
	
	
	/**
    * Read the information on any studies referred to and place in object instance
    * variables. 
	 * @param issues ArrayList to which any problems found will be added
    */
   protected void extractReferencedStudyInfo(ArrayList<String> issues)
   {
 
		DicomElement seqRefStudy  = bdo.get(Tag.ReferencedStudySequence);

		if (seqRefStudy == null)
		{
			warnings.add("No information in DICOM file on referenced studies.");
			return;
		}
			
		int nRefStudy = seqRefStudy.countItems();
		refStudyList = new List<>();

		for (int i=0; i<nRefStudy; i++)
		{
			DicomObject       refStudy = seqRefStudy.getDicomObject(i);
			RtReferencedStudy rrs      = new RtReferencedStudy();

			rrs.referencedSopInstanceUid = refStudy.getString(Tag.ReferencedSOPInstanceUID);
			refStudyList[i].SOPClassUID    = refStudy.getString(Tag.ReferencedSOPClassUID);   
			refStudyList.add(rrs);
		}
      
   }

}
