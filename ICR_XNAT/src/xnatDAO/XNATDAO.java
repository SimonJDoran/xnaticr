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
* Java class: XNATDAO.java
* First created on Dec 7, 2010, 9:25:00 AM
* 
* Main GUI for XNAT DataChooser
* Historical note: DAO stands for Data Access Object, the original
* name of this tool.
*********************************************************************/

package xnatDAO;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import generalUtilities.ColouredCellRenderer;
import generalUtilities.SimpleColourTable;
import generalUtilities.Vector2D;
import imageUtilities.DownloadIcon;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import treeTable.DAOTreeTableSettings.SettingsForSubtype;
import treeTable.*;
import xnatRestToolkit.RESTSearchWorker;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATReturnedField;



public final class XNATDAO extends XNATGUI
{
   static  XNATDAO                    singletonXNATDAO     = null;
   static  Logger                     logger               = Logger.getLogger(XNATDAO.class);
   private String[]                   tableColumns         = null;
   private int                        nTableColumns;
   private int                        returnCode;
   private RESTSearchWorker           searchWorker;
   private FileListWorker             fileListWorker;
   private DAOOutput                  daoo;
   private ArrayList<ArrayList<File>> fileList;
   private static final int           DATA_SELECTED        = 0;
   private boolean                    ignoreSettingsChange = false;
   private DAOTreeTableSettingsList   settingsList;
   private Vector2D                   latestQueryResult;
   private boolean                    queryInProgress      = false;
   private boolean                    isLazySearch         = true;
   private TreeNode[]                 expansionNodePath;
   private int                        oldFirstRow          = 0;
   private int                        oldLastRow           = 0;
   private XndWrapper                 xndw = null;
   


   /** Creates new form XNATDAO (Data Access Object) */
	protected XNATDAO(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);

