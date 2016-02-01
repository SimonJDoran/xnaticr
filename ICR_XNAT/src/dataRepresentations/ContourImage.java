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

import generalUtilities.DicomAssignString;
import java.util.ArrayList;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class ContourImage extends DicomEntityRepresentation
{
	public String referencedSopInstanceUid;
	public String referencedSopClassUid;
	public String referencedFrameNumber;
	
	public ContourImage(String sopInstance, String sopClass, String frame)
	{
		referencedSopInstanceUid = sopInstance;
		referencedSopClassUid    = sopClass;
		referencedFrameNumber    = frame;
	}
	
	public ContourImage(DicomObject ciDo)
	{
		referencedSopInstanceUid = das.assignString(ciDo, Tag.ReferencedSOPInstanceUID, 1);
		referencedSopClassUid    = das.assignString(ciDo, Tag.ReferencedSOPClassUID,    1);
		
		// Frame number is a class 1C tag. Ideally, I would check whether the
		// image is multiframe before reading it, but at this point, I don't have
		// easy access to the entire DICOM file to check whether it is multiframe.
		// For the moment, just try to read, but swallow the error if nothing
		// comes back.
		DicomAssignString junk = new DicomAssignString();
		referencedFrameNumber = junk.assignString(ciDo, Tag.ReferencedFrameNumber, "1C");
	}
}
