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
* Java class: MRIWResultSet.java
* First created on Jul 26, 2012 at 3:58:38 PM
* 
* Defines a representation of the MRIResultSet, including methods to
* read the data in from an XML file.
*********************************************************************/

package dataRepresentations.xnatUpload;

import xnatUploader.ContourRendererHelper;
import dataRepresentations.xnatUpload.XnatUpload;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.UIDGenerator;
import generalUtilities.Vector2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import exceptions.DataFormatException;
import generalUtilities.DicomXnatDateTime;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;

public final class MRIWOutput extends XnatUpload
{
   static  Logger logger = Logger.getLogger(MRIWOutput.class);
   
   public static final int CSV     = 1;
   public static final int BASE64  = 2;
   public static final int XML     = 3;
   public static final int ARRAY   = 4;
   public static final int FLOAT   = 5;
   public static final int BOOLEAN = 6;
   public static final int INT32   = 7;
   
   /**
    * Auxiliary classes defining the structure of various internal
 elements as specified in the MRIW_RECORD output file DTD.
    */ 
   public class MRIWRaw
   {    
      public int                       encoding;
      public String                    type;
      public String                    unit;
      public int                       xSize;
      public int                       ySize;
      public String                    cData;
      public int                       sourceFormat;     
   }
   
   
   public class MRIWProvenance
   {
      public String                    programName;
      public String                    programVersion;
      public String                    programBuildID;
      
      public String                    creationUser;
      public String                    creationDateTime;
      
      public String                    platformMachineArchitecture;
      public String                    platformMachineOSName;
      public String                    platformMachineOSType;
      public String                    platformRuntimeName;
      public String                    platformRuntimeVersion;
   }
   
   
   public class MRIWInput
   {
      public String                    modality;
      public String                    subtype;
      
      public String                    refStudyUID;
      public String                    refFilepath;
      public String                    refFilename;
      public String                    refSeriesInstanceUID;
      public String                    refSOPInstanceUID;
      public MRIWRaw                   refRaw;
      
      public String                    dynIgnore;
      public int                       dynNSlices;
      public String                    dynPath;
      public String                    dynStudyUID;
      public ArrayList<String>         dynFilenames;
      public ArrayList<String>         dynSeriesInstanceUIDs;
      public ArrayList<String>         dynSOPInstanceUIDs;
   }
   
   
   public class MRIWControl
   {
      public String                    converterClass;
      public SortedMap<String, String> converterKeyValue;
      
      public String                    modelClass;
      public String                    modelOnset;
      public Boolean                   modelAggregateAll;
      public Boolean                   modelValidateInput;
      public Boolean                   modelValidateOutput;
      public Boolean                   modelCheckPersistence;
      public SortedMap<String, String> modelKeyValue;
      public SortedMap<String, String> modelRangesKeyValue;
      
      public String                    solverClass;
      public SortedMap<String, String> solverKeyValue;
      
      public String                    agentName;
      public Float                     agentVolume;
      public Float                     agentConcentration;
      public Float                     agentR1;
      public Float                     agentR2;
      
      public Float                     patientWeight;
      
      public String                    helpersCurveExtractor;
      public String                    helpersOnsetLocator;
      public String                    helpersROICurveExtractor;
      
      public Integer                   roiEncoding;
      public Integer                   roiXSize;
      public Integer                   roiYSize;
      public ArrayList<Float>          roiX;
      public ArrayList<Float>          roiY;
      
      public String                    alignROIX;
      public String                    alignROIY;
      public String                    alignROIXSize;
      public String                    alignROIYSize;
      public String                    alignImageXSize;
      public String                    alignImageYSize;
      public Boolean                   dynamicOnly;
      public String                    alignType;
      public Integer                   alignOffsetEncoding;
      public String                    alignOffsetsX;
      public String                    alignOffsetsY;
      public Integer                   alignOffsetsXType;
      public Integer                   alignOffsetsYType;
      
      public Float                     timeInterval;
      public Float                     timeOnset;
      
      public Boolean                   fitUseImages;
      
      public String                    importFilterTypeClass;
      public SortedMap<String, String> importFilterKeyValue;
      
      public SortedMap<String, String> overrideKeyValue;
   }
   
   
   public class MRIWDataRecord
   {
      public int                       x;
      public int                       y;
      public ArrayList<Float>          data;
   }
   
   
   public class MRIWMap
   {
      public String                    mapName;
      public String                    mapType;
      public ArrayList<Float>          mapData;
   }
   
   
   public class MRIWResults
   {
      public Integer                   converterOutputEndcoding;
      public Integer                   converterOutputXSize;
      public Integer                   converterOutputYSize;
      public Integer                   converterOutputPointCount;
      public ArrayList<MRIWDataRecord> converterOutputData;
      
      public Integer                   computedMapEncoding;
      public ArrayList<MRIWMap>        computedMaps;
      public ArrayList<MRIWMap>        supplementalMaps;
      public ArrayList<MRIWMap>        failMaps;
      public ArrayList<String>         failCodes;
      
      public Integer                   aggregateResultsEncoding;
      public ArrayList<Float>          aggregateData;
      public SortedMap<String, String> aggregateKeyValue;
   }
   
   
   public static final int             BATCH_MODE = 1;
   public static final int             RESULT_SET = 2;
   
   public String                       version;
   public MRIWProvenance               prov;
   public MRIWInput                    inp;
   public MRIWControl                  con;
   public MRIWResults                  res; 
   public Document                     doc;
   public int                          outputType;
   public String                       sessionDate;
   public String                       sessionTime;
   public String                       patientCode;
   public String                       measurementCode;
   public String                       sliceLocation;
   public String                       sliceThickness;
   public float[]                      pixelSpacing;
   public float[]                      dirCosines;
   public float[]                      topLeftPos;
   public String                       frameOfReferenceUID;
   public String                       studyDescription;
   public String                       imageSOPClassUID;
   
   public XNATNamespaceContext         XNATns;
   
   // Question: Does it make sense to place XNAT-specific information in an
   // object that is describing a DICOM concept?
   // Answer: For a number of reasons, it becomes useful to take information
   // from the base DICOM scans and in order to access these, we use XNAT.
   // Some of the XNAT parameters may be useful to extract.
   public String                       XNATDateOfBirth;
   public String                       XNATGender;
     
   
   
