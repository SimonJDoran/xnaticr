/** ******************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
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
 *********************************************************************
 *
 *********************************************************************
 * @author Simon J Doran
 * Java class: sessionExporter.AnonScriptView
 * First created on 20-Mar-2018 at 10:53:06
 *
 * Dialogue panel view for anonymisation script selection/entry.
 * The dialogue uses the MVC pattern.
 ******************************************************************** */
package sessionExporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.IOUtils;
import static sessionExporter.AnonScriptWindow.logger;

public class AnonScriptView extends javax.swing.JDialog
{
   AnonScriptModel      asm;
   AnonScriptController asc;
   private File         chooserCurrentDir = new File(System.getProperty("user.home"));
   
   public AnonScriptView(java.awt.Frame parent, boolean modal)
   {
      super(parent, modal);
      initComponents();
      asm = new AnonScriptModel();
      asc = new AnonScriptController(this);
      addListeners();
      populateScriptJComboBox();
      setVisible(true);
   }
     
   private void addListeners()
   {
      approveJButton.addActionListener(asc);
		cancelJButton.addActionListener(asc);     
      loadJButton.addActionListener(asc);
      saveJButton.addActionListener(asc);
      saveAsJButton.addActionListener(asc);	  
      scriptJComboBox.addItemListener(asc); 
      scriptJTextArea.getDocument().addDocumentListener(asc);
   }
   
   private void populateScriptJComboBox()
   {
      Map<String, AnonScriptModel.ScriptDetails> scriptMap = asm.getScriptDetails();
      
      DefaultComboBoxModel scriptDcbm  = new DefaultComboBoxModel();
      for (String name : scriptMap.keySet())
      {
         String description = scriptMap.get(name).description;
         scriptDcbm.addElement(description);
      }
      scriptJComboBox.setModel(scriptDcbm);
      scriptJComboBox.setSelectedItem(AnonScriptModel.CUSTOM);
   }
   
   
   public void setText(String text)
   {
      scriptJTextArea.setText(text);
   }
   
   
   public boolean showCancelWarning(int reason)
   {
      String message = "";
      if (reason == AnonScriptController.NOT_SAVED)
         message = "You haven't saved the edits you made to \n"
                    + "the anonymisation script.";
      
      if (reason == AnonScriptController.NOT_APPROVED)
         message = "You haven't approved the anonymisation script.";
      
      Object[] options = {"Yes", "No"};
      int      choice = JOptionPane.showOptionDialog(this, message,
                                                     "Do you want to continue?",
                                                     JOptionPane.DEFAULT_OPTION,
                                                     JOptionPane.WARNING_MESSAGE,
                                                     null, options, options[1]);
      if (choice == 0) return true;
      else if ((choice == 1) || (choice == JOptionPane.CLOSED_OPTION)) return false;
      else return false;
   }
   
   
   public String tryScriptLoad()
   {
      String anonScript = null;
      int    choice = 2;
		do
		{			
			JFileChooser chooser = new JFileChooser();
		
			FileFilter   filter  = new FileNameExtensionFilter("Anon script (*.das)","das");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(chooserCurrentDir);
			chooser.setApproveButtonText("Open");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal  = chooser.showOpenDialog(this);
			chooserCurrentDir = chooser.getCurrentDirectory();
			if (returnVal != JFileChooser.APPROVE_OPTION) return null;
			
			String fileErrMsg = "The following error was encountered when \n"
                             + "trying to load the chosen anonymisation script: \n";
         
			File   chosenFile = chooser.getSelectedFile();
			FileInputStream fis;
			try
			{
				fis = new FileInputStream(chosenFile);
				anonScript = IOUtils.toString(fis, "UTF-8");
			}
			catch (IOException exIO)
			{
				logger.error(fileErrMsg + exIO.getMessage());
				
            JOptionPane.showMessageDialog(this, fileErrMsg + exIO.getMessage(),
                                          "File open error",
                                          JOptionPane.ERROR_MESSAGE);
			}
			
			// Check for pathological case of user selecting a very large non-text
			// file by mistake.
			if (chosenFile.length() >= 15000)
			{
				logger.warn("Script file larger than expected.");
            
            Object[] options = {"Cancel", "Reselect...", "Confirm"};
            
				choice  = JOptionPane.showOptionDialog(this,
						  "The anonymisation script file was larger than expected.\n"
						  + "Please confirm that this file is correct or reselect",
						  "Script larger than expected",
						  JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[1]);
				
				if ((choice == 0) || (choice == JOptionPane.CLOSED_OPTION)) anonScript = null;
			}
		}
		while (choice == 1);
      
      return anonScript;
   }
   
   
   public AnonScriptModel getModel()
   {
      return asm;
   }
   
