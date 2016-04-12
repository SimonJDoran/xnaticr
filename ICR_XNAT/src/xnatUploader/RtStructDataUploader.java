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
* Java class: RtStructDataUploader.java
* First created on Feb 23, 2016 at 9:36:36 AM
* 
* Object for uploading files to XNAT that conform to DICOM's
* RT-STRUCT format
*********************************************************************/

package xnatUploader;

import configurationLists.DAOSearchableElementsList;
import dataRepresentations.dicom.ContourImage;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import dataRepresentations.dicom.RoiContour;
import dataRepresentations.dicom.RtReferencedSeries;
import dataRepresentations.dicom.RtReferencedStudy;
import dataRepresentations.dicom.RtStruct;
import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.InvestigatorList.Investigator;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Platform;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Program;
import dataRepresentations.xnatSchema.Provenance.ProcessStep;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Library;
import dataRepresentations.xnatSchema.RoiDisplay;
import dataRepresentations.xnatSchema.Scan;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrRoiSetDataMdComplexType;
import xnatRestToolkit.XnatResource;

public class RtStructDataUploader extends DataUploader
{
	private DicomObject         bdo;
	private RtStruct            rts;
  	private Set<String>         studyUidSet       = new LinkedHashSet<>();
	private Set<String>         seriesUidSet      = new LinkedHashSet<>();
	private Set<String>         sopInstanceUidSet = new LinkedHashSet<>();
	private Map<String, String> fileSopMap        = new HashMap<>();
	private Map<String, String> fileScanMap       = new HashMap<>();	
	
	public RtStructDataUploader(XNATProfile xnprf)
	{
		super(xnprf);
	}
	
	@Override
   public void clearFields(MetadataPanel mdsp)
   {
      mdsp.populateJTextField("Label", "", true);
      mdsp.populateJTextField("Note",  "", true);
   }
	

