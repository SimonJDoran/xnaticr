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
* Java class: XNATProfile.java
* First created on Jan 12, 2011 at 11:58:00 PM
* 
* Extends XNATServerConnection to add functionality to turn a
* connection to the XNAT host into an object that has its own name,
* list of projects and authentication time.
*********************************************************************/


package xnatDAO;

import java.awt.Dialog;
import java.net.URL;
import java.util.ArrayList;
import xnatRestToolkit.XNATServerConnection;

/**
 *
 * @author simon
 */
public class XNATProfile extends XNATServerConnection
{
   protected String            profileName;
   protected ArrayList<String> projectList;
   protected long              lastAuthenticationTime;

   
   public XNATProfile()
   {
      // In some cases, we need to set up a completely blank profile, and then
      // assign values to the instance variables later, using the setter methods.
      super();
   }
   
   public XNATProfile(String profileName, URL serverURL, String userid,
                      String password, ArrayList<String> projectList, long authTime)
   {
      super(serverURL, userid, password);
      this.profileName            = profileName;
      this.projectList            = projectList;
      this.lastAuthenticationTime = authTime;
   }


   public void connectWithAuthentication(Dialog parent)
   {
      
      XNATProfileAuthenticator a = new XNATProfileAuthenticator(parent,
                                        "Authentication required", this);
      a.setVisible(true);
   }
   
   
   
   public String getProfileName()
   {
      return profileName;
   }


   public ArrayList<String> getProjectList()
   {
      return projectList;
   }
   
   
   public long getLastAuthenticationTime()
   {
      return lastAuthenticationTime;
   }
   
   
   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }


   public void setProjectList(ArrayList<String> projectList)
   {
      this.projectList = projectList;
   }
   
   
   public void updateAuthenticationTime()
   {
      lastAuthenticationTime = System.currentTimeMillis();
   }
}
