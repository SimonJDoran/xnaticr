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
* Java class: DataUploader.java
* First created on October 18, 2010 at 9:12 PM
* 
* Abstract class on which all the objects for uploading specific
* data types to XNAT are based.
*********************************************************************/

package xnatUploader;

import configurationLists.DAOSearchableElementsList;
import dataRepresentations.xnatSchema.Catalog;
import dataRepresentations.xnatSchema.InvestigatorList;
import dataRepresentations.xnatSchema.Provenance;
import exceptions.XNATException;
import generalUtilities.UidGenerator;
import generalUtilities.Vector2D;
import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.zip.DataFormatException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
import exceptions.XMLException;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import xnatMetadataCreators.CatCatalogMdComplexType;
import xnatRestToolkit.XnatResource;
import static xnatUploader.NextMatchingFileWorker.logger;


public abstract class DataUploader
{
	static  Logger                          logger = Logger.getLogger(DataUploader.class);
	
	protected XnatResource					    primaryResource;
	protected ArrayList<XnatResource>       auxiliaryResources;
	protected XMLUtilities                  XMLUtil;
   protected XNATNamespaceContext          XnatNs;
   protected XNATProfile                   xnprf;
   protected XNATRESTToolkit               xnrt;
   protected String                        RESTCommand;
   protected Vector2D<String>              result;
   protected Document                      doc;
   protected File                          uploadFile;
   protected String                        date;
   protected String                        time;
	protected String                        label;
	protected String                        labelTemplate;
   protected String                        note;
	protected String                        visit;
   protected String                        visitID;
	protected String                        original;
   protected String                        protocol;
   protected String                        XNATExperimentID;
	protected String                        XNATExperimentLabel;
   protected String                        XNATProject;
   protected Set<String>                   XNATScanIdSet;
   protected String                        XNATSubjectID;
	protected String                        XNATSubjectLabel;
	protected String                        XNATGender;
	protected String                        XNATDateOfBirth;
   protected String                        XNATAccessionID;
	protected InvestigatorList              invList;
   protected Provenance                    prov;
   protected Catalog                       inputCat;
   protected Map<String, AmbiguousSubjectAndExperiment> ambiguousSubjExp;
   protected UploadStructure               uploadStructure;
   protected boolean                       isPrepared;
   protected boolean                       errorOccurred;
   protected String                        errorMessage;
   protected File                          mostRecentSuccessfulPrep;
   protected String                        batchNote;
   protected boolean                       isBatchMode;
   protected String                        version;


   public DataUploader(XNATProfile xnprf)
   {
		this.xnprf      = xnprf;
				
		// The project has already been specified in the UI. In the current
      // incarnation, this comes about by uploading to a profile that consists
      // of only one project, but this will change.
      XNATProject        = xnprf.getProjectList().get(0);
      errorOccurred      = false;
      errorMessage       = "";
      XMLUtil            = new XMLUtilities();
      XnatNs             = new XNATNamespaceContext();
      xnrt               = new XNATRESTToolkit(this.xnprf);
      uploadStructure    = new UploadStructure(getRootComplexType());
      auxiliaryResources = new ArrayList<>();
		ambiguousSubjExp   = new LinkedHashMap<>();
   }
   
   
   /**
    * Prepare to upload a file by read, parsing and validating it.
    * Note that this is a time-consuming task and so has to be accomplished
    * by a separate SwingWorker thread, not the main Event Dispatch Thread.
    */
   public void prepareUpload()
   {
		// Check the health of the connection. Under some circumstances,
		// it appears that the JSessionID gets reset to null.
		// THIS IS A KLUDGE: I NEED TO DISCOVER WHY JSessionID is being reset.
		if (xnprf.getJSessionID() == null)
		{
			xnprf.connect();
		}
      // In batch mode, the NextMatchingFileScanner will have just parsed
      // the file, so don't do it again.
      if (uploadFile.equals(mostRecentSuccessfulPrep)) return;

      isPrepared    = false;
      mostRecentSuccessfulPrep = null;
      
      if (!readFile())                return;
      if (!parseFile())               return;
		if (!retrieveDemographics())    return;
      if (!retrieveProjectInvestigators()) return;
      isPrepared = true;
      mostRecentSuccessfulPrep = uploadFile;
   }


