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
* Java class: Patient.java
* First created on Feb 02, 2016 at 13:37:00 PM
* 
* Define a limited representation of the DICOM Patient module,
* including (mostly) mandatory components.
*********************************************************************/
package dataRepresentations.dicom;

import dataRepresentations.dicom.DicomEntityRepresentation;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;


public class Patient extends DicomEntityRepresentation
{
	public String patientName;
	public String patientId;
	public String patientBirthDate;
	public String patientSex;
   public String clinicalTrialSponsorName;
   public String clinicalTrialProtocolName;
   public String clinicalTrialProtocolId;
   public String clinicalTrialSiteId;
   public String clinicalTrialSubjectId;
   public String clinicalTrialSubjectReadingId;
   
   public Patient() {}
   
   public Patient(DicomObject pDo)
   {
      patientName               = readString(pDo, Tag.PatientName, 1);
      patientId                 = readString(pDo, Tag.PatientID, 1);
      patientBirthDate          = readString(pDo, Tag.PatientBirthDate, 2);
      patientSex                = readString(pDo, Tag.PatientSex, 2);
      clinicalTrialSponsorName  = readString(pDo, Tag.ClinicalTrialSponsorName, 2);
      clinicalTrialProtocolName = readString(pDo, Tag.ClinicalTrialProtocolName, 2);      
      clinicalTrialProtocolId   = readString(pDo, Tag.ClinicalTrialProtocolID, 2);
      clinicalTrialSiteId       = readString(pDo, Tag.ClinicalTrialSiteID, 2);
      
      // N.B. It is an error if the input DICOM file doesn't contain one or the 
      //      other of the following tags.
      if (pDo.contains(Tag.ClinicalTrialSubjectID))
         clinicalTrialSubjectId    = readString(pDo, Tag.ClinicalTrialSubjectID, "1C");
      else
         clinicalTrialSubjectReadingId = readString(pDo, Tag.ClinicalTrialSubjectReadingID, "1C");   
   }
   
   public void writeToDicom(DicomObject pDo)
   {
      writeString(pDo, Tag.PatientName,              VR.PN, 1, patientName);
      writeString(pDo, Tag.PatientID,                VR.LO, 1, patientId);
      writeString(pDo, Tag.PatientBirthDate,         VR.DA, 2, patientBirthDate);
      writeString(pDo, Tag.PatientSex,               VR.CS, 2, patientSex);
      writeString(pDo, Tag.ClinicalTrialSponsorName, VR.LO, 2, clinicalTrialSponsorName);
      writeString(pDo, Tag.ClinicalTrialProtocolName,VR.LO, 2, clinicalTrialProtocolName);
      writeString(pDo, Tag.ClinicalTrialProtocolID,  VR.LO, 2, clinicalTrialProtocolId);
      writeString(pDo, Tag.ClinicalTrialSiteID,      VR.LO, 2, clinicalTrialSiteId);
      writeString(pDo, Tag.ClinicalTrialSubjectID,   VR.LO, "1C", clinicalTrialSubjectId);
      writeString(pDo, Tag.ClinicalTrialSubjectReadingID, VR.LO, "1C", clinicalTrialProtocolName);    
   }
}
