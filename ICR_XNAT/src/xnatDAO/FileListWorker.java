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
* Java class: FileListWorker.java
* First created on June 14, 2010 at 22.57 PM
* 
* Retrieve a list of files on the local machine
* corresponding to the data selected. If the user is interacting with
* a database on the local machine, then things are simple. However,
* if the database is somewhere on the Internet, then this worker
* downloads the data to a local cache asynchronously and reports back
* when the task is completed.
*********************************************************************/

package xnatDAO;

import exceptions.XNATException;
import generalUtilities.Vector2D;
import imageUtilities.DownloadIcon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.netbeans.swing.outline.OutlineModel;
import treeTable.DAOMutableTreeNode;
import treeTable.DAOOutline;
import treeTable.DAOTreeNodeUserObject;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;


public abstract class FileListWorker extends SwingWorker<ArrayList<ArrayList<File>>, String>
{
   static    Logger               logger = Logger.getLogger(FileListWorker.class);
   protected XNATDAO              xndao;
   protected DAOOutput            daoo;
   protected XNATServerConnection xnsc;
   protected DAOOutline           outline;
   protected ThumbnailPreview     preview;
   protected String               rootElement;
   protected String               cacheDirName;
   protected DownloadIcon         icon;
   protected boolean              iconCycling;
   protected int                  downloadIconRow;
   protected int                  nFilesToDownload;
   protected int                  nFilesDownloaded;
   protected int                  nFileFailures;


   
   /**
    * Create a worker thread to return a list of files corresponding to the resources
    * selected in the XNAT_DAO tree table.
    * @param xnsc         the currently active XNATServerConnection
    * @param outline      the Outline treetable component on the main interface
    * @param preview      the ThumbnailPreview window on the interface
    * @param rootElement  the data type returned by the database search
    * @throws IOException
    */
   public FileListWorker(XNATDAO              xndao,
                         DAOOutput            daoo,
                         XNATServerConnection xnsc,
                         DAOOutline           outline,
                         ThumbnailPreview     preview,
                         String               rootElement,
                         String               cacheDirName)
          throws IOException, XNATException
   {
      this.xndao        = xndao;
      this.daoo         = daoo;
      this.xnsc         = xnsc;
      this.outline      = outline;
      this.preview      = preview;
      this.rootElement  = rootElement;
      this.cacheDirName = cacheDirName;
   }

   
   
   @Override
   protected ArrayList<ArrayList<File>> doInBackground() throws Exception
   {
      XNATRESTToolkit          xnrt    = new XNATRESTToolkit(xnsc);
      DAOSearchableElementList sel     = null;
      boolean                  isLocal = xnsc.getServerURL().getHost().equals("localhost");

      try {sel = DAOSearchableElementList.getSingleton();}
      catch (IOException exIO){throw exIO;}

     // LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATAliases();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);

      // The treeTable contains a list of all the possible parameters that can
      // be returned for the particular root element that we are interested in
      // (although not all are shown by default).
      int firstRow = outline.getSelectionModel().getMinSelectionIndex();
      int lastRow  = outline.getSelectionModel().getMaxSelectionIndex();
      int nSets    = lastRow - firstRow + 1;
      
      nFilesToDownload = calculateNumberOfFilesToDownload(nSets, firstRow, tableColumnElements);
      nFilesDownloaded = 0;
      nFileFailures    = 0;

      ArrayList<ArrayList<File>> outputFileList = new ArrayList<ArrayList<File>>();

      for (int j=0; j<nSets; j++)
      {
         // While the selection is being retrieved (potentially from a remote
         // database) change the icon to indicate a download. We send a signal
         // to the Event Dispatch Thread, then wait using Thread.sleep(100) for
         // the EDT to start the icon. Potentially bad practice, but a good
         // solution here.
         iconCycling     = false;
         downloadIconRow = j;
         setProgress(DAOOutput.START_ICON);
         while (!iconCycling)
         {
            Thread.sleep(100);
         }
         
         // Note that, if the column has been sorted, then we need to retrieve
         // the correct row in the original model here.
         int viewRow  = firstRow + j;
         int modelRow = outline.convertRowIndexToModel(viewRow);
         logger.debug("Row selected in view = " + viewRow + "  Model row = " + modelRow);
         
         String RESTCommand = constructRESTCommand(tableColumnElements,
                                                   modelRow, isLocal);

         ArrayList<File> fileList;
         if (isLocal) fileList = getLocalList(RESTCommand);
         else         fileList = getCacheList(RESTCommand);
         
         // Send stop signal to icon and wait for it to stop.
         setProgress(DAOOutput.STOP_ICON);
         while (iconCycling)
         {
            Thread.sleep(100);
         }

         outputFileList.add(fileList);
      }

      return outputFileList;
   }


