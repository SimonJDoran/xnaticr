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
* Java class: RtStructDataUploader.java
* First created on Feb 23, 2016 at 9:36:36 AM
* 
* Object for uploading files to XNAT that conform to DICOM's
* RT-STRUCT format
*********************************************************************/

package xnatUploader;

import exceptions.DataFormatException;
import org.w3c.dom.Document;
import xnatMetadataCreators.IcrRoiSetDataMdComplexType;

public class RtStructDataUploader extends DataUploader
{
	public Document createMetadataXML() throws DataFormatException
	{
		IcrRoiSetDataMdComplexType roiSet = new IcrRoiSetDataMdComplexType();
		
		dppXML = roiSet.createWriter();
		roiSet.createXmlAsRootElement(dppXML);
		dppXML.close();
		dppXML
		
	}
	
	
	public String convertToDateTime(String date, String time) throws DataFormatException
   {
      String month;
      String day;
      String year;
      String hour;
      String minute;
      String second;
      
      try
      {
         month  = date.substring(4, 6);
         day    = date.substring(6, 8);
         year   = date.substring(0, 4);
         hour   = time.substring(0, 2);
         minute = time.substring(2, 4);
         second = time.substring(4, 6);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return new String(year + "-" + month + "-" + day
                        + "T" + hour + ":" + minute + ":" + second);
   }
   
   
   
   @Override
   public String[] getRequiredFields()
   {
      return new String[]{"Label", "Note"};
   }
   
   
   @Override
   public boolean rightMetadataPresent()
   {
      return (!getStringField("Label").equals("")) &&
             (!getStringField("Note").equals(""))  &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanID.equals(""));
   }
   
   
   @Override
   public String getRootElement()
   {
      return "ROISet";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:roiSetData";
   }
}
