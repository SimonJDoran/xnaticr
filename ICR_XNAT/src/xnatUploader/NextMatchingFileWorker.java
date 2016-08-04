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
* Java class: NextMatchingFileWorker.java
* First created on May 19, 2011 at 09.25 AM
* 
* Wrapper routine to allow a potentially long directory search to be
* conducted in a worker thread. Recursively descend the directory
* hierarchy scanning for the next file that is parsed successfully by
* a given DataUploader. For use with xnatUploader package.
*********************************************************************/

package xnatUploader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import xnatUploader.DataUploader;
import static xnatUploader.DataUploader.logger;
import static xnatUploader.XNATUploader.logger;


public class NextMatchingFileWorker extends SwingWorker<File, Void>
{
   static  Logger logger = Logger.getLogger(NextMatchingFileWorker.class);
   private File   contextRoot;
   private File   previousSearchProgress;
   
   // Note: At present, the file match that we are talking about here
   // corresponds to a file that the supplied data uploader is able to parse
   // successfully, i.e., uploader.parse() will return true. However, the 
   // concept is obviously much broader and I may return to this to
   // make a more general function.
   private DataUploader uploader;
   
   /**
    * Create a new instance of the NextMatchingFileWorker.
    * @param contextRoot a File of type directory, containing the starting level
    * in the directory hierarchy for the whole search procedure
    * @param previousSearchProgress a File (not directory) containing the last
    * result from a similar search. The current search will start its scan from
    * this point. The caller should save the result of doInBackground(), as 
    * retrieved by get(), and pass it back in when the next scan needs to take
    * place.
    */
   public NextMatchingFileWorker(File contextRoot,
                                  File previousSearchProgress,
                                  DataUploader uploader)
   {
      this.contextRoot            = contextRoot;
      this.previousSearchProgress = previousSearchProgress;
      this.uploader               = uploader;
   }
   
   
   @Override
   protected File doInBackground() throws Exception
   {
      // Create an array of all directories from the contextRoot down to the
      // previous search level.
      ArrayList<File> directories = fillDirectoryHierarchy();
      if (directories == null) return null;
      
      File current;
      if (!previousSearchProgress.equals(contextRoot))
         current = getNextSibling(previousSearchProgress);
      else
         current = getFirstChild(contextRoot);
      
      boolean finished = false;
      do
      {         
         if (current == null)
         {
            // Go up a directory level.
            current = getNextSibling(directories.get(0));
            directories.remove(0);
            if (directories.isEmpty()) finished = true;
         }
         
         else if (current.isDirectory())
         {
            // Go down a directory level.
            directories.add(0, current);
            current = getFirstChild(current);            
         }
         else if (current.isFile())
         {
            // Check whether file satisfies criteria to return.
            logger.debug("Checking " + current.getPath());
            uploader.setUploadFile(current);
            uploader.prepareUpload();
            if (uploader.isPreparedForUpload()) return current;
            logger.warn(current.getPath() + " : file unsuitable for upload");
            
            // A crucial point here is that, if the file fails the test,
            // the uploader.prepareUpload() call might have set some variables
            // in the uploader instance. Hence, we get a fresh copy, whilst
            // remembering to copy any variables that we *do* want to preserve.
            try
            {
               uploader = uploader.getFreshCopyForBatchUpload();
            }
            catch (InstantiationException   | IllegalAccessException |
                   IllegalArgumentException | InvocationTargetException ex)
            {
               logger.error(ex.getMessage());
               throw new Exception(ex.getMessage());
            }
            
            current = getNextSibling(current);
         }            
      }
      
      while (!finished);
      
      return null;
   }
   
   
   
   protected ArrayList<File> fillDirectoryHierarchy()
   {
      ArrayList<File> directories = new ArrayList<File>();
         
      File parent;
      if (previousSearchProgress.isFile())
         parent = previousSearchProgress.getParentFile();
      else
         parent = previousSearchProgress;
      
      if (parent == null)
      {
         logger.error("Programming error: Incorrect input of previousSearchProgress \n"
                      + "to NextMatchingFileScanner");
         return null;
      }
      
      directories.add(parent);
      
      while (!parent.equals(contextRoot))
      {
         parent = parent.getParentFile();
         if (parent == null)
         {
            logger.error("Programming error: Incorrect input of previousSearchProgress \n"
                      + "to NextMatchingFileScanner. Upward hierarchy is not rooted in \n"
                      + "the directory specified by contextRoot");
            return null;
         }
         directories.add(parent);
      }
      
      
      return directories;
   }
   
   
   protected File getNextSibling(File currentPosition)
   {
      File[] siblings;
      
      siblings  = currentPosition.getParentFile().listFiles();
      int index = -1;
      for (int i=0; i<siblings.length; i++)
      {
         if (siblings[i].equals(currentPosition))
         {
            index = i;
            break;
         }
      }
      
      if (index == -1)
      {
         logger.error("This really shouldn't happen!");
         return null;
      }
      
      if (index == siblings.length-1) return null;
      else return siblings[index+1];
   }
   
   
   protected File getFirstChild(File currentPosition)
   {
      File[] children = currentPosition.listFiles();
      
      // Children will be null either if currentPosition is a regular file,
      // or if the filesystem security stops access to the file.
      if (children == null)     return null;
      if (children.length == 0) return null;
      
      return children[0];
   }
}
