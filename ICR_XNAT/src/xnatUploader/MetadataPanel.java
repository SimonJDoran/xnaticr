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
* Java class: MetadataPanel.java
* First created on Sep 28, 2010 at 9:41:34 AM
* 
* Specialised GUI component for use with the ICR DataUploader a.k.a.
* xnatUploader
*********************************************************************/

package xnatUploader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import configurationLists.DAOSearchableElementsList;
import java.util.List;
import xnatDAO.DAOConstants;



public class MetadataPanel extends JPanel
{
   static  Logger logger = Logger.getLogger(MetadataPanel.class);
   private HashMap<String, Component> fieldMap;
   private JScrollPane pane;
   private GroupLayout panelLayout;

   
   /**
    * Create a new instance of the MetadataPanel. Note that this object
    * is really only a container, the contents of which are dynamically
    * created and destroyed.
    */
   public MetadataPanel()
   {
      fieldMap = new HashMap<String, Component>();
      initialiseUI();
   }
   

   /**
    * Build a new JScrollPane complete with fields ready to receive the metadata
    * of the file being uploaded
    * 
    * @param sel DAOSearchableElementsList containing the contents of the
 configuration XML file in an appropriate structure
    * @param rootElement a string containing the .xsd element name of the "type"
    * of the data object being uploaded
    * @param panel a JPanel inside which all the components sit
    */
   public MetadataPanel(DAOSearchableElementsList sel, String rootElement, JPanel panel)
   {
      fieldMap = new HashMap<String, Component>();
      initialiseUI();
   }


