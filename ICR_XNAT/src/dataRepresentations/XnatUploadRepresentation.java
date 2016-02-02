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
* Java class: DataRepresentation.java
* First created on Sep 6, 2012 at 11:22:28 PM
* 
* Provide the common features that will be used by all classes that
* represent data objects to be uploaded to XNAT.
*********************************************************************/

package dataRepresentations;

import java.util.ArrayList;
import java.util.SortedMap;
import xnatDAO.XNATProfile;

public abstract class XnatUploadRepresentation
{
   // Question: Does it make sense to place XNAT-specific information in an
   // object that is describing a concept external to XNAT?
   //
   // Answer: In many cases, we want to be able to render the object, using
   // base images that are stored in the XNAT database (e.g., overlaying them
   // with a region of interest. So we have to be able to extract 
   // these base images from the database.

   public String                       XNATProjectID;
   public String                       XNATExperimentID;
   public String                       XNATRefExperimentID;
   public String                       XNATSubjectID;
   public String                       XNATSubjectLabel;
   public ArrayList<String>            XNATScanID;
   public SortedMap<String, String>    fileSOPMap;
   public SortedMap<String, String>    fileScanMap;
   public XNATProfile                  xnprf;
	
	public XnatUploadRepresentation() {}
}
