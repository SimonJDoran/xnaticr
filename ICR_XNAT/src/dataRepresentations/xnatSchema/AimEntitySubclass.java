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

/*********************************************************************
* @author Simon J Doran
* Java class: RoiDisplay.java
* First created on Jan 20, 2016 at 4:20:18 PM
* 
* Data structure parallelling the icr:roiDisplay element and used in
* conjunction with icrRoiDisplayDataMDComplexType.java
*********************************************************************/

package dataRepresentations.xnatSchema;

import java.util.List;

public class AimEntitySubclass extends XnatSchemaElement
{
	public String       comment;
	public String       description;
	public String       typeCode;
	public String       typeCodeSystemUid;
	public String       typeCodeSystemName;
	public String       typeCodeSystemVersion;
	public String       questionTypeCode;
	public String       questionTypeCodeSystemUid;
	public String       questionTypeCodeSystemName;
	public String       questionTypeCodeSystemVersion;
	public String       questionIndex;
	public String       templateUid;
	public String       subtypeName;
	public String       isPresent;
	public String       annotatorConfidence;
   public String       associatedRegionId;
	public String       associatedRegionSetId;
	public List<String> associatedAimEntitySubclassIdList;
	
	public AimEntitySubclass(){}			  
}