      /**
    * This method interprets the file just opened. It is implemented differently
    * by each concrete class, as each type of data has different requirements.
	 * The default implementation applies to subclasses that have the relevant
	 * data pre-computed by another part of the uploader and never need to read
	 * an actual data file.
    * 
    * @return a boolean indicating whether the file was parsed correctly
    */
   public boolean parseFile()
	{
		return true;
	}
   
   
	 /**
    * Open and read the specified file.
    * It is implemented differently
    * by each concrete class, as each type of data has different requirements.
	 * The default implementation applies to subclasses that have the relevant
	 * data pre-computed by another part of the uploader and never need to read
	 * an actual data file.
	 * 
    * @return a boolean variable with true if the file was opened successfully
    *         and false otherwise.
    */
   public boolean readFile()
   {
      return true;
   }
	
	
	
	public boolean isPreparedForUpload()
   {
      return isPrepared;
   }


   public boolean errorOccurred()
   {
      return errorOccurred;
   }
   


   public String getErrorMessage()
   {
      return errorMessage;
   }
   
   
   
   /**
	 * Return the URL needed to create the relevant XNAT "object" (scan, assessor, etc.)
    * @return a String containing the REST URL to which upload will occur
	 */
	public String getMetadataUploadCommand()
	{
		return getUploadRootCommand(XNATAccessionID) + "?inbody=true";
	}

	
	/**
    * First stage of the two-step process. This method places the metadata
    * in the SQL tables of the PostgreSQL database, by uploading a metadata
    * XML document using REST. Then in uploadResourcesToRepository(), the
	 * data file itself is uploaded, together with any auxiliary files,
	 * such as catalogue files and snapshots.
	 * 
	 * The basic metadata upload is here. However, this method is often
	 * over-ridden, because some of the data types require additional
    * work to be done. This is the cascade. For example, uploading an RT-STRUCT
    * not only uploads the metadata for an icr:regionSetData, but also creates
    * an icr:regionData entry for each individual ROI contained in the RT-STRUCT.
	 * @throws exceptions.XNATException
    */
   public void uploadMetadataAndCascade()
			      throws XNATException, exceptions.DataFormatException, IOException
   {
      errorOccurred = false;
      
      if (XNATAccessionID == null)
            XNATAccessionID = getRootElement() + '_' + UidGenerator.createShortUnique();

      // Replace template information in label with actual values and ensure that
      // only characters valid for XNAT experiment labels make it through.
		if (label != null)
		{
			label = label.replace("$SESSION$",      XNATExperimentLabel)
			             .replace("$SUBJECT$",      XNATSubjectLabel)
					       .replace("$DATE_TIME$",    date + "_" + time)
						//	 .replace("$FILENAME$",     uploadFile.getName())
					       .replace("$UPLOADER_UID$", UidGenerator.createShortUnique())
                      .replaceAll("[^A-Za-z0-9_]", "");
		}
      Document metaDoc = createMetadataXml();
      if (errorOccurred) throw new XNATException(XNATException.FILE_UPLOAD,
                          "There was a problem in creating the metadata to "
                          + "describe the uploaded file.\n"
                          + errorMessage);
      
      
      try
      {
         RESTCommand    = getMetadataUploadCommand();
         InputStream is = xnprf.doRESTPut(RESTCommand, metaDoc);
         int         n  = is.available();
         byte[]      b  = new byte[n];
         is.read(b, 0, n);
         String XNATUploadMessage = new String(b);
         
         if ((xnrt.XNATRespondsWithError(XNATUploadMessage)) ||
             (!XNATUploadMessage.equals(XNATAccessionID)))
         {
            errorOccurred = true;
            errorMessage  = XNATUploadMessage;
            throw new XNATException(XNATException.FILE_UPLOAD,
                          "XNAT generated the message:\n" + XNATUploadMessage);
         }
      }
      catch (Exception ex)
      {
         // Here we cater both for reporting the error by throwing an exception
         // and by setting the error variables. When performing the upload via
         // a SwingWorker, it is not easy to retrieve an Exception.
         errorOccurred = true;
         errorMessage = ex.getMessage();
         throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
      }             
   }
   
   
   
