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
* Java class: IcrAimAnnotationCollectionDataMDComplexType.java
* First created on Jan 25, 2016 at 12:16:54 PM
* 
* Creation of metadata XML for icr:aimImageAnnotationData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: AimImageAnnotationData.java
* First created on Jan 25, 2016 at 2:09:14 PM
*********************************************************************/


package xnatMetadataCreators;

import exceptions.XMLException;
import java.io.IOException;
import java.util.List;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrAimImageAnnotationDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected String       aimVersion;
   protected String       aimId;
	protected String       comment;
	protected String       aimUserName;
	protected String       aimUserLoginName;
	protected String       aimUserRole;
	protected Integer      aimUserNumberInRole;
   protected String       manufacturerName;
	protected String       manufacturerModelName;
	protected String       deviceSerialNumber;
	protected String       softwareVersion;
   protected String       personName;
	protected String       personId;
	protected String       personBirthDate;
	protected String       personSex;
	protected String       personEthnicGroup;
	protected String       associatedRegionSetId;
	protected List<String> aimEntitySubclassIdList;
	protected Integer      nMarkupEntity;
	protected Integer      nTaskContextEntity;
	protected Integer      nInferenceEntity;
	protected Integer      nAnnotationRoleEntity;
	protected Integer      nCalculationEntity;
	protected Integer      nImagingObservationEntity;
	protected Integer      nImagingPhysicalEntity;
   protected String       typeCode;
	protected String       typeCodeSystemUid;
	protected String       typeCodeSystemName;
	protected String       typeCodeSystemVersion;
   protected String       precedentReferencedUid;
   protected String       templateUid;
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntityWithText("aimVersion",                aimVersion)
            .delayedWriteEntityWithText("aimId",                     aimId)  
            .delayedWriteEntityWithText("comment",                   comment)
				.delayedWriteEntity("aimUser")
				   .delayedWriteAttribute("name",                        aimUserName)
				   .delayedWriteAttribute("loginName",                   aimUserLoginName)
				   .delayedWriteAttribute("roleInClinicalTrial",         aimUserRole)
				   .delayedWriteAttribute("numberWithinRoleOfClinicalTrial", aimUserNumberInRole)
				.delayedEndEntity()
				.delayedWriteEntity("aimEquipment")
				   .delayedWriteAttribute("manufacturerName",            manufacturerName)
				   .delayedWriteAttribute("manufacturerModelName",       manufacturerModelName)
				   .delayedWriteAttribute("deviceSerialNumber",          deviceSerialNumber)
				   .delayedWriteAttribute("softwareVersionString",       softwareVersion)
				.delayedEndEntity()
              .delayedWriteEntity("person")
				   .delayedWriteAttribute("name",                        personName)
				   .delayedWriteAttribute("id",                          personId)
				   .delayedWriteAttribute("birthDate",                   personBirthDate)
				   .delayedWriteAttribute("sex",                         personSex)
				   .delayedWriteAttribute("ethnicGroup",                 personEthnicGroup)
				.delayedEndEntity()
				.delayedWriteEntityWithText("associatedRegionSetId",     associatedRegionSetId);
		
		dppXML.delayedWriteEntity("aimEntitySubclassIds");
		for (String s : aimEntitySubclassIdList)
		{
			dppXML.delayedWriteEntityWithText("aimEntitySubclassId", s);
		}
		dppXML.delayedEndEntity();
		
		dppXML.delayedWriteEntityWithText("nMarkupEntity",             nMarkupEntity)
				.delayedWriteEntityWithText("nTaskContextEntity",        nTaskContextEntity)
				.delayedWriteEntityWithText("nInferenceEntity",          nInferenceEntity)
				.delayedWriteEntityWithText("nAnnotationRoleEntity",     nAnnotationRoleEntity)
				.delayedWriteEntityWithText("nCalculationEntity",        nCalculationEntity)
				.delayedWriteEntityWithText("nImagingObservationEntity", nImagingObservationEntity)
				.delayedWriteEntityWithText("nImagingPhysicalEntity",    nImagingPhysicalEntity)
            .delayedWriteEntityWithText("typeCode",                  typeCode)
            .delayedWriteEntityWithText("typeCodeSystemUid",         typeCodeSystemUid)
            .delayedWriteEntityWithText("typeCodeSystemName",        typeCodeSystemName)
            .delayedWriteEntityWithText("typeCodeSystemVersion",     typeCodeSystemVersion)
            .delayedWriteEntityWithText("precedentReferencedUid",    precedentReferencedUid)
            .delayedWriteEntityWithText("templateUid",               templateUid);
	}
	
	
	@Override
   public String getRootElementName()
   {
      return "AimImageAnnotation";
   }
	
	
	public void setAimVersion(String s)
	{
		aimVersion = s;
	}
	
	
	public void setAimId(String s)
	{
		aimId = s;
	}
	
	
	public void setComment(String s)
	{
		comment = s;
	}
	
	
	public void setAimUserName(String s)
	{
		aimUserName = s;
	}
	
	
	public void setAimUserLoginName(String s)
	{
		aimUserLoginName = s;
	}
	
	
	public void setAimUserRole(String s)
	{
		aimUserRole = s;
	}
	
	
	public void setAimUserNumberInRole(Integer n)
	{
		aimUserNumberInRole = n;
	}
	
	
	public void setPersonName(String s)
	{
		personName = s;
	}
	
	
	public void setPersonId(String s)
	{
		personId = s;
	}
	
	
	public void setPersonBirthDate(String s)
	{
		personBirthDate = s;
	}
	
	
	public void setPersonSex(String s)
	{
		personSex = s;
	}
	
	
	public void setPersonEthnicGroup(String s)
	{
		personEthnicGroup = s;
	}
	
	
	public void setManufacturerName(String s)
	{
		manufacturerName = s;
	}
	
	
	public void setManufacturerModelName(String s)
	{
		manufacturerModelName = s;
	}
	
	
	public void setDeviceSerialNumber(String s)
	{
		deviceSerialNumber = s;
	}
	
	
	public void setSoftwareVersion(String s)
	{
		softwareVersion = s;
	}
	
	
	public void setAssociatedRegionSetId(String s)
	{
		associatedRegionSetId = s;
	}
	
	
	public void setAimEntitySubclassIdList(List<String> ls)
	{
		aimEntitySubclassIdList = ls;
	}
	
	public void setNMarkupEntity(Integer n)
	{
		nMarkupEntity = n;
	}
	
	public void setNTaskContextEntity(Integer n)
	{
		nTaskContextEntity = n;
	}
	
	public void setNInferenceEntity(Integer n)
	{
		nInferenceEntity = n;
	}
	
	public void setNAnnotationRoleEntity(Integer n)
	{
		nAnnotationRoleEntity = n;
	}
	
	public void setNCalculationEntity(Integer n)
	{
		nCalculationEntity = n;
	}
	
	public void setNImagingObservationEntity(Integer n)
	{
		nImagingObservationEntity = n;
	}
	
	public void setNImagingPhysicalEntity(Integer n)
	{
		nImagingPhysicalEntity = n;
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
   
   public void setPrecedentReferencedUid(String s)
   {
      precedentReferencedUid = s;
   }
   
   public void setTemplateUid(String s)
   {
      templateUid = s;
   }
}