   /**
    * Public creator from an MRI file source.
	 * Parse the MRIW_RECORD ResultSet or Batch file and place the results in
	 * the object's structure.
	 * Note that This implementation is currently incomplete because of lack
	 * of appropriate examples of some of the tags.
    * @param MRIWDoc an XML Document, whose contents represent a valid MRIW output
    * @param xnprf an XNAT profile, already connected to an XNAT database, which
    * we can use to query the databases for image dependencies.
	 * @throws exceptions.DataFormatException
	 * @throws exceptions.XMLException
	 * @throws exceptions.XNATException
    */
   public MRIWOutput(Document MRIWDoc, XNATProfile xnprf)
                            throws DataFormatException, XMLException, XNATException
   {
		this.doc   = MRIWDoc;
      this.xnprf = xnprf;
		
      prov = new MRIWProvenance();
      inp  = new MRIWInput();
      con  = new MRIWControl();
      res  = new MRIWResults();
      
      inp.dynFilenames          = new ArrayList<>();
      inp.dynSOPInstanceUIDs    = new ArrayList<>();
      inp.dynSeriesInstanceUIDs = new ArrayList<>();
      fileSOPMap                = new TreeMap<>();
      fileScanMap               = new TreeMap<>();
      XNATns                    = new XNATNamespaceContext();            
      
      try
      {      
         if (XMLUtilities.getElement(doc, XNATns, "mriw-result-set") != null)
            outputType = RESULT_SET;

         else if (XMLUtilities.getElement(doc, XNATns, "mriwBatchMode") != null)
            outputType = BATCH_MODE;

         else
         {
            throw new DataFormatException(DataFormatException.MRIW_RECORD);
         }
         
         prov.programName                 = getAttr("program",   "name");
         prov.programVersion              = getAttr("program",   "version");
         prov.programBuildID              = getAttr("program",   "build-id");        
         prov.creationDateTime            = getAttr("creation",  "time");         
         prov.creationUser                = getAttr("creation",  "user");      
         prov.platformMachineArchitecture = getAttr("machine",   "architecture");
         prov.platformMachineOSName       = getAttr("machine",   "os-name");
         prov.platformMachineOSType       = getAttr("machine",   "os-type");  
         prov.platformRuntimeName         = getAttr("runtime",   "name");         
         prov.platformRuntimeVersion      = getAttr("runtime",   "version");
         
         inp.modality                     = getAttr("input",     "modality");         
         inp.subtype                      = getAttr("input",     "subtype");
         inp.refFilepath                  = getAttr("reference", "path");
         inp.refStudyUID                  = getAttr("reference", "study-uid");
         inp.refFilename                  = getFirstXPathResult("//reference/file/@name");
         inp.refSOPInstanceUID            = getFirstXPathResult("//reference/file/@sop-instance-uid");
         inp.refSeriesInstanceUID         = getFirstXPathResult("//reference/file/@series-uid");
         inp.dynPath                      = getAttr("dynamic",   "path");
         inp.dynStudyUID                  = getAttr("dynamic",   "study-uid");
         inp.dynNSlices                   = getAttrAsInteger("dynamic", "slices");
         inp.dynFilenames                 = getXPathResult("//dynamic/file/@name");
         inp.dynSeriesInstanceUIDs        = getXPathResult("//dynamic/file/@series-uid");
         inp.dynSOPInstanceUIDs           = getXPathResult("//dynamic/file/@sop-instance-uid"); 
         con.converterClass               = getAttr("converter", "class");
         con.converterKeyValue            = getKeyValue("converter");
         con.modelClass                   = getAttr("model",     "class");
         con.modelOnset                   = getAttr("model",     "onset");
         con.modelAggregateAll            = getAttrAsBoolean("model", "aggregate-all");
         con.modelValidateInput           = getAttrAsBoolean("model", "validate-input");
         con.modelValidateOutput          = getAttrAsBoolean("model", "validate-output");
         con.modelCheckPersistence        = getAttrAsBoolean("model", "check-persistence");
         con.modelKeyValue                = getKeyValue("model");
         con.modelRangesKeyValue          = getKeyValue("ranges");
         con.solverClass                  = getAttr("solver", "class");
         con.solverKeyValue               = getKeyValue("solver");
         con.agentName                    = getAttr("agent", "name");
         con.agentVolume                  = getAttrAsFloat("agent", "volume");
         con.agentR1                      = getAttrAsFloat("agent", "r1");
         con.agentR2                      = getAttrAsFloat("agent", "r2");
         con.patientWeight                = getAttrAsFloat("patient", "weight");
         con.helpersCurveExtractor        = getAttr("helpers",   "curve-extractor");
         con.helpersOnsetLocator          = getAttr("helpers",   "onset-locator");
         con.helpersROICurveExtractor     = getAttr("helpers",   "roi-curve-extractor");
         con.roiXSize                     = getAttrAsInteger("roi", "x-size");
         con.roiYSize                     = getAttrAsInteger("roi", "y-size");
         
         String encoding                  = getAttr("roi",       "encoding");
         if (encoding != null)
         {
            if (encoding.equals("csv"))
            {
               con.roiEncoding = CSV;
               con.roiX = getFloatArrayListFromElementCSV("roi-x");
               con.roiY = getFloatArrayListFromElementCSV("roi-y");
            }
            if (encoding.equals("base64"))
            {
               con.roiEncoding = BASE64;
               // TODO As soon as I have a relevant test dataset, create this function.
               //  con.roiX = getFloatArrayFromElementBase64("roi-x");
               //  con.roiX = getFloatArrayFromElementBase64("roi-y");
            }
         }
         
         res.converterOutputXSize         = getAttrAsInteger("converter-output", "x-size");
         res.converterOutputYSize         = getAttrAsInteger("converter-output", "y-size");
         res.converterOutputPointCount    = getAttrAsInteger("converter-output", "point-count");
         encoding                         = getAttr("converter-output",  "encoding");
         if (encoding != null)
         {
            if (encoding.equals("csv"))    res.converterOutputEndcoding = CSV;
            if (encoding.equals("base64")) res.converterOutputEndcoding = BASE64;
            
            res.converterOutputData       = getDataRecordsForElement("converter-output",
                                                  res.converterOutputEndcoding); 
         }
    
         encoding                         = getAttr("computed-maps", "encoding");
         if (encoding != null)
         {
            if (encoding.equals("csv"))    res.computedMapEncoding = CSV;
            if (encoding.equals("base64")) res.computedMapEncoding = BASE64;
         
            res.computedMaps = getMapsForElement("computed-maps", res.computedMapEncoding);
            res.failMaps     = getMapsForElement("failures", res.computedMapEncoding);
         }
         
         res.failCodes                    = getStringArrayListFromElementCSV("fail-codes");
         
         
         
         encoding                         = getAttr("aggregate-results", "encoding");
         if (encoding != null)
         {
            if (encoding.equals("csv"))
            {
               res.aggregateResultsEncoding = CSV;
               res.aggregateData            = getFloatArrayListFromElementCSV("aggregate-data");
            }
            if (encoding.equals("base64")) res.aggregateResultsEncoding = BASE64;
         }
         
         res.aggregateKeyValue            = getKeyValue("aggregate-values");
         
         
         // Check that all the studies, series and SOPInstances referenced are
         // already present in the database.
         dependenciesInDatabase(xnprf);
         
         // Extract additional parameters that can be found only by downloading
         // and opening one of the referenced DICOM files from the database
         // (sliceLocation and frameOfReferenceUID).
         getDICOMParameters();
      }
      catch (XNATException | XMLException ex)
      {
         logger.error(ex.getMessage());
         throw ex;
      }
      
      catch (DataFormatException exDF)
      {
         logger.warn("Encountered the following data format error\n"
                 + exDF.getMessage() + "\n"
                 + "This may be due to the parsing of an old version of MRIW output file.");
      }
   }
   
   
   
