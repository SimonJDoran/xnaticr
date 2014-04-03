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
* Java class: DownloadIcon.java
* First created on May 24, 2010 at 4:06:30 PM
* 
* Animated icon to be shown whenever the system is busy
* downloading data. Either a simple icon is shown for a general
* download, or a FancyLetterIcon is overlaid.
*********************************************************************/

package imageUtilities;

import generalUtilities.FancyLetterIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;


public class DownloadIcon extends AnimatedIcon
{
   static  final Logger     logger       = Logger.getLogger(DownloadIcon.class);

   private static final int TRANSPARENT         = 0x00;
   private static final int WHITE               = (0xFF << 24) | (0XFF << 16) | (0xFF << 8) | 0xFF;
   private static final int FRAME_DELAY         = 150; // time in ms
   private static final int DEFAULT_SIZE        = 16;
   private static final int SOURCE_IMAGE_SIZE   = 16;
   private static final ArrayList<BufferedImage> downloadImages = new ArrayList<BufferedImage>();
   private static final ArrayList<BufferedImage> defaultImages  = new ArrayList<BufferedImage>();
   

   // Read in the images for the icon once only, not at each instantiation of the
   // object, which would be very expensive.
   static
   {
      for (int i=0; i<12; i++)
      {
         StringBuilder sb = new StringBuilder("projectResources/downloadIcon");

         if (i<10) sb.append("0");
         sb.append(i);
         
         // Originally, I used the set of TIFF files in the resources directory.
         // but ImageIO.read does not work for these on the PC. Subsequent
         // investigation appears to show that ImageIO.read on the Mac does
         // not work correctly either(!), but the errors are more subtle.
         sb.append(".bmp");

         InputStream resourceIs = DownloadIcon.class.getResourceAsStream(sb.toString());

         BufferedImage image;
         if (resourceIs == null)
         {  // This is not a fatal error, so not worth having to use try ... catch 
            // when calling the function. Hence, just log the error without causing
            // an exception.
            logger.error("Couldn't find the download icon image resource. "
                    + "This shouldn't happen as it is supposed to be packaged with "
                    + "the application jar!");

            image = new BufferedImage(SOURCE_IMAGE_SIZE, SOURCE_IMAGE_SIZE,
                                      BufferedImage.TYPE_INT_ARGB);
         }
         else
         {
            try
            {
               image = ImageIO.read(resourceIs);
            }
            catch (Exception ex)
            {
               logger.error("Error while trying to load DownloadIcon image: "
                            + ex.getMessage());
               image = new BufferedImage(SOURCE_IMAGE_SIZE, SOURCE_IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
            }
         }

         downloadImages.add(image);
         
         BufferedImage scaledImage;
         try
         {
            /* Scale the source image to the given size. */
            float					sf = DEFAULT_SIZE / (float) SOURCE_IMAGE_SIZE;
            AffineTransform	at = AffineTransform.getScaleInstance(sf, sf);
            AffineTransformOp	op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            scaledImage          = op.createCompatibleDestImage(image, null);
            scaledImage          = op.filter(image, scaledImage);
         }
         catch (Exception ex)
         {
            logger.error("Unable to transform images to create thumbnails"
                           + ex.getMessage());
            scaledImage = new BufferedImage(DEFAULT_SIZE, DEFAULT_SIZE, BufferedImage.TYPE_INT_ARGB);
         }
         
         defaultImages.add(scaledImage);
      }

   }

   public DownloadIcon()
   {
      this(null, DEFAULT_SIZE, 'x', null, false, null, false, null, false, false);
   }


   public DownloadIcon(JComponent parent)
   {
      this(parent, DEFAULT_SIZE, 'x', null, false, null, false, null, false, false);
   }


   public DownloadIcon(int        iconDimension,
                       char       letter,
                       Color      background)
   {
      this(null, iconDimension, letter, background, false, background, false, background, false, false);
   }



   public DownloadIcon(JComponent component,
                       int        iconDimension,
                       char       letter,
                       Color      background)
   {
      this(component, iconDimension, letter, background, false, background, false, background, false, false);
   }


   /**
    *
    * @param parent        Component in which the new icon is drawn (can be null
    *                      if this is not known at the time of instantiation.
    * @param iconDimension integer valued number of pixels corresponding to the
    *                      width and height of the icon - note the download icon is square.
    * @param letter        character used to create the FancyLetterIcon that may be
    *                      used to create the icon
    * @param background    background colour of the FancyLetterIcon - if this is null,
    *                      then no FancyLetterIcon is used and the non-cycling icon is
    *                      completely transparent, meaning that effectively, this DownloadIcon
    *                      is invisible when not cycling.
    * @param arrow         boolean determining whether an arrow is added to the FancyLetterIcon
    * @param arrowColour   Color of arrow
    * @param plus          boolean determining whether an plus sign is added to the FancyLetterIcon
    * @param plusColour    Color of plus sign
    * @param useNonCycling boolean determining whether to include the "non-cycling" icon
    *                      in the animation sequence - this would give a flicker effect
    *                      once per cycle.
    * @param blend         boolean determining whether to blend the downloading symbol and
    *                      the FancyLetterIcon
    */
   public DownloadIcon(JComponent parent,
                       int        iconDimension,
                       char       letter,
                       Color      background,
                       boolean    arrow,
                       Color      arrowColour,
                       boolean    plus,
                       Color      plusColour,
                       boolean    useNonCycling,
                       boolean    blend)
   {
      // Create the object with a dummy first icon, because we need to call super()
      // as the first line of the constructor, before we have read in the image files.
      super(parent,
            FRAME_DELAY,
            new ImageIcon(new BufferedImage(iconDimension,
                                            iconDimension,
                                            BufferedImage.TYPE_INT_ARGB)));

      // If necessary, get the FancyLetter part of the icon.
      FancyLetterIcon flIcon;
      if (background == null) flIcon = null;
      else flIcon = new FancyLetterIcon(iconDimension, letter, background, arrow,
                                        arrowColour, plus, plusColour);

      for (int i=0; i<downloadImages.size(); i++)
      {
         BufferedImage scaledImage;
         
         if (iconDimension == DEFAULT_SIZE)
         {
            scaledImage = defaultImages.get(i);
         }
         
         else
         {
            BufferedImage downloadImage = downloadImages.get(i);
            try
            {
               /* Scale the source image to the given size. */
               float					sf = iconDimension / (float) SOURCE_IMAGE_SIZE;
               AffineTransform	at = AffineTransform.getScaleInstance(sf, sf);
               AffineTransformOp	op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
               scaledImage          = op.createCompatibleDestImage(downloadImage, null);
               scaledImage          = op.filter(downloadImage, scaledImage);
            }
            catch (Exception ex)
            {
               logger.error("Unable to transform images to create thumbnails"
                              + ex.getMessage());
               scaledImage = new BufferedImage(iconDimension, iconDimension, BufferedImage.TYPE_INT_ARGB);
            }
         }

         // If required, blend the FancyLetterIcon with the download images.
         BufferedImage iconImage;
         if (blend && (flIcon != null))
            iconImage = blendImages((BufferedImage) flIcon.getImage(), scaledImage, 0.5f);
         else
            iconImage = scaledImage;

         addIcon(new ImageIcon(makeTransparent(iconImage, WHITE, 60, 30)));
      }

      if (useNonCycling && (flIcon != null)) addIcon(flIcon);

      // Remove the dummy first icon.
      icons.remove(0);

      // Create a completely transparent image for the default non-cycling icon.
      BufferedImage transIm = new BufferedImage(iconDimension, iconDimension, BufferedImage.TYPE_INT_ARGB);
      for (int i=0; i<iconDimension; i++)
         for (int j=0; j<iconDimension; j++)
            transIm.setRGB(j, i, 0x00);
      
      setNonCyclingIcon((flIcon == null) ? new ImageIcon(transIm) : flIcon);
   }






   /**
    * Take an input images and return a transparent version that blends into the
    * background.
    * @param image      the source image
    * @param transpCol  the colour to be set as transparent
    * @param tol        the tolerance; everything less than a "distance" tol
    *                   from transpCol will be set to completely transparent. At
    *                   greater distances, there will be a linear blend.
    * @param blendWidth the width of the blend region
    * @return
    */
   private BufferedImage makeTransparent(BufferedImage image, int transC, int tol, int blendWidth)
   {
      int nx = image.getWidth();
      int ny = image.getHeight();

      BufferedImage outputImage = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);

      for (int j=0; j<ny; j++)
      {
         for (int i=0; i<nx; i++)
         {
            int imageC = image.getRGB(i, j);
            int imageT = (imageC >>> 24) & 0xFF;
            int imageR = (imageC >>> 16) & 0xFF;
            int imageG = (imageC >>>  8) & 0xFF;
            int imageB = (imageC >>>  0) & 0xFF;

            int transR = (transC >>> 16) & 0xFF;
            int transG = (transC >>>  8) & 0xFF;
            int transB = (transC >>>  0) & 0xFF;

            double dC = Math.sqrt(Math.pow(imageR - transR, 2)
                                + Math.pow(imageG - transG, 2)
                                + Math.pow(imageB - transB, 2));
            int newT;
            if (dC < tol) newT = 0;
            else          newT = (int) Math.min(0xFF, (0xFF*Math.abs(dC-tol))/(float) blendWidth);

            // The final new transparency is the more transparent of the
            // original and the new.
            int minT = Math.min(imageT, newT);
            int newC = (minT << 24) | (imageR << 16) | (imageG << 8) | imageB;
            outputImage.setRGB(i, j, newC);
            //System.out.println("i:" + i + " j:" + j + " T:" + imageT + " R:" + imageR
            //        + " G:" + imageG + " B:" + imageB + " dC:" + dC + " minT:" + minT + "newC:" + newC);
         }
      }

      return outputImage;
   }



