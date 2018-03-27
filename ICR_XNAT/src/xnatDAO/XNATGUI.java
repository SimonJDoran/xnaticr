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
* Java class: XNATGUI.java
* First created on Sep 28, 2010 at 2:08:56 PM
* 
* As I write more applications that interact with XNAT, it is clear
* that there are a number of common methods. These are contained in
* this abstract class, whilst code specific to each application is in
* the corresponding concrete subclasses.
*********************************************************************/

package xnatDAO;

import configurationLists.DAOReturnTypesList;
import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import generalUtilities.ColouredCellRenderer;
import imageUtilities.DownloadIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Timer;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATProfileList;


public abstract class XNATGUI extends javax.swing.JDialog implements ActionListener
{
   static    Logger            logger           = Logger.getLogger(XNATGUI.class);
   protected XNATProfileList   profileList      = null;
   protected boolean           ignoreItemChange = false; 
   protected DownloadIcon      downloadIcon     = null;
   protected String            typeItemSave;
   protected String            subtypeItemSave;
   protected String            deselectedItem;
   public    ArrayList<String> projectList      = null;
   public    String            dataSubtype      = null;
   public    String            dataSubtypeAlias = null;
   protected Timer             authTimer;
   protected boolean           invokedByRun     = false;
   protected String            status;
   protected String            cacheDirName;
   protected String            dicomRemapEx;
   protected String            log4jProps;
   protected boolean           authenticationInProgress = false;
   protected boolean           selectedProfileDidChange = false;
   protected String            version          = "2.1 (alpha) 08/12/2016";
   
   protected static final boolean REQUIRE_AUTHENTICATION = true;           
   

   public XNATGUI(java.awt.Frame parent, boolean modal)
   {
      super(parent, modal);
      parseConfigFile();
      configureLog4j();
      profileList  = new XNATProfileList(allowAll(), REQUIRE_AUTHENTICATION);
      authTimer    = new Timer(DAOConstants.AUTHENTICATION_EXPIRY, this);
      authTimer.start();
   }
   