   private void dependenciesInDatabase(XNATProfile xnprf)
           throws XNATException, XMLException
   {
      // Normally, the reason we are creating an MRIWOutput object is to upload a given
      // file to a particular project and so the given profile will be supplied
      // with only one element in the project list. However, this might change ...
      XNATProjectID = xnprf.getProjectList().get(0);
      
      String                RESTCommand;
      XNATRESTToolkit       xnrt = new XNATRESTToolkit(xnprf);
      Vector2D<String>      result;
      String[][]            parseResult;
      String[][]            parseResult2;
      Document              resultDoc;
      
      
      // Are all the studies in the database?
      try
      {       
         RESTCommand = "/data/archive/projects/" + XNATProjectID + "/experiments"
                       + "?xsiType=xnat:imageSessionData"
                       + "&columns=xnat:imageSessionData/UID,"
                       + "xnat:imageSessionData/subject_ID,"
                       + "xnat:imageSessionData/date,"
                       + "xnat:imageSessionData/time,"
                       + "xnat:imageSessionData/dcmPatientName,"
                       + "xnat:imageSessionData/visit_id"
                       + "&format=xml";
         result      = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (XNATException exXNAT)
      {
         String msg  = "Unable to process MRIWOutput: error while determining"
                       + " whether the DICOM studies are in the XNAT database.\n";
                    
         if (exXNAT.getMessage() != null) msg += exXNAT.getMessage();
         
         logger.error(msg);
         throw new XNATException(XNATException.GET, msg); 
      }
 
      
      HashSet<String> absentStudies = new HashSet<>();
      if (!result.columnContains(1, inp.dynStudyUID)) absentStudies.add(inp.dynStudyUID);
      
      // Note that inp.refStudyUID is an optional field coming out of MRIW_RECORD.
      if ((inp.refStudyUID != null) && (!result.columnContains(1, inp.refStudyUID)))
              absentStudies.add(inp.refStudyUID);
      
      if (!absentStudies.isEmpty())
      {
         String st    = (absentStudies.size() == 1) ? "study" : "studies";
         String UID   = (absentStudies.size() == 1) ? "UID" : "UIDs";
         String isare = (absentStudies.size() == 1) ? "is" : "are";
         
         String msg   = "The patient " + st + " with DICOM " + UID + "\n";
         for (String s : absentStudies) msg += s + "\n";
         msg += "associated with this MRIW Output " + isare + " absent\n"
                + "from from project " + XNATProjectID + " in the chosen XNAT database."
                + "\n(profile " + xnprf.getProfileName()
                + ")\n\nPlease archive data to XNAT before continuing.";
         
         throw new XNATException(XNATException.DATA_NOT_PRESENT, msg);
      }
      
      
      int row             = result.indexOfForCol(1, inp.refStudyUID);
      XNATRefExperimentID = result.atom(0, row);
      XNATSubjectID       = result.atom(2, row);
      
      row                 = result.indexOfForCol(1, inp.dynStudyUID);
      XNATExperimentID    = result.atom(0, row);
      String subjID       = result.atom(2, row);          
      
      if (!subjID.equals(XNATSubjectID))
      {
         String msg = "Unable to process MRIWOutput: XNAT subject ID's do not"
                      + " match between reference and dynamic scans.";
         throw new XNATException(XNATException.DATA_INCONSISTENT, msg);
      }
      
      if (!XNATRefExperimentID.equals(XNATExperimentID))
         logger.warn("DICOM UIDs for dynamic and reference studies are different.\n"
                     + "Whilst this is formally permitted, the MRIWOutput data\n"
                     + "will be uploaded only to the session containing the\n"
                     + "dynamic data and the session data and time recorded\n"
                     + "in XNAT will refer to the dynamic scan.");
      
      sessionDate     = result.atom(3, row);
      sessionTime     = result.atom(4, row);
      patientCode     = result.atom(5, row);
      measurementCode = result.atom(6, row);
      
      
      // Are all the series in the database?
      // Since there is the theoretical potential that the reference and
      // dynamic experiments could have been acquired with different
      // DICOM study UIDs (and hence archived as different XNAT experiments,
      // we need to go through the REST call twice.
      
      try
      {
         RESTCommand = "/data/archive/projects/" + XNATProjectID
                       + "/subjects/"            + XNATSubjectID
                       + "/experiments/"         + XNATExperimentID
                       + "?format=xml";
         resultDoc   = xnrt.RESTGetDoc(RESTCommand);
         parseResult = XMLUtilities.getAttributes(resultDoc, XNATns, "xnat:scan",
                                                   new String[] {"ID", "UID"});
         
         RESTCommand = "/data/archive/projects/" + XNATProjectID
                       + "/subjects/"            + XNATSubjectID
                       + "/experiments/"         + XNATRefExperimentID
                       + "?format=xml";
         resultDoc   = xnrt.RESTGetDoc(RESTCommand);
         parseResult2= XMLUtilities.getAttributes(resultDoc, XNATns, "xnat:scan",
                                                   new String[] {"ID", "UID"});
      }
      catch (XNATException exXNAT)
      {
         String msg  = "Unable to process MRIWOutput: error while determining"
                       + " whether the DICOM series are in the XNAT database.\n";
                    
         if (exXNAT.getMessage() != null) msg += exXNAT.getMessage();
         
         logger.error(msg);
         throw new XNATException(XNATException.GET, msg); 
      }
      
                 
      // Iterate only over unique series, so that we repeat the various
      // checks only as often as we need to.
      HashSet<String> uniqueSeries = new HashSet<>();
      for (String s : inp.dynSeriesInstanceUIDs) uniqueSeries.add(s);
      
      HashSet<String> absentSeries = new HashSet<>();
      XNATScanID = new LinkedHashSet<>();
      
      for (String series : uniqueSeries)
      {
         boolean present = false;
         for (int i=0; i<parseResult.length; i++)
         {
            // Not all of the XNAT experiments returned are scans. Some might be
            // assessors, with no UID. These need to be screened out.
            if (parseResult[i][1] != null)
            {
               if (parseResult[i][1].equals(series))
               {
                  present = true;
                  XNATScanID.add(parseResult[i][0]);
                  break;
               }
            }
         }
         if (!present) absentSeries.add(series);         
      }
      
      boolean present = false;
      for (int i=0; i<parseResult2.length; i++)
      {
         if (parseResult2[i][1] != null)
         {
            if (parseResult2[i][1].equals(inp.refSeriesInstanceUID))
            {
               present = true;
               XNATScanID.add(parseResult2[i][0]);
               break;
            }
         }
      }
      if (!present) absentSeries.add(inp.refSeriesInstanceUID);

      
      if (!absentSeries.isEmpty())
      {
         int    sz    = absentSeries.size();
         String st    = (sz == 1) ? "study" : "studies";
         String UID   = (sz == 1) ? "UID" : "UIDs";
         String isare = (sz == 1) ? "is" : "are";
         
         String msg = sz + " DICOM series associated with"
            + " this MRIWOutput" + isare + " absent from project "
            + XNATProjectID + "\n in the chosen XNAT database (profile "
            + xnprf.getProfileName() + ").\n\n";
         
         if (sz < 5)
         {
            msg += "The missing series have " + UID + "\n";
            for (String s : absentSeries) msg += s + "\n";
         }           
                 
         throw new XNATException(XNATException.DATA_NOT_PRESENT, msg);
      }

      
      // We need a list of the actual data files in the repository
      // that are referenced, to go in the "in" section of qcAssessmentData.
      // See the Class DICOMFileListWorker for an example of how to do this
      // both if the files are local or remote. Here, for simplicity, I don't
      // assume anything and use the REST method whether the files are local
      // or remote.       
      for (int i=0; i<XNATScanID.size(); i++)
      {
         try
         {
            RESTCommand = "/data/archive/projects/"    + XNATProjectID
                             + "/subjects/"            + XNATSubjectID
                             + "/experiments/"         + XNATExperimentID
                             + "/scans/"               + XNATScanID
                             + "/resources/DICOM?format=xml";
            resultDoc   = xnrt.RESTGetDoc(RESTCommand);
            parseResult = XMLUtilities.getAttributes(resultDoc, XNATns, "cat:entry",
                                                     new String[] {"URI", "UID"});
         }
         catch(XNATException exXNAT)
         {
            throw exXNAT;
         }
         
         // Cater for the obscure case where parseResult comes back null. This
         // happened to me after I had (manually) screwed up the data repository.
         if (parseResult == null)
            throw new XNATException(XNATException.DATA_NOT_PRESENT,
                            "There are no relevant DICOM image files. This might be an"
                            + " inconsistent condition in the repository and be"
                            + " worth investigating further.");

         for (int j=0; j<parseResult.length; j++)
         {
            if (inp.dynSOPInstanceUIDs.contains(parseResult[j][1]) ||
                           inp.refSOPInstanceUID.equals(parseResult[j][1]))
				{
               fileSOPMap.put(parseResult[j][1], parseResult[j][0]);
      //         fileScanMap.put(parseResult[j][1], XNATScanID.get(i));
				}
         }
      }
      
      
      // Finally, retrieve some further demographic information so that it is
      // available for output where necessary.
      try
      {
         RESTCommand      = "/data/archive/projects/" + XNATProjectID
                                + "/subjects/"        + XNATSubjectID
                                + "?format=xml";
         resultDoc        = xnrt.RESTGetDoc(RESTCommand);
         String[] s       = XMLUtilities.getAttribute(resultDoc, XNATns,
                                                   "xnat:Subject", "label");
         if (s != null) XNATSubjectLabel = s[0];
         
         s = XMLUtilities.getElementText(resultDoc, XNATns, "xnat:gender");
         if (s != null) XNATGender = s[0];
         
         s = XMLUtilities.getElementText(resultDoc, XNATns, "xnat:dob");
         if (s != null) XNATDateOfBirth = s[0];
      }
      catch(XNATException exXNAT)
      {
         throw exXNAT;
      }
   }
   
   
   /**
	 * Create a DICOM RT-STRUCT file using the region-of-interest contained
	 * within the MRIWOutput file.
	 * @return
	 * @throws Exception 
	 */
	public DicomObject createDICOM() throws Exception
   {
      DicomObject odo = new BasicDicomObject();
      
      odo.initFileMetaInformation(UID.ExplicitVRLittleEndian);
      odo.putString(Tag.MediaStorageSOPClassUID, VR.UI, UID.RTStructureSetStorage);
      
      String uid = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DATA_UPLOADER,
                                                  UIDGenerator.RT_STRUCT,
                                                  UIDGenerator.SeriesInstanceUID);
      odo.putString(Tag.MediaStorageSOPInstanceUID, VR.UI, uid);
      
      odo.putString(Tag.SpecificCharacterSet,    VR.CS, "ISO_IR 100");

      String formattedDate = String.format("%1$tY%1$tm%1$td", Calendar.getInstance());
      String formattedTime = String.format("%1$tH%1$tM%1$tS.%1$tM", Calendar.getInstance());
      odo.putString(Tag.InstanceCreationDate,    VR.DA, formattedDate); 
      odo.putString(Tag.InstanceCreationTime,    VR.TM, formattedTime);

      odo.putString(Tag.SOPClassUID,             VR.UI, UID.RTStructureSetStorage);
      odo.putString(Tag.SOPInstanceUID,          VR.UI, uid);
      
      
      odo.putString(Tag.StudyDate,               VR.DA, sessionDate);
      odo.putString(Tag.StudyTime,               VR.TM, sessionTime);
      odo.putString(Tag.Modality,                VR.CS, "RTSTRUCT");    
      odo.putString(Tag.Manufacturer,            VR.LO, "Institute of Cancer Research");
      odo.putString(Tag.StationName,             VR.SH, InetAddress.getLocalHost().getHostName());      
      
      // The MRIW_RECORD result set files do not contain study description data.
      odo.putString(Tag.StudyDescription,        VR.LO, "");
      odo.putString(Tag.ManufacturerModelName,   VR.LO, "ICR: XNAT DataUploader");
   
            
      outputReferencedFrameOfReferenceSequence(odo);
      
      // Patient demographics are a clear example of a potential conflict. Should we
      // output the values that is in the input DICOM file or the subject name
      // to which this is attached in the XNAT database?
      odo.putString(Tag.PatientName,             VR.PN, XNATSubjectLabel);
      odo.putString(Tag.PatientID,               VR.LO, XNATSubjectLabel);
      
      if (XNATDateOfBirth != null)
         odo.putString(Tag.PatientBirthDate,     VR.DA, XNATDateOfBirth);
      
      if (XNATGender != null)
         odo.putString(Tag.PatientSex,           VR.CS, XNATGender);
      
      odo.putString(Tag.SoftwareVersions,        VR.SH, version);
      
      // In the next four lines, I am assuming that the purpose of these tags
      // in this context is to associate this newly created DICOM file with the
      // *original study*, as represented both in DICOM (StudyInstanceUID) and
      // XNAT (StudyID), but as a new series (SeriesInstanceUID). However, there
      // is an alternate argument that says the outlining procedure constitutes
      // a separate study and thus the SeriesInstanceUID should relate to that.
      odo.putString(Tag.StudyInstanceUID,        VR.UI, inp.refStudyUID);
      
      uid = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DATA_UPLOADER,
                                           UIDGenerator.RT_STRUCT,
                                           UIDGenerator.SeriesInstanceUID);
      odo.putString(Tag.SeriesInstanceUID,       VR.UI, uid);
      
