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
* Java class: RtRoiObservation.java
* First created on Jan 27, 2016 at 10:41:16 AM
* 
* Data structure parallelling relating to the DICOM tag (3006,0080)
* RT ROI Observations Sequence.
*********************************************************************/

package dataRepresentations;

import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class RtRoiObservation extends DicomEntityRepresentation
{
   public int                           observationNumber;
   public int                           referencedRoiNumber;
	public String                        roiObservationLabel;
	public String                        roiObservationDescription;
	public List<RtRelatedRoi>            rtRelatedRoiList;
	public List<RelatedRtRoiObservation> relatedRtRoiObservationsList;
	public String                        rtRoiInterpretedType;
	public String                        roiInterpreter;
	public String                        materialId;
	public List<RoiPhysicalProperty>     roiPhysicalPropertiesList;
	
	
	public RtRoiObservation(DicomObject rroDo)
	{
		observationNumber         = dav.assignInt(rroDo,    Tag.ObservationNumber,   1);
		referencedRoiNumber       = dav.assignInt(rroDo,    Tag.ReferencedROINumber, 1);
		roiObservationLabel       = dav.assignString(rroDo, Tag.ROIObservationLabel, 3);
		roiObservationDescription = dav.assignString(rroDo, Tag.ROIObservationDescription, 3);
		rtRelatedRoiList          = dav.assignSequence(RtRelatedRoi.class, rroDo, Tag.RTRelatedROISequence, 3);
		
		// Segmented property category code sequence (0062,0003) not implemented.
		// RT ROI identification code sequence (3006,0086) not implemented.
		// Additional RT ROI identification code sequence (3006,00B9) not implemented.
		// Purpose of Reference code sequence (0040,A170) not implemented.
		
		relatedRtRoiObservationsList = dav.assignSequence(RelatedRtRoiObservation.class,
				                               rroDo, Tag.RelatedRTROIObservationsSequence, 3);
		rtRoiInterpretedType      = dav.assignString(rroDo, Tag.RTROIInterpretedType, 2);
		roiInterpreter            = dav.assignString(rroDo, Tag.ROIInterpreter, 2);
		materialId                = dav.assignString(rroDo, Tag.MaterialID, 3);
		roiPhysicalPropertiesList = dav.assignSequence(RoiPhysicalProperty.class,
				                               rroDo, Tag.ROIPhysicalPropertiesSequence, 3);		
	}
}
