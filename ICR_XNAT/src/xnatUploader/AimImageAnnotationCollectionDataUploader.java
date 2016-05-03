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
* THIS SOFTWARE IS PROVIDED BY XXHE COPYRIGHT HOLDERS AND CONTRIBUTORS
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
* Java class: AimImageAnnotationCollectionDataUploader.java
* First created on 25 April, 2016 at 3:00 PM
* 
* Object for uploading Annotation and Image Markup (AIM) instance
* files to XNAT 
*********************************************************************/

package xnatUploader;

import dataRepresentations.dicom.RtStruct;
import dataRepresentations.xnatSchema.AbstractResource;
import dataRepresentations.xnatSchema.AdditionalField;
import dataRepresentations.xnatSchema.InvestigatorList;
import dataRepresentations.xnatSchema.MetaField;
import dataRepresentations.xnatSchema.Provenance;
import dataRepresentations.xnatSchema.Provenance.ProcessStep;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Platform;
import dataRepresentations.xnatSchema.Provenance.ProcessStep.Program;
import dataRepresentations.xnatSchema.Scan;
import etherj.XmlException;
import etherj.aim.AimToolkit;
import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import exceptions.DataFormatException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.DicomXnatDateTime;
import generalUtilities.UidGenerator;
import generalUtilities.Vector2D;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;
import xnatMetadataCreators.IcrAimImageAnnotationCollectionDataMdComplexType;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XnatResource;
import static xnatUploader.ContourRendererHelper.logger;


public class AimImageAnnotationCollectionDataUploader extends DataUploader
{
   private ImageAnnotationCollection iac;
   private Set<String>               studyUidSet;
	private Set<String>               seriesUidSet;
	private Set<String>               sopInstanceUidSet;
   private Map<String, String>       filenameSopMap;
	private Map<String, String>       sopFilenameMap;
	private Map<String, String>       filenameScanMap;
   private Map<String, DicomObject>  seriesDoMap;
	private String                    assocRegionSetId;
	private RtStruct                  iacRts;
	
	
   public AimImageAnnotationCollectionDataUploader(XNATProfile xnprf)
   {
      super(xnprf);
      
      studyUidSet       = new LinkedHashSet<>();
      seriesUidSet      = new LinkedHashSet<>();
      sopInstanceUidSet = new LinkedHashSet<>();
   }

  /**
    * Open and read the specified file.
    * Note that the default type of file is XML, but this method will be over-
    * ridden in subclasses to allow us to open arbitrary file types, such as
    * DICOM.
    * @return a boolean variable with true if the file was opened successfully
    *         and false otherwise.
    */
	@Override
   public boolean readFile()
   {
		// James d'Arcy's Etherj package opens and reads AIM files as a single
      // method call, so nothing is needed here.
      return true;				
   }
	
	/**
    * Parse an AIM instance to extract the relevant metadata.
	 * Note that because an external library Ether is being used, most of the
	 * parsing of the XML is actually performed in etherj.aim.XmlParser.
	 * @return true if the parsing is successful, false otherwise.
    */
	@Override
   public boolean parseFile()
   {     
		try
		{
			iac = (AimToolkit.getToolkit().createXmlParser()).parse(uploadFile);
		}
		catch (XmlException | IOException ex)
		{
			errorMessage = "Problem reading AIM instance file: " + ex.getMessage();
			logger.error(errorMessage);
			errorOccurred = true;
			return false;
		}
      
      try
      {
         date = DicomXnatDateTime.convertAimToXnatDate(iac.getDateTime());
         time = DicomXnatDateTime.convertAimToXnatTime(iac.getDateTime());
      }
      catch (DataFormatException exDF)
      {
         errorMessage = "Incorrect date-time format in AIM metadata." + exDF.getMessage();
         logger.error(errorMessage);
         errorOccurred = true;
         return false;
      }
      
      // Extract the image reference data from the AIM structure.
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (ImageReference ir : ia.getReferenceList())
         {
            if (ir instanceof DicomImageReference)
            {
               DicomImageReference dir    = (DicomImageReference) ir;
               ImageStudy          study  = dir.getStudy();
               studyUidSet.add(study.getInstanceUid());
               ImageSeries         series = study.getSeries();
               seriesUidSet.add(series.getInstanceUid());
               for (Image im : series.getImageList())
               {
                  sopInstanceUidSet.add(im.getInstanceUid());
               }    
            }
         }
      }

