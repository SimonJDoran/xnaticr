/********************************************************************
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
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: AnonSendPostFetchAction.java
* First created on Feb 1, 2018 at 10:04:00 AM
* 
* Launch the anonymisation and send GUI to allow users to route the
* downloaded session to a different XNAT instance and project.
*********************************************************************/

package fileDownloads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import sessionExporter.AnonSessionInfo;
import sessionExporter.AnonymiseAndSend;
import xnatDAO.XNATProfile;

public class AnonSendPostFetchAction implements PostFetchAction
{
   static Logger logger = Logger.getLogger(AnonSendPostFetchAction.class);
   
   public final String SEP = System.getProperty("file.separator");
   
   @Override
	public void executeAction(FileListWorker caller, Map<Class, PreFetchStore> pfsMap)
          throws IOException
	{				
		caller.publishFromOutsidePackage("Starting export ...");
      
      // From the list of pre-fetch stores, extract the one containing
      // the anonymisation information.
      AnonSendPreFetchStore pfs = (AnonSendPreFetchStore) pfsMap.get(AnonSendPreFetchStore.class);
      if (pfs == null)
      {
         logger.error("No pre-fetch store information. Programming error - this shouldn't happen.");
         caller.publishFromOutsidePackage("Output failed - check system logs.");
         return;
      }     
      
      for (AnonSessionInfo asi : pfs.getAnonSessionInfo() )
      {
         String sessionDir   = caller.getCacheDirName() + "data/experiments/" + asi.getSessionId();
         String tempDasFile  = caller.getCacheDirName() + "temp" + SEP + "tempAnonScript.das";
         String dicomRemapEx = caller.getDicomRemapEx();
         
         // Edit the template anonymisation script to substitute the patient name and
         // session details and save it to a temporary file.
         String editedScript = pfs.getAnonScriptTemplate()
                                  .replaceAll(AnonymiseAndSend.PROJ_ID_TOKEN,   pfs.getDestProject())
                                  .replaceAll(AnonymiseAndSend.SUBJ_NAME_TOKEN, asi.getSubjDicomAnonName())
                                  .replaceAll(AnonymiseAndSend.SUBJ_ID_TOKEN,   asi.getSubjDicomAnonName())
                                  .replaceAll(AnonymiseAndSend.SUBJ_ID_TOKEN,   asi.getSubjDicomAnonName())
                                  .replaceAll(AnonymiseAndSend.SESS_NAME_TOKEN, asi.getDestSessionLabel());
         
         if (!writeToFile(editedScript, tempDasFile))
         {
            caller.publishFromOutsidePackage("Output failed - check system logs.");
            return;
         }
         
         if (!runDicomRemapShellCommand(caller, sessionDir, tempDasFile,
                                        dicomRemapEx, pfs.getDestProfile()))
         {
            caller.publishFromOutsidePackage("Output failed - check system logs.");
            return;
         }
      }
			
	}
   
