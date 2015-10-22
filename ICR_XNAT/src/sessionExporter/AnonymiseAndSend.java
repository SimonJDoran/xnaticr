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

import generalUtilities.SimpleColourTable;
import imageUtilities.DownloadIcon;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import xnatDAO.PermissionsWorker;
import xnatDAO.ProjectGetter;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATRESTToolkit;

/**
 *
 * @author simond
 */
public class AnonymiseAndSend extends xnatDAO.XNATGUI implements ProjectGetter
{
	protected String                     version = "2.01 alpha 21/10/2015";
	protected XNATProfile                destProf;
	protected XNATProfile                srcProf;
	protected ArrayList<String>          srcSessIDs;
	protected ArrayList<String>          srcSessLabels;
	protected ArrayList<String>          srcSessSubjs;
	protected ArrayList<String>          destSubjCodes;
	protected ArrayList<ArrayList<File>> srcFileList;
	protected DefaultTableModel          anonModel;
	protected AnonScriptWindow           asw;
	protected ExportLogWindow            elw;
	protected static Logger              logger = Logger.getLogger(AnonymiseAndSend.class);
	protected static final String        DEFAULT_SUBJ_CODE = "Anonymised Subject";
	protected static final String        SUBJ_TOKEN = "<AUTO-GENERATED-SUBJECT>";
	protected static final String        PROJ_TOKEN = "<AUTO-GENERATED-PROJECT>";
	

