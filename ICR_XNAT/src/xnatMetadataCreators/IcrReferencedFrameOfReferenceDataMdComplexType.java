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
* Java class: IcrReferencedFrameOfReferenceMDComplexType.java
* First created on Jan 21, 2016 at 10:27:34 AM
* 
* Creation of metadata XML for icr:referencedFrameOfReferenceData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.dicom.RtReferencedStudy;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import dataRepresentations.dicom.FrameOfReferenceRelationship;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrReferencedFrameOfReferenceDataMdComplexType extends MdComplexType
{
	protected ReferencedFrameOfReference rfor;
	
	public IcrReferencedFrameOfReferenceDataMdComplexType(ReferencedFrameOfReference    rfor,
			                                                DelayedPrettyPrinterXmlWriter dppXML)
	{
		this.rfor   = rfor;
		this.dppXML = dppXML;
	}
	
	public IcrReferencedFrameOfReferenceDataMdComplexType() {}
	
	
	public void setReferencedFrameOfReference(ReferencedFrameOfReference rfor)
	{
		this.rfor = rfor;
	}
	
	
	@Override
	public void insertXml() throws IOException, XMLException
	{		
		dppXML.delayedWriteEntityWithText("frameOfReferenceUID", rfor.frameOfReferenceUid);
		
		dppXML.delayedWriteEntity("frameOfReferenceRelationships");
		      for (FrameOfReferenceRelationship forr : rfor.frameOfReferenceRelationshipList)
				{
					(new IcrFrameOfReferenceRelationshipDataMdComplexType(forr, dppXML)).insertXmlAsElement("frameOfReferenceRelationship");
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntity("rtReferencedStudies");
		      for (RtReferencedStudy rrs : rfor.rtReferencedStudyList)
				{
					(new IcrRtReferencedStudyDataMdComplexType(rrs, dppXML)).insertXmlAsElement("rtReferencedStudy");
				}
		dppXML.delayedEndEntity();
	}
}
