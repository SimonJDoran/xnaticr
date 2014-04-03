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
* Java class: ThumbnailWorker.java
* First created on Apr 27, 2010 at 3:41 PM
* 
* SwingWorker class that manages the loading of preview thumbnails
* on a worker thread
*********************************************************************/

package xnatDAO;

import exceptions.ImageUtilitiesException;
import imageUtilities.ImageUtilities;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.iod.module.composite.ImagePixel;

public class ThumbnailWorker extends SwingWorker<Integer, BufferedImage>
{
   static  Logger          logger       = Logger.getLogger(ThumbnailPreview.class);
   public static final int UNCHANGED    = 0;
   public static final int READ_ERROR   = 1;
   public static final int CANCELLED    = 2;
   public static final int CHANGED      = 3;
   public static final int NO_DATA      = 4;

   ThumbnailPreview        panel;
   ArrayList<ArrayList<File>> files;
   LinkedHashMap<File, BufferedImage> fileImageMap = null;
   Iterator<Map.Entry<File, BufferedImage>> ime = null;

   


   public ThumbnailWorker(ArrayList<ArrayList<File>> files, ThumbnailPreview panel)
   {
      super();
      this.panel = panel;
      this.files = files;
      if (files == null) this.files = new ArrayList<ArrayList<File>>();
   }


   public synchronized void setFileList(ArrayList<ArrayList<File>> fileList)
   {
      files = fileList;
   }


   public synchronized void addFileList(ArrayList<File> fileList)
   {
      files.add(fileList);
   }


   public synchronized void addFile(File file)
   {
      ArrayList<File> list = new ArrayList<File>();
      list.add(file);
      files.add(list);
   }


   private Integer loadImages()
   {
      if (fileImageMap == null) fileImageMap = new LinkedHashMap<File, BufferedImage>();

      boolean changed = false;
     logger.debug("loadImages: files.size() = " + files.size());
      if (files.size() == 0) return NO_DATA;

      for (Iterator<ArrayList<File>> i1 = files.iterator(); i1.hasNext();)
      {
         ArrayList<File> alf = i1.next();
         for (Iterator<File> i2 = alf.iterator(); i2.hasNext();)
         {
            if (isCancelled()) return CANCELLED;

            File imageFile = i2.next();
            if (!fileImageMap.containsKey(imageFile))
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
                  logger.warn("Unable to read images to create thumbnails"
                                 + exIO.getMessage());
                  return READ_ERROR;
               }

               ImagePixel	imp			= new ImagePixel(bdo);
               int			nCols			= imp.getColumns();
               int			nRows			= imp.getRows();
               short[]		imageData	= new short[nCols*nRows];
               ShortBuffer sbb			= ByteBuffer.wrap(imp.getPixelData())
                                                   .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                                                   .asShortBuffer()
                                                   .get(imageData);
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
                  float					sfx	= panel.getWidth() / (float) width;
                  float					sfy	= panel.getHeight() / (float) height;
                  float					sfMin	= Math.min(sfx, sfy);

                  AffineTransform	at		= AffineTransform.getScaleInstance(sfMin, sfMin);
                  AffineTransformOp	op		= new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

                  dest = op.createCompatibleDestImage(src, null);
                  dest = op.filter(src, dest);

                  fileImageMap.put(imageFile, dest);
                  publish(dest);
                  changed = true;
               }
               catch (ImageUtilitiesException exIMU)
               {
                  logger.warn("Unable to read images to create thumbnails"
                                 + exIMU.getMessage());
                  return READ_ERROR;
               }
            }
         }
      }

      if (changed) return CHANGED; else return UNCHANGED;
   }


   @Override
   protected void process(List<BufferedImage> lbi)
   {
      BufferedImage bi = lbi.get(lbi.size()-1);
      panel.setImage(bi);
      panel.repaint();
   }


   @Override
   protected Integer doInBackground() throws Exception
   {
      while (!isCancelled())
      {
         // The first time this expression is encountered, it will load the whole
         // set of images. Subsequently, it will simply add any images that have
         // been inserted into the map asynchronously by setFileList().
         Integer returnCode = loadImages();
         if (returnCode == CANCELLED)  return CANCELLED;
         if (returnCode == READ_ERROR)
            throw new Exception("Error reading images for thumbnail preview");

         Set<Map.Entry<File, BufferedImage>> mapEntries = fileImageMap.entrySet();

         if ((ime == null) || (returnCode == CHANGED) || !ime.hasNext())
            ime = mapEntries.iterator();

         // We now have an iterator, at either the start, or the position
         // that it got to at the end of the previous cycle round this loop.
         // The rest of the code simply displays the image corresponding to
         // the next file pointed to by the iterator, the waits a given delay
         // time. The time for which a single image is displayed is based on
         // the total number of images in the dataset, but is limited to a
         // specified maximum rate of change, so that other events on the
         //event dispatch loop have a chance to execute.
         float MAX_GAP = 1000.0f; // in ms
         float MIN_GAP = 100.0f;
         int   nImages = fileImageMap.size();
         float gap;
         if (nImages == 0) gap = MAX_GAP;
         else
         {
            gap = Math.max((MAX_GAP / nImages), MIN_GAP);
            BufferedImage nextImage = ime.next().getValue();
            publish(nextImage);
         }

         Thread.sleep((long) gap);
      }
      logger.debug("ThumbnailWorker exited");

      return CANCELLED;
   }

}
