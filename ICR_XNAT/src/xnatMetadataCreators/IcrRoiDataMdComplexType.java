
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
* Creation of metadata XML for icr:roiData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
* 
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.dicom.RtRelatedRoi;
import dataRepresentations.dicom.RoiPhysicalProperty;
import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRoiDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected List<String>              associatedRoiSetIdList;
	protected String                    originalUid;
	protected String                    originalDataType;
	protected String                    originalLabel;
	protected String                    originatingApplicationName;
	protected String                    originatingApplicationVersion;
	protected String                    originalContainsMultipleRois;
	protected String                    roiNumberInOriginal;
	protected String                    roiDisplayColorInStructureSet;
	protected String                    referencedFrameOfReferenceUid;
	protected String                    roiName;
	protected String                    roiDescription;
	protected String                    roiVolume;
	protected String                    roiGenerationAlgorithm;
	protected String                    roiGenerationDescription;
	protected String                    roiGeometricType;
	protected String                    nDicomContours;
	protected String                    derivationCode;
	protected String                    observationNumber;
	protected String                    roiObservationLabel;
	protected String                    roiObservationDescription;
	protected List<RtRelatedRoi>        rrrList;
	protected String                    rtRoiInterpretedType;
	protected String                    roiInterpreter;
	protected String                    roiMaterialId;
	protected List<RoiPhysicalProperty> rppList;
	protected List<String>              associatedRoiParameterStatisticsIdList;
	
			  
	@Override
	public void insertXml()
			 throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntity("associatedRoiSetIDs");
		for (String s : associatedRoiSetIdList)
				{
			dppXML.delayedWriteEntityWithText("assocRoiSetID", s);
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntity("originatingRoiSource")
			      .delayedWriteAttribute("originalUID",                     originalUid)
			      .delayedWriteAttribute("originalDataType",                originalDataType)
					.delayedWriteAttribute("originalLabel",                   originalLabel)
			      .delayedWriteAttribute("originatingApplicationName",      originatingApplicationName)
		         .delayedWriteAttribute("originatingApplicationVersion",   originatingApplicationVersion)
		         .delayedWriteAttribute("originalContainsMultipleRois",    originalContainsMultipleRois)
			      .delayedWriteAttribute("roiNumberInOriginal",             roiNumberInOriginal)
			   .delayedEndEntity()
			   .delayedWriteEntityWithText("roiDisplayColorInStructureSet", roiDisplayColorInStructureSet)
			   .delayedWriteEntityWithText("referencedFrameOfReferenceUID", referencedFrameOfReferenceUid)
				.delayedWriteEntityWithText("roiName",                       roiName)
				.delayedWriteEntityWithText("roiDescription",                roiDescription)
				.delayedWriteEntityWithText("roiVolume",                     roiVolume)
				.delayedWriteEntityWithText("roiGenerationAlgorithm",        roiGenerationAlgorithm)
				.delayedWriteEntityWithText("roiGenerationDescription",      roiGenerationDescription)
			   .delayedWriteEntityWithText("roiGeometricType",              roiGeometricType)
				.delayedWriteEntityWithText("nDICOMContours",                nDicomContours)
			   .delayedWriteEntityWithText("derivationCode",                derivationCode)
			   .delayedWriteEntityWithText("observationNumber",             observationNumber)
			   .delayedWriteEntityWithText("roiObservationLabel",           roiObservationLabel)
			   .delayedWriteEntityWithText("roiObservationDescription",     roiObservationDescription);
				
		dppXML.delayedWriteEntity("rtRelatedRois");
		      for (RtRelatedRoi rrr : rrrList)
				{
			(new IcrRtRelatedRoiMdComplexType(rrr, dppXML)).insertXmlAsElement("rtRelatedRoi");
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntityWithText("rtRoiInterpretedType",          rtRoiInterpretedType)
			   .delayedWriteEntityWithText("roiInterpreter",                roiInterpreter)
			   .delayedWriteEntityWithText("roiMaterialID",                 roiMaterialId);
		
		dppXML.delayedWriteEntity("roiPhysicalProperties");
		      for (RoiPhysicalProperty rpp : rppList)
				{
					(new IcrRoiPhysicalPropertyMdComplexType(rpp, dppXML)).insertXmlAsElement("roiPhysicalProperty");
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntity("associatedRoiParameterStatisticsIDs");
		for (String s : associatedRoiParameterStatisticsIdList)
				{
			dppXML.delayedWriteEntityWithText("assocRoiParStatsID", s);
				}
		dppXML.delayedEndEntity();		
	}
	
	
	@Override
	public String getRootElementName()
	{
		return "ROI";
	}
	
	
	public void setAssociatedRoiSetIdList(List<String> ls)
	{
		associatedRoiSetIdList = ls;
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
	
	
	public void setOriginalContainsMultipleRois(String s)
	{
		originalContainsMultipleRois = s;
	}
	
	
	public void setRoiNumberInOriginal(String s)
	{
		roiNumberInOriginal = s;
	}
	
	
	public void setRoiDisplayColorInStructureSet(String s)
	{
		roiDisplayColorInStructureSet = s;
	}
	
	
	public void setReferencedFrameOfReferenceUid(String s)
	{
		referencedFrameOfReferenceUid = s;
	}
	
	
	public void setRoiName(String s)
	{
		roiName = s;
	}
	
	
	public void setRoiDescription(String s)
	{
		roiDescription = s;
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
	
	
	public void setNDicomContours(String s)
	{
		nDicomContours = s;
	}
	
	
	public void setDerivationCode(String s)
	{
		derivationCode = s;
	}
	
	
	public void setObservationNumber(String s)
	{
		observationNumber = s;
	}
	
	
	public void setRoiObservationLabel(String s)
	{
		roiObservationLabel = s;
	}
	
	
	public void setRoiObservationDescription(String s)
	{
		roiObservationDescription = s;
	}
	
	
	public void setRtRelatedRoiList(List<RtRelatedRoi> rrrl)
	{
		rrrList = rrrl;
	}
	
	
	public void setRtRoiInterpretedType(String s)
	{
		rtRoiInterpretedType = s;
	}
	
	
	public void setRoiInterpreter(String s)
	{
		roiInterpreter = s;
	}
	
	
	public void setRoiMaterialId(String s)
	{
		roiMaterialId = s;
	}
	
	
	public void setRoiPhysicalPropertyList(List<RoiPhysicalProperty> rppl)
	{
		rppList = rppl;
	}
	
	
	public void setAssociatedRoiParameterStatisticsIdList(List<String> ls)
	{
		associatedRoiParameterStatisticsIdList = ls;
	}
}
