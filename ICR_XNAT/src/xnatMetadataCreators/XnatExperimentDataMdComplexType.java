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
* Java class: ExperimentMDCreator.java
* First created on Jan 13, 2016 at 11:07:01 AM
* 
* Creation of metadata XML for xnat:experimentData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/


package xnatMetadataCreators;

import dataRepresentations.xnatSchema.InvestigatorList;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class XnatExperimentDataMdComplexType extends MdComplexType
{
	protected String id;
	protected String project;
	protected String visit;
	protected String visitId;
	protected String version;
	protected String original;
	protected String protocol;
	protected String label;
	protected String date;
	protected String time;
	protected String note;
	protected InvestigatorList.Investigator investigator;
	protected String investigatorID;
	
	
	@Override
	public void insertXml()
			      throws IOException, XMLException
	{
		dppXML.delayedWriteAttribute("id",       id)
				.delayedWriteAttribute("project",  project)
				.delayedWriteAttribute("visit_id", visitId)
				.delayedWriteAttribute("visit",    visit)
			   .delayedWriteAttribute("version",  version)
				.delayedWriteAttribute("label",    label)
				  
				.delayedWriteEntity("date")
					.delayedWriteText(date)
				.delayedEndEntity()

				.delayedWriteEntity("time")
					.delayedWriteText(time)
				.delayedEndEntity()

				.delayedWriteEntity("note")
					.delayedWriteText(note)
				.delayedEndEntity();
		
				XnatInvestigatorDataMdComplexType xid = new XnatInvestigatorDataMdComplexType(dppXML);
				xid.setInvestigator(investigator);
				xid.insertXmlAsElement("investigator");
	}
	
	
	public void setDate(String s)
	{
		date = s;
	}
	
	
	public void setLabel(String s)
	{
		label = s;
	}
	
	
	public void setId(String s)
	{
		id = s;
	}
	
	
	public void setNote(String s)
	{
		note = s;
	}
	
	
	public void setProject(String s)
	{
		project = s;
	}
	
	
	public void setTime(String s)
	{
		time = s;
	}
	
	
	public void setVersion(String s)
	{
		version = s;
	}
	
	
	public void setVisit(String s)
	{
		visit = s;
	}
	
	
	public void setVisitID(String s)
	{
		visitId = s;
	}
	
	
	public void setInvestigator(InvestigatorList.Investigator inv, String invID)
	{
		investigator   = inv;
		investigatorID = invID;
	}
}