   private BufferedImage blendImages(BufferedImage image1, BufferedImage image2,
                                     float frac)
   {
      int nx = image1.getWidth();
      int ny = image1.getHeight();

      BufferedImage outputImage = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);

      for (int j=0; j<nx; j++)
      {
         for (int i=0; i<nx; i++)
         {
            int image1C = image1.getRGB(i, j);
            int image1T = (image1C >>> 24) & 0xFF;
            int image1R = (image1C >>> 16) & 0xFF;
            int image1G = (image1C >>>  8) & 0xFF;
            int image1B = (image1C >>>  0) & 0xFF;

            int image2C = image2.getRGB(i, j);
            int image2T = (image2C >>> 24) & 0xFF;
            int image2R = (image2C >>> 16) & 0xFF;
            int image2G = (image2C >>>  8) & 0xFF;
            int image2B = (image2C >>>  0) & 0xFF;

            int outR = image1R;
            int outG = image1G;
            int outB = image1G;

            int outT = Math.min(image1T + image2T, 0xFF);

            if ((image2R) < 150)
            {
               outR = image1R + (int) (frac * (float)(image2R - image1R));
               outG = image1G + (int) (frac * (float)(image2G - image1G));
               outB = image1B + (int) (frac * (float)(image2B - image1B));
            }

            int outC = (outT << 24) | (outR << 16) | (outG << 8) | outB;
            outputImage.setRGB(i, j, outC);
         }
      }

      return outputImage;
   }

}
