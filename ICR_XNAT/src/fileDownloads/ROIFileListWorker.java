/*******************************************************************
* Copyright (c) 2014, Institute of Cancer Research
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

/********************************************************************
* @author Simon J Doran
* Java class: ROIFileListWorker.java
* First created on May 23, 2013 at 4:36:28 PM
*
* Retrieve a list of region-of-interest files to the local machine
* corresponding to the data selected. If the user is interacting with
* a database on the local machine, then things are simple. However,
* if the database is somewhere on the Internet, then this worker
* downloads the data to a local cache asynchronously and reports back
* when the task is completed.
*********************************************************************/

package fileDownloads;

import fileDownloads.FileListWorker;
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
import static fileDownloads.FileListWorker.logger;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;

public class ROIFileListWorker //extends FileListWorker
{
   public ROIFileListWorker(XNATDAO              xndao,
                                  DAOOutput            daoo,
                                  XNATServerConnection xnsc,
                                  DAOOutline           outline,
                                  ThumbnailPreview     preview,
                                  String               rootElement,
                                  String               cacheDirName)
          throws IOException, XNATException
   {
     // super(xndao, daoo, xnsc, outline, preview, rootElement, cacheDirName);
   }


//   @Override
//   protected int calculateNumberOfFilesToDownload(int nSelections, int firstRow,
//                                                  Vector<String> tableColumnElements)
//   {
//      // We need to download the defining structure set and the thumbnail
//		// images.
//		int nFiles = 0;
//		
//		for (int i=0; i<nSelections; i++)
//      {
//         int          modelRow = outline.convertRowIndexToModel(firstRow + i);
//         String       expXPath = rootElement + "/";
//         int          expIndex = tableColumnElements.indexOf(expXPath);
//         OutlineModel omdl     = outline.getOutlineModel();
//         
//         // Note expIndex+1: the first column of the table is the treenode column.
////         try
////         {
////            nFrames  += new Integer((String) omdl.getValueAt(modelRow, expIndex+1));
////         }
////         catch (NumberFormatException exNF)
////         {
////            throw new XNATException(XNATException.DATA_NOT_PRESENT,
////                                "The XNAT database does not contain the number\n"
////                                + "of frames for this scan and I cannot load it.");
////         }
//      }
//      return nFiles;
//   }
//
//
//   /**
//    * Get the URL where we need to go to obtain the list of filenames.
//    * For all root elements, we need to reconstruct an
//    * initial portion of a REST URL of form /REST/experiments/ID.
//    * After this, the final portion of the REST URL is dependent on what type
//    * of data we are retrieving. The ID part comes directly from the tree table.
//    * @param tableColumnElements
//    * @param tableRow
//    * @param isLocal
//    * @return required URL as a String
//    */
//   @Override
//   protected String constructRESTCommand(Vector<String> tableColumnElements,
//                                         int            tableRow,
//                                         boolean        isLocal)
//   {
//      StringBuilder RESTCommand = new StringBuilder("/data/archive/experiments/");
//
//      // Note that XNAT uses the terms "experiment" and "session" somewhat
//      // interchangeably, although session is formally a subset of experiment.
//      // The tree table is laid out with the column headings in the same order
//      // as the tableColumnElements variable, so if we get an index from the
//      // tableColumnElements, we can use it to find the right entry in the table.
//      String expXPath = rootElement + "/imageSession_ID";
//      int    expIndex = tableColumnElements.indexOf(expXPath);
//
//      // Note expIndex+1: the first column of the table is the treenode column.
//      OutlineModel omdl  = outline.getOutlineModel();
//      String       expID = (String) omdl.getValueAt(tableRow, expIndex+1);
//
//      String IDXPath = rootElement + "/ID";
//      int    IDIndex = tableColumnElements.indexOf(IDXPath);
//      String ID      = (String) omdl.getValueAt(tableRow, IDIndex+1);
//
//      RESTCommand.append(expID).append("/assessors/").append(ID);
//      if (!isLocal) RESTCommand.append("/files");
//      RESTCommand.append("?format=xml");
//
//      return RESTCommand.toString();
//   }
//
//   @Override
//   protected ArrayList<File> getCacheList(String RESTCommand)
//   {
//      ArrayList<File> scanFileList = new ArrayList<File>();
//
//      // There are two types of file associated with the icr:roiData assessor:
//		// the ROIs themselves, served out as an RT-STRUCT file, generated at the
//		// time of download and the image thumbnails generated at the time of upload.
//		// Only the RT-STRUCT is returned and the thumbnails are loaded into the preview.
//      Vector2D resultSet;
//      try
//      {
//         resultSet = (new XNATRESTToolkit(xnsc)).RESTGetResultSet(RESTCommand);
//      }
//      catch(XNATException exXNAT)
//      {
//         return null;
//      }
//      Vector<String> URI  = resultSet.getColumn(2);
//
//      for (int i=0; i<URI.size(); i++)
//      {
//			// Build the local cache filename where the data will be stored.
//			// The directory structure is a bit long-winded, but should be
//			// easy to manage.
//			StringBuilder sb = new StringBuilder(cacheDirName);
//			sb.append(URI.elementAt(i));
//			File cacheFile = new File(sb.toString());
//			File parent    = new File(cacheFile.getParent());
//
//			if (!cacheFile.exists())
//			{
//				// Retrieve the preview data and store it in the cache.
//				try
//				{
//					parent.mkdirs();
//					BufferedOutputStream bos
//						= new BufferedOutputStream(new FileOutputStream(cacheFile, true));
//
//					BufferedInputStream  bis
//						= new BufferedInputStream(xnsc.doRESTGet(URI.elementAt(i)));
//
//					byte[] buf = new byte[8192];
//
//					while (true)
//					{
//						int length = bis.read(buf);
//						if (length < 0) break;
//						bos.write(buf, 0, length);
//					}
//
//					try{bis.close();}
//					catch (IOException ignore) {;}
//
//					try{bos.close();}
//					catch (IOException ignore) {;}
//				}
//				catch (Exception ex)
//				{
//					return null;
//				}
//				preview.addFile(cacheFile, "PNG");
//			}
//			
//			// Now retrieve the associated RT-STRUCT file and create a subset
//			// RT-STRUCT dynamically with the data for the ROIs to be downloaded.
//
////			scanFileList.add(cacheFile);
////			logger.debug("Downloaded " + cacheFile.toString());
////
////			nFilesDownloaded++;
////			setProgress(100 * nFilesDownloaded / nFilesToDownload);
////			publishDownloadProgress(cacheFile.getName());
////         }
//      }
//      return scanFileList;
//   }
//
//
//
//   protected ArrayList<File> getLocalList(String RESTCommand)
//   {
//      ArrayList<File> ROIFileList = new ArrayList<File>();
//
//      // From the Document returned by RESTCommand, we can extract the
//      // the location of the dcmCatalog file and this, in turn, tells us where
//      // the actual DICOM data are.
//      try
//      {
//         Document             doc          = xnsc.getDOMDocument(RESTCommand);
//         XMLUtilities         xmlUtils     = new XMLUtilities();
//         XNATNamespaceContext XNATns       = new XNATNamespaceContext();
//         String[]             attrNames    = {"label", "URI"};
//
//         String[][] parseResults
//              = xmlUtils.getAttributes(doc, XNATns, "xnat:file", attrNames);
//
//         File catalogFile = null;
//         for (int k=0; k<parseResults.length; k++)
//         {
//            if (parseResults[k][0].equals("DICOM_RT-STRUCT"))
//               catalogFile = new File(parseResults[k][1]);
//         }
//
//         String          dirPath    = catalogFile.getParent();
//         FileInputStream fis        = new FileInputStream(catalogFile);
//         Document        catalogDoc = xmlUtils.getDOMDocument(fis);
//         String[] fileNames
//              = xmlUtils.getAttribute(catalogDoc, XNATns, "cat:entry", "URI");
//
//
//         for (int k=0; k<fileNames.length; k++)
//         {
//            File ROIFile = new File(dirPath + File.separator + fileNames[k]);
//            ROIFileList.add(ROIFile);
//            preview.addFile(ROIFile, "XML");
//            nFilesDownloaded++;
//            setProgress(100 * nFilesDownloaded / nFilesToDownload);
//            publishDownloadProgress(fileNames[k]);
//         }
//      }
//      catch (Exception ex)
//      {
//         return null;
//      }
//      return ROIFileList;
//   }

}
