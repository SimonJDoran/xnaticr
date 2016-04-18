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
* Object for uploading ROI metadata and generated thumbnails to XNAT
* extracted from source files that conform to DICOM RT-STRUCT format
* Note that there are no primary data files associated with these
* ROIs, as the primary file will have been uploaded as part of an
* ROIset. Because of the use to which this will be put, many of the
* abstract methods in DataUploader do not need implementing.
*********************************************************************/

package xnatUploader;

import dataRepresentations.dicom.RoiContour;
import dataRepresentations.dicom.RtStruct;
import dataRepresentations.dicom.StructureSet;
import dataRepresentations.dicom.StructureSetRoi;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrRegionDataMdComplexType;

public class RegionFromRtStructDataUploader extends DataUploader
{
	// Capture all the data from the class supervising the uploading.
	private RtStructDataUploader rtdsu;
   private int                  roiPos;
	
	public RegionFromRtStructDataUploader(XNATProfile xnprf, RtStructDataUploader rtsdu)
	{
		super(xnprf);
		this.rtdsu = rtdsu;
	}
	
@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrRegionDataMdComplexType region = new IcrRegionDataMdComplexType();
		
		List<String> arsl = new ArrayList<>();
		arsl.add(rtdsu.XNATAccessionID);
		region.setAssociatedRoiSetIdList(arsl);
		
		region.setOriginalUid(rtdsu.rts.sopCommon.sopInstanceUid);
		region.setOriginalDataType("RT-STRUCT");
		
		StructureSet    ss  = rtdsu.rts.structureSet;
		StructureSetRoi ssr = ss.structureSetRoiList.get(roiPos);
		
		region.setOriginalLabel(ss.structureSetLabel);
      region.setOriginalDescription(ss.structureSetDescription);
		region.setOriginatingApplicationName(rtdsu.rts.generalEquipment.modelName);
		
		final String sep = " | ";
		StringBuilder sb = new StringBuilder();
		for (String s : rtdsu.rts.generalEquipment.softwareVersions) sb.append(s).append(sep);
		region.setOriginatingApplicationVersion(sb.toString());
		
      region.setOriginalContainsMultipleRois((rtdsu.nRois > 1) ? "true" : "false");
      region.setRoiNumberInOriginal(Integer.toString(ssr.roiNumber));
		
		region.setRoiName(ssr.roiName);
		region.setRoiDescription(ssr.roiDescription);
		
		// In order to get to the ROI colour, we need to iterate through the contents
		// of roiContour.
      for (RoiContour rc : rtdsu.rts.roiContourList)
		{
			if (rc.referencedRoiNumber == ssr.roiNumber)
			{
				int[] col = rc.roiDisplayColour;
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<col.length; i++)
				region.setLineColour(Integer.toString(rc.roiDisplayColour);
			}
		}
		
	}
	
	public void setRoiPositionInSSRoiSequence(int n)
   {
      roiPos = n; 
   }
	
	@Override
   public String getRootElement()
   {
      return "ROI";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:roiData";
   }
}
