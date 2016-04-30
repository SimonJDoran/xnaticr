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
* Java class: DICOMFileListWorker.java
* First created on April 21, 2011 at 9.04 AM
* 
* Retrieve a list of DICOM files onto the local machine
* corresponding to the data selected. If the user is interacting with
* a database on the local machine, then things are simple. However,
* if the database is somewhere on the Internet, then this worker
* downloads the data to a local cache asynchronously and reports back
* when the task is completed.
*********************************************************************/

package fileDownloads;

import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import org.netbeans.swing.outline.OutlineModel;
import org.w3c.dom.Document;
import treeTable.DAOOutline;
import xmlUtilities.XMLUtilities;
import xnatDAO.ThumbnailPreview;
import xnatDAO.XNATDAO;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;



public class DICOMFileListWorker extends FileListWorker
{
   public DICOMFileListWorker(XNATDAO              xndao,
                              DAOOutput            daoo,
                              XNATServerConnection xnsc,
                              DAOOutline           outline,
                              ThumbnailPreview     preview,
                              String               rootElement,
                              String               cacheDirName)
          throws IOException, XNATException
   {
      super(xndao, daoo, xnsc, outline, preview, rootElement, cacheDirName);
   }
   
   
   
	protected ArrayList<File> generateCacheFiles(int            tableRow,
			                                       Vector<String> tableColumnElements,
			                                       String         outputFormat)
			                    throws XNATException
	{
		return new ArrayList<File>();
	}
	

   protected int calculateNumberOfFilesToDownload(int nSelections, int firstRow,
                                                  Vector<String> tableColumnElements)
                 throws XNATException
   {
      // Initially, work this out for DICOM files from an XNAT MRScan element.
      // This will need extending after the initial feasibility is proved.
      int nFrames = 0;
      
      for (int i=0; i<nSelections; i++)
      {
         int          modelRow = outline.convertRowIndexToModel(firstRow + i);
         String       expXPath = rootElement + "/frames";
         int          expIndex = tableColumnElements.indexOf(expXPath);
         OutlineModel omdl     = outline.getOutlineModel();
         
         // Note expIndex+1: the first column of the table is the treenode column.
         try
         {
            nFrames  += new Integer((String) omdl.getValueAt(modelRow, expIndex+1));
         }
         catch (NumberFormatException exNF)
         {
            throw new XNATException(XNATException.DATA_NOT_PRESENT,
                                "The XNAT database does not contain the number\n"
                                + "of frames for this scan and I cannot load it.");
         }
      }
      return nFrames;
   }
   
   

