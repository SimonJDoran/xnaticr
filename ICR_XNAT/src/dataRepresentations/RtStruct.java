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

public class RtStruct extends DataRepresentation implements RtStructWriter
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

		// Get information on the (potentially multiple) studies referenced
		// by this DICOM RT-Struct file.
		extractReferencedStudyInfo();

		// Nested within the frames-of-reference DICOM sequence is also all the
		// information on the referenced studies, on which the contours are
		// defined. Both the Java classes above and the corresponding custom
		// XNAT schema are designed to relate directly to the DICOM structures.
		extractFramesOfReferenceInfo();

		// Check that all the studies, series and SOPInstances referenced are
		// already present in the database.
		dependenciesInDatabase();

		// Extract overview information about the ROIs contained in the
		// structure set file, such as name, structureSetDescription, volumne, generating
		// algorithm, etc.
		extractStructureSetROIInfo();

		// Extract information on the individual contours that make up each
		// of the ROIs. Note that we extract only the metadata, not the actual
		// contour coordinates themselves.
		extractContourInfo();

		// Extract information on the radiotherapy-related interpretation
		// of the ROIs. This includes the identification as an organ, PTV,
		// marker, etc. and its physical composition.
		extractRtRoiObservationsInfo();
		
		if (!issues.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String issue : issues) sb.append(issue).append("\n");
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
		String  tagValue   = null;
		boolean tagPresent = bdo.contains(tag);
		if (tagPresent) tagValue = bdo.getString(tag);
		
		switch(requirementType)
		{
			case "1":  // Required
			case "1C": // Conditionally required. This is hard to treat for the general
				        // case. Treat as if required and handle the conditions in the
				        // calling code.
				if ((!tagPresent) || (tagValue == null) || (tagValue.length() == 0))
				{
					errors.add("Required tag not found in input: "
							         + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
			
			case "2":  // Required
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (!tagPresent)
				{
					errors.add("Required tag not found in input: "
							         + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
			
			case "3":  // Optional
				if (!tagPresent)
				{
					warnings.add("Optional tag not present in input: "
							         + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
		}
		return tagValue;
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
