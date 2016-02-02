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

package dataRepresentations;

import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class StructureSet extends DicomEntityRepresentation
{
	public String                           structureSetLabel;
   public String                           structureSetName;
	public String                           structureSetDescription;
   public String                           instanceNumber;
   public String                           structureSetDate;
   public String                           structureSetTime;
   public List<ReferencedFrameOfReference> referencedFrameOfReferenceList;
   public List<StructureSetRoi>            structureSetRoiList;
   public StructureSet                     predecessorStructureSet;
	
	public StructureSet(DicomObject ssDo)
	{
		structureSetLabel       = das.assignString(ssDo, Tag.StructureSetLabel, 1);
		structureSetName        = das.assignString(ssDo, Tag.StructureSetName, 3);
		structureSetDescription = das.assignString(ssDo, Tag.StructureSetDescription, 3);
		instanceNumber          = das.assignString(ssDo, Tag.InstanceNumber, 3);
		structureSetDate        = das.assignString(ssDo, Tag.StructureSetDate, 2);
		structureSetTime        = das.assignString(ssDo, Tag.StructureSetTime, 2);
		structureSetRoiList     = new ArrayList<>();
		referencedFrameOfReferenceList = new ArrayList<>();
		
		
		int          rforTag    = Tag.ReferencedFrameOfReferenceSequence;
		DicomElement rforSeq    = ssDo.get(rforTag);
		
		if (rforSeq == null)
		{
			das.errors.add("Optional tag " + Integer.toHexString(rforTag) + " " + ssDo.nameOf(rforTag)
					          + " is not present in input.");
		}
		else
		{
			for (int i=0; i<rforSeq.countItems(); i++)
			{
				DicomObject                rforDo = rforSeq.getDicomObject(i);
				ReferencedFrameOfReference rfor   = new ReferencedFrameOfReference(rforDo);
				if (rfor.das.errors.isEmpty()) referencedFrameOfReferenceList.add(rfor); 
				das.errors.addAll(rfor.das.errors);
				das.warnings.addAll(rfor.das.warnings);          
			}
		}
		
		
		int           ssrTag = Tag.StructureSetROISequence;
		DicomElement  ssrSeq = ssDo.get(ssrTag);

		if (ssrSeq == null)
		{
			das.errors.add("Required tag " + Integer.toHexString(ssrTag) + " " + ssDo.nameOf(ssrTag)
					          + " is not present in input.");
		}
		else
		{
			for (int i=0; i<ssrSeq.countItems(); i++)
			{
				DicomObject     ssrDo = ssrSeq.getDicomObject(i);
				StructureSetRoi ssr   = new StructureSetRoi(ssrDo);
				if (ssr.das.errors.isEmpty()) structureSetRoiList.add(ssr); 
				das.errors.addAll(ssr.das.errors);
				das.warnings.addAll(ssr.das.warnings);          
			}
		}
		
		// For some reason Tag.PredecessorStructureSetSequence is not defined in dcm4che 2.0.29.
		int          pssTag = Integer.valueOf("30060018", 16);
		DicomElement pssSeq = ssDo.get(pssTag);
		if (pssSeq == null)
		{
			das.warnings.add("Optional tag " + Integer.toHexString(pssTag) + " PredecessorStructureSetSequence"
					          + " is not present in input.");
		}
		else
		{
			int nPss = pssSeq.countItems();
			if (nPss != 1)
			{
				das.warnings.add("Optional tag " + Integer.toHexString(pssTag)
				               + "has VM=1 specified in the standard, but has "
				               + nPss + " entries in the input. Only first value "
				               + "will be returned.");
				if (nPss > 0)
				{
					DicomObject  pssDo       = pssSeq.getDicomObject(0);
					predecessorStructureSet = new StructureSet(pssDo);
				}
			}
		}
	}
}
