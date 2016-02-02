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
* Java class: RoiContour.java
* First created on Jan 27, 2016 at 10:29:38 AM
* 
* Data structure parallelling the DICOM tag (3006,0039)
* ROI Contour Sequence.
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RtStruct.DUMMY_INT;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class RoiContour extends DicomEntityRepresentation
{
   public int           referencedRoiNumber;
   public int[]         roiDisplayColour;
   public List<Contour> contourList;
		
	public RoiContour(BasicDicomObject rcDo)
	{
		referencedRoiNumber = rcDo.getInt(Tag.ReferencedROINumber);
		
		int rdcTag          = Tag.ROIDisplayColor;
		roiDisplayColour    = rcDo.getInts(rdcTag, new int[3]);
		
		if (roiDisplayColour == null)
		{
			das.warningOptionalTagNotPresent(rdcTag);
		}
		
		contourList = new ArrayList<>();
		int cTag          = Tag.ContourSequence;
		DicomElement cSeq = rcDo.get(cTag);
		
		if (cSeq == null)
		{
			das.warningOptionalTagNotPresent(cTag);
			return;
		}
		
		for (int i=0; i<cSeq.countItems(); i++)
		{
			DicomObject cDo = cSeq.getDicomObject(i);
			Contour     c   = new Contour(cDo);
			if (c.das.errors.isEmpty()) contourList.add(c);
			das.errors.addAll(c.das.errors);
			das.warnings.addAll(c.das.warnings);       
		}
		
	}
}
