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
* Java class: ImageUtilities.java
* First created on July 19, 2007, 8:57 AM
* 
* A set of static methods for processing images for display purposes,
* for example performing windowing and scaling.
*********************************************************************/

package imageUtilities;

import exceptions.ImageUtilitiesException;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Hashtable;
import edu.emory.mathcs.jtransforms.fft.*;


public class ImageUtilities
{
	
	/**
	 * Creates a new instance of ImageUtilities
	 *
	 * The ImageUtilities class contains a set of methods for processing images
	 * for display purposes, for example performing windowing. The constructor
	 * is empty, because this class contains only static methods.
	 */
	private ImageUtilities()
	{
	}
	

	/**
	 * Get the maximum value of an array of data.
	 */
	public static int max(int[] a)
	{
		int max = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > max)  max = a[i];
		}
		
		return max;
	}
	
	
	public static short max(short[] a)
	{
		short max = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > max)  max = a[i];
		}
		
		return max;
	}


	
	public static long max(long[] a)
	{
		long max = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > max)  max = a[i];
		}
		
		return max;
	}	
		

		
	public static float max(float[] a)
	{
		float max = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > max)  max = a[i];
		}
		
		return max;
	}	

	
	
	public static double max(double[] a)
	{
		double max = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > max)  max = a[i];
		}
		
		return max;
	}	
	

	
	public static int max(DataBufferUShort a)
	{
		int max = a.getElem(0);
		
		for (int i=1; i<a.getSize(); i++)
		{
			if (a.getElem(i) > max)  max = a.getElem(i);
		}
		
		return max;
	}
	
	
	
	/**
	 * Get the minimum value of an array of data.
	 */
	public static int min(int[] a)
	{
		int min = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] < min)  min = a[i];
		}
		
		return min;
	}
	
	
	public static short min(short[] a)
	{
		short min = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] < min)  min = a[i];
		}
		
		return min;
	}


	
	public static long min(long[] a)
	{
		long min = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] < min)  min = a[i];
		}
		
		return min;
	}	
		

		
	public static float min(float[] a)
	{
		float min = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] < min)  min = a[i];
		}
		
		return min;
	}	

	
	
	public static double min(double[] a)
	{
		double min = a[0];
		
		for (int i=1; i<a.length; i++)
		{
			if (a[i] > min)  min = a[i];
		}
		
		return min;
	}
	
	

	public static int min(DataBufferUShort a)
	{
		int min = a.getElem(0);
		
		for (int i=1; i<a.getSize(); i++)
		{
			if (a.getElem(i) < min)  min = a.getElem(i);
		}
		
		return min;
	}		

	
	
	
	/**
	 * Apply the windowBuffer specified by the min and max arguments to the image data
	 * in data. The result of the operation is a rescaled version of data for
	 * which all min maps to 0 and max maps to 2^16 - 1. Values less than
	 * min map to 0 and values greater than max map to 2^16 - 1.
	 */
	public static DataBufferUShort windowBuffer( DataBufferUShort data, int min, int max) throws ImageUtilitiesException
	{
		
		int		USHORTMAX	= 65535;
		int		range			= max - min;
		if ( range <= 0 ) throw new ImageUtilitiesException(ImageUtilitiesException.INCORRECT_WINDOW_RANGE);
		int		a;
		
		short[]	shortData	= data.getData();
		short[]	shortResult	= new short[shortData.length];
		
		
		for (int i=0; i<shortData.length; i++)
		{
			/* Although the data comes from an unsigned short buffer, when it is read
			 * into a short array, it is interpreted as signed. Before we can do any
			 * arithmetic on it, we need to convert it to an unsigned integer.
			 */
			a = (0 | shortData[i]) & 0xFFFF;
			
			// Now perform the windowing itself.
			a = (USHORTMAX * (a - min)) / range;
			
			a = (((a<0) ? 0 : a) > USHORTMAX) ? USHORTMAX : a;
			
			// Convert the result back to a (signed) short int.
			shortResult[i] = (short) (a & 0xFFFF);
		}
		
		DataBufferUShort result = new DataBufferUShort(shortResult, shortData.length);

		return result;
	}
	
	
	
	/**
	 * Create a BufferedImage (suitable for output to the screen using
	 * Graphics2D.drawImage) from a one-component dataset of unsigned short
	 * integers. Use a greyscale colour map.
	 *
	 * The data are stored as a 1-D array in data. To turn these into a 2-D
	 * image, we need to know the number of columns nCols.
	 */
	
	public static BufferedImage createGreyScaleImageFromUShort(short[] dataShort, int nCols) throws ImageUtilitiesException
	{
		if (nCols == 0) throw new ImageUtilitiesException(ImageUtilitiesException.ZERO_COLUMNS);
		
		int		pixelStride		= 1;
		int		scanLineStride	= nCols;
		int		nRows				= dataShort.length/nCols;
		if ( (float) nRows != (float) dataShort.length / (float) nCols )
			throw new ImageUtilitiesException(ImageUtilitiesException.NON_RECTANGULAR_IMAGE);
		
		int[]		bandOffsets		= new int[]{0};
		boolean	hasAlpha			= false;
		boolean	isAlphaPremult	= false;
		boolean	isRasterPremult= false;
		Point		topLeftCoords	= new Point (0,0);
		
		/* Note that in some CT images, the "undefined" region outside the 
       * imaged circle is set to a negative number. This creates problems
       * in rendering the image via this routine, so set any number less than
       * zero to zero.
       */
      for (int i=0; i<dataShort.length; i++) if (dataShort[i] < 0) dataShort[i] = 0;
      
		/* In what follows, the ComponentColorModel ccm transforms short values in
		 * DataBufferUShort db into normalised values in the range [0.0, 1.0] such that
		 * 0 corresponds to 0.0 and 2^n - 1 corresponds to 1.0. For TYPE_USHORT,
		 * n = 16. The normalised values are then passed to the ColorSpace cs for
		 * transformation into the colours that will actually appear on the screen.
		 *
		 * Hence, in order to see an image that is scaled to an appropriate level,
		 * we need to window our data before creating the WriteableRaster.
		 */
	   DataBufferUShort		db	= new DataBufferUShort(dataShort, dataShort.length);
		
		DataBufferUShort	  wdb = null;
		try
		{
			wdb = ImageUtilities.windowBuffer(db, ImageUtilities.min(db), ImageUtilities.max(db));
		}
		catch (ImageUtilitiesException ex)
		{
			throw ex;
		}
		
		ComponentSampleModel csm	= new ComponentSampleModel(DataBuffer.TYPE_USHORT,
													nCols, nRows, pixelStride, scanLineStride,
													bandOffsets);
		
		ColorSpace				cs		= ColorSpace.getInstance(ColorSpace.CS_GRAY);
		
		ComponentColorModel ccm		= new ComponentColorModel( cs, hasAlpha, isAlphaPremult,
													Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
		
		WritableRaster			wr		= Raster.createWritableRaster(csm, wdb, topLeftCoords);
		
		Hashtable				ht		= new Hashtable();
		
		BufferedImage			bi		= new BufferedImage(ccm, wr, isRasterPremult, ht );
		
		return bi;
	}
   
   
   /**
	 * Create a BufferedImage (suitable for output to the screen using
	 * Graphics2D.drawImage) from a one-component dataset of unsigned short
	 * integers. Implement the ColorSpace.CS_sRGB colour space. 
	 *
	 * The data are stored as a 1-D array in data. To turn these into a 2-D
	 * image, we need to know the number of columns nCols.
	 */
	
	public static BufferedImage createColourImageFromGreyScaleUShort(short[] dataShort, int nCols) throws ImageUtilitiesException
	{
		if (nCols == 0) throw new ImageUtilitiesException(ImageUtilitiesException.ZERO_COLUMNS);
		
		int		pixelStride		= 3;
		int		scanLineStride	= nCols*3;
		int		nRows				= dataShort.length/nCols;
		if ( (float) nRows != (float) dataShort.length / (float) nCols )
			throw new ImageUtilitiesException(ImageUtilitiesException.NON_RECTANGULAR_IMAGE);
		
		int[]		bandOffsets		= new int[]{0, 1, 2};
		boolean	hasAlpha			= false;
		boolean	isAlphaPremult	= false;
		boolean	isRasterPremult= false;
		Point		topLeftCoords	= new Point (0,0);
		
		/* Note that in some CT images, the "undefined" region outside the 
       * imaged circle is set to a negative number. This creates problems
       * in rendering the image via this routine, so set any number less than
       * zero to zero.
       */
      for (int i=0; i<dataShort.length; i++) if (dataShort[i] < 0) dataShort[i] = 0;
      
		/* Convert greyscale data to data suitable for placing in a colour image. */ 
      short[] dataRGB = new short[3*dataShort.length];
      for (int i=0; i<dataShort.length; i++)
      {
         dataRGB[3*i]     = dataShort[i];
         dataRGB[3*i + 1] = dataShort[i];
         dataRGB[3*i + 2] = dataShort[i];
      }
      
      
      /* In what follows, the ComponentColorModel ccm transforms short values in
		 * DataBufferUShort db into normalised values in the range [0.0, 1.0] such that
		 * 0 corresponds to 0.0 and 2^n - 1 corresponds to 1.0. For TYPE_USHORT,
		 * n = 16. The normalised values are then passed to the ColorSpace cs for
		 * transformation into the colours that will actually appear on the screen.
		 *
		 * Hence, in order to see an image that is scaled to an appropriate level,
		 * we need to window our data before creating the WriteableRaster.
		 */
	   DataBufferUShort		db	= new DataBufferUShort(dataRGB, dataRGB.length);
		
		DataBufferUShort	  wdb = null;
		try
		{
			wdb = ImageUtilities.windowBuffer(db, ImageUtilities.min(db), ImageUtilities.max(db));
		}
		catch (ImageUtilitiesException ex)
		{
			throw ex;
		}
		
		ComponentSampleModel csm	= new ComponentSampleModel(DataBuffer.TYPE_USHORT,
													nCols, nRows, pixelStride, scanLineStride,
													bandOffsets);
		
		ColorSpace				cs		= ColorSpace.getInstance(ColorSpace.CS_sRGB);
		
		ComponentColorModel ccm		= new ComponentColorModel( cs, hasAlpha, isAlphaPremult,
													Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
		
		WritableRaster			wr		= Raster.createWritableRaster(csm, wdb, topLeftCoords);
		
		Hashtable				ht		= new Hashtable();
		
		BufferedImage			bi		= new BufferedImage(ccm, wr, isRasterPremult, ht );
		
		return bi;
	}
   
   
   /**
    * Scale the input data by an arbitrary factor by interpolating.
    * This method works fine, but if we are down-sizing the data by more than a
    * factor of two, then not all the input pixels will contribute to the output.
    * @param src input data in the form of a BufferedImage
    * @param newWidth the width of the output image
    * @param newHeight the height of the output image
    * @return a scaled colour buffered image.
    * @throws ImageUtilitiesException 
    */
   public static BufferedImage scaleColourImageByInterpolation(BufferedImage src,
                                                               int wOut, int hOut)
                 throws ImageUtilitiesException
   {
      Hashtable      ht  = new Hashtable();
      DataBuffer     db  = src.getData().getDataBuffer();
      SampleModel    sm  = src.getSampleModel();
      int            wIn = src.getWidth();
      int            hIn = src.getHeight();
      
      
      // Initially, this routine works only for images made from a DataBufferUShort.
      if (db.getDataType() != DataBuffer.TYPE_USHORT) return null;
      db = (DataBufferUShort) db;
      
      // Sanity check
      if (wIn*hIn*3 != db.getSize()) return null;
       
      
      // Extract the original image data.
      short[][][] srcRGB  = new short[wIn][hIn][3];           
      for (int k=0; k<3; k++)
         for (int j=0; j<hIn; j++)
            for (int i=0; i<wIn; i++)
               srcRGB[i][j][k] = (short) db.getElem(3*(hIn*i + j) + k);
      
      
      // Find the interpolated image. Start with bilinear interpolation and
      // if not adequate, implement cubic convolution.
      
      DataBufferUShort destDb = new DataBufferUShort(3*hOut*wOut);
      float sf = (float) hIn / (float) hOut;
      
      for (int j=0; j<hOut; j++)
      {
         float yInt = sf * j;
         int   j0   = (int) Math.max(Math.floor(yInt), 0);
         int   j1   = (int) Math.min(Math.ceil(yInt), hIn-1);

         for (int i=0; i<wOut; i++)
         {
            float xInt = ((float) wIn  / (float) wOut)  * i;
            int   i0   = (int) Math.max(Math.floor(xInt), 0);
            int   i1   = (int) Math.min(Math.ceil(xInt), wIn-1);
            
            for (int k=0; k<3; k++)
            {
               float d00 = (float) srcRGB[i0][j0][k];
               float d10 = (float) srcRGB[i1][j0][k];
               float d01 = (float) srcRGB[i0][j1][k];
               float d11 = (float) srcRGB[i1][j1][k];
               
               // First interpolate along x.
               float dx0;
               float dx1;
               if (i1 != i0)
               {
                  dx0 = d00 + (xInt-i0) * (d10 - d00)/(float)(i1-i0);
                  dx1 = d01 + (xInt-i0) * (d11 - d01)/(float)(i1-i0);
               }
               else
               {
                  dx0 = d00;
                  dx1 = d01;
               }
                             
               // Now interpolate along y.
               if (j1 != j0)
                  destDb.setElem(3*(hOut*i + j) + k, (short) Math.round(dx0 + (yInt-j0) * (dx1 - dx0)/(float)(j1-j0)));
               else
                  destDb.setElem(3*(hOut*i + j) + k, (short) Math.round(dx0));
            }
         }
      }
      
      
		DataBufferUShort	  wdb = null;
		try
		{
			wdb = ImageUtilities.windowBuffer(destDb, ImageUtilities.min(destDb), ImageUtilities.max(destDb));
		}
		catch (ImageUtilitiesException exIUE)
		{
			throw exIUE;
		}
      
      int		pixelStride		 = 3;
		int		scanLineStride	 = wOut*3;		
		int[]		bandOffsets		 = new int[]{0, 1, 2};
		boolean	hasAlpha			 = false;
		boolean	isAlphaPremult	 = false;
		boolean	isRasterPremult = false;
		Point		topLeftCoords	 = new Point (0,0);
		
		ComponentSampleModel csm = new ComponentSampleModel(DataBuffer.TYPE_USHORT,
													wOut, wOut, pixelStride, scanLineStride,
													bandOffsets);
		
		ColorSpace				cs		= ColorSpace.getInstance(ColorSpace.CS_sRGB);
		
		ComponentColorModel ccm		= new ComponentColorModel( cs, hasAlpha, isAlphaPremult,
													Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
		
		WritableRaster			wr    = Raster.createWritableRaster(csm, wdb, topLeftCoords);
		
		BufferedImage			bi		= new BufferedImage(ccm, wr, isRasterPremult, ht );
		
		return bi;
      
   }
   
   
   
   /**
    * Scale the input data by an arbitrary factor using arbitrary length
    * Fourier Transforms.
    * This method is conceptually very easy. Take the FT of the input data;
    * cut it down or zero-pad to the desired size, then take the magnitude of
    * the inverse FT.
    * @param src input data in the form of a BufferedImage
    * @param wOut the width of the output image
    * @param hOut the height of the output image
    * @return a scaled colour BufferedImage.
    * @throws ImageUtilitiesException 
    */
   public static BufferedImage scaleColourImageByFFT(BufferedImage src, int wOut, int hOut)
                 throws ImageUtilitiesException
   {
      ColorModel     cm       = src.getColorModel();
      DataBuffer     dbIn     = src.getData().getDataBuffer();
      int            wIn      = src.getWidth();
      int            hIn      = src.getHeight();
      int            i0In     = Math.max((wIn-wOut)/2, 0); // Integer division
      int            i0Out    = Math.max((wOut-wIn)/2, 0);
      int            j0In     = Math.max((hIn-hOut)/2, 0); // Integer division
      int            j0Out    = Math.max((hOut-hIn)/2, 0);
      int            nx       = Math.min(wIn, wOut);
      int            ny       = Math.min(hIn, hOut);
      int            i1In     = wIn  - nx/2;
      int            i1Out    = wOut - nx/2;
      int            j1In     = hIn  - ny/2;
      int            j1Out    = hOut - ny/2;
      double[][]     dataIn   = new double[hIn][2*wIn];
      double[][]     dataOut  = new double[hOut][2*wOut];
      DoubleFFT_2D   ft2DIn   = new DoubleFFT_2D(hIn,  wIn);
      DoubleFFT_2D   ft2DOut  = new DoubleFFT_2D(hOut, wOut);
 
      // Initially, this routine works only for images made from a DataBufferUShort.
      if (dbIn.getDataType() != DataBuffer.TYPE_USHORT) return null;
      dbIn = (DataBufferUShort) dbIn;
      
      DataBufferUShort dbOut = new DataBufferUShort(3*hOut*wOut);
      
      // Sanity check
      if (wIn*hIn*3 != dbIn.getSize()) return null;

      // Extract and transform each colour plane separately.
      for (int k=0; k<3; k++)
      {
         // When we read in the data, we need to do a cyclic shift of half of
         // the FOV, because of the way the FT interprets data.
         int h2 = hIn/2;
         int w2 = wIn/2;
         
         for (int j=0; j<hIn; j++)
         {
            for (int i=0; i<wIn; i++)
            {
               dataIn[(j+h2)%hIn][((i+w2)%wIn)*2  ] = (double) dbIn.getElem(3*(j*wIn + i) + k);
               dataIn[(j+h2)%hIn][((i+w2)%wIn)*2+1] = 0;
            }
         }
         
         // If we are shrinking the data, then we take the central portion of
         // the FT in data0 and put it into data1, then put back-transform it.
         // If we are expanding the data, then we zero-fill the FT in data0 and
         // do the reverse transform.
         ft2DIn.complexForward(dataIn);
         
         // This code corresponds to the situation of an FT that puts low spatial
         // frequencies at the edges of the image and high in the centre.
         for (int j=0; j<ny/2; j++)
         {
            for (int i=0; i<nx/2; i++)
            {
               dataOut[j][i*2]                       = dataIn[j][i*2];
               dataOut[j][i*2 + 1]                   = dataIn[j][i*2 + 1];
               
               dataOut[j][(i1Out + i)*2]             = dataIn[j][(i1In + i)*2];
               dataOut[j][(i1Out + i)*2 + 1]         = dataIn[j][(i1In + i)*2 + 1];
               
               dataOut[j1Out + j][i*2]               = dataIn[j1In + j][i*2];
               dataOut[j1Out + j][i*2 + 1]           = dataIn[j1In + j][i*2 + 1];
               
               dataOut[j1Out + j][(i1Out + i)*2]     = dataIn[j1In + j][(i1In + i)*2];
               dataOut[j1Out + j][(i1Out + i)*2 + 1] = dataIn[j1In + j][(i1In + i)*2 + 1];
            }
            
         }
 
// Commented out code is what we would use if zero frequency corresponded to the
// centre of the matrix, as is logical from an MRI background.
//         for (int j=0; j<ny; j++)
//            for (int i=0; i<nx; i++)
//            {
//               dataOut[j0Out + j][(i0Out + i)*2]     = dataIn[j0In + j][(i0In + i)*2];
//               dataOut[j0Out + j][(i0Out + i)*2 + 1] = dataIn[j0In + j][(i0In + i)*2 + 1];
//            }
         
         ft2DOut.complexInverse(dataOut, true);
         
         // Cyclically shift of the data by half the field-of-view mirroring
         // what was done at the start.
         h2 = hOut/2;
         w2 = wOut/2;
         
         // Note that although the reverse FT was called with the scale
         // parameter set to true, we have to divide by an extra factor to
         // take account of the change in size of the dataset.
         float sf = (wOut * hOut) / (float)(wIn * hIn);
         
         for (int j=0; j<hOut; j++)
         {
            for (int i=0; i<wOut; i++)
            {
               
               short RGBOut = (short) (sf*Math.sqrt(Math.pow(dataOut[j][i*2], 2)
                                              + Math.pow(dataOut[j][i*2 + 1], 2)));
               int ishift = (i+w2)%wOut;
               int jshift = (j+h2)%hOut;
               
               dbOut.setElem(3*(jshift*hOut + ishift) + k, RGBOut);
            }   
         }
         
         
         
         
      }
      
      // Create a new BufferedImage using the data calculated.
      int		pixelStride		 = 3;
		int		scanLineStride	 = wOut*3;		
		int[]		bandOffsets		 = new int[]{0, 1, 2};
		boolean	hasAlpha			 = false;
		boolean	isAlphaPremult	 = false;
		boolean	isRasterPremult = false;
		Point		topLeftCoords	 = new Point (0,0);
		
		ComponentSampleModel csm = new ComponentSampleModel(DataBuffer.TYPE_USHORT,
													wOut, hOut, pixelStride, scanLineStride,
													bandOffsets);
		
		ColorSpace				cs		= ColorSpace.getInstance(ColorSpace.CS_sRGB);
		
		ComponentColorModel  ccm 	= new ComponentColorModel( cs, hasAlpha, isAlphaPremult,
													Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
		
		WritableRaster			wr    = Raster.createWritableRaster(csm, dbOut, topLeftCoords);
      
      BufferedImage			bi		= new BufferedImage(ccm, wr, isRasterPremult, new Hashtable() );
		
		return bi;
   
   }
}