   public void uploadXnatResource(XnatResource xr) throws XNATException
   {
      try
      {
			String rootCmd = getUploadRootCommand(XNATAccessionID);
			String cmd     = xr.getResourceDataUploadCommand(rootCmd);
			
         InputStream is = null;
			if (xr.getFile()     != null) is = xnprf.doRESTPut(cmd, xr.getFile());
			if (xr.getDocument() != null) is = xnprf.doRESTPut(cmd, xr.getDocument());
			if (xr.getStream()   != null) is = xnprf.doRESTPut(cmd, xr.getStream());
			
			assert (is != null);
         int         n  = is.available( );
         byte[]      b  = new byte[n];
         is.read(b, 0, n);
         String XNATUploadMessage = new String(b);
         
         if (xnrt.XNATRespondsWithError(XNATUploadMessage))
            throw new XNATException(XNATException.FILE_UPLOAD,
                                    "XNAT generated the message:\n"
                                    + XNATUploadMessage);         
      }
      catch (Exception ex)
      {
         // Here we cater both for reporting the error by throwing an exception
         // and by setting the error variables. When performing the upload via
         // a SwingWorker, it is not easy to retrieve an Exception.
         errorOccurred = true;
         errorMessage = ex.getMessage();
         throw new XNATException(XNATException.FILE_UPLOAD, errorMessage);
                  
         // TODO If there is an error here, what do we do about the XNAT
         // PostgreSQL database?
      }             
   }
   
   
  	/**
	 * Create the XNAT resources corresponding to the types of data being
	 * uploaded. This call will create a resource for the primary data file
	 * being uploaded and then a separate resource for each of the
	 * auxiliary data formats. 
	 */
	public void createXnatResources() throws XNATException
   {
		ArrayList<XnatResource> xrList = new ArrayList<>();	
		if (primaryResource != null) xrList.add(primaryResource);		
		for (XnatResource ar: auxiliaryResources) xrList.add(ar);
		
		Set<String> names = new HashSet<>();

		// Create a resource (which manifests itself as a directory on the
      // server) for each unique label in the set of files to be
		// uploaded.
		for (XnatResource xr: xrList)
		{
			if (!names.contains(xr.getName()))
			{
				names.add(xr.getName());
				
				try
				{
					String rootCmd = getUploadRootCommand(XNATAccessionID);
					String cmd     = xr.getResourceCreationCommand(rootCmd);
               InputStream is = xnprf.doRESTPut(cmd);
               int    n = is.available();
					byte[] b = new byte[n];
					is.read(b, 0, n);
					String XNATUploadMessage = new String(b);

					if (xnrt.XNATRespondsWithError(XNATUploadMessage))
						throw new XNATException(XNATException.RESOURCE_CREATE,
														"XNAT generated the message:\n"
														+ XNATUploadMessage);         
				}
				catch (Exception ex)
				{
					// Here we cater both for reporting the error by throwing an exception
					// and by setting the error variables. When performing the upload via
					// a SwingWorker, it is not easy to retrieve an Exception.
					errorOccurred = true;
					errorMessage = ex.getMessage();
					throw new XNATException(XNATException.RESOURCE_CREATE, errorMessage);

					// TODO If there is an error here, what do we do about the XNAT
					// PostgreSQL database?
				}              
			}
		}
   }
   


   
	/**
    * This is the method called by the upload worker thread.
    * @throws XNATException 
    */
   public void uploadResourcesToRepository() throws XNATException
   {
		if (uploadFile != null)
      {
         createPrimaryResource();
      }
		createAuxiliaryResources();
		createXnatResources();


		if (primaryResource != null)
		{
			uploadXnatResource(primaryResource);
			if (errorOccurred)
				throw new XNATException(XNATException.FILE_UPLOAD, errorMessage);
		}

		for (XnatResource ar : auxiliaryResources)
      {
			uploadXnatResource(ar);
			if (errorOccurred)
				throw new XNATException(XNATException.FILE_UPLOAD, errorMessage);
		}  
   }
	
	
   
