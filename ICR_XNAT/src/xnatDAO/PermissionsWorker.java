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
*********************************************************************/

/********************************************************************
 * @author Simon J Doran
 * Java class: PermissionsWorker.java
 * First created on Apr 29, 2010, 10:55:36 AM
 * 
 * Wrapper routine to check the file access permissions for an
 * XNAT project. This task is potentially time consuming and needs to
 * be performed asynchronously.
 ********************************************************************/


package xnatDAO;

import exceptions.FailedToConnectException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATServerConnection;

public class PermissionsWorker extends SwingWorker<ArrayList<String>, Void>
{
   static  Logger  logger = Logger.getLogger(PermissionsWorker.class);

   public static final String NOT_CONNECTED   = "Not connected to server";
   public static final String NO_PROJECTS     = "No accessible projects";
   public static final String ALL_PROJECTS    = "All accessible projects";
   public static final String NO_PROFILES     = "No profiles specified";


   private XNATProfileEditor xnpre;
   private XNATProfile       xnprf;
   private ArrayList<String> accessible;

   public PermissionsWorker(XNATProfileEditor xnpre)
   {
      this.xnpre = xnpre;
      xnprf = xnpre.getProfile();
   }
   
   @Override
   protected ArrayList<String> doInBackground()
   {
      XNATRESTToolkit  xnrt    = new XNATRESTToolkit(xnprf);
      Vector2D<String> v2dProj = null;

      accessible = new ArrayList<String>();

      if (xnprf == null )
      {
         accessible.add(NO_PROFILES);
         return accessible;
      }
            
      try
      {
         v2dProj  = xnrt.RESTGetResultSet("/data/archive/projects?format=xml");
      }
      catch (XNATException ex)
      {
         logger.warn("Unable to retrieve project list from profile \n"
                       + xnprf.profileName);
         accessible.add(NOT_CONNECTED);
         return accessible;
      }
      
      Vector<String>   projectNames = v2dProj.getColumn(0);
      
      // The list of projects that can be seen depends on a combination of the
      // default project accessibility and the specific user permissions. If
      // a project doesn't appear in projectNames, then this user definitely
      // won't be able to read data. o the next step is to check each
      // of these projects to see whether we have access. 
      
      Vector2D<String> v2dUsers     = null;
      
      for (Iterator<String> is = projectNames.iterator(); is.hasNext();)
      {
         String  defaultAccessibility = null;
         String  projectName          = is.next();
         boolean RESTGetError         = false;
         try
         {
            v2dUsers = xnrt.RESTGetResultSet("/data/archive/projects/" + projectName
                                            + "/users?format=xml");
            defaultAccessibility = xnprf.getMessage(
                     "/data/archive/projects/" + projectName + "/accessibility");            
         }
         catch (Exception ex)
         {
            logger.warn("Couldn't retrieve accessibility from project " + projectName);
            RESTGetError = true;
         }
         
         if (!RESTGetError)
         {
            if (v2dUsers.columnContains(2, xnprf.getUserid()) ||
                defaultAccessibility.equals("public"))
               accessible.add(projectName);
         }
     
      }
      
      if (accessible.size() == 0) accessible.add(NO_PROJECTS);
      if (accessible.size() >  1) accessible.add(0, ALL_PROJECTS);
      
      return accessible;
   }


   @Override
   protected void done()
   {
      xnpre.populateProjectJComboBox(accessible);
   }
}
