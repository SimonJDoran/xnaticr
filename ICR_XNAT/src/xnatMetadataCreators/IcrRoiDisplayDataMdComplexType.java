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
* Java class: IcrRoiDisplayDataMDComplexType.java
* First created on Jan 20, 2016 at 4:06:56 PM
* 
* Creation of metadata XML for icr:roiDisplayData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/


package xnatMetadataCreators;

import dataRepresentations.xnatSchema.RoiDisplay;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRoiDisplayDataMdComplexType extends MdComplexType
{
	protected RoiDisplay rd;
	
	public IcrRoiDisplayDataMdComplexType(RoiDisplay rd)
	{
		this.rd = rd;
	}
	
	public IcrRoiDisplayDataMdComplexType() {}
	
	
	public void setRoiDisplay(RoiDisplay rd)
	{
		this.rd = rd;
	}
	
	
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			      throws IOException, XMLException
	{
		dppXML.delayedWriteEntityWithText("roiID",               rd.roiId)
		      .delayedWriteEntityWithText("lineType",            rd.lineType)
				.delayedWriteEntityWithText("lineColour",          rd.lineColour)
				.delayedWriteEntityWithText("shadingType",         rd.shadingType)
				.delayedWriteEntityWithText("shadingColour",       rd.shadingColour)
				.delayedWriteEntityWithText("shadingTransparency", rd.shadingTransparency);
	}
	
	
	
}
