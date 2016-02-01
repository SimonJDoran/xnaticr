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
* Java class: StructureSetRoi.java
* First created on Jan 27, 2016 at 10:16:34 AM
* 
* Define a representation of the StructureSetROI DICOM sequence
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RtStruct.DUMMY_FLOAT;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class StructureSetRoi extends DicomEntityRepresentation
{
	public int                       roiNumber;
	public int                       correspondingROIContour;
	public int                       correspondingROIObservation;
	public String                    referencedFrameOfReferenceUid;
	public String                    roiName;
	public String                    roiDescription;
	public float                     roiVolume = DUMMY_FLOAT;
	public String                    roiGenerationAlgorithm;
	public String                    roiGenerationDescription;
	public String                    derivationCode;
	public String                    roiXNATID;

	public StructureSetRoi(DicomObject ssrDo)
	{
		String s = das.assignString(ssrDo, Tag.ROINumber, 1);
		roiNumber = Integer.parseInt(s);
		
		referencedFrameOfReferenceUid = das.assignString(ssrDo, Tag.ReferencedFrameOfReferenceUID, 1);
	   roiName                       = das.assignString(ssrDo, Tag.ROIName, 2);
		roiDescription                = das.assignString(ssrDo, Tag.ROIDescription, 3);
		
		s = das.assignString(ssrDo, Tag.ROIVolume, 3);
		if (s != null) roiVolume = Float.parseFloat(s);
		
		roiGenerationAlgorithm        = das.assignString(ssrDo, Tag.ROIGenerationAlgorithm, 2);
		roiGenerationDescription      = das.assignString(ssrDo, Tag.ROIGenerationDescription, 3);
		
		int            dcTag = Tag.DerivationCodeSequence;
		DicomElement   dcSeq = ssrDo.get(dcTag);
		DerivationCode dc    = new DerivationCode();
		if (dcSeq == null)
		{
			das.warnings.add("Optional tag " + dcTag + " " + ssrDo.nameOf(dcTag)
					          + " is not present in input.");
			return;
		}
		derivationCode = 
	}
}
