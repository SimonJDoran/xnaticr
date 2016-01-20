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
* Java class: XnatAddFieldMDComplexType.java
* First created on Jan 20, 2016 at 12:00:11 PM
* 
* Creation of metadata XML for xnat:addField
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class XnatAddFieldMDComplexType extends MDComplexType
{
	protected AdditionalField af;
	
	// Since there are only two variables to set, allow a variety of methods
	// fof initialisation for flexibility.
	public XnatAddFieldMDComplexType(AdditionalField af)
	{
		this.af = af;
	}
	
	public XnatAddFieldMDComplexType(String name, String value)
	{
		af = new AdditionalField(name, value);
	}
	
	public XnatAddFieldMDComplexType()
	{
		af = new AdditionalField();
	}	
	
	public void setName(String s)
	{
		af.name = s;
	}
	
	public void setValue(String s)
	{
		af.value = s;
	}
	
	
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			      throws IOException, XMLException
	{		
		dppXML.delayedWriteAttribute("name", af.name)
				.delayedWriteText(af.value);
	}
			   
}
