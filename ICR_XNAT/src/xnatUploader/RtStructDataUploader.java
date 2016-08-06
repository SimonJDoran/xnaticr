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

import dataRepresentations.dicom.ContourImage;
import dataRepresentations.dicom.ReferencedFrameOfReference;
import dataRepresentations.dicom.RoiContour;
import dataRepresentations.dicom.RtReferencedSeries;
import dataRepresentations.dicom.RtReferencedStudy;
import dataRepresentations.dicom.RtStruct;
import dataRepresentations.dicom.RtStructBuilder;
import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.Catalog;
import dataRepresentations.xnatSchema.CatalogEntry;
import dataRepresentations.xnatSchema.InvestigatorList.Investigator;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Platform;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Program;
import dataRepresentations.xnatSchema.Provenance.ProcessStep;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Library;
import dataRepresentations.xnatSchema.Resource;
import dataRepresentations.xnatSchema.RoiDisplay;
import dataRepresentations.xnatSchema.Scan;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.DicomXnatDateTime;
import generalUtilities.UidGenerator;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrRegionSetDataMdComplexType;
import xnatRestToolkit.XnatResource;

class RtStructDataUploader extends DataUploader
{
	private DicomObject        bdo;
	
	// These instance variables are public because they need to be accessed by
	// the RegionFromRtStructDataUploader class in the uploadMetadataAndCascade method.
	private RtStruct            rts;
  	private Set<String>         studyUidSet       = new LinkedHashSet<>();
	private Set<String>         seriesUidSet      = new LinkedHashSet<>();
	private Set<String>         sopInstanceUidSet = new LinkedHashSet<>();
	private Map<String, String> filenameSopMap;
	private Map<String, String> sopFilenameMap;
	private Map<String, String> filenameScanMap;
	private List<String>        assignedRegionIdList = new ArrayList<>();
	private int                 nRois;
   private String              originalDataType;
   private String              labelParent;
	
	RtStructDataUploader(XNATProfile xnprf)
	{
		super(xnprf);
	}
	
	@Override
   public void clearFields(MetadataPanel mdp)
   {
      mdp.populateJTextField("Label", "", true);
      mdp.populateJTextField("Note",  "", true);
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
			rts = (new RtStructBuilder()).buildNewInstance(bdo);
		}
		catch (DataFormatException exDF)
		{
			errorOccurred = true;
         errorMessage  = "Unable to parse file. \n" + exDF.getMessage();
			return false;
		}
		
		originalDataType = "RT-STRUCT";
		
		// Initially, the label of the XNAT assessor will be set to the same
		// as the structure set label, but this can be changed on the upload screen.
		if (!isBatchMode) labelTemplate = rts.structureSet.structureSetLabel;
      
		
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
		
		XnatDependencyChecker xnd = new XnatDependencyChecker(xnprf, XNATProject,
		                                                      studyUidSet, seriesUidSet, sopInstanceUidSet);
		errorOccurred    = !xnd.areDependenciesInDatabase();
		XNATSubjectID    = xnd.getSubjectId();
		XNATExperimentID = xnd.getExperimentId();
		XNATScanIdSet    = xnd.getScanIdSet();
		filenameSopMap   = xnd.getFilenameSopMap();
		sopFilenameMap   = xnd.getSopFilenameMap();
		filenameScanMap  = xnd.getFilenameScanMap();
		ambiguousSubjExp = xnd.getAmbiguousSubjectExperiement();
		errorMessage     = xnd.getErrorMessage();
		
