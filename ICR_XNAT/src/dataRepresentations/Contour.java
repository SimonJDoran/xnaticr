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

import java.util.ArrayList;
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
	public List<float[]>             contourData;


	public Contour(DicomObject cDo)
	{
		contourNumber        = dav.assignInt(cDo,    Tag.ContourNumber,         3);
		attachedContours     = dav.assignInts(cDo,   Tag.AttachedContours,      3);
		contourImageList     = dav.assignSequence(ContourImage.class, cDo, contourNumber, 3);
		contourGeometricType = dav.assignString(cDo, Tag.ContourGeometricType,  1);
		contourSlabThickness = dav.assignFloat(cDo,  Tag.ContourSlabThickness,  3);
		contourOffsetVector  = dav.assignFloats(cDo, Tag.ContourOffsetVector,   3);
		
		nContourPoints       = dav.assignInt(cDo,    Tag.NumberOfContourPoints, 1);
		if (nContourPoints < 0) dav.warningOptionalTagNotPresent(Tag.NumberOfContourPoints);
		
		float[] coords       = dav.assignFloats(cDo, Tag.ContourData,           1);
		int    nCoords = (coords == null) ? -1 : coords.length;
		if (nCoords != 3*nContourPoints) dav.errorTagContentsInvalid(Tag.ContourData);
		contourData = new ArrayList<>();
		for (int i=0; i<nContourPoints; i++)
		{
			float[] a = new float[3];
			a[0] = coords[i*3];
			a[1] = coords[i*3 + 1];
			a[2] = coords[i*3 + 2];
			contourData.add(a);
		}
	}
}
