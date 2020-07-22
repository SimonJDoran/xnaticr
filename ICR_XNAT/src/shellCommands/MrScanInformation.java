/** ******************************************************************
 * Copyright (c) 2020, Institute of Cancer Research
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
 * Java class: shellCommands.MrScanInformation
 * First created on 11-Feb-2020 at 14:02:00
 *
 * TYPE SIMPLE DESCRIPTION HERE
 ******************************************************************** */
package shellCommands;

import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.StatUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;

/**
 *
 * @author simond
 */
public class MrScanInformation
{
   boolean verbose = false;
   
   private class SequenceParameter
   {
      String                name;
      LinkedHashSet<Double> dataSet;
      List<Double>          dataList;
      Frequency             freq;
      String                xpeSuffix;
      
      public SequenceParameter(String name, String xpeSuffix)
      {
         this.name = name;
         
         // Maintain a set to enable easy counting of unique values
         // and a list for all values to allow the easy creation of
         // a histogram.
         dataSet  = new LinkedHashSet<>();
         dataList = new ArrayList<>();
         freq     = new Frequency();
         this.xpeSuffix = xpeSuffix;
      }
      
      public void addData(String val)
      {
         if (val == null) return;
         
         // Round double values to 3 s.f. to avoid very minor differences
         // being registered as separate numbers.
         double  d = Double.parseDouble(val);
         if (d == 0.0) return;
         
         BigDecimal bd = new BigDecimal(d);
         d = bd.round(new MathContext(3)).doubleValue();
         dataSet.add(d);
         dataList.add(d);
         freq.addValue(d);
      }
      
      public double[] getDblArr()
      {
         double[] ddata = new double[dataList.size()];
         for (int i=0; i<ddata.length; i++)
         {
            ddata[i] = dataList.get(i);
         }
         return ddata;
      }
      
      public List<Double> getSortedList()
      {
         List<Double> dl = new ArrayList<>(dataSet);
         Collections.sort(dl);
         return dl;
      }
      
      public String getStringValue(String scan, Document expDoc)
      {
         String xpe = "xnat:MRSession/xnat:scans/xnat:scan[@ID = '"
                      + scan + "']/xnat:parameters/xnat:" + xpeSuffix;
         
         // Interestingly, the number of frames (aka number of slices or
         // 3-D partitions is not a sequence parameter and needs to be
         // dealt with separately.
         if (name.equals("frames"))
            xpe = "xnat:MRSession/xnat:scans/xnat:scan[@ID = '" + scan + "']/xnat:frames";

         NamespaceContext XNATns = new XNATNamespaceContext();
         String vals[] = null;
         
         try
         {
            vals = (XMLUtilities.getXPathResult(expDoc, XNATns, xpe));
         }
         catch (XMLException exXML)
         {
            verbosePrint("Problem parsing XNAT experiment XML" 
                                   + exXML.getMessage());
         }
         
         if (vals == null) return null; else return vals[0];
      }
   }
   
   
   
	public static void main(String args[])
   {
      ArrayList<String> projectList = new ArrayList<>();
		
		URL XnatServerUrl;
		try
		{
			XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_anonymised");
         //XnatServerUrl = new URL("http://localhost:8020/XNAT_valhalla");
         //XnatServerUrl = new URL("https://xnatcruk.icr.ac.uk/XNAT_CRUK_ACTIVE");
		}
		catch (MalformedURLException exMFU) {return;}
		
		// The following variables need to be set, but are unused here, so can
      // take dummy values.
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
      
      
      MrScanInformation mrsi = new MrScanInformation();
      
      try
      {
         mrsi.printMrScanInformation(xnprf, "T2W");
         mrsi.printMrScanInformation(xnprf, "DCE_E");
         mrsi.printMrScanInformation(xnprf, "DW");
      }
      catch (XNATException|XMLException ex)
      {
         System.out.println(ex.getMessage());
      }
      System.out.println("Finished");
   }
   
   private void printMrScanInformation(XNATProfile xnprf, String contrast)
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
      
         
      
