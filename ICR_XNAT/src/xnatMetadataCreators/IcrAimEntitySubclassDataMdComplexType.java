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

import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrAimEntitySubclassDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected String       comment;
	protected String       description;
	protected String       typeCode;
	protected String       typeCodeSystemUid;
	protected String       typeCodeSystemName;
	protected String       typeCodeSystemVersion;
	protected String       questionTypeCode;
	protected String       questionTypeCodeSystemUid;
	protected String       questionTypeCodeSystemName;
	protected String       questionTypeCodeSystemVersion;
	protected String       questionIndex;
	protected String       templateUid;
	protected String       subtypeName;
	protected String       isPresent;
	protected String       annotatorConfidence;
	protected String       associatedRoiSetId;
	protected List<String> associatedAimEntitySubclassIdList;
	
	@Override
	public void insertXml(DelayedPrettyPrinterXmlWriter dppXML)
			      throws IOException, XMLException
	{
		super.insertXml(dppXML);
		
		dppXML.delayedWriteEntityWithText("comment",                       comment)
				.delayedWriteEntityWithText("description",                   description)
				.delayedWriteEntityWithText("typeCode",                      typeCode)
				.delayedWriteEntityWithText("typeCodeSystemUid",             typeCodeSystemUid)
				.delayedWriteEntityWithText("typeCodeSystemName",            typeCodeSystemName)
				.delayedWriteEntityWithText("typeCodeSystemVersion",         typeCodeSystemVersion)
				.delayedWriteEntityWithText("questionTypeCode",              questionTypeCode)
				.delayedWriteEntityWithText("questionTypeCodeSystemUid",     questionTypeCodeSystemUid)
				.delayedWriteEntityWithText("questionTypeCodeSystemName",    questionTypeCodeSystemName)
				.delayedWriteEntityWithText("questionTypeCodeSystemVersion", questionTypeCodeSystemVersion)
				.delayedWriteEntityWithText("questionIndex",                 questionIndex)
				.delayedWriteEntityWithText("templateUID",                   templateUid)
				.delayedWriteEntityWithText("subtypeName",                   subtypeName)
				.delayedWriteEntityWithText("isPresent",                     isPresent)
				.delayedWriteEntityWithText("annotatorConfidence",           annotatorConfidence)
				.delayedWriteEntityWithText("associatedRoiSetID",            associatedRoiSetId);
			
		
		dppXML.delayedWriteEntity("asociatedAimEntitySubclassIDs");
		for (String s : associatedAimEntitySubclassIdList)
		{
			dppXML.delayedWriteEntityWithText("asocEntSubID", s);
		}
		dppXML.delayedEndEntity();
	}
	
	
	public void setComment(String s)
	{
		comment = s;
	}
	
	
	public void setDescription(String s)
	{
		description = s;
	}
	
	
	public void setTypeCode(String s)
	{
		typeCode = s;
	}
		
	
	public void setTypeCodeSystemUid(String s)
	{
		typeCodeSystemUid = s;
	}
	
	
	public void setTypeCodeSystemName(String s)
	{
		typeCodeSystemName = s;
	}
	
	
	public void setTypeCodeSystemVersion(String s)
	{
		typeCodeSystemVersion = s;
	}
	
		
	
	public void setQuestionTypeCode(String s)
	{
		questionTypeCode = s;
	}
		
	
	public void setQuestionTypeCodeSystemUid(String s)
	{
		questionTypeCodeSystemUid = s;
	}
	
	
	public void setQuestionTypeCodeSystemName(String s)
	{
		questionTypeCodeSystemName = s;
	}
	
	
	public void setQuestionTypeCodeSystemVersion(String s)
	{
		questionTypeCodeSystemVersion = s;
	}
	
		
	public void setQuestionIndex(String s)
	{
		questionIndex = s;
	}
	
	
	public void setTemplateUid(String s)
	{
		templateUid = s;
	}
	
	
	public void setSubtypeName(String s)
	{
		subtypeName = s;
	}
	
	
	public void setIsPresent(String s)
	{
		isPresent = s;
	}
		
	
	public void setAnnotatorConfidence(String s)
	{
		annotatorConfidence = s;
	}
	
	
	public void setAssociatedRoiSetId(String s)
	{
		associatedRoiSetId = s;
	}
	
	public void setAssociatedAimEntitySubclassIdList(List<String> ls)
	{
		associatedAimEntitySubclassIdList = ls;
	}
}