   /**
    * Get the URL where we need to go to obtain the list of filenames.
    * For all root elements, we need to reconstruct an
    * initial portion of a REST URL of form /REST/experiments/ID.
    * After this, the final portion of the REST URL is dependent on what type
    * of data we are retrieving. The ID part comes directly from the tree table.
    * @param tableColumnElements
    * @param tableRow
    * @param isLocal
    * @return required URL as a String
    */
   protected String constructRESTCommand(Vector<String> tableColumnElements,
                                         int            tableRow,
                                         boolean        isLocal)
   {
      StringBuilder RESTCommand = new StringBuilder("/data/archive/experiments/");

      // Note that XNAT uses the terms "experiment" and "session" somewhat
      // interchangeably, although session is formally a subset of experiment.
      // The tree table is laid out with the column headings in the same order
      // as the tableColumnElements variable, so if we get an index from the
      // tableColumnElements, we can use it to find the right entry in the table.
      String expXPath = rootElement + "/image_session_ID";
      int    expIndex = tableColumnElements.indexOf(expXPath);

      // Note expIndex+1: the first column of the table is the treenode column.
      OutlineModel omdl    = outline.getOutlineModel();
      String       expID   = (String) omdl.getValueAt(tableRow, expIndex+1);
      
      String       IDXPath = rootElement + "/ID";
      int          IDIndex = tableColumnElements.indexOf(IDXPath);
      String       ID      = (String) omdl.getValueAt(tableRow, IDIndex+1);
           
      RESTCommand.append(expID).append("/scans/").append(ID);
      if (!isLocal) RESTCommand.append("/files");
      RESTCommand.append("?format=xml");

      logger.debug("tableRow = " + tableRow + "RESTCommand = " + RESTCommand.toString());
      return RESTCommand.toString();
   }


   
   protected ArrayList<File> getCacheList(String RESTCommand)
   {
      ArrayList<File> scanFileList = new ArrayList<File>();

      // When the files are remote, it is more problematic to retrieve just the
      // ones we want, as we have no access to the dcmCatalog file. Retrieve all
      // the filenames associated with the given experiment and rely on the
      // fact that correctly loaded DICOM images will have column 5 set to "DICOM".
      Vector2D resultSet;
      try
      {
         resultSet = (new XNATRESTToolkit(xnsc)).RESTGetResultSet(RESTCommand);
      }
      catch(XNATException exXNAT)
      {
         return null;
      }
      Vector<String> URI  = resultSet.getColumn(2);
      Vector<String> type = resultSet.getColumn(3);


      for (int i=0; i<URI.size(); i++)
      {
         if (type.elementAt(i).equals("DICOM"))
         {
            // Build the local cache filename where the data will be stored.
            // The directory structure is a bit long-winded, but should be
            // easy to manage.
            StringBuilder sb = new StringBuilder(cacheDirName);
            sb.append(URI.elementAt(i));
            File cacheFile = new File(sb.toString());
            File parent    = new File(cacheFile.getParent());

            // If the cache file does exist, but doesn't open correctly,
            // have another go at downloading it, on the basis that it
            // could have either become corrupted or a previous download
            // might have been interrupted, leaving an incomplete file.
            boolean success = true;
            if (!(cacheFile.exists() && preview.addFile(cacheFile, "DICOM")))
            {
               // Retrieve the actual data and store it in the cache.
               try
               {
                  parent.mkdirs();
                  BufferedOutputStream bos
                     = new BufferedOutputStream(new FileOutputStream(cacheFile, true));

                  BufferedInputStream  bis
                     = new BufferedInputStream(xnsc.doRESTGet(URI.elementAt(i)));

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
                                    
                  if (!preview.addFile(cacheFile, "DICOM"))
                  {
                     success = false;
                     nFileFailures++;
                  }                  

               }
               catch (Exception ex)
               {
                  logger.warn("Failed to download " + cacheFile.getName());
                  success = false;
                  nFileFailures++;
               }
            }

            if (success) scanFileList.add(cacheFile);
            nFilesDownloaded++;
            
            // Note: this is (DAOOutput.STOP_ICON - 1) not 100 because the values
            // 100 and 99 = DAOOutput.STOP_ICON is reserved for signalling
            // the START_ICON and STOP_ICON conditions.
            logger.debug("nFilesDownloaded " + nFilesDownloaded
                               + "  nFilesToDownload " + nFilesToDownload
                               + "  nFileFailures " + nFileFailures
                               + " progress: " + (DAOOutput.STOP_ICON - 1) * nFilesDownloaded / nFilesToDownload);
            setProgress((DAOOutput.STOP_ICON - 1) * nFilesDownloaded / nFilesToDownload);
            publishDownloadProgress(cacheFile.getName());
            
            if (isCancelled())
            {
               break;
            }            
         }
      }
      return scanFileList;
   }



   protected ArrayList<File> getLocalList(String RESTCommand)
   {
      ArrayList<File> scanFileList = new ArrayList<File>();

      // From the Document returned by RESTCommand, we can extract the
      // the location of the dcmCatalog file and this, in turn, tells us where
      // the actual DICOM data are.
      try
      {
         Document             doc          = xnsc.getDOMDocument(RESTCommand);
         XMLUtilities         xmlUtils     = new XMLUtilities();
         XNATNamespaceContext XNATns       = new XNATNamespaceContext();
         String[]             attrNames    = {"label", "URI"};

         String[][] parseResults
              = xmlUtils.getAttributes(doc, XNATns, "xnat:file", attrNames);

         File catalogFile = null;
         for (int k=0; k<parseResults.length; k++)
         {
            if (parseResults[k][0].equals("DICOM"))
               catalogFile = new File(parseResults[k][1]);
         }

         String          dirPath    = catalogFile.getParent();
         FileInputStream fis        = new FileInputStream(catalogFile);
         Document        catalogDoc = xmlUtils.getDOMDocument(fis);
         String[] fileNames
              = xmlUtils.getAttribute(catalogDoc, XNATns, "cat:entry", "URI");


         for (int k=0; k<fileNames.length; k++)
         {
            File imageFile = new File(dirPath + File.separator + fileNames[k]);
    
            if (preview.addFile(imageFile, "DICOM"))
               scanFileList.add(imageFile);
            
            else nFileFailures++; 
            
            nFilesDownloaded++;
            setProgress((DAOOutput.STOP_ICON - 1) * nFilesDownloaded / nFilesToDownload);
            publishDownloadProgress(fileNames[k]);
            
            if (isCancelled()) break;
         }
      }
      catch (Exception ex)
      {
         return null;
      }

      return scanFileList;
   }


   
}