   /** Parse the configuration file.
    *  This is quite a long method, but doesn't do anything complicated, just
    *  reacting to all the possible error conditions.
    *  Why does parsing always take quite so long?!
    */
   private void parseConfigFile()
   {
      final String SEP                 = System.getProperty("file.separator");
      final String XNAT_DAO_HOME       = getHomeDir();
      final String DICOM_REMAP_DEFAULT = "/Applications/DicomBrowser-1.5.2/bin/DicomRemap";
      final String FAILED              = "Cannot create file";
      final String CONFIG_FILENAME     = XNAT_DAO_HOME + "config" + SEP + "XNAT_DAO_config.xml";
      File  configFile                 = new File(CONFIG_FILENAME);

      
      // Step 1: Does the configuration file exist?      
      if (!configFile.exists())
      {
         cacheDirName = XNAT_DAO_HOME;
         dicomRemapEx = DICOM_REMAP_DEFAULT;
         log4jProps   = null;
      }
     
      
      // Step 2: Does the existing configuration file contain valid parameters?
      //         (Note that this somewhat extensive parsing is mainly to guard
      //         against the situation of either a copied parameter file that
      //         is inappropriate in the current context, or an inappropriately
      //         modified file.)
      else
      {
         String[] cacheDirs     = null;
         String[] log4jFiles    = null;
         String[] dicomRemapExs = null;
         try
         {
            BufferedInputStream bis
                = new BufferedInputStream(new FileInputStream(configFile));

            XMLUtilities xmlUtils       = new XMLUtilities();
            XNATNamespaceContext XNATns = new XNATNamespaceContext();
            Document DOMDoc             = XMLUtilities.getDOMDocument(bis);

            cacheDirs     = XMLUtilities.getElementText(DOMDoc, XNATns, "cacheDirectory");
            dicomRemapExs = XMLUtilities.getElementText(DOMDoc, XNATns, "dicomRemapExecutable");
            log4jFiles    = XMLUtilities.getElementText(DOMDoc, XNATns, "log4jProps");
         }
         catch (FileNotFoundException exFNF)
         {
            logger.error("This shouldn't happen!" + exFNF.getMessage());
            throw new RuntimeException(exFNF);
         }

         catch (XMLException exXML)
         {
            logger.error("Configuration file " + configFile + "contains invalid XML.");            
            cacheDirName = XNAT_DAO_HOME;
            log4jProps   = null;
         }

         if (cacheDirs == null) cacheDirs = new String[0];
         
         if (cacheDirs.length > 1)
         {
            logger.warn("More than one cache directory entry found in configuration file. \n"
               + "Rewriting new config file using the first value found. \n");
         }
         
         if (cacheDirs.length == 0)
         {
            logger.warn("No cache directory information found in configuration file. \n"
               + "Writing new config file using default value for cache directory. \n"
               + XNAT_DAO_HOME);
            cacheDirName = XNAT_DAO_HOME;
         }
         else cacheDirName = cacheDirs[0];
         
         
         // Step 3: We have a name? Is it valid?
         //         If it doesn't exist, try to create it. If it does, check access.
         //         If no good, rewrite using a default.
         try
         {
            (new File(cacheDirName)).getParentFile().mkdirs();
         }
         catch (Exception ex)
         {
            logger.warn("Couldn't create cache directory " + cacheDirName + ". "
                          + "Trying to use default.\n"
                          + "Exception was " + ex.getMessage());
            cacheDirName = XNAT_DAO_HOME;
            try
            {
               (new File(cacheDirName)).getParentFile().mkdirs();
            }
            catch (Exception exc)
            {
               logger.error("Couldn't create cache directory " + cacheDirName + ".");
               cacheDirName = FAILED;
               JOptionPane.showMessageDialog(this,
                  "Unable to create cache directory. Please check file permissions.",
                  "Creation of cache directory failed.",
                  JOptionPane.ERROR_MESSAGE);
            }            
         }
         
         // Step 4: Check DicomRemap is available on the system.
         if (dicomRemapExs == null)
         {
            logger.warn("No DicomRemap information found in configuration file.\n"
                        + "Trying default location ...");
            dicomRemapEx = DICOM_REMAP_DEFAULT;
         }
         
         else if (dicomRemapExs.length == 1) dicomRemapEx = dicomRemapExs[0];
         
         
         else if (dicomRemapExs.length > 1)
         {
            logger.warn("More than one dicomRemap entry found in configuration file. \n"
               + "Rewriting new config file using the first value found ...");
            dicomRemapEx = dicomRemapExs[0];
         }
         
         File f = new File(dicomRemapEx);
         if (!(f.isFile() && f.canExecute()))
         {
            logger.error("No executable found for DicomRemap. The anonymise and send"
                         + "function will not be available.");
         }
         
         
         
         
         
         
         // Step 5: Check for valid log4j file.
         if (log4jFiles == null) log4jFiles = new String[0];
         
         if (log4jFiles.length > 1)
         {
            logger.warn("More than one log4jFile entry found in configuration file. \n"
               + "Rewriting new config file using the first value found ...");
         }
         
         if (log4jFiles.length == 0)
         {
            logger.warn("No log4j file information found in configuration file. \n"
               + "Using application defaults ...");
            log4jProps = null;
         }
         else log4jProps = log4jFiles[0];
         
         if (log4jProps != null)
         {
            if (!(new File(log4jProps)).canRead())
            {
               logger.warn("File " + log4jProps + " does not exist.\n"
                             + "Using application default logging ...");
                         
               log4jProps = null;
            }
         }
      }
      
      writeConfigFile(CONFIG_FILENAME, cacheDirName, log4jProps);      
   }


   public boolean writeConfigFile(String configFilename, String cacheDirName,
                                                          String logFilename)
   {
      File configFile = new File(configFilename);
      
      try
      {
         configFile.getParentFile().mkdirs();
         configFile.delete();
         configFile.createNewFile();
         PrettyPrinterXmlWriter pp = new PrettyPrinterXmlWriter(
                                        new SimpleXmlWriter(
                                           new FileWriter(configFile)));
         pp.writeXmlVersion();
         pp.writeEntity("XNAT_DAO_config")
                 
              .writeEntity("cacheDirectory")
              .writeText(cacheDirName)
              .endEntity()
                 
              .writeEntity("log4jProps")
              .writeText(logFilename)
              .endEntity()
                 
           .endEntity();
         pp.close();
      }
      catch (IOException exIO)
      {
         logger.error("Unable to save configuration file.\n"
                          + "Error was: " + exIO.getMessage());
         
         JOptionPane.showMessageDialog(this,
              "I was unable to save the configuration file.\n"
                  + "Please check write permissions for\n"
                  + configFile.getAbsoluteFile(),
              "Failed to save configuration file",
              JOptionPane.ERROR_MESSAGE);
         return false;
      }
      return true;
   }


   
   /**
    * Set up the logging configuration
    */
   private void configureLog4j()
   {
      if (log4jProps != null)
      {
         PropertyConfigurator.configure(log4jProps);
      }
      else
      {
         URL configURL = XNATGUI.class.getResource("projectResources/log4j.config");
         if (configURL == null) return;
         PropertyConfigurator.configure(configURL);
      }
   }


   
   /**
    * External access to the current list of profiles
    * @return XNATProfileList object
    */
   public XNATProfileList getProfileList()
   {
      return profileList;
   }
   
  