      List<String>     subjList;
      Document         projectDoc;
      String           proj = "BRC_RADPRIM";
      int              nUsers;
      List<String>     seqNames = getSequenceNames(contrast);
      try
      {
         restCommand = "/data/archive/projects/" + proj
                              + "/subjects?format=xml";
         subjList    = xnrt.RESTGetResultSet(restCommand).getColumnAsList(2);

         restCommand = "/data/archive/projects/" + proj + "?format=xml";
         projectDoc  = xnrt.RESTGetDoc(restCommand);
         String projectXml = XMLUtilities.dumpDOMDocument(projectDoc);
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
      
      // Check that all the patients in Santosh's reduced cohort are in XNAT and
      // use these.
      if (subjList.containsAll(getSubjectSubset()))
      {
         Frequency    typeFreq   = new Frequency();
         
         List<String> parNames   = new ArrayList<>(Arrays.asList(
         "TR", "TE", "dx", "dy", "dz", "flip angle", "matrix x", "matrix y",
         "frames"));
         
         List<String> xpeSuffixes = new ArrayList<>(Arrays.asList(
         "tr", "te", "voxelRes/@x", "voxelRes/@y", "voxelRes/@z",
         "flip", "fov/@x", "fov/@y", "***"));
         
         ArrayList<SequenceParameter> pars = new ArrayList<>();
         for (int i=0; i<parNames.size(); i++)
            pars.add(new SequenceParameter(parNames.get(i), xpeSuffixes.get(i)));
         
         for (String subj : getSubjectSubset())
         {
            verbosePrint("");
            verbosePrint("Subject: " + subj);
            List<String> expList = new ArrayList<>();
            try
            {
               restCommand = "/data/archive/projects/" + proj
                                    + "/subjects/" + subj
                                    + "/experiments?format=xml";

               expList     = xnrt.RESTGetResultSet(restCommand).getColumnAsList(0);
            }
            catch (XNATException exXNAT)
            {
               verbosePrint("Problem checking for list of subjects: "
                                        + exXNAT.getMessage());
            }

            for (String exp : expList)
            {
               verbosePrint("--> MR Session: " + exp);
               
               List<String> scanList = new ArrayList<>();
               Document     expDoc = null;
               try
               {
                  restCommand = "/data/archive/projects/" + proj
                                       + "/subjects/" + subj
                                       + "/experiments/" + exp
                                       + "?format=xml";

                  expDoc      = xnrt.RESTGetDoc(restCommand);

                  restCommand = "/data/archive/projects/" + proj
                                       + "/subjects/" + subj
                                       + "/experiments/" + exp
                                       + "/scans?format=xml";

                  scanList    = xnrt.RESTGetResultSet(restCommand).getColumnAsList(1);
               }
               catch (XNATException exXNAT)
               {
                  verbosePrint("Problem checking for list of subjects: "
                                           + exXNAT.getMessage());
               }

               for (String scan : scanList)
               {
                  try
                  {
                     NamespaceContext XNATns = new XNATNamespaceContext();
                     String xpe = "xnat:MRSession/xnat:scans/xnat:scan[@ID = '" + scan + "']/@type";
                     String type = (XMLUtilities.getXPathResult(expDoc, XNATns, xpe))[0];
                     
                     if (seqNames.contains(type.toUpperCase()))
                     {
                        verbosePrint("----> Scan: " + scan);
                        
                        typeFreq.addValue(type);
                        
                        for (SequenceParameter par : pars)
                        {
                           String val = par.getStringValue(scan, expDoc);
                           if (val != null) par.addData(val);
                           verbosePrint("------> " + par.name + " = " + val);
                        }
                     }
                  }
                  catch (XMLException exXML)
                  {
                     throw new XMLException(XMLException.PARSE, "Problem parsing "
                                            + "XNAT experiment XML for subject " + subj 
                                            + exXML.getMessage());
                  }
               }
            }
         }
         System.out.println();
         System.out.println("Results for " + contrast + "images");
         for (SequenceParameter par : pars)
         {
            System.out.println();
            
            double ddata[] = par.getDblArr();
            System.out.println(StatUtils.min(ddata) + " <= "
                               + par.name + " <= " + StatUtils.max(ddata));
            
            System.out.println("Number of observations: " + par.dataList.size()); 
            System.out.println("Mode: " + par.freq.getMode());
            if (verbose)
            {
               System.out.println("Frequency table (values x 1000)");
               System.out.println(par.freq.toString());
            }
         }
         
         // Special treatment for FOV, which isn't a variable in its own right,
         // but, rather, the product of the resolution and number of voxels.
         Frequency fovXFreq = new Frequency();
         List<Double> dx = pars.get(2).dataList;
         List<Double> nx = pars.get(6).dataList;
         List<Double> fx = new ArrayList<>();
         for (int i=0; i<nx.size(); i++)
         {
            fx.set((i), nx.get(i) * dx.get(i));
            fovXFreq.addValue(fx.get(i));
         }
         
         Frequency fovYFreq = new Frequency();
         List<Double> dy = pars.get(3).dataList;
         List<Double> ny = pars.get(7).dataList;
         List<Double> fy = new ArrayList<>();
         for (int i=0; i<ny.size(); i++)
         {
            fy.set((i), ny.get(i) * dy.get(i));
            fovYFreq.addValue(fy.get(i));
         }
         
         Frequency fovZFreq = new Frequency();
         List<Double> dz = pars.get(4).dataList;
         List<Double> nz = pars.get(8).dataList;
         List<Double> fz = new ArrayList<>();
         for (int i=0; i<nz.size(); i++)
         {
            fz.set((i), nz.get(i) * dz.get(i));
            fovZFreq.addValue(fz.get(i));
         }
         
         System.out.println();
/*            
         double ddata[] = par.getDblArr();
            System.out.println(StatUtils.min(ddata) + " <= "
                               + par.name + " <= " + StatUtils.max(ddata));
            
            System.out.println("Number of observations: " + par.dataList.size()); 
            System.out.println("Mode: " + par.freq.getMode());
            if (verbose)
            {
               System.out.println("Frequency table (values x 1000)");
               System.out.println(par.freq.toString());
            }
  */

         }

   }
   
