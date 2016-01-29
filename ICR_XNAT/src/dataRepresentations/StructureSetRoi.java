/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/********************************************************************
* @author Simon J Doran
* Java class: StructureSetRoi.java
* First created on Jan 27, 2016 at 10:16:34 AM
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RtStruct.DUMMY_FLOAT;

public class StructureSetRoi extends DicomEntityRepresentation
{
      public int                       roiNumber;
      public int                       correspondingROIContour;
      public int                       correspondingROIObservation;
      public String                    referencedFrameOfReferenceUID;
      public String                    roiName;
      public String                    roiDescription;
      public float                     roiVolume = DUMMY_FLOAT;
      public String                    roiGenerationAlgorithm;
      public String                    roiGenerationDescription;
      public String                    derivationCode;
      public String                    roiXNATID;
}
