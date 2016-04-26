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
* THIS SOFTWARE IS PROVIDED BY XXHE COPYRIGHT HOLDERS AND CONTRIBUTORS
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
* Java class: AimImageAnnotationDataUploader.java
* First created on Apr 26, 2016 at 10:04:04 AM
* 
* Object for uploading Annotation and Image Markup (AIM) data
* to XNAT. Note that a single AIM instance file contains an
* annotation collection, which may be made up of a number of 
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: AimImageAnnotationDataUploader.java
* First created on Apr 26, 2016 at 10:04:04 AM
*********************************************************************/

package xnatUploader;

import etherj.aim.ImageAnnotation;
import exceptions.DataFormatException;
import exceptions.ImageUtilitiesException;
import exceptions.XMLException;
import exceptions.XNATException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.imageio.ImageIO;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimImageAnnotationDataMdComplexType;
import xnatRestToolkit.XnatResource;

public class AimImageAnnotationDataUploader extends DataUploader
{
	protected ImageAnnotation ia;

	
	public AimImageAnnotationDataUploader(XNATProfile xnprf)
	{
		super(xnprf);  
	}
	
	@Override
	protected Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrAimImageAnnotationDataMdComplexType annotation
				                    = new IcrAimImageAnnotationDataMdComplexType();
		
      
      // Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = annotation.createXmlAsRootElement();
		}
		catch (IOException | XMLException ex)
		{
			// This really shouldn't happen, but the mechanism is there to handle
			// it if it does.
			errorOccurred = true;
			errorMessage  = ex.getMessage();
		}
		
		return metaDoc;
		
	}
	
	
	@Override
   public String getRootElement()
   {
      return "AimImageAnnotation";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:aimImageAnnotationData";
   }
   

   @Override
   protected void createPrimaryResource()
   {
      // There is no primary resource associated with an AIM ImageAnnotation
      // entity. The AimImageAnnotationCollectionDataUploader does the upload
      // of the AIM instance XML file. 
   }
	

   @Override
   protected void createAuxiliaryResources()
   {
      // There are no auxiliary resources associated with an AIM ImageAnnotation.
      // The AimImageAnnotationCollectionDataUploader kicks off a separate upload
      // of an RT-STRUCT file, which, in turn archives the ROI objects. Hence,
      // nothing needs to be done here.
   }
	
	@Override
	public ContourRendererHelper createContourRendererHelper()
                                throws DataFormatException
	{
		ContourRendererHelper crh = new ContourRendererHelper();
		
//		// An RtStruct object corresponding to a single ROI has only one element
//		// in its roiContourList.
//		assert (rtsSingle.roiContourList.size() == 1);
//		RoiContour rc = rtsSingle.roiContourList.get(0);
//		crh.setDisplayColour(rc.roiDisplayColour);
//		
//		// The frame of reference in which the ROI is defined is in a separate DICOM
//		// IOD from the contour list!
//		for (StructureSetRoi ssr : rtsSingle.structureSet.structureSetRoiList)
//		{
//			if (ssr.roiNumber == rc.referencedRoiNumber)
//				crh.setFrameOfReference(ssr.referencedFrameOfReferenceUid);
//		}
//		
//		crh.setCoordsAsPixel(false);
//		
//      List<RenderContour> rcl = new ArrayList<>();
//		for (Contour c : rc.contourList)
//      {
//         if (c.contourImageList.size() != 1)
//         {
//            String msg = "This type of contour cannot yet be rendered."
//                         + "More than one base image for a single contour.";
//            logger.error(msg);
//            throw new DataFormatException(DataFormatException.RTSTRUCT, msg);
//         }
//         
//         RenderContour rndC       = new RenderContour();
//         String baseSop           = c.contourImageList.get(0).referencedSopInstanceUid;
//         rndC.baseImageFilename   = rtdsu.sopFileMap.get(baseSop);
//			rndC.baseFrameNumberList = c.contourImageList.get(0).referencedFrameNumber;
//         rndC.nContourPoints      = c.nContourPoints;
//         rndC.contourPoints       = new float[c.nContourPoints][3];
//
//         for (int j=0; j<c.nContourPoints; j++)
//            for (int i=0; i<3; i++)
//               rndC.contourPoints[j][i] = (c.contourData.get(j)).get(i);
//         
//         rcl.add(rndC);
//      }
//		crh.setRenderContourList(rcl);
//		crh.setFilenameSet(filenameSet);
		crh.setXnatExperimentId(XNATExperimentID);
		crh.setXnatProfile(xnprf);
		crh.setXnatScanIdSet(XNATScanIdSet);
		
		return crh;
	}
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }
	
}