   private void verbosePrint(String s)
   {
      if (verbose) System.out.println(s);
   }

   /*
    * This is a use-case-specific hack to compile a list of the MR sequence 
    * names to examine from the radiomics data for the BRC_A125 breast project.
   */
   private static List<String> getSequenceNames(String contrast)
   {
      List<String> seqNames = new ArrayList<>();
      
      switch(contrast)
      {
         
         case "T2W" :
            seqNames = new ArrayList<>(Arrays.asList(
            "T2_TSE_TRA", "T2W/TSEAX", "T2_TSE_TRA_3MM", "T2W_TSE"));
            break;
            
         case "DCE_E" :
            seqNames = new ArrayList<>(Arrays.asList(
            "T1_FL3D_TRA_DYNAMIC_SUB", "SDYN_SUB2", "T1_FL3D_TRA_FS_DYNAMIC_SPAIR_SUB",
            "SSUB 2-1_ETHRIVE_7DYN SENSE", "SDYN_SUB_2", "SREC DYN_SUB2",
            "S3-1DYN_ETHRIVE_7DYN SENSE", "S7-1DYN_ETHRIVE_7DYN SENSE",
            "T1_FL3D_TRA_DYNA_VIBE_SUB", "T1_FL3D_TRA_DYNA_CAIPI_SUB"));
            break;
            
         case "DW" :
            seqNames = new ArrayList<>(Arrays.asList(
            "EP2D_TRA_4B_SPAIR_DYNDIST", "IR_DWI_BILAT SENSE", "DWI_TRA",
            "EP2D_TRA_4B_SPAIR_DYNDIST_MIX", "DWI_SSH_4BV",
            "REG - DWI_SSH SENSE", "EP2D_TRA_4B_SPAIR_TRACEW_DFC_MIX",
            "EP2D_DIFF_BIPOLAR_TRACEW", "EP2D_DIFF_BIPOLAR 30SEP13_TRACEW",
            "EP2D_DIFF_SPAIR_TRACEW_DFC", "RESOLVE_DIFF_TRA_SPAIR_TRACEW"));
            break;
                 
         default:
            System.out.println("Invalid contrast type specified in code.");
            System.exit(1);
      }
      
      return seqNames;
   }
   
