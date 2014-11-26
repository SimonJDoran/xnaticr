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
* Java class: DAOOutput.java
* First created on May 21, 2010 at 10:15 AM
* 
* Retrieve the location on the current filesystem of the resources
* required. There are two possibilities. Either the local machine is
* the XNAT server, in which case, the file list refers to the
* repository itself, or the data are downloaded into a local cache
* and the path to the cache is returned.
*********************************************************************/

package xnatDAO;

import exceptions.XNATException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.netbeans.swing.outline.Outline;
import treeTable.DAOOutline;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;

public class DAOOutput
{
   static    Logger                     logger = Logger.getLogger(DAOOutput.class);
   protected XNATDAO                    xndao;
   protected XNATServerConnection       xnsc;
   protected XNATRESTToolkit            xnrt;
   protected DAOOutline                 outline;
   protected ThumbnailPreview           thumbnailPreview;
   protected FileListWorker             fileListWorker;
   protected String                     invocationCircumstance;
   protected String                     currentStatus;
   protected String                     cacheDirName;
   protected ArrayList<ArrayList<File>> retrievedFiles;
   protected boolean                    iconCycling;
   public static final int              START_ICON = 100;
   public static final int              STOP_ICON  = 99;

   
   /**
    * Create a new DAOOutput object to manage the data retrieval.
    * @param xndao      the parent XNATDAO object
    * @param xnsc       the current XNAT database server connection
    * @param outline    the tree table component
    * @param thumbnailPreview    the thumbnail component where preview images are drawn
    * @param invocationCircumstance a string indicating under what circumstances the
    *                   DAOOutput object was created. This is necessary, because the
    *                   data retrieval is performed asynchronously. When the
    *                   FileListWorker thread completes, we need to know what to do
    *                   with the list of files returned.
    * @param cacheDirName the name of the directory containing the XNATDataChooser
    *                   file cache. This is set in the file ~/.XNAT_DAO/config/XNAT_DAO_config
    */
   public DAOOutput(XNATDAO              xndao,
                    XNATServerConnection xnsc,
                    DAOOutline           outline,
                    ThumbnailPreview     thumbnailPreview,
                    String               invocationCircumstance,
                    String               cacheDirName)
   {
      this.xndao                  = xndao;
      this.xnsc                   = xnsc;
      this.outline                = outline;
      this.thumbnailPreview       = thumbnailPreview;
      this.invocationCircumstance = invocationCircumstance;
      this.cacheDirName           = cacheDirName;

      xnrt                        = new XNATRESTToolkit(xnsc);
      currentStatus               = "Retrieving";
   }


