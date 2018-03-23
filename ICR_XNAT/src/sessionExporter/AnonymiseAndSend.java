/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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
* Java class: AnonymiseAndSend.java
* First created on Feb 12, 2015
* 
* Anonymisation GUI allowing users to route a downloaded session to
* a different XNAT instance and project.
*********************************************************************/

package sessionExporter;

import obselete.AnonScriptWindow;
import configurationLists.DAOSearchableElementsList;
import fileDownloads.AnonSendPreFetchStore;
import fileDownloads.FileListWorker;
import fileDownloads.PreFetchStore;
import generalUtilities.SimpleColourTable;
import imageUtilities.DownloadIcon;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.netbeans.swing.outline.OutlineModel;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.PermissionsWorker;
import xnatDAO.ProjectGetter;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;

/**
 *
 * @author simond
 */
public class AnonymiseAndSend extends xnatDAO.XNATGUI
                              implements ProjectGetter
{
	private String                anonVersion = "2.1 alpha 8/2/2018";
	private XNATProfile           destProf;
	private boolean               tableChangeLock;
   public boolean                scriptApproved = false;
   public String                 scriptText = null;
	private List<AnonSessionInfo> asiList;
	private List<List<File>>      srcFileList;
   private FileListWorker        flw;
	private DefaultTableModel     anonModel;
	private AnonScriptView        asv;
   private AnonSendPreFetchStore pfs;
	protected static Logger       logger = Logger.getLogger(AnonymiseAndSend.class);
	
   public static final String    DEFAULT_SUBJ_NAME = "Anonymised";
   public static final String    DEFAULT_SUBJ_ID   = "Anonymised";
   public static final String    DEFAULT_SESS_NAME = "DEFAULT";
	public static final String    SUBJ_NAME_TOKEN   = "<INSERTED-SUBJ-NAME>";
   public static final String    SUBJ_ID_TOKEN     = "<INSERTED-SUBJ-ID>";
	public static final String    PROJ_ID_TOKEN     = "<INSERTED-PROJ-ID>";
   public static final String    SESS_NAME_TOKEN   = "<INSERTED-SESS-NAME>";
   

   /**
    * Gather the information required for anonymising data.
    * @param parent the awt Frame in which the GUI window is placed
    * @param modal Boolean variable indicating whether this dialog box is modal
    * @param flw object reference of the FileListWorker initiating this anonymisation process
    * @param pfs PreFetchStore into which the variables are returned
    */
	public AnonymiseAndSend(java.awt.Frame parent, boolean modal,
			                  FileListWorker flw, AnonSendPreFetchStore pfs)
	{
		super(parent, modal);
		
		this.flw = flw;
      this.pfs = pfs;

      initComponents();
      versionJLabel.setText("Version " + anonVersion);
		checkAccessJLabel.setVisible(false);
      
      asiList = getSessionInfo(); 	
		populateComponents();
      exportJButton.setEnabled(false);
      addListeners();
      implementRestrictions();
	}
   
   private List<AnonSessionInfo> getSessionInfo()
   {
      // Sessions need to be handled differently from other items, as they
		// are further up the hierarchy and do not have resources directly
		// underneath. This method should not have been called if this wasn't
      // a session
		assert flw.getRootElement().contains("Session");
      
      List<AnonSessionInfo> asiList = new ArrayList<>();
      
      int[] selTableRows  = flw.getOutline().getSelectedRows();
		int   nTableRows    = selTableRows.length;
      
      DAOSearchableElementsList sel;
      try
      {
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         // This really shouldn't happen. By the time we get this far into the
         // application, things will already have gone wrong, so it should be
         // safe to assume we won't ever get here in reality.
         logger.error(exIO.getMessage());
         return asiList;
      }
      
      Vector<String> tableColumnElements = sel.getSearchableXNATElements().get(flw.getRootElement());
		OutlineModel  omdl                 = flw.getOutline().getOutlineModel();
      
      for (int i=0; i<nTableRows; i++)
		{
         String          expXPath;
         int             expIndex;
         AnonSessionInfo asi = new AnonSessionInfo();
         
         int row = flw.getOutline().convertRowIndexToModel(selTableRows[i]);
         
         expXPath = flw.getRootElement() + "/ID";
	      expIndex = tableColumnElements.indexOf(expXPath);
         asi.setSessionId((String) omdl.getValueAt(row, expIndex+1));
         
         expXPath = flw.getRootElement() + "/label";
	      expIndex = tableColumnElements.indexOf(expXPath);
         asi.setSessionLabel((String) omdl.getValueAt(row, expIndex+1));
         
         expXPath = flw.getRootElement() + "/subject_ID";
	      expIndex = tableColumnElements.indexOf(expXPath);
         asi.setSubjXnatId((String) omdl.getValueAt(row, expIndex+1));
         
         XNATServerConnection xnsc = flw.getXNATServerConnection();
         asi.setSubjLabel(getSubjXnatLabelFromId(asi.getSubjXnatId(), xnsc));
         
         asiList.add(asi);
      }
      
      return asiList;
   }
   
   
   /**
    * Use the XNAT Subject ID to retrieve the XNAT Subject Label.
    * @param subjXnatId String containing the ID of the subject in XNAT
    * @param xnprf the XNATProfile containing the connection information needed to access XNAT
    * @return String containing the subject name
    */
   private String getSubjXnatLabelFromId(String subjXnatId, XNATServerConnection xnsc)
   {
      // It takes more work to retrieve this value than one might expect, because
      // only the subject ID is part of the session information. To get the subject
      // label, we need to retrieve the xnat:subjectData as an XML and parse it.
      String restCommand = "/data/subjects/" + subjXnatId + "?format=xml";
      String subjXnatLabel = "";
      try
      {
         Document resultDoc = new XNATRESTToolkit(xnsc).RESTGetDoc(restCommand);
         String[] attrs = XMLUtilities.getAttribute(resultDoc, new XNATNamespaceContext(),
                                                   "xnat:Subject", "label");
         if (attrs != null) subjXnatLabel = attrs[0];
      }
      catch (Exception ex)
      {
         logger.error("Error retrieving subject label");
         JOptionPane.showMessageDialog(this.getParent(), "Error retrieving subject name\n"
                                       + "Can't match a subject label with subject ID " + subjXnatId,
                                       "Data retrieval error", JOptionPane.ERROR_MESSAGE);
         // TODO : Work out how to recover from here!
      }
      
      return subjXnatLabel;
   }
	
	
	
	
	/**
    * Add the listeners that allow actions to be implemented when the
	 * users click on relevant options.
    */
   private void addListeners()
   {
		cancelJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
            pfs.setCancelled(true); 
				dispose();
			}	  
		});
		
		
		exportJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				pfs.setCancelled(false);
            pfs.setAnonScriptTemplate(scriptText);
            pfs.setDestProfile(destProf);
            pfs.setDestProject((String) destProjectJComboBox.getSelectedItem());
            pfs.setAnonSessionInfo(asiList);
            
            dispose();
			}	  
		});
		
		
		viewAnonScriptJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
            //asw = new AnonScriptWindow(new javax.swing.JFrame(), true, AnonymiseAndSend.this);
            AnonymiseAndSend.this.asv = new AnonScriptView(AnonymiseAndSend.this,
                                                    "Anonymisation Script Entry",
                                                    Dialog.ModalityType.MODELESS);
            asv.addPropertyChangeListener(new PropertyChangeListener()
            {
               @Override
               public void propertyChange(PropertyChangeEvent pce)
               {
                  if (pce.getPropertyName().equals("Approved"))
                  {
                     scriptApproved = pce.getNewValue().equals(1L);
                     if (scriptApproved) scriptText = asv.getModel().getCurrentScript();
                     checkExport();
                  }
               }
               
            });
         }
      });
		
		
		destProfileJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            profileJComboBoxItemStateChanged(evt, true);
         }
      });
		
		
		destProjectJComboBox.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				destProjectComboBoxClicked();
            checkExport();
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e){}
		});
				
		
		anonModel.addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				respondToTableChange(e.getFirstRow(), e.getColumn());
			}
    });
      
      
		
}
	
		
	
	protected void populateComponents()
	{	
		Object[] colNames = {"Subject name", "Anonymised name", "Anonymised DICOM ID", "Source session label", "Destination session label" }; 
		
		anonModel = new DefaultTableModel(colNames, 0)
		{
		   @Override public boolean isCellEditable(int row, int col)
			{
				return (col == 1) || (col == 2) || (col == 4);
			}
		};
		anonTwoColourJTable.setModel(anonModel);
		
		Set<String>         distinctSubjs   = new HashSet<>();		
		Map<String, String> subjAnonIdMap   = new HashMap<>();
      Map<String, String> subjAnonNameMap = new HashMap<>();
      
      for (int i=0; i<asiList.size(); i++)
		{
         AnonSessionInfo asi = asiList.get(i);
         String subj = asi.getSubjXnatId();
         if (!distinctSubjs.contains(subj))
         {
            distinctSubjs.add(subj);
            subjAnonIdMap.put(subj, DEFAULT_SUBJ_ID     + distinctSubjs.size());
            subjAnonNameMap.put(subj, DEFAULT_SUBJ_NAME + distinctSubjs.size());
         }
         
         asi.setSubjDicomAnonId(subjAnonIdMap.get(subj));
         asi.setSubjDicomAnonName(subjAnonNameMap.get(subj));
         
			Object[] row = {asi.getSubjLabel(), asi.getSubjDicomAnonName(),
                         asi.getSubjDicomAnonId(), asi.getSessionLabel(),
                         asi.getSessionLabel()};
         
			anonModel.addRow(row);
         
         exportEnableJTextArea.setText("Note: The export button is enabled only after you have chosen a destination profile and project, and viewed and approved the anonymisation script.");
		}
		
		
		populateProfileJComboBox();
	}
   
   
   
	
	/**
    * Method allowing subclasses to take specific action when the
    * profile has been changed.
    */
   @Override
   protected void profileChangeConcreteClassActions()
   {
      if (selectedProfileDidChange)
      {
         selectedProfileDidChange = false;
			destProf = profileList.getCurrentProfile();
			if (!destProf.isConnected()) destProf.connectWithAuthentication(this);
			
			// Fake an item event so that we can call the code for changing the profile
			// in order to set the correct colour post authentication.
			ItemEvent fakeEvt = new ItemEvent(destProfileJComboBox,
					                            ItemEvent.ITEM_FIRST,
					                            destProfileJComboBox,
			                                  ItemEvent.SELECTED);
			ignoreItemChange = false;
			profileJComboBoxItemStateChanged(fakeEvt, false);
			retrieveProjectList();
      }
   }
	
	
	/**
	 * This little function is needed to cope with the first use of the tool
	 * when the project combo box is not populated because the user is not
	 * authenticated on the remote database
	 */
	protected void destProjectComboBoxClicked()
	{
		destProf = profileList.getCurrentProfile();
		if (!destProf.isConnected())
		{
			destProf.connectWithAuthentication(this);
			// Fake an item event so that we can call the code for changing the profile
			// in order to set the correct colour.
			ItemEvent fakeEvt = new ItemEvent(destProjectJComboBox,
					                            ItemEvent.ITEM_FIRST,
					                            destProfileJComboBox,
			                                  ItemEvent.SELECTED);
			profileJComboBoxItemStateChanged(fakeEvt, false);
			retrieveProjectList();
         checkExport();
		}		
	}
	
	
	protected void retrieveProjectList()
	{
		// Retrieve list of projects and populate combo box.
			checkAccessJLabel.setVisible(true);
			DownloadIcon getPermsIcon = new DownloadIcon(checkAccessJLabel);
			checkAccessJLabel.setIcon(getPermsIcon);
			checkAccessJLabel.setVisible(true);
			getPermsIcon.start();
		
			// Note that when the PermissionsWorker finishes executing, it calls the
			// method that populates the combo box with the projects discovered.
			(new PermissionsWorker(this, false)).execute();
	}
	


	
	@Override
	public void populateProjectJComboBox(ArrayList<String> accessible)
	{
		DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
      destProjectJComboBox.setModel(dcbm);

      for (String projectName : accessible) dcbm.addElement(projectName);
      
      if (accessible.get(0).equals(PermissionsWorker.NO_PROJECTS) ||
          accessible.get(0).equals(PermissionsWorker.NOT_CONNECTED))
      {
         destProjectJComboBox.setForeground(SimpleColourTable.getColour("ICR red"));
      }
      else
		{
			destProjectJComboBox.setForeground(SimpleColourTable.getColour("black"));
         destProjectJComboBox.setSelectedItem(PermissionsWorker.ALL_PROJECTS);
		}

      checkAccessJLabel.setVisible(false);
      ((DownloadIcon) checkAccessJLabel.getIcon()).stop();
	}
	

	
	/* When the destination subject code is changed, make a corresponding change
	 * in the destination code for all other sessions with the same subject name.
	 */
	
	public void respondToTableChange(int row, int col)
	{
		if (tableChangeLock) return;
		
		// Once we have started this method, we need to not restart it
		// every time we make a change to the table below.
      // Any changes to the table made here will file a TableModelEvent,
		// which we want the TableModelListener to ignore.
		tableChangeLock = true;
		
      Object newEntry = anonModel.getValueAt(row, col);
      for (int i=0; i<asiList.size(); i++)
      {
         AnonSessionInfo asi = asiList.get(i);
         String subj = asi.getSubjXnatId();         
         if (subj.equals(asiList.get(row).getSubjXnatId()))
         {
            if (col == 1) asi.setSubjDicomAnonName((String) newEntry);
            if (col == 2) asi.setSubjDicomAnonId((String) newEntry);
            anonModel.setValueAt(newEntry, i, col);
         }
      }
      if (col == 4)
      {
         AnonSessionInfo asi = asiList.get(row);
         asi.setDestSessionLabel((String) newEntry);
      }
		
		tableChangeLock = false;
	}
	
		
		
	private String getSessionsAsString()
	{
		StringBuilder sb = new StringBuilder();
		for (AnonSessionInfo asi : asiList) sb.append(asi.getSessionLabel()).append("\n");
		// Last newline character is never needed.
		return sb.substring(0, sb.length()-1);
	}
   
   /**
    * Check whether the the export button should be enabled or not.
    */
   private void checkExport()
   {
      if (destProf == null)
      {
         exportJButton.setEnabled(false);
         return;
      }
      
      exportJButton.setEnabled(destProf.isConnected() && scriptApproved);
   }
	
	
	@Override
	public XNATProfile getProfile()
	{
		return destProf;
	}
	
	
	@Override
   public JComboBox getProfileJComboBox()
   {
      return destProfileJComboBox;
   }
	
	
	@Override
	public JComboBox getDataTypeJComboBox()
	{
		return new JComboBox();
	}
	
	
	@Override
	public JComboBox getDataSubtypeJComboBox()
	{
		return new JComboBox();
	}
	
	@Override
	public void implementRestrictions()
	{
		
	}

	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      twoColourJTable1 = new generalUtilities.TwoColourJTable();
      versionJLabel = new javax.swing.JLabel();
      destProfileJLabel = new javax.swing.JLabel();
      destProfileJComboBox = new javax.swing.JComboBox();
      destProjectJLabel = new javax.swing.JLabel();
      destProjectJComboBox = new javax.swing.JComboBox();
      dataToExportJLabel = new javax.swing.JLabel();
      dataDestinationJLabel = new javax.swing.JLabel();
      cancelJButton = new javax.swing.JButton();
      exportJButton = new javax.swing.JButton();
      checkAccessJLabel = new javax.swing.JLabel();
      viewAnonScriptJButton = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      anonTwoColourJTable = new generalUtilities.TwoColourJTable();
      jScrollPane2 = new javax.swing.JScrollPane();
      exportEnableJTextArea = new javax.swing.JTextArea();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      versionJLabel.setFont(versionJLabel.getFont().deriveFont(versionJLabel.getFont().getSize()+3f));
      versionJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      versionJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_DataExporter_large.png"))); // NOI18N
      versionJLabel.setText("2.2 alpha (31/01/2018)");
      versionJLabel.setAlignmentX(0.5F);
      versionJLabel.setAlignmentY(1.0F);
      versionJLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      versionJLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

      destProfileJLabel.setText("XNAT Profile");

      destProfileJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Choose a profile ..." }));

      destProjectJLabel.setText("XNAT Project");

      destProjectJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Choose a project ..." }));

      dataToExportJLabel.setFont(dataToExportJLabel.getFont().deriveFont(dataToExportJLabel.getFont().getStyle() | java.awt.Font.BOLD, dataToExportJLabel.getFont().getSize()+7));
      dataToExportJLabel.setText("Data source and anonymisation");

      dataDestinationJLabel.setFont(dataDestinationJLabel.getFont().deriveFont(dataDestinationJLabel.getFont().getStyle() | java.awt.Font.BOLD, dataDestinationJLabel.getFont().getSize()+7));
      dataDestinationJLabel.setText("Data destination");

      cancelJButton.setText("Cancel");

      exportJButton.setText("Export");

      checkAccessJLabel.setFont(checkAccessJLabel.getFont().deriveFont(checkAccessJLabel.getFont().getSize()-1f));
      checkAccessJLabel.setText("Checking project access");

      viewAnonScriptJButton.setText("View anonymisation script ...");

      anonTwoColourJTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][]
         {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
         },
         new String []
         {
            "Title 1", "Title 2", "Title 3", "Title 4"
         }
      ));
      anonTwoColourJTable.setRowHeight(24);
      jScrollPane1.setViewportView(anonTwoColourJTable);

      jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      jScrollPane2.setFocusable(false);

      exportEnableJTextArea.setEditable(false);
      exportEnableJTextArea.setColumns(20);
      exportEnableJTextArea.setLineWrap(true);
      exportEnableJTextArea.setRows(5);
      exportEnableJTextArea.setWrapStyleWord(true);
      exportEnableJTextArea.setBorder(null);
      jScrollPane2.setViewportView(exportEnableJTextArea);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGap(25, 25, 25)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(destProjectJLabel)
                     .addComponent(dataDestinationJLabel)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(destProfileJLabel)
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(destProfileJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addGroup(layout.createSequentialGroup()
                              .addComponent(destProjectJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(checkAccessJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addComponent(dataToExportJLabel)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 917, Short.MAX_VALUE)
                     .addComponent(jScrollPane2))
                  .addGap(0, 31, Short.MAX_VALUE))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(viewAnonScriptJButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(exportJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(versionJLabel)
            .addGap(351, 351, 351))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(29, 29, 29)
            .addComponent(versionJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(41, 41, 41)
            .addComponent(dataToExportJLabel)
            .addGap(18, 18, 18)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(46, 46, 46)
            .addComponent(dataDestinationJLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(destProfileJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(destProfileJLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(destProjectJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(destProjectJLabel)
               .addComponent(checkAccessJLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(viewAnonScriptJButton)
               .addComponent(exportJButton)
               .addComponent(cancelJButton))
            .addGap(41, 41, 41))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

//	/**
//	 * @param args the command line arguments
//	 */
//	public static void main(String args[]) {
//		/* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//		 */
//		try {
//			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//				if ("Nimbus".equals(info.getName())) {
//					javax.swing.UIManager.setLookAndFeel(info.getClassName());
//					break;
//				}
//			}
//		} catch (ClassNotFoundException ex) {
//			java.util.logging.Logger.getLogger(AnonymiseAndSend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//		} catch (InstantiationException ex) {
//			java.util.logging.Logger.getLogger(AnonymiseAndSend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//		} catch (IllegalAccessException ex) {
//			java.util.logging.Logger.getLogger(AnonymiseAndSend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
//			java.util.logging.Logger.getLogger(AnonymiseAndSend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//		}
//        //</editor-fold>
//
//		/* Create and display the dialog */
//		java.awt.EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				AnonymiseAndSend dialog = new AnonymiseAndSend(new javax.swing.JFrame(), true);
//				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//					@Override
//					public void windowClosing(java.awt.event.WindowEvent e) {
//						System.exit(0);
//					}
//				});
//				dialog.setVisible(true);
//			}
//		});
//	}

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private generalUtilities.TwoColourJTable anonTwoColourJTable;
   private javax.swing.JButton cancelJButton;
   private javax.swing.JLabel checkAccessJLabel;
   private javax.swing.JLabel dataDestinationJLabel;
   private javax.swing.JLabel dataToExportJLabel;
   private javax.swing.JComboBox destProfileJComboBox;
   private javax.swing.JLabel destProfileJLabel;
   private javax.swing.JComboBox destProjectJComboBox;
   private javax.swing.JLabel destProjectJLabel;
   private javax.swing.JTextArea exportEnableJTextArea;
   private javax.swing.JButton exportJButton;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private generalUtilities.TwoColourJTable twoColourJTable1;
   private javax.swing.JLabel versionJLabel;
   private javax.swing.JButton viewAnonScriptJButton;
   // End of variables declaration//GEN-END:variables

	@Override
	protected boolean allowAll()
	{
		return true;
	}

	@Override
	public void useSubtype(String subtype, Vector<String> subtypes, String subtypeAlias)
	{
		throw new UnsupportedOperationException("Not supported."); 
	}

	
	@Override
	public boolean typeIsSelectable(String type)
	{
		throw new UnsupportedOperationException("Not supported."); 
	}

	
	@Override
	public boolean subtypeIsSelectable(String subtype)
	{
		throw new UnsupportedOperationException("Not supported."); 
	}
}
