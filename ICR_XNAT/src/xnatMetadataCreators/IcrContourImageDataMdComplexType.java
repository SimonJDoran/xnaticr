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
* Java class: IcrContourImageData.java
* First created on Jan 20, 2016 at 11:32:00 PM
* 
* Creation of metadata XML for icr:contourImageData
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring. In addition
* note that, at present, only a subset of xnat:experimentData is
* implemented.
*********************************************************************/
package xnatMetadataCreators;

import dataRepresentations.dicom.ContourImage;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class IcrContourImageDataMdComplexType extends MdComplexType
{
	protected ContourImage ci;
	
	public IcrContourImageDataMdComplexType(ContourImage ci,
			                                  DelayedPrettyPrinterXmlWriter dppXML)
	{
		this.ci     = ci;
		this.dppXML = dppXML;
	}
	
	public IcrContourImageDataMdComplexType() {}
	
	
	public void setContourImage(ContourImage ci)
	{
		this.ci = ci;
	}
	
	
	@Override
	public void insertXml()
			 throws IOException, XMLException
	{
      //TODO We should possibly consider listing out all the frame numbers
      // as separate items. For the moment, just string them all together.
      String frameNumbers = null;
      StringBuilder sb = new StringBuilder();
      for (Integer fn : ci.referencedFrameNumberList)
         sb.append(fn.toString()).append(" ");
      if (sb.length() != 0) frameNumbers = sb.toString();
      
		dppXML.delayedWriteEntityWithText("referencedSOPInstanceUID", ci.referencedSopInstanceUid)
			   .delayedWriteEntityWithText("referencedSOPClassUID",    ci.referencedSopClassUid)
			   .delayedWriteEntityWithText("referencedFrameNumber",    frameNumbers);
	}
	
}
