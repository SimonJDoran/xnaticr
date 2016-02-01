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
* Java class: RtReferencedSeries.java
* First created on Jan 21, 2016 at 00:16:00 AM
* 
* Data structure parallelling the icr:rtReferencedSeriesData element
* and used in conjunction with IcrRtReferencedSeriesDataMDComplexType.
*********************************************************************/
package dataRepresentations;

import dataRepresentations.ContourImage;
import generalUtilities.DicomAssignString;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class RtReferencedSeries extends DicomEntityRepresentation
{
	public String             seriesInstanceUid;
	public List<ContourImage> contourImageList;
	
	public RtReferencedSeries(String series, List<ContourImage> ciList)
	{
		seriesInstanceUid = series;
		contourImageList  = ciList;
	}
	
	public RtReferencedSeries(DicomObject rrseDo)
	{
		seriesInstanceUid = das.assignString(rrseDo, Tag.SeriesInstanceUID, 1);
		contourImageList  = new ArrayList<>();
		
		int ciTag          = Tag.ContourImageSequence;
		DicomElement ciSeq = rrseDo.get(ciTag);
		
		if (ciSeq == null)
		{
			das.errors.add("Required tag " + ciTag + " " + rrseDo.nameOf(ciTag)
					          + " is not present in input.");
			return;
		}
		
		for (int i=0; i<ciSeq.countItems(); i++)
		{
			DicomObject  ciDo = ciSeq.getDicomObject(i);
			ContourImage ci   = new ContourImage(ciDo);
			if (ci.das.errors.isEmpty()) contourImageList.add(ci);
			das.errors.addAll(ci.das.errors);
			das.warnings.addAll(ci.das.warnings);       
		}
	}
	
}
