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
 * Java class: sessionExporter.AnonScriptController
 * First created on 20-Mar-2018 at 11:14:11
 *
 * Controller for the entry of anonymisation scripts.
 * The dialogue uses the MVC design pattern.
 ******************************************************************** */
package sessionExporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import org.apache.log4j.Logger;

public class AnonScriptController implements ActionListener, ItemListener, DocumentListener
{
   protected static Logger logger = Logger.getLogger(AnonScriptController.class);
   
   AnonScriptView  asv;
   AnonScriptModel asm;
   boolean         ignoreTextAreaEvent;
   
   public static final int NOT_SAVED    = 1;
   public static final int NOT_APPROVED = 2;
   
   public AnonScriptController(AnonScriptView asv)
   {
      this.asv = asv;
      asm = asv.getModel();
      ignoreTextAreaEvent = false;
   }
   

   @Override
   public void actionPerformed(ActionEvent ae)
   {
      Object source = ae.getSource();

      if (source == asv.getApproveJButton())
      {
         asm.setApproved(true);
         asv.firePropertyChange("Approved", 0, 1);
         asv.setButtonStates();
      }
      
      
      if (source == asv.getCancelJButton())
      {
         boolean proceedToCancel = true;
         if (!asm.isApproved()) proceedToCancel = asv.showCancelWarning(NOT_APPROVED);
         else if (!asm.isSaved()) proceedToCancel = asv.showCancelWarning(NOT_SAVED);
         if (proceedToCancel) asv.dispose();
      }
      
      
      if (source == asv.getLoadJButton())
      {
         String newScript = asv.tryScriptLoad();
         if (newScript != null)
         {
            asm.setCurrentScript(newScript);
            asv.setText();
         }
         asv.setButtonStates();
      }
      
      
      if (source == asv.getSaveJButton())
      {
         if (asv.tryScriptSave())
         {
            asm.setSaved(true);
            asm.setCurrentScript(asm.getUnsavedScript());
            asm.setUnsavedScript(null);
            asv.setButtonStates();
         }
      }
 
      
      if (source == asv.getSaveAsJButton())
      {
         File newFile = asv.tryScriptSaveAs();
         if (newFile != null)
         {
            asm.setCurrentFile(newFile);
            asm.setSaved(true);
            asm.setCurrentScript(asm.getUnsavedScript());
            asm.setUnsavedScript(null);
            asv.setButtonStates();
         }
      }
   }

   @Override
   public void itemStateChanged(ItemEvent ie)
   {
      assert (ie.getSource() == asv.getScriptJComboBox());
      
      // Note: There are two passes through this method per click: once to tell you
      // that an item has been deselected and once to say that a new item has been chosen.
      // Do something only on the selection pass.
      if (ie.getStateChange() == ItemEvent.SELECTED) setScriptText(); 
   }
   
   public void setScriptText()
   {
      asm.setCurrentName((String) asv.getScriptJComboBox().getSelectedItem());

      if (asv.getScriptJComboBox().getSelectedItem().equals(AnonScriptModel.CUSTOM))
      {
         // Don't do anything if this event was generated by a change
         // to the text area.
         if (ignoreTextAreaEvent)
         {
            ignoreTextAreaEvent = false;
            return;
         }
      }
      asm.setCurrentScript(asm.getDefaultScript(asm.getCurrentName()));
      asv.setText();
      asm.setUnsavedScript(null);
      asm.setSaved(true);
      asm.setApproved(false);
      asv.setButtonStates();
   }

   @Override
   public void insertUpdate(DocumentEvent de)
   {
      assert (de.getDocument() == asv.getScriptJTextArea().getDocument());
      scriptJTextAreaChanged();
   }

   @Override
   public void removeUpdate(DocumentEvent de)
   {
      assert (de.getDocument() == asv.getScriptJTextArea().getDocument());
      scriptJTextAreaChanged();
   }

   @Override
   public void changedUpdate(DocumentEvent de)
   {
      assert (de.getDocument() == asv.getScriptJTextArea().getDocument());
      scriptJTextAreaChanged();
   }
   
   private void scriptJTextAreaChanged()
   {
      // We only want to act on genuine edits of the script, not
      // the text change that comes about when one of the default scripts
      // is loaded up.
      boolean matchesDefault = false;
      for (String name : asm.getScriptMap().keySet())
      {
         if (asv.getScriptJTextArea().getText().equals(asm.getDefaultScript(name)))
            matchesDefault = true;
      }
      if (!matchesDefault)
      {
         // If a change to the text area has been detected, then by definition,
         // this must be a custom script, so we need to change the setting on
         // the combo box. However, in order to avoid getting into a loop with
         // the text area firing a message to the combo box and the combo box
         // firing a message to the text area because it has been changed, set
         // the ignoreTextAreaEvent variable.
         ignoreTextAreaEvent = true;
         asv.getScriptJComboBox().setSelectedItem(AnonScriptModel.CUSTOM);
         asm.setUnsavedScript(asv.getScriptJTextArea().getText());
         asm.setSaved(false);
         asv.firePropertyChange("Approved", (asm.isApproved() ? 1 : 0), 0);
         asm.setApproved(false);
         asv.setButtonStates();
      }
   }
   
}