	// Retrieve some demographic information so that it is available
   // for output where necessary.
	public boolean retrieveDemographics() 
	{
      try
      {
         RESTCommand    = "/data/archive/projects/" + XNATProject
                                + "/subjects/"      + XNATSubjectID
                                + "?format=xml";
         Document resultDoc = xnrt.RESTGetDoc(RESTCommand);
         String[] attrs = XMLUtilities.getAttribute(resultDoc, XnatNs,
                                                   "xnat:Subject", "label");
         if (attrs != null) XNATSubjectLabel = attrs[0];
         
         attrs = XMLUtilities.getElementText(resultDoc, XnatNs, "xnat:gender");
         if (attrs != null) XNATGender = attrs[0];
         
         attrs = XMLUtilities.getElementText(resultDoc, XnatNs, "xnat:dob");
         if (attrs != null) XNATDateOfBirth = attrs[0];      
      }
      catch (XNATException | XMLException ex)
      {
			errorOccurred = true;
			errorMessage  = "Problem retrieving demographic information:\n"
				             + ex.getMessage();
         return false;
      }
		
		return true;
	}
	
   
   /**
    * Retrieve the investigators for the currently selected project from XNAT.
    * 
    * @return true if the method executed without errors
    */
   protected boolean retrieveProjectInvestigators()
   {
		try
		{
			invList = new InvestigatorList(XNATProject, xnprf);
		}
		catch (XNATException | XMLException ex)
		{
			errorOccurred = true;
			errorMessage  = "Problem retrieving project investigator information:\n"
				             + ex.getMessage();
         return false;
      }
		
		return true;
	}

   
   
   /**
    * Retrieve any provenance information contained in the uploaded data
    * file. Note that not all upload types. Exactly what information is
    * available will clearly depend on the specific datatype and this
    * method may or may not be over-ridden by subclasses of DataUploader.
    */
   public Provenance createProvenance()
   {
      return new Provenance();
   }
   

	
   /**
    * This method updates the parsing of the file to take into account the
    * most recent selection of either subject or experiment labels from the
    * JCombo boxes in the user interface. The default implementation (i.e.,
	 * do nothing) is applicable to data types that are uploaded without the UI.
    */
   public void updateParseFile() {}
   
   
	
   /** While updateParseFile() is running, it is useful to set a busy cursor
    *  to show the user that something is happening. This is a fairly crude
    *  implementation at present.
    */
   public void setBusyCursor(JComponent comp)
   {
      RootPaneContainer root = (RootPaneContainer)comp.getTopLevelAncestor();
      root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      root.getGlassPane().setVisible(true);
   }
   
   public void cancelBusyCursor(JComponent comp)
   {
      RootPaneContainer root = (RootPaneContainer)comp.getTopLevelAncestor();
      root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      root.getGlassPane().setVisible(false);
   }
   


   /**
    * Once the file has been parsed, the values that are extracted are filled
    * in on the screen.
    * @param mdp the MetadataPanel currently being displayed in the uploader
    * @param clearFields if false, fill the panel with new data; if true,
    * then clear all the values.
    */
   public void populateFields(MetadataPanel mdp, boolean clearFields)
   {
      ArrayList<String> subjLabels = new ArrayList<>();
      ArrayList<String> expLabels  = new ArrayList<>();
      String            subjLabel;
      int               ind;
      
      if (clearFields)
      {
         subjLabels.add("No subject");
         expLabels.add("No experiment");
         subjLabel = subjLabels.get(0);
         ind = 0;
      }
      else
      {
         for (String key : ambiguousSubjExp.keySet())
         {
            subjLabels.add(ambiguousSubjExp.get(key).subjectLabel);
         }           
         AmbiguousSubjectAndExperiment ase = ambiguousSubjExp.get(XNATSubjectID);
         subjLabel = ase.subjectLabel;
         expLabels = ase.experimentLabels;
         ArrayList<String> expIDs = ase.experimentIDs;
         ind = expIDs.indexOf(XNATExperimentID);       
      }
      mdp.populateJComboBox("XNAT Subject", subjLabels);
      mdp.populateJComboBox("XNAT Session Label", expLabels);
      
      JComboBox jcbSubj = (JComboBox) mdp.getComponent("XNAT Subject");
      JComboBox jcbExp  = (JComboBox) mdp.getComponent("XNAT Session Label");
      
      jcbSubj.setSelectedItem(subjLabel);
      jcbExp.setSelectedItem(expLabels.get(ind));
		
		
		// Now populate all the other fields.
		DAOSearchableElementsList sel = null;
      try
      {
			// This try-catch behaviour with a RunTimeException needs refactoring!!
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         throw new RuntimeException("Can't load the DAOSearchableElement list "
                                    + "from SearchableXNATElements.xml");
      }
		Vector<String> elementAliases = sel.getSearchableXNATAliases().get(getRootComplexType());
		Vector<String> elements       = sel.getSearchableXNATElements().get(getRootComplexType());
		
				
		Document metaDoc = createMetadataXml();
		
		for (int i=0; i<elements.size(); i++)
		{
			try
			{
				// The notation used by the XNAT REST interface and adopted here
				// is ambiguous as to whether the last element is an attribute or 
				// an element name. So we need to test both possibilities.
				// Element
				String xpeEl  = "/" + elements.get(i).replace(getRootComplexType(), getRootElement());
				String xpRes  = XMLUtilities.getFirstXPathResult(metaDoc, XnatNs, xpeEl);

				if (xpRes == null)
				{
					// Attribute
					String xpeAtt = "/" + elements.get(i).replace(getRootComplexType(), getRootElement());
					ind           = xpeAtt.lastIndexOf("/");
					xpeAtt        = xpeAtt.substring(0, ind+1) + "@" + xpeAtt.substring(ind+1);
					xpRes         = XMLUtilities.getFirstXPathResult(metaDoc, XnatNs, xpeAtt);
				}
				
				if (elementAliases.get(i).equals("XNAT assessment ID"))
					xpRes = "ID will be generated by XNAT at point of upload.";
				
				boolean editable = false;
				if (getEditableFields().contains(elementAliases.get(i))) editable = true;
				
				if (clearFields)
            {
               mdp.populateJTextField(elementAliases.get(i), "", editable); 
            }
            else
            {
               if (xpRes != null) mdp.populateJTextField(elementAliases.get(i), xpRes, editable);
            }
			}
			catch (XMLException exXML)
			{
				logger.error("XML coding issue when populating metadata panel");
			}
			
		}
   }
	
	
	
