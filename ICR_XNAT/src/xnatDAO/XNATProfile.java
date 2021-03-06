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

import dataRepresentations.xnatSchema.InvestigatorList;
import exceptions.XMLException;
import generalUtilities.Vector2D;
import java.awt.Dialog;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import xmlUtilities.XMLUtilities;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
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
   protected String            dicomReceiverHost;
   protected int               dicomReceiverPort;
   protected String            dicomReceiverAeTitle;

   
   public XNATProfile()
   {
      // In some cases, we need to set up a completely blank profile, and then
      // assign values to the instance variables later, using the setter methods.
      super();
   }
   
   public XNATProfile(String profileName, URL serverURL, String userid,
                      String password, ArrayList<String> projectList, long authTime,
                      String dicomHost, int dicomPort, String aetitle)
   {
      super(serverURL, userid, password);
      this.profileName            = profileName;
      this.projectList            = projectList;
      this.lastAuthenticationTime = authTime;
      this.dicomReceiverHost      = dicomHost;
      this.dicomReceiverPort      = dicomPort;
      this.dicomReceiverAeTitle   = aetitle;
   }


   public void connectWithAuthentication(Dialog parent)
   {
      
      XNATProfileAuthenticator a = new XNATProfileAuthenticator(parent,
                                        "Authentication required", this);
      a.setVisible(true);
      analytics(this);
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
   
   
   public String getDicomReceiverHost()
   {
      return dicomReceiverHost;
   }


   public int getDicomReceiverPort()
   {
      return dicomReceiverPort;
   }
   
   
   public String getDicomReceiverAeTitle()
   {
      return dicomReceiverAeTitle;
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
   
   
   public void setDicomReceiverHost(String dicomHost)
   {
      dicomReceiverHost = dicomHost;
   }
   
   
   public void setDicomReceiverPort(int dicomPort)
   {
      dicomReceiverPort = dicomPort;
   }
   
   
   public void setDicomReceiverAeTitle(String aetitle)
   {
      dicomReceiverAeTitle = aetitle;
   }
	
	
/**
 * Quick and dirty way to get some basic system stats.
 * Call this routine from method connectWithAuthentication, by adding
 * analytics(this);
 * at the end of the method.
 * @param xnprf 
 */
	private void analytics(XNATProfile xnprf)
	{
      final XNATNamespaceContext xnatNs  = new XNATNamespaceContext();
      String xpe;
      BufferedWriter out     = null;
		try
		{
         // Open a file for writing the data to.
         String         outName = "/Users/simond/temp/XNAT_analytics_" + xnprf.profileName + ".txt";
         FileWriter     fstream = new FileWriter(outName);
         out = new BufferedWriter(fstream);
         
			XNATRESTToolkit    xnrt      = new XNATRESTToolkit(xnprf);
			ArrayList<Integer> nSessions = new ArrayList<>();   
			
			// Loop over all projects.
			Vector2D<String> v2dProj = xnrt.RESTGetResultSet("/data/archive/projects?format=xml");
			for (int i=0; i<v2dProj.size(); i++)
			{
				String proj = v2dProj.atom(0, i);
            String desc = v2dProj.atom(3, i);
            int    nSessionsProject = 0;
            
            // Get the project's PI and co-investigators.
            InvestigatorList invs = new InvestigatorList(proj, xnprf);
           	
				// Record experiment insertion dates as we go along so that we can
            // report back on project activity.
            TreeSet<String> sessionInsertionDates = new TreeSet<>();
            
            // Loop over all subjects within a project.
				Vector2D<String> v2dSubj = xnrt.RESTGetResultSet("/data/archive/projects/" + proj + "/subjects?format=xml");
				for (int j=0; j<v2dSubj.size(); j++)
				{
					String subj = v2dSubj.atom(0, j);
					
					// Loop over all sessions for a given subject.
					Vector2D<String>  v2dExp   = xnrt.RESTGetResultSet("/data/archive/projects/" + proj + "/subjects/" + subj + "/experiments?format=xml");
					TreeSet<String> sessionDates = new TreeSet<>();  

					for (int k=0; k<v2dExp.size(); k++)
					{
                  sessionDates.add(v2dExp.atom(3, k));
                  sessionInsertionDates.add(v2dExp.atom(6,k));
					}
               nSessionsProject += sessionDates.size();
					nSessions.add(sessionDates.size());
					//System.out.println("Project = " + proj + "  Subject =  " + subj + "  nUniqueDates = " + sessionDates.size());
				}
            
            if (sessionInsertionDates.isEmpty()) sessionInsertionDates.add("None");

            // Get the list of project users.
            Vector2D<String> v2dUsers = xnrt.RESTGetResultSet("/data/projects/" + proj + "/users?format=xml");
				List<String> userFullNames = new ArrayList<>();
            for (int j=0; j<v2dUsers.size(); j++)
            {
               userFullNames.add(v2dUsers.atom(3, j) + " " + v2dUsers.atom(4, j));
            }
             
            String outString = proj + "#" +
                      invs.pi.firstName + " " +
                      invs.pi.lastName  + "#" +
                      v2dSubj.size()   + "#" +
                      nSessionsProject + "#" +
                      sessionInsertionDates.first() + "#" +
                      sessionInsertionDates.last()  + "#" +
                      userFullNames.size() + "#" +
                      userFullNames.toString() + "#" +
                      invs.getFullNames().toString() + "#" +
                      desc + "\n";
            
            out.write(outString);
            System.out.print(outString);
			}
         
			System.out.println("Combined results for " + v2dProj.size() + " projects and " + nSessions.size() + " subjects:");
		   
			int mx = Collections.max(nSessions);
			for (int i=0; i<=mx; i++)
			{
				System.out.println(i + ", " + Collections.frequency(nSessions, i));
			}
			
			System.out.println();
		}
		
		catch (Exception ex)
		{
			System.out.println("Caught exception " + ex.getMessage());
		}

      finally
      {
         if(out != null)
         {
            try{out.close();}
            catch (IOException exIO) {}
         }
      }
      
	}
}
