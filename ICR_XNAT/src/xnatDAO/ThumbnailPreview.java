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
* Java class: ThumbnailPreview.java
* First created on Apr 26, 2010 at 6.05 PM
* 
* Panel to page through a set of image thumbnails for the purposes
* of selecting a set of scan images. If we are simply selecting a
* single image, then the display is static. If we are selecting a
* scan, then we get a sequential display of all the input data, image
* by image.
*********************************************************************/

package xnatDAO;

import exceptions.ImageUtilitiesException;
import imageUtilities.ThumbnailDisplayPanel;
import imageUtilities.ImageUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.iod.module.composite.ImagePixel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public class ThumbnailPreview extends ThumbnailDisplayPanel implements ActionListener
{
   static  Logger        logger     = Logger.getLogger(ThumbnailPreview.class);
   private boolean       cancelled  = false;
   private Timer         timer      = null;
   private int           imageCount = 0;
   private String        fileText;
   private BufferedImage blankImage;

   private LinkedHashMap<File, BufferedImage> fileImageMap;

   private static final int DEFAULT_DELAY  = 100;
   private static final int UNCHANGED      = 0;
   private static final int READ_ERROR     = 1;
   private static final int CANCELLED      = 2;
   private static final int CHANGED        = 3;
   private static final int NO_DATA        = 4;
   private static final int MAX_TEXT_LINES = 50;

   public ThumbnailPreview()
   {
      fileImageMap = new LinkedHashMap<File, BufferedImage>(); 
      timer        = new Timer(DEFAULT_DELAY, this);
      blankImage   = new BufferedImage(PANEL_SIZE, PANEL_SIZE, BufferedImage.TYPE_INT_ARGB);
   }


   public synchronized void clearImages()
   {
      fileImageMap = new LinkedHashMap<File, BufferedImage>();
   }
   
   
   /**
     * Add a file to the list of images cycling round in the preview window.
     * @param file
     * @param type String allowing different types to be specified, which
     * produce different preview images.
     * @return boolean result true if success false otherwise
     */
   public synchronized boolean addFile(File file, String type)
   {
      if (!fileImageMap.containsKey(file))
      {
         if (type.equals("DICOM"))
         {
            try
            {
               fileImageMap.put(file, readDICOMFile(file));
            }
            catch (IOException exIO)
            {
               logger.warn("DCM4CHE failed to open file " + file.getName() + ".");
               return false;
            }
            thumbnailTextArea.setVisible(false);
            thumbnailPanel.setVisible(true);
         }
         
         if (type.equals("XML"))
         {
            try
            {
               fileText = readTextFile(file, MAX_TEXT_LINES);
            }
            catch (IOException exIO)
            {
               logger.warn("Failed to open file " + file.getName() + ". IOException "
                           + exIO.getMessage());
               return false;
            }
            setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            thumbnailPanel.setVisible(false);
            thumbnailTextArea.setVisible(true);
            setText(fileText);
         }
         
         if (type.equals("PNG"))
         {
            try
            {
               fileImageMap.put(file, readPNGFile(file));
            }
            catch (IOException exIO)
            {
               logger.warn("Failed to read PNG file " + file.getName() + ".");
               return false;
            }
            thumbnailTextArea.setVisible(false);
            thumbnailPanel.setVisible(true);
         }
      }
      return true;
   }
   
   
   public synchronized int loadImages(ArrayList<ArrayList<File>> imageFileList)
   {
      boolean changed = false;

      if (imageFileList.size() == 0) return NO_DATA;

      for (Iterator<ArrayList<File>> i1 = imageFileList.iterator(); i1.hasNext();)
      {
         ArrayList<File> alf = i1.next();
         for (Iterator<File> i2 = alf.iterator(); i2.hasNext();)
         {
            if (cancelled) return CANCELLED;

            File imageFile = i2.next();
            if (!fileImageMap.containsKey(imageFile))
            {
               try
               {
                  fileImageMap.put(imageFile, readDICOMFile(imageFile));
                  changed = true; 
               }
               catch (IOException exIO)
               {
                  // Fail silently - error has already been flagged.
               }
       
            }
         }
      }

      if (changed) return CHANGED; else return UNCHANGED;
   }
   
   
   protected String readTextFile(File textFile, int nLines)
             throws IOException
   {
      StringBuilder  sb    = new StringBuilder();

      BufferedReader br    = new BufferedReader(new FileReader(textFile));
      int            count = 0;
      String         s;
      String         NL = System.getProperty("line.separator");

      while ( (count < nLines) && ((s = br.readLine()) != null) )
      {
         sb.append(s).append(NL);
         count++;
      }

      br.close();
      
      return sb.toString();
   }
   
   
   
   protected BufferedImage readPNGFile(File imageFile)
             throws IOException
   {
      BufferedImage img;
              
      try
      {
         img = ImageIO.read(imageFile);
      }
      catch (Exception exIO)
      {
         String message = "Unable to read image to render thumbnail.\n"
                           + "Offending file: " + imageFile.getPath() + "\n"
                           + exIO.getMessage();
         logger.warn(message);
         throw new IOException(message);
      }
      
      return img;
   }
   
   
   
   protected BufferedImage readDICOMFile(File imageFile)
             throws IOException
   {
      BasicDicomObject bdo	= new BasicDicomObject();
      try
      {
         BufferedInputStream bis
            = new BufferedInputStream(new FileInputStream(imageFile));
         DicomInputStream dis = new DicomInputStream(bis);
         dis.readDicomObject(bdo, -1);
      }
      catch (IOException exIO)
      {
         String message = "Unable to read image to create thumbnail.\n"
                           + "Offending file: " + imageFile.getPath() + "\n"
                           + exIO.getMessage();
         logger.warn(message);
         throw new IOException(message);
      }

      ImagePixel	imp			= new ImagePixel(bdo);
      int			nCols			= imp.getColumns();
      int			nRows			= imp.getRows();
      short[]		imageData	= new short[nCols*nRows];
      
      try
      {
      ShortBuffer sbb			= ByteBuffer.wrap(imp.getPixelData())
                                          .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                                          .asShortBuffer()
                                          .get(imageData);
      }
      catch (NullPointerException exNPE)
      {
         String message = "Unable to read image to create thumbnail.\n"
                           + "Offending file: " + imageFile.getPath() + "\n"
                           + "Caught NPE - suggests that file size to be read is not valid DICOM.";
         logger.warn(message);
         throw new IOException(message);
      }
      BufferedImage src  = null;
      BufferedImage dest = null;
      try
      {
         src = ImageUtilities.createGreyScaleImageFromUShort(imageData, nCols);

         /* Scale the source image down to thumbnail size. Preserve the aspect ratio,
          * at the same time ensuring that both dimensions fit into the thumbnail
          * box, by using the smaller of the two scale factors. */
         int					height= src.getHeight();
         int					width = src.getWidth();
         float					sfx	= getWidth() / (float) width;
         float					sfy	= getHeight() / (float) height;
         float					sfMin	= Math.min(sfx, sfy);

         AffineTransform	at		= AffineTransform.getScaleInstance(sfMin, sfMin);
         AffineTransformOp	op		= new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

         dest = op.createCompatibleDestImage(src, null);
         dest = op.filter(src, dest);
      }
      catch (ImageUtilitiesException exIMU)
      {
         String message = "Unable to read image to create thumbnail.\n"
                           + "Offending file: " + imageFile.getPath() + "\n"
                           + exIMU.getMessage();
         logger.warn(message);
         throw new IOException(message);
      }

      return dest;
   }


   public void setDelay(int delay)
   {
      timer.setDelay(delay);
   }


   public void start()
   {
 		if (!timer.isRunning()) timer.start();
   }


   public void stop()
   {
      timer.stop();
   }


   @Override
   public void actionPerformed(ActionEvent e)
   {
      if (this.thumbnailPanel.isVisible()) displayNextImage();
   }


   public synchronized void displayNextImage()
   {
      if (fileImageMap.size() == 0)
      {
         setImage(blankImage);
         repaint();
         return;
      }

      Set<Map.Entry<File, BufferedImage>> mapEntries = fileImageMap.entrySet();
      Iterator<Map.Entry<File, BufferedImage>> ime = mapEntries.iterator();
      
      // In my original method, I made the set iterator ime an instance
      // variable and kept track of it between Timer events. However, this
      // leads to ConcurrentModificationException problems when the method
      // addImage tries to modify fileImageMap. Here, I regenerate ime
      // each time, but keep track of which number in the sequence we are
      // at via the variable imageCount. imageCount can potentially become
      // greater than the number of images available if fileImageMap has
      // been reset to a different list of files between calls. In this
      // case, simply restart the display at zero and continue.
      if (imageCount > fileImageMap.size()-1) imageCount = 0;
      
      // Skip the images we have already displayed.
      for (int i=0; i<imageCount; i++) ime.next();

      setImage(ime.next().getValue());
      repaint();

      imageCount++;
   }







   

}
