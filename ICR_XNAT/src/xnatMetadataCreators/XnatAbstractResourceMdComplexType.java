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
* Java class: XnatAbstractResourceMDComplexType.java
* First created on Jan 20, 2016 at 9:00:08 AM
* 
* Creation of metadata XML for xnat:abstractResource
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.xnatSchema.AbstractResource;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import dataRepresentations.xnatSchema.MetaField;

public class XnatAbstractResourceMdComplexType extends MdComplexType
{
	protected AbstractResource ar;
	
	
	// Give the user two options for constructing the object.
	public XnatAbstractResourceMdComplexType(){}
	
	public XnatAbstractResourceMdComplexType(AbstractResource ar,
			                                   DelayedPrettyPrinterXmlWriter dppXML)
	{
		this.ar     = ar;
		this.dppXML = dppXML;
	}
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		dppXML.delayedWriteAttribute("label",      ar.label)
				.delayedWriteAttribute("file_count", ar.fileCount)
				.delayedWriteAttribute("file_size",  ar.fileSize)
				.delayedWriteEntityWithText("note",  ar.note)
				.delayedWriteEntity("tags");
		
				for (MetaField tag : ar.tagList)
				{
					dppXML.delayedWriteEntity("tag")
								.delayedWriteAttribute("name", tag.name)
								.delayedWriteText(tag.value)
							.delayedEndEntity();
				}
				
		dppXML.delayedEndEntity();
				  
	}
	
	public void setResource(AbstractResource ar)
	{
		this.ar = ar;
	}
}
