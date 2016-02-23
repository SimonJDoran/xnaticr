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
* Java class: IcrGenericImageAssessmentDataMDCompleType.java
* First created on Jan 20, 2016 at 8:53:59 AM
* 
* Creation of metadata XML for icr:genericImageAssessmentData
* 
* This is virtually identical to xnat:qcAssessmentData, but based on
* imageAssessorData not mrAssessorData.
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.xnatSchema.Scan;
import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import dataRepresentations.xnatSchema.Scan.Slice;

public class IcrGenericImageAssessmentDataMdComplexType extends XnatImageAssessorDataMdComplexType
{
	protected String     type;
	protected String     xnatSubjId;
	protected String     dicomSubjName;
	protected List<Scan> scanList;
	
	public void setType(String s)
	{
		type = s;
	}
	
	
	public void setXnatSubjId(String s)
	{
		xnatSubjId = s;
	}
	
	
	public void setDicomSubjName(String s)
	{
		dicomSubjName = s;
	}
	
	
	public void setScanList(List<Scan> lsc)
	{
		scanList = lsc;
	}
	
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		dppXML.delayedWriteAttribute("type", type)
				.delayedWriteEntityWithText("xnatSubjID",    xnatSubjId)
				.delayedWriteEntityWithText("dicomSubjName", dicomSubjName)
				.delayedWriteEntity("scans");
		
				for (Scan scan : scanList)
				{
					dppXML.delayedWriteEntity("scan")
							.delayedWriteAttribute("id", scan.id)
					      .delayedWriteEntity("sliceQC");
					
					      for (Slice slice : scan.sliceList)
							{
								dppXML.delayedWriteEntity("slice")
										   .delayedWriteAttribute("number", slice.number);
										   if (slice.sliceStatistics != null)
											{
												slice.sliceStatistics.dppXML = dppXML;
												slice.sliceStatistics.insertXmlAsElement("sliceStatistics");
											}
								dppXML.delayedEndEntity();
							}
							
					if (scan.scanStatistics != null)
					{
						scan.scanStatistics.dppXML = dppXML;
						scan.scanStatistics.insertXmlAsElement("scanStatistics");
					}
					
					dppXML.delayedEndEntity();
				}		
	}
}