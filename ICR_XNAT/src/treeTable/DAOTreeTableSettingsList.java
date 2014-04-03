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
* Java class: DAOTreeTableSettingsList.java
* First created on Jan 27, 2011 at 11:59 PM
* 
* Mainly I/O of the DAOTreeTableSettings data.
*********************************************************************/

package treeTable;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import exceptions.XMLException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import treeTable.DAOTreeTableSettings.ColumnSettings;
import treeTable.DAOTreeTableSettings.SettingsForSubtype;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATDAO;
import xnatRestToolkit.XNATNamespaceContext;

public class DAOTreeTableSettingsList extends ArrayList<DAOTreeTableSettings>
{
   static    Logger logger = Logger.getLogger(DAOTreeTableSettingsList.class);
   protected File   tableSettingsFile;
   protected int    preferredSettings = -1;
   protected int    currentSettings   = -1;


/**
 * Create a new list of tree table settings.
 */
   public DAOTreeTableSettingsList()
   {
      String homeDir    = System.getProperty("user.home");
      String fileSep    = System.getProperty("file.separator");
      String filename   = homeDir + fileSep + ".XNAT_DAO"
                           + fileSep + "config"
                           + fileSep + "XNAT_DAO_treetable_settings.xml";
      tableSettingsFile = new File(filename);

      // Sets display parameters for tree table.
      readDefaultSettings();
      readSettingsFile();
      currentSettings = preferredSettings;
   }