	/**
    * Create an XML representation of the metadata relating to the input files
    * referred to and output files created by the application whose data we are
    * uploading.
	 * 
	 * Note: Although I have set up the infrastructure here to create this
	 * input catalogue file, it is not really necessary, because XNAT does a
	 * good job of maintaining its own catalogues for every separate resource
	 * the material contained within my catalogue file is virtually redundant.
	 * So, this method call is actually commented out in practice.
	 * 
	 * @param cat input catalog data structure
    */
   protected void createInputCatalogue()
   {
		if (inputCat == null) return;
      Document xmlDoc = null;
      try
      {
         xmlDoc = (new CatCatalogMdComplexType(inputCat)).createXmlAsRootElement();
      }
      catch (XMLException | IOException ex)
      {
         reportError(ex, "create input catalogue file");
      }
      
      if (!errorOccurred)
		{
			assert (xmlDoc != null);
			XnatResource xr = new XnatResource(xmlDoc,
					                             "out",
					                             "INPUT_CATALOGUE",
					                             "XML",
			                                   "FILE_CATALOGUE",
			                                   "catalogue of input files",
			                                   XNATAccessionID+"_input.xml");

			auxiliaryResources.add(xr);
		}
   }
	
	
	
	/**
    * Error reporting method that takes some often repeated lines out of the
    * other methods
    * @param ex the Exception that gave rise to this call
    * @param operation a String giving details of what the program was doing
    */
   protected void reportError(Exception ex, String operation)
   {
      logger.error("Error during operation " + operation + " of metadata XML creation.");
      
      errorOccurred = true;
      errorMessage  = "Unable to create XML document required for uploading the data.\n";
      errorMessage  += "Error during operation " + operation + "\n";
      
      if (ex == null) return;
      if (ex.getMessage() != null) errorMessage += "The detailed error message was:\n"
                                                     + ex.getMessage();
   }
   

	 
   /**
    * After uploading a file, we need to clear all the fields in the UI.
	 * @param mdsp the MetadataPanel in which the fields and titles are displayed
    */
   protected void clearFields(MetadataPanel mdsp) {}

   
   /**
    * Create primary resource. All files uploaded to the XNAT repository need to
    * be created within the context of an XNAT resource. This method manages
    * the setting of resource attributes label, format, content and description
    * for the primaryFile file being uploaded.
    */
   protected abstract void createPrimaryResource();
	
	
	/**
    * Create auxiliary resource files. These are additional files generated by the
    * uploader - typically snapshots for quick visualisation - and will be
    * uploaded to XNAT at the same time as the actual data object. This method
	 * both creates the auxiliary files and manages the setting of resource
	 * attributes label, format, content and description
    */
   protected abstract void createAuxiliaryResources();
	
	
	/**
	 * Change appropriate data variables to reflect any edits made in the UI.
    * Note that this routine "catches" the event *before* the new character
    * has been added to the JTextField and so when we update the corresponding
    * String variables, we need to add the newly typed character explicitly.
	 * @param mdsp MetadataPanel in the UI where the fields are edited
    * @param key  the Character that has just been typed.
	 */
   protected void updateVariablesForEditableFields(MetadataPanel mdsp,
                                                   Character     key,
                                                   Object        source) {}
	
	
   /**
    * Transfer values for user-editable fields between uploaders.
    * This is important in batch mode. Each time we load a file, a new uploader
    * is created. However, we do not want to keep returning control to the user
    * to re-enter essential editor fields, so this function allows these fields
    * to be passed on.
    * @param oldUploader - the uploader that loaded the previous file 
    */
   protected void copyVariablesForEditableFields(DataUploader oldUploader) {}
   
   
   /**
    * Create a fresh copy of the uploader that is blank except for a few
    * selected fields.
    * @return A data uploader of the same type as is being used currently.
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException 
    */
   protected DataUploader getFreshCopyForBatchUpload()
             throws InstantiationException,   IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException
   {
      DataUploader   freshUploader;
      Constructor<?> con;
      
      con = (this.getClass().getDeclaredConstructors())[0];
      freshUploader = (DataUploader) con.newInstance(xnprf);              
      freshUploader.copyVariablesForEditableFields(this);
      freshUploader.setBatchModeEnabled(true);
      
      return freshUploader;
   }
   
   
	/**
    * Return a list of the fields that may be edited.
	 * Allow the flexibility for each concrete class to define its own.
	 * @return a List containing the aliases of all the fields that can be edited
    */
   protected List<String> getEditableFields()
	{
		return new ArrayList<>();
	}
	
	
	/**
    * Return a list of the fields to which listeners will be attached
	 * that force a value to be entered. Allow the flexibility for each
    * concrete class to define its own fields to watch.
    */
   protected List<String> getRequiredFields()
	{
		return new ArrayList<>();
	}


