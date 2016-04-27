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
* Java class: IcrAimImageAnnotationCollectionDataMDComplexType.java
* First created on Jan 25, 2016 at 12:16:54 PM
* 
* Creation of metadata XML for icr:aimImageAnnotationCollectionData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/

package xnatMetadataCreators;

import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrAimImageAnnotationCollectionDataMdComplexType extends IcrGenericImageAssessmentDataMdComplexType
{
	protected String  aimVersion;
	protected String  aimUserName;
	protected String  aimUserLoginName;
	protected String  aimUserRole;
	protected Integer aimUserNumberInRole;
	protected String  manufacturerName;
	protected String  manufacturerModelName;
	protected String  deviceSerialNumber;
	protected String  softwareVersion;
	protected String  personName;
	protected String  personId;
	protected String  personBirthDate;
	protected String  personSex;
	protected String  personEthnicGroup;
	protected Integer numImageAnnotations;
	protected String  associatedRegionSetId;
	
	@Override
	public void insertXml() throws IOException, XMLException
	{
		super.insertXml();
		
		dppXML.delayedWriteEntityWithText("aimVersion",          aimVersion)
				.delayedWriteEntity("aimUser")
				   .delayedWriteAttribute("name",                  aimUserName)
				   .delayedWriteAttribute("loginName",             aimUserLoginName)
				   .delayedWriteAttribute("roleInClinicalTrial",   aimUserRole)
				   .delayedWriteAttribute("numberWithinRoleOfClinicalTrial", aimUserNumberInRole)
				.delayedEndEntity()
				.delayedWriteEntity("equipment")
				   .delayedWriteAttribute("manufacturerName",      manufacturerName)
				   .delayedWriteAttribute("manufacturerModelName", manufacturerModelName)
				   .delayedWriteAttribute("deviceSerialNumber",    deviceSerialNumber)
				   .delayedWriteAttribute("softwareVersion",       softwareVersion)
				.delayedEndEntity()
				.delayedWriteEntity("person")
				   .delayedWriteAttribute("name",                  personName)
				   .delayedWriteAttribute("id",                    personId)
				   .delayedWriteAttribute("birthDate",             personBirthDate)
				   .delayedWriteAttribute("sex",                   personSex)
				   .delayedWriteAttribute("ethnicGroup",           personEthnicGroup)
				.delayedEndEntity()
				.delayedWriteEntityWithText("numImageAnnotations", numImageAnnotations)
				.delayedWriteEntityWithText("associatedRoiSetId",  associatedRegionSetId);
	}
	
	
	public void setAimVersion(String s)
	{
		aimVersion = s;
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
	
	
	public void setnumImageAnnotations(Integer n)
	{
		numImageAnnotations = n;
	}
	
	
	public void setAssociatedRegionSetId(String s)
	{
		associatedRegionSetId = s;
	}
	
	
}
