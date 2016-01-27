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
* Java class: Contour.java
* First created on Jan 27, 2016 at 10:34:36 AM
* 
* Data structure parallelling relating to the DICOM tag (3006,0040)
* Contour Sequence.
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: Contour.java
* First created on Jan 27, 2016 at 10:34:36 AM
*********************************************************************/

package dataRepresentations;

import static dataRepresentations.RTStruct_old.DUMMY_INT;
import static dataRepresentations.RtStruct.DUMMY_FLOAT;

public class Contour
{
   public class Contour
   {
      public int                       contourNumber    = DUMMY_INT;
      public int[]                     attachedContours = {DUMMY_INT};
      public ContourImage[]            imageList;
      public String                    geometricType;
      public float                     slabThickness = DUMMY_FLOAT;
      public float[]                   offsetVector  = {DUMMY_FLOAT, DUMMY_FLOAT, DUMMY_FLOAT};
      public int                       nContourPoints;
      public float[][]                 contourPoints;
   }
}