      odo.putString(Tag.StudyID,                 VR.SH, XNATExperimentID);
      
      // Very tricky to know what to put here, or what use other software might
      // make of the value. My exemplar file had 1 here.
      odo.putString(Tag.SeriesNumber,            VR.IS, "1");
      
      
      // The label has to be there according to the standard, but the name and
      // description don't. MRIW_RECORD doesn't provide these pieces of metadata.
      String user  = ((prov.creationUser.isEmpty()) ? "" : (" by user " + prov.creationUser));
      String label = "Auto-generated from MRIW result set originally created by " +
                     prov.programName + " " + prov.programVersion + "-" +
                     prov.programBuildID + " at " + prov.creationDateTime + user;
                   
      odo.putString(Tag.StructureSetLabel,       VR.SH, label);
      
      /*
      if ((structureSetName != null) && (!structureSetName.isEmpty()))
         odo.putString(Tag.StructureSetName,     VR.LO, structureSetName);
      
      if ((structureSetDescription != null) && (!structureSetDescription.isEmpty()))
         odo.getString(Tag.StructureSetDescription, VR.ST, structureSetDescription);
      */
      
      // It is not terribly clear from the DICOM documentation exactly
      // what instance number is or when it would be used.
      /*
      odo.putInt(Tag.InstanceNumber,          VR.IS, instanceNumber);
      */
      