		return !errorOccurred;
	}
	
	
	

	
	/**
    * Update the parsing of the file to take into account the most
    * recent selection of either subject or experiment labels from the
    * JCombo boxes in the user interface.
    */
   @Override
   public void updateParseFile()
   {

// TODO: This can probably all be deleted now.      
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
    * See comment in the superclass method.
    * @throws XNATException
    */
   @Override
   public void uploadMetadataAndCascade()
			      throws XNATException, DataFormatException, IOException
   {
      errorOccurred = false;
		
		// ----------------------------------------------
      // Step 1: Upload the icr:regionSetData metadata.
      // ----------------------------------------------
      
      date = rts.sopCommon.instanceCreationDate;
      time = rts.sopCommon.instanceCreationTime;
      
      if (XNATAccessionID == null)
         XNATAccessionID = getRootElement() + "_" + UidGenerator.createShortUnique();
      
      if (labelTemplate == null) labelTemplate = labelParent;
      label = expandLabelTemplate(labelTemplate);
		
		// Create separate accession IDs for all the individual ROI's.
		nRois = rts.structureSet.structureSetRoiList.size();
		if (assignedRegionIdList.isEmpty())
		{
			for (int i=0; i<nRois; i++)
				assignedRegionIdList.add("Region_" + UidGenerator.createShortUnique());
		}
		
      super.uploadMetadataAndCascade();
 
      // -------------------------------------------------------------
      // Step 2: Upload the icr:regionData metadata and data files for
      //         each ROI referred to by the structure set.
      // -------------------------------------------------------------    
      	
      for (int i=0; i<rts.structureSet.structureSetRoiList.size(); i++)
      {
         RegionFromRtStructDataUploader ru = new RegionFromRtStructDataUploader(xnprf);
         try
         {
            ru.setVersion(version);
            ru.setOriginalDataType(originalDataType);
            ru.setAccessionId(assignedRegionIdList.get(i));
				ru.setParentAccessionId(XNATAccessionID);
            ru.setRoiPositionInSSRoiSequence(i);
            ru.setSubjectId(XNATSubjectID);
				ru.setSubjectLabel(XNATSubjectLabel);
            ru.setExperimentId(XNATExperimentID);
				ru.setExperimentLabel(XNATExperimentLabel);
				ru.setParentProvenance(prov);
				ru.setParentRtStruct(rts);
            ru.setUploadFileParent(uploadFile);
            ru.setParentLabel(label);
            ru.setParentNRois(nRois);
            ru.setSopFilenameMap(sopFilenameMap);
            ru.setFilenameSopMap(filenameSopMap);
            ru.setFilenameScanMap(filenameScanMap);
            ru.setDate(date);
            ru.setTime(time);
            ru.setNote(note);
                    
				ru.uploadMetadataAndCascade();
            ru.uploadResourcesToRepository();
         }
         catch (Exception ex)
         {
            errorOccurred = true;
            errorMessage = "Problem uploading ROI data to XNAT.\n"
                           + ex.getMessage();
            throw new XNATException(XNATException.FILE_UPLOAD, ex.getMessage());
         }
      }
   }
	
	
	
	
	
	@Override
	public void createPrimaryResource()
	{
		if (primaryResource == null)
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
	}
   
      
   /**
    * Create additional thumbnail files for upload with the DICOM-RT structure set.
    */
   @Override
   public void createAuxiliaryResources()
   {
      createInputCatalogue();
      // TODO: Consider whether some composite visualisation is needed to
      // summarise all the icr:region instances making up the icr:regionSet instance.
   }
	
	@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrRegionSetDataMdComplexType regionSet  = new IcrRegionSetDataMdComplexType();
		
		regionSet.setOriginalUid(rts.sopCommon.sopInstanceUid);
		regionSet.setOriginalDataType(originalDataType);
		regionSet.setOriginalLabel(rts.structureSet.structureSetLabel);
		regionSet.setOriginatingApplicationName(rts.generalEquipment.manufacturer + " software");
		
		final String SEP = " | ";
      StringBuilder sb = new StringBuilder();
		for (String s : rts.generalEquipment.softwareVersions)
		{
			if (!(sb.toString().contains(s))) 
			{
			   if (sb.length() != 0) sb.append(SEP);
				if (s != null) sb.append(s);
			}
		}
		regionSet.setOriginatingApplicationVersion(truncateString(getDefaultIfEmpty(sb.toString()), 255));
		
		regionSet.setNRegions(rts.structureSet.structureSetRoiList.size());
		regionSet.setRegionIdList(assignedRegionIdList);
		regionSet.setStructureSetName(rts.structureSet.structureSetName);
		regionSet.setStructureInstanceNumber(rts.structureSet.instanceNumber);
		regionSet.setReferencedFrameOfReferenceList(rts.structureSet.referencedFrameOfReferenceList);
		
		// IcrRegionSetDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// regionSet.setType();  Not currently sure what should go here.
		regionSet.setXnatSubjId(XNATSubjectID);
		regionSet.setDicomSubjName(rts.patient.patientName);
		
		// Although the full version of Scan, including scan and slice image
		// statistics is implemented, this is overkill for the RT-STRUCT and
		// the only part of scan for which information is available is the
		// list of scan IDs. 
		Set<String> idSet = new HashSet<>();
		for (String filename : filenameSopMap.keySet()) idSet.add(filenameScanMap.get(filename));
		
		List<Scan> lsc = new ArrayList<>();
		for (String id : idSet)
		{
			Scan sc = new Scan();
			sc.id = id;
			lsc.add(sc);
		}
		regionSet.setScanList(lsc);
		
		// IcrGenericImageAssessmentDataMdComplexType inherits from XnatImageAssessorDataMdComplexType.
		
		// The "in" section of the assessor XML contains all files that were already
		// in the database at the time of upload, whilst the "out" section lists
		// the files that added at the time of upload, including those generated
		// automatically. The latter is automatically updated by XNAT and may be
      // set as empty here.
      inputCat = new Catalog();
      List<CatalogEntry> lce = new ArrayList<>();
      for (String filename : filenameSopMap.keySet())
		{
			CatalogEntry ce   = new CatalogEntry();
			ce.name           = filename;
         ce.id             = filenameSopMap.get(filename);
         ce.format         = "DICOM";
         ce.content        = "IMAGE";
         lce.add(ce);
      }
      CatalogEntry ce      = new CatalogEntry();
      ce.name              = (uploadFile == null) ? "GENERATED" : uploadFile.getName();
      // ce.id = ?? It's not easy to retrieve the SOPInstanceUID in this context.
      ce.format            = "DICOM";
      ce.content           = "RT-STRUCT";
      lce.add(ce);
      
      inputCat.entryList   = lce;
      inputCat.id          = "INPUT_FILES";
      inputCat.description = "catalogue of input files for assessor " + XNATAccessionID;
      
      Resource         r   = new Resource();
      List<MetaField>  mfl = new ArrayList<>();
		r.tagList            = mfl;
      r.uri                = XNATAccessionID+"_input.xml";
      r.format             = "INPUT_CATALOGUE";
      r.fileCount          = lce.size();
      r.description        = "Input data for assessor " + XNATAccessionID;
      Provenance       p   = new Provenance();
      p.stepList           = new ArrayList<>();
      r.prov               = p;
      
      List<Resource> inList = new ArrayList<>();
		inList.add(r);
	
      regionSet.setInList(inList);  // should be inList
      regionSet.setOutList(new ArrayList<>()); // should be outList
		
		regionSet.setImageSessionId(XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		regionSet.setParamList(new ArrayList<AdditionalField>());
		
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		
		prov = createProvenance();  // Used later, too.
		regionSet.setProvenance(prov);
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      regionSet.setId(XNATAccessionID);
      regionSet.setProject(XNATProject);
      
      //StringBuilder versions = new StringBuilder();
		//for (String s : rts.generalEquipment.softwareVersions) versions.append(s);
      //roiSet.setVersion(versions.toString());
      
		// Apparently the version XML element has to be an integer, so my ideal
		// version above is no use.
		regionSet.setVersion("1");
		
		regionSet.setLabel(label);
      
		regionSet.setDate(date);
      regionSet.setTime(time);
      regionSet.setNote(note);
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator.
		regionSet.setInvestigator(new Investigator());      
      
      // Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = regionSet.createXmlAsRootElement();
		}
		catch (IOException | XMLException ex)
		{
			// This really shouldn't happen, but the mechanism is there to handle
			// it if it does.
			errorOccurred = true;
			errorMessage  = ex.getMessage();
		}
		
		return metaDoc;
		
	}
   
   
   private String getDefaultIfEmpty(String src)
	{
		final String DEFAULT = "Unknown";
		
		if (src == null)   return DEFAULT;
		if (src.isEmpty()) return DEFAULT;
		return src;
	}
	
	@Override
   public Provenance createProvenance()
   {
      // A number of fields are mandatory in the birn.xsd provenance schema,
		// so provide defaults if they do not exist.
		final String DEFAULT = "Unknown";
		final String SEP     = " | ";
		
		// Provenance step 1 : source data
		
		StringBuilder sb    = new StringBuilder();
		for (String s : rts.generalEquipment.softwareVersions)
		{
			if (!(sb.toString().contains(s))) 
			{
			   if (sb.length() != 0) sb.append(SEP);
				if (s != null) sb.append(s);
			}
		}
      String versions = truncateString(getDefaultIfEmpty(sb.toString()), 255);
		
		Program                prog1    = new Program(getDefaultIfEmpty(rts.generalEquipment.manufacturer) + " software",
		                                              versions,
		                                              DEFAULT);
		
		Platform               plat1    = new Platform(getDefaultIfEmpty(rts.generalEquipment.modelName),
				                                         DEFAULT);
		
		// Note that ts1 has to be initialised with a valid default. 
		String ts1 ="1900-01-01T00:00:00";
		try
		{
			ts1 = DicomXnatDateTime.convertDicomToXnatDateTime(rts.structureSet.structureSetDate,
				                                                rts.structureSet.structureSetTime);
		}
		catch (DataFormatException exDF)
		{
			errorOccurred = true;
			errorMessage  = "Incorrect DICOM date format in structure set file";
		}
		
		String                 cvs1     = DEFAULT;		
		String                 user1    = getDefaultIfEmpty(rts.rtRoiObservationList.get(0).roiInterpreter);
		String                 mach1    = getDefaultIfEmpty(rts.generalEquipment.stationName);
		
		// We don't have a compiler version, but we still need to specify it, as
		// the instance variables are accessed later. (Still needed even though the instance
		// variables are null themselves ...)
		
		String                 compN1   = null;
		String                 compV1   = null;
		ProcessStep.Compiler   comp1    = new ProcessStep.Compiler(compN1, compV1);
		
      // Even though  the library list is empty we still need to specify it, otherwise
		// a null pointer exception will pop up when we try to iterate through the list.
		List<Library>          ll1      = new ArrayList<Library>();
				  
		ProcessStep            ps1      = new ProcessStep(prog1, ts1, cvs1, user1, mach1, plat1, comp1, ll1);
		

		// Provenance step 2: record transit through DataUploader
		
      Program                prog2    = new Program("ICR XNAT DataUploader", version, "None");
      
		Platform               plat2    = new Platform(System.getProperty("os.arch") + " " + System.getProperty("os.name"),
				                                         System.getProperty("os.version"));
   
      String                 ts2      = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
      
      String                 cvs2     = DEFAULT;
		
		String                 user2    = System.getProperty("user.name");
      
      String                 mach2;
		try
		{
			 InetAddress addr;
			 addr = InetAddress.getLocalHost();
			 mach2 = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
			 mach2 = DEFAULT;
		}
    
		List<Library>          ll2      = new ArrayList<Library>();
		
		String                 compN2   = null;
		String                 compV2   = null;
		ProcessStep.Compiler   comp2    = new ProcessStep.Compiler(compN2, compV2);
		
		ProcessStep            ps2      = new ProcessStep(prog2, ts2, cvs2, user2, mach2, plat2, comp2, ll2);
		
      ArrayList<ProcessStep> stepList = new ArrayList<>();
		stepList.add(ps1);
      stepList.add(ps2);
      
      return new Provenance(stepList);
   }
	
	
	
   
   
   @Override
	public void updateVariablesForEditableFields(MetadataPanel mdp, Character key, Object source)
	{
		labelTemplate = mdp.getJTextFieldContents("Label", key, source);
		note          = mdp.getJTextFieldContents("Note", key, source);
	}
	
	
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
      return (!labelTemplate.equals(""))      &&
             (!XNATSubjectID.equals(""))      &&
             (!XNATExperimentID.equals(""))   &&
             (!XNATScanIdSet.isEmpty());
   }
   
   
   @Override
   public String getRootElement()
   {
      return "RegionSet";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:regionSetData";
   }
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }
   
   
   // One application of this uploader is to upload RtStruct objects created
   // dynamically from other file formats. In that case, many of the instance
   // variables have to be set from outside, rather than being parsed from an
   // initial RT-STRUCT file.
   void setSubject(String s)
   {
      XNATSubjectID = s;
   }
   
   
   void setExperiment(String s)
   {
      XNATExperimentID = s;
   }
   
   
   void setSopFilenameMap(Map<String, String> m)
   {
      sopFilenameMap = m;
   }
   
   
   void setFilenameSopMap(Map<String, String> m)
   {
      filenameSopMap = m;
   }
   
   
   void setFilenameScanMap(Map<String, String> m)
   {
      filenameScanMap = m;
   }

      
   void setRtStruct(RtStruct r)
	{
		rts = r;
	}
	
	
	void setAssignedRegionIdList(List<String> ls)
	{
		assignedRegionIdList = ls;
	}
	
	   
	public void setProvenance(Provenance p)
	{
		prov = p;
	}
	
	
	public void setPrimaryResource(XnatResource xnr)
	{
		primaryResource = xnr;
	}
   
   
   void setOriginalDataType(String s)
   {
      originalDataType = s;
   }
   
   
   void setLabelParent(String s)
   {
      labelParent = s;
   }
}
