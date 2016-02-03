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
import generalUtilities.DicomAssignVariable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
   
	public String                           version;
   public DicomObject                      bdo;
   public String                           structureSetUID;
   public String                           roiSetID;
   public String                           roiSetLabel;
   public List<String>                     studyUIDs;
   public List<String>                     seriesUIDs;
   public List<String>                     SOPInstanceUIDs;
   public String                           studyDate;
   public String                           studyTime;
   public String                           studyDescription;
   public String                           patientName;
	public String                           XNATDateOfBirth;
   public String                           XNATGender;
   public Map<String,
			   AmbiguousSubjectAndExperiment> ambiguousSubjExp;
	public DicomAssignVariable                das;
	public StructureSet                     structureSet;
	public List<RoiContour>                 roiContourList;
	
	
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
		das              = new DicomAssignVariable();
           
		// Before we start, check that this really is a structure set!
		if (!bdo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

		structureSetUID  = das.assignString(bdo, Tag.SOPInstanceUID, 1);
		studyDate        = das.assignString(bdo, Tag.StudyDate, 2);
		studyTime        = das.assignString(bdo, Tag.StudyTime, 2);
		studyDescription = das.assignString(bdo, Tag.StudyDescription, 3);
		patientName      = das.assignString(bdo, Tag.PatientName, 2);
		
		structureSet     = new StructureSet(bdo);
		
		 
		if (!das.errors.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String er : das.errors) sb.append(er).append("\n");
			throw new DataRepresentationException(DataRepresentationException.RTSTRUCT,
			                                       sb.toString());
		}
   }


}
