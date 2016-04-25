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
* Java class: ContourRenderer.java
* First created on Dec 1, 2011 at 17:18 PM
* 
* Take the specifications of a contour enclosing an image region-of-
* interest and create visual representations of it, e.g., thumbnail
* images.
*********************************************************************/


package xnatUploader;

import dataRepresentations.xnatUpload.MRIWOutput;
import dataRepresentations.xnatUpload.AIMOutput;
import dataRepresentations.xnatUpload.ADEPTOutput;
import dataRepresentations.xnatUpload.XnatUpload;
import exceptions.ImageUtilitiesException;
import imageUtilities.ImageUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.iod.module.composite.ImagePixel;
import dataRepresentations.RTStruct_old.ROIContour;
import dataRepresentations.dicom.Contour;
import dataRepresentations.dicom.RtStruct;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.DataFormatException;
import org.dcm4che2.data.DicomObject;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATRESTToolkit;
import static xnatUploader.DataUploader.logger;

public class ContourRendererHelper
{
   // The "R" in this auxilliary class name is to distinguish it from the 
   // Contour class in RTStruct_old.java.
   public static class RenderContour
   {
      public String              baseImageFilename;
		public ArrayList<Integer>  baseFrameNumberList;
      public int                 nContourPoints;
      public float[][]           contourPoints;
   }
   
