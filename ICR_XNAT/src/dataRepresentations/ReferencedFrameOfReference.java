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
* Java class: ReferencedFrameOfReference.java
* First created on Jan 21, 2016 at 10:19:41 AM
* 
* Data structure parallelling the icr:referenceFrameOfReferenceData
* element and used in conjunction with
* IcrReferencedFrameOfReferenceDataMDComplexType.
*********************************************************************/

package dataRepresentations;

import dataRepresentations.FrameOfReferenceRelationship;
import java.util.List;
import dataRepresentations.RtReferencedStudy;
import generalUtilities.DicomAssignString;
import java.util.ArrayList;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class ReferencedFrameOfReference extends DicomEntityRepresentation
{
	public String                             frameOfReferenceUid;
	public List<RtReferencedStudy>            rtReferencedStudyList;
	
	// Note that since the first coding of this software, the frame of reference
	// relationship tag has been withdrawn from the DICOM standard and replaced
	// by the Spatial Registration IOD.
	public List<FrameOfReferenceRelationship> frameOfReferenceRelationshipList;
	
	
	public ReferencedFrameOfReference(String uid,
			                            List<FrameOfReferenceRelationship> forrList,
												 List<RtReferencedStudy> rrsList)
	{
		frameOfReferenceUid              = uid;
		frameOfReferenceRelationshipList = forrList;
		rtReferencedStudyList            = rrsList;
	}
	
	
	public ReferencedFrameOfReference(DicomObject rforDo)
	{
		frameOfReferenceUid              = das.assignString(rforDo, Tag.FrameOfReferenceUID, 1);
		frameOfReferenceRelationshipList = new ArrayList<>();
		rtReferencedStudyList            = new ArrayList<>();
		
		
		int frrTag          = Tag.FrameOfReferenceRelationshipSequence;
		DicomElement frrSeq = rforDo.get(frrTag);
		if (frrSeq != null)
		{
			das.warningRetiredTagPresent(frrTag);
			
			for (int i=0; i<frrSeq.countItems(); i++)
			{
				DicomObject                  frrDo = frrSeq.getDicomObject(i);
			   FrameOfReferenceRelationship frr   = new FrameOfReferenceRelationship(frrDo);
				if (frr.das.errors.isEmpty()) frameOfReferenceRelationshipList.add(frr);
				das.errors.addAll(frr.das.errors);
				das.warnings.addAll(frr.das.warnings);
			}
		}
		
		
		int rrsTag          = Tag.RTReferencedStudySequence;
		DicomElement rrsSeq = rforDo.get(rrsTag);
		
		if (rrsSeq == null)
		{
			das.warningOptionalTagNotPresent(rrsTag);
			return;
		}

		for (int i=0; i<rrsSeq.countItems(); i++)
		{
			DicomObject       rrsDo  = rrsSeq.getDicomObject(i);
			RtReferencedStudy rrs    = new RtReferencedStudy(rrsDo) ;
			if (rrs.das.errors.isEmpty()) rtReferencedStudyList.add(rrs);
			das.errors.addAll(rrs.das.errors);
			das.warnings.addAll(rrs.das.warnings);       
		}
	}
}