   /**
    * Initialise the user interface components of the MetadataPanel,
    * including JScrollPane where needed and a variety of editable
    * and non-editable components based on XNAT's XML schema descriptions
    * of the metadata to be uploaded.
    */
   private void initialiseUI()
   {      
      // The actual data go in this component.
      String defaultRootElement = "xnat:reconstructedImageData";
      JPanel contentPanel = initialiseContentPanel(defaultRootElement);
      pane = new JScrollPane(contentPanel);
      
      panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
		this.setBackground(DAOConstants.BG_COLOUR);

		panelLayout.setAutoCreateContainerGaps(true);

      panelLayout.setHorizontalGroup(
			panelLayout.createParallelGroup()
			.addComponent(pane, 10, 520, 540)
      );

      panelLayout.setVerticalGroup(
			panelLayout.createSequentialGroup()
         .addComponent(pane, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
      );
   }


   private JPanel initialiseContentPanel(String rootElement)
   {
      DAOSearchableElementsList sel = null;
      try
      {
         // The DAOSearchableElementsList (sel) is an object that maps the contents of the
         // searchableXNATElements.xml file into an easy format for the program to
         // interrogate. It is used here when creating the tabs and in a variety of
         // other places around XNAT_DAO.
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         logger.error("Can't load the DAOSearchableElement list from "
                      + "SearchableXNATElements.xml");
         throw new RuntimeException("Can't load the DAOSearchableElement list "
                                    + "from SearchableXNATElements.xml");
      }

      Vector<String> elementAliases = sel.getSearchableXNATAliases().get(rootElement);
      int            nAliases       = elementAliases.size();


      // Create the JPanel that will actually hold the data.
      JPanel contentPanel = new JPanel();
      
      GroupLayout contentLayout = new GroupLayout(contentPanel);
      contentPanel.setLayout(contentLayout);
		contentPanel.setBackground(DAOConstants.BG_COLOUR);
      
      GroupLayout.SequentialGroup outerSeqGroup = contentLayout.createSequentialGroup();
      GroupLayout.ParallelGroup   outerParGroup = contentLayout.createParallelGroup();

      
      
      boolean invComboBoxStarted = false;
      
      for (int i=-2; i<nAliases; i++)
      {
         String alias = null;
         JLabel label = null;
         Component fieldComponent = null;
         
         // Start by adding two elements at the top, common to all uploads
         // no matter what the data to be uploaded are.
         if (i == -2)
         {
            alias = "XNAT Subject";
            fieldComponent = new JComboBox();
            label = new JLabel(alias);
            label.setBackground(DAOConstants.BG_COLOUR);
         }
         
         if (i == -1)
         {
            alias = "XNAT Session Label";
            fieldComponent = new JComboBox();
            label = new JLabel(alias);
            label.setBackground(DAOConstants.BG_COLOUR);
         }
            
         if (i >= 0)
         {
            alias = elementAliases.elementAt(i);

            // Special treatment for investigator fields.
            if (alias.startsWith("Investigator"))
            {
               label = new JLabel("Investigator");
               label.setBackground(DAOConstants.BG_COLOUR);
               if (!invComboBoxStarted)
               {
                  fieldComponent =  new JComboBox();
                  invComboBoxStarted = true;
               }         
            }
            else
            {
               label = new JLabel(alias);
               label.setBackground(DAOConstants.BG_COLOUR);
               JTextField textField = new JTextField();
               textField.setBackground(DAOConstants.BG_COLOUR);
               textField.setColumns(10);
               textField.setEditable(false);
               fieldComponent = textField;
            }
         }
         
         // Some entries in elementAliases may be grouped together and we do not
         // want a separate component for each.
         if (fieldComponent != null)
         {
            String fieldName = alias;
            if (alias.startsWith("Investigator")) fieldName = "Investigators";
            fieldMap.put(fieldName, fieldComponent);

            GroupLayout.SequentialGroup innerSeqGroup = contentLayout.createSequentialGroup();
            GroupLayout.ParallelGroup   innerParGroup = contentLayout.createParallelGroup();

            innerSeqGroup.addComponent(label, 50, 100, Short.MAX_VALUE);
            innerSeqGroup.addComponent(fieldComponent, 50, 100, Short.MAX_VALUE);
            innerParGroup.addComponent(label, 35, 35, 35);
            innerParGroup.addComponent(fieldComponent, 35, 35, 35);

            outerSeqGroup.addGroup(innerParGroup);
            outerParGroup.addGroup(innerSeqGroup);
         }
      }

      contentLayout.setHorizontalGroup(outerParGroup);
      contentLayout.setVerticalGroup(outerSeqGroup);

      return contentPanel;
   }



   public void replaceContentPanel(String rootElement)
   {
      JPanel newContentPanel = initialiseContentPanel(rootElement);
      JScrollPane newPane    = new JScrollPane(newContentPanel);
      
      panelLayout.replace(pane, newPane);

      // Don't forget to update instance variable pane with newPane,
      // as well as doing the actual Swing replace operation, because
      // otherwise the next time we get round here, we will be trying to
      // replace a component that no longer exists!
      pane = newPane;
   }


   
   /**
    * Get the contents of the dynamically-created text field that maps to the
    * the given alias.
    * 
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @return a String containing the current contents of the field.
    */
   public String getJTextFieldContents(String fieldAlias)
   {
      Component comp = fieldMap.get(fieldAlias);
      
      if (!(comp instanceof JTextField))
         logger.error("Expected component of type JTextField, but found "
                      + comp.getClass().getName());


      JTextField textField = (JTextField) fieldMap.get(fieldAlias);
      return textField.getText();
   }
   
   
   /**
    * Get the contents of the dynamically-created JComboBox that maps to the
    * the given alias.
    * @param fieldAlias a String containing the title of the JComboBox. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @return the selected index of a dynamically created JComboBox
    */
   public int getJComboBoxIndex(String fieldAlias)
   {
      Component comp = fieldMap.get(fieldAlias);
      
      if (!(comp instanceof JComboBox))
         logger.error("Expected component of type JComboBox, but found "
                      + comp.getClass().getName());
      
      JComboBox comboBox = (JComboBox) fieldMap.get(fieldAlias);
      return comboBox.getSelectedIndex();
   }
   
   
   
   /**
    * Set the contents of the dynamically-created JComboBox that maps to the
    * the given alias.
    * @param fieldAlias a String containing the title of the JTextField. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * Most of the pre-populated fields cannot be changed by the user,
    * so the common calling method for this function is to set the
    * field editable status to false.
    * @param value String containing the new text box contents
    */
   public void populateJTextField(String fieldAlias, String value)
   {
      populateJTextField(fieldAlias, value, false);
   }


   
   /**
    * Set the contents of the dynamically-created JComboBox that maps to the
    * the given alias.
    * @param fieldAlias a String containing the title of the JTextField. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * This version of the method allows the field to be set as either editable
    * or non editable. The editable version has a different coloured background.
    * @param value String containing the new text box contents
    */
   public void populateJTextField(String fieldAlias, String value, boolean editable)
   {
      Component comp = fieldMap.get(fieldAlias);
      
      if (!(comp instanceof JTextField))
         logger.error("Expected component of type JTextField, but found "
                      + comp.getClass().getName());


      JTextField textField = (JTextField) comp;
      textField.setText(value);
      textField.setEditable(editable);
      if (editable) textField.setBackground(DAOConstants.EDITABLE_COLOUR);
   }
   
   
   /**
    * Set the contents of the dynamically-created JComboBox that maps to the
    * the given alias.
    * @param fieldAlias a String containing the title of the JTextField. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * Most of the pre-populated fields cannot be changed by the user,
    * so the common calling method for this function is to set the
    * field editable status to false.
    * @param values String array of values to be assigned as items in the JComboBox
    */
   public void populateJComboBox(String fieldAlias, List<String> values)
   {
      Component comp = fieldMap.get(fieldAlias);
      
      if (!(comp instanceof JComboBox))
         logger.error("Expected component of type JComboBox, but found "
                      + comp.getClass().getName());
      
      JComboBox comboBox        = (JComboBox) fieldMap.get(fieldAlias);
      DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
      for (int i=0; i<values.size(); i++) dcbm.addElement(values.get(i));
      comboBox.setModel(dcbm);
   }



   /**
    * Monitor the various components of the interface to decide whether
    * we yet have enough information to upload the metadata.
    */
   public void addInternalListeners(final DataUploader du)
   {
      // Before anything else can happen, the XNAT subject and experiment
      // must be disambiguated. Altering the state of the corresponding
      // JComboBox triggers re-execution of the appropriate part of
      // RTStruct.dependenciesInDatabase.
      final JComboBox jcbSubj = (JComboBox) getComponent("XNAT Subject");
      final JComboBox jcbExp  = (JComboBox) getComponent("XNAT Session Label");
      
      jcbSubj.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            // Note: There are two passes through this method per click: once to tell you
            // that an item has been deselected and once to say that a new item has been chosen.
            // Ignore the first of these.
            if (evt.getStateChange() == ItemEvent.DESELECTED) return;
            String subjLabel = (String) jcbSubj.getSelectedItem();
            for (String key : du.ambiguousSubjExp.keySet())
            {
               if (du.ambiguousSubjExp.get(key).subjectLabel.equals(subjLabel))
               {
                  du.XNATSubjectID = key;
                  ArrayList<String> expIDs    = du.ambiguousSubjExp.get(key).experimentIDs;
                  ArrayList<String> expLabels = du.ambiguousSubjExp.get(key).experimentLabels;
                  
                  populateJComboBox("XNAT Session Label", expLabels);
                  jcbExp.setSelectedItem(expLabels.get(0));
                  du.XNATExperimentID = expIDs.get(0);
               }
            }
            // TODO: There is an issue about this update occurring on the
            //       Event Dispatch Thread. However, since on first setup of
            //       the ComboBox, the process is already running on a different
            //       during which updateParseFile should execute *synchronously*,
            //       this starts to get confusing.
            du.updateParseFile();
         }
      });
      
      jcbExp.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            if (evt.getStateChange() == ItemEvent.DESELECTED) return;
            String subjLabel = (String) jcbSubj.getSelectedItem();
            for (String key : du.ambiguousSubjExp.keySet())
            {
               if (du.ambiguousSubjExp.get(key).subjectLabel.equals(subjLabel))
               {
                  du.XNATSubjectID = key;
                  ArrayList<String> expIDs    = du.ambiguousSubjExp.get(key).experimentIDs;
                                    
                  int ind = jcbExp.getSelectedIndex();
                  du.XNATExperimentID = expIDs.get(ind);
               }
            }
            du.setBusyCursor(jcbSubj);
            du.updateParseFile();
            du.cancelBusyCursor(jcbSubj);
         }
      });
      
      
      // Add listeners for the editable fields. Allow the flexibility for each
      //concrete class to define its own fields to watch.
      String[] requiredData = du.getRequiredFields();

      for (int i=0; i<requiredData.length; i++)
      {
         final String     alias = requiredData[i];
         final JTextField tf    = (JTextField) getComponent(alias);
         tf.addActionListener(new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               du.setStringField(alias, tf.getText());
               MetadataPanel.this.putClientProperty("enableUpload", du.rightMetadataPresent());
               }
         });

         tf.addFocusListener(new FocusListener()
         {
            @Override
            public void focusGained(FocusEvent evt)
            {
            }

            @Override
            public void focusLost(FocusEvent evt)
            {
               du.setStringField(alias, tf.getText());
               MetadataPanel.this.putClientProperty("enableUpload", du.rightMetadataPresent());
            }
         });

         tf.addKeyListener(new KeyAdapter()
         {
            @Override
            public void keyTyped(KeyEvent evt)
            {
               du.setStringField(alias, tf.getText());
               MetadataPanel.this.putClientProperty("enableUpload", du.rightMetadataPresent());
            }
         });
      }

      // All the uploaders allow the investigator to be selected.
      final JComboBox jcb = (JComboBox) getComponent("Investigators");
      jcb.addItemListener(new ItemListener()
      {
         @Override
         public void itemStateChanged(ItemEvent evt)
         {
            // Note: There are two passes through this method per click: once to tell you
            // that an item has been deselected and once to say that a new item has been chosen.
            // Ignore the first of these.
            if (evt.getStateChange() == ItemEvent.DESELECTED) return;
            int ind = jcb.getSelectedIndex();
          //  du.getXNATInvestigators().setInvestigatorNumber(ind);
         }
      });
   }
   
   
   /**
    * Get the dynamically-created GUI Component that is associated with the
    * the given alias. This allows, for example, methods in other classes
    * to add listeners.
    * @param fieldAlias a String containing the title of the Component. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @return a dynamically-created Swing Component
    */
   public Component getComponent(String fieldAlias)
   {
      return fieldMap.get(fieldAlias);
   }
}