   public JButton getApproveJButton()
   {
      return approveJButton;
   }
   
   public JButton getCancelJButton()
   {
      return cancelJButton;
   }
   
   public JButton getLoadJButton()
   {
      return loadJButton;
   }
   
   public JButton getSaveJButton()
   {
      return saveJButton;
   }
   
   public JButton getSaveAsJButton()
   {
      return saveAsJButton;
   }
   
   public JComboBox getScriptJComboBox()
   {
      return scriptJComboBox;
   }
   
   public JTextArea getScriptJTextArea()
   {
      return scriptJTextArea;
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

      iconJLabel = new javax.swing.JLabel();
      anonScriptJLabel = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      scriptJTextArea = new javax.swing.JTextArea();
      scriptJComboBox = new javax.swing.JComboBox<>();
      loadJButton = new javax.swing.JButton();
      saveJButton = new javax.swing.JButton();
      saveAsJButton = new javax.swing.JButton();
      approveJButton = new javax.swing.JButton();
      cancelJButton = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      iconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/ICR_DataExporter_small.png"))); // NOI18N

      anonScriptJLabel.setFont(anonScriptJLabel.getFont().deriveFont(anonScriptJLabel.getFont().getStyle() | java.awt.Font.BOLD, anonScriptJLabel.getFont().getSize()+7));
      anonScriptJLabel.setText("Anonymisation Script");

      scriptJTextArea.setColumns(20);
      scriptJTextArea.setRows(5);
      jScrollPane1.setViewportView(scriptJTextArea);

      scriptJComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      loadJButton.setText("Load ...");

      saveJButton.setText("Save");

      saveAsJButton.setText("Save As...");

      approveJButton.setText("Approve");

      cancelJButton.setText("Cancel");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jScrollPane1)
               .addGroup(layout.createSequentialGroup()
                  .addGap(198, 198, 198)
                  .addComponent(iconJLabel)
                  .addGap(18, 18, 18)
                  .addComponent(anonScriptJLabel)
                  .addGap(0, 0, Short.MAX_VALUE))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addGap(25, 25, 25)
                  .addComponent(scriptJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                  .addComponent(loadJButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(saveJButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(saveAsJButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(approveJButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cancelJButton)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(anonScriptJLabel)
               .addComponent(iconJLabel))
            .addGap(18, 18, 18)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(cancelJButton)
               .addComponent(saveJButton)
               .addComponent(loadJButton)
               .addComponent(approveJButton)
               .addComponent(scriptJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(saveAsJButton))
            .addContainerGap(28, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents



   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel anonScriptJLabel;
   private javax.swing.JButton approveJButton;
   private javax.swing.JButton cancelJButton;
   private javax.swing.JLabel iconJLabel;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JButton loadJButton;
   private javax.swing.JButton saveAsJButton;
   private javax.swing.JButton saveJButton;
   private javax.swing.JComboBox<String> scriptJComboBox;
   private javax.swing.JTextArea scriptJTextArea;
   // End of variables declaration//GEN-END:variables
}
