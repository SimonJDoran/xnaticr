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
 ********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: XNATProfileList.java
* First created on Apr 1, 2010 at 10:50:17 AM
* 
* Create and maintain a list of XNAT profiles. Includes methods
* for reading and writing XML profile list files.
*********************************************************************/

package xnatRestToolkit;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatDAO.DAOConstants;
import xnatDAO.PermissionsWorker;



public class XNATProfileList extends ArrayList<XNATProfile>
{
   static  Logger    logger           = Logger.getLogger(XNATProfileList.class);
   protected int     preferredProfile = -1;
   protected int     currentProfile   = -1;
   protected boolean requireAuthentication;
   protected File    profFile;

   // Used in Cipher below for encoding the connection list data.
   byte[]            secretKeyBytes      = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
        0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };


   public XNATProfileList(boolean allowAll, boolean requireAuthentication)
   {
      super();

      String homeDir      = System.getProperty("user.home");
      String fileSep      = System.getProperty("file.separator");
      String profFilename = homeDir + fileSep + ".XNAT_DAO"
                              + fileSep + "config"
                              + fileSep + "XNAT_DAO_profiles.xml";
      profFile = new File(profFilename);
      
      this.requireAuthentication = requireAuthentication;
      
      try
      {
         // Sets connections, preferredProfile, etc.
         readProfilesFile();
         
         // Set the current profile. Note that when using the uploader, we
         // require the current profile to be one that refers to only a single
         // project. For the downloader, we can search all the projects 
         // simultaneously.
         currentProfile = preferredProfile;
         if ((!allowAll) && (preferredProfile != -1))
         {
            XNATProfile xnprf = this.get(preferredProfile);
            if (xnprf.getProjectList().size() > 1)
            {
               // Search for another profile that is OK.
               currentProfile = -1;
               for (int i=0; i<this.size(); i++)
               {
                  if (this.get(i).getProjectList().size() == 1)
                  {
                     currentProfile = i;
                     break;
                  }
               }
            }
         }
      }
      catch (IOException exIO)
      {
         logger.warn("Couldn't find or read an XNAT profile list.");
         // Ignore and assume that we have no profiles.
         //A new file will be written.
      }

   }

   private void readProfilesFile() throws IOException
   {
      if (profFile.exists())
      {
         try
         {
            getProfilesFromFile(profFile);
            
            // Reconnect all profiles for which the authentication time has not
            // elapsed. Under certain circumstances, we might also want to ignore
            // authentication e.g., to speed up testing. However, this should not
            // be the norm.
            for (XNATProfile ip : this)
               if ((System.currentTimeMillis() <= 
                   (ip.getLastAuthenticationTime() + DAOConstants.AUTHENTICATION_EXPIRY))
                  || (!requireAuthentication))
                  ip.connect();               
         }
         catch (Exception ex)
         {
            throw new IOException(ex.getMessage());
         }
      }
      else
      {
         try
         {
            saveProfiles();
         }
         catch (IOException exIO)
         {
            throw exIO;
         }
      }
   }


   private void getProfilesFromFile(File profFile)
            throws FileNotFoundException, XMLException, Exception
   {
      String[] profileNames;
      String[] serverURLs;
      String[] userids;
      ArrayList<ArrayList<String>> projectLists;
      String[] dicomHosts;
      String[] dicomPorts;
      String[] aetitles;
      String[] preferred;

      XNATNamespaceContext XNATns = new XNATNamespaceContext();
      Document DOMDoc;
      try
      {
         BufferedInputStream bis
             = new BufferedInputStream(new FileInputStream(profFile));
         DOMDoc = XMLUtilities.getDOMDocument(bis);
         String s = XMLUtilities.dumpDOMDocument(DOMDoc);

         profileNames = XMLUtilities.getAttribute(  DOMDoc, XNATns, "XNATProfile", "profileName");
         serverURLs   = XMLUtilities.getElementText(DOMDoc, XNATns, "serverURL");
         dicomHosts   = XMLUtilities.getElementText(DOMDoc, XNATns, "userid");
         dicomPorts   = XMLUtilities.getElementText(DOMDoc, XNATns, "dicomReceiverHost");
         aetitles     = XMLUtilities.getElementText(DOMDoc, XNATns, "dicomReceiverPort");
         userids      = XMLUtilities.getElementText(DOMDoc, XNATns, "dicomReceiverAeTitle");   
         preferred    = XMLUtilities.getElementText(DOMDoc, XNATns, "preferredProfile");
      }
      catch (FileNotFoundException exFNF)
      {
         logger.error("This shouldn't happen!" + exFNF.getMessage());
         throw exFNF;
      }
      catch (XMLException exXML)
      {
         logger.error("File " + profFile + "contains invalid XML.");
         throw exXML;
      }


      if (profileNames == null)
      {
         logger.warn("File " + profFile + "contains no valid XNAT connections.");
         return;
      }

      int validURL = 0;
      for (int i=0; i<profileNames.length; i++)
      {
         // Get the list of projects for the ith profile.
         ArrayList<String> projectList = new ArrayList<String>();
         String xpathQuery = "//XNATProfile[@profileName=\"" + profileNames[i] + "\"]"
                              + "/projectList/project";
         String[] projects = XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery);
         for (int j=0; j<projects.length; j++) projectList.add(projects[j]);
         
         try
         {
            XNATProfile xnprf = new XNATProfile();
            xnprf.setProfileName(profileNames[i]);
            xnprf.setServerURL(new URL(serverURLs[i]));
            xnprf.setUserid(userids[i]);
            xnprf.setProjectList(projectList);
            xnprf.setDicomReceiverHost(dicomHosts[i]);
            xnprf.setDicomReceiverPort(Integer.parseInt(dicomPorts[i]));
            xnprf.setDicomReceiverAeTitle(aetitles[i]);
            add(xnprf);
            ++validURL;
         }
         catch (MalformedURLException exMF)
         {
            logger.warn("URL " + serverURLs[i] + " is malformed.");
            throw exMF;
         }
      }
      if (validURL == 0)
      {
         logger.warn("File " + profFile + "contains no valid XNAT profiles.");
         throw new Exception("No valid XNAT profiles");
      }


      XNATProfile returnValue = null;

      int ppl = (preferred == null) ? 0 : preferred.length;
      if (ppl == 0)
      {
         logger.warn("No <preferredServer> found in file " + profFile
                   + ", so selecting first entry in server list.");
         preferredProfile = 0;
      }
      if (ppl > 1)
      {
         logger.warn("More than one <preferredProfile> found in file " + profFile
                   + "- using first entry");
         ppl = 1;
      }

      if (ppl == 1)
      {
         try
         {
            int n = Integer.parseInt(preferred[0]);
            if ((n < 0) || (n > profileNames.length))
            {
               logger.warn("Invalid entry for <preferredProfile> in " + profFile
                    + " - using first entry in list." );
               preferredProfile = 0;
            }
            else preferredProfile = n;
         }
         catch (NumberFormatException exNF)
         {
            logger.warn("Invalid entry for <preferredProfile> in " + profFile
                    + " - using first entry in server list." );
            preferredProfile = 0;
         }
      }

   }


   /**
    * Write an XML file containing the profiles.
    * @throws IOException 
    */
   public void saveProfiles() throws IOException
   {
      try
      {
         profFile.getParentFile().mkdirs();
         profFile.delete();
         profFile.createNewFile();
         PrettyPrinterXmlWriter pp = new PrettyPrinterXmlWriter(new SimpleXmlWriter(new FileWriter(profFile)));
         pp.writeXmlVersion();
         pp.writeEntity("XNATProfiles");
         if (this.size() != 0)
         {
            for (XNATProfile ip : this)
            {
               pp.writeEntity("XNATProfile")
                 .writeAttribute("profileName", ip.getProfileName())
                       
                    .writeEntity("serverURL")
                    .writeText(ip.getServerURL().toString())
                    .endEntity()                       
                    // encrypt(sc.getServerURL().toString()));
                    
                    .writeEntity("userid")
                    .writeText(ip.getUserid())
                    .endEntity()
                       
                    .writeEntity("projectList");
               
               for (String is : ip.getProjectList())
                     pp.writeEntity("project")
                       .writeText(is)
                       .endEntity();
               
                  pp.endEntity()
                    .writeEntity("dicomReceiverHost")
                    .writeText(ip.getDicomReceiverHost())
                    .endEntity()
                          
                    .writeEntity("dicomReceiverPort")
                    .writeText(ip.getDicomReceiverPort())
                    .endEntity()
                          
                    .writeEntity("dicomReceiverAeTitle")
                    .writeText(ip.getDicomReceiverAeTitle())
                    .endEntity()
                          
                 .endEntity();
            }
         }
         pp.writeEntity("preferredProfile")
           .writeText(currentProfile)
           .endEntity()
         .endEntity()
         .close();
      }
      catch (IOException exIO)
  		{
         throw exIO;
      }
   }


   public String encrypt(String clearText) throws IOException
   {
      byte[] encrypted;
      try
      {
         SecretKeySpec key    = new SecretKeySpec(secretKeyBytes, "AES");
         Cipher        cipher = Cipher.getInstance("AES");
         cipher.init(Cipher.ENCRYPT_MODE, key);
         encrypted = cipher.doFinal(clearText.getBytes("UTF-8"));
      }
      catch (Exception ex)
      {
         throw new IOException("Serious error: Password encryption failed "
                                    + "- please contact Simon");
      }

      // Return the encrypted array as a hexadecimal string.
      StringBuffer sb = new StringBuffer(encrypted.length * 2);
      for (int i = 0; i < encrypted.length; i++)
      {
       if (((int) encrypted[i] & 0xff) < 0x10)
	    sb.append("0");
       sb.append(Long.toString((int) encrypted[i] & 0xff, 16));
      }

      return sb.toString();
   }


   public String decrypt(String encryptedText) throws IOException
   {
      // The encrypted text is a hexadecimal text representation of the
      // output of the cryptographic routine. So, the first task is to
      // recreate the encrypted byte array.
      int len = encryptedText.length();
      if ((len % 32) != 0)
         throw new IOException("Serious error: decryption string has incorrect length "
                                    + "- please contact Simon");
      byte[] encrypted = new byte[len/2];
      for (int i=0; i<len; i+=2)
      {
        encrypted[i/2] = (byte) ((Character.digit(encryptedText.charAt(i), 16) << 4)
                                + Character.digit(encryptedText.charAt(i+1), 16));
      }

      try
      {
         SecretKeySpec key    = new SecretKeySpec(secretKeyBytes, "AES");
         Cipher        cipher = Cipher.getInstance("AES");

         cipher.init(Cipher.DECRYPT_MODE, key);
         byte[] decrypted = cipher.doFinal(encrypted);
         return new String(decrypted, "UTF-8");
      }
      catch (Exception ex)
      {
         throw new IOException("Serious error: Password decryption failed "
                                    + "- please contact Simon");
      }
   }
   


   public XNATProfile getPreferredProfile()
   {
      return get(preferredProfile);
   }


   public void setPreferredProfile(XNATProfile xnpr) throws Exception
   {
      int ind = indexOf(xnpr);
      if (ind != -1) preferredProfile = ind;
      else throw new Exception("Illegal value for XNAT connection");
   }


   public void setPreferredProfile(int profileIndex) throws Exception
   {
      if ((profileIndex > 0) && (profileIndex < this.size()))
         preferredProfile = profileIndex;
      else throw new Exception("Illegal value for XNAT connection");
   }
   

   public XNATProfile getCurrentProfile()
   {
      if (currentProfile == -1) return null;
      return get(currentProfile);
   }


   public void setCurrentProfile(XNATProfile xnprf) throws Exception
   {
      int ind = indexOf(xnprf);
      if (ind != -1) currentProfile = ind;
      else throw new Exception("Illegal value for XNAT profile");
   }


   public void setCurrentProfile(int profileIndex) throws Exception
   {
      if (currentProfile == -1) return;
      if ((profileIndex >= 0) && (profileIndex < size()))
         currentProfile = profileIndex;
      else throw new Exception("Illegal value for XNAT profile");
   }

}
