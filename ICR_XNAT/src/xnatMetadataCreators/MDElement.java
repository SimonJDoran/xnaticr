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
* Java class: MDCreator.java
* First created on Jan 13, 2016 at 10:34:53 AM
* 
* Base class for creation of the metadata XML files needed to upload
* data to XNAT.
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public abstract class MDElement
{
	protected DelayedPrettyPrinterXmlWriter dppXML;
	protected String                        currentLabel;
	protected String                        XNATAccessionID;
	protected String                        XNATProject;
	protected String                        XNATRootElement;
		
	/**
    * Create the output stream and writer for the metadata XML and write
    * the root element.
	 * @throws java.io.IOException
	 * @throws exceptions.XMLException
    */
   protected void createMetaDataXML() throws IOException, XMLException
   {          
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      dppXML  = new DelayedPrettyPrinterXmlWriter(
                   new SimpleXmlWriter(
                      new OutputStreamWriter(baos)));

		dppXML.setIndent("   ")
				.writeXmlVersion()
				.writeEntity(XNATRootElement)
				.writeAttribute("xmlns:xnat", "http://nrg.wustl.edu/xnat")
				.writeAttribute("xmlns:xsi",  "http://www.w3.org/2001/XMLSchema-instance")
				.writeAttribute("xmlns:prov", "http://www.nbirn.net/prov")
				.writeAttribute("xmlns:icr",  "http://www.icr.ac.uk/icr")
				.writeAttribute("ID",         XNATAccessionID)
				.writeAttribute("label",      currentLabel)
				.writeAttribute("project",    XNATProject);
   }
	
	public void setCurrentLabel(String s)
	{
		currentLabel = s;
	}
	
	public void setXNATAccessionID(String s)
	{
		XNATAccessionID = s;
	}
	
	public void setXNATProject(String s)
	{
		XNATProject = s;
	}
	
	public void setXNATRootElement(String s)
	{
		XNATRootElement = s;
	}
}