   private void readSettingsFile()
   {
      if (tableSettingsFile.exists())
      {
         try
         {
            BufferedInputStream bis
                = new BufferedInputStream(new FileInputStream(tableSettingsFile));

            readSettingsFromStream(bis);
         }
         catch (Exception ex)
         {
            // If we can't read the user's file, simply log the error here and
            // rewrite a valid file. This is potentially a problem, because if
            // a single part of the file gets corrupted somehow, the whole thing
            // will be lost. To be reconsidered (28.1.11)
            logger.error("Can't read user's settings file.\n" + ex.getMessage());
         }
      }
      else
      {
         try
         {
            saveSettings();
         }
         catch (IOException exIO)
         {
            // Fail silently. saveSettings will already have reported the failure.
         }
      }
   }


   
   protected void readDefaultSettings()
   {
      InputStream resourceIs 
         = XNATDAO.class.getResourceAsStream("projectResources/defaultTableSettings.xml");

      if (resourceIs == null)
      {
         logger.error("Couldn't read the table settings XML resource.\n"
                    + "This shouldn't happen as it is supposed to be packaged with the application jar!");
         throw new RuntimeException("Couldn't read the default table settings.");
      }

      try
      {
         readSettingsFromStream(resourceIs);
      }

      catch (XMLException exXML)
      {
         // This really shouldn't happen, as the default resource should be valid.
         logger.error("Default table settings file leads to an XML error.");         
         throw new RuntimeException("Default table settings file leads to an XML error.");
      }
   }
   
   
   private void readSettingsFromStream(InputStream is) throws XMLException
   {     
      try
      {
         XMLUtilities xmlUtils       = new XMLUtilities();
         XNATNamespaceContext XNATns = new XNATNamespaceContext();
         Document DOMDoc             = XMLUtilities.getDOMDocument(is);

         // By contrast with the similar function in XNATProfileList, we
         // cannot here use simple calls to XMLUtilities.getElementText.
         // That would return a list of all the times a particular element
         // appeared, but we would have no means of working out which
         // column any given entry belonged to. Instead, do the analysis
         // in the direct, if rather long-winded way, knowing the order
         // with which the file is written.
         NodeList settingsNodes = (NodeList) XMLUtilities.getElement(DOMDoc, XNATns, "TreeTableSettings");
         for (int i=0; i<settingsNodes.getLength(); i++)
         {
            Node         settingsNode = settingsNodes.item(i);
            NamedNodeMap attributes   = settingsNode.getAttributes();
            if (!attributes.item(0).getNodeName().equals("name"))
               throw new XMLException(XMLException.PARSE);

            String               name = attributes.item(0).getNodeValue();
            DAOTreeTableSettings tts  = new DAOTreeTableSettings(name);
            add(tts);
            

              NodeList settingsForSubtypeNodes
                  = XMLUtilities.getElementsRelative(settingsNode, XNATns, "SettingsForSubtype");
              
            // There are several ways of doing this. I could also have used
            // the XPath expression
            // TreeTableSettings[@name=\"" + name + "\"]" + "/SettingsForSubtype"

            // Cater for case of empty <TreeTableSettings name="Defaults"> element
            if (settingsForSubtypeNodes != null)
            {
               for (int j=0; j<settingsForSubtypeNodes.getLength(); j++)
               {
                  Node         settingsForSubtypeNode = settingsForSubtypeNodes.item(j);
                  NamedNodeMap attributesForSubtype   = settingsForSubtypeNode.getAttributes();
                  if (!attributesForSubtype.item(0).getNodeName().equals("DAO_subtype"))
                     throw new XMLException(XMLException.PARSE);

                  // By the time we get to here, each <SettingsForSubtype> element
                  // does have subelements specified in an easy-to-analyse way.
                  String subtype = attributesForSubtype.item(0).getNodeValue();

                  String xpathQuery = "//TreeTableSettings[@name=\"" + name + "\"]"
                              + "/SettingsForSubtype[@DAO_subtype=\"" + subtype + "\"]"
                              + "/leafDisplayAlias";
                  String leafDisplayAlias = (XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery))[0];

                  String xpathQueryBase = "//TreeTableSettings[@name=\"" + name + "\"]"
                              + "/SettingsForSubtype[@DAO_subtype=\"" + subtype + "\"]"
                              + "/Column/";

                  xpathQuery = xpathQueryBase + "JTableIndex";
                  String[] jTI = XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery);

                  xpathQuery = xpathQueryBase + "Identifier";
                  String[] identifiers = XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery);

                  xpathQuery  = xpathQueryBase + "ModelIndex";
                  String[] mI = XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery);

                  xpathQuery = xpathQueryBase + "Width";
                  String[] w = XMLUtilities.getXPathResult(DOMDoc, XNATns, xpathQuery);


                  int jl = jTI.length;
                  if ((jl != mI.length) || (jl != w.length)) throw new
                     XMLException(XMLException.PARSE,
                                  "Error in specification of <Column> data");

                  int[] jTableIndices = new int[jl];
                  int[] modelIndices  = new int[jl];
                  int[] widths        = new int[jl];

                  for (int k=0; k<jl; k++)
                  {
                     jTableIndices[k] = Integer.parseInt(jTI[k]);
                     modelIndices[k]  = Integer.parseInt(mI[k]);
                     widths[k]        = Integer.parseInt(w[k]);
                  }

                  tts.addSettingsForDataSubtype(subtype,
                                             identifiers,
                                             jTableIndices,
                                             modelIndices,
                                             widths,
                                             leafDisplayAlias);

               }
            }
         }
         
         String[] preferred = XMLUtilities.getElementText(DOMDoc, XNATns, "PreferredSettings");
         int psl = (preferred == null) ? 0 : preferred.length;
         
         if (psl == 0)
         {
            logger.warn("No <PreferredSettings> found, so selecting first entry"
                      + " in settings list.");
            preferredSettings = 0;
         }
         if (psl > 1)
         {
            logger.warn("More than one <PreferredSettings> found "
                      + "- using first entry");
            psl = 1;
         }
         
         if (psl == 1)
         {
            int found = -1;
            for (int i=0; i<this.size(); i++)
               // Note: If there are two profiles with the same name, this code
               // always selects the later ocurring. This situation can't arise
               // if the table settings file is generated by the application.
               // However, if the file has been edited manually it is a possibility.
               if (get(i).getName().equals(preferred[0]))
               {
                  preferredSettings = i;
                  found = i;
               }
            
            if (found == -1)
            {
               logger.warn("Invalid entry for <PreferredSettings> in "
                             + " - using first entry in list." );
               preferredSettings = 0;
            }             
               
         }
      }
      
      catch (XMLException exXML)
      {
         logger.error("Invalid table settings input.");
         throw exXML;
      }

   }



   public void saveSettings() throws IOException
   {
      try
      {
         tableSettingsFile.getParentFile().mkdirs();
         tableSettingsFile.delete();
         
         if (size() == 1) return; // Only defaults exist. No user data to write.
         
         tableSettingsFile.createNewFile();
         PrettyPrinterXmlWriter pp = new PrettyPrinterXmlWriter(
                                        new SimpleXmlWriter(
                                           new FileWriter(tableSettingsFile)));
         pp.writeXmlVersion();
         pp.writeEntity("XNAT_DAO_TreeTableSettingsList");

         for (int i=1; i<size(); i++)
         {
            DAOTreeTableSettings itts = get(i);
            pp.writeEntity("TreeTableSettings")
              .writeAttribute("name", itts.name);

            Set<String> keys = itts.keySet();
            for (String ikey : keys)
            {
               SettingsForSubtype settingsForSubtype = itts.get(ikey);
               pp.writeEntity("SettingsForSubtype")
                 .writeAttribute("DAO_subtype", ikey)
               
                    .writeEntity("leafDisplayAlias")
                    .writeText(settingsForSubtype.leafDisplayAlias)
                    .endEntity();

               for (ColumnSettings ics : settingsForSubtype)
               {
                  pp.writeEntity("Column")
                          
                       .writeEntity("JTableIndex")
                       .writeText(ics.jTableIndex)
                       .endEntity()
                          
                       .writeEntity("Identifier")
                       .writeText(ics.identifier)
                       .endEntity()
                          
                       .writeEntity("ModelIndex")
                       .writeText(ics.modelIndex)
                       .endEntity()
                          
                       .writeEntity("Width")
                       .writeText(ics.width)
                       .endEntity()
                          
                     .endEntity();
               }
               
               pp.endEntity();
            }

            pp.endEntity();
         }
            pp.writeEntity("PreferredSettings")
              .writeText((get(currentSettings)).getName())
              .endEntity();
            
         pp.endEntity();
         pp.close();
      }
      catch (IOException exIO)
      {
         logger.error(exIO.getMessage());
         throw exIO;
      }
   }
   
   
   
   public DAOTreeTableSettings getCurrentTableSettings()
   {
      return get(currentSettings);
   }
   
   
   public DAOTreeTableSettings getDefaultTableSettings()
   {
      return get(0); 
   }
   
   
   public int getCurrentSettingsNumber()
   {
      return currentSettings;
   }
   
   
   public String getCurrentSettingsName()
   {
      return get(currentSettings).getName();
   }
   
   
   public void setCurrentSettings(int settingsIndex) throws Exception
   {
      if (currentSettings == -1) return;
      if ((settingsIndex >= 0) && (settingsIndex < size()))
         currentSettings = settingsIndex;
      else throw new Exception("Illegal value for XNAT settings");
   }
   
   
   public void setCurrentSettings(DAOTreeTableSettings tts) throws Exception
   {
      int ind = indexOf(tts);
      if (ind != -1) currentSettings = ind;
      else throw new Exception("Illegal value for table settings");
   }

}
