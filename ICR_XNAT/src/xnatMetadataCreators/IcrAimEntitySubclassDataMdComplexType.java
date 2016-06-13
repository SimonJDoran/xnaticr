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
* Java class: IcrAimEntitySubclassDataMdComplexType.java
* First created on Jan 25, 2016 at 2:33:55 PM
* 
* Creation of metadata XML for icr:aimEntitySubclassData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import dataRepresentations.xnatSchema.AimEntitySubclass;
import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrAimEntitySubclassDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected AimEntitySubclass es;
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntityWithText("subclassType",                  es.subclassType)
				.delayedWriteEntityWithText("comment",                       es.comment)
				.delayedWriteEntityWithText("description",                   es.description)
				.delayedWriteEntityWithText("typeCode",                      es.typeCode)
				.delayedWriteEntityWithText("typeCodeSystemUid",             es.typeCodeSystemUid)
				.delayedWriteEntityWithText("typeCodeSystemName",            es.typeCodeSystemName)
				.delayedWriteEntityWithText("typeCodeSystemVersion",         es.typeCodeSystemVersion)
				.delayedWriteEntityWithText("questionTypeCode",              es.questionTypeCode)
				.delayedWriteEntityWithText("questionTypeCodeSystemUid",     es.questionTypeCodeSystemUid)
				.delayedWriteEntityWithText("questionTypeCodeSystemName",    es.questionTypeCodeSystemName)
				.delayedWriteEntityWithText("questionTypeCodeSystemVersion", es.questionTypeCodeSystemVersion)
				.delayedWriteEntityWithText("questionIndex",                 es.questionIndex)
				.delayedWriteEntityWithText("templateUid",                   es.templateUid)
				.delayedWriteEntityWithText("shapeIdentifier",               es.shapeIdentifier)
				.delayedWriteEntityWithText("lineColour",                    es.lineColour)
				.delayedWriteEntityWithText("lineOpacity",                   es.lineOpacity)
				.delayedWriteEntityWithText("lineStyle",                     es.lineStyle)
				.delayedWriteEntityWithText("lineThickness",                 es.lineThickness)
				.delayedWriteEntityWithText("isPresent",                     es.isPresent)
				.delayedWriteEntityWithText("annotatorConfidence",           es.annotatorConfidence)
            .delayedWriteEntityWithText("associatedRegionId",            es.associatedRegionId)
				.delayedWriteEntityWithText("associatedRegionSetId",         es.associatedRegionSetId);
			
		
		dppXML.delayedWriteEntity("associatedAimEntitySubclassIds");
		for (String s : es.associatedAimEntitySubclassIdList)
		{
			dppXML.delayedWriteEntityWithText("assocEntSubId", s);
		}
		dppXML.delayedEndEntity();
	}
	
	
	public void setEntitySubclass(AimEntitySubclass es)
	{
		this.es = es;
	}
   
   
   @Override
   public String getRootElementName()
   {
      return "AimEntitySubclass";
   }
	
	

}
