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


package dataRepresentations;

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
import dataRepresentations.RTStruct.Contour;
import dataRepresentations.RTStruct.ROIContour;
import exceptions.XNATException;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.DataFormatException;
import org.dcm4che2.data.DicomObject;
import xnatDAO.XNATProfile;

public class ContourRenderer
{
   // The "R" in this auxilliary class name is to distinguish it from the 
   // Contour class in RTStruct.java.
   public class RContour
   {
      public String              baseImageUID;
      public int                 nContourPoints;
      public float[][]           contourPoints;
   }
   
   static    Logger              logger = Logger.getLogger(ContourRenderer.class);
   private   static final int    THUMBNAIL_SIZE = 128;
   protected DataRepresentation  dr;
	protected Map<String, File>   cachedImageFiles;
   protected float[]             pixelSpacing;
   protected float[]             dirCosines;
   protected float[]             topLeftPos;
   protected int[]               displayColour;
   protected String              frameOfReferenceUID;
   protected boolean             coordsAsPixel;
   protected ArrayList<RContour> rcList;   
     
   
   /**
    * Constructor creates a new renderer of a given RTStruct object.
    * @param rts RTStruct that contains all the
    * information necessary for rendering the set of contours.
    * @param contourNumber integer value containing the number of the contour
    * to be rendered in list of contours represented by rts.
    */
   public ContourRenderer(RTStruct rts, int contourNumber,
			                 Map<String, File> cachedImageFiles)
                         throws DataFormatException
   {
      this.dr               = rts;
		this.cachedImageFiles = cachedImageFiles;
		
		ROIContour roiContour = rts.roiContourList[contourNumber];
      displayColour         = roiContour.roiDisplayColour;
      frameOfReferenceUID   = roiContour.frameOfReferenceUID;
      coordsAsPixel         = false;
      rcList                = new ArrayList<RContour>();
      
      for (Contour c : roiContour.contourList)
      {
         if (c.imageList.length != 1)
         {
            String msg = "This type of contour cannot yet be rendered."
                         + "More than one base image for a single contour.";
            logger.error(msg);
            throw new DataFormatException(msg);
         }
         
         RContour rc = new RContour();
         rc.baseImageUID   = c.imageList[0].SOPInstanceUID;
         rc.nContourPoints = c.nContourPoints;
         rc.contourPoints  = new float[c.nContourPoints][3];

         for (int j=0; j<c.nContourPoints; j++)
            for (int i=0; i<3; i++)
               rc.contourPoints[j][i] = c.contourPoints[j][i];
         
         rcList.add(rc);
      }
   }
   
   
   /**
    * Constructor creates a new renderer of a given MRIWOutput object.
    * @param mriw MRIWOutput that contains most of the
    * information necessary for rendering a contour. Note that the MRIW format
    * does not have a number of the contour-related features of DICOM-RT on
    * which these developments were originally based, so various default
    * values need to be supplied and in addition, the data that are present
    * need to be manipulated to get the correct format.
    */
   public ContourRenderer(MRIWOutput mriw)
   {
      this.dr = mriw;

      displayColour         = new int[]{255, 0, 0};
      frameOfReferenceUID   = mriw.frameOfReferenceUID;
      coordsAsPixel         = true;
      rcList                = new ArrayList<RContour>();
      
      RContour rc           = new RContour();
      rc.baseImageUID       = mriw.inp.dynSOPInstanceUIDs.get(0);
      rc.nContourPoints     = mriw.con.roiX.size();
      rc.contourPoints      = new float[rc.nContourPoints][3];
      rcList.add(rc);
      for (int i=0; i<rc.nContourPoints; i++)
      {
         rc.contourPoints[i][0] = mriw.con.roiX.get(i).floatValue();
         rc.contourPoints[i][1] = mriw.con.roiY.get(i).floatValue();
         rc.contourPoints[i][2] = -999.9f; // dummy value
      }           
   }
   
   
	/**
    * Constructor creates a new renderer of a given MRIWOutput object.
    * @param mriw MRIWOutput that contains most of the
    * information necessary for rendering a contour. Note that the MRIW format
    * does not have a number of the contour-related features of DICOM-RT on
    * which these developments were originally based, so various default
    * values need to be supplied and in addition, the data that are present
    * need to be manipulated to get the correct format.
    */
   public ContourRenderer(AIMOutput aim)
   {
      this.dr = aim;

      /*
		]displayColour         = new int[]{255, 0, 0};
      frameOfReferenceUID   = mriw.frameOfReferenceUID;
      coordsAsPixel         = true;
      rcList                = new ArrayList<RContour>();
      
      RContour rc           = new RContour();
      rc.baseImageUID       = mriw.inp.dynSOPInstanceUIDs.get(0);
      rc.nContourPoints     = mriw.con.roiX.size();
      rc.contourPoints      = new float[rc.nContourPoints][3];
      rcList.add(rc);
      for (int i=0; i<rc.nContourPoints; i++)
      {
         rc.contourPoints[i][0] = mriw.con.roiX.get(i).floatValue();
         rc.contourPoints[i][1] = mriw.con.roiY.get(i).floatValue();
         rc.contourPoints[i][2] = -999.9f; // dummy value
      } 
		*/
   }
	
	
   /**
    * Constructor creates a new renderer of a given MRIWOutput object.
    * @param adept ADEPTOutput that contains most of the
    * information necessary for rendering a contour. Note that the ADEPT format
    * does not have a number of the contour-related features of DICOM-RT on
    * which these developments were originally based, so various default
    * values need to be supplied and in addition, the data that are present
    * need to be manipulated to get the correct format.
    */
   public ContourRenderer(ADEPTOutput adept)
   {
      this.dr = adept;          
   }
	
	
	public ArrayList<BufferedImage> createImages() throws Exception
   {

      ArrayList<BufferedImage> result = new ArrayList<BufferedImage>();
      
      for (RContour rc : rcList)
      {         
         try
         {
            BufferedImage bi = getBaseImage(rc.baseImageUID);
            overlayContour(bi, rc);
            result.add(ImageUtilities.scaleColourImageByFFT(bi,
                                              THUMBNAIL_SIZE, THUMBNAIL_SIZE));
         }
         catch (ImageUtilitiesException exIUE)
         {
            String msg  = "There was an error in the image scaling process: "
                            + exIUE.getMessage();
            logger.error(msg);
            throw new ImageUtilitiesException(ImageUtilitiesException.THUMBNAIL_CREATION, msg);
         }
      }
      
      return result;
   }
      
      
      
      
   private BufferedImage getBaseImage(String imageUID)
                         throws XNATException, ImageUtilitiesException
   {
      DicomObject bdo	= new BasicDicomObject();
      

		try
      {
         String imageFilename = dr.fileSOPMap.get(imageUID);
         DicomInputStream dis = new DicomInputStream(cachedImageFiles.get(imageFilename));
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
      if (!fOR.equals(frameOfReferenceUID))
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
   
   
   
   private void overlayContour(BufferedImage bi, RContour rc)
   {
      Graphics2D g2d = bi.createGraphics();
      g2d.setPaint(new Color(displayColour[0], displayColour[1], displayColour[2]));
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
