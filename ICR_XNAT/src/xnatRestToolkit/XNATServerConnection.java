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
********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: XNATServerConnection.java
* First created on March 16, 2010, 9:14 PM
* 
* Establish a connection with an XNAT database, which persists across
* multiple calls to the data access object. 
********************************************************************/

package xnatRestToolkit;

import exceptions.FailedToConnectException;
import exceptions.CodedException;
import exceptions.XMLException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.apache.log4j.Logger;
//import org.apache.commons.codec.binary.Base64;
import base64.Base64;
import java.awt.Dialog;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JDialog;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfileEditor;

public class XNATServerConnection
{
   static  Logger  logger = Logger.getLogger(XNATRESTToolkit.class);

   protected URL     serverURL;
   protected String  userid;
   protected String  password;
   protected String  JSessionID;
   protected boolean connected;
   protected String  XNATVersion;
   protected String  mostRecentErrorMessage = null;
   protected int     mostRecentErrorCode = -1;
   protected String  mostRecentOutput;
   

   /**
    * Create an uninitialised instance of a server connection. In some cases,
    * we might wish to create an object with no instance variables set and then
    * assign values later with the various setter methods.
    */
   public XNATServerConnection()
   {
   }

   /**
    * Create an initialised instance of a server connection.
    * @param serverURL
    * @param userid
    * @param password 
    */
   public XNATServerConnection(URL serverURL, String userid, String password)
   {
      this.serverURL = serverURL;
      this.userid    = userid;
      this.password  = password;

      connected      = false;
      JSessionID     = null;
   }


   /**
    * Create an initialised instance of a server connection.
    * @param serverURLAsString
    * @param userid
    * @param password
    * @throws MalformedURLException 
    */
   public XNATServerConnection(String serverURLAsString, String userid, String password)
          throws MalformedURLException
   {
      this(new URL(serverURLAsString), userid, password);
   }


   public URL getServerURL()
   {
      return serverURL;
   }


   public void setServerURL(URL serverURL)
   {
      this.serverURL = serverURL;
   }


   public String getUserid()
   {
      return userid;
   }


   public void setUserid(String userid)
   {
      this.userid = userid;
   }


   public String getPassword()
   {
      return password;
   }
   
   
   public void setPassword(String password)
   {
      this.password = password;
   }


   public String getJSessionID()
   {
      return JSessionID;
   }


   public int getMostRecentErrorCode()
   {
      return mostRecentErrorCode;
   }


   public String getMostRecentErrorMessage()
   {
      return mostRecentErrorMessage;
   }


   public String getMostRecentOutputAsString()
   {
      return mostRecentOutput;
   }
   
   
   public String getVersion()
   {
      return XNATVersion;
   }


   public boolean isConnected()
   {
      return connected;
   }

 
   /**
    * Issue a REST command and interpret the InputStream returned from XNAT as
    * a DOM document. Assumes the RESTCommand is a GET query.
    * @param RESTCommand
    * @return a Document containing XNAT's response
    */
   public Document getDOMDocument(String RESTCommand)
   {
      return getDOMDocument(RESTCommand, null, false);
   }


