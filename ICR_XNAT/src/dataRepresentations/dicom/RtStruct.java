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

import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import exceptions.DataFormatException;
import exceptions.DataRepresentationException;
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
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
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
		// deepCopy() method and RtStructBuilder class.
	}
	
   // All the heavy lifting for constructing and RtStruct is performed by the
   // class RtStructBuilder, which contains an overloaded method buildNewInstance
   // that takes different parameters depending on how we wish to build the
   // RtStruct object.
						 
	@Override
	public void writeToDicom(DicomObject rtsDo)
	{
      final String EXPLICIT_VR_LITTLE_ENDIAN = "1.2.840.10008.1.2.";
      writeString(rtsDo, Tag.TransferSyntaxUID, VR.UI, 1, EXPLICIT_VR_LITTLE_ENDIAN);
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

