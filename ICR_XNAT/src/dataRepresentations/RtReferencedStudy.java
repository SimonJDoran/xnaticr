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
* Java class: RtReferencedStudy.java
* First created on Jan 21, 2016 at 00:31:00 AM
* 
* Data structure parallelling the icr:rtReferencedStudyData element
* and used in conjunction with IcrRtReferencedStudyDataMDComplexType.
*********************************************************************/

package dataRepresentations;

import java.util.List;
import org.dcm4che2.data.DicomObject;
import java.util.ArrayList;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class RtReferencedStudy extends DicomEntityRepresentation
{
	public String                   referencedSopClassUid;
	public String                   referencedSopInstanceUid;
	public List<RtReferencedSeries> rtReferencedSeriesList;
	
	public RtReferencedStudy() {}
	
	public RtReferencedStudy(String sopClass, String sopInstance,
									 List<RtReferencedSeries> seriesList)
	{
		referencedSopClassUid    = sopClass;
		referencedSopInstanceUid = sopInstance; 
		rtReferencedSeriesList   = seriesList;
	}
	
	
	public RtReferencedStudy(DicomObject rrsDo)
	{
		referencedSopClassUid    = readString(rrsDo, Tag.ReferencedSOPClassUID,    1);
		referencedSopInstanceUid = readString(rrsDo, Tag.ReferencedSOPInstanceUID, 1);
	   rtReferencedSeriesList   = readSequence(RtReferencedSeries.class, rrsDo, Tag.RTReferencedSeriesSequence, 1);
	}
	
	
	@Override
	public void writeToDicom(DicomObject rrsDo)
	{
		writeString(rrsDo,   Tag.ReferencedSOPClassUID,       VR.UI, 1, referencedSopClassUid);
		writeString(rrsDo,   Tag.ReferencedSOPInstanceUID,    VR.UI, 1, referencedSopInstanceUid);
		writeSequence(rrsDo, Tag.RTReferencedSeriesSequence, VR.SQ, 1, rtReferencedSeriesList);		
	}
}
