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

package fileListWorkers;

import configurationLists.DAOOutputDefinitionsList;
import configurationLists.DAOOutputFormatsList;
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
import xnatDAO.DAOOutput;
import configurationLists.DAOSearchableElementsList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import xnatDAO.ThumbnailPreview;
import xnatDAO.XNATDAO;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;


public class FileListWorker extends SwingWorker<ArrayList<ArrayList<File>>, String>
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
	protected int                  nFilesToGenerate;
	protected int                  nFilesToOutput;
   
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
		Map od = getOutputDefinition();
		downloadResources(od);
		
		ArrayList<ArrayList<File>> outputFileList = performActions(od);
		
		return outputFileList;
	}
	
	protected Map<String, String> getOutputDefinition() throws IOException
	{
		DAOOutputFormatsList      ofl    = null;
		DAOOutputDefinitionsList  odl    = null;
		
		try
		{
			ofl = DAOOutputFormatsList.getSingleton();
			odl = DAOOutputDefinitionsList.getSingleton();
		}
      catch (IOException exIO){throw exIO;}
		
		String formatAlias  = (String) xndao.getOutputFormatJComboBox().getSelectedItem();
		String formatChosen = ofl.getMap()
											.get(xndao.dataSubtypeAlias)
												.get(formatAlias);
		
		return odl.getMap().get(formatChosen);
	}
	
	
	protected void downloadThumbnails(Map od)
	{

		
	}
	
	
	protected void downloadResources(Map<String, String> od) throws InterruptedException
	{
		int firstRow    = outline.getSelectionModel().getMinSelectionIndex();
      int lastRow     = outline.getSelectionModel().getMaxSelectionIndex();
      int nSelections = lastRow - firstRow + 1;
		
      for (int j=0; j<nSelections; j++)
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
            try{Thread.sleep(100);}
				catch (InterruptedException exIE) {throw exIE;}
         }
         
         // Note that, if the column has been sorted, then we need to retrieve
         // the correct row in the original model here.
         int viewRow  = firstRow + j;
         int modelRow = outline.convertRowIndexToModel(viewRow);
         logger.debug("Row selected in view = " + viewRow + "  Model row = " + modelRow);
			
			if (!od.get("resourceName").equals(od.get("thumbnailResourceName")))
			{
				ArrayList<File> thumbnailList = downloadResource("thumbnail", od, modelRow);
			}
			
			ArrayList<File> sourceList = downloadResource("source", od);
         
         ArrayList<File> fileList = generateCacheFiles(modelRow, tableColumnElements);
         
         // Send stop signal to icon and wait for it to stop.
         setProgress(DAOOutput.STOP_ICON);
         while (iconCycling)
         {
            Thread.sleep(100);
         }

         outputFileList.add(fileList);
      }
		// If the thumbnails are calculated on-the-fly from the source data
		// then update the thumbnails in parallel with the main download.
		// Otherwise, preload the thumbnails.
		if (!od.get("thumbnailResourceName").equals(od.get("resourceName")))
			downloadThumbnails(od);
		
	}
	
	protected int getNumberOfFilesForResourceSet(String type, Map<String, String> od, int modelRow)
			        throws IOException, XNATException
	{
		String restPrefix = constructRestPrefix(type, od, modelRow);

		// Check for multiple elements in the name and format entries.
		HashMap<String, String> nameFormat       = getNameAndFormatMap(type, od, modelRow);
		Set<String>             resourceNames    = nameFormat.keySet();
		ArrayList<String>       filesForResource = new ArrayList<>();
		
		int n = 0;
		for (String resourceName : resourceNames)
		{		
			 filesForResource = getFilenamesForResource(resourceName, restPrefix);
			 n += filesForResource.size();
		}
		return n;
	}
	
	/**
	 * Download a resource set specified in the output definition map supplied.
	 * The use of the term "resource set" rather than resource indicates that the
	 * resource in a single line of the output definition may span more than a 
	 * single resource (e.g., the entry "DICOM|NIFTI") which means that the data
	 * in the repository for the chosen list of experiments might be stored in
	 * either format.
	 * @param type either "primary" or "thumbnail" depending on what this resource
	 *             is used for by the downloader
	 * @param od   the output definition
	 * @param modelRow the row of the table in the UI currently being worked on
	 * @return an ArrayList of cache files containing the data downloaded
	 */
	protected ArrayList<File> downloadResourceSet(String type, Map<String, String> od, int modelRow)
			    throws IOException, XNATException
	{
		String restPrefix = constructRestPrefix(type, od, modelRow);

		// Check for multiple elements in the name and format entries.
		HashMap<String, String> nameFormat     = getNameAndFormatMap(type, od, modelRow);
		Set<String>             resourceNames  = nameFormat.keySet();
		ArrayList<File>         filesRetrieved = new ArrayList<>();
		
		for (String resourceName : resourceNames)
		{
			String format = nameFormat.get(resourceName);
			
			ArrayList<String> filenames = getFilenamesForResource(resourceName, restPrefix);
			for (String fname : filenames)
			{
				File    retrieved = retrieveFileToCache(fname);
				boolean success   = false;
				if (retrieved != null)
				{
					if (resourceName.equals(od.get("thumbnailName")))
					{
						// If this is a thumbnail type, then we have a further check,
						// over and above simply whether any file data were retrieved,
						// to tell us whether the download was successful.
						success = preview.addFile(retrieved, format);
					}
					else
					{
						success = true;
					}
				}
				if (success) filesRetrieved.add(retrieved);
			}
		}
		return filesRetrieved;
	}

	
	protected File retrieveFileToCache(String URI)
	{
		// Build the local cache filename where the data will be stored.
		// The directory structure is a bit long-winded, but should be
		// easy to manage.
		File                 cacheFile = new File(cacheDirName + URI);
		File                 parent    = new File(cacheFile.getParent());
		BufferedInputStream  bis       = null;
		BufferedOutputStream bos       = null;
		
		if (!cacheFile.exists())
		{
			// Retrieve the actual data and store it in the cache.
			try
			{
				parent.mkdirs();
				bos = new BufferedOutputStream(new FileOutputStream(cacheFile, true));
				bis = new BufferedInputStream(xnsc.doRESTGet(URI));

				byte[] buf = new byte[8192];

				while (true)
				{
					int length = bis.read(buf);
					if (length < 0) break;
					bos.write(buf, 0, length);
				}

				logger.debug("Worker ID = " + this.toString() + " Downloaded " + cacheFile.toString());                                
			}
			catch (Exception ex)
			{
				logger.warn("Failed to download " + cacheFile.getName());
				return null;
			}
			finally
			{				
				try{bis.close();}
				catch (IOException | NullPointerException ignore) {}

				try{bos.close();}
				catch (IOException | NullPointerException ignore) {}  
			}
		}
		
		return cacheFile;
	}
	
	
	
	protected ArrayList<String> getFilenamesForResource(String resourceName, String restPrefix)
			                      throws XNATException
	{
		String restCommand = restPrefix + "/" + resourceName + "/files";
		Vector2D resultSet;
		try
		{
			resultSet = (new XNATRESTToolkit(xnsc)).RESTGetResultSet(restCommand);
		}
		catch(XNATException exXNAT)
		{
			logger.warn("Problem retrieving list of files in resource"
					  + resourceName + "from XNAT");
			throw new XNATException(XNATException.RETRIEVING_LIST);
		}
		
		return new ArrayList<String>(resultSet.getColumn(1));
	}
	
	
	
	protected HashMap<String, String> getNameAndFormatMap(String type, Map<String, String> od, int modelRow)
			                            throws XNATException, IOException
	{
		// First check the formats specified in the XML defining the output type.
		String            names     = od.get(type + "Name");
		ArrayList<String> namesList = new ArrayList<>();
		int               pos       = 0;
		for (int i=0; i<names.length(); i++)
		{
			if (names.charAt(i) == '|')
			{
				namesList.add(names.substring(pos, i));
				pos = i+1;
			}
		}
		namesList.add(names.substring(pos));
		
		
		String            formats     = od.get(type + "Format");
		ArrayList<String> formatsList = new ArrayList<>();
		pos = 0;
		for (int i=0; i<names.length(); i++)
		{
			if (formats.charAt(i) == '|')
			{
				formatsList.add(formats.substring(pos, i));
				pos = i+1;
			}
		}
		formatsList.add(formats.substring(pos));

		if (formatsList.size() != namesList.size())
		{
			throw new RuntimeException("Programming error: file DAO_outputDefinitions.xml"
			                           + " is incorrectly specified.");
		}
		
		HashMap<String, String> nameFormatMap = new HashMap<>();
		
		// Return only those resources that are present in the archive.
		String restPrefix  = constructRestPrefix(type, od, modelRow);
		String restCommand = restPrefix + "?format=xml";
		
		Vector2D resultSet;
      try
      {
         resultSet = (new XNATRESTToolkit(xnsc)).RESTGetResultSet(restCommand);
      }
      catch(XNATException exXNAT)
      {
         logger.warn("Problem retrieving list of resources from XNAT");
			return nameFormatMap;  // Return an empty result if there is an error.
      }
		
		ArrayList<String> namesFromXNAT = new ArrayList<String>(resultSet.getColumn(1));
		
		for (int i=0; i<namesList.size(); i++)
		{
			if (namesFromXNAT.contains(namesList.get(i)))
					  nameFormatMap.put(namesList.get(i), formatsList.get(i));
		}
		
		return nameFormatMap;
	
	}

	
	/**
	 * Build up the REST command from the information in the table by
	 * extracting the fields specified by the template.
	 * @param type
	 * @param od
	 * @param modelRow
	 * @return a String containing the REST command needed to access the files contained
	 * in the resource being extracted
	 * @throws IOException 
	 */
	protected String constructRestPrefix(String type, Map<String, String> od, int modelRow)
			    throws IOException
	{
		DAOSearchableElementsList sel = DAOSearchableElementsList.getSingleton();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);

		StringBuilder restPrefix = new StringBuilder(od.get("restTemplate"));
		
		ArrayList<Integer> occurrences = new ArrayList<>();
		for (int i=0; i<restPrefix.length(); i++)
		{
			if (restPrefix.charAt(i) == '$') occurrences.add(i);
		}
		
      OutlineModel omdl    = outline.getOutlineModel();
		for (int i=0; i<occurrences.size(); i++)
		{
			int    startPos  = occurrences.get(i);
			int    nextSlash = restPrefix.substring(startPos).indexOf('/');
			String token     = restPrefix.substring(startPos, nextSlash);
			String expXPath  = rootElement + "/" + token;
			int    expIndex  = tableColumnElements.indexOf(expXPath);
			
			// Note expIndex+1: the first column of the table is the treenode column.
			
			String replText  = (String) omdl.getValueAt(modelRow, expIndex+1);
			
			restPrefix.replace(startPos, startPos+nextSlash, replText);
		}
			
		return restPrefix.toString();
	}
	


	
	protected ArrayList<ArrayList<File>> performActions(Map<String, String> od)
	{
		
      
//      nFilesToDownload = calculateNumberOfFilesToDownload(nSelections, firstRow, tableColumnElements);
//		//nFilesToGenerate = calculateNumberOfFilesToGenerate();
//		//nFilesToOutput   = calculateNumberOfFilesToOutput();
//      nFilesDownloaded = 0;
//      nFileFailures    = 0;

      ArrayList<ArrayList<File>> outputFileList = new ArrayList<ArrayList<File>>();

//      for (int j=0; j<nSelections; j++)
//      {
//         // While the selection is being retrieved (potentially from a remote
//         // database) change the icon to indicate a download. We send a signal
//         // to the Event Dispatch Thread, then wait using Thread.sleep(100) for
//         // the EDT to start the icon. Potentially bad practice, but a good
//         // solution here.
//         iconCycling     = false;
//         downloadIconRow = j;
//         setProgress(DAOOutput.START_ICON);
//         while (!iconCycling)
//         {
//            Thread.sleep(100);
//         }
//         
//         // Note that, if the column has been sorted, then we need to retrieve
//         // the correct row in the original model here.
//         int viewRow  = firstRow + j;
//         int modelRow = outline.convertRowIndexToModel(viewRow);
//         logger.debug("Row selected in view = " + viewRow + "  Model row = " + modelRow);
//         
//         ArrayList<File> fileList = generateCacheFiles(modelRow, tableColumnElements);
//         
//         // Send stop signal to icon and wait for it to stop.
//         setProgress(DAOOutput.STOP_ICON);
//         while (iconCycling)
//         {
//            Thread.sleep(100);
//         }
//
//         outputFileList.add(fileList);
//      }

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
   

	
	
//   /**
//    * Returns the string corresponding to the URL where we need to go to obtain
//    * the list of filenames. For all root elements, we need to reconstruct an
//    * initial portion of a REST URL of form /REST/experiments/ID.
//    * After this, the final portion of the REST URL is dependent on what type
//    * of data we are retrieving and will be provided by the concrete class.
//    * @param tableColumnElements
//    * @param tableRow
//    * @param isLocal
//    * @return required URL as a String
//    */
//   protected abstract String constructRESTCommand(Vector<String> tableColumnElements,
//                                                  int            tableRow,
//                                                  boolean        isLocal);
//   
//   
//   /**
//    * Organises the subclass-specific elements of the data download.
//	 * Account for both "straight" file downloads and files that are generated
//	 * by the system depending on the data output format chosen.
//    * @param tableColumnElements
//    * @param tableRow
//    * @return required URL as a String
//    */
//   protected abstract ArrayList<File> generateCacheFiles(int            tableRow,
//			                                                Vector<String> tableColumnElements,
//			                                                String         outputFormat);
//   
//   
//   
//   protected abstract int calculateNumberOfFilesToDownload(int nSets, int firstRow,
//                                                Vector<String> tableColumnElements)
//                          throws XNATException;
}
