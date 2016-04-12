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
* Java class: GenericImageAssessmentDataUploader.java
* First created on Jan 15, 2016 at 3:21:03 PM
* 
* Creation of metadata XML for prov:process
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.xnatSchema.Provenance;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Library;

public class ProvProcessStepMdComplexType extends MdComplexType
{
	protected Provenance.ProcessStep ps;
	
	public ProvProcessStepMdComplexType(Provenance.ProcessStep ps, DelayedPrettyPrinterXmlWriter dppXML)
	{
		this.ps     = ps;
		this.dppXML = dppXML;
	}
	
	
	@Override
	public void insertXml() throws IOException, XMLException
	{		
		dppXML.delayedWriteEntity("program")
					.delayedWriteAttribute("version", ps.program.version)
					.delayedWriteAttribute("arguments", ps.program.arguments)
					.delayedWriteText(ps.program.name)
				.delayedEndEntity()
				.delayedWriteEntityWithText("timestamp", ps.timestamp)
				.delayedWriteEntityWithText("cvs",       ps.cvs)
				.delayedWriteEntityWithText("user",      ps.user)
				.delayedWriteEntityWithText("machine",   ps.machine)
				.delayedWriteEntity("platform")
					.delayedWriteAttribute("version", ps.platform.version)
					.delayedWriteText(ps.platform.name)
				.delayedEndEntity()
				.delayedWriteEntity("compiler")
					.delayedWriteAttribute("version", ps.compiler.version)
					.delayedWriteText(ps.compiler.name)
				.delayedEndEntity();

		for (Library lib: ps.libraryList)
		{
			dppXML.delayedWriteEntity("library")
				.delayedWriteAttribute("version", lib.version)
				.delayedWriteText(lib.name)
			.delayedEndEntity();
		}
	}
}