	/* Wrapper function for setting up various components of the UI */
	protected void populateCommonComponents()
	{
      populateProfileJComboBox();
      populateDataTypeJComboBox();
      populateDataSubtypeJComboBox();
	}



	/** Using the contents of the file $HOME/.XNAT_DAO/config/XNAT_DAO_profiles.xml
    * insert all the profiles currently known about into the combo box.
    */
	protected void populateProfileJComboBox()
	{
      final String ADD_TEXT    = "Add new profile...";
      final String EDIT_TEXT   = "Edit current profile...";
      final String DELETE_TEXT = "Delete current profile...";
      
		DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
      getProfileJComboBox().setModel(dcbm);
      
      // Step 1: Add the element text to the list.      
      dcbm.insertElementAt(ADD_TEXT,    0);
      dcbm.insertElementAt(EDIT_TEXT,   1);
      dcbm.insertElementAt(DELETE_TEXT, 2);
      
      if (profileList.isEmpty())
         dcbm.insertElementAt(PermissionsWorker.NO_PROFILES, 0);
      
      for (XNATProfile ixnprf : profileList)
      {
         int n = dcbm.getSize();
         dcbm.insertElementAt(ixnprf.getProfileName(), n-3);
      }
      
      
      // Step 2: Select the correct item and set the foreground colour appropriately.
      if (profileList.isEmpty())
      {
         dcbm.setSelectedItem(PermissionsWorker.NO_PROFILES);
         getProfileJComboBox().setForeground(DAOConstants.NON_SELECTABLE_COLOUR);
      }
      else
      {
         XNATProfile currentProfile = profileList.getCurrentProfile();
         projectList = currentProfile.getProjectList();
         dcbm.setSelectedItem(currentProfile.getProfileName());
         if (currentProfile.isConnected())
            getProfileJComboBox().setForeground(DAOConstants.CONNECTED_COLOUR);
         else
            getProfileJComboBox().setForeground(DAOConstants.DISCONNECTED_COLOUR);
      }
      
      
      // Step 3: Set the colours of the list elements according to their type.
      ArrayList<String> actionList        = new ArrayList<String>();
      ArrayList<String> connectedList     = new ArrayList<String>();
      ArrayList<String> disconnectedList  = new ArrayList<String>();
      ArrayList<String> nonSelectableList = new ArrayList<String>();
      
      actionList.add(ADD_TEXT);
      if (profileList.isEmpty())
      {
         nonSelectableList.add(EDIT_TEXT);
         nonSelectableList.add(DELETE_TEXT);
         nonSelectableList.add(PermissionsWorker.NO_PROFILES);
      }
      else
      {
         actionList.add(EDIT_TEXT);
         actionList.add(DELETE_TEXT);
      }
      
      for (Iterator<XNATProfile> ixnprf = profileList.iterator(); ixnprf.hasNext();)
      {
         XNATProfile profile = ixnprf.next();
         
         // Some connected profiles may not be allowable in the context of, say,
         // the data uploader, because they refer to all accessible projects instead
         // of just one. 
         if ((profile.getProjectList().size() > 1) && !allowAll())
              nonSelectableList.add(profile.getProfileName());
         else
         {
            if (profile.isConnected())
               connectedList.add(profile.getProfileName());
            else
               disconnectedList.add(profile.getProfileName());
         }

      }
      
      
      ColouredCellRenderer ccr =
              new ColouredCellRenderer<String>(DAOConstants.ACTION_COLOUR,
                                               DAOConstants.CONNECTED_COLOUR,
                                               DAOConstants.DISCONNECTED_COLOUR,
                                               DAOConstants.NON_SELECTABLE_COLOUR);
      ccr.setElementsForType(0, actionList);
      ccr.setElementsForType(1, connectedList);
      ccr.setElementsForType(2, disconnectedList);
      ccr.setElementsForType(3, nonSelectableList);
      
      getProfileJComboBox().setRenderer(ccr);
   }
   
