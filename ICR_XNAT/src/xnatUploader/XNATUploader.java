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
* Java class: XNATUploader.java
* First created on Sep 28, 2010, 8:31:44 AM
* 
* Main GUI for ICR DataUploader a.k.a. xnatUploader
* Mirrors XNATDAO.java
*********************************************************************/

package xnatUploader;

import generalUtilities.NextMatchingFileWorker;
import generalUtilities.SimpleColourTable;
import imageUtilities.DownloadIcon;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import configurationLists.DAOSearchableElementsList;
import org.apache.commons.lang.WordUtils;
import xnatDAO.XNATGUI;
import xnatDAO.XNATProfile;

/**
 *
 * @author simond
 */
public final class XNATUploader extends XNATGUI
{
   static  Logger                 logger   = Logger.getLogger(XNATUploader.class);
   private DataUploader           uploader = null;
   private GroupLayout            panelLayout;
   private File                   contextRoot;
   private File                   searchProgress;
   private File                   chooserCurrentDir = null;
   private NextMatchingFileWorker nmfWorker;
   private UploadToXNATWorker     uploadWorker;
   private PrepareUploadWorker    puWorker;
   
   // String constants to ensure consistency if wording changes need to occur.
   private static final String    NONE_SELECTED = "<None selected>";
   private static final String    UPLOADING     = "<Uploading to XNAT>";
   private static final String    DIR_SCAN_PROG = "Directory scanning in progress";
   private static final String    DIR_SCAN_INT  = "Directory scanning interrupted";
   private static final String    UPLOAD_PREP   = "Preparing upload";
   private static final String    UPLOAD_PROG   = "Upload to XNAT in progress";
   private static final String    UPLOAD_INT    = "Upload to XNAT interrupted";
   private static final String    UPLOAD        = "Upload";
   private static final String    ABORT         = "Abort";
   
   // Map to contain all the DataUploader objects that have so far been implemented.
   HashMap<TypeSubtype, Class<? extends DataUploader>> uploaderClassMap;
   

   /** Creates new form XNATUploader */
   public XNATUploader(java.awt.Frame parent, boolean modal)
   {
      super(parent, modal);

      initComponents();
      versionJLabel.setText("Version " + version); 
      addListeners();
      implementRestrictions();
      populateCommonComponents();
      populateSpecificComponents();
      
      // Temporary code to relieve some clicking during development.
      dataTypeJComboBox.setSelectedItem("Regions-of-interest");
      
      // The upload button should be greyed out until there are some valid
      // data to upload.
      enableUpload(false);
   }



   /**************************************
    * External API for client applications
    ***************************************/


