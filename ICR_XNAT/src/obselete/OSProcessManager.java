/*******************************************
 * OSProcessManager
 *
 * @author        Simon J. Doran
 * Creation date: Mar 9, 2009 at 11:23:14 AM
 *
 * Filename:      OSProcessManager.java
 * Package:       xnat_experiments
 *
 * This is a wrapper around the Java Process
 * and ProcessBuilder classes.
 ********************************************/

package obselete;

import exceptions.OSProcessManagerException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;


public class OSProcessManager
{
   static  Logger    logger = Logger.getLogger(OSProcessManager.class);
   private Process   pid;
   private byte[]    processOutput;
   private boolean   isFinished;
   private boolean   startedSuccessfully;
   private boolean   redirectError;

   public OSProcessManager()
   {
      isFinished           = false;
      startedSuccessfully  = false;
      redirectError        = true;
      processOutput        = null;
   }


   public Process initiateProcess(String[] args) throws OSProcessManagerException
   {
      ProcessBuilder      pb  = new ProcessBuilder(args);
      Map<String, String> env = pb.environment();

      pb.redirectErrorStream(redirectError);

      try
      {
         pid = pb.start();
         startedSuccessfully = true;
      }
      catch (NullPointerException exNP)
      {
         throw new OSProcessManagerException(OSProcessManagerException.NP);
      }

      catch (IndexOutOfBoundsException exIOOB)
      {
         throw new OSProcessManagerException(OSProcessManagerException.IOOB);
      }

      catch (SecurityException exSec)
      {
         throw new OSProcessManagerException(OSProcessManagerException.SECURITY);
      }

      catch (IOException exIO)
      {
         throw new OSProcessManagerException(OSProcessManagerException.IO);
      }

      return pid;
   }



   public Process initiateProcessAndWait(String[] args) throws OSProcessManagerException
   {
      Process p = initiateProcess(args);
      try
      {
         p.waitFor();
      }
      catch (InterruptedException ex)
      {
         throw new OSProcessManagerException(OSProcessManagerException.INTERRUPT);
      }

      return p;
   }


   public void getProcessOutputAsByteArray()
   {
      String s = null;

      try
      {
         BufferedInputStream bis = (BufferedInputStream) pid.getInputStream();
         int                 n   = bis.available();
         processOutput           = new byte[n];
         bis.read(processOutput);
      }
      catch (Exception ex)
      {
         logger.error("Exception generated when getting process output.", ex);
      }
   }



   public BufferedInputStream getProcessOutputAsStream()
   {
      BufferedInputStream bis = null;

      try
      {
         if (processOutput == null) getProcessOutputAsByteArray();
 //        bis = new BufferedInputStream( new ByteArrayInputStream(processOutput));

         // Temporary kludge to overcome a bug in the XNAT search XML output.
         String s;
         s = getProcessOutputAsString();
         bis = new BufferedInputStream( new ByteArrayInputStream(s.getBytes()));

      }
      catch (Exception ex)
      {
         logger.error("Exception generated when getting process output.", ex);
      }

      return bis;
   }


   public String getProcessOutputAsString()
   {
      String s = new String();
      try
      {
         if (processOutput == null) getProcessOutputAsByteArray();
         s = new String(processOutput);

         // Temporary kludge to overcome a bug in the XNAT search XML output.
         s = s.replaceAll("<column clickable[^>]+", "<column");
      }
      catch (Exception ex)
      {
         logger.error("Exception generated when getting process output.", ex);
      }

      return s;
   }


   public boolean successfulStart()
   {
      return startedSuccessfully;
   }



   public boolean stillRunning()
   {
      try
      {
         int retCode = pid.exitValue();
      }
      catch (IllegalThreadStateException exITS)
      {
         return false;
      }

      return true;
   }



   public void setRedirectError(boolean redirectError)
   {
      this.redirectError = redirectError;
   }

}