		XnatDependencyChecker xnd = new XnatDependencyChecker(xnprf, XNATProject,
		                                                      studyUidSet, seriesUidSet, sopInstanceUidSet);
		boolean  isOk    = xnd.areDependenciesInDatabase();
		XNATSubjectID    = xnd.getSubjectId();
		XNATExperimentID = xnd.getExperimentId();
		XNATScanIdSet    = xnd.getScanIdSet();
		filenameSopMap   = xnd.getFilenameSopMap();
		sopFilenameMap   = xnd.getSopFilenameMap();
		filenameScanMap  = xnd.getFilenameScanMap();
		ambiguousSubjExp = xnd.getAmbiguousSubjectExperiement();
		errorMessage     = xnd.getErrorMessage();
		
		if (!isOk) return false;
      
      // In order to convert the ROI part of the AIM file to an RT-STRUCT file,
      // we need to create a number of DICOM structures. Much of the information
      // required can be obtained from the files referenced by the AIM
      // document. Now that we know the mapping between filename and SOP instance
      // UID, we can loop back through the list and download a representative
      // file from each series.
      seriesDoMap = new HashMap<>();
      for (ImageAnnotation ia : iac.getAnnotationList())
      {
         for (ImageReference ir : ia.getReferenceList())
         {
            if (ir instanceof DicomImageReference)
            {
               DicomImageReference dir    = (DicomImageReference) ir;
               ImageStudy          study  = dir.getStudy();
               ImageSeries         series = study.getSeries();
               if (!seriesDoMap.containsKey(series.getInstanceUid()))
               {
                  Image       im       = series.getImageList().get(0);
                  String      filename = sopFilenameMap.get(im.getInstanceUid());
                  DicomObject bdo      = downloadDicomObject(filename);
                  
                  if (bdo != null) seriesDoMap.put(series.getInstanceUid(), bdo);
                  else return false;
               }             
            }
         }
      }
      return true;
   }
  
   
   private DicomObject downloadDicomObject(String filename)
   {
      DicomObject      bdo = null;
      String           homeDir      = System.getProperty("user.home");
      String           fileSep      = System.getProperty("file.separator");
		String           cacheDirName = homeDir + fileSep + ".XNAT_DAO";
      String           testScan     = filenameScanMap.get(filename);
      String           restCommand  = "/data/archive/experiments/" + XNATExperimentID
                                      + "/scans/" + testScan + "/files?format=xml";
      Vector2D<String> resultSet;
      try
      {
         resultSet = (new XNATRESTToolkit(xnprf)).RESTGetResultSet(restCommand);
      }
      catch(XNATException exXNAT)
      {
         errorMessage  = "Error retrieving file list: " + exXNAT.getMessage();
         errorOccurred = true;
         return null;
      }
      Vector<String> URI  = resultSet.getColumn(2);
      Vector<String> type = resultSet.getColumn(3);
      
      for (int i=0; i<URI.size(); i++)
      {
         if (type.elementAt(i).equals("DICOM"))
         {
            // Build the local cache filename where the data will be stored.
            // The directory structure is a bit long-winded, but should be
            // easy to manage.
            StringBuilder sb = new StringBuilder(cacheDirName);
            sb.append(URI.elementAt(i));
            File cacheFile = new File(sb.toString());
            File parent    = new File(cacheFile.getParent());
            
            if (cacheFile.getName().equals(filename))
            {

               boolean success = true;
               if (!cacheFile.exists())
               {
                  // Retrieve the actual data and store it in the cache.
                  try
                  {
                     parent.mkdirs();
                     BufferedOutputStream bos
                        = new BufferedOutputStream(new FileOutputStream(cacheFile, true));

                     BufferedInputStream  bis
                        = new BufferedInputStream(xnprf.doRESTGet(URI.elementAt(i)));

                     byte[] buf = new byte[8192];

                     while (true)
                     {
                        int length = bis.read(buf);
                        if (length < 0) break;
                        bos.write(buf, 0, length);
                     }

                     logger.debug("Worker ID = " + this.toString() + " Downloaded " + cacheFile.toString());

                     try{bis.close();}
                     catch (IOException ignore) {;}

                     try{bos.close();}
                     catch (IOException ignore) {;}                                  
                  }
                  catch (Exception ex)
                  {
                     errorOccurred = true;
                     errorMessage = "Failed to download " + cacheFile.getName();
                     logger.error(errorMessage);
                     return null;
                  }
               }
               
               // Now try to open the DICOM file just downloaded.
               try
               {
                  bdo = new BasicDicomObject();
                  DicomInputStream dis = new DicomInputStream(cacheFile);
                  dis.readDicomObject(bdo, -1);
               }
               catch(Exception ex)
               {
                  errorOccurred = true;
                  errorMessage  = "Incorrect image format" + ex.getMessage();
                  logger.error(errorMessage);
                  return null;
               }
            }           
         }
      }
      return bdo;
   }
   
   @Override
   public void updateParseFile()
   {
      //TODO
   }
   
	
	@Override
   public void clearFields(MetadataPanel mdp)
   {
      mdp.populateJTextField("Label", "", true);
      mdp.populateJTextField("Note",  "", true);
   }


   
      
   /**
	 * Uploading data for an assessor to XNAT is a two-stage process. First the data file
	 * is placed in the repository, then the metadata are placed in the SQL
	 * tables of the PostgreSQL database. This method attempts the repository
    * upload. See also the comment for this method in the superclass.
    *
	 * The AIM upload is particularly complicated, because a single AIM file
	 * can describe multiple ROIs. In this sense, the situation is a little like
	 * the case for an RT-STRUCT, but it is even more involved, because one also
	 * needs to upload the references to the various radiological observations.
	 * 
	 * THE AIM UML document is rather complex and so the translation into the
	 * XNAT schema necessarily misses out some of the relationships.
    * 
    * Note also that for the situations that I have come across so far, the image
    * annotation collection is a redundant layer of hierarchy, because all of the
    * annotation collections I have examples of so far have only one image
    * annotation in them.
	 * 
	 * @throws exceptions.XNATException
	 * @throws exceptions.DataFormatException
	 * @throws java.io.IOException
	 */
   @Override
   public void uploadMetadataAndDependencies() throws XNATException, DataFormatException, IOException
   {
      errorOccurred = false;
          
      // ------------------------------------------------------
      // Step 1: Create and upload the corresponding RT-STRUCT.
      // ------------------------------------------------------
      RtStructDataUploader ru = new RtStructDataUploader(xnprf);
      try
      {
         assocRegionSetId = ru.getRootElement() + "_" + UidGenerator.createShortUnique(); 
         ru.setAccessionId(assocRegionSetId);
         ru.setSubjectId(XNATSubjectID);
         ru.setExperimentId(XNATExperimentID);
			iacRts = new RtStruct(iac, seriesDoMap);
			ru.setRtStruct(iacRts);
         ru.setProvenance(createProvenance(iacRts));
         ru.setSopFilenameMap(sopFilenameMap);
         ru.setFilenameSopMap(filenameSopMap);
         ru.setFilenameScanMap(filenameScanMap);
         
         ru.uploadMetadataAndDependencies();
			
			String description = "DICOM RT-STRUCT file auto-created by ICR XNAT uploader from AIM instance file";
			DicomObject iacDo  = new BasicDicomObject();
			iacRts.writeToDicom(iacDo);
			
			// Create the new RT-STRUCT as an input stream to be fed into the uploader.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DicomOutputStream     dos  = new DicomOutputStream(baos);
			dos.writeDicomFile(iacDo);		
			InputStream           bais = new ByteArrayInputStream(baos.toByteArray());
            
			XnatResource xr = new XnatResource(bais,
														  "out",
														  "RT-STRUCT",
														  "DICOM",
														  "GENERATED",
														  description.toString(),
														  XNATAccessionID + "_RTSTRUCT.dcm");
			
			ru.setPrimaryResource(xr);
         ru.uploadResourcesToRepository();
      }
      catch (XNATException | DataFormatException | IOException ex)
		{
			errorOccurred = true;
			errorMessage  = ex.getMessage();
			throw ex;
		}

		
      // -----------------------------------------------------------------
      // Step 2: Upload the icr:aimImageAnnotationCollectionData metadata.
      // -----------------------------------------------------------------		
		XNATAccessionID = iac.getUid();
      super.uploadMetadataAndDependencies();
      
		
		// -----------------------------------------------------------------------
		// Step 3: Each AIM image annotation collection includes a number of
		//         individual image annotations. Upload each of these in the form
		//         of an icr:aimImageAnnotationData.
		// -----------------------------------------------------------------------	
      for (ImageAnnotation ia : iac.getAnnotationList())
		{
			AimImageAnnotationDataUploader iau = new AimImageAnnotationDataUploader(xnprf);
			
			try
			{
				iau.setAccessionId(ia.getUid());
				iau.setImageAnnotation(ia);
				iau.setUserParent(iac.getUser());
            iau.setEquipmentParent(iac.getEquipment());
            iau.setPersonParent(iac.getPerson());
            iau.setAssociatedRegionSetId(assocRegionSetId);
            
            iau.uploadMetadataAndDependencies();
				
			}
			catch  (XNATException | DataFormatException | IOException ex)
			{
				errorOccurred = true;
				errorMessage  = ex.getMessage();
				throw ex;
			}
		}

      
      
	}   
      
     
	@Override
	public Document createMetadataXml()
	{
		// Metadata are created simply by instantiating the metadata creator
		// object for the required complex type, filling it with the correct
		// and calling its createXmlAsRootElement() method. The complexity
		// in this method comes from the number of pieces of data that must
		// be transferred from the RT-STRUCT to the metadata creator.
		IcrAimImageAnnotationCollectionDataMdComplexType
				    iacd = new IcrAimImageAnnotationCollectionDataMdComplexType();
		
		iacd.setVersion(iac.getAimVersion());
      
      iacd.setAimUserName(          iac.getUser().getName());
      iacd.setAimUserLoginName(     iac.getUser().getLoginName());
      iacd.setAimUserRole(          iac.getUser().getRoleInTrial());
      iacd.setAimUserNumberInRole(  iac.getUser().getNumberWithinRoleOfClinicalTrial());
      
      iacd.setManufacturerName(     iac.getEquipment().getManufacturerName());
      iacd.setManufacturerModelName(iac.getEquipment().getManufacturerModelName());
      iacd.setDeviceSerialNumber(   iac.getEquipment().getDeviceSerialNumber());
      iacd.setSoftwareVersion(      iac.getEquipment().getSoftwareVersion());
      
      iacd.setPersonName(           iac.getPerson().getName());
      iacd.setPersonId(             iac.getPerson().getId());
      iacd.setPersonBirthDate(      iac.getPerson().getBirthDate());
      iacd.setPersonSex(            iac.getPerson().getSex());
      iacd.setPersonEthnicGroup(    iac.getPerson().getEthnicGroup());
      
		iacd.setnumImageAnnotations(iac.getAnnotationCount());
		iacd.setAssociatedRegionSetId(assocRegionSetId);
		
		List<String> iaIdl = new ArrayList<>();
		for (ImageAnnotation ia : iac.getAnnotationList()) iaIdl.add(ia.getUid());
		iacd.setImageAnnotationIdList(iaIdl);
		
     
      // IcrRegionSetDataMdComplexType inherits from IcrGenericImageAssessmentDataMdComplexType.
		
		// iacd.setType();  Not currently sure what should go here.
		iacd.setXnatSubjId(XNATSubjectID);
	   iacd.setDicomSubjName(iacRts.patient.patientName);
		
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
		iacd.setScanList(lsc);
      
      
      // IcrGenericImageAssessmentDataMdComplexType inherits from XnatImageAssessorDataMdComplexType.
		
		// The "in" section of the assessor XML contains all files that were already
		// in the database at the time of upload, whilst the "out" section lists
		// the files that added at the time of upload, including those generated
		// automatically. In this, the only generated files are the snapshots, but
		// this information is already included in the separately uploaded ROI
		// metadata files and need not be duplicated here.
		List<AbstractResource> inList = new ArrayList<>();
		
		for (String filename : filenameSopMap.keySet())
		{
			AbstractResource ar  = new AbstractResource();
			List<MetaField>  mfl = new ArrayList<>();
			mfl.add(new MetaField("filename",       filename));
			mfl.add(new MetaField("format",         "DICOM"));
			mfl.add(new MetaField("SOPInstanceUID", filenameSopMap.get(filename)));
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
		
		iacd.setInList(inList);
		iacd.setOutList(outList);
		
		iacd.setImageSessionId(XNATExperimentID);
		
		// For this object, there are no additional fields. This entry is
		// empty, but still needs to be set.
		iacd.setParamList(new ArrayList<AdditionalField>());
		
		// XnatImageAssessorDataMdComplexType inherits from XnatDerivedDataMdComplexType.
		
		prov = createProvenance();
		iacd.setProvenance(prov);
				                                 
		
		// XnatDerivedDataMdComplexType inherits from XnatExperimentData.
		
      iacd.setId(XNATAccessionID);
      iacd.setProject(XNATProject);
      
     
		// Apparently the version XML element has to be an integer, so it is not
		// really clear what this field signifies.
		iacd.setVersion("1");
		
      String labelSuffix = "_" + uploadFile.getName() + "_" + UidGenerator.createShortUnique();
		label = isBatchMode ? labelPrefix + labelSuffix : labelPrefix;
		iacd.setLabel(label);
      
		iacd.setDate(date);
      iacd.setTime(time);
      iacd.setNote(note);
		
      // No correlates in the structure set read in for visit, visitId,
      // original, protocol and investigator.
		iacd.setInvestigator(new InvestigatorList.Investigator());      
		// Finally write the metadata XML document.
		Document metaDoc = null;
		try
		{
			metaDoc = iacd.createXmlAsRootElement();
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
	
	
	Provenance createProvenance(RtStruct iacRts)
	{
		// A number of fields are mandatory in the birn.xsd provenance schema,
		// so provide defaults if they do not exist.
		final String DEFAULT = "Unknown";
		final String SEP     = " | ";
		
		
		// Provenance step 1 : source DICOM data	
		StringBuilder sb    = new StringBuilder();
		for (String s : iacRts.generalEquipment.softwareVersions)
		{
			if (!(sb.toString().contains(s))) 
			{
			   if (sb.length() != 0) sb.append(SEP);
				if (s != null) sb.append(s);
			}
		}
      String versions = getDefaultIfEmpty(sb.toString());
		
		ProcessStep.Program    prog1    = new Program(getDefaultIfEmpty(iacRts.generalEquipment.manufacturer) + " software",
		                                              versions,
		                                              DEFAULT);
		
		ProcessStep.Platform   plat1    = new Platform(getDefaultIfEmpty(iacRts.generalEquipment.modelName),
				                                         DEFAULT);

		
		// Note that ts1 has to be initialised with a valid default. 
		String ts1 ="1900-01-01T00:00:00";
		try
		{
			ts1 = DicomXnatDateTime.convertDicomToXnatDateTime(iacRts.structureSet.structureSetDate,
				                                                iacRts.structureSet.structureSetTime);
		}
		catch (DataFormatException exDF)
		{
			errorOccurred = true;
			errorMessage  = "Incorrect DICOM date format in structure set file";
		}
		
		String                 cvs1     = null;
		String                 user1    = getDefaultIfEmpty(iacRts.rtRoiObservationList.get(0).roiInterpreter);	
		String                 mach1    = getDefaultIfEmpty(iacRts.generalEquipment.stationName);
		
		// We don't have a compiler version, but we still need to specify it, as
		// the instance variables are accessed later. (Still needed even though the instance
		// variables are null themselves ...)
		
		String                 compN1   = null;
		String                 compV1   = null;
		ProcessStep.Compiler   comp1    = new ProcessStep.Compiler(compN1, compV1);
		
      // Even though  the library list is empty we still need to specify it, otherwise
		// a null pointer exception will pop up when we try to iterate through the list.
		List<ProcessStep.Library>  ll1 = new ArrayList<ProcessStep.Library>();
				  
		ProcessStep ps1 = new Provenance.ProcessStep(prog1, ts1, cvs1, user1, mach1, plat1, comp1, ll1);
		

		
		// Provenance step 2: record the step that created the ImageAnnotationCollection.
		ProcessStep.Program    prog2    = new Program("AIM data source", iac.getAimVersion(), DEFAULT);
 
		String                 iacMn    = "Equipment manufacturer from AIM document: " + iac.getEquipment().getManufacturerName();	
		String                 iacSv    = "Equipment software version from AIM document: " + iac.getEquipment().getSoftwareVersion();	
		ProcessStep.Platform   plat2    = new Platform(iacMn, iacSv);
   
      String                 ts2      = iac.getDateTime();
      
      String                 cvs2     = null;
			
		sb = new StringBuilder();
		sb.append("Name:").append(iac.getUser().getName()).append(SEP)
		  .append("Login name:").append(iac.getUser().getLoginName()).append(SEP)
		  .append("Role in trial").append(iac.getUser().getRoleInTrial()).append(SEP)
		  .append("Number within role").append(iac.getUser().getNumberWithinRoleOfClinicalTrial());
		
		String                 user2    = sb.toString();
      
      String                 mach2    = "Equipment model from AIM document: " + iac.getEquipment().getManufacturerName();;
    
		List<ProcessStep.Library> ll2   = new ArrayList<ProcessStep.Library>();
		
		String                 compN2   = null;
		String                 compV2   = null;
		ProcessStep.Compiler   comp2    = new Provenance.ProcessStep.Compiler(compN2, compV2);
		
		Provenance.ProcessStep ps2      = new Provenance.ProcessStep(prog2, ts2, cvs2, user2, mach2, plat2, comp2, ll2);


		// Provenance step 3: record the transit through DataUploader.
		
      ProcessStep.Program    prog3    = new Program("ICR XNAT DataUploader", version, "None");
      
		ProcessStep.Platform   plat3    = new Platform(System.getProperty("os.arch") + " " + System.getProperty("os.name"),
				                                         System.getProperty("os.version"));
   
      String                 ts3      = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      String                 cvs3     = null;
		
		String                 user3    = System.getProperty("user.name");
      
      String                 mach3    = DEFAULT;
    
		List<ProcessStep.Library> ll3   = new ArrayList<ProcessStep.Library>();
		
		String                 compN3   = null;
		String                 compV3   = null;
		ProcessStep.Compiler   comp3    = new Provenance.ProcessStep.Compiler(compN3, compV3);
		
		Provenance.ProcessStep ps3      = new Provenance.ProcessStep(prog3, ts3, cvs3, user3, mach3, plat3, comp3, ll3);
		
      ArrayList<Provenance.ProcessStep> stepList = new ArrayList<>();
		stepList.add(ps1);
      stepList.add(ps2);
		stepList.add(ps3);
      
      return new Provenance(stepList);
	}
	
	
	@Override
	public void createPrimaryResource()
	{
		StringBuilder description = new StringBuilder();
		description.append("AIM instance file");

		primaryResource = new XnatResource(uploadFile,
		                                   "out",
		                                   "AIM-INSTANCE",
				                             "XML",
		                                   "EXTERNAL",
		                                   description.toString(),
				                             uploadFile.getName());
	}
   
   
   
   @Override
   public void createAuxiliaryResources()
   {
      // There are no auxiliary resources associated with an
      // AimImageAnnotationCollection. uploadMetadataAndDependencies() above kicks off a
      // separate upload of an RT-STRUCT, which, in turn archives the ROI objects.
      // Hence, nothing needs to be done here.
   }
   
   
   @Override
   public String getRootElement()
   {
      return "AimImageAnnotationCollection";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:aimImageAnnotationCollectionData";
   }
	
	
	@Override
   public String getUploadRootCommand(String uploadItem)
   {
		return "/data/archive/projects/" + XNATProject
             + "/subjects/"            + XNATSubjectID
             + "/experiments/"         + XNATExperimentID
             + "/assessors/"           + uploadItem;
   }
	
	
	@Override
	public void updateVariablesForEditableFields(MetadataPanel mdp)
	{
		labelPrefix = mdp.getJTextFieldContents("Label");
		note        = mdp.getJTextFieldContents("Note");
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
      return (!labelPrefix.equals("")) &&
             (!XNATSubjectID.equals(""))           &&
             (!XNATExperimentID.equals(""))        &&
             (!XNATScanIdSet.isEmpty());
   }
   
}