   /**
    * Invoke a worker thread to retrieve a list of files corresponding to the
    * locations on the local machine of the selected data.
    *
    * @param  rootElement a String giving the XNAT XPath description of the type
    *         of resource being displayed (e.g., images,
    *         spectra, XML descriptors, ROI's, descriptors, etc.
    * @return the FileListWorker that does the retrieval
    * @throws IOException
    */
   public FileListWorker invoke(String rootElement)
          throws Exception
   {
      HashMap<String, Class> map = new HashMap<String, Class>();
      map.put("xnat:mrScanData",    DICOMFileListWorker.class);
      map.put("xnat:ctScanData",    DICOMFileListWorker.class);
      map.put("xnat:petScanData",   DICOMFileListWorker.class);
      map.put("icr:mriwOutputData", MRIWOutputFileListWorker.class);
//      map.put("icr:roiSetData",     ROIFileListWorker.class);
      map.put("icr:roiData",        ROIFileListWorker.class);
      
//      if ((rootElement.equals("xnat:mrScanData"))  ||
//          (rootElement.equals("xnat:ctScanData"))  ||
//          (rootElement.equals("xnat:petScanData")))
//              fileListWorker = new DICOMFileListWorker(xndao, this, xnsc, outline,
//                                      thumbnailPreview, rootElement, cacheDirName);
//
//      else if (rootElement.equals("icr:mriwOutputData"))
//              fileListWorker = new MRIWOutputFileListWorker(xndao, this, xnsc, outline,
//                                      thumbnailPreview, rootElement, cacheDirName);
      if (map.containsKey(rootElement))
      {
         try
         {
            Class c = map.get(rootElement);
 
            Constructor<FileListWorker> con = c.getConstructor(XNATDAO.class,
                                                         DAOOutput.class,
                                                         XNATServerConnection.class,
                                                         DAOOutline.class,
                                                         ThumbnailPreview.class,
                                                         String.class,
                                                         String.class);
            
            fileListWorker = con.newInstance(xndao, this, xnsc, outline,
                                  thumbnailPreview, rootElement, cacheDirName);
         }
         catch (Exception ex)
         {
            throw new UnsupportedOperationException("Couldn't instantiate the "
                    + "required class \nfor downloading data of type "
                    + rootElement + ".\nPlease contact Simon Doran.");
         }
      }
     
      
      else throw new UnsupportedOperationException("Output type " + rootElement + " not supported yet");
              
      // Add a progress monitor so that the progress bar and other UI elements
      // can be updated as each file is loaded. A zero or negative value of the 
      // property indicates a change in the table row downloading, which means
      // that we need to set the download icon appropriately and clear the
      // image preview area.
      logger.debug("\nCreated " + this.toString() + " with associated "
              + fileListWorker.toString() + ".");
      fileListWorker.revealDownloadArea();
      fileListWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public  void propertyChange(PropertyChangeEvent evt)
         {
            if ("progress".equals(evt.getPropertyName()))
            {
               int progVal = (Integer)evt.getNewValue();
               logger.debug("\n\npropertyChangeEvent value = " + progVal
                       + " sent by " + evt.getSource().toString());
               if (progVal == START_ICON)
               {
                  thumbnailPreview.clearImages();
                  fileListWorker.startIcon();
                  iconCycling = true;
                  return;
               }
               if ((progVal == STOP_ICON) && (!currentStatus.equals("Download cancelled")))
               {
                  logger.debug("Calling stopIcon");
                  fileListWorker.stopIcon();
                  iconCycling = false;
               }
               
               // Note: the 100/(STOP_ICON - 1) is because 100 (START_ICON) 
               // and 99 (STOP_ICON) are used to flag conditions, rather
               // than report incremental progress.
               else xndao.getDownloadJProgressBar()
                       .setValue(progVal*100/(STOP_ICON - 1));
            }
                 
         }
      });
         
      fileListWorker.execute();
      return fileListWorker;
   }
 
   
   public boolean isIconCycling()
   {
      return iconCycling;
   }


   public void cancel(boolean canInterrupt)
   {
      if (fileListWorker != null)
      {
         fileListWorker.cancel(canInterrupt);
      }
   }


   /**
    * Since the list of files is prepared asynchronously and the execute()
    * method returns void, we need to know what to do with data
    * when we get it. The FileListWorker, which is the object that is actually
    * doing the "getting" has a reference to this DAOOutput object and will call
    * the distributeList method when it has retrieved the list of files.
    * @param fileList
    */
   public void distributeList(ArrayList<ArrayList<File>> fileList)
   {
      currentStatus = "Download succeeded - awaiting selection by user";
      retrievedFiles = fileList;
      
      if (fileListWorker.nFileFailures != 0)
      {
         currentStatus = fileListWorker.nFileFailures
                 + " files failed to download correctly or are otherwise not valid";
         JOptionPane.showMessageDialog(
            xndao,
            fileListWorker.nFileFailures + " files failed to download correctly from XNAT\n" +
            "or were not valid when downloaded.",
            "File download error", JOptionPane.ERROR_MESSAGE);
      }

      if (fileList == null)
      {
         JOptionPane.showMessageDialog(xndao,
                                       "Failed to retrieve DICOM file\n" +
                                       "location from XNAT",
                                       "I/O error", JOptionPane.ERROR_MESSAGE);
         currentStatus = "Failed";
         xndao.getDownloadingJLabel().setVisible(false);
         xndao.getDownloadDetailsJLabel().setVisible(false);
         xndao.getDownloadJProgressBar().setVisible(false);
      }
      
      
      if (invocationCircumstance.equals("Return list to calling app"))
      {
         //          xndao.dispose();
      }


      if (invocationCircumstance.equals("TreeTable selection changed"))
      {
         xndao.setSelectDataJButtonEnabled(true);        
      }
   }

   public void interrupted()
   {
      currentStatus = "Download interrupted";
   }
   
   
   public void cancelled()
   {
      currentStatus = "Download cancelled";
      
      // Now stop the running icon and hide the download area.
      fileListWorker.stopIcon();
      //fileListWorker.hideDownloadArea();
   }
   
   
   public String getStatus()
   {
      return currentStatus;
   }


   public ArrayList<ArrayList<File>> getFileList()
   {
      return retrievedFiles;
   }
}