   /**
    * Concrete classes implement this method to communicate whether to allow
    * selection of profiles referencing "All accessible XNAT projects".
    * @return true if "all projects" can be selected false otherwise.
    */
   protected abstract boolean allowAll();
   
     
   /** Using the contents of the file DAOreturnTypes.xml included in the project JAR
    * insert the different types of data that can be returned by the Data Access Object
    * into the combo box.
    */
	private void populateDataTypeJComboBox()
	{
		DefaultComboBoxModel typeDcbm       = new DefaultComboBoxModel();
      ArrayList<String>    forbiddenTypes = new ArrayList<String>(); 
		getDataTypeJComboBox().setModel(typeDcbm);

      DAOReturnTypesList rtl = null;
      try
      {
         rtl = DAOReturnTypesList.getSingleton();
      }
      catch (IOException exIO)
      {
         JOptionPane.showMessageDialog(this, "Programming error - please report to Simon!\n"
               + exIO.getMessage(), "XNAT DAO programming error!", JOptionPane.ERROR_MESSAGE);
         System.exit(1);
      }

      LinkedHashMap<String, Vector<String>>  aliasMap        = rtl.getDAOReturnAliases();
      Set<Map.Entry<String, Vector<String>>> aliasMapEntries = aliasMap.entrySet();

      for (Iterator<Map.Entry<String, Vector<String>>> ime = aliasMapEntries.iterator(); ime.hasNext();)
      {
         Map.Entry<String, Vector<String>> mapEntry = ime.next();

         String typeAlias = mapEntry.getKey();
         typeDcbm.addElement(typeAlias);
         if (!typeIsSelectable(typeAlias)) forbiddenTypes.add(typeAlias);
      }
      
      // Now we need to grey out elements that are not selectable. This is mainly
      // because various datatypes have not yet been implemented.
      ColouredCellRenderer ccr =
           new ColouredCellRenderer<String>(DAOConstants.NON_SELECTABLE_COLOUR);
      ccr.setElementsForType(0, forbiddenTypes);
      getDataTypeJComboBox().setRenderer(ccr);
      
      // Select the first of the allowed elements.
      for (int i=0; i<typeDcbm.getSize(); i++)
         if (typeIsSelectable((String) typeDcbm.getElementAt(i)))
         {            
            getDataTypeJComboBox().setSelectedIndex(i);
            break;
         }
   }


   /**
    * Using the contents of the file DAOreturnTypes.xml included in the project JAR
    * insert the different subtypes of data that can be returned by the Data Access Object
    * into the combo box.
    */
	protected void populateDataSubtypeJComboBox()
	{
      DefaultComboBoxModel subtypeDcbm       = new DefaultComboBoxModel();
      ArrayList<String>    forbiddenSubtypes = new ArrayList<String>(); 
      getDataSubtypeJComboBox().setModel(subtypeDcbm);

      DAOReturnTypesList rtl;
      try
      {
         rtl = DAOReturnTypesList.getSingleton();
      }
      catch (IOException exIO)
      {
         throw new RuntimeException(exIO.getMessage());
      }

      LinkedHashMap<String, Vector<String>> aliasMap = rtl.getDAOReturnAliases();

      String type = (String) getDataTypeJComboBox().getSelectedItem();
      Vector<String> subtypeAliases = aliasMap.get(type);
      for (String subtype : subtypeAliases)
      {
         subtypeDcbm.addElement(subtype);
         if (!subtypeIsSelectable(subtype)) forbiddenSubtypes.add(subtype); 
      }
      
      // Now we need to grey out elements that are not selectable. This is mainly
      // because various datatypes have not yet been implemented.
      ColouredCellRenderer ccr =
           new ColouredCellRenderer<String>(DAOConstants.NON_SELECTABLE_COLOUR);
      ccr.setElementsForType(0, forbiddenSubtypes);
      getDataSubtypeJComboBox().setRenderer(ccr);
      
      // Select the first of the allowed elements.
      for (int i=0; i<subtypeDcbm.getSize(); i++)
         if (subtypeIsSelectable((String) subtypeDcbm.getElementAt(i)))
         {            
            getDataSubtypeJComboBox().setSelectedIndex(i);
            break;
         }
   }