   @Override
   public boolean readFile()
   {
      bdo = new BasicDicomObject();
      try
      {
         BufferedInputStream bis
            = new BufferedInputStream(new FileInputStream(uploadFile));
         DicomInputStream dis = new DicomInputStream(bis);
         dis.readDicomObject(bdo, -1);
      }
      catch (IOException exIO)
      {
         errorOccurred = true;
         errorMessage  = "Unable to open selected file. \n" + exIO.getMessage();
         return false;
      }

      return true;
   }
	
	
	@Override
   public boolean parseFile()
   {
		try
		{
			rts = new RtStruct(bdo);
		}
		catch (DataFormatException exDF)
		{
			errorOccurred = true;
         errorMessage  = "Unable to parse file. \n" + exDF.getMessage();
			return false;
		}
		
		// Generate a single Set of all studies referenced for later use
		// and similarly for all series and SOPInstances referenced.
		for (ReferencedFrameOfReference rfor : rts.structureSet.referencedFrameOfReferenceList)
		{
			for (RtReferencedStudy rrs : rfor.rtReferencedStudyList)
			{
				studyUidSet.add(rrs.referencedSopInstanceUid);
				for (RtReferencedSeries rrse : rrs.rtReferencedSeriesList)
				{
					seriesUidSet.add(rrse.seriesInstanceUid);
					for (ContourImage ci : rrse.contourImageList)
					{
						sopInstanceUidSet.add(ci.referencedSopInstanceUid);
					}
				}
			}
		}
		
		return areDependenciesInDatabase();
	}
	
	
	/**
    * Check that all the relevant studies, series and SOPInstances have been
    * loaded into the XNAT database.
    */
   protected boolean areDependenciesInDatabase()
   {
      /* This operation is complicated by the facts that:
         - a structure set can reference more than one DICOM study;
          
         - the study can appear in more than one XNAT project;
      
         - there is nothing in the XNAT structure to stop the DICOM images
           being uploaded for more than one XNAT subject within a single project;
      
         - even for a single XNAT subject, it is possible to create multiple
           sessions using the same DICOM data.
      
         Normally, the reason we are parsing an RTStruct_old is to upload a given
         file to a particular XNAT Session and so some disambiguation is needed.
         This is achieved by splitting the parsing for dependencies into two
         parts:
         
         1. We assume that the project has already been specified, so that we
            can avoid searching through the entire database. We find all the
            information we can about matching DICOM sessions already uploaded
            within that project, and then work out how to populate the subject
            and sessions possibilities for disambiguation. If there is no
            ambiguity or either or both of the subject or sessions has been
            specified already to remove the ambiguity, then we move directly on
            to 2 below.
         
         2. Using the specified values of XNATProjectID, XNATSubjectID and
            XNATExperimentID, we generate all the other required information to
            determine whether all the individual scans required are present in
            the database.
      */
 
      // We need to generate this information only once.
      if (ambiguousSubjExp.size() == 0) checkForSubjectSessionAmbiguities();
		if (!errorMessage.isEmpty()) return false;
      
		checkForScansInDatabase();
		return (errorMessage.isEmpty()); 
   }
	
   
   protected void checkForSubjectSessionAmbiguities()
   {
      try
      {         
         // Find information on all the studies in the project.
         // This could be time-consuming for a large database.
         RESTCommand = "/data/archive/projects/" + XNATProject + "/experiments"
                       + "?xsiType=xnat:imageSessionData"
                       + "&columns=xnat:imageSessionData/UID,xnat:imageSessionData/label,xnat:imageSessionData/subject_ID"
                       + "&format=xml";
         result      = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (XNATException exXNAT)
      {
         errorMessage = "Problem checking for subject and session ambiguities: "
					          + exXNAT.getMessage();
			return;
      }
      
      // Check that the DICOM studies from the uploaded RT-STRUCT file are present
      // in the database and add the relevant XNAT experiment and subject IDs
      // to the lists for disambiguation.		
      for (String studyUid : studyUidSet)
      {
         if (!result.columnContains(1, studyUid))
			{
            errorMessage = "The DICOM study with UID " + studyUid + "\n"
                            + " is referenced by the file you are loading,"
                            + " but it is not yet in the database.\n"
                            + "Please ensure that all files on which this"
                            + " structure set is dependent are already loaded into XNAT.";
            return;
			}
            
         for (int j=0; j<result.size(); j++)
         {
            if (result.atom(1, j).equals(studyUid))
            {
               String expID    = result.atom(0, j);
               String expLabel = result.atom(2, j);
               String subjID   = result.atom(3, j);
               String subjLabel;
               
               try
               {
                  // Retrieve the subject label for a given subject ID.
                  RESTCommand    = "/data/archive/projects/" + XNATProject
                                   + "/subjects/"            + subjID
                                   + "?format=xml";
                  Document resultDoc = xnrt.RESTGetDoc(RESTCommand);                 
                  String[] attrs = XMLUtilities.getAttribute(resultDoc, XnatNs, "xnat:Subject", "label");
                  subjLabel      = attrs[0];
               }
               catch (XMLException | XNATException ex)
               {
                  errorMessage = "Problem retrieving the subject label for subject ID" + subjID
								          + ": " + ex.getMessage();
						return;
               }
               
               if (!ambiguousSubjExp.containsKey(subjID))
                  ambiguousSubjExp.put(subjID, new AmbiguousSubjectAndExperiment());
               
               AmbiguousSubjectAndExperiment ase = ambiguousSubjExp.get(subjID);
               ase.experimentIDs.add(expID);
               ase.experimentLabels.add(expLabel);
               ase.subjectLabel = subjLabel;
            }
         }
      }
      
      // Simply choose the first entry as the default.
      for (String key : ambiguousSubjExp.keySet())
      {
         XNATSubjectID    = key;
         XNATExperimentID = ambiguousSubjExp.get(key).experimentIDs.get(0);
      }         
   }
   
   
   public void checkForScansInDatabase()
   {      
      XNATScanIdList = new ArrayList<>(); 
      String[][] parseResult;
		
		try
      {
         RESTCommand = "/data/archive/projects/" + XNATProject
                       + "/subjects/"            + XNATSubjectID
                       + "/experiments/"         + XNATExperimentID
                       + "?format=xml";
         Document resultDoc   = xnrt.RESTGetDoc(RESTCommand);
         parseResult = XMLUtilities.getAttributes(resultDoc, XnatNs, "xnat:scan",
                                             new String[] {"ID", "UID"});
      }
      catch (XMLException | XNATException ex)
      {
         errorMessage = "Problem retrieving experiment list for subject " + XNATSubjectID
					          + ": " + ex.getMessage();
			return;
      }
      
      // Now process all the seriesUIDs found when parsing the input DICOM file. 
      for (String seriesUid : seriesUidSet)
      {
         boolean present = false;
         for (int j=0; j<parseResult.length; j++)
         {
            // Not all of the returned values correspond to scans. Some might be
            // assessors, with no SOPInstanceUID. These need to be screened out.
            if (parseResult[j][1] != null)
            {
               if (parseResult[j][1].equals(seriesUid))
               {
                  present = true;
                  XNATScanIdList.add(parseResult[j][0]);
               }
            }
         }
      
         if (!present)
			{
				errorMessage = "The DICOM series with UID " + seriesUid + "\n"
                           + " is referenced by the file you are loading,"
                           + " but it is not yet in the database.\n"
                           + "Please ensure that all files on which this"
                           + "structure set is dependent are already loaded into XNAT.";
				return;
			}
      }
      
      // We need a list of the actual data files in the repository
      // that are referenced, to go in the "in" section of qcAssessmentData.
      // See the Class DICOMFileListWorker for an example of how to do this
      // both if the files are local or remote. Here, for simplicity, I don't
      // assume anything and use the REST method whether the files are local
      // or remote.       
      for (String scanId : XNATScanIdList)
      {
         try
         {
            RESTCommand = "/data/archive/projects/"    + XNATProject
                             + "/subjects/"            + XNATSubjectID
                             + "/experiments/"         + XNATExperimentID
                             + "/scans/"               + scanId
                             + "/resources/DICOM?format=xml";
            Document resultDoc   = xnrt.RESTGetDoc(RESTCommand);
            parseResult = XMLUtilities.getAttributes(resultDoc, XnatNs, "cat:entry",
                                                     new String[] {"URI", "UID"});
         }
         catch(XNATException | XMLException ex)
         {
            errorMessage = "Problem finding correct image data files in the repository for subject "
				                  + XNATSubjectID + ": " + ex.getMessage();
				return;
         }
         
         // Cater for the obscure case where parseResult comes back null. This
         // happened to me after I had (manually) screwed up the data repository.
         if (parseResult == null)
			{
				errorMessage = "There are no relevant DICOM image files. This might be an \n"
                        + " inconsistent condition in the repository. Please contact \n"
                        + " the system administrator.";
				return;
			}

         for (int j=0; j<parseResult.length; j++)
         {
            if (sopInstanceUidSet.contains(parseResult[j][1]))
               fileSopMap.put(parseResult[j][0], parseResult[j][1]);
            fileScanMap.put(parseResult[j][0], scanId);
         }
      }
      
      
      
 
   }

	
	/**
    * Update the parsing of the file to take into account the most
    * recent selection of either subject or experiment labels from the
    * JCombo boxes in the user interface.
    */
   @Override
   public void updateParseFile()
   {
//      rts.XNATExperimentID = XNATExperimentID;
//      rts.XNATSubjectID    = XNATSubjectID;
//      
//		ArrayList<String> issues = new ArrayList<>();
//		rts.checkForScansInDatabase(issues);
//		
//		// This error should not happen, since both XNATExperimentID and
//		// XNATSubjectID should have been the result of a previous scan of
//		// database for appropriately matching files.
//      if (!issues.isEmpty())
//		{
//			StringBuilder sb = new StringBuilder();
//			for (String issue : issues) sb.append(issue).append("\n");
//			
//         throw new RuntimeException("Error in updateParseFile: " + sb.toString() );
//      }
   }
           
	
	/**
    * Uploading data to XNAT is a two-stage process. First the data file
    * is placed in the repository, then the metadata are placed in the SQL
    * tables of the PostgreSQL database. This method attempts the repository
    * upload.
    * 
    * Note that we have to override the method in the parent class DataUploader.
    * Loading an RT-STRUCT file is special because not only do we create a
    * set-of-ROIs element in the database (icr:roiSetData), but we also create
    * all the individual ROIs as separate icr:roiData objects.
    * @throws Exception
    */
   @Override
   public void uploadMetadata() throws Exception
   {
      errorOccurred = false;
   }
	
	
	/**
	 * Get the list of files containing the input data used in the creation of this
	 * XNAT assessor. 
	 * @return ArrayList of String file names
	 */
   protected ArrayList<String> getInputCatEntries()
	{
		ArrayList<String>	fileURIs	= new ArrayList<>();
//		Set<String>			ks			= rts.fileSOPMap.keySet();
//      for (String s : ks) fileURIs.add(rts.fileSOPMap.get(s));
		
		return fileURIs;
	}
	
	
	
	@Override
	public void createPrimaryResource()
	{
		StringBuilder description = new StringBuilder();
		description.append("DICOM RT-STRUCT file created by node ")
					  .append(rts.generalEquipment.stationName)
					  .append(" of type ")
				     .append(rts.generalEquipment.manufacturer)
				     .append(" ")
				     .append(rts.generalEquipment.modelName)
				     .append(" using software ")
				     .append(rts.generalEquipment.softwareVersions);

		primaryResource = new XnatResource(uploadFile,
		                                   "out",
		                                   "RT-STRUCT",
				                             "DICOM",
		                                   "EXTERNAL",
		                                   description.toString(),
				                             uploadFile.getName());
	}
   
   
   
   /**
    * Create additional thumbnail files for upload with the DICOM-RT structure set.
    */
   @Override
   public void createAuxiliaryResources()
   {
      // In the first instance, the only auxiliary file needed is the
		// input catalogue, since the referenced ROI objects already contain
		// the required thumbnails.
      // TODO: Consider whether some composite visualisation is needed to
      // summarise all the ROI_old's making up the ROISet object.
	//	createInputCatalogue("DICOM", "RAW", "referenced contour image");
   }
	
	@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrRoiSetDataMdComplexType roiSet  = new IcrRoiSetDataMdComplexType();
		
		roiSet.setOriginalUid(rts.sopCommon.sopInstanceUid);
		roiSet.setOriginalDataType("RT-STRUCT");
		roiSet.setOriginalLabel(rts.structureSet.structureSetLabel);
		roiSet.setOriginatingApplicationName(rts.generalEquipment.modelName);
		
		final String sep = " | ";
		StringBuilder sb = new StringBuilder();
		for (String s : rts.generalEquipment.softwareVersions) sb.append(s).append(sep);
		roiSet.setOriginatingApplicationVersion(sb.toString());
		
		roiSet.setNRois(rts.structureSet.structureSetRoiList.size());
		
		List<RoiDisplay> rdl = new ArrayList<>();
		for (RoiContour rc : rts.roiContourList)
		{
			// Nulls here because RT-STRUCTS contain only a subset of the
			// information other formats can carry about the ROI presentation.
			RoiDisplay rd = new RoiDisplay(Integer.toString(rc.referencedRoiNumber),
				                            null, Arrays.toString(rc.roiDisplayColour),
				                            null, null, null);
			rdl.add(rd);
		}
		roiSet.setRoiDisplayList(rdl);
		
		roiSet.setStructureSetLabel(rts.structureSet.structureSetLabel);
		roiSet.setStructureSetName(rts.structureSet.structureSetName);
		roiSet.setStructureSetDescription(rts.structureSet.structureSetDescription);
		roiSet.setStructureInstanceNumber(rts.structureSet.instanceNumber);
		roiSet.setStructureSetDate(rts.structureSet.structureSetDate);
		roiSet.setStructureSetTime(rts.structureSet.structureSetTime);
		roiSet.setReferencedFrameOfReferenceList(rts.structureSet.referencedFrameOfReferenceList);
		
		// IcrRoiSetDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// roiSet.setType();  Not currently sure what should go here.
		roiSet.setXnatSubjId(XNATSubjectID);
		roiSet.setDicomSubjName(rts.patient.patientName);
		
		// Although the full version of Scan, including scan and slice image
		// statistics is implemented, this is overkill for the RT-STRUCT and
		// the only part of scan for which information is available is the
		// list of scan IDs.
		List<Scan> lsc = new ArrayList<>();
		for (String id : seriesUidSet)
		{
			Scan sc = new Scan();
			sc.id = id;
			lsc.add(sc);
		}
		roiSet.setScanList(lsc);
		
		// IcrGenericImageAssessmentDataMdComplexType inherits from XnatImageAssessorDataMdComplexType.
		
		// The "in" section of the assessor XML contains all files that were already
		// in the database at the time of upload, whilst the "out" section lists
		// the files that added at the time of upload, including those generated
		// automatically. In this, the only generated files are the snapshots, but
		// this information is already included in the separately uploaded ROI
		// metadata files and need not be duplicated here.
		List<AbstractResource> inList = new ArrayList<>();
		
		for (String filename : fileSopMap.keySet())
		{
			AbstractResource ar  = new AbstractResource();
			List<MetaField>  mfl = new ArrayList<>();
			mfl.add(new MetaField("filename",       filename));
			mfl.add(new MetaField("format",         "DICOM"));
			mfl.add(new MetaField("SOPInstanceUID", fileSopMap.get(filename)));
			ar.tagList = mfl;
			inList.add(ar);
		}
		
		List<AbstractResource> outList = new ArrayList<>();
		AbstractResource       ar      = new AbstractResource();
		List<MetaField>        mfl     = new ArrayList<>();
		mfl.add(new MetaField("filename", uploadFile.getName()));
		mfl.add(new MetaField("format",   "RT-STRUCT"));
		ar.tagList = mfl;
		outList.add(ar);
		
		roiSet.setInList(inList);
		roiSet.setOutList(outList);
		
		roiSet.setImageSessionId(XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		roiSet.setParamList(new ArrayList<AdditionalField>());
		
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		
 
		roiSet.setProvenance(retrieveProvenance());
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      roiSet.setId(XNATAccessionID);
      roiSet.setProject(XNATProject);
      
      StringBuilder versions = new StringBuilder();
		for (String s : rts.generalEquipment.softwareVersions) versions.append(s);
      roiSet.setVersion(versions.toString());
      
      roiSet.setLabel(rts.structureSet.structureSetLabel);
      roiSet.setDate(rts.structureSet.structureSetDate);
      roiSet.setTime(rts.structureSet.structureSetTime);
      roiSet.setNote(getStringField("Note"));
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator.
		roiSet.setInvestigator(new Investigator());      
      
      // Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = roiSet.createXmlAsRootElement();
		}
		catch (IOException | XMLException ex){}
		
		return metaDoc;
		
	}
   
   
   @Override
   public Provenance retrieveProvenance()
   {
      
		StringBuilder versions = new StringBuilder();
		for (String s : rts.generalEquipment.softwareVersions) versions.append(s);
		
		Program prog1      = new Program(rts.generalEquipment.manufacturer + " software",
		                           versions.toString(),
		                           null);
		
		Platform plat1     = new Platform(rts.generalEquipment.modelName, null);
		
		String timestamp1  = rts.structureSet.structureSetDate + "_"
				               + rts.structureSet.structureSetTime;
		
		String user1       = rts.rtRoiObservationList.get(0).roiInterpreter;
		
		String machine1    = rts.generalEquipment.stationName;
		
		// We don't have a compiler version, but we still need to specify it, as
		// the instance variables are accessed later. (Still needed even though the instance
		// variables are null themselves ...)
		ProcessStep.Compiler c1 = new ProcessStep.Compiler(null, null);
		
      // Even though  the library list is empty we still need to specify it, otherwise
		// a null pointer exception will pop up when we try to iterate through the list.
		List<Library> ll1  = new ArrayList<Library>();
				  
		ProcessStep ps1    = new ProcessStep(prog1, timestamp1, null, user1, machine1, plat1, c1, ll1);

		
		Platform plat2     = new Platform(null, null);
   
      Program prog2      = new Program("ICR XNAT DataUploader", version, null);
      
      String timestamp2  = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
      
      String user2       = System.getProperty("user.name");
      
      StringBuilder mac2 = new StringBuilder(System.getProperty("os.arch")).append(" ")
                             .append(System.getProperty("os.name")).append(" ")
                             .append(System.getProperty("os.version"));
    
		List<Library> ll2  = new ArrayList<Library>();
		
		ProcessStep.Compiler c2 = new ProcessStep.Compiler(null, null);
		
		ProcessStep ps2    = new ProcessStep(prog2, timestamp2, null, user2, mac2.toString(), plat2, c2, ll2);
		
      ArrayList<ProcessStep> stepList = new ArrayList<>();
		stepList.add(ps1);
      stepList.add(ps2);
      
      return new Provenance(stepList);
   }
	
	
	public String convertToDateTime(String date, String time) throws DataFormatException
   {
      String month;
      String day;
      String year;
      String hour;
      String minute;
      String second;
      
      try
      {
         month  = date.substring(4, 6);
         day    = date.substring(6, 8);
         year   = date.substring(0, 4);
         hour   = time.substring(0, 2);
         minute = time.substring(2, 4);
         second = time.substring(4, 6);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return new String(year + "-" + month + "-" + day
                        + "T" + hour + ":" + minute + ":" + second);
   }
   
   
   @Override
	public void updateVariablesForEditableFields.
	
	
	@Override
   public List<String> getEditableFields()
   {
      List<String> s = new ArrayList<>();
		s.add("Label");
		s.add("Note");
		
		return s;
   }
   
	
	@Override
   public List<String> getRequiredFields()
   {
      List<String> s = new ArrayList<>();
		s.add("Label");
		
		return s;
   }
   
   
   @Override
   public boolean rightMetadataPresent()
   {
      return (!getStringField("Label").equals("")) &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanIdList.equals(""));
   }
   
   
   @Override
   public String getRootElement()
   {
      return "ROISet";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:roiSetData";
   }
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }


}
