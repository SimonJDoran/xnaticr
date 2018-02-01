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

package fileDownloads;

import configurationLists.DAOOutputDefinitionsList;
import configurationLists.DAOOutputFormatsList;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import imageUtilities.DownloadIcon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import configurationLists.DAOSearchableElementsList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import xnatDAO.ThumbnailPreview;
import xnatDAO.XNATDAO;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;


public class FileListWorker extends SwingWorker<ArrayList<ArrayList<File>>, String>
{
   static    Logger                     logger = Logger.getLogger(FileListWorker.class);
   protected XNATDAO                    xndao;
   protected DAOOutput                  daoo;
   protected XNATServerConnection       xnsc;
   protected DAOOutline                 outline;
   protected ThumbnailPreview           preview;
   protected String                     rootElement;
   protected String                     cacheDirName;
   protected DownloadIcon               icon;
   protected boolean                    iconCycling;
   protected int                        downloadIconRow;
   protected int                        nFilesToDownload;
   protected int                        nFilesDownloaded;
   protected int                        nFileFailures;
	protected int                        nFilesToGenerate;
	protected int                        nFilesToOutput;
	protected int[]                      selTableRows;
	protected int[]                      selModelRows;
	protected int                        nTableRows;
	protected ArrayList<File>            workingListCurrentRow;
	protected ArrayList<File>            outputListCurrentRow;
	protected ArrayList<File>            sourceListCurrentRow = new ArrayList<>();
	protected ArrayList<ArrayList<File>> sourceListAllRows    = new ArrayList<>();
	protected ArrayList<ArrayList<File>> outputListAllRows    = new ArrayList<>();
	protected ArrayList<String>          sessionIDList        = new ArrayList<>();
	protected ArrayList<String>          sessionLabelList     = new ArrayList<>();
	protected ArrayList<String>          sessionSubjectList   = new ArrayList<>();
   protected ArrayList<PreFetchStore>   pfsList              = new ArrayList<>();
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
		getPreFetchResources(od);
		performPreFetchActions(od);
		
		sourceListAllRows = downloadResources(od);
		if (!isCancelled()) performPostFetchActions(od, pfsList);
		
