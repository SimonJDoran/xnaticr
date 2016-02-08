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

/********************************************************************
* @author Simon J Doran
* Java class: ContourImage.java
* First created on Jan 20, 2016 at 11:40:00 PM
* 
* Data structure parallelling the icr:contourImageData element and
* used in conjunction with IcrContourImageDataMDComplexType.
*********************************************************************/
package dataRepresentations;

import generalUtilities.DicomAssignVariable;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class ContourImage extends DicomEntityRepresentation
{
	public String referencedSopInstanceUid;
	public String referencedSopClassUid;
	public int[]  referencedFrameNumber;
	public int    referencedSegmentNumber;
	
	
	public ContourImage(DicomObject ciDo)
	{
		referencedSopInstanceUid = readString(ciDo, Tag.ReferencedSOPInstanceUID, 1);
		referencedSopClassUid    = readString(ciDo, Tag.ReferencedSOPClassUID,    1);
		
		// Frame number and segment number areclass 1C tags. Ideally, I would check
		// whether the images being referred to are multiframe before reading
		// these, but at this point in the process, I don't have easy access
		// easy access to the relevant DICOM file to check whether it is multiframe.
		// For the moment, just try to read, but swallow the error if nothing
		// comes back.
		ArrayList<String> tempErrors = errors;
		referencedFrameNumber   = readInts(ciDo, Tag.ReferencedFrameNumber, "1C");
		referencedSegmentNumber = readInt(ciDo, Tag.ReferencedFrameNumber,  "1C");
		errors = tempErrors;
	}
	
	
	public void writeToDicom(DicomObject cdDo)
	{
		writeString(cdDo, Tag.ReferencedSOPInstanceUID, VR.UI, 1, referencedSopInstanceUid);
		writeString(cdDo, Tag.ReferencedSOPClassUID,    VR.UI, 1, referencedSopClassUid);
		
		if (referencedFrameNumber != null)
			writeInts(cdDo, Tag.ReferencedFrameNumber, VR.IS, "1C", referencedFrameNumber);
		
		if (referencedSegmentNumber != DUMMY_INT)
			writeInt(cdDo, Tag.ReferencedSegmentNumber, VR.IS, "1C", referencedSegmentNumber);
	}
}
