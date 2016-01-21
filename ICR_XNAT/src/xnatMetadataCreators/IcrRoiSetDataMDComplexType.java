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
* Java class: IcrRoiSetDataMDComplexType.java
* First created on Jan 13, 2016 at 4:49:53 PM
* 
* Creation of metadata XML for icr:roiSetData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/


package xnatMetadataCreators;

import exceptions.XMLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRoiSetDataMDComplexType extends IcrGenericImageAssessmentDataMDComplexType
{
	protected String                           originalUid;
	protected String                           originalDataType;
	protected String                           originalLabel;
	protected String                           originatingApplicationName;
	protected String                           originatingApplicationVersion;
	protected Integer                          nRois;
	protected List<RoiDisplay>                 roiDisplayList;
	protected String                           structureSetLabel;
	protected String                           structureSetName;
	protected String                           structureSetDescription;
	protected String                           instanceNumber;
	protected String                           structureSetDate;
	protected String                           structureSetTime;
	protected List<ReferencedFrameOfReference> rforList;
			  
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			 throws IOException, XMLException
	{
		super.insertXml(dppXML);
		
		dppXML.delayedWriteEntity("originatingRoiSetSource")
				   .delayedWriteAttribute("originalUID",                   originalUid)
				   .delayedWriteAttribute("originalDataType",              originalDataType)
				   .delayedWriteAttribute("originalLabel",                 originalLabel)
				   .delayedWriteAttribute("originatingApplicationName",    originatingApplicationName)
				   .delayedWriteAttribute("originatingApplicationVersion", originatingApplicationVersion)
				.delayedEndEntity()
				.delayedWriteEntityWithText("nRois", nRois);
		
		dppXML.delayedWriteEntity("roiDisplays");
		         for (RoiDisplay rd : roiDisplayList)
					{
						(new IcrRoiDisplayDataMDComplexType(rd)).insertXmlAsElement("roiDisplay", dppXML);
					}
	   dppXML.delayedEndEntity();
			
		dppXML.delayedWriteEntityWithText("structureSetLabel",       structureSetLabel)
				.delayedWriteEntityWithText("structureSetName",        structureSetName)
				.delayedWriteEntityWithText("structureSetDescription", structureSetDescription)
				.delayedWriteEntityWithText("instanceNumber",          instanceNumber)
				.delayedWriteEntityWithText("structureSetDate",        structureSetDate)
				.delayedWriteEntityWithText("structureSetTime",        structureSetTime);
		
		dppXML.delayedWriteEntity("referencedFramesOfReference");
		      for (ReferencedFrameOfReference rfor : rforList)
				{
					(new IcrReferencedFrameOfReferenceDataMDComplexType(rfor)).insertXmlAsElement("referencedFrameOfReference", dppXML);
				}		  
	}
	
	
	public void setOriginalUid(String s)
	{
		originalUid = s;
	}
	
	
	public void setOriginalDataType(String s)
	{
		originalDataType = s;
	}
		
		
	public void setOriginalLabel(String s)
	{
		originalLabel = s;
	}
	
	
	public void setOriginatingApplicationName(String s)
	{
		originatingApplicationName = s;
	}
			
			
	public void setOriginatingApplicationVersion(String s)
	{
		originatingApplicationVersion = s;
	}
	
	
	public void setNRois(Integer n)
	{
		nRois = n;
	}
	
	
	public void setRoiDisplayList(List<RoiDisplay> rdList)
	{
		roiDisplayList = rdList;
	}
	
	
	public void setStructureSetLabel(String s)
	{
		structureSetLabel = s;
	}
	
	
	public void setStructureSetName(String s)
	{
		structureSetName = s;
	}
	
	
	public void setStructureSetDescription(String s)
	{
		structureSetDescription = s;
	}
	
	
	public void setStructureInstanceNumber(String s)
	{
		instanceNumber = s;
	}
	
	
	public void setStructureSetDate(String s)
	{
		structureSetDate = s;
	}
	
	
	public void seSttructureSetTime(String s)
	{
		structureSetTime = s;
	}
	
	
	public void setReferencedFrameOfReferenceList(List<ReferencedFrameOfReference> rl)
	{
		this.rforList = rl;
	}
	
	
	
	
}
