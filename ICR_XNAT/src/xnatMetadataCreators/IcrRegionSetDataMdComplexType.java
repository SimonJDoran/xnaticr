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
* Creation of metadata XML for icr:regionSetData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/


package xnatMetadataCreators;

import dataRepresentations.xnatSchema.RoiDisplay;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import exceptions.XMLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRegionSetDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected String                           originalUid;
	protected String                           originalDataType;
	protected String                           originalLabel;
   protected String                           originalDescription;
	protected String                           originatingApplicationName;
	protected String                           originatingApplicationVersion;
	protected Integer                          nRegions;
	protected List<String>                     regionIdList;
	protected String                           structureSetName;
	protected String                           instanceNumber;
	protected List<ReferencedFrameOfReference> rforList;
			  
	@Override
	public void insertXml() throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntity("originatingRegionSetSource")
				   .delayedWriteAttribute("originalUid",                   originalUid)
				   .delayedWriteAttribute("originalDataType",              originalDataType)
				   .delayedWriteAttribute("originalLabel",                 originalLabel)
               .delayedWriteAttribute("originalDescription",           originalDescription)
				   .delayedWriteAttribute("originatingApplicationName",    originatingApplicationName)
				   .delayedWriteAttribute("originatingApplicationVersion", originatingApplicationVersion)
				.delayedEndEntity()
				.delayedWriteEntityWithText("nRegions", Integer.toString(nRegions));

		dppXML.delayedWriteEntity("regionIds");
		         for (String rId : regionIdList)
					{
                  dppXML.delayedWriteEntityWithText("regionId", rId);
					}
	   dppXML.delayedEndEntity();
			
		dppXML.delayedWriteEntityWithText("structureSetName",            structureSetName)
				.delayedWriteEntityWithText("structureSetInstanceNumber",  instanceNumber);
		
		dppXML.delayedWriteEntity("referencedFramesOfReference");
		      for (ReferencedFrameOfReference rfor : rforList)
				{
					(new IcrReferencedFrameOfReferenceDataMdComplexType(rfor, dppXML)).insertXmlAsElement("referencedFrameOfReference");
				}
		dppXML.delayedEndEntity();
	}
	
	@Override
	public String getRootElementName()
	{
		return "RegionSet";
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
	
	
	public void setOriginalDescription(String s)
	{
		originalDescription = s;
	}
	
	
   public void setOriginatingApplicationName(String s)
	{
		originatingApplicationName = s;
	}
			
			
	public void setOriginatingApplicationVersion(String s)
	{
		originatingApplicationVersion = s;
	}
	
	
	public void setNRegions(Integer n)
	{
		nRegions = n;
	}
	
	
	public void setRegionIdList(List<String> ril)
	{
		regionIdList = ril;
	}
	
	
public void setStructureSetName(String s)
	{
		structureSetName = s;
	}
	
	
	public void setStructureInstanceNumber(String s)
	{
		instanceNumber = s;
	}
	
	
	public void setReferencedFrameOfReferenceList(List<ReferencedFrameOfReference> rl)
	{
		this.rforList = rl;
	}
		
}
