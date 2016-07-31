/********************************************************************
* Copyright (c) 2016, Institute of Cancer Research
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
* Java class: DeleteAnnotations.java
* First created on July 31, 2016 at 10:49:36 PM
* 
* Remove all uploaded AIM annotations assessors from a project
*********************************************************************/
package shellCommands;

import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATRESTToolkit;

public class DeleteAnnotations
{
   public DeleteAnnotations() {}
   
   
   public static void main(String args[])
   {
      ArrayList<String> projectList = new ArrayList<>();
		
		projectList.add("BRC_RADPRIM");
		URL XnatServerUrl;
		try
		{
			XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_ROI");
		}
		catch (MalformedURLException exMFU) {return;}
		
		XNATProfile xnprf = new XNATProfile("deleteAnnotationsProfile",
				                              XnatServerUrl,
				                              args[0],
				                              args[1],				                              
		                                    projectList,
		                                    System.currentTimeMillis());
		
      DeleteAnnotations da = new DeleteAnnotations();
      
      try
      {
         da.delete(xnprf);
      }
      catch (XNATException exXNAT)
      {
         System.out.println(exXNAT.getMessage());
      }
      System.out.println("Finished");
   }
   
   
   private void delete(XNATProfile xnprf)
           throws XNATException
   {
      String xnatProject = xnprf.getProjectList().get(0);
      String indent      = "   ";
      String cumIndent   = "";
      
      System.out.println("Retrieving and sorting subject list");

      XNATRESTToolkit  xnrt = new XNATRESTToolkit(xnprf);
      Vector2D<String> result;
      try
      {
         String restCommand = "/data/archive/projects/" + xnatProject +
                                "/subjects?format=xml";
         result             = xnrt.RESTGetResultSet(restCommand);
      }
      catch (XNATException exXNAT)
      {
         throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of subjects: "
					                   + exXNAT.getMessage());
      }
      
      List<String> subjList = new ArrayList<>(); 
      for (int j=0; j<result.size(); j++)
      {
         subjList.add(result.atom(2, j));
      }
      Collections.sort(subjList);
      
      
      for (String xnatSubjLabel : subjList)
      {
         System.out.println("Getting list of experiments for subject " + xnatSubjLabel);
         
         try
         {
            String restCommand = "/data/archive" +
                                 "/projects/" + xnatProject +
                                 "/subjects/" + xnatSubjLabel +
                                 "/experiments?format=xml";

            result             = xnrt.RESTGetResultSet(restCommand);
         }
         catch (XNATException exXNAT)
         {
            throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of experiments: "
                                     + exXNAT.getMessage());
         }
         
         cumIndent += indent;
         for (int i=0; i<result.size(); i++)
         {
            String xnatExpLabel = result.atom(5, i);
            System.out.println(cumIndent + "Getting AIM assessors for experiment " + xnatExpLabel);
            
            try
            {
               String restCommand = "/data/archive" +
                                    "/projects/" + xnatProject +
                                    "/subjects/" + xnatSubjLabel +
                                    "/experiments/" + xnatExpLabel +
                                    "/assessors?format=xml";

               result             = xnrt.RESTGetResultSet(restCommand);
            }
            catch (XNATException exXNAT)
            {
               throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of assessors: "
                                        + exXNAT.getMessage());
            }
            
            cumIndent += indent;
            for (int j=0; j<result.size(); j++)
            {
               String xnatAssLabel = result.atom(5, j);
               System.out.println(cumIndent + xnatAssLabel);
            }
         }
         
      }
      
   }
}