   private static List<String> getSubjectSubset()
   {
      List<String> subjNames = new ArrayList<>(Arrays.asList(
      "BRC_RADPRIM_001STL",
      "BRC_RADPRIM_005SNS",
      "BRC_RADPRIM_009BEA",
      "BRC_RADPRIM_010THOL",
      "BRC_RADPRIM_011IRC",
      "BRC_RADPRIM_012RIB",
      "BRC_RADPRIM_013SMM",
      "BRC_RADPRIM_014CHD",
      "BRC_RADPRIM_015HOS",
      "BRC_RADPRIM_016WOK",
      "BRC_RADPRIM_017MAS",
      "BRC_RADPRIM_019MOS",
      "BRC_RADPRIM_023PLC",
      "BRC_RADPRIM_029TAJ",
      "BRC_RADPRIM_032CHH",
      "BRC_RADPRIM_033FRA",
      "BRC_RADPRIM_035ELS",
      "BRC_RADPRIM_036GIM",
      "BRC_RADPRIM_037BRK",
      "BRC_RADPRIM_038CAM",
      "BRC_RADPRIM_041BRE",
      "BRC_RADPRIM_044ADE",
      "BRC_RADPRIM_045TOJ",
      "BRC_RADPRIM_046WIL",
      "BRC_RADPRIM_049ALG",
      "BRC_RADPRIM_052WED",
      "BRC_RADPRIM_053BRC",
      "BRC_RADPRIM_054GEC",
      "BRC_RADPRIM_057HOM",
      "BRC_RADPRIM_058BAS",
      "BRC_RADPRIM_062BEG",
      "BRC_RADPRIM_064PED",
      "BRC_RADPRIM_065COA",
      "BRC_RADPRIM_084MOV",
      "BRC_RADPRIM_088SMM",
      "BRC_RADPRIM_089SMA",
      "BRC_RADPRIM_096HAS",
      "BRC_RADPRIM_102BUI",
      "BRC_RADPRIM_106DIB",
      "BRC_RADPRIM_117WIJ",
      "BRC_RADPRIM_123JOM",
      "BRC_RADPRIM_124BUL",
      "BRC_RADPRIM_125GIE",
      "BRC_RADPRIM_129DAD",
      "BRC_RADPRIM_130WAH",
      "BRC_RADPRIM_131ONC",
      "BRC_RADPRIM_133GOK",
      "BRC_RADPRIM_134SNS",
      "BRC_RADPRIM_139ALD",
      "BRC_RADPRIM_140OCC",
      "BRC_RADPRIM_145DAJ",
      "BRC_RADPRIM_149COA",
      "BRC_RADPRIM_150HID",
      "BRC_RADPRIM_152KIC",
      "BRC_RADPRIM_153FIM",
      "BRC_RADPRIM_154NYC",
      "BRC_RADPRIM_155THH",
      "BRC_RADPRIM_156ODS",
      "BRC_RADPRIM_161MCF",
      "BRC_RADPRIM_162BLD",
      "BRC_RADPRIM_164ROP",
      "BRC_RADPRIM_166COA",
      "BRC_RADPRIM_170EDK",
      "BRC_RADPRIM_172DAH",
      "BRC_RADPRIM_175JEN",
      "BRC_RADPRIM_176PIJ",
      "BRC_RADPRIM_177BUE",
      "BRC_RADPRIM_180FRC",
      "BRC_RADPRIM_185LUV",
      "BRC_RADPRIM_191BAH",
      "BRC_RADPRIM_192LLS",
      "BRC_RADPRIM_197WHJ",
      "BRC_RADPRIM_198WHA",
      "BRC_RADPRIM_199STD",
      "BRC_RADPRIM_201SCS",
      "BRC_RADPRIM_202PEJ",
      "BRC_RADPRIM_206BRJ",
      "BRC_RADPRIM_207BUI",
      "BRC_RADPRIM_208ALS",
      "BRC_RADPRIM_247YOS",
      "BRC_RADPRIM_250BAS",
      "BRC_RADPRIM_264NWR",
      "BRC_RADPRIM_265BOP",
      "BRC_RADPRIM_269CAJ",
      "BRC_RADPRIM_271COA",
      "BRC_RADPRIM_277MIM",
      "BRC_RADPRIM_285ALR",
      "BRC_RADPRIM_289CHB",
      "BRC_RADPRIM_291STL",
      "BRC_RADPRIM_292NEP",
      "BRC_RADPRIM_293SMD"));
   
      return subjNames;
   }
      
}
