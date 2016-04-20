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

package dataRepresentations.dicom;

import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class StructureSetRoi extends DicomEntity
{
	public int         roiNumber;
	public int         correspondingROIObservation;
	public String      referencedFrameOfReferenceUid;
	public String      roiName;
	public String      roiDescription;
	public float       roiVolume;
	public String      roiGenerationAlgorithm;
	public String      roiGenerationDescription;
	public List<Code>  derivationCodeList;
	
	protected StructureSetRoi()
	{
		// The empty constructor is necessary as part of the process for the
		// deepCopy() method.
	}

	public StructureSetRoi(DicomObject ssrDo)
	{
		roiNumber                     = readInt(ssrDo,    Tag.ROINumber, 1);		
		referencedFrameOfReferenceUid = readString(ssrDo, Tag.ReferencedFrameOfReferenceUID, 1);
	   roiName                       = readString(ssrDo, Tag.ROIName, 2);
		roiDescription                = readString(ssrDo, Tag.ROIDescription, 3);
		roiVolume                     = readFloat(ssrDo,  Tag.ROIVolume, 3);
		roiGenerationAlgorithm        = readString(ssrDo, Tag.ROIGenerationAlgorithm, 2);
		roiGenerationDescription      = readString(ssrDo, Tag.ROIGenerationDescription, 3);
		
		derivationCodeList = readSequence(Code.class, ssrDo, Tag.DerivationCodeSequence, 1);
	}
	
	
	@Override
	public void writeToDicom(DicomObject ssrDo)
	{
		writeInt(ssrDo,      Tag.ROINumber, VR.IS, 1, roiNumber);
		writeString(ssrDo,   Tag.ReferencedFrameOfReferenceUID, VR.UI, 1, referencedFrameOfReferenceUid);
      writeString(ssrDo,   Tag.ROIName,   VR.LO, 2, roiName);
		writeString(ssrDo,   Tag.ROIDescription, VR.ST, 3, roiDescription);
		writeFloat(ssrDo,    Tag.ROIName,   VR.DS, 3, roiVolume);
		writeString(ssrDo,   Tag.ROIGenerationAlgorithm, VR.CS, 3, roiGenerationAlgorithm);
		writeString(ssrDo,   Tag.ROIGenerationDescription, VR.LO, 3, roiGenerationDescription);
		writeSequence(ssrDo, Tag.DerivationCodeSequence, VR.SQ, 1, derivationCodeList);
	}
}