   /**
    * Take appropriate action when the profile changes.
    * This involves either selecting a different profile, adding a new one,
    * editing the current profile, or deleting the current profile.
    * @param evt an ItemEvent returning details of the user's choice.
	 * @param concreteClassActions a boolean determining whether any concrete class actions
	 * will be performed. This is important because in some circumstances, the concrete class
	 * action is to change the authentication status of a menu item and re-call this method
	 * to change the colour of the item in the combo box. We thus don't want to get into an
	 * infinite recursion.
    */
   protected void profileJComboBoxItemStateChanged(ItemEvent evt, boolean concreteClassActions)
   {
      // Note: There are two passes through this method per click: once to tell you
      // that an item has been deselected and once to say that a new item has been chosen.
      // Ignore the first of these.
      if (evt.getStateChange() == ItemEvent.DESELECTED)
      {
         deselectedItem = (String) evt.getItem();
         return;
      }

      // Further down, the item state is reset. It is important not to get into
      // an infinite recursion condition, so set up this lock variable.
      if (ignoreItemChange) return;
      ignoreItemChange = true;
      
      if (deselectedItem != null)
      {
         selectedProfileDidChange = true;
         deselectedItem = null;
      }
      else
         selectedProfileDidChange = false;
      
      JComboBox scb = getProfileJComboBox();

      int n   = scb.getItemCount();
      int ind = scb.getSelectedIndex();

      if ((ind < n-3) && (profileList.size() != 0))
      {            
         if ((profileList.get(ind).getProjectList().size() > 1) && (!allowAll()))
               scb.setSelectedItem(deselectedItem);
         else
         {
            try
            {
               profileList.setCurrentProfile(ind);
            }
            catch (Exception ex)
            {
               // This should never happen since the condition ind < n-3 implies that
               // the JComboBox item must be a valid profile, as long as there are
               // some profiles.
               logger.error("Exception when item " + ind + " selected on profile menu.\n"
                            + "Total number of profiles in list should be " + (n-3));
               throw new RuntimeException("Programming error - please contact Simon");
            }

            logger.debug("Selected profile " + ind);
            XNATProfile xnprf = profileList.getCurrentProfile();
            projectList = xnprf.getProjectList();


            // If the server is already connected, we don't have to do anything and
            // the colouring of the list will be correct already. If not, try to
            // connect and change the colour of the list appropriately. It turns
            // out that the easiest way to do this is to repopulate the JComboBox.
            //if (!xnprf.isConnected()) xnprf.connectWithAuthentication(this);
            tryToSaveProfiles();
            populateProfileJComboBox();
            
            // Subclass-dependent actions when changing profile.
            if (concreteClassActions) profileChangeConcreteClassActions();
         }
      }

      if (ind == n-3)
      {
         XNATProfileEditor dialog = new XNATProfileEditor(this,
                                                    "Add new XNAT profile", null);
         dialog.setVisible(true);
         XNATProfile xnprf = dialog.getProfile();
         if (xnprf != null)
         {
            profileList.add(xnprf);
            logger.debug("Added new profile named " + xnprf.profileName + " to list.");
            try {profileList.setCurrentProfile(xnprf);}
            catch (Exception exIgnore){} // This really shouldn't happen.
            populateProfileJComboBox();
            scb.setSelectedItem(xnprf.getProfileName());
            tryToSaveProfiles();
         }
         else scb.setSelectedIndex(0);
         if (concreteClassActions) profileChangeConcreteClassActions();
      }
      
      
      if (ind == n-2)
      {
         if (profileList.size() == 0) scb.setSelectedIndex(0);
         else
         {
            XNATProfile xnprf = profileList.getCurrentProfile();
            XNATProfileEditor dialog = new XNATProfileEditor(this,
                                                       "Edit selected XNAT profile", xnprf);
            logger.debug("Edited profile named " + xnprf.profileName + " to list.");
            dialog.setVisible(true);
            XNATProfile xnprfEdited = dialog.getProfile();
            if (xnprfEdited != null)
            {
               int pos = profileList.indexOf(xnprf);
               profileList.set(pos, xnprfEdited);
               scb.setSelectedItem(xnprfEdited.getProfileName());
               tryToSaveProfiles();
               projectList = xnprfEdited.getProjectList();
            }
         }
         if (concreteClassActions) profileChangeConcreteClassActions();
      }

      if (ind == n-1)
      {
         if (profileList.size() == 0) scb.setSelectedIndex(0);
         else
         {
            XNATProfile xnprf = profileList.getCurrentProfile();
         
            Object[] options = {"Delete", "Cancel"};       
            int choice = JOptionPane.showOptionDialog(this,
                               "Please confirm that you wish to delete the following profile:\n\n"
                             + "Profile name: " + xnprf.getProfileName() + "\n"
                             + "Server: "   + xnprf.getServerURL().toString() + "\n"
                             + "Userid: "   + xnprf.getUserid() + "\n"
                             + "Projects: " + xnprf.getProjectList().toString(),
                               "Confirm delete",
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.QUESTION_MESSAGE,
                               null,
                               options,
                               options[1]);

            profileList.remove(xnprf);
            logger.debug("Deleted profile named " + xnprf.profileName + " from list.");
            try
            {
               profileList.setCurrentProfile((profileList.size() == 0) ? -1 : 0);
               projectList = profileList.getCurrentProfile().getProjectList();
            }
            catch (Exception ex)
            {
               logger.error("Programming error: this really shouldn't happen");
            }

            populateProfileJComboBox();
            scb.setSelectedIndex(0);
            tryToSaveProfiles();
            if (concreteClassActions) profileChangeConcreteClassActions();
         }
      }
      
      ignoreItemChange = false;
   }
   
   
   protected void tryToSaveProfiles()
   {
      try
      {
         profileList.saveProfiles();
      }
      catch (IOException exIO)
      {
         JOptionPane.showMessageDialog(this,
                                       "A system error is preventing the XML file containing\n"
                                     + "the profile information from being updated.",
                                       "Profile save error",
                                       JOptionPane.ERROR_MESSAGE);
         logger.error("Error while attempting to update profiles XML file:\n"
                      + exIO.getMessage());
      }
   }



