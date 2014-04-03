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
* Java class: XNATProfileEditor.java
* First created on Apr 29, 2010, 10:55:36 AM
* 
* GUI for input and edit of profile information (profile name, server
* URL, userid, password and chosen projects)
*********************************************************************/

package xnatDAO;

import generalUtilities.SimpleColourTable;
import imageUtilities.DownloadIcon;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import xnatRestToolkit.XNATServerConnection;
import xnatRestToolkit.XNATProfileList;


public class XNATProfileEditor extends javax.swing.JDialog
{
   XNATProfile xnprf;
   XNATProfile xnprfOrig;

   /** Creates new form XNATProfileEditor */
   public XNATProfileEditor(Dialog parent, String title, XNATProfile xnprfOrig)
   {
      super(parent, title, true);
      initComponents();
      addListeners();
      this.xnprfOrig = xnprfOrig;
      
      xnprf = new XNATProfile();
      
      if (xnprfOrig != null)
      {
         if (xnprfOrig.getProfileName() != null)
         {
            xnprf.setProfileName(xnprfOrig.getProfileName());
            profileNameJTextField.setText(xnprf.getProfileName());
         }

         if (xnprfOrig.getServerURL() != null)
         {
            xnprf.setServerURL(xnprfOrig.getServerURL());
            serverJTextField.setText(xnprf.getServerURL().toString());
         }

         if (xnprfOrig.getUserid() != null)
         {
            xnprf.setUserid(xnprfOrig.getUserid());
            useridJTextField.setText(xnprf.getUserid());
         }
      }
      
      // We can't populate the project box until we have a password and database
      // connection.
      passwordInfoJLabel.setForeground(SimpleColourTable.getColour("ICR red"));
      projectJComboBox.setEnabled(false);
      checkAccessJLabel.setVisible(false);
      submitJButton.setEnabled(false);
      checkAccessJButton.setEnabled(false);  
   }
    
    
   public XNATProfileEditor(Dialog parent, XNATProfile xnprfOrig)
   {
      this(parent, "Enter details of XNAT profile", xnprfOrig);
   }
   
   
   /**
    * Add the listeners that allow actions to be implemented when the
    * users click on relevant buttons.
    */
   private void addListeners()
   {
      submitJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
            setProjectList();
				dispose();
			}	  
		});


      checkAccessJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
            checkProjectAccess();
			}
		});


      cancelJButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
            xnprf = null;
				dispose();
			}	  
		});
      
      
      
      
      ActionListener submitAL = new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               if (submitJButton.isEnabled()) dispose();
            }
         };
      
      KeyListener enableSubmitButtonKL = new KeyAdapter()
         {
            @Override
            public void keyTyped(KeyEvent evt)
            {
               if (checkAccessDataPresent()) checkAccessJButton.setEnabled(true);
               else checkAccessJButton.setEnabled(false);
            }
         };
         
      KeyListener enableCheckAccessButtonKL = new KeyAdapter()
         {
            @Override
            public void keyTyped(KeyEvent evt)
            {
               if (checkAccessDataPresent()) checkAccessJButton.setEnabled(true);
               else submitJButton.setEnabled(false);
            }
         };   
      
      profileNameJTextField.addActionListener(submitAL);       
      profileNameJTextField.addKeyListener(enableSubmitButtonKL);
      
      serverJTextField.addActionListener(submitAL);
      serverJTextField.addKeyListener(enableSubmitButtonKL);
      
      useridJTextField.addActionListener(submitAL);
      useridJTextField.addKeyListener(enableSubmitButtonKL);
      
      jPasswordField.addActionListener(submitAL);
      jPasswordField.addKeyListener(enableSubmitButtonKL);
   }
   
   
   private boolean checkAccessDataPresent()
   {
      try
      {
         String profileName = profileNameJTextField.getText();
         String serverName  = serverJTextField.getText();
         String userid      = useridJTextField.getText();
         String password    = new String(jPasswordField.getPassword());
         
         if ((profileName.length() == 0) ||
             (serverName.length()  == 0) ||
             (userid.length()      == 0) ||
             (password.length()    == 0)) return false; 
      }
      
      // I don't quite understand the JTextField (JTextComponent) documentation.
      // A NullPointerException occurs if "the underlying document is null". Does
      // this mean if there is no text in the field, or something else?
      catch (NullPointerException exNP)
      {
         return false;
      }
      
      return true;
   }
   
   
   protected void checkProjectAccess()
   {
      try
       {
         String profileName         = profileNameJTextField.getText();
         URL    serverURL           = new URL(serverJTextField.getText());
         String userid              = useridJTextField.getText();
         String password            = new String(jPasswordField.getPassword());
         ArrayList<String> nullList = new ArrayList<String>();
         
         nullList.add(PermissionsWorker.NOT_CONNECTED);
         
         if (xnprf == null)
            xnprf = new XNATProfile(profileName,
                                    serverURL,
                                    userid,
                                    password,
                                    nullList,
                                    0);
         else
         {
            xnprf.setProfileName(profileName);
            xnprf.setServerURL(serverURL);
            xnprf.setUserid(userid);
            xnprf.setPassword(password);
         }
         xnprf.connect();
       }
       catch (Exception exIgnore){}

       if (xnprf.isConnected())
       {
          xnprf.updateAuthenticationTime();
          populateProjectJComboBoxInvoke();
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

   
   
   /**
    * Get information on all the projects that can be accessed by this user at
    * the current server address.
    */
   protected void populateProjectJComboBoxInvoke()
   {
      profileNameJTextField.setEnabled(false);
      serverJTextField.setEnabled(false);
      useridJTextField.setEnabled(false);
      projectJComboBox.setEnabled(false);
      checkAccessJButton.setEnabled(false);
      submitJButton.setEnabled(false);
      
      checkAccessJLabel.setVisible(true);
      DownloadIcon getPermsIcon = new DownloadIcon(checkAccessJLabel);
      checkAccessJLabel.setIcon(getPermsIcon);
      getPermsIcon.start();

      (new PermissionsWorker(this)).execute();
   }



   /**
    * Add the names of all projects for which the user has access rights
    * to the drop list.
    * @param accessible and ArrayList of Strings containing the project list.
    */
   protected void populateProjectJComboBox(ArrayList<String> accessible)
   {
      profileNameJTextField.setEnabled(true);
      serverJTextField.setEnabled(true);
      useridJTextField.setEnabled(true);
      projectJComboBox.setEnabled(true);
      checkAccessJButton.setEnabled(true);
      submitJButton.setEnabled(true);
      
      DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
      projectJComboBox.setModel(dcbm);

      for (String projectName : accessible) dcbm.addElement(projectName);
      
      if (accessible.get(0).equals(PermissionsWorker.NO_PROJECTS) ||
          accessible.get(0).equals(PermissionsWorker.NOT_CONNECTED))
      {
         projectJComboBox.setForeground(SimpleColourTable.getColour("ICR red"));
         submitJButton.setEnabled(false);
      }
      else
         projectJComboBox.setSelectedItem(PermissionsWorker.ALL_PROJECTS);

      checkAccessJLabel.setVisible(false);
      ((DownloadIcon) checkAccessJLabel.getIcon()).stop();
      
      
      if (submitJButton.isEnabled())
      {
         xnprf.updateAuthenticationTime();
         xnprf.connect();
         passwordInfoJLabel.setVisible(false);
      }
   }
   



   protected void setProjectList()
   {
      String            project     = (String) projectJComboBox.getSelectedItem();      
      ArrayList<String> projectList = new ArrayList<String>();
     
      if (!(project.equals(PermissionsWorker.NO_PROJECTS)  ||
            project.equals(PermissionsWorker.ALL_PROJECTS) ||
            project.equals(PermissionsWorker.NOT_CONNECTED)))
      {   
         projectList.add(project);
      }
      
      if (project.equals(PermissionsWorker.ALL_PROJECTS))
      {
         for (int i=0; i<projectJComboBox.getItemCount(); i++)
         {
            String item = (String) projectJComboBox.getItemAt(i);
            if (!item.equals(PermissionsWorker.ALL_PROJECTS)) projectList.add(item);
         }
      }
      
      xnprf.setProjectList(projectList);      
   }
    
    
   public void setServerName(String serverName)
   {
      serverJTextField.setText(serverName);
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
      serverJTextField = new javax.swing.JTextField();
      passwordJLabel = new javax.swing.JLabel();
      useridJTextField = new javax.swing.JTextField();
      profileNameJLabel = new javax.swing.JLabel();
      jPasswordField = new javax.swing.JPasswordField();
      submitJButton = new javax.swing.JButton();
      cancelJButton = new javax.swing.JButton();
      checkAccessJButton = new javax.swing.JButton();
      projectJLabel = new javax.swing.JLabel();
      projectJComboBox = new javax.swing.JComboBox();
      checkAccessJLabel = new javax.swing.JLabel();
      serverJLabel = new javax.swing.JLabel();
      profileNameJTextField = new javax.swing.JTextField();
      passwordInfoJLabel = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      messageJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      messageJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xnatDAO/projectResources/powered_by_XNAT.jpg"))); // NOI18N
      messageJLabel.setText("     Please provide the required information for your profile.");

      useridJLabel.setText("XNAT userid");

      passwordJLabel.setText("XNAT password");

      profileNameJLabel.setText("Profile name");

      submitJButton.setText("Submit");

      cancelJButton.setText("Cancel");

      checkAccessJButton.setText("Check project access");

      projectJLabel.setText("XNAT project");

      projectJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Choose project name" }));

      checkAccessJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
      checkAccessJLabel.setText("Checking project access permissions");

      serverJLabel.setText("XNAT server");

      passwordInfoJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      passwordInfoJLabel.setText("Valid password and connection needed to verify project access");

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                  .add(layout.createSequentialGroup()
                     .add(37, 37, 37)
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(profileNameJLabel)
                        .add(layout.createSequentialGroup()
                           .add(1, 1, 1)
                           .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(serverJLabel)
                              .add(useridJLabel)))
                        .add(passwordJLabel))
                     .add(26, 26, 26)
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jPasswordField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, useridJTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, serverJTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, profileNameJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                           .add(cancelJButton)
                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                           .add(checkAccessJButton)
                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                           .add(submitJButton))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, passwordInfoJLabel)))
                  .add(layout.createSequentialGroup()
                     .addContainerGap()
                     .add(messageJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
               .add(layout.createSequentialGroup()
                  .add(38, 38, 38)
                  .add(projectJLabel)
                  .add(41, 41, 41)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(checkAccessJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(projectJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 351, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
            .add(21, 21, 21))
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
            .add(26, 26, 26)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(serverJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(serverJLabel))
            .add(24, 24, 24)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(useridJLabel)
               .add(useridJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(26, 26, 26)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(passwordJLabel))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(passwordInfoJLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 27, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(projectJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(projectJLabel))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(checkAccessJLabel)
            .add(42, 42, 42)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cancelJButton)
               .add(checkAccessJButton)
               .add(submitJButton))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
         @Override
            public void run() {
                XNATProfileEditor dialog = new XNATProfileEditor(new javax.swing.JDialog(),
                                     "Enter required profile information", null);
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
    
    
    public XNATProfile getProfile()
    {
       return xnprf;
    }




   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelJButton;
   private javax.swing.JButton checkAccessJButton;
   private javax.swing.JLabel checkAccessJLabel;
   private javax.swing.JPasswordField jPasswordField;
   private javax.swing.JLabel messageJLabel;
   private javax.swing.JLabel passwordInfoJLabel;
   private javax.swing.JLabel passwordJLabel;
   private javax.swing.JLabel profileNameJLabel;
   private javax.swing.JTextField profileNameJTextField;
   private javax.swing.JComboBox projectJComboBox;
   private javax.swing.JLabel projectJLabel;
   private javax.swing.JLabel serverJLabel;
   private javax.swing.JTextField serverJTextField;
   private javax.swing.JButton submitJButton;
   private javax.swing.JLabel useridJLabel;
   private javax.swing.JTextField useridJTextField;
   // End of variables declaration//GEN-END:variables

}
