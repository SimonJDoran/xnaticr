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

import dataRepresentations.Provenance;
import exceptions.XMLException;
import java.io.IOException;
import java.util.ArrayList;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import dataRepresentations.Provenance.Library;
import dataRepresentations.Provenance.ProvenanceEntry;

public class ProvProcessMdComplexType extends MdComplexType
{
	protected Provenance prov;
	
	public ProvProcessMdComplexType(Provenance p)
	{
		prov = p;
	}
	
	public void setProvenance(Provenance p)
	{
		prov = p;
	}
	
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			 throws IOException, XMLException
	{		
		for (Provenance.ProvenanceEntry pe: prov.entries)
		{
			dppXML.writeEntity("processStep")
				.delayedWriteEntity("program")
					.delayedWriteAttribute("version", pe.program.version)
					.delayedWriteAttribute("arguments", pe.program.arguments)
					.delayedWriteText(pe.platform.name)
				.delayedEndEntity()
				.delayedWriteEntity("timestamp")
					.delayedWriteText(pe.timestamp)
				.delayedEndEntity()
				.delayedWriteEntity("cvs")
					.delayedWriteText(pe.cvs)
				.delayedEndEntity()
				.delayedWriteEntity("user")
					.delayedWriteText(pe.user)
				.delayedEndEntity()
				.delayedWriteEntity("machine")
					.delayedWriteText(pe.machine)
				.delayedEndEntity()
				.delayedWriteEntity("platform")
					.delayedWriteAttribute("version", pe.platform.version)
					.delayedWriteText(pe.platform.name)
				.delayedEndEntity()
				.delayedWriteEntity("compiler")
					.delayedWriteAttribute("version", pe.compiler.version)
					.delayedWriteText(pe.compiler.name)
				.delayedEndEntity();

				for (Library lib: pe.libraryList)
				{
					dppXML.delayedWriteEntity("library")
						.delayedWriteAttribute("version", lib.version)
						.delayedWriteText(lib.name)
					.delayedEndEntity();
				}
			dppXML.endEntity();
		}
	}
}
