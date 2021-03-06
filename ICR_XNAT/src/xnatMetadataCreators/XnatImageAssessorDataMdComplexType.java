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
* Java class: XnatImageAssessorData.java
* First created on Jan 20, 2016 at 8:53:59 AM
* 
* Creation of metadata XML for xnat:imageAssessorData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.Resource;
import exceptions.XMLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XnatImageAssessorDataMdComplexType extends XnatDerivedDataMdComplexType
{
	protected List<Resource> inList;
	protected List<Resource> outList;
	protected String                 imageSessionId;
	protected List<AdditionalField>  paramList; 	
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntity("in");
		for (Resource r : inList)
		{
			(new XnatResourceMdComplexType(r, dppXML)).insertXmlAsElement("file");
		}
		dppXML.delayedEndEntity();
	
		
		dppXML.delayedWriteEntity("out");
		for (Resource r : outList)
		{
			(new XnatResourceMdComplexType(r, dppXML)).insertXmlAsElement("file");
		}
		dppXML.delayedEndEntity();
		
		
		dppXML.delayedWriteEntityWithText("imageSession_ID", imageSessionId);
		
		
		dppXML.delayedWriteEntity("parameters");
		for (AdditionalField af : paramList)
		{
			(new XnatAddFieldMdComplexType(af, dppXML)).insertXmlAsElement("addParam");
		}
		dppXML.delayedEndEntity();
	}
	
	
	public void setInList(List<Resource> lr)
	{
		inList = lr;
	}
	
	
	public void setOutList(List<Resource> lr)
	{
		outList = lr;
	}
	
	
	public void setImageSessionId(String s)
	{
		imageSessionId = s;
	}
	
	
	public void setParamList(List<AdditionalField> pl)
	{
		paramList = pl;
	}
}