   /**
    * Call the program directly from the command line. It is not intended
    * that this be the norm, but it is useful to have a main() method for use
    * with the IDE.
    *
    * @param args the command line arguments
    */
   public static void main(String args[])
   {
      java.awt.EventQueue.invokeLater(new Runnable()
      {
         @Override
         public void run()
         {
            try
				{
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          //     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				}
				catch (Exception exIgnore) {}

				XNATUploader xnup  = new XNATUploader(new javax.swing.JFrame(), true);
				xnup.invoke(true);
			}
		});
   }


   /**
    * Put the dialogue onto the screen and wait for a selection from the user.
    * However, note that, once the user has clicked "Upload", we return
    * immediately with void. Since the selection might involve transfer of data
    * from to a database that is not on the local machine, things have to be done
    * asynchronously. In order not to block the UI and the calling application,
    * we cannot hang around and wait for this method to return a result to the
    * calling thread. Instead, we also provide the method checkForResult(), which
    * also indicates whether the data download operation was terminated by a click
    * on the cancel button.
	 */
	public void invoke(boolean invokedByRun)
	{
	   this.invokedByRun = invokedByRun;
		setVisible(true);
	}
   
   
   /**
    * API for the external client application to find out the status of the
    * file-retrieve operation.
    *
    * @return the status String
    */
   public String getStatus()
   {
      return status;
   }

   /********************* End of external API ***********************/



   /**
    * Add the listeners that allow actions to be implemented when the
    * users click on relevant buttons.
    */
   private void addListeners()
   {
      cancelUploaderJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
            
            // If we have started up the code from the command line, or from the IDE,
            // then clicking cancel should cause Java to exit. If invoked programmatically,
            // then the object should stick around to receive queries from the API.
            if (invokedByRun) System.exit(0);
			}
		});

      
      chooseFileJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chooseFileJButtonActionPerformed();
			}
		});
      
      
      uploadJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				uploadJButtonActionPerformed();
			}
		});



      profileJComboBox.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            profileJComboBoxItemStateChanged(evt, true);
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



   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      downloadIcon1 = new imageUtilities.DownloadIcon();
      jPanel1 = new javax.swing.JPanel();
      jSplitPane1 = new javax.swing.JSplitPane();
      curDBLogPanel = new javax.swing.JPanel();
      poweredByXNATJLabel = new javax.swing.JLabel();
      ICRLogoJLabel = new javax.swing.JLabel();
      titleLabel = new javax.swing.JLabel();
      versionJLabel = new javax.swing.JLabel();
      uploadJButton = new javax.swing.JButton();
      cancelUploaderJButton = new javax.swing.JButton();
      profileJLabel = new javax.swing.JLabel();
      profileJComboBox = new javax.swing.JComboBox();
      dataSubtypeJLabel = new javax.swing.JLabel();
      dataSubtypeJComboBox = new javax.swing.JComboBox();
      dataFileJLabel = new javax.swing.JLabel();
      chooseFileJButton = new javax.swing.JButton();
      datatypeJLabel = new javax.swing.JLabel();
      dataTypeJComboBox = new javax.swing.JComboBox();
      dataFilenameJLabel = new javax.swing.JLabel();
      dirRootJLabel = new javax.swing.JLabel();
      dirRootNameJLabel = new javax.swing.JLabel();
      modeLabel = new javax.swing.JLabel();
      jPanel2 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      logJTextArea = new javax.swing.JTextArea();
      metadataJPanel = new xnatUploader.MetadataPanel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      jPanel1.setBackground(new java.awt.Color(255, 255, 255));
      jPanel1.setPreferredSize(new java.awt.Dimension(0, 500));

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 0, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 683, Short.MAX_VALUE)
      );

      jSplitPane1.setDividerLocation(370);
      jSplitPane1.setDividerSize(5);
      jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
      jSplitPane1.setPreferredSize(new java.awt.Dimension(500, 778));

      curDBLogPanel.setBackground(new java.awt.Color(255, 255, 255));
      curDBLogPanel.setPreferredSize(new java.awt.Dimension(500, 336));

      poweredByXNATJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/powered_by_XNAT.jpg"))); // NOI18N

      ICRLogoJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_fibonacci_logo.jpg"))); // NOI18N

      titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 28)); // NOI18N
      titleLabel.setText("Data Uploader");

      versionJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
      versionJLabel.setText("1.0 beta (build 243)");

      uploadJButton.setText("Upload");

      cancelUploaderJButton.setText("Cancel");

      profileJLabel.setText("XNAT profile");

      profileJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Choose a profile..." }));

      dataSubtypeJLabel.setText("Data subtype");

      dataSubtypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Enter data type..." }));

      dataFileJLabel.setText("Data file");

      chooseFileJButton.setText("Choose file/directory root...");
      chooseFileJButton.setActionCommand("Choose file/root directory...");

      datatypeJLabel.setText("XNAT datatype");

      dataTypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Choose a data type..." }));

      dataFilenameJLabel.setText("Placemarker text");

      dirRootJLabel.setText("Directory root");

      dirRootNameJLabel.setText("Placemarker text");

      modeLabel.setForeground(new java.awt.Color(172, 18, 28));
      modeLabel.setText("Batch mode");

      org.jdesktop.layout.GroupLayout curDBLogPanelLayout = new org.jdesktop.layout.GroupLayout(curDBLogPanel);
      curDBLogPanel.setLayout(curDBLogPanelLayout);
      curDBLogPanelLayout.setHorizontalGroup(
         curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(curDBLogPanelLayout.createSequentialGroup()
            .addContainerGap()
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(curDBLogPanelLayout.createSequentialGroup()
                  .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(curDBLogPanelLayout.createSequentialGroup()
                        .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                           .add(datatypeJLabel)
                           .add(profileJLabel))
                        .add(18, 18, 18)
                        .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                           .add(profileJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 378, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                           .add(dataTypeJComboBox, 0, 392, Short.MAX_VALUE)))
                     .add(curDBLogPanelLayout.createSequentialGroup()
                        .add(16, 16, 16)
                        .add(ICRLogoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                           .add(org.jdesktop.layout.GroupLayout.TRAILING, titleLabel)
                           .add(org.jdesktop.layout.GroupLayout.TRAILING, versionJLabel))
                        .add(18, 18, 18)
                        .add(poweredByXNATJLabel))
                     .add(curDBLogPanelLayout.createSequentialGroup()
                        .add(dataSubtypeJLabel)
                        .add(28, 28, 28)
                        .add(dataSubtypeJComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                  .add(40, 40, 40))
               .add(org.jdesktop.layout.GroupLayout.TRAILING, curDBLogPanelLayout.createSequentialGroup()
                  .add(modeLabel)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .add(chooseFileJButton)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(uploadJButton)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(cancelUploaderJButton)
                  .addContainerGap())
               .add(org.jdesktop.layout.GroupLayout.TRAILING, curDBLogPanelLayout.createSequentialGroup()
                  .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(dataFileJLabel)
                     .add(dirRootJLabel))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(curDBLogPanelLayout.createSequentialGroup()
                        .add(dirRootNameJLabel)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, curDBLogPanelLayout.createSequentialGroup()
                        .add(dataFilenameJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 361, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(48, 48, 48))))))
      );
      curDBLogPanelLayout.setVerticalGroup(
         curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(curDBLogPanelLayout.createSequentialGroup()
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(curDBLogPanelLayout.createSequentialGroup()
                  .addContainerGap()
                  .add(ICRLogoJLabel))
               .add(curDBLogPanelLayout.createSequentialGroup()
                  .add(28, 28, 28)
                  .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(poweredByXNATJLabel)
                     .add(curDBLogPanelLayout.createSequentialGroup()
                        .add(titleLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(versionJLabel)))))
            .add(20, 20, 20)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(profileJLabel)
               .add(profileJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(datatypeJLabel)
               .add(dataTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(dataSubtypeJLabel)
               .add(dataSubtypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(21, 21, 21)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(dirRootJLabel)
               .add(dirRootNameJLabel))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(dataFileJLabel)
               .add(dataFilenameJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(27, 27, 27)
            .add(curDBLogPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cancelUploaderJButton)
               .add(uploadJButton)
               .add(chooseFileJButton)
               .add(modeLabel))
            .add(8, 8, 8))
      );

      jSplitPane1.setTopComponent(curDBLogPanel);

      logJTextArea.setBackground(new java.awt.Color(238, 238, 238));
      logJTextArea.setColumns(20);
      logJTextArea.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      logJTextArea.setRows(5);
      jScrollPane1.setViewportView(logJTextArea);

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
      );

      jSplitPane1.setRightComponent(jPanel2);

      metadataJPanel.setPreferredSize(new java.awt.Dimension(300, 300));

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
               .add(metadataJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
            .add(202, 202, 202))
         .add(layout.createSequentialGroup()
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(metadataJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 413, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents


 
   /**
    * Initialisation for specific fields of the UI that are not handled by the
    * superclass.
    */
   private void populateSpecificComponents()
   {
      downloadIcon = new DownloadIcon(dataFilenameJLabel);
      dataFilenameJLabel.setText(NONE_SELECTED);
      dataFilenameJLabel.setIcon(downloadIcon);
      dataFilenameJLabel.setHorizontalTextPosition(SwingConstants.LEFT);
      dirRootNameJLabel.setText(NONE_SELECTED);
      modeLabel.setText("");
      modeLabel.setForeground(SimpleColourTable.getColour("ICR red"));
      logJTextArea.setFont(new Font("Courier", Font.PLAIN, 10));
   }
   

   /**
    * Until all the different DataUploader objects are implemented, there
    * are restrictions on what can be uploaded and hence, some menu options
    * will be greyed out.
    */
   @Override
   public void implementRestrictions()
   {
      uploaderClassMap = new HashMap<TypeSubtype, Class<? extends DataUploader>>();
      
      uploaderClassMap.put(new TypeSubtype("Regions-of-interest", "Set of ROIs"),
                           RtStructDataUploader.class);
	
		uploaderClassMap.put(new TypeSubtype("Image annotations", "AIM image annotation collection"),
                           AimImageAnnotationCollectionDataUploader.class);
//      uploaderClassMap.put(new TypeSubtype("Application outputs", "Adept output"),
//                           AdeptDataUploader.class);
//      uploaderClassMap.put(new TypeSubtype("Application outputs", "MRIW output"),
//                           MRIWDataUploader.class);
//		uploaderClassMap.put(new TypeSubtype("Application outputs", "AIM output"),
//                           AIMDataUploader.class);
      //uploaderClassMap.put(new TypeSubtype("Parametric images", "Any parametric image"),
      //                     ParametricImageUploader.class);
   }
   
   
   /**
    * Helper inner class used by uploaderClassMap
    */
   public class TypeSubtype
   {
      public String type;
      public String subtype;
      
      public TypeSubtype(String type, String subtype)
      {
         this.type    = type;
         this.subtype = subtype;
      }
   }
   
   
   
   /**
    * Check whether the given type can be selected from the ComboBox. Here, it
    * might not be selectable because the given functionality has not yet been
    * built, i.e., the relevant DataUploader object has not been built.
    * @param type String containing the name of the type to check
    * @return true if the given type entry should be selectable
    */
   @Override
   public boolean typeIsSelectable(String type)
   {
      Set<TypeSubtype>  keys = uploaderClassMap.keySet();
      
      for (TypeSubtype ts : keys)
         if ((ts.type).equals(type))
            return true;
      
      return false;
   }
   
 
   
   /**
    * Check whether the given subtype can be selected from the ComboBox. Here, it
    * might not be selectable because the given functionality has not yet been
    * built, i.e., the relevant DataUploader object has not been built.
    * @param subtype String containing the name of the subtype to check
    * @return true if the given type entry should be selectable
    */
   @Override
   public boolean subtypeIsSelectable(String subtype)
   {
      Set<TypeSubtype>  keys = uploaderClassMap.keySet();
      
      for (TypeSubtype ts : keys)
         if ((ts.subtype).equals(subtype)) 
           return true;
      
      return false;
   }
   
   
    
   
   /* -------------------------------------------------------------------------
    * The following section of the code is a somewhat involved set of methods
    * to invoke a set of SwingWorker objects. There are a number of operations
    * involved in the upload operation that are time-consuming and that each
    * need their own separate threads in order not to block the event dispatch
    * thread. There is a recurring pattern to the way I have done this. In each
    * case, a SwingWorker is invoked and this works asynchronously. A
    * PropertyChangeListener is attached to the worker thread and this triggers
    * a responding method when the SwingWorker returns.
    * -------------------------------------------------------------------------
   
   /**
    * Take appropriate action when the "Choose Data File" button is pressed.
    */
   private void chooseFileJButtonActionPerformed()
   {
      // Safety net: this should never happen, because unavailable entries in
      // the data type and data subtype should be greyed out.
      if (uploader == null)
      {
         JOptionPane.showMessageDialog(this, "Loading this type of object is not implemented yet!",
              "Not yet implemented", JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(chooserCurrentDir);
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      int returnVal  = chooser.showOpenDialog(this);
      if (returnVal != JFileChooser.APPROVE_OPTION) return;
      File fileOrDir = chooser.getSelectedFile();
      
      // Don't try to upload if not connected.
      XNATProfile xnprf = profileList.getCurrentProfile();
      if (!xnprf.isConnected()) xnprf.connectWithAuthentication(this);
      if (!xnprf.isConnected()) return;
      populateProfileJComboBox();
      tryToSaveProfiles();   
      
      // Note that the profile might have changed since the Uploader
      // was first created.
      uploader.setProfile(profileList.getCurrentProfile());
      
      
      chooserCurrentDir = fileOrDir.getParentFile();
      if (fileOrDir.isFile())
      {
			uploader.setUploadFile(fileOrDir);
         invokeUploadPreparation();
      }
      
      
      if (fileOrDir.isDirectory())
      {
         modeLabel.setText("Batch mode");
         contextRoot    = fileOrDir;
         searchProgress = fileOrDir;
         invokeGetNextMatchingFile();
         // Note: When we get the next matching file back from the system, we
         // also have to run invokeUploadPreparation, as required for the file
         // case above. However, this can't be done until the worker thread that
         // descends the directory tree has returned with the next file.
      }      
   }
   
   
   
   /**
    * Search down the directory hierarchy for the next file that can be loaded
    * by the selected uploader.
    * This is potentially an extremely expensive operation, since we have
    * to parse every file as we descend the hierarchy. We cannot rely on
    * clues in the form of the filename or extension, as these will be
    * present only inconsistently. We also have no idea at the start just
    * how extensive the hierarchy is and what fraction of the files are of
    * the desired type. So we need to make this a background task with the
    * facility to cancel it. 
    */
   private void invokeGetNextMatchingFile()
   {
      dataFilenameJLabel.setText("<Scanning directory tree for first file>");
      dirRootNameJLabel.setText(getAbbreviatedString(contextRoot.getPath(), 50));
      downloadIcon.start();
      chooseFileJButton.setEnabled(false);
           
      nmfWorker = new NextMatchingFileWorker(contextRoot, contextRoot, uploader);
      nmfWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            processNextMatchingFile(evt);
         }
      });

      try
      {
         nmfWorker.execute();
      }
      catch (Exception ex){} // ignore
   }



   /**
    * This is where we get to once the NextMatchingFileWorker returns with data
    * for a single file upload.
    */
   private void processNextMatchingFile(PropertyChangeEvent evt)
   {
      // Ignore this call if the property change in question is that from "PENDING"
      // to "STARTED".
      if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;

      try
      {
         searchProgress = nmfWorker.get();
      }
      catch (InterruptedException exIE)
      {
         return;
      }
      catch (ExecutionException exEE)
      {
         JOptionPane.showMessageDialog(XNATUploader.this,
              "I was unable to retrieve any valid filenames\n"
                  + "for the following reason:\n"
                  + exEE.getMessage(),
              "Failed to retrieve filenames",
              JOptionPane.ERROR_MESSAGE);
         return;
      }
      finally
      {
         downloadIcon.stop();
         chooseFileJButton.setEnabled(true);
      }

      // Now perform all the actions from chooseFileJButtonActionPerformed()
      // that would have been done had we had direct access to the file.
      if (searchProgress == null)
      {
         JOptionPane.showMessageDialog(XNATUploader.this,
              "There were no files of the correct type located\n"
              + "below the selected root directory",
              "No matching files",
              JOptionPane.ERROR_MESSAGE);
         clearDisplay();
         modeLabel.setText(" ");
         return;
      }

      uploader.setUploadFile(searchProgress);
      invokeUploadPreparation();
   }

   
   
   /**
    * This method does a similar job to invokeGetNextMatchingFile above, except
    * that, rather than simply returning a File on a single occasion, it is
    * designed to be called repeatedly during a batch upload operation.
    */
   private void invokeGetNextMatchingFileDuringBatchUpload()
   {
      dataFilenameJLabel.setText("<Scanning directory tree for next file>");
      downloadIcon.start();
      nmfWorker = new NextMatchingFileWorker(contextRoot, searchProgress, uploader);
      nmfWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            processNextMatchingFileDuringBatchUpload(evt);
         }
      });

      try
      {
         nmfWorker.execute();
      }
      catch (Exception ex){} // ignore
   }


   /**
    * This is where we get to when NextMatchingFileWorker returns with data
    * during the batch upload process.
    * @param evt
    */
   private void processNextMatchingFileDuringBatchUpload(PropertyChangeEvent evt)
   {
      // Ignore this call if the property change in question is that from "PENDING"
      // to "STARTED".
      if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;

      try
      {
         searchProgress = nmfWorker.get();
      }
      catch (InterruptedException exIE)
      {
         return;
      }
      catch (ExecutionException exEE)
      {
         JOptionPane.showMessageDialog(XNATUploader.this,
              "I was unable to retrieve any valid filenames\n"
                  + "for the following reason:\n"
                  + exEE.getMessage(),
              "Failed to retrieve filenames",
              JOptionPane.ERROR_MESSAGE);
         return;
      }
      finally
      {
         downloadIcon.stop();
         chooseFileJButton.setEnabled(true);
      }

      if (searchProgress == null) return;

      uploader.setUploadFile(searchProgress);
      invokeBatchUpload();
   }
   
   
   
   /**
    * Prepare a file for upload by reading, parsing and validating it as a
    * file of the correct type. This is a time-consuming process and hence
    * needs to be done by a separate worker thread and not on the event dispatch
    * thread.
    */
   private void invokeUploadPreparation()
   {
      status = UPLOAD_PREP;
      dataFilenameJLabel.setText(
         getAbbreviatedString(uploader.getUploadFile().getName(), 30)
         + "  <Preparing upload>");
      downloadIcon.start();
      chooseFileJButton.setEnabled(false);

      puWorker = new PrepareUploadWorker(uploader);
      puWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            continueUploadPreparation(evt);
         }
      });

      try
      {
         puWorker.execute();
      }
      catch (Exception ex){} // ignore
   }
   
   
   
   /**
    * This is where we get to once the invokeUploadPreparation completes its task.
    */
   private void continueUploadPreparation(PropertyChangeEvent evt)
   {
      // Ignore this call if the property change in question is that from "PENDING"
      // to "STARTED".
      if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;

      // The PrepareUploadWorker doesn't actually need to return anything,
      // because all it is doing is using methods of uploader and the latter
      // object stores everything that is needed.
      chooseFileJButton.setEnabled(true);
      downloadIcon.stop();
      
      if (uploader.errorOccurred())
      {
         String err = WordUtils.wrap(uploader.getErrorMessage(), 60, "\n", true);
         JOptionPane.showMessageDialog(this, uploader.getUploadFile().getName() +
                 "\n\n" + err,
                 "File-open error", JOptionPane.ERROR_MESSAGE);
         dataFilenameJLabel.setText(NONE_SELECTED);
         clearDisplay();
         logger.debug(uploader.getUploadFile().getName() +
                      "\n" + uploader.getErrorMessage() + "\n");
         return;
      }
      
      if (uploader.isPreparedForUpload())
      {
         dataFilenameJLabel.setText(
                 getAbbreviatedString(uploader.getUploadFile().getName(), 50));
         uploader.populateFields(metadataJPanel);
         enableUpload(uploader.rightMetadataPresent());
      }
      
   }

   
   
   /**
    * Perform the actual upload.
    */
   private void uploadJButtonActionPerformed()
   {
      if (status.equals(DIR_SCAN_PROG))
      {
         status = DIR_SCAN_INT;
         nmfWorker.cancel(true);
         uploadJButton.setText(UPLOAD);
      }
      
      if (status.equals(UPLOAD_PROG))
      {
         status = UPLOAD_INT;
         uploadWorker.cancel(true);
         uploadJButton.setText(UPLOAD);
      }
      
      
      if (modeLabel.getText().equals("Batch mode"))
      {
         uploader.setBatchLabelPrefix(uploader.getStringField("Label"));
         uploader.setBatchNote(uploader.getStringField("Note"));
         uploader.setBatchModeEnabled(true);
         uploadJButton.setText(ABORT);
         invokeBatchUpload();
      }

      else  // single file mode
      {
         uploader.setBatchModeEnabled(false);
         uploadJButton.setText(ABORT);
         invokeSingleFileUpload();
      }
   }


   
   private void invokeSingleFileUpload()
   {
      status = UPLOAD_PROG;
      dataFilenameJLabel.setText(
         getAbbreviatedString(uploader.getUploadFile().getName(), 30)
         + "  " + UPLOADING);
      downloadIcon.start();
      chooseFileJButton.setEnabled(false);
      
      uploadWorker = new UploadToXNATWorker(uploader);
      uploadWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            processSingleFileUpload(evt);
         }
      });

      try
      {
         uploadWorker.execute();
      }
      catch(Exception ex)
      {
         JOptionPane.showMessageDialog(this, "Unable to upload selected data"
               + " file to XNAT. \n"
               + ex.getMessage(), "Data upload error", JOptionPane.ERROR_MESSAGE);

         logJTextArea.append("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + ex.getMessage() + "\n");

         logger.debug("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + ex.getMessage() + "\n");

         clearDisplay();
         downloadIcon.stop();
         uploadJButton.setText(UPLOAD);
         return;
      }
   }
   
   
   
   private void processSingleFileUpload(PropertyChangeEvent evt)
   {
      // Ignore this call if the property change in question is that from "PENDING"
      // to "STARTED".
      if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;
      
      // The uploader is designed to be called in one of two ways, either via
      // instantiation by an external client (e.g., IDL or MATLAB) or via
      // the run() method. If the tool has been called from outside, then we
      // leave it to the calling application to dispose of the tool. If calling
      // via run(), then the default behaviour could be for the tool to exit on
      // completion, as with the DataChooser. However, it proves quite
      // convenient to keep it open during testing, so in this case, simply
      // reset the fields, buttons, etc. and write something to the log window.
      downloadIcon.stop();
      if (uploader.errorOccurred())
      {
         String err = WordUtils.wrap(uploader.getErrorMessage(), 60, "\n", true);
         JOptionPane.showMessageDialog(this, "Unable to upload selected data"
            + " file to XNAT. \n\n"
            + err, "Data upload error", JOptionPane.ERROR_MESSAGE);
         
         logJTextArea.append("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + uploader.getErrorMessage() + "\n\n");
            
         logger.debug("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + uploader.getErrorMessage() + "\n\n");
      }
      
      else
      {
         logJTextArea.append("Upload successful for " + uploader.getUploadFile().getPath() + "\n\n");
         logger.debug("Upload successful for " + uploader.getUploadFile().getPath() + "\n\n");
      }
      
      clearDisplay();
      uploadJButton.setText(UPLOAD);
      chooseFileJButton.setEnabled(true);
   }
   
   

   private void invokeBatchUpload()
   {
      uploadJButton.setText(ABORT);
      
      dataFilenameJLabel.setText(
         getAbbreviatedString(uploader.getUploadFile().getName(), 30)
            + "  " + UPLOADING);
      downloadIcon.start();
      chooseFileJButton.setEnabled(false);
      
      // The only items not catered for by the upload preparation are the
      // label and note elements. For batch upload, note will be taken to apply
      // to all the datasets, whilst label will be taken from the text that the
      // user enters, followed by the current value of System.nanoTime(), which
      // should be unique.
      
      uploadWorker = new UploadToXNATWorker(uploader);
      uploadWorker.addPropertyChangeListener(new PropertyChangeListener()
      {
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            processBatchUpload(evt);
         }
      });

      try
      {
         uploadWorker.execute();
      }     
      catch(Exception ex)
      {
         logJTextArea.append("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + ex.getMessage() + "\n\n");

         logger.debug("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + ex.getMessage() + "\n\n");
         
         invokeGetNextMatchingFileDuringBatchUpload();

         return;
      }
   }
   
   
   
   private void processBatchUpload(PropertyChangeEvent evt)
   {
      // Ignore this call if the property change in question is that from "PENDING"
      // to "STARTED".
      if (evt.getOldValue().equals(SwingWorker.StateValue.PENDING)) return;
      
      uploadJButton.setText(UPLOAD);
      if (uploader.errorOccurred())
      {  
         logJTextArea.append("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + uploader.getErrorMessage() + "\n\n");
            
         logger.debug("FAILED to upload "
            + uploader.getUploadFile().getPath() + "\n" + uploader.getErrorMessage() + "\n\n");
      }
      
      else
      {
         logJTextArea.append("Upload successful for " + uploader.getUploadFile().getPath() + "\n\n");
         logger.debug("Upload successful for " + uploader.getUploadFile().getPath() + "\n\n");
      }
      
      invokeGetNextMatchingFileDuringBatchUpload();
   }

   

   private void clearDisplay()
   {
      dataFilenameJLabel.setText(NONE_SELECTED);
      dirRootNameJLabel.setText(NONE_SELECTED);
      uploader.clearFields(metadataJPanel);
   }
   

   // End of invocation section
   // -------------------------------------------------------------------------
   
   
   

   /** Enable or grey out the upload button appropriately.
    * 
    * @param enabled boolean indicating whether or not uploads should be allowed.
    */
   public void enableUpload(boolean enabled)
   {
      uploadJButton.setEnabled(enabled);
   }
   
    
   
   /** Method allowing this class to use the results obtained from the
    *  dataSubtypeJComboBoxItemStateChanged in the supertype for its own purposes.
    */
   @Override
   public void useSubtype(String subtype, Vector<String> subtypes, String subtypeAlias)
   {
      // Change the bottom half of the display to reflect the different fields
      // that can be set with the new subtype.
      metadataJPanel.replaceContentPanel(subtype);
      
      // Extract the correct uploader class and initialise a new instance.
      Set<TypeSubtype>  keys = uploaderClassMap.keySet();
      uploader = null;
      Constructor<?> con;
      
      for (TypeSubtype ts : keys)
      {
         if (ts.subtype.equals(subtypeAlias))
         {
            con = (uploaderClassMap.get(ts).getDeclaredConstructors())[0];
            try
            {
               uploader = (DataUploader) con.newInstance(profileList.getCurrentProfile());
            }
            catch (InstantiationException exIE)    {logger.error(exIE);}
            catch (IllegalAccessException exIA)    {logger.error(exIA);}
            catch (IllegalArgumentException exIA)  {logger.error(exIA);}
            catch (InvocationTargetException exIT)
				{
					logger.error(exIT);
				}
         }
      }
      
      if (uploader != null)
      {
         uploader.setVersion(version);
         metadataJPanel.addInternalListeners(uploader);
         metadataJPanel.addPropertyChangeListener(new PropertyChangeListener()
         {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
               if (evt.getPropertyName().equals("enableUpload"))
                  enableUpload((Boolean) evt.getNewValue());
            }
         });
         clearDisplay();
      }
   }
  
   

   private DAOSearchableElementsList getSearchableElementList()
   {
      DAOSearchableElementsList sel = null;
      try
      {
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         JOptionPane.showMessageDialog(this, "Programming error - please report to Simon!\n"
               + exIO.getMessage(), "XNAT Uploader programming error!", JOptionPane.ERROR_MESSAGE);
         System.exit(1);
      }
      return sel;
   }

   

   public static String getAbbreviatedString(String s, int nChar)
   {
      String result;
      int    len  = s.length();
      if (len <= nChar) result = s;
      else
      {
         int n  = (nChar - 3)/2;
         result = s.substring(0, n+(nChar%2)) + "..."
                  + s.substring(len - n + 1);
      }
      return result;
   }
   
   
   
   /**
    * Getter method to allow a method defined in this superclass to have
    * access to the subclass's variable. Each different subclass of XNATGUI
    * will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data type JcomboBox.
    */
   @Override
   public JComboBox getDataTypeJComboBox()
   {
      return dataTypeJComboBox;
   }

   

   /**
    * Getter method to allow a method defined in this superclass to have
    * access to the subclass's variable. Each different subclass of XNATGUI
    * will implement this to return the projectJComboBox from its own GUI.
    * @return the variable corresponding to the data subtype JcomboBox.
    */
   @Override
   public JComboBox getDataSubtypeJComboBox()
   {
      return dataSubtypeJComboBox;
   }


   @Override
   public JComboBox getProfileJComboBox()
   {
      return profileJComboBox;
   }
   
   
   /**
    * Concrete classes implement this method to communicate whether to allow
    * selection of profiles referencing "All accessible XNAT projects".
    * @return true if "all projects" can be selected false otherwise.
    */
   @Override
   protected boolean allowAll()
   {
      return false;
   }


   /**
    * Concrete subclasses of XNATGUI implement this method to define specific
    * actions that take place when the profile is changed.
    */
   @Override
   protected void profileChangeConcreteClassActions()
   {
      // Ensure that the correct details about the connection are passed to
      // the uploader.
      if (uploader != null) uploader.setProfile(profileList.getCurrentProfile());
   }


    

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel ICRLogoJLabel;
   private javax.swing.JButton cancelUploaderJButton;
   private javax.swing.JButton chooseFileJButton;
   private javax.swing.JPanel curDBLogPanel;
   private javax.swing.JLabel dataFileJLabel;
   private javax.swing.JLabel dataFilenameJLabel;
   private javax.swing.JComboBox dataSubtypeJComboBox;
   private javax.swing.JLabel dataSubtypeJLabel;
   private javax.swing.JComboBox dataTypeJComboBox;
   private javax.swing.JLabel datatypeJLabel;
   private javax.swing.JLabel dirRootJLabel;
   private javax.swing.JLabel dirRootNameJLabel;
   private imageUtilities.DownloadIcon downloadIcon1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JSplitPane jSplitPane1;
   private javax.swing.JTextArea logJTextArea;
   private xnatUploader.MetadataPanel metadataJPanel;
   private javax.swing.JLabel modeLabel;
   private javax.swing.JLabel poweredByXNATJLabel;
   private javax.swing.JComboBox profileJComboBox;
   private javax.swing.JLabel profileJLabel;
   private javax.swing.JLabel titleLabel;
   private javax.swing.JButton uploadJButton;
   private javax.swing.JLabel versionJLabel;
   // End of variables declaration//GEN-END:variables

}
