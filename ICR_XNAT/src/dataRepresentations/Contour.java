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
* Java class: Contour.java
* First created on Jan 27, 2016 at 10:34:36 AM
* 
* Data structure parallelling relating to the DICOM tag (3006,0040)
* Contour Sequence.
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RTStruct_old.DUMMY_INT;
import static dataRepresentations.RtStruct.DUMMY_FLOAT;
import java.util.List;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class Contour extends DicomEntityRepresentation
{
   
	public int                       contourNumber;
	public int[]                     attachedContours;
	public List<ContourImage>        contourImageList;
	public String                    contourGeometricType;
	public float                     contourSlabThickness;
	public float[]                   contourOffsetVector;
	public int                       nContourPoints;
	public float[][]                 contourData;


	public Contour(DicomObject cDo)
	{
		int cnTag        = Tag.ContourNumber;
		contourNumber    = cDo.getInt(cnTag, DUMMY_INT);
		if (contourNumber == DUMMY_INT) das.warningOptionalTagNotPresent(cnTag);

		
		int acTag        = Tag.AttachedContours;
		attachedContours = cDo.getInts(acTag);
		if (attachedContours == null) das.warningOptionalTagNotPresent(acTag);

		
		int ciTag          = Tag.ContourImageSequence;
		DicomElement ciSeq = cDo.get(ciTag);

		if (ciSeq == null)
		{
			das.warningOptionalTagNotPresent(ciTag);
		}
		else
		{
			for (int i=0; i<ciSeq.countItems(); i++)
			{
				DicomObject  ciDo = ciSeq.getDicomObject(i);
				ContourImage ci   = new ContourImage(ciDo);
				if (ci.das.errors.isEmpty()) contourImageList.add(ci);
				das.errors.addAll(ci.das.errors);
				das.warnings.addAll(ci.das.warnings);       
			}
		}

		
		contourGeometricType = das.assignString(cDo, Tag.ContourGeometricType, 1);
		
		
		int cstTag = Tag.ContourSlabThickness;
		contourSlabThickness = cDo.getFloat(cstTag, DUMMY_FLOAT);
		if (contourSlabThickness == DUMMY_FLOAT) das.warningOptionalTagNotPresent(cstTag);
		
		
		int covTag = Tag.ContourOffsetVector;
		contourOffsetVector = cDo.getFloats(covTag);
		if (contourOffsetVector == null) das.warningOptionalTagNotPresent(covTag);
		if (contourOffsetVector.length != 3) das.errorTagContentsInvalid(covTag);
		
		
		int ncpTag     = Tag.NumberOfContourPoints;
		nContourPoints = cDo.getInt(ncpTag, DUMMY_INT);
		if ((nContourPoints == DUMMY_INT) || (nContourPoints < 0))
			                              das.warningOptionalTagNotPresent(ncpTag);
		
		int     cdTag  = Tag.ContourData;
		float[] coords = cDo.getFloats(cdTag);
		int    nCoords = (coords == null) ? -1 : coords.length;
		if (nCoords != 3*nContourPoints) das.errorTagContentsInvalid(cdTag);
	}
}