   /**
    * Issue a REST command and interpret the InputStream returned from XNAT as
    * a DOM document.
    * @param RESTCommand
    * @param uploadDoc Document to POST to XNAT
    * @param post set to true when there is a document to POST, false for GET
    * @return 
    */
   public Document getDOMDocument(String RESTCommand, Document uploadDoc, boolean post)
   {
      InputStream is  = null;
      Document    doc = null;
      try
      {
         if (post) is = doRESTPost(RESTCommand, uploadDoc);
         else      is = doRESTGet(RESTCommand);
      }
      catch (FailedToConnectException exFTC)
      {
         int code = exFTC.getReturnCode();
         
         connected              = false;
         mostRecentErrorCode    = code;
         mostRecentErrorMessage = exFTC.getMessage();
         
         if ((code == FailedToConnectException.MALFORMED_URL)
          || (code == FailedToConnectException.NULL_AUTH))
         {
            // These are programming errors and should never occur.
            logger.error(exFTC.getMessage());
         }
         else
         {
            // All the other errors relate to "genuine" issues with the particular server.
            logger.warn(exFTC.getMessage());
         }
         
         return null;
      }
      catch (Exception ex)
      {
         // All the other errors relate to "genuine" issues with the particular server.
         logger.warn(ex.getMessage());
         mostRecentErrorCode    = FailedToConnectException.IO;
         mostRecentErrorMessage = ex.getMessage();
         return null;
      }
      
      try
      {
         // Take a copy of the output for later analysis in the case that it turns out not
         // to be valid XML.
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         int r = is.read();
         while (r != -1)
         {
            baos.write(r);
            r = is.read();
         }
         mostRecentOutput = baos.toString("UTF-8");

         ByteArrayInputStream bais = new ByteArrayInputStream(mostRecentOutput.getBytes("UTF-8"));
         doc = (new XMLUtilities()).getDOMDocument(bais);
      }
      catch (XMLException exXML)
      {
         logger.warn("The REST command " + serverURL + RESTCommand
                 + " did not return valid XML\n"
                 + "The message returned from XNAT is: \n"
                 + mostRecentOutput);
         mostRecentErrorCode    = -2;
         mostRecentErrorMessage = mostRecentOutput;
      }
      catch (IOException exIOignore){}
      finally
      {
         try {if (is != null) is.close();} catch (IOException exIOignore) {}
      }
      return doc;
   }
   
   
   /**
    * Retrieve only the information or error message sent back by XNAT.
    * Effectively this is a getDOMDocument where no document is expected back.
    * A typical command might be "/data/archive/projects/PROJ/accessibility".
    * @param RESTCommand
    * @return 
    */
   public String getMessage(String RESTCommand)
   {
      Document doc = getDOMDocument(RESTCommand);
      return mostRecentOutput;
   }
   
   
   /**
    * Retrieve only the information or error message sent back by XNAT.
    * Effectively this is a getDOMDocument where no document is expected back.
    * Full version in response to a POST.
    * A typical command might be "/data/archive/projects/PROJ/accessibility".
    * @param RESTCommand
    * @return 
    */
   public String getMessage(String RESTCommand, Document uploadDoc, boolean post)
   {
      Document doc = getDOMDocument(RESTCommand, uploadDoc, post);
      return mostRecentOutput;
   }

  
   
   public void connect()
   {
      InputStream is  = null;
      Document    doc = null;
      try
      {
         is = doRESTPost("/data/JSESSION");
      }
      catch (XMLException exXML)
      {
         logger.error(exXML.getMessage()
            + ": This should never happen, as no XML has been passed to doRESTPost!");
         JSessionID = null;
         connected  = false;
         return;
      }        
      catch (FailedToConnectException exFC)
      {
         int code = exFC.getReturnCode();

         connected              = false;
         mostRecentErrorCode    = code;
         mostRecentErrorMessage = exFC.getMessage();

         if ((code == FailedToConnectException.MALFORMED_URL)
          || (code == FailedToConnectException.NULL_AUTH))
         {
            // These are programming errors and should never occur.
            logger.error(exFC.getMessage());
         }
         else
         {
            // All the other errors relate to "genuine" issues with the particular server.
            logger.warn(exFC.getMessage());
         }

         JSessionID = null;
         connected  = false;
         return;
      }
      catch (Exception ex)
      {
         logger.error(ex.getMessage());
         JSessionID = null;
         connected  = false;
         return;
      }

      try
      {
         StringBuilder sb = new StringBuilder();
         int           b;
         while ((b = is.read()) != -1) sb.append((char) b);
         JSessionID = sb.toString();
      }
      catch (IOException exIO)
      {
         connected              = false;
         mostRecentErrorCode    = FailedToConnectException.JSESSION;
         mostRecentErrorMessage = "The server " + serverURL + " did not return"
                                  + "the correct data for the JSESSION object "
                                  + "and gave the following message:" + exIO.getMessage();
         logger.warn(mostRecentErrorMessage);
      }
      finally
      {
         try {is.close();} catch (IOException exIOignore) {}
      }


      connected  = true;
      
      try
      {
         is = doRESTGet("/data/version");
      }
      catch (Exception ex)
      {
			// Starting from version 1.7, this call has been removed from the REST
			// API and returns a 404 Not Found http error.
			if (ex.getMessage().contains("Not Found"))
			{
				logger.warn(ex.getMessage() + "\n assumed due to this being version 1.7+");
				XNATVersion = "1.7+";
				return;
			}
			
         logger.error(ex.getMessage()
            + ": This should never happen, as we should be connected and XNAT ought to return the value!");
         XNATVersion = null;
         return;
      }


      try
      {
         StringBuilder sb = new StringBuilder();
         int           b;
         while ((b = is.read()) != -1) sb.append((char) b);

         XNATVersion = sb.toString();
			// Temporary kludge.
			if (XNATVersion.equals("Unknown version")) XNATVersion = "1.6.4";
			
      }
      catch (IOException exIO)
      {
         mostRecentErrorCode    = -1;  // For completeness - not sure what to put here.
         mostRecentErrorMessage = "The server " + serverURL + " did not return"
                                  + "the correct data for the XNAT version "
                                  + "and gave the following message:" + exIO.getMessage();
         logger.warn(mostRecentErrorMessage);
      }
      finally
      {
         try {is.close();} catch (IOException exIOignore) {}
      }
   }

   
   public void disconnect()
   {
      JSessionID = null;
      connected  = false;
   }

