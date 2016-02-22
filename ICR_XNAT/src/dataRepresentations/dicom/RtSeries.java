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
* Java class: RtSeries.java
* First created on Feb 22, 2016 at 11:14:15 AM
* 
* Define a partial representation of the DICOM RT Series module,
* including all mandatory and some optional components.
*********************************************************************/


package dataRepresentations.dicom;

import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class RtSeries extends DicomEntityRepresentation
{
	public String     modality;
	public String     seriesInstanceUid;
	public int        seriesNumber;
	public String     seriesDate;
	public String     seriesTime;
	public String     seriesDescription;
	public List<Code> seriesDescriptionCodeList;
	public String     operatorName;

	public RtSeries() {}
	
	public RtSeries(DicomObject rtsDo)
	{
		modality          = readString(rtsDo, Tag.Modality, 1);
		seriesInstanceUid = readString(rtsDo, Tag.SeriesInstanceUID, 1);
		seriesNumber      = readInt(rtsDo,    Tag.SeriesNumber, 2);
		seriesDate        = readString(rtsDo, Tag.SeriesDate, 3);
		seriesTime        = readString(rtsDo, Tag.SeriesTime, 3);
		seriesDescription = readString(rtsDo, Tag.SeriesDescription, 3);
		
		seriesDescriptionCodeList = readSequence(Code.class, rtsDo,
				                             Tag.SeriesDescriptionCodeSequence, 3);
		
		operatorName      = readString(rtsDo, Tag.OperatorsName, 2);
	}
	
	
	@Override
	public void writeToDicom(DicomObject rtsDo)
	{
		writeString(rtsDo, Tag.Modality,          VR.CS, 1, modality);
		writeString(rtsDo, Tag.SeriesInstanceUID, VR.UI, 1, seriesInstanceUid);
		writeInt(rtsDo,    Tag.SeriesNumber,      VR.IS, 2, seriesNumber);
		writeString(rtsDo, Tag.SeriesDate,        VR.DA, 3, seriesDate);
		writeString(rtsDo, Tag.SeriesTime,        VR.TM, 3, seriesTime);
		writeString(rtsDo, Tag.SeriesDescription, VR.LO, 3, seriesDescription);
		
		writeSequence(rtsDo, Tag.SeriesDescriptionCodeSequence, VR.SQ, 3,
				                                        seriesDescriptionCodeList);
		
		writeString(rtsDo, Tag.OperatorsName,     VR.PN, 2, operatorName);
	}
	
}