      initComponents();
      jSplitPane1.setOneTouchExpandable(true);
      addListeners();
      settingsList = new DAOTreeTableSettingsList();
      downloadIcon = new DownloadIcon(submitQueryJButton);
      submitQueryJButton.setIcon(downloadIcon);
      populateCommonComponents();
      populateSettingsJComboBox();
      updateLeafComboBox();
      implementRestrictions();
      setDefaultSearch();
      downloadingJLabel.setVisible(false);
      downloadDetailsJLabel.setVisible(false);
      downloadJProgressBar.setVisible(false);
      thumbnailPreview1.start();
      status = "No files chosen yet";
      selectDataJButton.setEnabled(false);
      versionJLabel.setText("Version " + version);
	}
   
   
   

   
   /* =====================================
    * External API for client applications
    * =====================================
    * 
    * Note that other methods are declared public for lower level
    * integration with other packages and general requirements for
    * over-ridden methods. But these are the ones specifically
    * aimed at when the object is included in other people's code
    * e.g., in IDL, MATLAB, etc.
    */
   

   /**
    * Call the program directly from the command line. It is not intended
    * that this be the norm, but it is useful to have a main() method for use
    * with the IDE.
    * 
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
      if ((args.length == 0) || (!args[0].equals("testAPI")))
      {
         // Normal startup
         java.awt.EventQueue.invokeLater(new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               }
               catch (Exception exIgnore) {}

               XNATDAO xndao = XNATDAO.getSingleton(new javax.swing.JFrame(), true);

               xndao.invoke(false, true);
            }
         });
      }
      else
      {
         // Test the external API.
         java.awt.EventQueue.invokeLater(new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               }
               catch (Exception exIgnore) {}

               XNATDAO xndao = XNATDAO.getSingleton(new javax.swing.JFrame(), true);

               TestDAOExternalAPI.runTest(xndao);
            }
         });
      }
         
	}


   /**
    * External API: Access an instance of XNATDAO according to the Singleton design pattern.
    * Almost all the time, this is the way that we will want to instantiate
    * the XNATDAO, i.e., as a modal dialog with a new Frame.
    * @return a singleton instance of XNATDAO
    */
   public static XNATDAO getSingleton()
   {
      if ( singletonXNATDAO == null )
		{
         singletonXNATDAO = new XNATDAO(new javax.swing.JFrame(), true);
		}
      
      return singletonXNATDAO;
   }
   
   
   /**
    * External API: Access an instance of XNATDAO according to the Singleton design pattern.
    * Occasionally, we might wish to specify the parent Frame and permit the
    * JDialog to be non-modal.
    * @param parent
    * @param modal
    * @return a singleton instance of XNATDAO.
    */
   public static XNATDAO getSingleton(java.awt.Frame parent, boolean modal)
   {
      if ( singletonXNATDAO == null )
		{
         singletonXNATDAO = new XNATDAO(parent, modal);
		}

		return singletonXNATDAO;
   }
   
   
   /**
    * Check to see whether an XNATDAO object has already been created.
    * @return true if the object has already been instantiated, false otherwise
    */
   public static boolean alreadyInstantiated()
   {
      return singletonXNATDAO != null;
   }

   
   /**
    * External API: Put the dialogue onto the screen and wait for a selection
    * from the user.
    * @param initiateSearch true if a search should be started immediately with
    * the parameters currently set and without waiting for user interaction.
    * @param invokedByRun true if the application has been invoked from the run
    * command in an IDE. In normal use, false.
    * 
    * Notes:
    * (1) This method returns false to the caller immediately. However,
    * when created, the XNATDAO object can either be a modal or non-
    * modal JDialog. If modal, the UI should be locked while it is displayed, but
    * the behaviour still needs investigating for the case where this is called
    * from a non-Java environment.
    * 
    * (2) The selection might involve transfer of data from a database that is
    * not on the local machine. This task is performed asynchronously. The UI
    * does not block: "Cancel" can be pressed or the user can highlight a
    * different row of the table, but the "Select" button is disabled until all
    * of the data are present locally.
	 */
	public void invoke(boolean initiateSearch, boolean invokedByRun)
	{
      // If we have started up the code from the command line, or from the IDE,
      // then clicking cancel should cause the JVM to exit. If invoked programmatically,
      // then the object should stick around to receive queries from the API.
      this.invokedByRun = invokedByRun;
      if (initiateSearch)
      {
         expansionNodePath = dAOTreeTable1.getTreeModel().getRootNode().getPath();
         invokeXNATQuery();
      }
      setVisible(true);
	}
   
   /**
    * External API: Put the dialogue onto the screen and wait for a selection
    * from the user.
    * @param initiateSearch true if a search should be started immediately with
    * the parameters currently set and without waiting for user interaction.
    */
   public void invoke(boolean initiateSearch)
   {
      invoke(initiateSearch, false);
   }

   
   /**
    * This variant of the method allows us to pass control back to the wrapper on
    * exit.
    * @param xndw wrapper object from which the XNATDAO might be called  
    */
   public void invoke(boolean invokedByRun, boolean initiateSearch, XndWrapper xndw)
   {
      this.xndw = xndw;
      invoke(invokedByRun, initiateSearch);
   }
 
   
   /**
    * External API: reset the DataChooser parameters to the default.
    * 
    */
   public void reset()
   {
      setTypeSubtype("Set of images", "MR image set", true);
      implementRestrictions();
      setDefaultSearch();
      dAOTreeTable1.clearTreeTable();
      thumbnailPreview1.stop();
      thumbnailPreview1.clearImages();
      thumbnailPreview1.start();
      downloadingJLabel.setVisible(false);
      downloadDetailsJLabel.setVisible(false);
      downloadJProgressBar.setVisible(false);
      status = "No files chosen yet";
      selectDataJButton.setEnabled(false);
   }
   

   
   /**
    * External API: find out the status of the file-retrieve operation.
    *
    * @return the status String: possible values "Retrieving", "Succeeded", "Failed"
    */
   public String getStatus()
   {
      if (!( (status.equals("Cancelled")) || (status.equals("Succeeded - data selected")) ||
             (daoo == null) ))
         return daoo.getStatus();
      
      else return status;
   }

   

   /**
    * External API: retrieve the file names in their "internal" format of an
    * ArrayList<ArrayList<File>>.
    *
    * @return list of files on the local filesystem corresponding to the data
    * chosen
    */
   public ArrayList<ArrayList<File>> getOutputFileArrayList()
   {
      if (daoo == null)  return null;
      else               return fileList;
   }

      
   /**
    * External API: retrieve the number of separate results obtained.
    * This is the same as the number of leaves selected in the tree table.
    * If we are downloading XNAT scans (= DICOM series), then this is the
    * number of series. Each series will, in general, have a number of image
    * files associated with it.
    *
    * @return an int containing the number of separate results selected.
    */
   public int getNumberOfSelections()
   {
      if (fileList == null) return 0;
      else                  return fileList.size();
   }
   
   
   
   /**
    * External API: retrieve the total number of files selected by the user.
    * If the datatype is one for which there is a single file underlying each
    * selection then this will be the same as the number of leaf elements
    * selected in the tree table. However, if we are downloading, for example,
    * DICOM series, then each will, in general, have a number of image files
    * associated.
    * 
    * @return an int containing the total number of files 
    */
   public int getTotalNumberOfFiles()
   {
      if (fileList == null) return -1;
      int nFiles = 0;
      for (int i=0; i<fileList.size(); i++)
         nFiles += fileList.get(i).size();
      
      return nFiles;
   }

   

   /**
    * External API: retrieve the number of files associated with a particular
    * result.
    * E.g., if we are downloading XNAT scans (= DICOM studies),
    * then this is the number of individual DICOM files associated with the nth
    * chosen study.
    *
    * @return an integer containing the number of files for the nth element of the selection.
    */
   public int getNumberOfFilesForSelection(int n)
   {
      if (fileList == null)       return -1;
      if (n > fileList.size()-1 ) return -1;
      else                        return fileList.get(n).size();
   }


   
   /**
    * External API: retrieve the file paths associated with a particular selection.
    * E.g., if we are downloading XNAT scans (= DICOM studies),
    * then these are the local paths to the DICOM files for the nth scan.
    *
    * @return a String array containing the DICOM files for the nth element of the selection.
    */
   public String[] getFilePathsForSelection(int n)
   {
      if (fileList == null) return null;
      ArrayList<File> resultN = fileList.get(n);
      int             nFiles  = resultN.size();

      String[] filePaths = new String[nFiles];
      for (int i=0; i<nFiles; i++)
         filePaths[i] = resultN.get(i).getAbsolutePath();

      return filePaths;
   }
   
   
   
   /**
    * External API: retrieve the file paths selected
    *
    * @return a String array containing the full paths of all the files
    * (e.g., DICOM images) for the selected output.
    */
   public String[] getAllFilePaths()
   {
      if (fileList == null) return null;
      
      int nFiles = 0;
      for (int i=0; i<fileList.size(); i++)
         nFiles += fileList.get(i).size();
      
      String[] filePaths = new String[nFiles];
      int count = 0;
      for (int i=0; i<fileList.size(); i++)
      {
         ArrayList<File> resultI = fileList.get(i);
         for (int j=0; j<resultI.size(); j++)
            filePaths[count++] = resultI.get(j).getAbsolutePath();
      }

      return filePaths;
   }


   
   /**
    * External API: retrieve the directory associated
    * with a particular result. E.g., if we are downloading XNAT scans (= DICOM studies),
    * then this is the local directory to the DICOM files for the nth scan.
    *
    * @return a String array containing the DICOM files for the nth element of the selection.
    */
   public String getDirectoryForSelection(int n)
   {
      if (fileList == null) return null;
      File firstFile = fileList.get(n).get(0);

      String dir = firstFile.getParent();

      return dir;
   }


   
   /**
    * External API: Set profile programmatically
    * Returns silently if the supplied profile does not match any in the list.
    * @param profileName 
    */
   public void setProfile(String profileName)
   {
      for (int i=0; i<profileJComboBox.getItemCount(); i++)
         if (profileName.equals(profileJComboBox.getItemAt(i)))
         {
            profileJComboBox.setSelectedIndex(i);
            break;
         }      
   }
   
   
   /**
    * External API: Get current profile programmatically
    * @return a String containing the current profile name 
    */
   public String getProfile()
   {
      return (String) profileJComboBox.getSelectedItem();      
   }
   
   
 /**
    * External API: Set programmatically the (alias of the) XNAT element used
    * for determining the leaf information displayed. This is equivalent to
    * using the combo box in the UI to set the leaf element.
    * 
    * Returns silently if the supplied profile does not match any in the list.
    * @param profileName 
    */
   public void setLeafElement(String profileName)
   {
      for (int i=0; i<leafDAOElementsComboBox.getItemCount(); i++)
         if (profileName.equals(leafDAOElementsComboBox.getItemAt(i)))
         {
            leafDAOElementsComboBox.setSelectedIndex(i);
            break;
         }      
   }
   
   
   /**
    * External API: Get programmatically the (alias of the) XNAT element used
    * for determining the leaf information displayed.
    * 
    * @return a String containing the current profile name 
    */
   public String getLeafElement()
   {
      return (String) leafDAOElementsComboBox.getSelectedItem();      
   }
   
   
   /**
    * External API: Set current TreeTable settings programmatically.
    * This is equivalent to using the combo box in the UI to choose the desired
    * TreeTable settings from the list.
    * 
    * Returns silently if the supplied profile does not match any in the list.
    * @param profileName 
    */
   public void setCurrentSettings(String settingsName)
   {
      for (int i=0; i<settingsJComboBox.getItemCount(); i++)
         if (settingsName.equals(settingsJComboBox.getItemAt(i)))
         {
            settingsJComboBox.setSelectedIndex(i);
            break;
         }      
   }
   
   
   /**
    * External API: Get current profile programmatically.
    * @return a String containing the current profile name 
    */
   public String getCurrentSettings()
   {
      return (String) settingsJComboBox.getSelectedItem();      
   }
   
   
   /**
    * External API: Change selection mode for the tree table 
    * @param mode simplified selection mode name as String 
    */
   public void setTableSelectionMode(String mode)
   {
      Outline outline = dAOTreeTable1.getOutline();
      
      if (mode.equals("SINGLE"))
         outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      if (mode.equals("SINGLE_CONTIGUOUS"))
         outline.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      
      if (mode.equals("SINGLE_CONTIGUOUS"))
         outline.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
   }
   
   
   
   /**
    * External API: Modify the combo boxes in the GUI to select the data type
    * and subtype returned by the DataChooser. Note: If the parameters are not
    * valid, the method just does nothing (silently).
    * @param type a valid String element from the drop-down list of types
    * @param subtype a valid String element in the drop-down list of types
    *  (Note that this may have been modified by the preceding change to type.)
    * @param changeable false if the control should be disabled for user change
    * after the method call
    */
   public void setTypeSubtype(String type, String subtype, boolean changeable)
   {
      /* TODO Check that type/subtype combination is valid and implemented. */
      if (type !=null)
      {
         dataTypeJComboBox.setSelectedItem(type);
         dataTypeJComboBox.setEnabled(changeable);
      }
      
      if (subtype != null)
      {
         dataSubtypeJComboBox.setSelectedItem(subtype);
         dataSubtypeJComboBox.setEnabled(changeable);
      }
   }

   
   
   /**
    * External API: Create a new set of search criteria for the DataChooser.
    * @return DAOSearchCriteriaAPI object. Whilst not complicated
    * in structure, this object is designed to be manipulated by the API here.
    */
   public DAOSearchCriteriaSet createNewSearchCriteriaSet()
   {
      return new DAOSearchCriteriaSet(dAOSearchCriteria1);
   }
   
   
   /**
    * Esternal API: Add a new criterion to the set of search criteria.
    * @param dsc a previously created set of search criteria (could be empty)
    * @param comparisonElement the XNAT schema element to compare as a String
    * @param comparisonOperator the comparison operator (e.g., LIKE, =, >) as a String
    * @param comparisonString  the String to compare the element against
    */
   public void addSearchCriterion(DAOSearchCriteriaSet dsc,
                                  String comparisonElement,
                                  String comparisonOperator,
                                  String comparisonString)
   {
      dsc.addSearchCriterion(comparisonElement, comparisonOperator, comparisonString);
   }
   
  
   /**
    * External API: Check whether the supplied search criteria are valid.
    * The criteria will be judged valid if:
    * (a) there is at least one criterion;
    * (b) the comparison elements are all in the list for the data type chosen;
    * (c) the comparison operators are all in the valid list;
    * (d) the comparison strings all contain only valid characters.
    * @param dsc search criteria as appropriate Java object
    * @return true if criteria are valid, false otherwise.
    */
   public boolean searchCriteriaValid(DAOSearchCriteriaSet dsc)
   {
      return dsc.searchCriteriaValid();
      
      // TODO: Provide a method to report errors in the criteria, so that
      // users can work out what they are doing wrong.
   }
   
   
   /**
    * External API: Apply the chosen search criteria to the DataChooser.
    * @param dsc a previously created set of search criteria 
    */
   public void applySearchCriteria(DAOSearchCriteriaSet dsc)
   {
      dsc.applySearchCriteria();
   }
   
   
   /**
    * External API: Get the version of the currently running DataChooser.
    * @return the version as a String
    */
   public String getVersion()
   {
      return version;
   }
   
   /********************* End of external API ***********************/

   
   /**
    * Add the listeners that allow actions to be implemented when the
    * users click on relevant buttons.
    */
   private void addListeners()
   {
      dAOSearchCriteria1.addChangeListener(new ChangeListener()
      {
         @Override
         public void stateChanged(ChangeEvent evt)
         {
            checkSubmitQueryButtonState();
         }
		});

      
      dAOTreeTable1.addDAOTreeTableSelectionListener(new DAOTreeTableSelectionListener()
      {
         @Override
         public void rowSelectionChanged(DAOTreeTableSelectionEvent evt)
         {
            respondToRowSelection(evt);
         }
      });
      
      
     
      
      selectDataJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (dAOTreeTable1.getOutline().getSelectedRow() != -1)
            {
               fileList = daoo.getFileList();
               status   = daoo.getStatus();
               if (status.equals("Download succeeded - awaiting selection by user"))
                  status = "Succeeded - data selected";
            }

            //dispose();
            setVisible(false);
            
            // If we have started up the code from the command line, or from the IDE,
            // then clicking cancel should cause Java to exit. 
            if (invokedByRun) System.exit(0);

			}	  
		});


      submitQueryJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
            expansionNodePath = dAOTreeTable1.getTreeModel().getRootNode().getPath();
            invokeXNATQuery();
			}
		});


      cancelJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
            status = "Cancelled";
            setVisible(false);
				//dispose();  
            
            // If we have started up the code from the command line, or from the IDE,
            // then clicking cancel should cause Java to exit. 
            if (invokedByRun) System.exit(0);            
			}	  
		});
      

      settingsJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            settingsJComboBoxItemStateChanged(evt);
         }
      });
      

      leafDAOElementsComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            leafDAOElementsComboBoxItemStateChanged(evt);
         }
      });
      

      profileJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            profileJComboBoxItemStateChanged(evt);
         }
      });


      dataSubtypeJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            dataSubtypeJComboBoxItemStateChanged(evt);
         }
      });


      dataTypeJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            dataTypeJComboBoxItemStateChanged(evt);
         }
      });
   }


   /**
    * Return the state of the dialogue on exit. Possible values are:
    * DATA_SELECTED - the user pressed the "Select" button with a valid
    *                 choice from the tree table.
    * CANCELLED     - the user pressed the cancel button
    * @return return code as an int
    */
   public int getReturnCode()
   {
      return returnCode;
   }



   /**
    * Use the search criteria on the left of the display to query
    * the connected XNAT database and fill the tree table in the right panel.
    */
   private void invokeXNATQuery()
	{
      XNATProfile xnprf = profileList.getCurrentProfile();
      if (!xnprf.isConnected()) xnprf.connectWithAuthentication(this);
      if (!xnprf.isConnected()) return;
      populateProfileJComboBox();
      tryToSaveProfiles();
      
      XNATRESTToolkit xnrt = new XNATRESTToolkit(xnprf);
      
      // Use a separate class to obtain information on the tree table rather than get
      // it directly, because recreateTreeTable() also needs access to the same info.
      TreeTableProperties    ttp = new TreeTableProperties();   
      XNATReturnedField[] distinctFields;
      int nNodes = expansionNodePath.length;
      if (isLazySearch && (nNodes < ttp.treeElements.size()-1))
      {
         // Case 1: Lazy search and expansion will lead to a set of non-leaf nodes.
         distinctFields = new XNATReturnedField[nNodes];
         for (int i=0; i<nNodes; i++)
            distinctFields[i] = new XNATReturnedField(ttp.rootElement,
                                ttp.treeElements.elementAt(i), "string");
      }
      else
      {
         // Case 2: Non-lazy search or lazy search expanding to set of leaves.
         int n = ttp.tableColumnElements.size();
         distinctFields = new XNATReturnedField[n];
         for (int i=0; i<n; i++)
            distinctFields[i] = new XNATReturnedField(
               ttp.rootElement, ttp.tableColumnElements.elementAt(i), "string");
      }
         
      
      // Perform the actual search in XNAT. This will execute in the background and,
      // once done, will trigger the listener, which will then update the tree table.
      downloadIcon.start();
      dAOTreeTable1.setEnabled(false);
      queryInProgress = true;
      
      searchWorker = new RESTSearchWorker(this, xnprf,
               ttp.rootElement,
               distinctFields,
               dAOSearchCriteria1.getDAOSearchCriterion(1).getCombinationOperator(),
               dAOSearchCriteria1.getXNATCriteria(),
               xnprf.getProjectList(),
               expansionNodePath);

      searchWorker.addPropertyChangeListener(new RESTSearchWorkerListener());
      submitQueryJButton.setEnabled(false);
      searchWorker.execute();
   }
   



   /**
    * Listen for the completion of the SearchWorker thread and update
    * the tree table when done.
    */
   private class RESTSearchWorkerListener implements PropertyChangeListener
   {
      public RESTSearchWorkerListener()
      {
      }

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
         // Ignore this call if the property change in question is that from "PENDING"
         // to "STARTED".
         if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;

         dAOTreeTable1.setEnabled(true);
         try
         {
            latestQueryResult = XNATDAO.this.searchWorker.get();
            
            // Current query has finished executing. OK to resubmit.
            submitQueryJButton.setEnabled(true);
            
         }
         catch (InterruptedException exIE)
         {
            return;
         }
         catch (ExecutionException exEE)
         {
            JOptionPane.showMessageDialog(XNATDAO.this,
                 "I was unable to retrieve the virtual directory list for your criteria\n"
                     + "for the following reason:\n"
                     + exEE.getMessage(),
                 "Failed to retrieve filenames",
                 JOptionPane.ERROR_MESSAGE);
            return;
         }
         finally
         {
            downloadIcon.stop();
            queryInProgress = false;
            logger.debug("RESTSearchWorkerListener exited");
         }

         if (!latestQueryResult.isEmpty())
            recreateTreeTable(latestQueryResult, expansionNodePath);
      }
   }


   /**
    * Compile information about what should be displayed in the tree table.
    */
   private class TreeTableProperties
   {
      String         rootElement;
      Vector<String> tableColumnAliases;
      Vector<String> tableColumnElements;
      Vector<String> treeElements;
      Vector<String> searchElements;
      int            leafIndex;


      public TreeTableProperties()
      {
         // Note that the XNAT REST search API allows combinations of multiple
         // operators to be chosen, but XNATRESTToolkit.createSearchXML doesn't
         // yet take advantage of this - hence the use of getDAOSearchCriterion(1) below,
         // i.e., we make use only of the first selection. I hope this will be
         // rectified in the future.
         rootElement = getCurrentSubtypeXPath();
         
         DAOSearchableElementList sel=null;
         try
         {
            sel = DAOSearchableElementList.getSingleton();
         }
         catch (IOException exIO)
         {
            JOptionPane.showMessageDialog(XNATDAO.this, "Programming error - please report to Simon!\n"
                  + exIO.getMessage(), "XNAT DAO programming error!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
         }
         LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATAliases();
         tableColumnAliases  = sel.getSearchableXNATAliases().get(rootElement);
         tableColumnElements = sel.getSearchableXNATElements().get(rootElement);


         // Collect information about what data to request for the tree structure.
         treeElements = dAOSearchCriteria1.getDistinctXPaths();

         // Convert from the leaf display alias that is selected in the UI to the
         // corresponding schema element name.
         String leafAlias   = (String) leafDAOElementsComboBox.getSelectedItem();
         leafIndex          = tableColumnAliases.indexOf(leafAlias);
         String leafDisplay = tableColumnElements.elementAt(leafIndex);
         
         // Important distinction! The element used as the node name for the leaves
         // of the tree is different from the element which is displayed. This is
         // because each leaf has to be unique in order for the tree to be displayed
         // correctly. However, we want to give users control over what they
         // display for the leaves, in order for them not to have to list
         // unique elements such as non-human-readable DICOM names. But what
         // people want to put there might not be unique.
         String leafElement = getLeafElement(rootElement);
         
         treeElements.add(leafElement);
         treeElements.add(leafDisplay);
                  
         
         // Now do the same for the table.
         nTableColumns = tableColumnAliases.size();

         // We will return the tree data and the table data from the same XNAT 
         // query, so combine the two sets of elements.
         searchElements  = new Vector<String>();
         searchElements.addAll(treeElements);
         searchElements.addAll(tableColumnElements);
      }
   }


   /**
    * Refresh the tree table with the results of an XNAT query.
    * @param distinctQueryResult a Vector2D containing the information to display
    */
   private void recreateTreeTable(Vector2D   distinctQueryResult,
                                  TreeNode[] expansionNodePath)
   {
      // Occasionally this routine may be called with no valid data. If this
      // occurs, just return without attempting to display anything.
      if (distinctQueryResult == null) return;
      
      TreeTableProperties ttp = new TreeTableProperties();

      int nTreeColumns        = ttp.treeElements.size();
      int nRows               = distinctQueryResult.size();

      int nCols               = nTreeColumns + nTableColumns;

      
      if (isLazySearch && (expansionNodePath.length<nTreeColumns-1))
      {
         dAOTreeTable1.refresh(distinctQueryResult,
                              ttp.treeElements,
                              ttp.tableColumnAliases,
                              ttp.rootElement,
                              dataSubtypeAlias,
                              settingsList,
                              isLazySearch,
                              expansionNodePath);
      }
      else
      {
         Vector2D queryResult    = new Vector2D<String>();

         // Because of the way the XNAT searches work, the query only returns
         // distinct columns regardless of whether any fields are repeated in
         // what is passed to xnrt.search above. In order to populate the tree
         // and the table properly, we now need to reconstruct any duplicated columns.
         for (int i=0; i<nCols; i++)
         {
            int columnInDistinct = ttp.tableColumnElements.indexOf(ttp.searchElements.elementAt(i));
            for (int j=0; j<nRows; j++)
            {
               if (i == 0) queryResult.add(new Vector<String>());
               Vector<String> row = queryResult.getRow(j);
               row.add((String) distinctQueryResult.atom(columnInDistinct, j));
               queryResult.replaceRow(row, j);
            }
         }

         dAOTreeTable1.refresh(queryResult,
                              ttp.treeElements,
                              ttp.tableColumnAliases,
                              ttp.rootElement,
                              dataSubtypeAlias,
                              settingsList,
                              isLazySearch,
                              expansionNodePath);
      }
      
       
      // Refreshing the tree table will a new outline model if we have a new.
      // search with different table columns. Since it is
      // this that provides the link through to the listener that will tell
      // us about tree expansion events, we need to register the listner here
      final DAOOutline o = dAOTreeTable1.outline;
      if (expansionNodePath.length == 1)
         o.getOutlineModel().getTreePathSupport()
          .addTreeExpansionListener(new TreeExpansionListener()
         {
            @Override
            public void treeExpanded(TreeExpansionEvent tee)
            {
               if ((isLazySearch) && (!o.isDummyExpand()))
               {
                  expandTree(tee.getPath());
               }   
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee)
            {
               // Leave the TreeTable to do its own thing.
            }
                 
         });
	}
   
   
   private void expandTree(TreePath path)
   {
      int nNodes        = path.getPathCount();
      expansionNodePath = new DAOMutableTreeNode[nNodes];
      for (int i=0; i<nNodes; i++)
         expansionNodePath[i] = (DAOMutableTreeNode) path.getPathComponent(i);
      
      
      DAOMutableTreeNode    nd = (DAOMutableTreeNode) expansionNodePath[nNodes-1];
      DAOTreeNodeUserObject uo = nd.getUserObject();
      
      // Check whether this node has been previously expanded. If so nothing
      // else needs to be done; we just let the TreeTable display the underlying
      // nodes.
      DAOMutableTreeNode    child = (DAOMutableTreeNode) nd.getFirstChild();
      DAOTreeNodeUserObject cuo   = child.getUserObject();
      if (!cuo.getDisplayName().equals(DAOTreeModel.FETCHING)) return;
      downloadIcon.start();
      invokeXNATQuery();      
   }
   


   /**
    * Override the timer event that checks to see whether the current
    * authentication is still valid and disconnect if it is not. This is
    * the same as the version in XNAT_GUI, except that we do not invalidate
    * a currently downloading profile.
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
            {
               // Don't try and disconnect a currently running download.
               if (!(queryInProgress && (iprf.equals(profileList.getCurrentProfile()))))
                  iprf.disconnect();
            }
            authenticationInProgress = true;
            populateProfileJComboBox();
            authenticationInProgress = false;
         }
      }
   }

   private void populateSettingsJComboBox()
   {
      final String ADD_TEXT     = "Save as new settings...";
      final String UPDATE_TEXT  = "Update current settings";
      final String DELETE_TEXT  = "Delete current settings";

		DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
      settingsJComboBox.setModel(dcbm);

      ColouredCellRenderer ccr
              = new ColouredCellRenderer<String>(DAOConstants.NON_SELECTABLE_COLOUR);
      settingsJComboBox.setRenderer(ccr);

      dcbm.insertElementAt(ADD_TEXT,    0);
      dcbm.insertElementAt(UPDATE_TEXT, 1);
      dcbm.insertElementAt(DELETE_TEXT, 2);

      if (settingsList.isEmpty())
      {
         logger.error("Empty table settings - this shouldn't happen, "
                      + "as the defaults should always be there.");
         return;
      }
      
      ArrayList<String> selectableList    = new ArrayList<String>();
		ArrayList<String> nonSelectableList = new ArrayList<String>();
      for (DAOTreeTableSettings itts : settingsList)
      {
         int n = dcbm.getSize();
         dcbm.insertElementAt(itts.getName(), n-3);
         selectableList.add(itts.getName());
      }
      
      dcbm.setSelectedItem(settingsList.getCurrentSettingsName());
      settingsJComboBox.setForeground(Color.BLACK);
      
   }



   /**
    * Take appropriate action when the chosen table settings change.
    * This involves either selecting different settings, adding new settings,
    * updating the current settings, or deleting the current settings.
    * @param evt and ItemEvent returning details of the user's choice.
    */
   protected void settingsJComboBoxItemStateChanged(ItemEvent evt)
   {
      // Note: There are two passes through this method per click: once to tell you
      // that an item has been deselected and once to say that a new item has been chosen.
      // Ignore the first of these.
      if (evt.getStateChange() == ItemEvent.DESELECTED) return;

      // Further down, the item state is reset. It is important not to get into
      // an infinite recursion condition, so set up this lock variable.
      if (ignoreSettingsChange) return;
      ignoreSettingsChange = true;
      
      int        n       = settingsJComboBox.getItemCount();
      int        ind     = settingsJComboBox.getSelectedIndex();
      DAOOutline outline = dAOTreeTable1.getOutline();

      if ((ind < n-3) && (settingsList.size() != 0))
      {
         try
         {
            settingsList.setCurrentSettings(ind);
            dAOTreeTable1.implementSettings(settingsList, dataSubtypeAlias);
            updateLeafComboBox();
            tryToSaveTableSettings(false);
         }
         catch (Exception ex)
         {
            // This should never happen since the condition ind < n-3 implies that
            // the JComboBox item must be a valid profile, as long as there are
            // some profiles.
            logger.error("Exception when item " + ind + " selected on table settings combo box.\n"
                         + "Total number of profiles in list should be " + (n-3));
            throw new RuntimeException("Programming error - please contact Simon");
         }

      }

      if (ind == n-3)
      {
         TableSettingsNameEntry dialog = new TableSettingsNameEntry(this,
                                          "Choose the name for these table settings");
         dialog.setVisible(true);
         String name = dialog.getSettingsName();
         String leafDisplayAlias = (String) leafDAOElementsComboBox.getSelectedItem();
         if (name != null)
         {
            DAOTreeTableSettings tts
               = new DAOTreeTableSettings(name, dataSubtypeAlias, outline,
                                          leafDisplayAlias);
            settingsList.add(tts);
            logger.debug("Added new table settings named " + name + " to list.");

            try {settingsList.setCurrentSettings(tts);}
            catch (Exception exIgnore){} // This really shouldn't happen.
            populateSettingsJComboBox();
            settingsJComboBox.setSelectedItem(name);
            tryToSaveTableSettings(true);
         }
         else settingsJComboBox.setSelectedIndex(0);
      }
      
      
      if (ind == n-2)
      {
         if (settingsList.size() == 0) settingsJComboBox.setSelectedIndex(0);
         else
         {
            String leafDisplayAlias  = (String) leafDAOElementsComboBox.getSelectedItem();
            DAOTreeTableSettings tts = settingsList.getCurrentTableSettings();
            if (tts.containsKey(dataSubtypeAlias))
               tts.removeSettingsForDataSubtype(dataSubtypeAlias);
            tts.addSettingsForDataSubtype(dataSubtypeAlias, outline, leafDisplayAlias);          
            logger.debug("Replacing settings named " + tts.getName() + ".");
            tryToSaveTableSettings(true);
            settingsJComboBox.setSelectedItem(tts.getName());
         }
      }

      if (ind == n-1)
      {
         if (settingsList.size() == 0) settingsJComboBox.setSelectedIndex(0);
         else
         {
            DAOTreeTableSettings tts = settingsList.getCurrentTableSettings();
         
            Object[] options = {"Delete", "Cancel"};       
            int choice = JOptionPane.showOptionDialog(this,
                               "Please confirm that you wish to delete the "
                               + "table settings \"" + tts.getName() + "\".",
                               "Confirm delete",
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.QUESTION_MESSAGE,
                               null,
                               options,
                               options[1]);

            settingsList.remove(tts);
            logger.debug("Deleted table settings named \"" + tts.getName() + "\" from list.");
            try
            {
               settingsList.setCurrentSettings((settingsList.size() == 0) ? -1 : 0);
            }
            catch (Exception ex)
            {
               logger.error("Programming error: this really shouldn't happen");
            }
            tryToSaveTableSettings(true);
            populateSettingsJComboBox();

         }
      }
      
      ignoreSettingsChange = false;
   }
   
   
   
   /**
    * Take appropriate action when the leaf element to be displayed changes.
    * @param evt
    */
   protected void leafDAOElementsComboBoxItemStateChanged(ItemEvent evt)
   {
      // Note: There are two passes through this method per click: once to tell you
      // that an item has been deselected and once to say that a new item has been chosen.
      // Ignore the first of these.
      if (evt.getStateChange() == ItemEvent.DESELECTED) return;
      
      TreeTableProperties ttp   = new TreeTableProperties();
      DAOTreeModel        model = dAOTreeTable1.getTreeModel();
      DAOMutableTreeNode  root  = model.getRootNode();
      String              leaf  = leafDAOElementsComboBox.getFullXPath();
      model.traverseTreeChangingLeaves(root, leaf, ttp.leafIndex);
      
      // Generate dummy collapse and expansion events, as this seems to be
      // required to make the new nodes show up correctly.
      TreePath   tp = new TreePath(root);
      DAOOutline o  = dAOTreeTable1.getOutline();
      o.collapsePath(tp);
      o.setDummyExpand(true);
      o.expandPath(tp);
      o.validate();
      o.repaint();
      o.setDummyExpand(false);
   }
   
   
      
   
   /**
    * Save the XML file representation of settingsList.
    */
   private void tryToSaveTableSettings(boolean showConfirmation)
   {
      try
      {
         settingsList.saveSettings();
      }
      catch(IOException exIO)
      {
         JOptionPane.showMessageDialog(this,
                           "A system error is preventing the XML file containing\n"
                           + "the table settings information from being updated.",
                           "Table settings save error",
                           JOptionPane.ERROR_MESSAGE);
         logger.error("Error while attempting to update table settings XML file:\n"
                      + exIO.getMessage());
         return;
      }
      
      if (showConfirmation) JOptionPane.showMessageDialog(this,
                           "The file containing the Tree Table settings has been\n"
                           + "successfully updated with your new preferences.",
                           "Successful update",
                           JOptionPane.INFORMATION_MESSAGE);
   }

   

   /**
    * Update the leaf display alias combo box with the appropriate value
    * from the current settings list.
    */
   protected void updateLeafComboBox()
   {
      SettingsForSubtype sfs = settingsList.getCurrentTableSettings().get(dataSubtypeAlias);
      if (sfs != null) leafDAOElementsComboBox.setSelectedItem(sfs.leafDisplayAlias);
      else leafDAOElementsComboBox.setSelectedItem("XNAT image session ID");
   }


   /**
    * Concrete classes implement this method to communicate whether to allow
    * selection of profiles referencing "All accessible XNAT projects".
    * @return true if "all projects" can be selected false otherwise.
    */
   @Override
   protected boolean allowAll()
   {
      return true;
   }
   
   
   
   /**
    * Return the leaf element that corresponds to a given root element in the
    * tree table. This corresponds to the object we are actually returning and
    * is often a UID. The XNAT schema element used for this is chosen in the UI.
    * @param rootElement
    * @return leaf
    */
   private String getLeafElement(String rootElement)
   {
      HashMap<String, String> leafDef = new HashMap<String, String>();
      leafDef.put("xnat:mrScanData",     "xnat:mrScanData/UID");
      leafDef.put("xnat:petScanData",    "xnat:petScanData/UID");
      leafDef.put("xnat:ctScanData",     "xnat:ctScanData/UID");
      leafDef.put("xnat:imageScanData",  "xnat:imageScanData/UID");
      leafDef.put("icr:mriwOutputData",  "icr:mriwOutputData/ID");
      leafDef.put("icr:roiSetData",      "icr:roiSetData/ID");
      leafDef.put("icr:roiData",         "icr:roiData/ID");
      
      String leaf = leafDef.get(rootElement);
      if (leaf == null)  throw new UnsupportedOperationException("Don't know the leaf type for " + rootElement);
      
      return leaf;
//      if (rootElement.equals("xnat:mrScanData")) return rootElement + "/UID";
//      else if (rootElement.equals("xnat:petScanData")) return rootElement + "/UID";
//      else if (rootElement.equals("xnat:ctScanData")) return rootElement + "/UID";
//      else if (rootElement.equals("xnat:imageScanData")) return rootElement + "/UID";
//      else if (rootElement.equals("icr:mriwOutputData")) return "icr:mriwOutputData/ID";
//      else throw new UnsupportedOperationException("Don't know the leaf type for " + rootElement);
   }




   /** Method allowing subclasses to use the results obtained from the
    *  dataSubtypeJComboBoxItemStateChanged for their own purposes.
    */
   @Override
   public void useSubtype(String subtype, Vector<String> subtypes, String subtypeAlias)
   {
      dAOSearchCriteria1.changeSearchRootElement(subtype);
      leafDAOElementsComboBox.resetModel(subtype);
      updateLeafComboBox();
      setDefaultSearch();
      dAOTreeTable1.clearTreeTable();
      thumbnailPreview1.stop();
      thumbnailPreview1.clearImages();
      thumbnailPreview1.start();
   }

   

   @Override
   public void implementRestrictions()
   {
      // At present, no restrictions are in force, because I have not got
      // around to it!
   }
   
 
   
   @Override
   public boolean typeIsSelectable(String type)
   {
      if ((type.equals("Set of images"))       ||
          (type.equals("Application outputs")) ||
          (type.equals("Regions-of-interest")))
         return true;
      else return false;
   }
   

   
   @Override
   public boolean subtypeIsSelectable(String subtype)
   {
      if ((subtype.equals("MR image set"))  ||
          (subtype.equals("PET image set")) ||
          (subtype.equals("CT image set"))  ||
          (subtype.equals("MRIW output"))   ||
          (subtype.equals("DICOM-RT structure set"))   ||
          (subtype.equals("Single ROI")))
         return true;
      else return false;
   }
   


   /**
    * Return the XPath in the XNAT schema corresponding to the data type chosen
    * to be returned from this data access object.
    *
    * @return schema XPath as String
    */
   protected String getCurrentSubtypeXPath()
   {
      String type = (String) dataTypeJComboBox.getSelectedItem();

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
      Vector<String>                        subtypes = typesMap.get(type);

      return subtypes.elementAt(dataSubtypeJComboBox.getSelectedIndex());
   }


	private void checkSubmitQueryButtonState()
	{
      XNATProfile prf = profileList.getCurrentProfile();
      if (prf == null) submitQueryJButton.setEnabled(false);
      else
      {
         ArrayList<String> projectList = prf.getProjectList();
		   if (dAOSearchCriteria1.isValidForSubmitQuery() &&
             !((projectList.get(0).equals(PermissionsWorker.NOT_CONNECTED)) ||
               (projectList.get(0).equals(PermissionsWorker.NO_PROJECTS))   ||
               (projectList.get(0).equals(PermissionsWorker.NO_PROFILES))))
            
			   submitQueryJButton.setEnabled(true);
		   else
			   submitQueryJButton.setEnabled(false);
      }
	}
	
	
	
   /**
    * Method allowing subclasses to take specific action when the
    * profile has been changed.
    */
   @Override
   protected void profileChangeConcreteClassActions()
   {
      checkSubmitQueryButtonState();
      if (selectedProfileDidChange)
      {
         dAOTreeTable1.clearTreeTable();
         thumbnailPreview1.stop();
         thumbnailPreview1.clearImages();
         thumbnailPreview1.start();
         selectedProfileDidChange = false;
      }
   }
   
   
   /** Check the state of the "Select Data" button based on the current
	 *  selection in the TreeTable.
	 */
	private void checkSelectDataButtonState()
	{
//		if (dBTreeTable1.getSelectedRowCount() == 0)
//			selectButton.setEnabled(false);
//		else
//			selectButton.setEnabled(true);
	}


   

  
   
   

   /** Getter method to allow a method defined in this superclass to have
    *  access to the subclass's variable. Each different subclass of XNATGUI
    *  will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data type JcomboBox.
    */
   @Override
   public JComboBox getDataTypeJComboBox()
   {
      return dataTypeJComboBox;
   }


   /** Getter method to allow a method defined in this superclass to have
    *  access to the subclass's variable. Each different subclass of XNATGUI
    *  will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data subtype JcomboBox.
    */
   @Override
   public JComboBox getDataSubtypeJComboBox()
   {
      return dataSubtypeJComboBox;
   }


    /**
    * Getter method to allow a method defined in this superclass to have
    * access to the subclass's variable. Each different subclass of XNATGUI
    * will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the server JcomboBox.
    */
   @Override
   public JComboBox getProfileJComboBox()
   {
      return profileJComboBox;
   }
   
   
   /**
    * Getter method to allow access to a private variable defined by Matisse.
    * @return the JProgressBar variable corresponding to the download bar.
    */
   public JProgressBar getDownloadJProgressBar()
   {
      return downloadJProgressBar;
   }

   
   /**
    * Getter method to allow access to a private variable defined by Matisse.
    * @return the JLabel variable corresponding to the "Downloading" label.
    */
   public JLabel getDownloadingJLabel()
   {
      return downloadingJLabel;
   }
 

   /**
    * Getter method to allow access to a private variable defined by Matisse.
    * @return the JLabel variable corresponding to the "DownloadDetails" label.
    */
   public JLabel getDownloadDetailsJLabel()
   {
      return downloadDetailsJLabel;
   }


   /**
    * Enable the "Select Data" button in the UI.
    * Unfortunately, this has to be called by object DAOOutput, which receives
    * the signal back when data are successfully downloaded.
    */
   public void setSelectDataJButtonEnabled(boolean enabled)
   {
      selectDataJButton.setEnabled(enabled);
   }
   

   
   /**
    * Method called by the data access object to initiate a query to the associated tree
    * table and return the list of local files corresponding to the selected data.
    * If the current project is on a remote XNAT server, then the files are
    * automatically downloaded into a cache. Since the operation is asynchronous,
    * and since the UI must continue to function during the retrieve operation,
    * the paradigm is to invoke the retrieval from the event-dispatch thread, have
    * it run in a separate thread, and then query the status from the event-dispatch
    * thread (ultimately from the calling client), using the getStatus() and getFileList()
    * methods.
    */
   private void invokeGetOutputFileList(String invocationCircumstance)
   {
      daoo = new DAOOutput(this,
                           profileList.getCurrentProfile(),
                           dAOTreeTable1.getOutline(),
                           thumbnailPreview1,
                           invocationCircumstance,
                           cacheDirName);

      

      try
      {
      //   if (thumbnailWorker != null) thumbnailPreview1.stop();
      //      thumbnailWorker = thumbnailPreview1.invoke(null);

         fileListWorker = daoo.invoke(getCurrentSubtypeXPath());
      }
      catch (Exception ex)
      {
         JOptionPane.showMessageDialog(this,
                 "I was unable to retrieve the local filenames for the  \n"
                     + "data that you selected for the following reason:\n"
                     + ex.getMessage(),
                 "Failed to retrieve filenames",
                 JOptionPane.ERROR_MESSAGE);
         return;
      }     
   }


   /** When a leaf item is clicked, start to download and display images.
    */
   protected void respondToRowSelection(DAOTreeTableSelectionEvent evt)
   {
      // Two issues here:
      // 1. It appears that we get two notifications from the tree table for a 
      // change. If the first and last rows are the same, i.e., there is no
      // change, then ignore this call.
      //
      // 2. When we sort the columns the row number in the user view is not
      // the same as the row number in the model and we do not want to get
      // all the files again just because the position of the row has changed.
      int firstModelRow = dAOTreeTable1.getOutline()
                                  .convertRowIndexToModel(evt.getFirstIndex());
      int lastModelRow  = dAOTreeTable1.getOutline()
                                  .convertRowIndexToModel(evt.getLastIndex());
      
      if ((oldFirstRow == firstModelRow) && (oldLastRow  == lastModelRow))
      {
         logger.debug("Same values - duplicate call to respondToRowSelection()");
         return;
      }
      oldFirstRow = firstModelRow;
      oldLastRow  = lastModelRow;
      
      if (downloadJProgressBar.isVisible()
          && (!daoo.getStatus().equals("Download cancelled")))
      {
         daoo.cancel(true);
      }
      
      XNATProfile xnprf = profileList.getCurrentProfile();
      if (!xnprf.isConnected()) xnprf.connectWithAuthentication(XNATDAO.this);
      if (!xnprf.isConnected()) return;
      populateProfileJComboBox();
      selectDataJButton.setEnabled(false);
      XNATDAO.this.invokeGetOutputFileList("TreeTable selection changed");
   }
   
   
   private void setDefaultSearch()
   {
      // This will in due course be made more sophisticated, but for the moment
      // set some defaults that are good for DICOM files.
      if (dataSubtypeAlias.contains("image set") && !dataSubtypeAlias.contains("Arbitrary"))
      {
         dAOSearchCriteria1.nCriteriaDisplayed = 3;
         
         DAOSearchCriterion sc = dAOSearchCriteria1.criteria[0];
         sc.setComparisonOperator("LIKE");
         sc.setComparisonString("%");
         sc.setElementSelectedItem("XNAT project");
         sc.setVisible(true);
         
         sc = dAOSearchCriteria1.criteria[1];
         sc.setComparisonOperator("LIKE");
         sc.setComparisonString("%");
         sc.setElementSelectedItem("Patient name");
         sc.setVisible(true);
         
         sc = dAOSearchCriteria1.criteria[2];
         sc.setComparisonOperator(">");
         sc.setComparisonString("2000-01-01");
         sc.setElementSelectedItem("Scan date");
         sc.setVisible(true);
         
         //dAOSearchCriteria1.fireCriteriaChanged(dAOSearchCriteria1);
      }
      
   }
   
   
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jSplitPane1 = new javax.swing.JSplitPane();
      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jLabel2 = new javax.swing.JLabel();
      titleLabel = new javax.swing.JLabel();
      versionJLabel = new javax.swing.JLabel();
      selectDataJButton = new javax.swing.JButton();
      cancelJButton = new javax.swing.JButton();
      thumbnailPreview1 = new xnatDAO.ThumbnailPreview();
      jPanel3 = new javax.swing.JPanel();
      jPanel5 = new javax.swing.JPanel();
      jLabel13 = new javax.swing.JLabel();
      jLabel10 = new javax.swing.JLabel();
      profileJLabel = new javax.swing.JLabel();
      profileJComboBox = new javax.swing.JComboBox();
      dataTypeJLabel = new javax.swing.JLabel();
      dataTypeJComboBox = new javax.swing.JComboBox();
      dataSubtypeJLabel = new javax.swing.JLabel();
      dataSubtypeJComboBox = new javax.swing.JComboBox();
      submitQueryJButton = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      jPanel4 = new javax.swing.JPanel();
      dAOSearchCriteria1 = new xnatDAO.DAOSearchCriteria();
      jPanel6 = new javax.swing.JPanel();
      jScrollPane2 = new javax.swing.JScrollPane();
      dAOTreeTable1 = new treeTable.DAOTreeTable();
      jPanel7 = new javax.swing.JPanel();
      tableSettingsJLabel = new javax.swing.JLabel();
      settingsJComboBox = new javax.swing.JComboBox();
      downloadJProgressBar = new javax.swing.JProgressBar();
      downloadingJLabel = new javax.swing.JLabel();
      downloadDetailsJLabel = new javax.swing.JLabel();
      leafJLabel = new javax.swing.JLabel();
      leafDAOElementsComboBox = new xnatDAO.DAOElementsComboBox();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setResizable(false);

      jSplitPane1.setDividerLocation(530);
      jSplitPane1.setDividerSize(4);

      jPanel2.setBackground(new java.awt.Color(255, 255, 255));

      jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_fibonacci_logo.jpg"))); // NOI18N

      titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 28)); // NOI18N
      titleLabel.setText("DataChooser");
      titleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

      versionJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
      versionJLabel.setText("Version 1.0 beta (build 233)");

      selectDataJButton.setText("Select ");

      cancelJButton.setText("Cancel");

      thumbnailPreview1.setMinimumSize(new java.awt.Dimension(130, 139));
      thumbnailPreview1.setPreferredSize(new java.awt.Dimension(130, 139));

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(31, 31, 31)
            .add(jLabel2)
            .add(30, 30, 30)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jPanel2Layout.createSequentialGroup()
                  .add(selectDataJButton)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(cancelJButton))
               .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(versionJLabel)
                  .add(titleLabel)))
            .add(37, 37, 37)
            .add(thumbnailPreview1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(227, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel2Layout.createSequentialGroup()
                  .add(38, 38, 38)
                  .add(jLabel2))
               .add(jPanel2Layout.createSequentialGroup()
                  .add(28, 28, 28)
                  .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(thumbnailPreview1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel2Layout.createSequentialGroup()
                        .add(titleLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(versionJLabel)
                        .add(32, 32, 32)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                           .add(selectDataJButton)
                           .add(cancelJButton))))))
            .addContainerGap(29, Short.MAX_VALUE))
      );

      jPanel3.setPreferredSize(new java.awt.Dimension(672, 500));

      org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 0, Short.MAX_VALUE)
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 493, Short.MAX_VALUE)
      );

      jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/powered_by_XNAT.jpg"))); // NOI18N

      jLabel10.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
      jLabel10.setText("XNAT database selection criteria");

      profileJLabel.setText("XNAT profile");

      profileJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Enter profile name..." }));

      dataTypeJLabel.setText("Data type");

      dataTypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      dataSubtypeJLabel.setText("Data subtype");

      dataSubtypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      submitQueryJButton.setText("Submit XNAT query");

      jScrollPane1.setBackground(new java.awt.Color(204, 204, 204));
      jScrollPane1.setBorder(null);
      jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
      jPanel4.setLayout(jPanel4Layout);
      jPanel4Layout.setHorizontalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel4Layout.createSequentialGroup()
            .add(dAOSearchCriteria1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 495, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(1088, Short.MAX_VALUE))
      );
      jPanel4Layout.setVerticalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .add(dAOSearchCriteria1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(223, Short.MAX_VALUE))
      );

      jScrollPane1.setViewportView(jPanel4);

      org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
      jPanel5.setLayout(jPanel5Layout);
      jPanel5Layout.setHorizontalGroup(
         jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel5Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 508, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                  .add(submitQueryJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 177, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5Layout.createSequentialGroup()
                     .add(jLabel13)
                     .add(18, 18, 18)
                     .add(jLabel10))
                  .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5Layout.createSequentialGroup()
                     .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(dataSubtypeJLabel)
                        .add(dataTypeJLabel)
                        .add(profileJLabel))
                     .add(28, 28, 28)
                     .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(profileJComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(dataTypeJComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(dataSubtypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 272, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
            .addContainerGap(20, Short.MAX_VALUE))
      );

      jPanel5Layout.linkSize(new java.awt.Component[] {dataSubtypeJComboBox, dataTypeJComboBox, profileJComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

      jPanel5Layout.setVerticalGroup(
         jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel5Layout.createSequentialGroup()
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel5Layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel13))
               .add(jPanel5Layout.createSequentialGroup()
                  .add(32, 32, 32)
                  .add(jLabel10)))
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel5Layout.createSequentialGroup()
                  .add(22, 22, 22)
                  .add(profileJLabel))
               .add(jPanel5Layout.createSequentialGroup()
                  .add(18, 18, 18)
                  .add(profileJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(18, 18, 18)
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(dataTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(dataTypeJLabel))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(dataSubtypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(dataSubtypeJLabel))
            .add(18, 18, 18)
            .add(submitQueryJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
      );

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jPanel3, 0, 204, Short.MAX_VALUE))
               .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(39, 39, 39)
                  .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 493, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(jPanel1Layout.createSequentialGroup()
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addContainerGap())
      );

      jSplitPane1.setLeftComponent(jPanel1);

      jPanel6.setPreferredSize(new java.awt.Dimension(770, 690));

      dAOTreeTable1.setPreferredSize(new java.awt.Dimension(440, 419));
      jScrollPane2.setViewportView(dAOTreeTable1);

      jPanel7.setPreferredSize(new java.awt.Dimension(780, 40));

      tableSettingsJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      tableSettingsJLabel.setText("Table settings");

      settingsJComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      settingsJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      downloadingJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      downloadingJLabel.setText("Downloading");

      downloadDetailsJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      downloadDetailsJLabel.setText("File details");

      leafJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      leafJLabel.setText("Leaf");

      leafDAOElementsComboBox.setToolTipText("Choose the type of data to be attached to the 'leaf' elements of the tree table.");
      leafDAOElementsComboBox.setAlignmentX(0.0F);
      leafDAOElementsComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      leafDAOElementsComboBox.setMinimumSize(new java.awt.Dimension(133, 27));
      leafDAOElementsComboBox.setPreferredSize(new java.awt.Dimension(133, 27));

      org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
      jPanel7.setLayout(jPanel7Layout);
      jPanel7Layout.setHorizontalGroup(
         jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel7Layout.createSequentialGroup()
            .addContainerGap()
            .add(tableSettingsJLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(settingsJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(26, 26, 26)
            .add(leafJLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(leafDAOElementsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(downloadingJLabel)
            .add(18, 18, 18)
            .add(downloadDetailsJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(downloadJProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(67, 67, 67))
      );
      jPanel7Layout.setVerticalGroup(
         jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel7Layout.createSequentialGroup()
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                  .add(tableSettingsJLabel)
                  .add(leafJLabel)
                  .add(settingsJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(leafDAOElementsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(downloadingJLabel)
                  .add(downloadDetailsJLabel))
               .add(downloadJProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(13, Short.MAX_VALUE))
      );

      ((org.jdesktop.layout.GroupLayout) jPanel7.getLayout()).setHonorsVisibility(false);

      org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
      jPanel6.setLayout(jPanel6Layout);
      jPanel6Layout.setHorizontalGroup(
         jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
         .add(jPanel6Layout.createSequentialGroup()
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 796, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      jPanel6Layout.setVerticalGroup(
         jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel6Layout.createSequentialGroup()
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 627, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      jSplitPane1.setRightComponent(jPanel6);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1332, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 677, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelJButton;
   private xnatDAO.DAOSearchCriteria dAOSearchCriteria1;
   private treeTable.DAOTreeTable dAOTreeTable1;
   private javax.swing.JComboBox dataSubtypeJComboBox;
   private javax.swing.JLabel dataSubtypeJLabel;
   private javax.swing.JComboBox dataTypeJComboBox;
   private javax.swing.JLabel dataTypeJLabel;
   private javax.swing.JLabel downloadDetailsJLabel;
   private javax.swing.JProgressBar downloadJProgressBar;
   private javax.swing.JLabel downloadingJLabel;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel13;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JPanel jPanel5;
   private javax.swing.JPanel jPanel6;
   private javax.swing.JPanel jPanel7;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JSplitPane jSplitPane1;
   private xnatDAO.DAOElementsComboBox leafDAOElementsComboBox;
   private javax.swing.JLabel leafJLabel;
   private javax.swing.JComboBox profileJComboBox;
   private javax.swing.JLabel profileJLabel;
   private javax.swing.JButton selectDataJButton;
   private javax.swing.JComboBox settingsJComboBox;
   private javax.swing.JButton submitQueryJButton;
   private javax.swing.JLabel tableSettingsJLabel;
   private xnatDAO.ThumbnailPreview thumbnailPreview1;
   private javax.swing.JLabel titleLabel;
   private javax.swing.JLabel versionJLabel;
   // End of variables declaration//GEN-END:variables

}
