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
* Java class: GeneralStudy.java
* First created on Feb 22, 2016 at 10:38:35 AM
* 
* Define a limited representation of the DICOM General Study module,
* including (mostly) mandatory components.
*********************************************************************/

package dataRepresentations.dicom;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class GeneralStudy extends DicomEntityRepresentation
{
	public String studyInstanceUid;
	public String studyDate;
	public String studyTime;
	public String referringPhysicianName;
	
	public GeneralStudy() {}
	
	public GeneralStudy(DicomObject gsDo)
	{
		studyInstanceUid       = readString(gsDo, Tag.StudyInstanceUID, 1);
		studyDate              = readString(gsDo, Tag.StudyDate, 2);
		studyTime              = readString(gsDo, Tag.StudyTime, 2);
		referringPhysicianName = readString(gsDo, Tag.ReferringPhysicianName, 2);
	}
	
	@Override
	public void writeToDicom(DicomObject gsDo)
	{
		writeString(gsDo, Tag.StudyInstanceUID,       VR.UI, 1, studyInstanceUid);
		writeString(gsDo, Tag.StudyDate,              VR.DA, 2, studyDate);
		writeString(gsDo, Tag.StudyTime,              VR.TM, 2, studyTime);
		writeString(gsDo, Tag.ReferringPhysicianName, VR.PN, 2, referringPhysicianName);
	}
}