   protected void dataSubtypeJComboBoxItemStateChanged(ItemEvent evt)
   {
      // Note: There are two passes through this method per click: once to
      // tell you that an item has been deselected and once to say that a
      // new item has been chosen. Ignore the first of these.
      if (evt.getStateChange() == ItemEvent.DESELECTED) 
      {
         subtypeItemSave = (String) evt.getItem();
         return;
      }

      if (subtypeIsSelectable((String) evt.getItem()))
      {
         int newSubtypeIndex = getDataSubtypeJComboBox().getSelectedIndex();
         DAOReturnTypesList rtl;
         try
         {
            rtl = DAOReturnTypesList.getSingleton();
         }
         catch (IOException exIO)
         {
            throw new RuntimeException(exIO.getMessage());
         }

         LinkedHashMap<String, Vector<String>> typesMap = rtl.getDAOReturnTypes();
         LinkedHashMap<String, Vector<String>> aliasMap = rtl.getDAOReturnAliases();

         String type             = (String) getDataTypeJComboBox().getSelectedItem();
         Vector<String> subtypes = typesMap.get(type);
         Vector<String> aliases  = aliasMap.get(type);
         dataSubtype             = subtypes.elementAt(newSubtypeIndex);
         dataSubtypeAlias        = aliases.elementAt(newSubtypeIndex);

         useSubtype(dataSubtype, subtypes, dataSubtypeAlias);
      }
      
      else
      {
         // See explanation in dataTypeJComboBoxItemStateChanged().
         if ((subtypeItemSave != null) && (subtypeItemSave != evt.getItem()))
            getDataTypeJComboBox().setSelectedItem(subtypeItemSave);
      }
   }


