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

/*********************************************************************
* @author Simon J Doran
* Java class: StructureSet.java
* First created on Feb 2, 2016 at 8:22:18 AM
* 
* Define a representation of the StructureSet DICOM module/sequence.
* It is not entirely clear to me whether the this is what is implied
* by the description of element (3006,0018), which describes a
* "structure set sequence". Is this the same as the structure set
* module. If so, then we have a recursive definition.
*********************************************************************/

package dataRepresentations.dicom;

import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class StructureSet extends DicomEntity
{
	public String                           structureSetLabel;
   public String                           structureSetName;
	public String                           structureSetDescription;
   public String                           instanceNumber;
   public String                           structureSetDate;
   public String                           structureSetTime;
   public List<ReferencedFrameOfReference> referencedFrameOfReferenceList;
   public List<StructureSetRoi>            structureSetRoiList;
   public List<StructureSet>               predecessorStructureSetList;
	
	protected StructureSet()
	{
		// The empty constructor is necessary as part of the process for the
		// deepCopy() method.
	}
	
	public StructureSet(DicomObject ssDo)
	{
		structureSetLabel       = readString(ssDo, Tag.StructureSetLabel, 1);
		structureSetName        = readString(ssDo, Tag.StructureSetName, 3);
		structureSetDescription = readString(ssDo, Tag.StructureSetDescription, 3);
		instanceNumber          = readString(ssDo, Tag.InstanceNumber, 3);
		structureSetDate        = readString(ssDo, Tag.StructureSetDate, 2);
		structureSetTime        = readString(ssDo, Tag.StructureSetTime, 2);
		
		referencedFrameOfReferenceList = readSequence(ReferencedFrameOfReference.class, 
				                             ssDo, Tag.ReferencedFrameOfReferenceSequence, 3);
		
		structureSetRoiList     = readSequence(StructureSetRoi.class, ssDo,
				                                   Tag.StructureSetROISequence, 1);
		
		// For some reason Tag.PredecessorStructureSetSequence is not defined in dcm4che 2.0.29.
		// Note also that there is only ever one value of the predecessor structure
		// set, but formally it is obtained from a sequence, which is represented
		// by a list here.
		int pssTag = Integer.valueOf("30060018", 16);
		predecessorStructureSetList = readSequence(StructureSet.class, ssDo, pssTag, 3);
	}


	@Override
	public void writeToDicom(DicomObject ssDo)
	{
		writeString(ssDo,   Tag.StructureSetLabel,        VR.SH, 1, structureSetLabel);
		writeString(ssDo,   Tag.StructureSetName,         VR.LO, 1, structureSetName);
		writeString(ssDo,   Tag.StructureSetDescription,  VR.ST, 3, structureSetDescription);
		writeString(ssDo,   Tag.InstanceNumber,           VR.IS, 3, instanceNumber);
		writeString(ssDo,   Tag.StructureSetDate,         VR.DA, 2, structureSetDate);
		writeString(ssDo,   Tag.StructureSetTime,         VR.TM, 2, structureSetTime);
		writeSequence(ssDo, Tag.ReferencedFrameOfReferenceSequence, VR.SQ, 3,
				                                    referencedFrameOfReferenceList);
		
		writeSequence(ssDo, Tag.StructureSetROISequence,  VR.SQ, 1, structureSetRoiList);
		
		int pssTag = Integer.valueOf("30060018", 16);
		writeSequence(ssDo, pssTag,                       VR.SQ, 3, predecessorStructureSetList);
	}
}
