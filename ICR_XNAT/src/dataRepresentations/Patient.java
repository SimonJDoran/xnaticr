/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
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
package dataRepresentations;

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
      patientName               = dav.assignString(pDo, Tag.PatientName, 1);
      patientId                 = dav.assignString(pDo, Tag.PatientID, 1);
      patientBirthDate          = dav.assignString(pDo, Tag.PatientBirthDate, 2);
      patientSex                = dav.assignString(pDo, Tag.PatientSex, 2);
      clinicalTrialSponsorName  = dav.assignString(pDo, Tag.ClinicalTrialSponsorName, 2);
      clinicalTrialProtocolName = dav.assignString(pDo, Tag.ClinicalTrialProtocolName, 2);      
      clinicalTrialProtocolId   = dav.assignString(pDo, Tag.ClinicalTrialProtocolID, 2);
      clinicalTrialSiteId       = dav.assignString(pDo, Tag.ClinicalTrialSiteID, 2);
      
      // N.B. It is an error if the input DICOM file doesn't contain one or the 
      //      other of the following tags.
      if (pDo.contains(Tag.ClinicalTrialSubjectID))
         clinicalTrialSubjectId    = dav.assignString(pDo, Tag.ClinicalTrialSubjectID, "1C");
      else
         clinicalTrialSubjectReadingId = dav.assignString(pDo, Tag.ClinicalTrialSubjectReadingID, "1C");   
   }
   
   public void writeToDicom(DicomObject pDo)
   {
      pDo.putString(Tag.PatientName,              VR.PN, patientName);
      pDo.putString(Tag.PatientID,                VR.LO, patientId);
      pDo.putString(Tag.PatientBirthDate,         VR.DA, patientBirthDate);
      pDo.putString(Tag.PatientSex,               VR.CS, patientSex);
      pDo.putString(Tag.ClinicalTrialSponsorName, VR.LO, clinicalTrialSponsorName);
      pDo.putString(Tag.ClinicalTrialProtocolName,VR.LO, clinicalTrialProtocolName);
      pDo.putString(Tag.ClinicalTrialProtocolID,  VR.LO, clinicalTrialProtocolId);
      pDo.putString(Tag.ClinicalTrialSiteID,      VR.LO, clinicalTrialSiteId);
      
      if (clinicalTrialSubjectId != null)
         pDo.putString(Tag.ClinicalTrialSubjectID, VR.LO, clinicalTrialSubjectId);
      
      if (clinicalTrialSubjectReadingId != null)
         pDo.putString(Tag.ClinicalTrialSubjectReadingID,VR.LO, clinicalTrialProtocolName);    
   }
}