   static Logger                   logger = Logger.getLogger(ContourRendererHelper.class);
   public static final int         THUMBNAIL_SIZE = 128;
   public Map<String, File>        filenameCacheFileMap;
   public float[]                  pixelSpacing;
   public float[]                  dirCosines;
   public float[]                  topLeftPos;
   public List<Integer>            displayColour;
   public String                   frameOfReferenceUid;
   public boolean                  coordsAsPixel;
   public ArrayList<RenderContour> rndCList;
	public ContourRenderer          caller;
     
   
   public ContourRendererHelper() {}
	   
//   /**
//    * Constructor creates a new renderer of a given MRIWOutput object.
//    * @param mriw MRIWOutput that contains most of the
//    * information necessary for rendering a contour. Note that the MRIW format
//    * does not have a number of the contour-related features of DICOM-RT on
//    * which these developments were originally based, so various default
//    * values need to be supplied and in addition, the data that are present
//    * need to be manipulated to get the correct format.
//    */
//   public ContourRendererHelper(MRIWOutput mriw)
//   {
//      this.dr = mriw;
//
//      displayColour         = new int[]{255, 0, 0};
//      frameOfReferenceUid   = mriw.frameOfReferenceUID;
//      coordsAsPixel         = true;
//      rndCList                = new ArrayList<RenderContour>();
//      
//      RenderContour rc           = new RenderContour();
//      rc.baseImageUid       = mriw.inp.dynSOPInstanceUIDs.get(0);
//      rc.nContourPoints     = mriw.con.roiX.size();
//      rc.contourPoints      = new float[rc.nContourPoints][3];
//      rndCList.add(rc);
//      for (int i=0; i<rc.nContourPoints; i++)
//      {
//         rc.contourPoints[i][0] = mriw.con.roiX.get(i).floatValue();
//         rc.contourPoints[i][1] = mriw.con.roiY.get(i).floatValue();
//         rc.contourPoints[i][2] = -999.9f; // dummy value
//      }           
//   }
//   
//   
//	/**
//    * Constructor creates a new renderer of a given MRIWOutput object.
//    * @param mriw MRIWOutput that contains most of the
//    * information necessary for rendering a contour. Note that the MRIW format
//    * does not have a number of the contour-related features of DICOM-RT on
//    * which these developments were originally based, so various default
//    * values need to be supplied and in addition, the data that are present
//    * need to be manipulated to get the correct format.
//    */
//   public ContourRendererHelper(AIMOutput aim)
//   {
//      this.dr = aim;
//
//      /*
//		]displayColour         = new int[]{255, 0, 0};
//      frameOfReferenceUid   = mriw.frameOfReferenceUid;
//      coordsAsPixel         = true;
//      rndCList                = new ArrayList<RContour>();
//      
//      RenderContour rc           = new RenderContour();
//      rc.baseImageUid       = mriw.inp.dynSOPInstanceUIDs.get(0);
//      rc.nContourPoints     = mriw.con.roiX.size();
//      rc.contourPoints      = new float[rc.nContourPoints][3];
//      rndCList.add(rc);
//      for (int i=0; i<rc.nContourPoints; i++)
//      {
//         rc.contourPoints[i][0] = mriw.con.roiX.get(i).floatValue();
//         rc.contourPoints[i][1] = mriw.con.roiY.get(i).floatValue();
//         rc.contourPoints[i][2] = -999.9f; // dummy value
//      } 
//		*/
//   }
//	
//	
//   /**
//    * Constructor creates a new renderer of a given MRIWOutput object.
//    * @param adept ADEPTOutput that contains most of the
//    * information necessary for rendering a contour. Note that the ADEPT format
//    * does not have a number of the contour-related features of DICOM-RT on
//    * which these developments were originally based, so various default
//    * values need to be supplied and in addition, the data that are present
//    * need to be manipulated to get the correct format.
//    */
//   public ContourRendererHelper(ADEPTOutput adept)
//   {
//      this.dr = adept;          
//   }
	
	
	public void retrieveBaseImagesToCache() throws XNATException
	{
		Set<String> filenames = caller.getFilenameSet();
		filenameCacheFileMap = new HashMap<>();
		
		String homeDir   = System.getProperty("user.home");
      String fileSep   = System.getProperty("file.separator");
		String cacheDir  = homeDir + fileSep + ".XNAT_DAO";

		// Notice that we need to cater explicitly for the allowed possibility
		// that a single structure set can reference base data from more than
		// one scan.
		int nDownloadFailures = 0;
		for (String scanId : caller.getXnatScanIdSet())
		{
			String RESTCommand = "/data/archive/experiments/" + caller.getXnatExperimentId()
						                + "/scans/"              + scanId
						                + "/resources/DICOM"
						                + "/files"
						                + "?format=xml";

			Vector2D  resultSet;
			try
			{
				XNATRESTToolkit xnrt = new XNATRESTToolkit(caller.getXnatProfile());
				resultSet = xnrt.RESTGetResultSet(RESTCommand);
			}
			catch(XNATException exXNAT)
			{
				throw new XNATException(XNATException.RETRIEVING_LIST);
			}
			
			Vector<String> URI  = resultSet.getColumn(2);
			for (int i=0; i<URI.size(); i++)
			{
				int pos     = URI.elementAt(i).lastIndexOf(fileSep);
				String name = URI.elementAt(i).substring(pos+1);
				if (filenames.contains(name))
				{
					// Build the local cache filename where the data will be stored.
					// The directory structure is a bit long-winded, but should be
					// easy to manage.
					StringBuilder sb = new StringBuilder(cacheDir);
					sb.append(URI.elementAt(i));
					File cacheFile = new File(sb.toString());
					File parent    = new File(cacheFile.getParent());

					boolean success = true;
					if (!(cacheFile.exists()))
					{
						// Retrieve the actual data and store it in the cache.
						try
						{
							parent.mkdirs();
							BufferedOutputStream bos
								= new BufferedOutputStream(new FileOutputStream(cacheFile, true));

							BufferedInputStream  bis
								= new BufferedInputStream(caller.getXnatProfile().doRESTGet(URI.elementAt(i)));

							byte[] buf = new byte[8192];

							while (true)
							{
								int length = bis.read(buf);
								if (length < 0) break;
								bos.write(buf, 0, length);
							}

							logger.debug("Worker ID = " + this.toString() + " Downloaded " + cacheFile.toString());

							try{bis.close();}
							catch (IOException ignore) {;}

							try{bos.close();}
							catch (IOException ignore) {;}                                 

						}
						catch (Exception ex)
						{
							logger.warn("Failed to download " + cacheFile.getName());
							nDownloadFailures++;
							success = false;
						}
					}
					
					if (success) filenameCacheFileMap.put(name, cacheFile);
				}				
			}
		}
		if (nDownloadFailures != 0)
		{
			throw new XNATException(XNATException.DATA_NOT_PRESENT,
			                        "Problem retrieving " + nDownloadFailures +
								          "base images from XNAT to generate required thumbnails");
		}
	}
	
		
	public ArrayList<BufferedImage> createImages() throws ImageUtilitiesException
   {

      ArrayList<BufferedImage> result = new ArrayList<BufferedImage>();
      
      for (RenderContour rc : rndCList)
      {         
         try
         {
            BufferedImage bi = getBaseImage(rc.baseImageFilename, rc.baseFrameNumberList);
            overlayContour(bi, rc);
            result.add(ImageUtilities.scaleColourImageByFFT(bi,
                                              THUMBNAIL_SIZE, THUMBNAIL_SIZE));
         }
         catch (ImageUtilitiesException | XNATException ex)
         {
            String msg  = "There was an error in the image scaling process: "
                            + ex.getMessage();
            logger.error(msg);
            throw new ImageUtilitiesException(ImageUtilitiesException.THUMBNAIL_CREATION, msg);
         }
      }
      
      return result;
   }
      
      
      
      
   private BufferedImage getBaseImage(String imageFilename, ArrayList<Integer> frameNumber)
                         throws XNATException, ImageUtilitiesException
   {
      DicomObject bdo	= new BasicDicomObject();
      
      File cacheFile = filenameCacheFileMap.get(imageFilename);
		try
      {
         DicomInputStream dis = new DicomInputStream(cacheFile);
         dis.readDicomObject(bdo, -1);
      }
      catch(Exception ex)
      {
         String msg = "Error retrieving base image data from XNAT\n\n" + ex.getMessage();
         logger.error(msg);
         throw new XNATException(XNATException.GET, msg);
      }
      
      
      // Extract the image data themselves and turn into a BufferedImage.
      
      ImagePixel	imp			= new ImagePixel(bdo);
      int			nCols			= imp.getColumns();
      int			nRows			= imp.getRows();
      short[]		imageData	= new short[nCols*nRows];
      ShortBuffer sbb			= ByteBuffer.wrap(imp.getPixelData())
                                          .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                                          .asShortBuffer()
                                          .get(imageData);
      BufferedImage bi  = null;
      try
      {
         bi = ImageUtilities.createColourImageFromGreyScaleUShort(imageData, nCols);
      }
      catch (ImageUtilitiesException exIMU)
      {
         String msg = "DICOM base image can't be read\n\n" + exIMU.getMessage();
         logger.error(msg);
         throw new ImageUtilitiesException(ImageUtilitiesException.THUMBNAIL_CREATION,
                                                                         msg);
      }
      
      
      // Now we need to figure out how to transform from the patient-based
      // coordinate system onto our images. We assume initially that all the
      // contour data are on the single plane just loaded in - this might change.
      
      
      // Start with a sanity check!
      String fOR = bdo.getString(Tag.FrameOfReferenceUID);
      if (!fOR.equals(frameOfReferenceUid))
      {
         String msg = "Frame of reference UID's don't match - this shouldn't happen!";
         logger.error(msg);
         throw new XNATException(XNATException.DATA_INCONSISTENT, msg);
      }
      
      pixelSpacing = bdo.getFloats(Tag.PixelSpacing);
      dirCosines   = bdo.getFloats(Tag.ImageOrientationPatient);
      topLeftPos   = bdo.getFloats(Tag.ImagePositionPatient);
      
      return bi;
   }
   
   
   
