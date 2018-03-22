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

public class AnonScriptController implements ActionListener, ItemListener, DocumentListener
{
   AnonScriptView asv;
   AnonScriptModel asm;
   
   public static final int NOT_SAVED    = 1;
   public static final int NOT_APPROVED = 1;
   
   public AnonScriptController(AnonScriptView asv)
   {
      this.asv = asv;
      asm = asv.getModel();
   }
   

   @Override
   public void actionPerformed(ActionEvent ae)
   {
      Object source = ae.getSource();

      if (source == asv.getApproveJButton())
      {
         asm.setApproved(true);
         asv.setVisible(false);
      }
      
      
      if (source == asv.getCancelJButton())
      {
         boolean proceedWithCancel = true;
         if (!asm.isSaved()) proceedWithCancel = asv.showCancelWarning(NOT_SAVED);
         asv.setVisible(proceedWithCancel);
         asm.setCancelled(proceedWithCancel);
      }
      
      
      if (source == asv.getLoadJButton())
      {
         String newScript = asv.tryScriptLoad();
         if (newScript != null)
         {
            asm.setCurrentScript(newScript);
            asv.setText(newScript);
         }
      }
      
      
      if (source == asv.getSaveJButton())
      {
         
      }
      
      
      if (source == asv.getSaveAsJButton())
      {
         
      }
   }

   @Override
   public void itemStateChanged(ItemEvent ie) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void insertUpdate(DocumentEvent de) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void removeUpdate(DocumentEvent de) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void changedUpdate(DocumentEvent de) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
}
