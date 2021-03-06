/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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
** Java class: MDComplexType.java
* First created on Jan 13, 2016 at 12:04:49 PM
* 
* Base class for creation of the metadata XML needed to upload
* data to XNAT. The difference between this base class and the
* MDElement class is that the complexType is not written by itself,
* but always included into the XML already created by MDElement.
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.w3c.dom.Document;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xmlUtilities.XMLUtilities;

public abstract class MdComplexType
{
	protected DelayedPrettyPrinterXmlWriter dppXML;


	public Document createXmlAsRootElement()
		         throws IOException, XMLException
	{
		if (getRootElementName().equals("None"))
		{
			throw new XMLException(XMLException.ROOT_ELEMENT);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		dppXML = new DelayedPrettyPrinterXmlWriter(
                   new SimpleXmlWriter(
                       new OutputStreamWriter(baos)));
		
		dppXML.setIndent("   ")
				.writeXmlVersion()
				.delayedWriteEntity(getRootElementName())
				   .writeAttribute("xmlns:xnat", "http://nrg.wustl.edu/xnat")
				   .writeAttribute("xmlns:xsi",  "http://www.w3.org/2001/XMLSchema-instance")
				   .writeAttribute("xmlns:prov", "http://www.nbirn.net/prov")
				   .writeAttribute("xmlns:icr",  "http://www.icr.ac.uk/icr");
		
		         insertXml();
		
		dppXML.delayedEndEntity();
		dppXML.close();
		
		String s = new String(baos.toByteArray()); // for diagnostics
      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());     
      
		return XMLUtilities.getDOMDocument(bis);
	}
	
	
	public void insertXmlAsElement(String elementName) throws IOException, XMLException
	{
		dppXML.delayedWriteEntity(elementName);
		      insertXml();
		dppXML.delayedEndEntity();
	}
	
	
	abstract void insertXml() throws IOException, XMLException;
	
	
	
	String getRootElementName()
	{
		return "AimEntitySubclass";
	}
}