   public InputStream doRESTDelete(String RESTCommand)
          throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "DELETE", null, null, null);
   }


   public InputStream doRESTGet(String RESTCommand)
          throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "GET", null, null, null);
   }


   public InputStream doRESTPost(String RESTCommand)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "POST", null, null, null);
   }


   public InputStream doRESTPost(String RESTCommand, Document doc)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "POST", doc, null, null);
   }


   public InputStream doRESTPost(String RESTCommand, File file)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "POST", null, file, null);
   }


   public InputStream doRESTPost(String RESTCommand, InputStream is)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "POST", null, null, is);
   }


   public InputStream doRESTPut(String RESTCommand, Document doc)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "PUT", doc, null, null);
   }


   public InputStream doRESTPut(String RESTCommand)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "PUT", null, null, null);
   }


   public InputStream doRESTPut(String RESTCommand, File file)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "PUT", null, file, null);
   }


   public InputStream doRESTPut(String RESTCommand, InputStream is)
           throws FailedToConnectException, XMLException, Exception
   {
      return doRESTCommand(RESTCommand, "PUT", null, null, is);
   }


   /**
    * Perform a command using a REST interface.
    * This will typically be the XNAT REST API.
    * Note that if the "POST" or "PUT" methods are used, then data can be
    * uploaded. At present, the supported options are a Document (typical
    * for XNAT), an arbitrary File, or an output stream. It makes no sense
    * to try and upload two different things at once, so an Exception is
    * generated if more than one doc, file or os is non-null.
    * Normally speaking, do RESTCommand would not be called directly, but
    * rather via doRESTGet, doRESTPost or doRESTPut above.
    * @param RESTCommand a String containing the REST URI to process
    * @param RESTMethod a String with enumerated values "GET", "POST" or "PUT"
    * @param doc a Document to upload
    * @param file a File to upload
	 * @param is an InputStream to upload
    * @return an InputStream to allow the XNAT REST response to be processed 
    * @throws FailedToConnectException
    * @throws XMLException
	 * @throws java.io.IOException
    */
   public InputStream doRESTCommand(String RESTCommand, String RESTMethod,
                                    Document doc, File file, InputStream is)
          throws FailedToConnectException, XMLException, IOException
   {
      int                  responseCode    = 0;
      String               responseMessage = null;
      URL                  RESTURL         = null;
      HttpURLConnection    connection      = null;
      InputStream          bis             = null;
      String               xmlString       = null;

      StringBuilder sb = new StringBuilder(serverURL.toString());
      sb.append(RESTCommand);

      int count = ((doc == null)?0:1) + ((file == null)?0:1) + ((is == null)?0:1);
      assert ((count == 0) || (count == 1));
  

      if (doc != null)
      {
         try
         {
            xmlString = (new XMLUtilities()).dumpDOMDocument(doc);
         }
         catch (XMLException exXML)
         {
            throw exXML;
         }
      }

      try
      {
         RESTURL = new URL(sb.toString());
      }
      catch (MalformedURLException exMF)
      {
         logger.error("Malformed REST URL " + sb.toString()
            + ": This shouldn't happen! Please check contents of $HOME/.XNAT_DAO/config/XNAT_DAO_profiles.xml");
         return null;
      }

      try
      {
         connection = (HttpURLConnection) RESTURL.openConnection();

         connection.setDoOutput(true);
         connection.setDoInput(true);
         connection.setRequestMethod(RESTMethod);
			//connection.setChunkedStreamingMode(-1);
         if (JSessionID == null)
			{
				connection.setRequestProperty("Authorization", getAuthorization());
			}
         else connection.setRequestProperty("Cookie", "JSESSIONID=" + JSessionID);
         connection.setConnectTimeout(5000); // 5 seconds

         if (doc != null)
         {
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(xmlString);
            osw.close();
				
				// Calculate the content length.
				//byte[] 
				//connection.setRequestProperty("Content-Length", );
         }

         
			if ((file != null) || (is != null))
         {
            connection.setRequestProperty("Cache-Control", "no-cache");
			   connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            BufferedOutputStream bos  = new BufferedOutputStream(connection.getOutputStream());
            if (file != null)     is  = new FileInputStream(file);
            byte[]               buff = new byte[256];
            int bytesRead;

            assert (is != null);
				while (-1 != (bytesRead = is.read(buff, 0, buff.length)))
            {
               bos.write(buff, 0, bytesRead);
               bos.flush();
            }

            is.close();
            bos.flush();
            bos.close();
         }

         connection.connect();
         responseCode    = connection.getResponseCode();
         responseMessage = connection.getResponseMessage();
         
         bis = new BufferedInputStream(connection.getInputStream());
         
      }
      catch (SocketTimeoutException exST)
      {
         throw new FailedToConnectException(FailedToConnectException.SOCKET_TIMEOUT,
            "The attempted connection to URL " + serverURL + " timed out after 5 s.");
      }

      catch (IOException exIO)
      {
         throw new IOException("Unexpected response\n" + responseMessage + "\n"
                               + "from " + serverURL.toString() + ".\n"
                               + exIO.getMessage());
      }

      catch (IllegalStateException exIS)
      {
         // This probably just means that we are already connected.
         // Ignore the error and carry on with getting the connection.
         try
         {
            responseCode    = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            bis             = new BufferedInputStream(connection.getInputStream());
         }
         catch (IOException exIO)
         {
            throw new FailedToConnectException(FailedToConnectException.IO,
            "Couldn't open connection to " + serverURL.toString() + ". "
            + exIO.getMessage());
         }
      }

      catch (NullPointerException exNP)
      {
         throw new FailedToConnectException(FailedToConnectException.NULL_AUTH,
            "A null pointer was given as the authorization. This shouldn't happen!");
      }


      logger.debug("HTTP request " + RESTURL.toString() + " returned with response "
              + responseMessage + " code " + responseCode);

      return bis;
   }


   private String getAuthorization()
   {
      final StringBuilder sb = new StringBuilder(userid);
      sb.append(':');
      sb.append(password);

      final StringBuilder auth = new StringBuilder("Basic ");
      
      byte[] b = sb.toString().getBytes();
      String s1 = null;
      String s2 = null;
      try
      {
         s1 = new base64.Base64().encodeToString(b);
        // s2 = new org.apache.commons.codec.binary.Base64().encodeToString(b);
      }
      catch (Exception ex)
      {
         logger.error("This really can't happen!!");
      }
      auth.append(s1.trim());

  //    auth.append(new Base64().encodeToString(sb.toString().getBytes()).trim());

      return auth.toString();
   }

   @Override
   public String toString()
   {
      return new String("XNAT server: " + serverURL.toString()
                      + "\nUserid: "    + userid
                      + "\nPassword: <hidden>");
   }

}
