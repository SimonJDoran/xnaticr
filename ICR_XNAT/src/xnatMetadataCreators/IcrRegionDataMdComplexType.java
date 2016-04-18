
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
* Java class: IcrRoiDataMDComplexType.java
* First created on Jan 21, 2016 at 3:12:50 PM
* 
* Creation of metadata XML for icr:regionData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
* 
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.dicom.RtRelatedRoi;
import dataRepresentations.dicom.RoiPhysicalProperty;
import dataRepresentations.dicom.RtRoiObservation;
import exceptions.XMLException;
import java.io.IOException;
import java.util.List;

public class IcrRegionDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected List<String>              associatedRegionSetIdList;
	protected String                    originalUid;
	protected String                    originalDataType;
	protected String                    originalLabel;
   protected String                    originalDescription;
	protected String                    originatingApplicationName;
	protected String                    originatingApplicationVersion;
	protected String                    originalContainsMultipleRois;
	protected String                    roiNumberInOriginal;
	protected String                    roiName;
   protected String                    roiDescription;
   protected String                    lineType;
   protected String                    lineColour;
   protected String                    shadingType;
   protected String                    shadingColour;
	protected String                    shadingTransparency;
	protected String                    roiVolume;
	protected String                    roiGenerationAlgorithm;
	protected String                    roiGenerationDescription;
	protected String                    roiGeometricType;
	protected int                       nDicomContours;
	protected String                    derivationCode;
	protected List<RtRoiObservation>    rtRoiObservationList;
	protected List<String>              associatedRegionParStatsIdList;
	
			  
	@Override
	public void insertXml()
			 throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntity("associatedRegionSetIDs");
		for (String s : associatedRegionSetIdList)
				{
			dppXML.delayedWriteEntityWithText("assocRegionSetID", s);
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntity("originatingRegionSource")
			      .delayedWriteAttribute("originalUid",                     originalUid)
			      .delayedWriteAttribute("originalDataType",                originalDataType)
					.delayedWriteAttribute("originalLabel",                   originalLabel)
			      .delayedWriteAttribute("originalDescription",             originalDescription)
               .delayedWriteAttribute("originatingApplicationName",      originatingApplicationName)
		         .delayedWriteAttribute("originatingApplicationVersion",   originatingApplicationVersion)
		         .delayedWriteAttribute("originalContainsMultipleRois",    originalContainsMultipleRois)
			      .delayedWriteAttribute("roiNumberInOriginal",             roiNumberInOriginal)
			   .delayedEndEntity()
			   .delayedWriteEntityWithText("roiName",                       roiName)
				.delayedWriteEntityWithText("roiDescription",                roiDescription)
				.delayedWriteEntityWithText("lineType",                      lineType)
				.delayedWriteEntityWithText("lineColour",                    lineColour)
				.delayedWriteEntityWithText("shadingType",                   shadingType)
				.delayedWriteEntityWithText("shadingColour",                 shadingColour)
				.delayedWriteEntityWithText("shadingTransparency",           shadingTransparency)
            .delayedWriteEntityWithText("roiVolume",                     roiVolume)
				.delayedWriteEntityWithText("roiGenerationAlgorithm",        roiGenerationAlgorithm)
				.delayedWriteEntityWithText("roiGenerationDescription",      roiGenerationDescription)
			   .delayedWriteEntityWithText("roiGeometricType",              roiGeometricType)
				.delayedWriteEntityWithText("nDicomContours",                nDicomContours)
			   .delayedWriteEntityWithText("derivationCode",                derivationCode);
			   	
		dppXML.delayedWriteEntity("rtRoiObservations");
		for (RtRoiObservation rro : rtRoiObservationList)
		{
			(new IcrRtRoiObservationMdComplexType(rro, dppXML)).insertXmlAsElement("rtRoiObservation");
		}
		dppXML.delayedEndEntity();		
		
		dppXML.delayedWriteEntity("associatedRegionParStatsIds");
		for (String s : associatedRegionParStatsIdList)
		{
			dppXML.delayedWriteEntityWithText("assocRegionParStatsId", s);
		}
		dppXML.delayedEndEntity();		
	}
	
	
	@Override
	public String getRootElementName()
	{
		return "Region";
	}
	
	
	public void setAssociatedRegionSetIdList(List<String> ls)
	{
		associatedRegionSetIdList = ls;
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
	
	
	public void setOriginalContainsMultipleRois(String s)
	{
		originalContainsMultipleRois = s;
	}
	
	
	public void setRoiNumberInOriginal(String s)
	{
		roiNumberInOriginal = s;
	}
	
	
	public void setRoiName(String s)
	{
		roiName = s;
	}
	
	
	public void setRoiDescription(String s)
	{
		roiDescription = s;
	}
	
	
	public void setLineColour(String s)
	{
		lineColour = s;
	}
	
	
	public void setLineType(String s)
	{
		lineType = s;
	}
	
	
	public void setShadingColour(String s)
	{
		shadingColour = s;
	}
	
	
	public void setShadingType(String s)
	{
		shadingType = s;
	}
	
	
	public void setShadingTransparency(String s)
	{
		shadingTransparency = s;
	}
	
	
	public void setRoiVolume(String s)
	{
		roiVolume = s;
	}
	
	
	public void setRoiGenerationAlgorithm(String s)
	{
		roiGenerationAlgorithm = s;
	}
	
	
	public void setRoiGenerationDescription(String s)
	{
		roiGenerationDescription = s;
	}
	
	
	public void setRoiGeometricType(String s)
	{
		roiGeometricType = s;
	}
	
	
	public void setNDicomContours(int n)
	{
		nDicomContours = n;
	}
	
	
	public void setDerivationCode(String s)
	{
		derivationCode = s;
	}
	
	
	public void setRtRoiObservationList(List<RtRoiObservation> lrro)
	{
		rtRoiObservationList = lrro;
	}
	
	
	public void setAssociatedRegionParameterStatisticsIdList(List<String> ls)
	{
		associatedRegionParStatsIdList = ls;
	}
}
