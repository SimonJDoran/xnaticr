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
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/

package xnatMetadataCreators;

import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrRoiDataMDComplexType extends IcrGenericImageAssessmentDataMDComplexType
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
	protected List<String>              associatedRoiParameterStatisticsList;
	
			  
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			 throws IOException, XMLException
	{
		super.insertXml(dppXML);
		
		dppXML.delayedWriteEntity("associatedRoiSetIDs");
			   for (String id : associatedRoiSetIdList)
				{
					dppXML.delayedWriteEntityWithText("assocRoiSetID", id);
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
			   .delayedWriteEntityWithText("nDicomContours",                nDicomContours)
			   .delayedWriteEntityWithText("derivationCode",                derivationCode)
			   .delayedWriteEntityWithText("observationNumber",             observationNumber)
			   .delayedWriteEntityWithText("roiObservationLabel",           roiObservationLabel)
			   .delayedWriteEntityWithText("roiObservationDescription",     roiObservationDescription);
				
		dppXML.delayedWriteEntity("rtRelatedRois");
		      for (RtRelatedRoi rrr : rrrList)
				{
					(new IcrRtRelatedRoiMDComplexType(rrr)).insertXmlAsElement("rtReleatedRoi", dppXML);
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntityWithText("rtRoiInterpretedType",          rtRoiInterpretedType)
			   .delayedWriteEntityWithText("roiInterpreter",                roiInterpreter)
			   .delayedWriteEntityWithText("roiMaterialID",                 roiMaterialId);
		
		dppXML.delayedWriteEntity("roiPhysicalProperties");
		      for (RoiPhysicalProperty rpp : rppList)
				{
					(new IcrRoiPhysicalPropertyMDComplexType(rpp)).insertXmlAsElement("roiPhysicalProperty", dppXML);
				}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntity("associatedRoiParameterStatisticsIDs");
			   for (String id : associatedRoiParameterStatisticsList)
				{
					dppXML.delayedWriteEntityWithText("assocRoiParStatsID", id);
				}
		dppXML.delayedEndEntity();		
	}
}