   @Override
   protected void done()
   {
      hideDownloadArea();
      
      try
      {
         daoo.distributeList(get());
      }
      catch (InterruptedException exIE)
      {
         daoo.interrupted();
      }
      catch (CancellationException exCA)
      {
         daoo.cancelled();
      }
      catch (ExecutionException exEE)
      {
         JOptionPane.showMessageDialog(xndao,
              "I was unable to retrieve the local filenames for the  \n"
                  + "data that you selected for the following reason:\n"
                  + exEE.getMessage(),
              "Failed to retrieve filenames",
              JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   
   protected void publishDownloadProgress(String filename)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(nFilesDownloaded);
      sb.append("/");
      sb.append(nFilesToDownload);
      sb.append("/");
      sb.append(nFileFailures);
      sb.append(" ");
         
      int len = filename.length();
      if (len < 17) sb.append(filename);
      else
      {
         sb.append(filename.substring(0, 10));
         sb.append("...");
         sb.append(filename.substring(len-2));
      }
      publish(sb.toString());
   }
   
   
   @Override
   protected void process(List<String> listToProcess)
   {
      String lastElement = listToProcess.get(listToProcess.size()-1);
      xndao.getDownloadDetailsJLabel().setText(lastElement);
   }
   
   
   // Note that this needs to be executed on the Event Dispatch Thread.
   public void revealDownloadArea()
   {     
      xndao.getDownloadingJLabel().setVisible(true);
      xndao.getDownloadDetailsJLabel().setVisible(true);
      xndao.getDownloadJProgressBar().setVisible(true);
      xndao.getDownloadJProgressBar().setValue(0);
   }
   
   
   // Note that this needs to be executed on the Event Dispatch Thread.
   public void hideDownloadArea()
   {
      xndao.getDownloadingJLabel().setVisible(false);
      xndao.getDownloadDetailsJLabel().setVisible(false);
      xndao.getDownloadJProgressBar().setVisible(false);
   }
   
   

   /**
    * Start the download leaf icon. Note that this method must be called
    * on the event dispatch thread in response to a propertyChangeEvent with
    * value DAOOutput.START_ICON.
    */
   public void startIcon()
   {
      logger.debug("\n\nstartIcon()\nWorker ID " + this.toString() + " downloadIconRow = " + downloadIconRow);
      int                   firstRow  = outline.getSelectionModel().getMinSelectionIndex();
      DAOMutableTreeNode    treeNode  = (DAOMutableTreeNode) outline.getValueAt(
                                                        firstRow+downloadIconRow, 0);
      DAOTreeNodeUserObject treeObj   = treeNode.getUserObject();
      
      icon = (DownloadIcon) treeObj.getIcon();
      icon.setRepaintComponent(outline);
      icon.start();
      iconCycling = true;
   }
   
   
   /**
    * Stop the download leaf icon. Note that this method must be called
    * on the event dispatch thread  in response to a propertyChangeEvent with
    * value DAOOutput.START_ICON.
    */
   public void stopIcon()
   {
      logger.debug("\n\nstopIcon()\nWorkerID = " + this.toString() + " Icon ID = "
              + ((icon == null) ? "icon is null" : icon.toString()));

      if (icon != null)
      {
         icon.stop();
         iconCycling = false;
      }
   }
   
   
   /**
    * Returns the string corresponding to the URL where we need to go to obtain
    * the list of filenames. For all root elements, we need to reconstruct an
    * initial portion of a REST URL of form /REST/experiments/ID.
    * After this, the final portion of the REST URL is dependent on what type
    * of data we are retrieving and will be provided by the concrete class.
    * @param tableColumnElements
    * @param tableRow
    * @param isLocal
    * @return required URL as a String
    */
   protected abstract String constructRESTCommand(Vector<String> tableColumnElements,
                                                  int            tableRow,
                                                  boolean        isLocal);
   
   
   
   protected abstract ArrayList<File> getCacheList(String RESTCommand);
   
   
   protected abstract ArrayList<File> getLocalList(String RESTURL);
   
   
   protected abstract int calculateNumberOfFilesToDownload(int nSets, int firstRow,
                                                Vector<String> tableColumnElements)
                          throws XNATException;
}