   private boolean writeToFile(String script, String tempDasFile)
   {
      PrintWriter pw = null;
      try
      {

         Path tempPath = Paths.get(tempDasFile);
         Files.createDirectories(tempPath.getParent());
         pw = new PrintWriter(tempDasFile);
         pw.println(script);
      }
      catch (IOException exIO)
      {
         logger.error("Couldn't write temporary anonymisation script - this shouldn't happen.\n" + exIO.getMessage());       
         return false;
      }
      finally
      {
         if (pw != null) pw.close();
      }
      return true;
   }
   
   
   /**
    * Export the DICOM files by calling a command line process.
	 * This mechanism was coded but then abandoned on the basis that it is not optimal
	 * to expect DicomBrowser to be installed on every machine that runs this application.
	 * 
    * However, I then restarted the development when it become clear
    * that this was a quicker route to something workable in the 
    * short term.
    * 
    * Note that this class has been created by a FileListWorker and so long
    * tasks are already off the Event Dispatch Thread. If the method below
    * blocks or takes a long time, we can, in principle, cancel it.
    * 
    * @param sessionDir
    * @return 
    */
   private boolean runDicomRemapShellCommand(FileListWorker caller,
                                             String sessionDir, String tempDasFile,
                                             String dicomRemapEx, XNATProfile destProf)
   {
      InputStream    is = null;
      BufferedReader br = null;
		try
		{	
//			List<String> cl = new ArrayList<String>();
//			cl.add(dicomRemapEx);
//			cl.add("-d");
//			cl.add(tempDasFile);
//			cl.add("-o");
//			cl.add("dicom://" + destProf.getDicomReceiverHost()
//                 + ":" + destProf.getDicomReceiverPort()
//                 + "/" + destProf.getDicomReceiverAeTitle());
//         cl.add(sessionDir);
         String remapCommand = dicomRemapEx
                               + " -d " + tempDasFile
                               + " -o dicom://" + destProf.getDicomReceiverHost()
                               + ":" + destProf.getDicomReceiverPort()
                               + "/" + destProf.getDicomReceiverAeTitle()
                               + " " + sessionDir ;
			
			
			ProcessBuilder pb = new ProcessBuilder();
         pb.command("sh", "-c", remapCommand);
         pb.redirectErrorStream(true);
			Process        p  = pb.start();
			String         line;
         
         // Get the input stream connected to the normal output of the process
         // and display lines as they are generated, until the operation is finished.
			is = p.getInputStream();
         br = new BufferedReader(new InputStreamReader(p.getInputStream()));

         while ((line = br.readLine()) != null)
         {
            // The tiny log field at the bottom of the panel can't display
            // more than about 25 characters.
            int pos = Math.max(0, (line.length()-25));
            caller.publishFromOutsidePackage(line.substring(pos));
            logger.info(line);
         }
         
         // Opinion seems divided as to whether it is up to the programmer to
         // close these three streams, but it shouldn't do any harm!
         p.getInputStream().close();
         p.getOutputStream().close();
         p.getErrorStream().close();   
		}
		catch (IOException exIO)
		{
			logger.error("Error during anonymise-and-send process: " + exIO.getMessage());
         return false;
      }
		finally
      {
         if (br != null) try{br.close();} catch (IOException exIgnore){}
         if (is != null) try{is.close();} catch (IOException exIgnore){}
      }
      return true;
   }
   
   /**
	 * Export the DICOM files by calling a command line process.
	 * This mechanism was coded but then abandoned on the basis that it is not optimal
	 * to expect DicomBrowser to be installed on every machine that runs this application.
	 * 
    * However, I then restarted the development when it become clear
    * that this was a quicker route to something workable in the 
    * short term.
    * 
	 * @param anonScript DicomEdit-compatible anonymisation script
	 * @param sourceList list of DICOM files
	 
	private void exportViaDicomRemap()
	{
		InputStream is = null;
		try
		{
			String         homeDir = System.getProperty("user.home");
			String         sep     = System.getProperty("file.separator");
			String         dasName = homeDir + sep + ".XNAT_DAO" + sep + "temp"
			                         + sep + "anonSendSessionTemp.das";
			
         FileWriter     dasWrt  = new FileWriter(dasName);
		//	dasWrt.write(a);
			dasWrt.close();
			
			List<String>	cl      = new ArrayList<String>();
			cl.add("/Applications/DicomBrowser-1.5.2/bin/DicomRemap");
			cl.add("-d");
			cl.add(dasName);
			cl.add("-o");
			cl.add("dicom://" + destProf.getDicomReceiverHost()
                 + ":" + destProf.getDicomReceiverPort()
                 + "/" + destProf.getDicomReceiverAeTitle());
			
			
			ProcessBuilder pb = new ProcessBuilder(cl);
			Process        p  = pb.start();
			StringBuilder  sb = new StringBuilder();
         int            b;
			is = p.getInputStream();
         while ((b = is.read()) != -1) sb.append((char) b);
			elw.updateLogWindow(sb.toString());
		}
		catch (IOException exIO)
		{
			elw.updateLogWindow("Error initiating send process: " + exIO.getMessage());
      }
		finally
      {
         try {is.close();} catch (IOException exIOignore) {}
      }
      
	}
   /*
   
   
   /* Attempting to use Kevin Archie's DicomEdit library natively
      is currently on hold, either by using Kevin's code directly
      or developing my own variants.
         
		DicomRemapAndSend remapper = new DicomRemapAndSend(elw, destProf, destProj,
                                                    destSubjCodes, templateScript);
   */
   
}