		// outputList is built up by the executeAction() method of the concrete
		// DownloadAction classes created as part of the performPostFetchActions(od) method.
		return outputListAllRows;
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
         sb.append(filename.substring(0, 9));
         sb.append("...");
         sb.append(filename.substring(len-3));
      }
      publish(sb.toString());
   }
	
	
	public void publishFromOutsidePackage(String textToPublish)
	{
		publish(textToPublish);
	}
	
	
	public void setProgressFromOutsidePackage(int progValue)
	{
		setProgress(progValue);
	}
   
   
   @Override
   protected void process(List<String> listToProcess)
   {
      String lastElement = listToProcess.get(listToProcess.size()-1);
      xndao.getDownloadDetailsJLabel().setText(lastElement);
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
	

	protected void getPreFetchResources(Map<String, String> od)
			         throws InterruptedException, IOException, XNATException
	{
		// At present, the only thing that we need is a list of session
		// labels for the anonymise and send GUI.
		selTableRows       = outline.getSelectedRows();
		nTableRows         = selTableRows.length;
      selModelRows = new int[nTableRows];		
		for (int i=0; i<nTableRows; i++)
		{
			selModelRows[i] = outline.convertRowIndexToModel(selTableRows[i]);
			
			// Sessions need to be handled differently from other items, as they
			// are further up the hierarchy and do not have resources directly
			// underneath.
			if (rootElement.contains("Session"))
			{
				sessionIDList.add(getIDForRow(selModelRows[i]));
				sessionLabelList.add(getLabelForRow(selModelRows[i]));
				String subj = getSubjectForRow(selModelRows[i]);
				if (subj != null) sessionSubjectList.add(subj);
			}
		}
	}
	
	
	
	protected ArrayList<ArrayList<File>> downloadResources(Map<String, String> od)
			         throws InterruptedException, IOException, XNATException
	{
      for (int j=0; j<nTableRows; j++)
      {
         // While the selection is being retrieved (potentially from a remote
         // database) change the icon to indicate a download. We send a signal
         // to the Event Dispatch Thread, then wait using Thread.sleep(100) for
         // the EDT to start the icon. Potentially bad practice, but a good
         // solution here.
         iconCycling     = false;
         downloadIconRow = selTableRows[j];
			int modelRow    = selModelRows[j];
         setProgress(DAOOutput.START_ICON);
         while (!iconCycling)
         {
            try{Thread.sleep(100);}
				catch (InterruptedException exIE) {throw exIE;}
         }
         
         
			// Sessions need to be handled differently from other items, as they
			// are further up the hierarchy and do not have resources directly
			// underneath.
			if (rootElement.contains("Session"))
			{
				sessionLabelList.add(getSubjectForRow(modelRow));
				
				boolean downloadThumbnailsSeparately = !od.get("sourceName").equals(od.get("thumbnailName"));
				nFilesDownloaded = 0;
				nFilesToDownload = 0;
				ArrayList<String> scanIDs = getScanIDsForSession(modelRow);
				for (String scanID : scanIDs)
				{
					String restPrefix = constructRestPrefix("source", od, modelRow, scanID); 
					int    nSource    = getNumberOfFilesForResourceSet("source", od, restPrefix);
					int    nThumb     = getNumberOfFilesForResourceSet("thumbnail", od, restPrefix);
					nFilesToDownload += nSource;
					if (downloadThumbnailsSeparately) nFilesToDownload += nThumb;
				}
				
				// We can't do this in the same loop as above, because we need to have the
				// total number of files to download calculated for all scans before
				// any downloads happen, so that the progress bar works properly.
				for (String scanID : scanIDs)
				{
					String restPrefix = constructRestPrefix("source", od, modelRow, scanID);
					
					if (downloadThumbnailsSeparately)					
						sourceListCurrentRow.addAll(downloadResourceSet("thumbnail", od, restPrefix));
		
					sourceListCurrentRow.addAll(downloadResourceSet("source", od, restPrefix));
				}
			}
			else // Each row represents a single item under which there are
			     // resources to download.
			{
				boolean downloadThumbnailsSeparately = !od.get("sourceName").equals(od.get("thumbnailName"));
				
				String restPrefix = constructRestPrefix("source", od, modelRow, null);
				int nSource       = getNumberOfFilesForResourceSet("source", od, restPrefix);
				
				restPrefix        = constructRestPrefix("thumbnail", od, modelRow, null);
				int nThumb        = getNumberOfFilesForResourceSet("thumbnail", od, restPrefix);
				
				nFilesDownloaded  = 0;
				nFilesToDownload  = nSource;
				if (downloadThumbnailsSeparately)
				{
					nFilesToDownload += nThumb;
					sourceListCurrentRow.addAll(downloadResourceSet("thumbnail", od, restPrefix));
				}

				sourceListCurrentRow.addAll(downloadResourceSet("source", od, restPrefix));
			}
         
         // Send stop signal to icon and wait for it to stop.
         setProgress(DAOOutput.STOP_ICON);
         while (iconCycling)
         {
            Thread.sleep(100);
         }

         sourceListAllRows.add(sourceListCurrentRow);
      }
		
		return sourceListAllRows;
	}
	
	protected int getNumberOfFilesForResourceSet(String type, Map<String, String> od,
			                                       String restPrefix)
			        throws IOException, XNATException
	{
		// Check for multiple elements in the name and format entries.
		HashMap<String, String> nameFormat       = getNameAndFormatMap(type, od, restPrefix);
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
	protected ArrayList<File> downloadResourceSet(String type, Map<String, String> od,
			                                        String restPrefix)
			    throws IOException, XNATException
	{
		// Check for multiple elements in the name and format entries.
		HashMap<String, String> nameFormat     = getNameAndFormatMap(type, od, restPrefix);
		Set<String>             resourceNames  = nameFormat.keySet();
		ArrayList<File>         filesRetrieved = new ArrayList<>();
		
		for (String resourceName : resourceNames)
		{
			String format = nameFormat.get(resourceName);
			
			ArrayList<String> filenames = getFilenamesForResource(resourceName, restPrefix);
			for (String fname : filenames)
			{
				// The status will be cancelled, for example, if a selection on the
				// table has been modified to add extra lines, in which case, a new
				// FileListWorker will have been created to handle the retrieval of
				// all lines.
				if (!isCancelled())
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
					else nFileFailures++;
//					System.out.println(this.toString());
//					if (((DAOOutput.STOP_ICON - 1) * nFilesDownloaded / nFilesToDownload) > 100)
//						System.out.println("Too large");
					setProgress((DAOOutput.STOP_ICON - 1) * nFilesDownloaded / nFilesToDownload);
				}
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
				nFilesDownloaded++;	
				logger.debug("Worker ID = " + this.toString() + " Downloaded " + cacheFile.toString());
				publishDownloadProgress(cacheFile.getName());
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
		String restCommand = restPrefix + "/" + resourceName + "/files?format=xml";
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
		
		return new ArrayList<String>(resultSet.getColumn(2));
	}
	
	
	
	protected HashMap<String, String> getNameAndFormatMap(String type, Map<String, String> od,
			                                                String restPrefix)
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

	
	protected String getIDForRow(int modelRow) throws IOException		  
	{
		DAOSearchableElementsList sel      = DAOSearchableElementsList.getSingleton();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);
		OutlineModel  omdl                 = outline.getOutlineModel();
		String        expXPath             = rootElement + "/ID";
		int           expIndex             = tableColumnElements.indexOf(expXPath);
		
		return (String) omdl.getValueAt(modelRow, expIndex+1);
	}
	
	
	
	protected String getLabelForRow(int modelRow) throws IOException
	{
		DAOSearchableElementsList sel      = DAOSearchableElementsList.getSingleton();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);
		OutlineModel  omdl                 = outline.getOutlineModel();
		String        expXPath             = rootElement + "/label";
		int           expIndex             = tableColumnElements.indexOf(expXPath);
		
		return (String) omdl.getValueAt(modelRow, expIndex+1);
	}
	
	
	protected String getSubjectForRow(int modelRow) throws IOException
	{
		DAOSearchableElementsList sel      = DAOSearchableElementsList.getSingleton();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);
		OutlineModel  omdl                 = outline.getOutlineModel();
		String        expXPath             = rootElement + "/subject_ID";
		int           expIndex             = tableColumnElements.indexOf(expXPath);
		
		// Note that many (perhaps most) root elements will not have a subject_ID
		// field. In this case, the method returns null and the caller should
		// explicitly test for this.
		if (expIndex == -1) return null;
		
		return (String) omdl.getValueAt(modelRow, expIndex+1);
	}
		
		
	protected ArrayList<String> getScanIDsForSession(int modelRow)
			    throws IOException, XNATException
	{
		String sessionID = getIDForRow(modelRow);
		String restCommand = "/data/archive/experiments/" + sessionID + "/scans?format=xml";
		
		Vector2D resultSet;
		try
		{
			resultSet = (new XNATRESTToolkit(xnsc)).RESTGetResultSet(restCommand.toString());
		}
		catch(XNATException exXNAT)
		{
			logger.warn("Problem retrieving list of scans for session "
					       + sessionID + "from XNAT");
			throw new XNATException(XNATException.RETRIEVING_LIST);
		}
		
		return new ArrayList<String>(resultSet.getColumn(1));
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
	protected String constructRestPrefix(String type, Map<String, String> od,
			                               int modelRow, String insert)
			    throws IOException
	{
		DAOSearchableElementsList sel = DAOSearchableElementsList.getSingleton();
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(rootElement);

		StringBuilder restPrefix = new StringBuilder(od.get("restTemplate"));
		
      OutlineModel omdl    = outline.getOutlineModel();
		int startPos;
		while ((startPos = restPrefix.indexOf("$")) != -1)
		{
			int    nextSlash = restPrefix.substring(startPos).indexOf('/');
			String token     = restPrefix.substring(startPos+1, startPos+nextSlash);
			String expXPath  = rootElement + "/" + token;
			int    expIndex  = tableColumnElements.indexOf(expXPath);
			
			// Note expIndex+1: the first column of the table is the treenode column.
			
			String replText  = (String) omdl.getValueAt(modelRow, expIndex+1);
			
			restPrefix.replace(startPos, startPos+nextSlash, replText);
		}
		
		// The purpose of the additional insert argument is to allow a part of
		// the REST prefix to be generated by a loop around a set of predetermined
		// values that don't come from the table. The particular use case for
		// which this is important is downloading session resources. If each line
		// in the treetable is a session, then we need to iterate around all the
		// scans in the session and download the resources for each scan, each
		// assessor, or other element.
		startPos = restPrefix.indexOf("@");
		if ((startPos != -1) && (insert != null))
		{
			int  nextSlash = restPrefix.substring(startPos).indexOf('/');
			restPrefix.replace(startPos, startPos+nextSlash, insert);
		}
		if (((startPos == -1) && (insert != null)) ||
			 ((startPos != -1  && (insert == null))))
			throw new RuntimeException("Programming error: incorrect insertion into REST prefix.");
				  
			
		return restPrefix.toString();
	}
	
   /**
    * 
    * @param od output definition entries for the particular form of output chosen by the user
    * @return pfsList List<PreFetchStore> with the possibility of
    * storing arbitrary objects, allowing the preFetch actions to
    * communicate object references forward to be used by the final output
    * methods after all the data have been fetched.
    * @throws Exception 
    */
	protected List<PreFetchStore> performPreFetchActions(Map<String, String> od)
             throws Exception
	{
		outputListAllRows = new ArrayList<ArrayList<File>>();
      ArrayList<PreFetchStore> pfsList = new ArrayList<>();
		
		// Get all action entries in the output definition.
		Set<String>       keys    = od.keySet();
		SortedSet<String> actions = new TreeSet<>();
		for (String key : keys) if (key.startsWith("preFetchAction")) actions.add(key);

		// Perform the actions.
		DownloadActionFactory af = new DownloadActionFactory();
		for (String action : actions)
		{
			String actionName = od.get(action);
         // The return type of the executeAction() method is Object.
         // If the action wants to pass anything forward then it
         // should return a non-null object.
			pfsList.add(af.getAction(actionName).executeAction(this));
		}
	}
	
	
	protected void performPostFetchActions(Map<String, String> od,
                                          ArrayList<Object> preFetchStore)
             throws Exception
	{
		for (int i=0; i<nTableRows; i++)
		{	
			outputListCurrentRow      = new ArrayList<File>();
			workingListCurrentRow     = sourceListAllRows.get(i);
			
			// Get all action entries in the output definition.
			Set<String>       keys    = od.keySet();
			SortedSet<String> actions = new TreeSet<>();
			for (String key : keys) if (key.startsWith("postFetchAction")) actions.add(key);
		
			// Perform the actions.
			DownloadActionFactory af = new DownloadActionFactory();
			for (String action : actions)
			{
				String actionName = od.get(action);
				af.getAction(actionName).executeAction(this);
			}
			
			String cardinality = od.get("outputCardinality");
			if (cardinality.equals("OnePerRow"))
				outputListAllRows.add(outputListCurrentRow);
			
			if ((cardinality.equals("Single")) && !outputListCurrentRow.isEmpty())
			{
				outputListAllRows.clear();
				outputListAllRows.add(outputListCurrentRow);
			}	
		}
   }
	
	
	public String getCacheDirName()
	{
		return cacheDirName;
	}
	
	
	public ArrayList<File> getOutputListCurrentRow()
	{
		return outputListCurrentRow;
	}

	
	public void putOutputListCurrentRow(ArrayList<File> fileList)
	{
		outputListCurrentRow = fileList;
	}
	
	
	public void addAllToOutputListCurrentRow(ArrayList<File> fileList)
	{
		outputListCurrentRow.addAll(fileList);
	}
	
	
	public void addToOutputListCurrentRow(File file)
	{
		outputListCurrentRow.add(file);
	}
	
	
	public ArrayList<ArrayList<File>> getOutputListAllRows()
	{
		return outputListAllRows;
	}

	
	public void putOutputListAllRows(ArrayList<ArrayList<File>> fileList)
	{
		outputListAllRows = fileList;
	}
	
	
	public void addAllToOutputListAllRows(ArrayList<ArrayList<File>> fileList)
	{
		outputListAllRows.addAll(fileList);
	}
	
	
	public void addToOutputListAllRows(ArrayList<File> fileList)
	{
		outputListAllRows.add(fileList);
	}
	
	
	public ArrayList<ArrayList<File>> getSourceListAllRows()
	{
		return sourceListAllRows;
	}
	
	
	public ArrayList<File> getWorkingListCurrentRow()
	{
		return workingListCurrentRow;
	}
	
	
	public void putWorkingListCurrentRow(ArrayList<File> fileList)
	{
		workingListCurrentRow = fileList;
	}
	
	
	public void addAllToWorkingList(ArrayList<File> fileList)
	{
		workingListCurrentRow.addAll(fileList);
	}
	
	public void addToWorkingList(File file)
	{
		workingListCurrentRow.add(file);
	}
	
// Note that this needs to be executed on the Event Dispatch Thread.
	public void clearDownloadArea()
	{
		xndao.getDownloadJButton().setText("Download");
		xndao.getDownloadJButton().setEnabled(false);
	}
   

// Note that this needs to be executed on the Event Dispatch Thread.
	public void revealDownloadArea()
   {
		xndao.getDownloadJButton().setText("Cancel");
      xndao.getDownloadJButton().setEnabled(true);
		xndao.getDownloadDetailsJLabel().setVisible(true);
      xndao.getDownloadJProgressBar().setVisible(true);
      xndao.getDownloadJProgressBar().setValue(0);
   }
   
   
   // Note that this needs to be executed on the Event Dispatch Thread.
   public void hideDownloadArea()
   {
		xndao.getDownloadJButton().setText("Download");
      xndao.getDownloadJButton().setEnabled(false);
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
      DAOMutableTreeNode    treeNode  = (DAOMutableTreeNode) outline.getValueAt(downloadIconRow, 0);
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
   
}