   private void overlayContour(BufferedImage bi, RenderContour rc)
   {
      Graphics2D g2d = bi.createGraphics();
      g2d.setPaint(new Color(displayColour.get(0), displayColour.get(1), displayColour.get(2)));
//      g2d.setStroke(new BasicStroke(1.0f)); // Need to experiment with some values here.
//      for (int j=0; j<rc.nContourPoints-1; j++)
//      {
//         float[] r0 = new float[3];
//         float[] r1 = new float[3];
//         
//         for (int i=0; i<3; i++)
//         {
//            r0[i] = rc.contourPoints[j][i];
//            r1[i] = rc.contourPoints[j+1][i];
//         }
//         
//         
//         int[] startPoint = new int[3];
//         int[] endPoint   = new int[3];
//         
//         if (coordsAsPixel)
//         {
//            // For example, MRIW specifies ROI's like this.
//            for (int i=0; i<3; i++)
//            {
//               startPoint[i] = Math.round(r0[i]);
//               endPoint[i]   = Math.round(r1[i]);
//            }
//         }
//         
//         else
//         {
//            // For example, RT-STRUCTS have the ROI's coded in patient coords.
//            startPoint = convertFromPatientToImageCoords(
//                                      r0, topLeftPos, dirCosines, pixelSpacing);
//            
//            endPoint   = convertFromPatientToImageCoords(
//                                      r1, topLeftPos, dirCosines, pixelSpacing);
//         }
//         
//         g2d.drawLine(startPoint[0], startPoint[1], endPoint[0], endPoint[1]);
//         
//      }
      
      int[] x = new int[rc.nContourPoints];
      int[] y = new int[rc.nContourPoints];
      for (int j=0; j<rc.nContourPoints; j++)
      {
         if (coordsAsPixel)
         {
            // For example, MRIW specifies ROI's like this.
            x[j] = Math.round(rc.contourPoints[j][0]);
            y[j] = Math.round(rc.contourPoints[j][1]);
         }
         else
         {
            // For example, RT-STRUCTS have the ROI's coded in patient coords.
            int[] point = convertFromPatientToImageCoords(
                             rc.contourPoints[j], topLeftPos, dirCosines, pixelSpacing);
            x[j] = point[0];
            y[j] = point[1];
         }
      }
      
      GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x.length);
      polygon.moveTo(x[0], y[0]);
      for (int j=1; j<x.length; j++) polygon.lineTo(x[j], y[j]);
      polygon.closePath();
      g2d.fill(polygon);
      g2d.dispose();    
   }

   
   
   // Legacy code, replaced by ImageUtilities.scaleColourImageByFFT, because
   // the library returns an error for the colour images I am using.
   protected BufferedImage scaleImage(BufferedImage src, int thumbnailSize)
                           throws ImagingOpException
   {
      BufferedImage dest = null;
      try
      {
         int	height = src.getHeight();
         int	width  = src.getWidth();
         float	sfx	 = thumbnailSize / (float) width;
         float	sfy	 = thumbnailSize / (float) height;
         float	sfMin	 = Math.min(sfx, sfy);

         AffineTransform	at = AffineTransform.getScaleInstance(sfMin, sfMin);
         AffineTransformOp	op	= new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

         dest = op.createCompatibleDestImage(src, null);
         dest = op.filter(src, dest);
      }
      catch (ImagingOpException exIMOP) {throw exIMOP;}
      
      return dest;
   }
   
   
   /**
    * Convert coordinates supplied (e.g., from DICOM file) in patient space
    * to the column and row indices of the raw pixel image.
    * 
    * @param r 3-element float[] containing the x-, y- and z-coordinates to transform
    * @param p the coordinates in mm of the top-left pixel in the image
    * @param c the direction cosines/unit vectors: c[0], c[1] and c[2] = unit
    *          vector in column direction (i.e., parallel to rows); c[3],
    *          c[4], c[5] = unit vector in row direction (parallel to columns)
    * @param d the pixel dimensions: d[0] = row spacing; d[1] = column spacing
    * @return two element float[]: x[0] = image column, x[1] = image row. 
    */
   public static int[] convertFromPatientToImageCoords(float[] r, float[] p, float[] c, float[] d)
   {
      float fCol = 0;
      float fRow = 0;
      int[] result = new int[2];
      
      // Note that we cannot blindly find the inverse of the 4 x 4
      // matrix specified on p. 1288 of Volume 3 (Information Object
      // Definitions) of the 2011 DICOM standard, because it is not
      // actually a valid 3-D transformation and the inverse does not
      // exist. We solve some simultaneous equations, but have to check
      // for special cases because quite often the coefficients are zero.

      if (c[3] != 0)
      {
         if (c[1] != 0) 
         {
            fCol = ((r[1] - p[1]) - c[4]/c[3] * (r[0] - p[0]))
                                  / (c[1]*d[1] * (1 - c[4]/c[3] * c[0]/c[1]));
         }
         
         
         if (c[2] != 0)
         {
            fCol = ((r[2] - p[2]) - c[5]/c[3] * (r[0] - p[0]))
                                  / (c[2]*d[1] * (1 - c[5]/c[3] * c[0]/c[2]));
         }
         
         fRow = ((r[0] - p[0]) - c[0]*d[1]*fCol) / (c[3]*d[0]);         
      }
         
         
      if (c[4] != 0)
      {
         if (c[2] != 0) 
         {
            fCol = ((r[2] - p[2]) - c[5]/c[4] * (r[1] - p[1]))
                                  / (c[2]*d[1] * (1 - c[5]/c[4] * c[1]/c[2]));
         }
         
         
         if (c[0] != 0)
         {
            fCol = ((r[0] - p[0]) - c[3]/c[4] * (r[1] - p[1]))
                                  / (c[0]*d[1] * (1 - c[3]/c[4] * c[1]/c[0]));
         }
         
         fRow = ((r[1] - p[1]) - c[1]*d[1]*fCol) / (c[4]*d[0]);         
      }
      
      
      if (c[5] != 0)
      {
         if (c[0] != 0) 
         {
            fCol = ((r[0] - p[0]) - c[3]/c[5] * (r[2] - p[2]))
                                  / (c[0]*d[1] * (1 - c[3]/c[5] * c[2]/c[0]));
         }
         
         
         if (c[1] != 0)
         {
            fCol = ((r[0] - p[0]) - c[4]/c[5] * (r[2] - p[2]))
                                  / (c[1]*d[1] * (1 - c[4]/c[5] * c[2]/c[1]));
         }
         
         fRow = ((r[2] - p[2]) - c[2]*d[1]*fCol) / (c[5]*d[0]);         
      }
      
      
      result[0] = Math.round(fCol);
      result[1] = Math.round(fRow);
      
      return result;
   }
   
   
   /**
    * Convert coordinates supplied in terms of 2-D image pixels to patient space
    * as encoded in a DICOM file.
    * 
    * @param col the column of the point in the raw pixel image (can be fractional)
    * @param row the row of the point in the raw pixel image (can be fractional)
    * @param p the coordinates in mm of the top-left pixel in the image
    * @param c the direction cosines/unit vectors: c[0], c[1] and c[2] = unit
    *          vector in column direction (i.e., parallel to rows); c[3],
    *          c[4], c[5] = unit vector in row direction (parallel to columns)
    * @param d the pixel dimensions: d[0] = row spacing; d[1] = column spacing
    * @return 3-element float[] containing the x-, y- and z-coordinates of the
    *          transformed point.
    */
   public static float[] convertFromImageToPatientCoords(float col, float row, float[] p,
                                                         float[] c, float[] d)
   {
      float[] result = new float[3];
      
      result[0] = p[0] + col*c[0] + row*c[3];
      result[1] = p[1] + col*c[1] + row*c[4];
      result[2] = p[2] + col*c[2] + row*c[5];
      
      return result;
   }
}