   /**
    * Get the XNAT root element corresponding to the relevant concrete class.
    * @return a String containing the root element name
    */
   protected abstract String getRootElement();
   
   
   /**
    * Get the XNAT complexType of the root element corresponding to the relevant concrete class.
    * @return a String containing the root element name
    */
   protected abstract String getRootComplexType();
   
  
   /**
    * Create the root of the REST command required for uploading a data file,
	 * creating resources, etc.
    * @param uploadItem the name of the item being processed
    * @return a String containing the root of the REST URL to which upload will occur
    */
   protected abstract String getUploadRootCommand(String uploadItem);
	
  
   
   /**
    * Create the XML that is uploaded to XNAT and contains all the metadata
    * that will go into the PostgreSQL database.
    * 
    * @return an XNAT-compatible metadata XML Document
    */
   protected abstract Document createMetadataXml();

   
   protected String truncateString(String s, int maxLength)
   {
      return  (s.length() <= maxLength) ? s : s.substring(0, maxLength-3) + "...";     
   }
	



   /**
    * Each data type for upload should have a policy of what metadata are
    * necessary. (E.g., enforce that all ROI's have a label and a note
    * (= commit message).
    * @return boolean informing the caller whether the correct metadata
    * have been entered
    */
   protected boolean rightMetadataPresent()
	{
		return true;
	}


   /**
    * Set the value of a metadata field to be uploaded.
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @param value an Object containing the data to be uploaded. N.B. No checks
    * on the data type are performed.
    */
   public void setField(String fieldAlias, Object value)
   {
      uploadStructure.setField(fieldAlias, value);
   }


   /**
    * Set one of the fields of the UploadStructure. The latter is simply a
    * container for all of the pieces of metadata that are required during
    * the upload operation.
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @param value a String value to be stored
    */
   public void setStringField(String fieldAlias, String value)
   {
      uploadStructure.setStringField(fieldAlias, value);
   }


   /**
    * Set XNAT investigator instance variable using data supplied by the user
    * @param titles
    * @param firstNames
    * @param lastNames
    * @param institutions
    * @param departments
    * @param emails
    * @param phoneNumbers 
    */
   public void setXNATInvestigators(String[] titles,
                                    String[] firstNames,
                                    String[] lastNames,
                                    String[] institutions,
                                    String[] departments,
                                    String[] emails,
                                    String[] phoneNumbers)
   {
      uploadStructure.setInvestigatorList(titles, firstNames, lastNames,
                                          institutions, departments, emails,
                                          phoneNumbers);
   }