	/**
	 * 
	 * @param parent
	 * @param modal
	 * @param srcProf
	 * @param srcSess
	 * @param srcFileList 
	 */
	public AnonymiseAndSend(java.awt.Frame parent,
			                  boolean modal,
			                  XNATProfile srcProf,
									ArrayList<String> srcSessIDs,
									ArrayList<String> srcSessLabels,
									ArrayList<String> srcSessSubjs)
	{
		super(parent, modal);
		
		this.srcProf       = srcProf;
		this.srcSessIDs    = srcSessIDs;
		this.srcSessLabels = srcSessLabels;
		this.srcSessSubjs  = srcSessSubjs;

      initComponents();
      versionJLabel.setText("Version " + version);
		checkAccessJLabel.setVisible(false);
		elw = new ExportLogWindow(new javax.swing.JFrame(), false);
		elw.updateLogWindow("Export Log\n\n");
		
		populateComponents();
      addListeners();
      implementRestrictions();
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
				dispose();
			}	  
		});
		
		
		exportJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
				exportData();
			}	  
		});
		
		
		viewAnonScriptJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
				if (asw == null)
					asw = new AnonScriptWindow(new javax.swing.JFrame(), false);
				asw.setVisible(true);;
			}	  
		});
		
		
		viewExportLogJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{            
				elw.setVisible(true);
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
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e){}
		});
		
		
		anonCodeSameJCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				respondToCheck();
			}
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
		srcProfileJTextField.setText(srcProf.getProfileName());
		
		Object[] colNames = {"Source session", "Source subject name", "Anonymised subject name"}; 
		
		anonModel = new DefaultTableModel(colNames, 0)
		{
		   @Override public boolean isCellEditable(int row, int col)
			{
				return col == 2;
			}
		};
		anonTwoColourJTable.setModel(anonModel);
		
		int               nDistinct     = 0;
		ArrayList<String> distinctSubjs = new ArrayList<>();
		destSubjCodes                   = new ArrayList<>();
		
		for (int i=0; i<srcSessIDs.size(); i++)
		{
			if (!distinctSubjs.contains(srcSessSubjs.get(i)))
			{
				nDistinct++;
				distinctSubjs.add(srcSessSubjs.get(i));
			}
			destSubjCodes.add("AnonymisedPatient_" + nDistinct);
			
			Object[] row = {srcSessLabels.get(i), srcSessSubjs.get(i), destSubjCodes.get(i)};
			anonModel.addRow(row);
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
	
	
	
	
	
	protected void exportData()
	{
		if (asw == null)
		{
			JOptionPane.showMessageDialog(this, "Please view the anonymisation script before exporting data\n"
								+ "to confirm that it meets your requirements.");
			return;
		}
		
		String templateScript = asw.getScriptText();
		
		// Check whether the subject name still has the default value.
		String subjCode   = "junk";
		if (subjCode.equals(DEFAULT_SUBJ_CODE))
		{
			Object[] options = { "Continue", "Cancel" };
			int choice =JOptionPane.showOptionDialog(this,
					                       "You have not changed the subject name\n"
					                       + "away from its default value", "Warning",
			                             JOptionPane.DEFAULT_OPTION,
												  JOptionPane.WARNING_MESSAGE,
			                             null, options, options[0]);
			if (choice == 1) return;
		}
		
		String anonScript = templateScript.replaceAll(SUBJ_TOKEN, subjCode)
		                                  .replaceAll(PROJ_TOKEN, (String) destProjectJComboBox.getSelectedItem());

	//	remapper = new DicomRemapAndSend(logJTextArea);
		
		
		
	}
	/*
	private void exportViaCustomCode(String anonScript, ArrayList<String> sourceList))
	{
		
	}
	*/
	
	/**
	 * Export the DICOM files by calling a command line process.
	 * This mechanism was coded but then abandoned on the basis that it is not optimal
	 * to expect DicomBrowser to be installed on every machine that runs this application.
	 * Replacement function is exportViaCustomCode.
	 * @param anonScript DicomEdit-compatible anonymisation script
	 * @param sourceList list of DICOM files
	 */
	private void exportViaDicomRemap(String anonScript, ArrayList<String> sourceList)
	{
		InputStream is = null;
		try
		{
			String         homeDir = System.getProperty("user.home");
			String         sep     = System.getProperty("file.separator");
			String         dasName = homeDir + sep + ".XNAT_DAO" + sep + "temp"
			                         + sep + "anonSendSessionTemp.das";
			FileWriter     dasWrt  = new FileWriter(dasName);
			dasWrt.write(anonScript);
			dasWrt.close();
			
			List<String>	cl      = new ArrayList<String>();
			cl.add("/Applications/DicomBrowser-1.5.2/bin/DicomRemap");
			cl.add("-d");
			cl.add(dasName);
			cl.add("-o");
			cl.add("dicom://172.16.14.8:8104/XNAT");
			
			
			ProcessBuilder pb = new ProcessBuilder(cl);
			Process        p  = pb.start();
			StringBuilder  sb = new StringBuilder();
         int            b;
			is = p.getInputStream();
         while ((b = is.read()) != -1) sb.append((char) b);
			elw.updateLogWindow(sb.toString());
		}
		catch (IOException exIO)
		{
			elw.updateLogWindow("Error initiating send process: " + exIO.getMessage());
      }
		finally
      {
         try {is.close();} catch (IOException exIOignore) {}
      }
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
	

	
	/* Action taken when the anonCodeSameJCheckBox is selected.
	*/
	public void respondToCheck()
	{
		if (anonCodeSameJCheckBox.isSelected())
		{
			// Find all duplicate subject names and use the code for the first
			// occurrence for all the rest.		
			// The first destCode entry will always be correct already. From the
			// second one down, the destCode will depend on whether the subject
			// name has appeared already.
			for (int i=1; i<srcSessIDs.size(); i++)
			{
				int    firstInd = srcSessSubjs.indexOf(srcSessSubjs.get(i));
				Object code     = anonModel.getValueAt(firstInd, 2);
				if (firstInd <= i) destSubjCodes.set(i, (String) code);
				anonModel.setValueAt(destSubjCodes.get(i), i, 2);
			}		
		}
	}
	
	
	/* When the destination subject code is changed, make a corresponding change
	 * in the destination code for all other sessions with the same subject name.
	 */
	
	public void respondToTableChange(int row, int col)
	{
		
		if (anonCodeSameJCheckBox.isSelected())
		{
			for (int i=1; i<srcSessIDs.size(); i++)
			{
				int    firstInd = srcSessSubjs.indexOf(srcSessSubjs.get(i));
				Object code     = anonModel.getValueAt(firstInd, 2);
				if (firstInd <= i) destSubjCodes.set(i, (String) code);
				anonModel.setValueAt(destSubjCodes.get(i), i, 2);
			}		
		}
	}
		
	protected String getSessionsAsString()
	{
		StringBuilder sb = new StringBuilder();
		for (String s : srcSessLabels) sb.append(s).append("\n");
		// Last newline character is never needed.
		return sb.substring(0, sb.length()-1);
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
   private void initComponents() {

      twoColourJTable1 = new generalUtilities.TwoColourJTable();
      versionJLabel = new javax.swing.JLabel();
      destProfileJLabel = new javax.swing.JLabel();
      destProfileJComboBox = new javax.swing.JComboBox();
      destProjectJLabel = new javax.swing.JLabel();
      destProjectJComboBox = new javax.swing.JComboBox();
      dataToExportJLabel = new javax.swing.JLabel();
      srcProfileJLabel = new javax.swing.JLabel();
      srcProfileJTextField = new javax.swing.JLabel();
      dataDestinationJLabel = new javax.swing.JLabel();
      cancelJButton = new javax.swing.JButton();
      exportJButton = new javax.swing.JButton();
      exportJProgressBar = new javax.swing.JProgressBar();
      checkAccessJLabel = new javax.swing.JLabel();
      viewExportLogJButton = new javax.swing.JButton();
      viewAnonScriptJButton = new javax.swing.JButton();
      dataDestinationJLabel1 = new javax.swing.JLabel();
      fileProgressJLabel = new javax.swing.JLabel();
      anonCodeSameJCheckBox = new javax.swing.JCheckBox();
      jScrollPane1 = new javax.swing.JScrollPane();
      anonTwoColourJTable = new generalUtilities.TwoColourJTable();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      versionJLabel.setFont(versionJLabel.getFont().deriveFont(versionJLabel.getFont().getSize()+3f));
      versionJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      versionJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_DataExporter_large.png"))); // NOI18N
      versionJLabel.setText("2.01 alpha (20/10/2015)");
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

      srcProfileJLabel.setText("XNAT Profile");

      srcProfileJTextField.setText("No profile specified");

      dataDestinationJLabel.setFont(dataDestinationJLabel.getFont().deriveFont(dataDestinationJLabel.getFont().getStyle() | java.awt.Font.BOLD, dataDestinationJLabel.getFont().getSize()+7));
      dataDestinationJLabel.setText("Data destination");

      cancelJButton.setText("Cancel");

      exportJButton.setText("Export");

      checkAccessJLabel.setFont(checkAccessJLabel.getFont().deriveFont(checkAccessJLabel.getFont().getSize()-1f));
      checkAccessJLabel.setText("Checking project access");

      viewExportLogJButton.setText("View export log ...");
      viewExportLogJButton.setActionCommand("View download log ...");

      viewAnonScriptJButton.setText("View anonymisation script ...");

      dataDestinationJLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
      dataDestinationJLabel1.setText("Export progress");

      fileProgressJLabel.setText("Inactive");

      anonCodeSameJCheckBox.setText("Force anonymisation code to be the same for all sessions from the same patient.");
      anonCodeSameJCheckBox.setIconTextGap(8);

      anonTwoColourJTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
         },
         new String [] {
            "Title 1", "Title 2", "Title 3", "Title 4"
         }
      ));
      jScrollPane1.setViewportView(anonTwoColourJTable);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(25, 25, 25)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(destProjectJLabel)
                     .addComponent(dataToExportJLabel)
                     .addComponent(dataDestinationJLabel1)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(srcProfileJLabel)
                        .addGap(45, 45, 45)
                        .addComponent(srcProfileJTextField))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(exportJProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(fileProgressJLabel))
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                           .addComponent(viewAnonScriptJButton)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(viewExportLogJButton)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(exportJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                           .addComponent(destProfileJLabel)
                           .addGap(31, 31, 31)
                           .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addComponent(destProfileJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addGroup(layout.createSequentialGroup()
                                 .addComponent(destProjectJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(checkAccessJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addContainerGap(16, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(dataDestinationJLabel)
                     .addComponent(anonCodeSameJCheckBox))
                  .addGap(0, 0, Short.MAX_VALUE))))
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(versionJLabel)
            .addGap(205, 205, 205))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(37, 37, 37)
            .addComponent(versionJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(33, 33, 33)
            .addComponent(dataToExportJLabel)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(srcProfileJTextField)
               .addComponent(srcProfileJLabel))
            .addGap(14, 14, 14)
            .addComponent(anonCodeSameJCheckBox)
            .addGap(18, 18, 18)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
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
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(dataDestinationJLabel1)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(exportJProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(fileProgressJLabel))
            .addGap(30, 30, 30)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(viewAnonScriptJButton)
               .addComponent(viewExportLogJButton)
               .addComponent(exportJButton)
               .addComponent(cancelJButton))
            .addContainerGap())
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
   private javax.swing.JCheckBox anonCodeSameJCheckBox;
   private generalUtilities.TwoColourJTable anonTwoColourJTable;
   private javax.swing.JButton cancelJButton;
   private javax.swing.JLabel checkAccessJLabel;
   private javax.swing.JLabel dataDestinationJLabel;
   private javax.swing.JLabel dataDestinationJLabel1;
   private javax.swing.JLabel dataToExportJLabel;
   private javax.swing.JComboBox destProfileJComboBox;
   private javax.swing.JLabel destProfileJLabel;
   private javax.swing.JComboBox destProjectJComboBox;
   private javax.swing.JLabel destProjectJLabel;
   private javax.swing.JButton exportJButton;
   private javax.swing.JProgressBar exportJProgressBar;
   private javax.swing.JLabel fileProgressJLabel;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JLabel srcProfileJLabel;
   private javax.swing.JLabel srcProfileJTextField;
   private generalUtilities.TwoColourJTable twoColourJTable1;
   private javax.swing.JLabel versionJLabel;
   private javax.swing.JButton viewAnonScriptJButton;
   private javax.swing.JButton viewExportLogJButton;
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