   protected void dataTypeJComboBoxItemStateChanged(ItemEvent evt)
   {
      // Note: There are two passes through this method per click: once to
      // tell you that an item has been deselected and once to say that a
      // new item has been chosen. The first time we go through, we record the
      // item that has been deselected, so that if the new item chosen is
      // forbidden, we can reset the old one.
      if (evt.getStateChange() == ItemEvent.DESELECTED)
      {
         typeItemSave = (String) evt.getItem();
         return;
      }

      if (typeIsSelectable((String) evt.getItem()))
      {
         populateDataSubtypeJComboBox();
      }
      else
      {
         /* If the item isn't selectable, then we need to change it
          * back to what it was previously. There are three problems with
          * doing that here:
          * 1. On the very first call typeItemSave will be null.
          * 2. Temporarily, there may be a situation where the combo box
          *    model's list contains no selectable item. Every time an item
          *    is added to the list, the ItemStateChanged method is called.
          *    However, this is only a transitory situation. If we simply
          *    ignore the request until the list is built, then all will be well.
          * 3. We need to avoid recursion. When we do change the selected item
          *    to revert back to a previous (allowable) state, we generate an
          *    ItemStateChanged event, which will get us back here again. We
          *    can break the cycle by ignoring any event for which the item
          *    newly selected is the same as the item that was deselected and
          *    stored in typeItemSave.
          */
         if ((typeItemSave != null) && (typeItemSave != evt.getItem()))
            getDataTypeJComboBox().setSelectedItem(typeItemSave);
      }     
   }


   /** Method allowing subclasses to use the results obtained from the
    *  dataSubtypeJComboBoxItemStateChanged for their own purposes.
    */
   public abstract void useSubtype(String subtype, Vector<String> subtypes,
                                   String subtypeAlias);


   /**
    * Method allowing subclasses to check, according to their own criteria
    * whether a given type can be selected from the ComboBox. Typically, it
    * might not be selectable because the given functionality has not yet been
    * built.
    * @param type String containing the name of the type to check
    * @return true if the given type entry should be selectable
    */
   public abstract boolean typeIsSelectable(String type);
   
   
   
   /**
    * Method allowing subclasses to check, according to their own criteria
    * whether a given subtype can be selected from the ComboBox. Typically, it
    * might not be selectable because the given functionality has not yet been
    * built.
    * @param subtype String containing the name of the subtype to check
    * @return true if the given type entry should be selectable
    */
   public abstract boolean subtypeIsSelectable(String subtype);



   /**
    * Method allowing subclasses to take specific action when the
    * profile has been changed.
    */
   protected void profileChangeConcreteClassActions()
   {
      // The default is to do nothing.
   }



   /**
    * This method allows subclasses to set restrictions determining among
    * other things which types and subtypes are selectable.
    */
   public abstract void implementRestrictions();
   
   
   /**
    * Implement the timer event that checks to see whether the current
    * authentication is still valid and disconnect if it is not.
    * @param e
    */
   @Override
   public void actionPerformed(ActionEvent e)
   {
      // Stop several requests stacking up if the user takes a long time to
      // reauthenticate.
      if (authenticationInProgress) return;
      
      if (e.getSource().equals(authTimer))
      {
         for (XNATProfile iprf : profileList)
         {
            if (System.currentTimeMillis() > (iprf.getLastAuthenticationTime()
                                                 + DAOConstants.AUTHENTICATION_EXPIRY))
               iprf.disconnect();
               authenticationInProgress = true;
               populateProfileJComboBox();
               authenticationInProgress = false;
         }
      }
   }
   
   
   /** Getter method to allow a method defined in this superclass to have
    *  access to the subclass's variable. Each different subclass of XNATGUI
    *  will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data type JcomboBox.
    */
   public abstract JComboBox getDataTypeJComboBox();


   /** Getter method to allow a method defined in this superclass to have
    *  access to the subclass's variable. Each different subclass of XNATGUI
    *  will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data subtype JcomboBox.
    */
   public abstract JComboBox getDataSubtypeJComboBox();


   /** Getter method to allow a method defined in this superclass to have
    *  access to the subclass's variable. Each different subclass of XNATGUI
    *  will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the project JcomboBox.
    */
   public abstract JComboBox getProfileJComboBox();
   
   
   /**
    * External API: Get the current version of the DataChooser.
    * @return String containing version number 
    */
   public String getVersion()
   {
      return version;
   }
   
   
   public String getDicomRemapEx()
   {
      return dicomRemapEx;
   }
   
   
   public static String getHomeDir()
   {
      String homeDir            = System.getProperty("user.home");
      String fileSep            = System.getProperty("file.separator");
      
      return homeDir + fileSep + ".XNAT_DAO" + fileSep;     
   }

}
