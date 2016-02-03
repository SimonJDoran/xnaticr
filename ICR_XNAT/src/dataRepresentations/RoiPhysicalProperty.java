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
* Java class: RoiPhysicalProperty.java
* First created on Jan 21, 2016 at 5:18:28 PM
* 
* Data structure parallelling the icr:roiPhysicalProperty element and
* used in conjunction with icrRoiPhysicalPropertyMDComplexType.java
*********************************************************************/

package dataRepresentations;

import dataRepresentations.RoiElementalComposition;
import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class RoiPhysicalProperty extends DicomEntityRepresentation
{
	public String                        roiPhysicalProperty; // the property name
	public String                        roiPhysicalPropertyValue;
	public List<RoiElementalComposition> roiElementalCompositionList;
	
	public RoiPhysicalProperty(String name, String value, List<RoiElementalComposition> ecl)
	{
		roiPhysicalProperty         = name;
		roiPhysicalPropertyValue    = value;
		roiElementalCompositionList = ecl;
	}
	
	
	public RoiPhysicalProperty(DicomObject rppDo)
	{
		roiPhysicalProperty         = dav.assignString(rppDo, Tag.ROIPhysicalProperty, 1);
		roiPhysicalPropertyValue    = dav.assignString(rppDo, Tag.ROIPhysicalPropertyValue, 1);
		roiElementalCompositionList = dav.assignSequence(RoiElementalComposition.class,
				                             rppDo, Tag.ROIElementalCompositionSequence, "1C");
	}
}
