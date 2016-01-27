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
* Java class: IcrRoiPhysicalPropertyMDComplexType.java
* First created on Jan 21, 2016 at 5:24:26 PM
* 
* Creation of metadata XML for icr:roiPhysicalProperty
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.RoiPhysicalProperty;
import dataRepresentations.ElementalComposition;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRoiPhysicalPropertyMdComplexType extends MdComplexType
{
	protected RoiPhysicalProperty rpp;
	
	public IcrRoiPhysicalPropertyMdComplexType(RoiPhysicalProperty rpp)
	{
		this.rpp = rpp;
	}
	
	public IcrRoiPhysicalPropertyMdComplexType() {}
	
	
	public void setRoiPhysicalProperty(RoiPhysicalProperty rpp)
	{
		this.rpp = rpp;
	}
	
	
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			      throws IOException, XMLException
	{		
		dppXML.delayedWriteEntityWithText("propertyName",  rpp.propertyName)
				.delayedWriteEntityWithText("propertyValue", rpp.propertyValue);
		
		dppXML.delayedWriteEntity("elementalCompositionList");
		      for (ElementalComposition ec : rpp.elementalCompositionList)
				{
					(new IcrElementalCompositionDataMdComplexType(ec)).insertXmlAsElement("elementalComposition", dppXML);
				}
		dppXML.delayedEndEntity();		  
	}
}