      formattedDate = DicomXnatDateTime.convertMriwToDicomDate(prov.creationDateTime);
      formattedTime = DicomXnatDateTime.convertXnatToDicomTime(prov.creationDateTime);
      odo.putString(Tag.StructureSetDate,        VR.DA, formattedDate);
      odo.putString(Tag.StructureSetTime,        VR.TM, formattedTime);
      
      outputReferencedStudySequence(odo);
      outputStructureSetROISequence(odo);
      outputROIContourSequence(odo);
      outputRTROIObservationsSequence(odo);
     
      return odo;
   }
     
   
   /**
    * Write the DICOM sequence at (0008, 1110).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputReferencedStudySequence(DicomObject odo)
   {
      DicomElement seqRefStudy = odo.putSequence(Tag.ReferencedStudySequence);
      
      // Note that the "referenced" studies here are those that are referred to
      // by the RT-STRUCT file. These include both what MRIW_RECORD calls the
      // reference study and the dynamic study.
      DicomObject  doRefStudy  = new BasicDicomObject();
      doRefStudy.setParent(odo);
      seqRefStudy.addDicomObject(doRefStudy);
      doRefStudy.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inp.dynStudyUID);
   
      // It's not clear that this (now deprecated) UID is providing any useful
      // information, but this line is provided for completeness.
      doRefStudy.putString(Tag.ReferencedSOPClassUID,    VR.UI, "1.2.840.10008.3.1.2.3.2");
 
      if ((inp.refStudyUID != null) && (!inp.refStudyUID.equals(inp.dynStudyUID)))
      {
         doRefStudy  = new BasicDicomObject();
         doRefStudy.setParent(odo);
         seqRefStudy.addDicomObject(doRefStudy);
         doRefStudy.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inp.refStudyUID);
         doRefStudy.putString(Tag.ReferencedSOPClassUID,    VR.UI, "1.2.840.10008.3.1.2.3.2");
   
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0010).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputReferencedFrameOfReferenceSequence(DicomObject odo)
   {
      DicomElement seqRefFOR = odo.putSequence(Tag.ReferencedFrameOfReferenceSequence);

      // Tag (0020,0052)
      // Only one referenced frame of reference and no related ones.
      // The reference and dynamic datasets are acquired in the same FOR.
      DicomObject  doRefFOR  = new BasicDicomObject();
      doRefFOR.setParent(odo);
      seqRefFOR.addDicomObject(doRefFOR);
      doRefFOR.putString(Tag.FrameOfReferenceUID, VR.UI, frameOfReferenceUID);

      // Subsequence at (3006,0012)
      DicomElement seqRtRefStudy = doRefFOR.putSequence(Tag.RTReferencedStudySequence);
      DicomObject  doRtRefStudy  = new BasicDicomObject();
      doRtRefStudy.setParent(doRefFOR);
      seqRtRefStudy.addDicomObject(doRtRefStudy);
      // MRIW_RECORD result set files are structured with the theoretical possibility
      // of the dynamic and reference studies being different, but in practice
      // this will not occur. Choose dyn not ref, because ref is optional in MRIW_RECORD.
      doRtRefStudy.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inp.dynStudyUID);
      doRtRefStudy.putString(Tag.ReferencedSOPClassUID,    VR.UI, "1.2.840.10008.3.1.2.3.2");
 
      // Subsequence at (3006,0014)
      DicomElement seqRtRefSer = doRtRefStudy.putSequence(Tag.RTReferencedSeriesSequence);
      DicomObject  doRtRefSer  = new BasicDicomObject();
      doRtRefSer.setParent(doRtRefStudy);
      seqRtRefSer.addDicomObject(doRtRefSer);
      
      doRtRefSer.putString(Tag.SeriesInstanceUID, VR.UI, inp.dynSeriesInstanceUIDs.get(0));
      
      // Subsequence at (3006,0016)
      // In the case of MRIW_RECORD, the contour spans only a single image, but is
      // equally applicable to all the reference and dynamic data. Choose
      // (arbitrarily) the first of the dynamic series.
      DicomElement seqContourImage = doRtRefSer.putSequence(Tag.ContourImageSequence);
      DicomObject  doContourImage  = new BasicDicomObject();
      doContourImage.setParent(doRtRefSer);
      seqContourImage.addDicomObject(doContourImage);
      doContourImage.putString(Tag.ReferencedSOPClassUID,    VR.UI, imageSOPClassUID);
      doContourImage.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inp.dynSOPInstanceUIDs.get(0));      
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0020).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputStructureSetROISequence(DicomObject odo)
   {
      DicomElement seqSSRoi = odo.putSequence(Tag.StructureSetROISequence);
      DicomObject  doSSRoi  = new BasicDicomObject();
      doSSRoi.setParent(odo);
      seqSSRoi.addDicomObject(doSSRoi);
            
      doSSRoi.putInt(Tag.ROINumber,  VR.IS, 1);          
      doSSRoi.putString(Tag.ReferencedFrameOfReferenceUID, VR.UI, frameOfReferenceUID);
      doSSRoi.putString(Tag.ROIName, VR.LO, "MRIW ROI");
            
      // There are no metadata for the following DICOM concepts in the MRIW_RECORD
      // result set files.
      /*
      doSSRoi.putString(Tag.ROIDescription, VR.ST, roiDescription);
      doSSRoi.putFloat(Tag.ROIVolume,       VR.DS, roiVolume);
      doSSRoi.putString(Tag.ROIGenerationAlgorithm, VR.CS, roiGenerationAlgorithm);
      doSSRoi.putString(Tag.ROIGenerationDescription, VR.LO, roiGenerationDescription);
      */          
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0039).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputROIContourSequence(DicomObject odo)
   {
      DicomElement seqRoiContour = odo.putSequence(Tag.ROIContourSequence);
      DicomObject  doRoiContour  = new BasicDicomObject();
      doRoiContour.setParent(odo);
      seqRoiContour.addDicomObject(doRoiContour);
      
      // MRIW_RECORD result set files consist of only one ROI.
      doRoiContour.putInt(Tag.ReferencedROINumber, VR.IS, 1);
      
      // The result set file does not specify any aspects of the ROI presentation.
      // doRoiContour.putInts(Tag.ROIDisplayColor, VR.IS, roiDisplayColour);
            
      DicomElement seqContour = doRoiContour.putSequence(Tag.ContourSequence);     
      DicomObject  doContour  = new BasicDicomObject();
      doContour.setParent(doRoiContour);
      seqContour.addDicomObject(doContour);
      doContour.putInt(Tag.ContourNumber, VR.IS, 1);
      
      DicomElement seqContourImage = doContour.putSequence(Tag.ContourImageSequence);      
      DicomObject  doContourImage  = new BasicDicomObject();
      doContourImage.setParent(doContour);
      seqContourImage.addDicomObject(doContourImage);
      doContourImage.putString(Tag.ReferencedSOPClassUID,    VR.UI, imageSOPClassUID);
      doContourImage.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inp.dynSOPInstanceUIDs.get(0));
      
      doContour.putString(Tag.ContourGeometricType,      VR.CS, "CLOSED_PLANAR");
      doContour.putFloat( Tag.ContourSlabThickness,      VR.DS, Float.valueOf(sliceThickness));

      // doContour.putFloats(Tag.ContourOffsetVector,  VR.DS, offsetVector);
                  
      int n = con.roiX.size();
      doContour.putInt(Tag.NumberOfContourPoints, VR.IS, n);
      if (n != 0)
      {
         float[] contPoints = new float[n*3];
         for (int i=0; i<n; i++)
         {
            float[] c = ContourRendererHelper.convertFromImageToPatientCoords(
                 con.roiX.get(i), con.roiY.get(i), topLeftPos, dirCosines, pixelSpacing);
            for (int j=0; j<3; j++)
               contPoints[i*3 + j] = c[j];
         }
         doContour.putFloats(Tag.ContourData, VR.DS, contPoints);
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0080).
    * Despite the fact that MRIW_RECORD provides no metadata whatsoever to place in
 this sequence, this is a mandatory tag for structure sets.
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputRTROIObservationsSequence(DicomObject odo)
   {
      DicomElement seqRtRoiObs = odo.putSequence(Tag.RTROIObservationsSequence);
      DicomObject  doRtRoiObs = new BasicDicomObject();
      doRtRoiObs.setParent(odo);
      seqRtRoiObs.addDicomObject(doRtRoiObs);
      doRtRoiObs.putInt(Tag.ObservationNumber,       VR.IS, 1);
      doRtRoiObs.putInt(Tag.ReferencedROINumber,     VR.IS, 1);
      
      // DICOM Type 2 variables - must be present, but can be empty.
      doRtRoiObs.putString(Tag.RTROIInterpretedType, VR.CS, "");
      doRtRoiObs.putString(Tag.ROIInterpreter,       VR.CS, "");       
   }
   
   
   /**
    * Retrieve various additional details from the base images for which the
 MRIW_RECORD data were generated.
    * This is a non-trivial task, as these are not present directly in the
 MRIW_RECORD file being parsed. However, the information is valuable and worth
 the effort to extract.
    * @return the slice location as a String
    */
   protected void getDICOMParameters()
   {     
      // Get the file list for the the reference scan previously identified,
      // which should be the first entry in XNATScanID.
      XNATRESTToolkit  xnrt = new XNATRESTToolkit(xnprf);
      String           RESTCommand;
      Vector2D<String> result;
      String           dynScanID = new ArrayList<String>(XNATScanID).get(0);
      
      try
      {
         RESTCommand = "/data/archive/projects/"    + XNATProjectID
                                  + "/subjects/"    + XNATSubjectID
                                  + "/experiments/" + XNATRefExperimentID
                                  + "/scans/"       + dynScanID
                                  + "/files?format=xml";
         result = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (XNATException exXNAT)
      {
         logger.warn("Couldn't get a file list for scan " + dynScanID
                 + " while trying to extract required DICOM header information.\n"
                 + exXNAT.getMessage());
         return;
      }
      
      
      // Unfortunately, the lowest level in the XNAT hierarchy is the scan.
      // To gain access to parameters that change within the scan, such as the
      // slice position, there is no substitute for downloading each file
      // individually.  
      for (int i=0; i<result.size(); i++)
      {        
         // Note that not all of the images are DICOM files. Some in the XNAT
         // archive will be snapshots/thumbnails. So ignore these.
         if (result.atom(3, i).equals("DICOM"))
         {
            String filename = result.atom(0, i);      
            RESTCommand = "/data/archive/projects/"    + XNATProjectID
                                     + "/subjects/"    + XNATSubjectID
                                     + "/experiments/" + XNATExperimentID
                                     + "/scans/"       + dynScanID
                                     + "/files/"       + filename;
            
            DicomObject bdo = getDicomObjectFromXNATFile(RESTCommand, filename);
            
            String SOPInstanceUID = bdo.getString(Tag.SOPInstanceUID);
            if (inp.dynSOPInstanceUIDs.get(0).equals(bdo.getString(Tag.SOPInstanceUID)))
            {
               sliceLocation       = bdo.getString(Tag.SliceLocation);
               sliceThickness      = bdo.getString(Tag.SliceThickness);
               frameOfReferenceUID = bdo.getString(Tag.FrameOfReferenceUID);
               studyDescription    = bdo.getString(Tag.StudyDescription);
               imageSOPClassUID    = bdo.getString(Tag.SOPClassUID);
               pixelSpacing        = bdo.getFloats(Tag.PixelSpacing);
               dirCosines          = bdo.getFloats(Tag.ImageOrientationPatient);
               topLeftPos          = bdo.getFloats(Tag.ImagePositionPatient);
               
               return;
            }
         }
      }
      
      
      // If we get to here then there was no match.
      logger.warn("Couldn't extract DICOM parameters, because there was no matching\n"
                  + "SOPInstanceUID between the MRIW result set file and the XNAT\n"
                  + " database. This shouldn't happen.");
   }
   
   
   
   /**
    * Given an XNAT REST command representing a file, retrieve the corresponding
    * DCM4CHE DicomObject. This simply takes some ugly code out of the preceding
    * function.
    * @param RESTCommand
    * @param filename
    * @return the DicomObject, or null if there is any Error or Exception 
    */
   private DicomObject getDicomObjectFromXNATFile(String RESTCommand, String filename)
   {
      XNATRESTToolkit     xnrt = new XNATRESTToolkit(xnprf);
      BufferedInputStream bis;
      try
      {
         bis = new BufferedInputStream(xnrt.RESTGetFileAsStream(RESTCommand));
      }
      catch (XNATException exXNAT)
      {
         logger.warn("Couldn't download DICOM file " + filename
                  + " while trying to extract the slice location.\n"
                  + exXNAT.getMessage());
         return null;
      }
      DicomObject bdo     = new BasicDicomObject();
      boolean     success = false;
      try
      {
         DicomInputStream  dis = new DicomInputStream(bis);

         try
         {
            dis.readDicomObject(bdo, -1);

            String s = bdo.getString(Tag.SOPInstanceUID);
            success = (s != null);
         }

         // The full code dealing explicitly with all the different types of error
         // is in imageUtilities.DICOMLoader. Here, we don't care!
         catch (Exception ex)
         {
            logger.warn("While trying to extract the slice location, DCM4CHE generated"
                        + "an exception while trying to deal with file "
                        + filename + ".\n" + ex.getMessage());
            return null;
         }

         catch (Error e)
         {
            logger.warn("While trying to extract the slice location, DCM4CHE generated"
                        + "an serious error while trying to deal with file "
                        + filename + ".\n" + e.getMessage());
            return null;
         }

         finally
         {
            dis.close();
         }
      }
      catch (IOException ex)
      {
         logger.warn("While trying to extract the slice location, DCM4CHE could not"
                     + "open the file " + filename + ".");
         return null;
      }
      
      return bdo;
   }
   

   
   /**
    * Extract the first match for the element/attribute combination specified
    * by the arguments. Call this routine only when we are entitled to assume
 that there should be only one element, because this is the model for MRIW_RECORD
 result set files.
    * @param element
    * @param attr
    * @return
    * @throws XMLException 
    */
   private String getAttr(String element, String attr) throws XMLException
   {
      String[] s = XMLUtilities.getAttribute(doc, XNATns, element, attr);
      if (s == null) return null;
      else return s[0];
   }
   
   
   private Boolean getAttrAsBoolean(String element, String attr)
                   throws XMLException, DataFormatException
   {
      return XMLUtilities.getFirstAttributeAsBoolean(doc, XNATns, element, attr);
   }
   
   
   private Integer getAttrAsInteger(String element, String attr)
           throws XMLException, NumberFormatException
   {
      return XMLUtilities.getFirstAttributeAsInteger(doc, XNATns, element, attr);
   }
   
   
   
   private Float getAttrAsFloat(String element, String attr)
           throws XMLException, NumberFormatException
   {
      return XMLUtilities.getFirstAttributeAsFloat(doc, XNATns, element, attr);
   }
   
   
   private String getElementText(String element) throws XMLException
   {
      String[] s = XMLUtilities.getElementText(doc, XNATns, element);
      if (s == null) return null;
      else return s[0];
   }
   
   
   
   private String getFirstXPathResult(String xpathExpr) throws XMLException
   {
      return XMLUtilities.getFirstXPathResult(doc, XNATns, xpathExpr);
   }
   
   
   private ArrayList<String> getXPathResult(String xpathExpr) throws XMLException
   {
      return XMLUtilities.getXPathResultA(doc, XNATns, xpathExpr);
   }
   
   
   private SortedMap<String, String> getKeyValue(String element) throws XMLException
   {
      SortedMap<String, String> sm = null;
      
      // Note that earlier versions of MRIW_RECORD used "keyValue" as the name of the
      // element containing the key-value pair, but later versions use "key-value".
      sm = XMLUtilities.getKeyValue(doc, XNATns, element, "key-value", "key", "value");
      
      if (sm == null)
         sm = XMLUtilities.getKeyValue(doc, XNATns, element, "keyValue", "key", "value");
      
      return sm;
   }
   
   
   private ArrayList<Float> getFloatArrayListFromElementCSV(String element)
           throws XMLException, NumberFormatException
   {
      // N.B. We assume here that there is at most one matching element
      // in a valid MRIW_RECORD file.
      return XMLUtilities.getFloatArrayListFromElementCSV(doc, XNATns, element, ",");
   }
   
   
    private ArrayList<String> getStringArrayListFromElementCSV(String element)
           throws XMLException, NumberFormatException
   {
      // N.B. We assume here that there is at most one matching element
      // in a valid MRIW_RECORD file.
      return XMLUtilities.getStringArrayListFromElementCSV(doc, XNATns, element, ",");
   }
   
   
   private ArrayList<MRIWDataRecord> getDataRecordsForElement(String element, int encoding)
           throws XMLException, NumberFormatException, DataFormatException
   {
      ArrayList<MRIWDataRecord> result = new ArrayList<MRIWDataRecord>();
      
      NodeList elementNodes = XMLUtilities.getElement(doc, XNATns, element);
      if (elementNodes == null) return null;
      
      ArrayList<Integer> x = XMLUtilities.getAttributeAsIntegerA(
                              elementNodes.item(0), XNATns, "data-record", "x");
      
      ArrayList<Integer> y = XMLUtilities.getAttributeAsIntegerA(
                              elementNodes.item(0), XNATns, "data-record", "y");
      
      ArrayList<String>  s = XMLUtilities.getElementTextA(
                              elementNodes.item(0), XNATns, "data-record");
      
      for (int i=0; i<x.size(); i++)
      {
         MRIWDataRecord dr = new MRIWDataRecord();
         dr.x    = x.get(i);
         dr.y    = y.get(i);
         dr.data = new ArrayList<Float>();
         
         if (encoding == CSV)
         {
            String[] sa = s.get(i).split(",");
            if (sa == null)
            {
               logger.error("Invalid format for MRIW data-record " + i +
                                                         " at element " + element);
               throw new DataFormatException(DataFormatException.MRIW_RECORD,
                                             "record " + i + " at element " + element);
            }

            
            for (int j=0; j<sa.length; j++)
            {
               try
               {   
                  dr.data.add(new Float(sa[j]));
               }
               catch (NumberFormatException exNF)
               {
                  logger.error("Invalid number format for entry " + j + " of MRIW"
                                 + " data-record " + i + " at element " + element);
                  throw new NumberFormatException("Invalid format for entry " + j
                  + " of MRIW" + " data-record " + i + " at element " + element);
               }
            }
         }
         
         if (encoding == BASE64)
         {
            // TODO BASE64 encoding for MRIW_RECORD data-record
         }
         
         result.add(dr);
      }
      
      return result;
   }
   
   
   private ArrayList<MRIWMap> getMapsForElement(String element, int encoding)
           throws XMLException, NumberFormatException, DataFormatException
   {
      ArrayList<MRIWMap> result = new ArrayList<MRIWMap>();
      
      NodeList elementNodes = XMLUtilities.getElement(doc, XNATns, element);
      if (elementNodes == null) return null;
      
      ArrayList<String> name = XMLUtilities.getAttributeA(
                                   elementNodes.item(0), XNATns, "map", "name");
      
      ArrayList<String> type = XMLUtilities.getAttributeA(
                                   elementNodes.item(0), XNATns, "map", "type");
      
      ArrayList<String> s    = XMLUtilities.getElementTextA(
                                   elementNodes.item(0), XNATns, "map");
      
      for (int i=0; i<name.size(); i++)
      {
         MRIWMap m = new MRIWMap();
         m.mapName = name.get(i);
         if (type != null) m.mapType = type.get(i);
         
         if (encoding == CSV) 
         {
            String[] sa = s.get(i).split(",");
            if (sa == null)
            {
               logger.error("Invalid format for MRIW map " + i +
                                                         " at element " + element);
               throw new DataFormatException(DataFormatException.MRIW_MAP,
                                             i + " at element " + element);
            }

            m.mapData = new ArrayList<Float>();
            for (int j=0; j<sa.length; j++)
            {
               try
               {   
                  m.mapData.add(new Float(sa[j]));
               }
               catch (NumberFormatException exNF)
               {
                  logger.error("Invalid number format for entry " + j + " of MRIW"
                                 + " map " + i + " at element " + element);
                  throw new NumberFormatException("Invalid format for entry " + j
                  + " of MRIW" + " map " + i + " at element " + element);
               }
            }
         }
         
         if (encoding == BASE64)
         {
            /* TODO BASE64 encoding for MRIW_RECORD maps */
         }
         
         result.add(m);
      }
      
      return result;
   }
 
   
   /**
    * Get the keys from an MRIW_RECORD key-value Map as single comma-delimited String.
    * Although XNAT has a mechanism for storing key-value pairs as additional
    * parameters, this makes searching difficult. The rationale for this method
    * of storage is that we can search the a keys element in XNAT using a
    * PostgreSQL search with regular experession.
    * @param keyValue
    * @return key part of an MRIW_RECORD key-value Map as a single comma-delimited String
    */
   public String getMRIWKeysAsString(SortedMap<String, String> keyValue)
   {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, String> entry : keyValue.entrySet())
         sb.append(entry.getKey()).append(",");
      
      if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
      
      return sb.toString();
   }
   

   
/**
    * Get the values from an MRIW_RECORD key-value Map as single comma-delimited String.
    * Note that the same method of iterating through the keyValue Map is used as
    * for the keys to ensure that the ordering is the same.
    * @param keyValue
    * @return key part of an MRIW_RECORD key-value Map as a single comma-delimited String
    */
   public String getMRIWValuesAsString(SortedMap<String, String> keyValue)
   {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, String> entry : keyValue.entrySet())
         sb.append(entry.getValue()).append(",");
 
      if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
      
      return sb.toString();
   }
   
   
   /**
    * Get the names of the MRIW_RECORD computed maps present as a single,
 comma-delimited String
    * @param mapList
    * @return 
    */
   public String getMRIWMapNamesAsString(ArrayList<MRIWOutput.MRIWMap> mapList)
   {
      StringBuilder sb = new StringBuilder();

      for (MRIWOutput.MRIWMap map : mapList) sb.append(map.mapName).append(",");
 
      if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
      
      return sb.toString();
   }
   
}
