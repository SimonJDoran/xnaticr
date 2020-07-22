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
* Java class: ListSessionsForPatient.java
* First created on Aug 26, 2016 at 12:55:58 PM
*********************************************************************/

package shellCommands;

import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;

// Started but went for an alternative strategy.

public class ListSessionsForPatient
{

	public static void main(String args[])
   {
      ArrayList<String> projectList = new ArrayList<>();
		
		projectList.add("MALIMAR_ALL");
		URL XnatServerUrl;
		try
		{
			XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_anonymised");
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
      
      ListSessionsForPatient lsp = new ListSessionsForPatient();
      
		List<String> patientList = new ArrayList<>();
      //patientList.add("RM1056"); Add patients as relevant.


      try
      {
         //lsp.listSessions(xnprf, patientList);
         lsp.listScansMALIMAR(xnprf, patientList);
      }
      catch (XNATException exXNAT)
      {
         System.out.println(exXNAT.getMessage());
      }
      System.out.println("Finished");
   }
	
	
	private void listSessions(XNATProfile xnprf, List<String> patientList)
           throws XNATException
   {
      // This line is important, because it establishes the JSESSIONID.
      // If this isn't in here, then 
      xnprf.connect();
      String xnatProject = xnprf.getProjectList().get(0);
      
      System.out.println("Retrieving and sorting subject list");

      XNATRESTToolkit  xnrt = new XNATRESTToolkit(xnprf);
      Vector2D<String> resultSubj;
      try
      {
         String restCommand = "/data/archive/projects/" + xnatProject +
                                "/subjects?format=xml";
         resultSubj         = xnrt.RESTGetResultSet(restCommand);
      }
      catch (XNATException exXNAT)
      {
         throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of subjects: "
					                   + exXNAT.getMessage());
      }
      
      for (int j=0; j<resultSubj.size(); j++)
      {
         String subj = resultSubj.atom(2, j);
         String restCommand = "/data/archive/projects/" + xnatProject +
                                "/subjects/" + subj +
                                "/experiments/?format=xml";
         Vector2D<String> resultSession;
         try
         {    
            resultSession = xnrt.RESTGetResultSet(restCommand);  
         }
         catch (XNATException exXNAT)
         {
            throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of sessions: "
                                     + exXNAT.getMessage());
         }
         
         for (int k=0; k<resultSession.size(); k++)
         {
            String session = resultSession.atom(5, k);
            //System.out.println(subj + "  " + subj + "  " + session);
            System.out.println(subj + "  " + session);
         }
         
         
      }
      System.out.println("Here");
	}
   
   
   private void listScansMALIMAR(XNATProfile xnprf, List<String> patientList)
           throws XNATException
   {
      // This line is important, because it establishes the JSESSIONID.
      // If this isn't in here, then 
      xnprf.connect();
      String project = xnprf.getProjectList().get(0);
      
      List<String> subjSessionList = new ArrayList<>();
      subjSessionList.add("RMH4820_039	782LEVI	20161117_115127_Avanto");	
      subjSessionList.add("RMH4820_039	782LEVI	20170224_090553_Avanto");
      subjSessionList.add("RMH4820_039	782LEVI	20170803_110927_Avanto");
      subjSessionList.add("RMH4820_040	886BOGO	20160830_141417_Avanto");
      subjSessionList.add("RMH4820_051	035FRSU	20170127_082953_Avanto");
      subjSessionList.add("RMH4820_057	999IGDE	20170617_145946_Avanto");
      subjSessionList.add("RMH4820_060	944HATI	20170907_151404_Avanto");
      subjSessionList.add("RMH4820_065	202BRRI	20170502_120253_Avanto");
      subjSessionList.add("RMH4820_069	212DEJO	20170720_140915_Avanto");
      subjSessionList.add("RMH4820_075	472DOPH	20161018_182510_Avanto");
      subjSessionList.add("RMH4820_077	159LASE	20171007_094211_Avanto");
      subjSessionList.add("RMH4820_085	034HAMI	20161012_125504_Avanto");
      subjSessionList.add("RMH4820_086	252ATBR	20161027_152917_Avanto");
      subjSessionList.add("RMH4820_092	479FECA	20170113_101445_Avanto");
      subjSessionList.add("RMH4820_101	033EAKE	20170714_164235_Avanto");
      subjSessionList.add("RMH4820_104	991SINO	20170926_135334_Avanto");
      subjSessionList.add("RMH4820_105	387ABSA	20171128_122021_Avanto");
      subjSessionList.add("RMH4820_135	063POKE	20161119_085919_Avanto");
      subjSessionList.add("RMH4820_135	063POKE	20170112_182650_Avanto");
      subjSessionList.add("RMH4820_135	063POKE	20170819_155747_Avanto");
      subjSessionList.add("RMH4820_135	063POKE	20171118_102150_Avanto");
      subjSessionList.add("RMH4820_143	203PARO	20161114_125605_Avanto");
      subjSessionList.add("RMH4820_143	203PARO	20170207_121939_Avanto");
      subjSessionList.add("RMH4820_143	203PARO	20170915_131724_Avanto");
      subjSessionList.add("RMH4820_155	369POHE	20170620_154353_Avanto");
      subjSessionList.add("RMH4820_156	561ABKA	20170720_152016_Avanto");
      subjSessionList.add("RMH4820_162	139ROMA	20171130_190208_Avanto");
      subjSessionList.add("RMH4820HV_001	699MOVE	20180719_112336_Avanto");
      subjSessionList.add("RMH4820HV_002	077PAHA	20180723_112801_Avanto");
      subjSessionList.add("RMH4820HV_003	381THSU	20180723_123300_Avanto");
      subjSessionList.add("RMH4820HV_004	364DOSI	20180724_142000_Avanto");
      subjSessionList.add("RMH4820HV_007	017MAKA	20180910_093713_Avanto");
      subjSessionList.add("RMH4820HV_008	490BRCH	20180925_100042_Avanto");
      subjSessionList.add("RMH4820HV_009	003SCER	20180912_135044_Avanto");
      subjSessionList.add("RMH4820HV_010	928KECA	20180917_081257_Avanto");
      subjSessionList.add("RMH4820HV_011	868ALPA	20180924_114106_Avanto");
      subjSessionList.add("RMH4820HV_013	761FEAL	20180927_120354_Avanto");
      subjSessionList.add("RMH4820HV_015	416LEKE	20181001_121751_Avanto");
      subjSessionList.add("RMH4820HV_016	140CHCH	20181009_084102_Avanto");
      

      XNATRESTToolkit  xnrt = new XNATRESTToolkit(xnprf);      
      for (String subjSession: subjSessionList)
      {
         String s[] = subjSession.split("\\s+");
         String subjTrialCode = s[0];
         String subjDicomId   = s[1];
         String session       = s[2]; 
         String restCommand = "/data/archive/projects/"    + project +
                                "/subjects/"               + subjTrialCode +
                                "/experiments/"            + session +
                                "/scans?format=xml";
         Vector2D<String> resultScans;
         try
         {    
            resultScans = xnrt.RESTGetResultSet(restCommand);  
         }
         catch (XNATException exXNAT)
         {
            throw new XNATException(XNATException.RETRIEVING_LIST, "Problem checking for list of scans: "
                                     + exXNAT.getMessage());
         }
         
         System.out.print(subjTrialCode + " " + subjTrialCode + " " + subjDicomId + " " + session + " ");
         for (int k=0; k<resultScans.size(); k++)
         {
            String scan = resultScans.atom(1, k);
            restCommand = "/data/archive/projects/"    + project +
                           "/subjects/"                + subjTrialCode +
                           "/experiments/"             + session +
                           "/scans/"                   + scan    +
                           "?format=xml";
 
            int nFrames;
            float echoTime;
            String seqName;
            String scanOptionsMr;
            
            try
            {
               NamespaceContext XNATns = new XNATNamespaceContext();  
               Document scanDoc = xnrt.RESTGetDoc(restCommand);
               s = XMLUtilities.getElementText(scanDoc, XNATns, "xnat:frames");
               nFrames = Integer.parseInt(s[0]);
               s = XMLUtilities.getElementText(scanDoc, XNATns, "xnat:te");
               echoTime = Float.parseFloat(s[0]);
               s = XMLUtilities.getElementText(scanDoc, XNATns, "xnat:sequence");
               seqName = (s == null) ? "" : s[0];
               s = XMLUtilities.getElementText(scanDoc, XNATns, "xnat:scanOptions");
               if (s != null) scanOptionsMr = s[0];
               }
               catch (XNATException | XMLException ex)
               {
                  throw new XNATException(XNATException.GET, "Problem retrieving scan details: "
                                           + ex.getMessage());
               }
            
            // Conditions for needing to transfer MALIMAR scans to shared platform
            if ((nFrames == 240) &&
                  (seqName.equals("ep_b50_900") || seqName.equals("ep_b50t") ||
                   seqName.equals("ep_b600t")   || seqName.equals("ep_b900t")))
            {
               System.out.print(scan + " ");
            }
            
            if ((nFrames == 240) && seqName.equals("*fl3d2") &&
                    ((echoTime == 4.76f) || (echoTime == 2.38f)))
            {
               System.out.print(scan + " ");
            }
         }
         System.out.println();
         
      }
      System.out.println("Here");
	}

}
