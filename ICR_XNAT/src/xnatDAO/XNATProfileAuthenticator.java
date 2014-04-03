/*******************************************************************
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
********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: XNATProfileAuthenticator.java
* First created on Apr 29, 2010, 10:55:36 AM
* 
* GUI dialogue panel to allow user to enter authentication details 
********************************************************************/

package xnatDAO;

import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;


public class XNATProfileAuthenticator extends javax.swing.JDialog
{
   XNATProfile xnprf;

   /** Creates new form XNATProfileEditor */
   public XNATProfileAuthenticator(Dialog parent, String title, XNATProfile xnprf)
   {
      super(parent, title, true);
      initComponents();
      this.xnprf = xnprf;
      
      xnprf.disconnect();
      profileNameJTextField.setEnabled(false);
      profileNameJTextField.setBackground(DAOConstants.BG_COLOUR);
      profileNameJTextField.setText(xnprf.profileName);
      
      jPasswordField.addKeyListener(new KeyAdapter()
         {
            @Override
            public void keyTyped(KeyEvent evt)
            {
               char ch = evt.getKeyChar();
               if (ch == '\n') authenticate(); 
            }
         });
   }
    
    
   public XNATProfileAuthenticator(Dialog parent, boolean modal)
   {
      super(parent, modal);
      initComponents();
   }
   
   
   private void authenticate()
   {       
      String userid    = useridJTextField.getText();
      String password  = new String(jPasswordField.getPassword());

      if ((userid == null) || (password == null) )
      {
         JOptionPane.showMessageDialog(this,
                                     "Please enter a valid userid and password.\n"
                                   + "These entries may not be null.",
                                     "Non-null userid and password required",
                                     JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      if (!(userid.equals(xnprf.getUserid())))
      {
         JOptionPane.showMessageDialog(this,
                                     "The userid and password combination\n"
                                   + "that you entered was not valid.",
                                     "Invalid userid/password combination",
                                     JOptionPane.ERROR_MESSAGE);
         return;
      }
           
      try
      {
         xnprf.setPassword(password);
         xnprf.connect();
         if (xnprf.isConnected()) xnprf.updateAuthenticationTime();
      }
      catch (Exception exIgnore){}

      if (xnprf.isConnected())
      {
         dispose();
         return;
      }

      JOptionPane.showMessageDialog(this,
                                    "Either the given set of inputs do not represent a valid\n"
                                  + "XNAT connection, or the server is currently down.\n"
                                  + "Please re-enter your details appropriately or try\n"
                                  + "again later.",
                                    "Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
   }
   
   
   
   public boolean authenticate(XNATProfile xnprf, String userid, String password)
   {
      if ((userid == null) || (password == null) ) return false;
      if (!userid.equals(xnprf.getUserid()))       return false;
      try
      {
         xnprf.setPassword(password);
         xnprf.connect();
      }
      catch (Exception exIgnore){}
      if (xnprf.isConnected()) xnprf.updateAuthenticationTime();
      return xnprf.isConnected();
   }
    
    
   

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      messageJLabel = new javax.swing.JLabel();
      useridJLabel = new javax.swing.JLabel();
      passwordJLabel = new javax.swing.JLabel();
      useridJTextField = new javax.swing.JTextField();
      profileNameJLabel = new javax.swing.JLabel();
      jPasswordField = new javax.swing.JPasswordField();
      submitJButton = new javax.swing.JButton();
      cancelJButton = new javax.swing.JButton();
      profileNameJTextField = new javax.swing.JTextField();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setPreferredSize(new java.awt.Dimension(450, 330));

      messageJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      messageJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/powered_by_XNAT.jpg"))); // NOI18N
      messageJLabel.setText("     Please provide the required authentication.");

      useridJLabel.setText("XNAT userid");

      passwordJLabel.setText("XNAT password");

      profileNameJLabel.setText("Profile name");

      jPasswordField.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jPasswordFieldActionPerformed(evt);
         }
      });

      submitJButton.setText("Submit");
      submitJButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            submitJButtonActionPerformed(evt);
         }
      });

      cancelJButton.setText("Cancel");
      cancelJButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelJButtonActionPerformed(evt);
         }
      });

      profileNameJTextField.setEditable(false);
      profileNameJTextField.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            profileNameJTextFieldActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                  .add(37, 37, 37)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(profileNameJLabel)
                     .add(passwordJLabel)
                     .add(useridJLabel))
                  .add(26, 26, 26)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, profileNameJTextField)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, useridJTextField)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)))
               .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(messageJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 425, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap(252, Short.MAX_VALUE)
            .add(cancelJButton)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(submitJButton)
            .add(33, 33, 33))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(messageJLabel)
            .add(28, 28, 28)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(profileNameJLabel)
               .add(profileNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(18, 18, 18)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(useridJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(useridJLabel))
            .add(18, 18, 18)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(passwordJLabel))
            .add(26, 26, 26)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(submitJButton)
               .add(cancelJButton))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void submitJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitJButtonActionPerformed
      authenticate();
}//GEN-LAST:event_submitJButtonActionPerformed

    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
       dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    private void jPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldActionPerformed

    }//GEN-LAST:event_jPasswordFieldActionPerformed

    private void profileNameJTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileNameJTextFieldActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_profileNameJTextFieldActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
         @Override
            public void run() {
                XNATProfileAuthenticator dialog = new XNATProfileAuthenticator(new javax.swing.JDialog(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
               @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }




   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelJButton;
   private javax.swing.JPasswordField jPasswordField;
   private javax.swing.JLabel messageJLabel;
   private javax.swing.JLabel passwordJLabel;
   private javax.swing.JLabel profileNameJLabel;
   private javax.swing.JTextField profileNameJTextField;
   private javax.swing.JButton submitJButton;
   private javax.swing.JLabel useridJLabel;
   private javax.swing.JTextField useridJTextField;
   // End of variables declaration//GEN-END:variables

}
