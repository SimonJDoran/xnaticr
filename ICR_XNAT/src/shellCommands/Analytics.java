/** ******************************************************************
 * Copyright (c) 2019, Institute of Cancer Research
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
 * Java class: shellCommands.Analytics
 * First created on 04-Dec-2019 at 11:14:36
 *
 * TYPE SIMPLE DESCRIPTION HERE
 ******************************************************************** */
package shellCommands;

import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;

/**
 *
 * @author simond
 */
public class Analytics
{

	public static void main(String args[])
   {
      ArrayList<String> projectList = new ArrayList<>();
		
		URL XnatServerUrl;
		try
		{
			//XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_anonymised");
         //XnatServerUrl = new URL("http://localhost:8020/XNAT_valhalla");
         XnatServerUrl = new URL("https://xnatcruk.icr.ac.uk/XNAT_CRUK_ACTIVE");
		}
		catch (MalformedURLException exMFU) {return;}
		
		String dicomReceiverHost    = "bifrost.icr.ac.uk";
      int    dicomReceiverPort    = 8104;
      String dicomReceiverAeTitle = "XNAT";
      
		XNATProfile xnprf = new XNATProfile("listSessionsProfile",
				                              XnatServerUrl,
				                              args[0],
				                              args[1],				                              
		                                    projectList,
		                                    System.currentTimeMillis(),
                                          dicomReceiverHost,
                                          dicomReceiverPort,
                                          dicomReceiverAeTitle);
      
      Analytics an = new Analytics();
      
      try
      {
         //lsp.listSessions(xnprf, patientList);
         an.printAnalytics(xnprf);
      }
      catch (XNATException|XMLException ex)
      {
         System.out.println(ex.getMessage());
      }
      System.out.println("Finished");
   }
   
   private void printAnalytics(XNATProfile xnprf)
                throws XNATException, XMLException
           
   {
      // This line is important, because it establishes the JSESSIONID.
      xnprf.connect();
      
      XNATRESTToolkit  xnrt = new XNATRESTToolkit(xnprf);
      String           restCommand;
      Vector2D<String> projData;
      
      try
      {
         restCommand  = "/data/archive/projects?format=xml";
         projData     = xnrt.RESTGetResultSet(restCommand);
      }
      catch (XNATException exXNAT)
      {
         throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of projects: "
					                   + exXNAT.getMessage());
      }
      
      
      for (int i=0; i<projData.size(); i++)
      {
         StringBuilder projStats = new StringBuilder();
         String        proj      = projData.atom(0, i);
         
         NamespaceContext XNATns = new XNATNamespaceContext();
         List<String>     subjList;
         List<String>     userList;
         Document         projectDoc;
         String[]         investigatorFirstNames;
         String[]         investigatorLastNames;
         int              nUsers;       
         try
         {
            restCommand = "/data/archive/projects/" + proj
                                 + "/subjects?format=xml";
            subjList    = xnrt.RESTGetResultSet(restCommand).getColumnAsList(2);
            
            restCommand = "/data/archive/projects/" + proj + "/users?format=xml";
            userList    = xnrt.RESTGetResultSet(restCommand).getColumnAsList(2);
            
            restCommand = "/data/archive/projects/" + proj + "?format=xml";
            projectDoc  = xnrt.RESTGetDoc(restCommand);
            String projectXml = XMLUtilities.dumpDOMDocument(projectDoc);
              
            investigatorFirstNames = XMLUtilities.getElementText(projectDoc, XNATns, "xnat:firstname");
            investigatorLastNames  = XMLUtilities.getElementText(projectDoc, XNATns, "xnat:lastname");
            
         }
         catch (XNATException exXNAT)
         {
            throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of subjects: "
                                     + exXNAT.getMessage());
         }
         catch (XMLException exXML)
         {
            throw new XMLException(XMLException.PARSE, "Problem checking for parsing project XML: "
                                     + exXML.getMessage());
         }
         
         int expCount = 0;
         for (String subj : subjList)
         {
            List<String> expList = new ArrayList<>();
            try
            {
               restCommand = "/data/archive/projects/" + proj
                                    + "/subjects/" + subj
                                    + "/experiments?format=xml";
               // Skip problematic subject containing space.
               if (!subj.contains(" "))
                  expList     = xnrt.RESTGetResultSet(restCommand).getColumnAsList(2);
            }
            catch (XNATException exXNAT)
            {
               throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of subjects: "
                                        + exXNAT.getMessage());
            }
            
            expCount += expList.size();    
         }
         
         // Process data into correct form for displaying.
         String pi = "None";
         StringBuilder coIs = new StringBuilder();
         if (investigatorFirstNames != null)
         {
            pi = investigatorFirstNames[0] + " " + investigatorLastNames[0];
            for (int j=0; j < investigatorFirstNames.length - 1; j++)
            {
               coIs.append(investigatorFirstNames[j])
                   .append(" ")
                   .append(investigatorLastNames[j]);

               if (j < investigatorFirstNames.length - 2) coIs.append(", ");
            }
         }      
         
         StringBuilder usernames = new StringBuilder();
         for (String user : userList)
         {
            usernames.append(user);
            if (!user.equals(userList.get(userList.size()-1))) usernames.append(", ");
         }
         
         String desc = projData.atom(3, i); // Project description
         String runT = projData.atom(2, i); // Running title

         // Perform a few checks to try and determine whether projects correspond
         // to clinical trials (CCR) or service evaluations (SE). Both of these
         // normally have four-digit codes for which we can check.
         String ccrOrSe = "?";
         if (proj.contains("CCR")) ccrOrSe = "CCR";
         if (proj.contains("SE"))  ccrOrSe  = "SE?";
         if (proj.matches("\\w*\\d{4}\\w*") && proj.contains("SE")) ccrOrSe = "SE";
         if (proj.matches("\\w*\\d{4}\\w*")
                 && !proj.contains("CCR")
                 && !proj.contains("SE"))
            ccrOrSe = "CCR? SE?";
         
         if (runT.contains("CCR")) ccrOrSe = "CCR";
         if (runT.contains("SE"))  ccrOrSe  = "SE?";
         if (runT.matches("\\w*\\d{4}\\w*") && proj.contains("SE")) ccrOrSe = "SE";
         if (runT.matches("\\w*\\d{4}\\w*")
                 && !proj.contains("CCR")
                 && !proj.contains("SE"))
            ccrOrSe = "CCR? SE?";
            
         if (desc.contains("CCR")) ccrOrSe = "CCR";
         if (desc.contains("SE"))  ccrOrSe  = "SE?";
         if (desc.matches("\\w*\\d{4}\\w*") && proj.contains("SE")) ccrOrSe = "SE";
         if (desc.matches("\\w*\\d{4}\\w*")
                 && !proj.contains("CCR")
                 && !proj.contains("SE"))
            ccrOrSe = "CCR? SE?";
         
         
         // Output data in tab-separated format for import into Excel..
         projStats.append(proj).append("\t")
                  .append(pi).append("\t")
                  .append(subjList.size()).append("\t")
                  .append(expCount).append("\t")
                  .append(ccrOrSe).append("\t")
                  .append(userList.size()).append("\t")
                  .append(usernames).append("\t")
                  .append(coIs).append("\t")
                  .append(desc);
         
         
         System.out.println(projStats.toString());
         
      }
	}
}