   /**
    * Get the value of a metadata field to be uploaded.
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @return an Object representing metadata. Note that this method does not
    * provide any check that the class that is returned is the one the programmer
    * was expecting and the result needs to be explicitly cast to the correct
    * type. This may lead to programming errors that are not detected at
    * compile time or by the IDE.
    */
   public Object getField(String fieldAlias)
   {
      return uploadStructure.getField(fieldAlias);
   }


   /**
    * Get one of the String fields of the UploadStructure. The latter is simply
    * a container for all of the pieces of metadata that are required during
    * the upload operation.
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    */
   public String getStringField(String fieldAlias)
   {
      return uploadStructure.getStringField(fieldAlias);
   }


   /**
    * Get the names and other details of the XNAT investigators
    * who are allowed to upload data.
    * @return name of investigator as a String
    */
   public InvestigatorList getXNATInvestigators()
   {
      return uploadStructure.getInvestigatorList();
   }



   /**
    * Inform the uploader whether it should be working in batch mode.
    * @param isBatchMode
    */
   public void setBatchModeEnabled(boolean isBatchMode)
   {
      this.isBatchMode = isBatchMode;
   }
   
   
   
   /**
    * Set the label template, which stays constant between multiple uploads
    * batch mode.
    * @param prefix
    */
   public void setLabelTemplate(String s)
   {
      labelTemplate = s;
   }
   
   
   protected String expandLabelTemplate(String template)
   {
      // The replacement throws an error if any of the replacement
      // values are null, so check these just in case.
      String s = template;
      if (XNATExperimentLabel != null)      s = s.replace("$SESSION$",   XNATExperimentLabel);
      if (XNATSubjectLabel    != null)      s = s.replace("$SUBJECT$",   XNATSubjectLabel);
      if ((date != null) && (time != null)) s = s.replace("$DATE_TIME$", date + "_" + time);
      if (uploadFile          != null)      s = s.replace("$FILENAME$",  uploadFile.getName());
 
      // Note this setting will return something different every time the
      // method is called.
      s = s.replace("$GENERATED_UID$" , UidGenerator.createShortUnique());

      // Remove characters that are invalid in labels.
      s = s.replaceAll("[^A-Za-z0-9_]", "");
      
      return s;
   }
  
   
   /**
    * Set the batchNote, which stays constant between multiple uploads
    * batch mode.
    * @param note
    */
   public void setBatchNote(String note)
   {
      batchNote = note;
   }
   
   
   
   /**
    * Method used only by the GUI to communicate with the uploader. This should
    * not need to be called by users.
    * @param uploadFile
    */
   public void setUploadFile(File uploadFile)
   {
      this.uploadFile = uploadFile;
   }

   /**
    * Method used only by the GUI to communicate with the uploader. This should
    * not need to be called by users.
    * @param project
    */
   public void setProject(String project)
   {
      XNATProject = project;
   }
   
   
   public void setAccessionId(String id)
   {
      XNATAccessionID = id;
   }
   
   
   public void setSubjectId(String id)
   {
      XNATSubjectID = id;
   }
   
   
   public void setSubjectLabel(String s)
   {
      XNATSubjectLabel = s;
   }
   
   
   public void setExperimentId(String id)
   {
      XNATExperimentID = id;
   }
	
	
	public void setExperimentLabel(String s)
	{
		XNATExperimentLabel = s;
	}
   
   
   public void setDate(String s)
   {
      date = s;
   }
   
   
   public void setTime(String s)
   {
      time = s;
   }
   
   
   public void setNote(String s)
   {
      note = s;
   }
   
   
   /**
    * Transfer the software version from the GUI to the uploader, so that it
    * is only set in one place.
    * @param version a String containing the version number
    */
   public void setVersion(String version)
   {
      this.version = version;
   }
   
   
   /**
    * Method used only by the GUI to communicate with the uploader. This should
    * not need to be called by users.
    * @param xnprf an appropriate XNATProfile object
    */
   void setProfile(XNATProfile xnprf)
   {
      this.xnprf  = xnprf;
      xnrt        = new XNATRESTToolkit(this.xnprf);
      XNATProject = xnprf.getProjectList().get(0);
   }
   
   
   /**
    * Get the File currently being uploaded.
    * @return the required file
    */
   protected File getUploadFile()
   {
      return uploadFile;
   }
   
   

	   
}
