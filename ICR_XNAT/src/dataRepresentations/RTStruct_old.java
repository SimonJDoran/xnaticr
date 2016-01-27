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
* Java class: RTStruct.java
* First created on Jul 12, 2012 at 10:53:42 PM
* 
* Define a representation of the RTStruct data structure, including
* methods to read the data in from a DICOM file and create a new
* DICOM file from an instance.
*********************************************************************/

package dataRepresentations;

import exceptions.XNATException;
import generalUtilities.Vector2D;
import generalUtilities.UIDGenerator;
import java.net.InetAddress;
import java.util.*;
import exceptions.DataFormatException;
import exceptions.DataRepresentationException;
import exceptions.XMLException;
import org.apache.log4j.Logger;
import org.dcm4che2.data.*;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;
import xnatUploader.AmbiguousSubjectAndExperiment;

public final class RTStruct_old extends DataRepresentation implements RtStructWriter
{
   static    Logger             logger      = Logger.getLogger(RTStruct_old.class);
   
   protected static final int   DUMMY_INT   = -999;
   protected static final float DUMMY_FLOAT = -999.9f; 
   
   /**
    * Auxiliary classes used to define a structure for the frame-of-reference
    * and contour information to be placed into. These classes mirror the DICOM
    * nested sequence structure. In some cases, as with class ContourImage and
    * ROICOntour / Contour, there appears to be needless nesting of concepts,
    * but that's what is in the DICOM standard!
    * 
    * Note that all members are public, with no getter and setter methods
    * defined. The whole idea about this class is to make the individual fields
    * available to consumers.
    */
   public class ReferencedStudy
   {
      public String                    SOPInstanceUID;
      public String                    SOPClassUID; 
   }
   
   public class ReferencedFrameOfReference
   {
      public String                    UID;
      public int                       nRelatedFOR;
      public RelatedFrameOfReference[] relatedFOR;
      public int                       nStudies;
      public RTReferencedStudy[]       studies;
   }
   
   
   public class RelatedFrameOfReference
   {
      public String                    UID;
      public String                    transformationMatrix;
      public String                    transformationComment;
   }
   
   
   public class RTReferencedStudy
   {
      public String                    SOPInstanceUID;
      public String                    SOPClassUID;
      public int                       nSeries;
      public RTReferencedSeries[]      series;      
   }

   
   public class RTReferencedSeries
   {
      public String                    UID;
      public int                       nImages;
      public ContourImage[]            imageList;
   }
   
   
   public class ContourImage
   {
      public String                    SOPInstanceUID;
      public String                    SOPClassUID;
   }
   
   
   public class StructureSetROI
   {
      public int                       roiNumber;
      public int                       correspondingROIContour;
      public int                       correspondingROIObservation;
      public String                    referencedFrameOfReferenceUID;
      public String                    roiName;
      public String                    roiDescription;
      public float                     roiVolume = DUMMY_FLOAT;
      public String                    roiGenerationAlgorithm;
      public String                    roiGenerationDescription;
      public String                    derivationCode;
      public String                    roiXNATID;
   }
   
   
   public class ROIContour
   {
      public int                       referencedRoiNumber;
      public int[]                     roiDisplayColour = {DUMMY_INT, DUMMY_INT, DUMMY_INT};
      public String                    frameOfReferenceUID;
      public Contour[]                 contourList;
   }
   
   
   public class Contourx
   {
      public int                       contourNumber    = DUMMY_INT;
      public int[]                     attachedContours = {DUMMY_INT};
      public ContourImage[]            imageList;
      public String                    geometricType;
      public float                     slabThickness = DUMMY_FLOAT;
      public float[]                   offsetVector  = {DUMMY_FLOAT, DUMMY_FLOAT, DUMMY_FLOAT};
      public int                       nContourPoints;
      public float[][]                 contourPoints;
   }

   
   public class RTROIObservation
   {
      public int                       obsNumber;
      public int                       referencedRoiNumber;
      public String                    obsLabel;
      public String                    obsDescription;
      public RTRelatedROI[]            relatedROIs;
      public int[]                     relatedROIObservations;
      public String                    rtRoiInterpretedType;
      public String                    roiInterpreter;
      public String                    roiMaterialID;
      public ROIPhysicalProperties[]   roiPhysicalProps;
   }
   
   
   public class RTRelatedROI
   {
      public int                       referencedRoiNumber;
      public String                    relationship;
   }
   
   
   public class ROIPhysicalProperties
   {
      public String                    propertyName;
      public String                    propertyValue;
      public ElementalComposition[]    elementalComp;
   }
   
   
   public class ElementalComposition
   {
      public int                       atomicNumber;
      public float                     atomicMassFraction;
   }
   
   
   public String                       version;
   public DicomObject                  bdo;
   public String                       structureSetUID;
   public String                       structureSetLabel;
   public String                       structureSetName;
   public String                       structureSetDate;
   public String                       structureSetTime;
   public String                       structureSetDescription;
   public int                          instanceNumber = DUMMY_INT;
   public ReferencedStudy[]            refStudyList;
   public ReferencedFrameOfReference[] fORList;
   public StructureSetROI[]            roiList;
   public ROIContour[]                 roiContourList;
   public RTROIObservation[]           roiObsList;   
   public String                       roiSetID;
   public String                       roiSetLabel;
   public ArrayList<String>            studyUIDs;
   public ArrayList<String>            seriesUIDs;
   public ArrayList<String>            SOPInstanceUIDs;
   public String                       studyDate;
   public String                       studyTime;
   public String                       studyDescription;
   public String                       patientName;
   
   
   // Question: Does it make sense to place XNAT-specific information in an
   // object that is describing a DICOM concept?
   // Answer: If we want to be able to render the contour, then we have to
   // be able to get the base images from somewhere, so yes! Remember, all
	// the classes in the DataRepresentations package are designed for use
	// in XNAT-specific, not general-purpose, code, although they might also
   // be used for other purposes where useful.
   public String                       XNATDateOfBirth;
   public String                       XNATGender;
   public LinkedHashMap<String, AmbiguousSubjectAndExperiment> ambiguousSubjExp;
   
	
    /**
    * Constructor with data from an existing DataRepresentations.RTStruct.
    * @param src a valid source RTStruct object
    * @param rois a subset of rois in the original object to be used for the creation of the new one. 
    */ 
  public RTStruct_old(RTStruct_old src, Set<Integer> rois)
	{  
  		version                 = src.version;
		bdo                     = src.bdo;
		structureSetUID         = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DAO,
		                                                         UIDGenerator.RT_STRUCT,
																					UIDGenerator.SOPInstanceUID);
		structureSetLabel       = src.structureSetLabel + " - ROI subset";
		structureSetName        = src.structureSetName + "_ROIsubset";
		structureSetDescription = src.structureSetDescription + " generated by XNATDataChooser";
		instanceNumber          = src.instanceNumber;
		structureSetDate        = src.structureSetDate;
		structureSetTime        = src.structureSetTime;
		studyDate               = src.studyDate;
		studyTime               = src.studyTime;
		studyDescription        = src.studyDescription;
		patientName             = src.patientName;
		
		// Note that it is not straightforward to separate the studies referenced
		// by the ROIs actually chosen from those referenced by the other ROIs in
		// the input DICOM multi-ROI structure set. In almost all cases, the studies
		// will be the same, but this code may entail the odd instance where the
		// generated ROI-subset RT-STRUCTs will reference studies that they shouldn't.
		refStudyList            = src.refStudyList;
		fORList                 = src.fORList;
		studyUIDs               = src.studyUIDs;
		seriesUIDs              = src.seriesUIDs;
		XNATScanID              = src.XNATScanID;
		
		ArrayList<StructureSetROI>  alss = new ArrayList<>();
		ArrayList<ROIContour>       alrc = new ArrayList<>();
		ArrayList<RTROIObservation> alro = new ArrayList<>();

		for (Iterator<Integer> roiIt = rois.iterator(); roiIt.hasNext();)
		{
			Integer roi = roiIt.next();
			
			for (int i=0; i<roiList.length; i++)
				if (src.roiList[i].roiNumber == roi) alss.add(src.roiList[i]);			
						
			for (int i=0; i<roiContourList.length; i++)
				if (src.roiContourList[i].referencedRoiNumber == roi) alrc.add(src.roiContourList[i]);			
						
			for (int i=0; i<roiObsList.length; i++)
				if (src.roiObsList[i].referencedRoiNumber == roi) alro.add(src.roiObsList[i]);					
		}
		
		roiList             = alss.toArray(new StructureSetROI[alss.size()]);
		roiContourList      = alrc.toArray(new ROIContour[alrc.size()]);
		roiObsList          = alro.toArray(new RTROIObservation[alro.size()]);
		
		// The following variables are not used in the output of an RT-STRUCT
		// to a DICOM file, but relate to the upload of the object to XNAT.
		// They can safely be set here to arbitrary values here and reset
		// appropriately in the unlikely event of needing to upload this new
		// "subset" RT-STRUCT as a separate icr:roiSet.
		roiSetID            = src.roiSetID + "_subset" + UIDGenerator.createShortUnique();
		roiSetLabel         = src.roiSetLabel + " - ROI subset";
		XNATProjectID       = "Not defined";
		XNATExperimentID    = "Not defined";
		XNATRefExperimentID = src.XNATExperimentID;
		XNATSubjectID       = src.XNATSubjectID;
		XNATSubjectLabel    = src.XNATSubjectLabel;
		fileSOPMap          = src.fileSOPMap;
		fileScanMap         = src.fileScanMap;
		xnprf               = null;
		
		// The variable SOPInstanceUIDs is not used outside the code that checks
		// whether the base images are in XNAT, so there should be no need to
		// set it.
	}
  
  
  
   /**
    * Constructor with data from an RT-STRUCT DICOM.
    * @param bdo a DCM4CHE Basic DICOM object that has already been initialised,
    * typically from an RT-STRUCT file, although it could have been created dynamically.
    * @param xnprf an XNAT profile, already connected to an XNAT database, which
    * we can use to query the databases for image dependencies. 
	 * @throws exceptions.DataFormatException 
	 * @throws DataRepresentationException 
    */
	public RTStruct_old(DicomObject bdo, XNATProfile xnprf)
          throws DataFormatException, DataRepresentationException
   {
      this.bdo         = bdo;
      this.xnprf       = xnprf;
		
		studyUIDs        = new ArrayList<>();
      seriesUIDs       = new ArrayList<>();
      SOPInstanceUIDs  = new ArrayList<>();
      fileSOPMap       = new TreeMap<>();
      fileScanMap      = new TreeMap<>();
      ambiguousSubjExp = new LinkedHashMap<>();
           
		// Before we start, check that this really is a structure set!
		if (!bdo.getString(Tag.Modality).equals("RTSTRUCT"))
		{
			throw new DataFormatException(DataFormatException.RTSTRUCT, 
						 "Can't create an RTStruct object.\n");
		}

		structureSetUID          = bdo.getString(Tag.SOPInstanceUID);
		structureSetLabel        = bdo.getString(Tag.StructureSetLabel);
		structureSetName         = bdo.getString(Tag.StructureSetName);
		structureSetDescription  = bdo.getString(Tag.StructureSetDescription);
		if (bdo.contains(Tag.InstanceNumber))
			instanceNumber        = bdo.getInt(Tag.InstanceNumber);
		structureSetDate         = bdo.getString(Tag.StructureSetDate);
		structureSetTime         = bdo.getString(Tag.StructureSetTime);
		studyDate                = bdo.getString(Tag.StudyDate);
		studyTime                = bdo.getString(Tag.StudyTime);
		studyDescription         = bdo.getString(Tag.StudyDescription);
		patientName              = bdo.getString(Tag.PatientName);

		ArrayList<String> issues = new ArrayList<>();

		// Get information on the (potentially multiple) studies referenced
		// by this DICOM RT-Struct file.
		extractReferencedStudyInfo(issues);

		// Nested within the frames-of-reference DICOM sequence is also all the
		// information on the referenced studies, on which the contours are
		// defined. Both the Java classes above and the corresponding custom
		// XNAT schema are designed to relate directly to the DICOM structures.
		extractFramesOfReferenceInfo(issues);

		// Check that all the studies, series and SOPInstances referenced are
		// already present in the database.
		dependenciesInDatabase(xnprf, issues);

		// Extract overview information about the ROIs contained in the
		// structure set file, such as name, structureSetDescription, volumne, generating
		// algorithm, etc.
		extractStructureSetROIInfo(issues);

		// Extract information on the individual contours that make up each
		// of the ROIs. Note that we extract only the metadata, not the actual
		// contour coordinates themselves.
		extractContourInfo(issues);

		// Extract information on the radiotherapy-related interpretation
		// of the ROIs. This includes the identification as an organ, PTV,
		// marker, etc. and its physical composition.
		extractRTROIObservationsInfo(issues);
		
		if (!issues.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for (String issue : issues) sb.append(issue).append("\n");
			throw new DataRepresentationException(DataRepresentationException.RTSTRUCT,
			                                       sb.toString());
		}
   }
   
   
   /**
    * Read the information on any studies referred to and place in object instance
    * variables. 
	 * @param issues ArrayList to which any problems found will be added
    */
   protected void extractReferencedStudyInfo(ArrayList<String> issues)
   {
 
		DicomElement seqRefStudy  = bdo.get(Tag.ReferencedStudySequence);

		if (seqRefStudy == null)
		{
			issues.add("No information in DICOM file on referenced studies.");
			return;
		}
			
		int nRefStudy = seqRefStudy.countItems();
		refStudyList = new ReferencedStudy[nRefStudy];

		for (int i=0; i<nRefStudy; i++)
		{
			refStudyList[i] = new ReferencedStudy();
			DicomObject refStudy = seqRefStudy.getDicomObject(i);

			refStudyList[i].SOPInstanceUID = refStudy.getString(Tag.ReferencedSOPInstanceUID);
			refStudyList[i].SOPClassUID    = refStudy.getString(Tag.ReferencedSOPClassUID);   
		}
      
   }
   
   /** Extract the frame-of-reference-information.
    * This entails not only the SOPInstanceUID of the frame-of-reference in which the
    * structure set contours themselves are defined, but also the SOPInstanceUID's
    * of any related frames of reference, together with their relationship
    * to the frame-of-reference of this structure set. As an added complication
    * if the ROIs were drawn on more than one series, then there are multiple
    * items in this sequence.
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractFramesOfReferenceInfo(ArrayList<String> issues)
   {   
		DicomElement seqRefFOR  = bdo.get(Tag.ReferencedFrameOfReferenceSequence);

		if (seqRefFOR == null)
		{
			issues.add("No information in DICOM file on frames of reference.");
			return;
		}

		int nRefFOR = seqRefFOR.countItems();
		fORList = new ReferencedFrameOfReference[nRefFOR];

		for (int i=0; i<nRefFOR; i++)
		{
			fORList[i] = new ReferencedFrameOfReference();
			DicomObject fOR = seqRefFOR.getDicomObject(i);

			fORList[i].UID  = fOR.getString(Tag.FrameOfReferenceUID);

			extractFrameOfReferenceRelationship(i, fOR, issues);
			extractReferencedStudies(i, fOR, issues);            
		}
   }
   
   
   /**
    * Extract information on the relationship of the frame-of-reference in
    * which the structure set is defined with other specified frames of
    * reference - e.g., describe how an MR data set is related to a CT one.
    * @param nf int DICOM referenced frame-of-reference number
    * @param fOR the DICOM object corresponding to item nf in the DICOM
    * "referenced frame of reference" sequence at tag (3006,0010)
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractFrameOfReferenceRelationship(int nf,
			                                 DicomObject fOR, List<String> issues)
   {
		DicomElement seqFORRel = fOR.get(Tag.FrameOfReferenceRelationshipSequence);
		if (seqFORRel == null) fORList[nf].nRelatedFOR = 0;
		else
		{
			int nRel = seqFORRel.countItems();
			fORList[nf].nRelatedFOR = nRel;
			fORList[nf].relatedFOR  = new RelatedFrameOfReference[nRel];

			for (int i=0; i<nRel; i++)
			{
				fORList[nf].relatedFOR[i] = new RelatedFrameOfReference(); 
				DicomObject relFOR = seqFORRel.getDicomObject(i);

				fORList[nf].relatedFOR[i].UID
					 = relFOR.getString(Tag.RelatedFrameOfReferenceUID);

				fORList[nf].relatedFOR[i].transformationComment
					 = relFOR.getString(Tag.FrameOfReferenceTransformationComment);

				fORList[nf].relatedFOR[i].transformationMatrix
					 = relFOR.getString(Tag.FrameOfReferenceTransformationMatrix);
			}
		}
   }
   
   
   /**
    * Extract the referenced studies embedded in the
    * nested DICOM "referenced frame of reference" sequence.
    * @param nf the item number in the DICOM "referenced frame of reference" sequence
    * @param fOR the DICOM object corresponding to item nf in the DICOM
    * "referenced frame of reference" sequence at tag (3006,0010)
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractReferencedStudies(int nf, DicomObject fOR,
			                                                   List<String> issues)
   {
		// There is a separate DICOM sequence for each study. This caters with
		// the situation, for example, where some outlining is done on CT and
		// some on MRI.
		DicomElement seqRtRefStudies = fOR.get(Tag.RTReferencedStudySequence);
		if (seqRtRefStudies == null)
		{
			issues.add("Input does not contain required information about the"
						  + " studies on which the regions-of-interest were drawn.");
			return;
		}
		
		int nst = seqRtRefStudies.countItems();
		fORList[nf].nStudies = nst;
		fORList[nf].studies  = new RTReferencedStudy[nst];

		for (int i=0; i<nst; i++)
		{
			fORList[nf].studies[i] = new RTReferencedStudy();
			DicomObject refStudy   = seqRtRefStudies.getDicomObject(i);

			String UID = refStudy.getString(Tag.ReferencedSOPInstanceUID);
			fORList[nf].studies[i].SOPInstanceUID = UID;
			studyUIDs.add(UID);

			DicomElement seqRefSeries = refStudy.get(Tag.RTReferencedSeriesSequence);
			if (seqRefSeries == null)
			{
				issues.add("Input does not contain required information about"
							  + " the series on which the regions-of-interest were drawn.");
				return;
			}

			int nse = seqRefSeries.countItems();
			fORList[nf].studies[i].nSeries = nse;
			fORList[nf].studies[i].series  = new RTReferencedSeries[nse];

			for (int j=0; j<nse; j++)
			{
				fORList[nf].studies[i].series[j] = new RTReferencedSeries();
				DicomObject refSeries            = seqRefSeries.getDicomObject(j); 

				UID = refSeries.getString(Tag.SeriesInstanceUID);
				fORList[nf].studies[i].series[j].UID = UID;
				seriesUIDs.add(UID);

				DicomElement seqContourImage = refSeries.get(Tag.ContourImageSequence);
				if (seqContourImage == null)
				{
					issues.add("Input does not contain required information about "
									 + " the images on which the regions-of-interest were drawn.\n\n");
					return;
				}
					
					
				int ni = seqContourImage.countItems();
				fORList[nf].studies[i].series[j].nImages   = ni;
				fORList[nf].studies[i].series[j].imageList = new ContourImage[ni];

				for (int k=0; k<ni; k++)
				{
					fORList[nf].studies[i].series[j].imageList[k] = new ContourImage();
					DicomObject contourImage = seqContourImage.getDicomObject(k);

					UID = contourImage.getString(Tag.ReferencedSOPInstanceUID);
					fORList[nf].studies[i].series[j].imageList[k].SOPInstanceUID = UID;

					String SOPClassUID = contourImage.getString(Tag.ReferencedSOPClassUID); 
					fORList[nf].studies[i].series[j].imageList[k].SOPClassUID = SOPClassUID;

					SOPInstanceUIDs.add(UID);                
				}  
			}       
		}         
	}      


   
   
   /**
    * Check that all the relevant studies, series and SOPInstances have been
    * loaded into the XNAT database.
	 * @param xnprf profile data for current XNAT connection
    * @param issues ArrayList to which any problems found will be added 
    */
   protected void dependenciesInDatabase(XNATProfile xnprf, List<String> issues)
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
      if (ambiguousSubjExp.size() == 0) checkForSubjectSessionAmbiguities(issues);
      checkForScansInDatabase(issues);
   }
	
   
   protected void checkForSubjectSessionAmbiguities(List<String> issues)
   {
      // The project has already been specified in the UI. In the current
      // incarnation, this comes about by uploading to a profile that consists
      // of only one project, but this will change.
      XNATProjectID = xnprf.getProjectList().get(0);
      
      String                RESTCommand;
      XNATRESTToolkit       xnrt = new XNATRESTToolkit(xnprf);
      Vector2D<String>      result;
      String[]              parseResult;
      Document              resultDoc;
      XNATNamespaceContext  XNATns = new XNATNamespaceContext();
      
      try
      {         
         // Find information on all the studies in the project.
         // This could be time-consuming for a large database.
         RESTCommand = "/data/archive/projects/" + XNATProjectID + "/experiments"
                       + "?xsiType=xnat:imageSessionData"
                       + "&columns=xnat:imageSessionData/UID,xnat:imageSessionData/label,xnat:imageSessionData/subject_ID"
                       + "&format=xml";
         result      = xnrt.RESTGetResultSet(RESTCommand);
      }
      catch (XNATException exXNAT)
      {
         issues.add("Problem checking for subject and session ambiguities: "
					     + exXNAT.getMessage());
			return;
      }
      
      // Check that the DICOM studies from the uploaded RT-STRUCT file are present
      // in the database and add the relevant XNAT experiment and subject IDs
      // to the lists for disambiguation.
      for (int i=0; i<studyUIDs.size(); i++)
      {
         if (!result.columnContains(1, studyUIDs.get(i)))
			{
            issues.add("The DICOM study with UID " + studyUIDs.get(i) + "\n"
                            + " is referenced by the file you are loading,"
                            + " but it is not yet in the database.\n"
                            + "Please ensure that all files on which this"
                            + " structure set is dependent are already loaded into XNAT.");
            return;
			}
            
         for (int j=0; j<result.size(); j++)
         {
            if (result.atom(1, j).equals(studyUIDs.get(i)))
            {
               String expID    = result.atom(0, j);
               String expLabel = result.atom(2, j);
               String subjID   = result.atom(3, j);
               String subjLabel;
               
               try
               {
                  // Retrieve the subject label for a given subject ID.
                  RESTCommand = "/data/archive/projects/" + XNATProjectID
                                + "/subjects/"            + subjID
                                + "?format=xml";
                  resultDoc   = xnrt.RESTGetDoc(RESTCommand);                 
                  parseResult = XMLUtilities.getAttribute(resultDoc, XNATns, "xnat:Subject", "label");
                  subjLabel   = parseResult[0];
               }
               catch (XMLException | XNATException ex)
               {
                  issues.add("Problem retrieving the subject label for subject ID" + subjID
								     + ": " + ex.getMessage());
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
   
   
   public void checkForScansInDatabase(List<String> issues)
   {      
      String                RESTCommand;
      XNATRESTToolkit       xnrt = new XNATRESTToolkit(xnprf);
      Vector2D<String>      result;
      String[][]            parseResult;
      Document              resultDoc;
      XNATNamespaceContext  XNATns = new XNATNamespaceContext();
      
      XNATScanID = new ArrayList<>();
      
      try
      {
         RESTCommand = "/data/archive/projects/" + XNATProjectID
                       + "/subjects/"            + XNATSubjectID
                       + "/experiments/"         + XNATExperimentID
                       + "?format=xml";
         resultDoc   = xnrt.RESTGetDoc(RESTCommand);
         parseResult = XMLUtilities.getAttributes(resultDoc, XNATns, "xnat:scan",
                                             new String[] {"ID", "UID"});
      }
      catch (XMLException | XNATException ex)
      {
         issues.add("Problem retrieving experiment list for subject " + XNATSubjectID
					     + ": " + ex.getMessage());
			return;
      }
      
      // Now process all the seriesUIDs found when passing the input DICOM file. 
      for (int i=0; i<seriesUIDs.size(); i++)
      {
         boolean present = false;
         for (int j=0; j<parseResult.length; j++)
         {
            // Not all of the returned values correspond to scans. Some might be
            // assessors, with no SOPInstanceUID. These need to be screened out.
            if (parseResult[j][1] != null)
            {
               if (parseResult[j][1].equals(seriesUIDs.get(i)))
               {
                  present = true;
                  XNATScanID.add(parseResult[j][0]);
               }
            }
         }
      
         if (!present)
			{
				issues.add("The DICOM series with UID " + studyUIDs.get(i) + "\n"
                           + " is referenced by the file you are loading,"
                           + " but it is not yet in the database.\n"
                           + "Please ensure that all files on which this"
                           + "structure set is dependent are already loaded into XNAT.");
				return;
			}
      }
      
      // We need a list of the actual data files in the repository
      // that are referenced, to go in the "in" section of qcAssessmentData.
      // See the Class DICOMFileListWorker for an example of how to do this
      // both if the files are local or remote. Here, for simplicity, I don't
      // assume anything and use the REST method whether the files are local
      // or remote. 
     // fileSOPMap = new TreeMap<String, String>();
      
      for (int i=0; i<XNATScanID.size(); i++)
      {
         try
         {
            RESTCommand = "/data/archive/projects/"    + XNATProjectID
                             + "/subjects/"            + XNATSubjectID
                             + "/experiments/"         + XNATExperimentID
                             + "/scans/"               + XNATScanID.get(i)
                             + "/resources/DICOM?format=xml";
            resultDoc   = xnrt.RESTGetDoc(RESTCommand);
            parseResult = XMLUtilities.getAttributes(resultDoc, XNATns, "cat:entry",
                                                     new String[] {"URI", "UID"});
         }
         catch(XNATException | XMLException ex)
         {
            issues.add("Problem finding correct image data files in the repository for subject "
				           + XNATSubjectID + ": " + ex.getMessage());
				return;
         }
         
         // Cater for the obscure case where parseResult comes back null. This
         // happened to me after I had (manually) screwed up the data repository.
         if (parseResult == null)
			{
				issues.add("There are no relevant DICOM image files. This might be an \n"
                        + " inconsistent condition in the repository. Please contact \n"
                        + " the system administrator.");
				return;
			}

         for (int j=0; j<parseResult.length; j++)
         {
            if (SOPInstanceUIDs.contains(parseResult[j][1]))
               fileSOPMap.put(parseResult[j][1], parseResult[j][0]);
               fileScanMap.put(parseResult[j][1], XNATScanID.get(i));
         }
      }
      
      
      // Retrieve some further demographic information so that it is available
      // for output where necessary.
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
      catch (XNATException | XMLException ex)
      {
         issues.add("Problems retrieving demographic information.");
      }
 
   }
   
   /**
    * Extract information on the regions-of-interest described by this structure
    * set. Note that this DICOM sequence (3006,0020) presents an "overview" of
    * the ROIs and does not contain the contours themselves.
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractStructureSetROIInfo(List<String> issues)
   {
		DicomElement seqSSRoi = bdo.get(Tag.StructureSetROISequence);

		if (seqSSRoi == null)
		{
			issues.add("Source contains no information on regions-of-interest.");
			return;
		}

		int nRoi = seqSSRoi.countItems();
		roiList = new StructureSetROI[nRoi];

		for (int i=0; i<nRoi; i++)
		{
			roiList[i] = new StructureSetROI();
			DicomObject ROI = seqSSRoi.getDicomObject(i);

			roiList[i].roiNumber                     = ROI.getInt(Tag.ROINumber);

			// Note: The following entry will be set during the contour parsing
			//       section below.
			roiList[i].correspondingROIContour       = -1;

			// Note: The following entry will be set during the RT ROIobservation
			//       list parsing section below.
			roiList[i].correspondingROIObservation   = -1;

			roiList[i].referencedFrameOfReferenceUID = ROI.getString(Tag.ReferencedFrameOfReferenceUID);
			roiList[i].roiName                       = ROI.getString(Tag.ROIName);
			roiList[i].roiDescription                = ROI.getString(Tag.ROIDescription);

			if (ROI.contains(Tag.ROIVolume))
				roiList[i].roiVolume                  = ROI.getFloat(Tag.ROIVolume);
			roiList[i].roiGenerationAlgorithm        = ROI.getString(Tag.ROIGenerationAlgorithm);
			roiList[i].roiGenerationDescription      = ROI.getString(Tag.ROIGenerationDescription);
			// N.B. For the moment implementing the "derivation code sequence" (0008,9215)
			// seems too much like hard work and clutter for no obvious gain.
			// Decision to be reviewed in due course.
		}   
	}
   
   
   
   /**
    * Extract information on the contours describing the ROIs, as described by the
    * DICOM sequence at (3006,0039). Note that the contours are defined in a
    * completely separate DICOM sequence from the ROI's (which come from the
    * StructureSetROI sequence above) and have to be linked through the 
    * ReferencedROINumber.
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractContourInfo(List<String> issues)
   {
		DicomElement seqRoiContour = bdo.get(Tag.ROIContourSequence);
		if (seqRoiContour == null)
		{
			issues.add("Source does not contain any contour information.");
			return;
		}

		int nc = seqRoiContour.countItems();
		roiContourList = new ROIContour[nc];

		for (int i=0; i<nc; i++)
		{
			roiContourList[i] = new ROIContour();
			DicomObject roiC = seqRoiContour.getDicomObject(i);

			roiContourList[i].referencedRoiNumber = roiC.getInt(Tag.ReferencedROINumber);
			roiContourList[i].roiDisplayColour    = roiC.getInts(Tag.ROIDisplayColor, new int[3]);

			// Search through the list of ROI's for the one with the above
			// referencedRoiNumber and insert the matching contour list number
			// for later cross-referencing. In addition, copy the frame-of-reference
			// information here for use by other routines.
			for (int j=0; j<roiList.length; j++)
			{
				if (roiList[j].roiNumber == roiContourList[i].referencedRoiNumber)
				{
					roiList[j].correspondingROIContour    = i;
					roiContourList[i].frameOfReferenceUID = roiList[j].referencedFrameOfReferenceUID;
				}
			}

			DicomElement seqContour = roiC.get(Tag.ContourSequence);
			if (seqContour == null)
			{
				logger.warn("No contours found in source for referenced ROI number " +
									 + roiContourList[i].referencedRoiNumber + ". \n\n");
				// Is this actually an error?
				//issues.add("No contours found in source for referenced ROI number " +
				//                + roiContourList[i].referencedRoiNumber + ". \n\n");
			}
			else
			{            
				int nCont = seqContour.countItems();
				roiContourList[i].contourList = new Contour[nCont];

				for (int j=0; j<nCont; j++)
				{
					roiContourList[i].contourList[j] = new Contour();
					DicomObject contour = seqContour.getDicomObject(j);

					if (contour.contains(Tag.ContourNumber))
						roiContourList[i].contourList[j].contourNumber
											 = contour.getInt(Tag.ContourNumber);

					if (contour.contains(Tag.AttachedContours))
						roiContourList[i].contourList[j].attachedContours
											 = contour.getInts(Tag.AttachedContours);

					roiContourList[i].contourList[j].geometricType
											 = contour.getString(Tag.ContourGeometricType);

					if (contour.contains(Tag.ContourSlabThickness))
						roiContourList[i].contourList[j].slabThickness
											  = contour.getFloat(Tag.AttachedContours);

					if (contour.contains(Tag.ContourOffsetVector))
						roiContourList[i].contourList[j].offsetVector
											  = contour.getFloats(Tag.ContourOffsetVector);

					int n = contour.getInt(Tag.NumberOfContourPoints);
					roiContourList[i].contourList[j].nContourPoints = n;

					roiContourList[i].contourList[j].contourPoints = new float[n][3];

					float[] coords = contour.getFloats(Tag.ContourData);
					int nCoords = (coords == null) ? -1 : coords.length;
					if (nCoords != 3*n)
					{
						issues.add("The source does not contain the correct information about "
											 + " the contour datapoints themselves.");
						return;
					}


					for (int k=0; k<n; k++)
						for (int m=0; m<3; m++)
							roiContourList[i].contourList[j].contourPoints[k][m]
								  = coords[k*3 + m];


					DicomElement seqContourImage = contour.get(Tag.ContourImageSequence);
					if (seqContourImage == null)
					{
						issues.add("The source does not contain required information about "
											 + " the images on which the regions-of-interest were drawn.");
						return;
					}

					int ni = seqContourImage.countItems();
					roiContourList[i].contourList[j].imageList = new ContourImage[ni];

					for (int k=0; k<ni; k++)
					{
						roiContourList[i].contourList[j].imageList[k] = new ContourImage();
						DicomObject contourImage = seqContourImage.getDicomObject(k);

						String uid = contourImage.getString(Tag.ReferencedSOPInstanceUID);
						roiContourList[i].contourList[j].imageList[k].SOPInstanceUID = uid;

						// Check that the corresponding file is definitely present in XNAT.
						// Ignore this test if XNATExperiment has not yet been disambiguated.
						if ((XNATExperimentID != null) && (!fileSOPMap.containsKey(uid)))
						{
							issues.add("The image data file corresponding to the DICOM "
							  + "SOPInstance UID " + uid + " is not present in the XNAT database."
							  + "You must upload all required contour base image data "
							  + "before uploading the RT-STRUCT file.");
							return;
						}            
					}
				}
			}
		}
   }
      
      
   /**
    * Extract information on the radiotherapy interpretation of the ROIs,
    * as described by the DICOM RT ROI observations sequence at (3006,0039).
    * Note that we choose not to support saving observations separately from
    * ROIs. Instead, we abstract any relevant information from the observations
    * and attach it to the corresponding ROI.
    * @param issues ArrayList to which any problems found will be added
    */
   protected void extractRTROIObservationsInfo(List<String> issues)
   {
		DicomElement seqRtRoiObs = bdo.get(Tag.RTROIObservationsSequence);

		// Note: Strictly, DICOM requires this item to be present. However,
		// it is not clear that in a non-radiotherapy context there would
		// be any meaning attached to this. So, do not generate an error if
		// it is not present.
		if (seqRtRoiObs == null) return;

		int nObs = seqRtRoiObs.countItems();
		roiObsList = new RTROIObservation[nObs];

		for (int i=0; i<nObs; i++)
		{
			roiObsList[i] = new RTROIObservation();
			DicomObject obs = seqRtRoiObs.getDicomObject(i);

			roiObsList[i].referencedRoiNumber = obs.getInt(Tag.ReferencedROINumber);
			roiObsList[i].obsNumber           = obs.getInt(Tag.ObservationNumber);
			roiObsList[i].obsLabel            = obs.getString(Tag.ROIObservationLabel);
			roiObsList[i].obsDescription      = obs.getString(Tag.ROIObservationDescription);

			// Search through the list of ROI's for the one with the above
			// referencedRoiNumber and insert the matching contour list number
			// for later cross-referencing.
			for (int j=0; j<roiList.length; j++)
				if (roiList[j].roiNumber == roiObsList[i].referencedRoiNumber)
					roiList[j].correspondingROIObservation = i;

			DicomElement seqRtRelatedRoi = obs.get(Tag.RTRelatedROISequence);
			if (seqRtRelatedRoi != null)
			{
				int nRelRoi = seqRtRelatedRoi.countItems();
				roiObsList[i].relatedROIs = new RTRelatedROI[nRelRoi];

				for (int j=0; j<nRelRoi; j++)
				{
					roiObsList[i].relatedROIs[j] = new RTRelatedROI();
					DicomObject rtRel = seqRtRelatedRoi.getDicomObject(j);

					roiObsList[i].relatedROIs[j].referencedRoiNumber
										 = rtRel.getInt(Tag.ReferencedROINumber);
					roiObsList[i].relatedROIs[j].relationship
										 = rtRel.getString(Tag.RTROIRelationship);
				}
			}   

			// Ignore RT ROI Identification Code sequence. (3006,0086)

			DicomElement seqRtRelatedRoiObs = obs.get(Tag.RelatedRTROIObservationsSequence);
			if (seqRtRelatedRoiObs != null)
			{
				int nRelRoiObs = seqRtRelatedRoiObs.countItems();
				roiObsList[i].relatedROIObservations = new int[nRelRoiObs];

				for (int j=0; j<nRelRoiObs; j++)
				{
					DicomObject rtRelObs = seqRtRelatedRoiObs.getDicomObject(j);
					roiObsList[i].relatedROIObservations[j] = rtRelObs.getInt(Tag.ObservationNumber);
				}
			}

			roiObsList[i].rtRoiInterpretedType = obs.getString(Tag.RTROIInterpretedType);
			roiObsList[i].roiInterpreter       = obs.getString(Tag.ROIInterpreter);
			// This is our best guess at the user for the provenance record,
			// in the absence of any other information in the DICOM file.

			roiObsList[i].roiMaterialID        = obs.getString(Tag.MaterialID);

			DicomElement seqPhysProps = obs.get(Tag.ROIPhysicalPropertiesSequence);
			if (seqPhysProps != null)
			{
				int nProps = seqPhysProps.countItems();
				roiObsList[i].roiPhysicalProps = new ROIPhysicalProperties[nProps];

				for (int j=0; j<nProps; j++)
				{
					roiObsList[i].roiPhysicalProps[j] = new ROIPhysicalProperties();
					DicomObject props = seqPhysProps.getDicomObject(j);

					roiObsList[i].roiPhysicalProps[j].propertyName
							  = props.getString(Tag.ROIPhysicalProperty);

					roiObsList[i].roiPhysicalProps[j].propertyValue
							  = props.getString(Tag.ROIPhysicalPropertyValue);

					DicomElement seqElemComp = obs.get(Tag.ROIElementalCompositionSequence);
					if (seqElemComp != null)
					{
						int nElem = seqElemComp.countItems();
						roiObsList[i].roiPhysicalProps[j].elementalComp = new ElementalComposition[nElem];

						for (int k=0; k<nElem; k++)
						{
							roiObsList[i].roiPhysicalProps[j].elementalComp[k] = new ElementalComposition();
							DicomObject elem = seqElemComp.getDicomObject(k);

							roiObsList[i].roiPhysicalProps[j].elementalComp[k].atomicNumber
									  = elem.getInt(Tag.ROIElementalCompositionAtomicNumber);

							roiObsList[i].roiPhysicalProps[j].elementalComp[k].atomicMassFraction
									  = elem.getInt(Tag.ROIElementalCompositionAtomicMassFraction);
						}
					}
				}
			}            
		}                  
   }
	
	
   /**
    * Take a date String variable in the form that is used by DICOM and
    * convert it to the XNAT date format yyyy-mm-dd.
    * @param date DICOM-formatted variable
    * @return an output String containing the date formatted appropriately for XNAT
    * @throws DataFormatException
    */
   public String convertToXNATDate(String date) throws DataFormatException
   {
      String month;
      String day;
      String year;
      
      try
      {
         month = date.substring(4, 6);
         day   = date.substring(6, 8);
         year  = date.substring(0, 4);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.TIME);
      }

      return year + "-" + month + "-" + day;
   }


   /**
    * Take a time String variable in the form that is used by DICOM and convert
    * it to the XNAT time format hh:mm:ss.
    * @param time and input structureSetTime String
    * @return A String containing the structureSetDate
    * @throws DataFormatException
    */
   public String convertToXNATTime(String time) throws DataFormatException
   {
      String hour;
      String minute;
      String second;
      
      try
      {
         hour   = time.substring(0, 2);
         minute = time.substring(2, 4);
         second = time.substring(4, 6);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.TIME);
      }

      return hour + ":" + minute + ":" + second;
   }
   

   /**
    * Take a date String variable in the form that is used by XNAT and
    * convert it to the DICOM date format yyyymmdd.
    * @param date an input structureSetDate String
    * @return an output String containing the structureSetDate formatted for DICOM
    * @throws DataFormatException
    */
   public String convertToDICOMDate(String date) throws DataFormatException
   {
      String month;
      String day;
      String year;
      
      try
      {
         month = date.substring(5, 7);
         day   = date.substring(8, 10);
         year  = date.substring(0, 4);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }

      return year + month + day;
   }


   /**
    * Take a String variable in the form that is used by XNAT and
    * convert it to the DICOM time format hhmmss.
    * @param time and input structureSetTime String
    * @return A String containing the structureSetTime formatted for DICOM
    * @throws DataFormatException
    */
   public String convertToDICOMTime(String time) throws DataFormatException
   {
      String hour;
      String minute;
      String second;
      
      try
      {
         hour   = time.substring(0, 2);
         minute = time.substring(3, 5);
         second = time.substring(6, 8);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.TIME);
      }

      return hour + minute + second;
   }
   
   /**
    * Output the current contents of the object in DICOM RT-STRUCT format.
    * This method draws heavily on an analysis of a sample RT-STRUCT file that
    * I have acquired and may need extending as I learn more.
    * 
    * One important question that may become clearer is whether we should
    * track information from input DICOM RT-STRUCT to output, when we would
    * not otherwise store it in the XNAT database. For example, my prototype
    * RT-STRUCT file has information from the Clinical Trials group 0012, which
    * is not pertinent to the process of exchanging ROI's and not a formal part
    * of the DICOM structure set module. We can exclude this information by
    * setting <code>odo = new BasicDicomObject()</code> below. However, an
    * equally powerful argument is that we should not lose any data during
    * conversion. So the current solution is to copy the original parameters in
    * bdo and replace anything that might have changed. Arguably, we are parsing
    * and rewriting things that could just stay as they were in bdo. However,
    * this work is really in preparation for writing the DICOM from a source
    * of ROI data where we do not have any original DICOM.
    * 
    * @return DCM4CHE BasicDicomObject ready to write to a file.
    */
   @Override
   public DicomObject createDICOM() throws Exception
   {
      DicomObject odo = new BasicDicomObject();
      bdo.copyTo(odo);
      
      odo.initFileMetaInformation(UID.ExplicitVRLittleEndian);
      odo.putString(Tag.MediaStorageSOPClassUID, VR.UI, UID.RTStructureSetStorage);
      
      String uid = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DATA_UPLOADER,
                                                  UIDGenerator.RT_STRUCT,
                                                  UIDGenerator.SeriesInstanceUID);
      odo.putString(Tag.MediaStorageSOPInstanceUID, VR.UI, uid);
      
      odo.putString(Tag.SpecificCharacterSet,    VR.CS, "ISO_IR 100");

      String formattedDate = String.format("%1$tY%1$tm%1$td", Calendar.getInstance());              
      odo.putString(Tag.InstanceCreationDate,    VR.DA, formattedDate);
      
      String formattedTime = String.format("%1$tH%1$tM%1$tS.%1$tM", Calendar.getInstance());
      odo.putString(Tag.InstanceCreationTime,    VR.TM, formattedTime);

      odo.putString(Tag.SOPClassUID,             VR.UI, UID.RTStructureSetStorage);
      odo.putString(Tag.SOPInstanceUID,          VR.UI, uid);
      
      
      odo.putString(Tag.StudyDate,               VR.DA, studyDate);
      odo.putString(Tag.StudyTime,               VR.TM, studyTime);
      odo.putString(Tag.Modality,                VR.CS, "RTSTRUCT");    
      odo.putString(Tag.Manufacturer,            VR.LO, "Institute of Cancer Research");
      odo.putString(Tag.StationName,             VR.SH, InetAddress.getLocalHost().getHostName());      
      odo.putString(Tag.StudyDescription,        VR.LO, studyDescription);
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
      odo.putString(Tag.StudyInstanceUID,        VR.UI, studyUIDs.get(0));
      
      uid = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DATA_UPLOADER,
                                           UIDGenerator.RT_STRUCT,
                                           UIDGenerator.SeriesInstanceUID);
      odo.putString(Tag.SeriesInstanceUID,       VR.UI, uid);
      
      odo.putString(Tag.StudyID,                 VR.SH, XNATExperimentID);
      
      // Very tricky to know what to put here, or what use other software might
      // make of the value. My exemplar file had 1 here.
      odo.putString(Tag.SeriesNumber,            VR.IS, "1");
      
      
      // The label has to be there according to the standard, but the name and
      // description don't.
      odo.putString(Tag.StructureSetLabel,       VR.SH, structureSetLabel);
      
      if ((structureSetName != null) && (!structureSetName.isEmpty()))
         odo.putString(Tag.StructureSetName,     VR.LO, structureSetName);
      
      if ((structureSetDescription != null) && (!structureSetDescription.isEmpty()))
         odo.getString(Tag.StructureSetDescription, VR.ST, structureSetDescription);
      
      if (instanceNumber != DUMMY_INT)
         odo.putInt(Tag.InstanceNumber,          VR.IS, instanceNumber);
      odo.putString(Tag.StructureSetDate,        VR.DA, structureSetDate);
      odo.putString(Tag.StructureSetTime,        VR.TM, structureSetTime);
      
      outputReferencedFrameOfReferenceSequence(odo);
      outputStructureSetROISequence(odo);
      outputROIContourSequence(odo);
      outputRTROIObservationsSequence(odo);
     
      return odo;
   }
     
   
   /**
    * Write the DICOM sequence at (0008, 1110)
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputReferencedStudySequence(DicomObject odo)
   {
      if (refStudyList.length != 0)
      {
         DicomElement seqRtRefStudy = odo.putSequence(Tag.ReferencedStudySequence);
         for (int i=0; i<refStudyList.length; i++)
         {
            DicomObject  doRtRefStudy = new BasicDicomObject();
            doRtRefStudy.setParent(odo);
            seqRtRefStudy.addDicomObject(doRtRefStudy);
            
            ReferencedStudy refStudy = refStudyList[i];

            doRtRefStudy.putString(Tag.ReferencedSOPInstanceUID, VR.UI, refStudy.SOPInstanceUID);
            doRtRefStudy.putString(Tag.ReferencedSOPClassUID,    VR.UI, refStudy.SOPClassUID);
         }
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0010)
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputReferencedFrameOfReferenceSequence(DicomObject odo)
   {
      if (fORList.length != 0)
      {
         DicomElement seqRefFOR = odo.putSequence(Tag.ReferencedFrameOfReferenceSequence);
         for (int i=0; i<fORList.length; i++)
         {
            DicomObject  doRefFOR = new BasicDicomObject();
            doRefFOR.setParent(odo);
            seqRefFOR.addDicomObject(doRefFOR);
            
            ReferencedFrameOfReference fOR = fORList[i];

            doRefFOR.putString(Tag.FrameOfReferenceUID, VR.UI, fOR.UID);

            if (fOR.nRelatedFOR != 0)
            {
               // Subsequence at (3006,00C0)
               DicomElement seqFORRel
                  = doRefFOR.putSequence(Tag.FrameOfReferenceRelationshipSequence);
               for (int j=0; j<fOR.nRelatedFOR; j++)
               {
                  DicomObject doFORRel = new BasicDicomObject();
                  doFORRel.setParent(doRefFOR);
                  seqFORRel.addDicomObject(doFORRel);
                  
                  RelatedFrameOfReference rFOR = fOR.relatedFOR[j];
                  
                  doFORRel.putString(Tag.RelatedFrameOfReferenceUID, VR.UI, rFOR.UID );
                  
                  doFORRel.putString(Tag.FrameOfReferenceTransformationType, VR.CS,
                                   "HOMOGENEOUS" );
                  
                  doFORRel.putString(Tag.FrameOfReferenceTransformationMatrix, VR.DS,
                                   rFOR.transformationMatrix);
                  
                  doFORRel.putString(Tag.FrameOfReferenceTransformationComment, VR.LO,
                                   rFOR.transformationComment);
               }
            }
            
            if (fOR.nStudies != 0)
            {
               // Subsequence at (3006,0012)
               DicomElement seqRtRefStudy = doRefFOR.putSequence(Tag.RTReferencedStudySequence);
               for (int j=0; j<fOR.nStudies; j++)
               {
                  DicomObject doRtRefStudy = new BasicDicomObject();
                  doRtRefStudy.setParent(doRefFOR);
                  seqRtRefStudy.addDicomObject(doRtRefStudy);
                  
                  RTReferencedStudy study = fOR.studies[j];
                  
                  doRtRefStudy.putString(Tag.ReferencedSOPInstanceUID, VR.UI, study.SOPInstanceUID);
                  doRtRefStudy.putString(Tag.ReferencedSOPClassUID,    VR.UI, study.SOPClassUID);
                  
                  // Subsequence at (3006,0014)
                  DicomElement seqRtRefSer
                     = doRtRefStudy.putSequence(Tag.RTReferencedSeriesSequence);
                  for (int k=0; k<study.nSeries; k++)
                  {
                     DicomObject doRtRefSer = new BasicDicomObject();
                     doRtRefSer.setParent(doRtRefStudy);
                     seqRtRefSer.addDicomObject(doRtRefSer);
                     
                     doRtRefSer.putString(Tag.SeriesInstanceUID, VR.UI,
                                           study.series[k].UID);
                     
                     // Subsequence at (3006,0016)
                     DicomElement seqContourImage
                        = doRtRefSer.putSequence(Tag.ContourImageSequence);
                     for (int l=0; l<study.series[k].imageList.length; l++)
                     {
                        DicomObject doContourImage = new BasicDicomObject();
                        doContourImage.setParent(doRtRefSer);
                        seqContourImage.addDicomObject(doContourImage);
                        
                        doContourImage.putString(Tag.ReferencedSOPClassUID, VR.UI,
                           study.series[k].imageList[l].SOPClassUID);
                        
                        doContourImage.putString(Tag.ReferencedSOPInstanceUID, VR.UI,
                           study.series[k].imageList[l].SOPInstanceUID);
                     }
                  }
               }      
            }
         }
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0020).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputStructureSetROISequence(DicomObject odo)
   {
      if ((roiList != null) && (roiList.length != 0))
      {
         DicomElement seqSSRoi = odo.putSequence(Tag.StructureSetROISequence);
         for (int i=0; i<roiList.length; i++)
         {
            StructureSetROI ssROI = roiList[i];
            
            DicomObject doSSRoi = new BasicDicomObject();
            doSSRoi.setParent(odo);
            seqSSRoi.addDicomObject(doSSRoi);
            
            doSSRoi.putInt(Tag.ROINumber, VR.IS, ssROI.roiNumber);          
            doSSRoi.putString(Tag.ReferencedFrameOfReferenceUID, VR.UI,
                                      ssROI.referencedFrameOfReferenceUID);
            doSSRoi.putString(Tag.ROIName, VR.LO, ssROI.roiName);
            
            if ((ssROI.roiDescription != null) && (!ssROI.roiDescription.isEmpty()))
               doSSRoi.putString(Tag.ROIDescription, VR.ST, ssROI.roiDescription);
            
            if (ssROI.roiVolume != DUMMY_FLOAT)
               doSSRoi.putFloat(Tag.ROIVolume, VR.DS, ssROI.roiVolume);
            
            doSSRoi.putString(Tag.ROIGenerationAlgorithm, VR.CS, ssROI.roiGenerationAlgorithm);
            
            if ((ssROI.roiGenerationDescription != null) && (!ssROI.roiGenerationDescription.isEmpty()))
               doSSRoi.putString(Tag.ROIGenerationDescription, VR.LO, ssROI.roiGenerationDescription);
         }     
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0039).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputROIContourSequence(DicomObject odo)
   {
      if ((roiContourList != null) && (roiContourList.length != 0))
      {
         DicomElement seqRoiContour = odo.putSequence(Tag.ROIContourSequence);
         for (int i=0; i<roiContourList.length; i++)
         {
            DicomObject doRoiContour = new BasicDicomObject();
            doRoiContour.setParent(odo);
            seqRoiContour.addDicomObject(doRoiContour);
            
            ROIContour roiCont = roiContourList[i];
            
            doRoiContour.putInt(Tag.ReferencedROINumber, VR.IS, roiCont.referencedRoiNumber);
            
            if (roiCont.roiDisplayColour[0] != -1)
               doRoiContour.putInts(Tag.ROIDisplayColor, VR.IS, roiCont.roiDisplayColour);
            
            if ((roiCont.contourList != null) && (roiCont.contourList.length != 0))
            {
               // Subsequence at (3006, 0040)
               DicomElement seqContour = doRoiContour.putSequence(Tag.ContourSequence);
               for (int j=0; j<roiCont.contourList.length; j++)
               {
                  DicomObject doContour = new BasicDicomObject();
                  doContour.setParent(doRoiContour);
                  seqContour.addDicomObject(doContour);
                  
                  Contour cont = roiCont.contourList[j];
                  
                  if (cont.contourNumber != DUMMY_INT)
                     doContour.putInt(Tag.ContourNumber, VR.IS, cont.contourNumber);
                  
                  if (cont.attachedContours[0] != DUMMY_INT)
                     doContour.putInts(Tag.AttachedContours, VR.IS, cont.attachedContours);
                  
                  if ((cont.imageList != null) && (cont.imageList.length != 0))
                  {
                     DicomElement seqContourImage = doContour.putSequence(Tag.ContourImageSequence);
                     for (int k=0; k<cont.imageList.length; k++)
                     {
                        DicomObject doContourImage = new BasicDicomObject();
                        doContourImage.setParent(doContour);
                        seqContourImage.addDicomObject(doContourImage);
                        
                        doContourImage.putString(Tag.ReferencedSOPClassUID, VR.UI,
                            cont.imageList[k].SOPClassUID);
                        
                        doContourImage.putString(Tag.ReferencedSOPInstanceUID, VR.UI,
                            cont.imageList[k].SOPInstanceUID);
                     }
                  }
                  
                  doContour.putString(Tag.ContourGeometricType,    VR.CS, cont.geometricType);
                   
                  if (cont.slabThickness != DUMMY_FLOAT)
                     doContour.putFloat( Tag.ContourSlabThickness, VR.DS, cont.slabThickness);
                  
                  if (cont.offsetVector[0] != DUMMY_FLOAT)
                     doContour.putFloats(Tag.ContourOffsetVector,  VR.DS, cont.offsetVector);
                  
                  doContour.putInt(Tag.NumberOfContourPoints,      VR.IS, cont.nContourPoints);
                  
                  int n = cont.nContourPoints;
                  if (n != 0)
                  {
                     float[] contPoints = new float[n*3];
                     for (int k=0; k<n; k++)
                        for (int l=0; l<3; l++)
                           contPoints[k*3 + l] = cont.contourPoints[k][l];
                     
                     doContour.putFloats(Tag.ContourData, VR.DS, contPoints);
                  }
               }
            }
         }         
      }
   }
   
   
   /**
    * Write the DICOM sequence at (3006, 0080).
    * @param odo - BasicDicomObject being constructed for output to a file
    */
   protected void outputRTROIObservationsSequence(DicomObject odo)
   {
      if (roiObsList.length != 0)
      {
         DicomElement seqRtRoiObs = odo.putSequence(Tag.RTROIObservationsSequence);
         for (int i=0; i<roiObsList.length; i++)
         {
            DicomObject doRtRoiObs = new BasicDicomObject();
            doRtRoiObs.setParent(odo);
            seqRtRoiObs.addDicomObject(doRtRoiObs);
            
            RTROIObservation obs = roiObsList[i];
            
            doRtRoiObs.putInt(Tag.ObservationNumber,   VR.IS, obs.obsNumber);
            doRtRoiObs.putInt(Tag.ReferencedROINumber, VR.IS, obs.referencedRoiNumber);
            
            if ((obs.obsLabel != null) && (!obs.obsLabel.isEmpty()))
               doRtRoiObs.putString(Tag.ROIObservationLabel, VR.SH, obs.obsLabel);
            
            if ((obs.obsDescription != null) && (!obs.obsDescription.isEmpty()))
               doRtRoiObs.putString(Tag.ROIObservationDescription, VR.ST, obs.obsDescription);
            
            if ((obs.relatedROIs != null) && (obs.relatedROIs.length != 0))
            {
               // Subsequence at (3006, 0030)
               DicomElement seqRtRelRoi = doRtRoiObs.putSequence(Tag.RTRelatedROISequence);
               for (int j=0; j<obs.relatedROIs.length; j++)
               {
                  DicomObject doRtRelRoi = new BasicDicomObject();
                  doRtRelRoi.setParent(doRtRoiObs);
                  seqRtRoiObs.addDicomObject(doRtRelRoi);
                  
                  doRtRelRoi.putInt(Tag.ReferencedROINumber, VR.IS,
                                        obs.relatedROIs[j].referencedRoiNumber);
                     
                  if ((obs.relatedROIs[j].relationship != null) &&
                      (!(obs.relatedROIs[j].relationship.isEmpty())))
                     doRtRelRoi.putString(Tag.RTROIRelationship, VR.CS,
                                        obs.relatedROIs[j].relationship);
               }
            }
            
            if (obs.relatedROIObservations.length != 0)
            {
               // Subsequence at (3006, 00A0)
               DicomElement seqRelRtRoiObs = doRtRoiObs.putSequence(Tag.RelatedRTROIObservationsSequence);
               for (int j=0; j<obs.relatedROIs.length; j++)
               {
                  DicomObject doRelRtRoiObs = new BasicDicomObject();
                  doRelRtRoiObs.setParent(doRtRoiObs);
                  seqRtRoiObs.addDicomObject(doRelRtRoiObs);
                  
                  doRelRtRoiObs.putInt(Tag.ObservationNumber, VR.IS,
                                        obs.relatedROIObservations[j]);
               }
            }
            
            doRtRoiObs.putString(Tag.RTROIInterpretedType, VR.CS, obs.rtRoiInterpretedType);
            doRtRoiObs.putString(Tag.ROIInterpreter,       VR.PN, obs.roiInterpreter);
            
            if ((obs.roiMaterialID != null) && (!obs.roiMaterialID.isEmpty()))
               doRtRoiObs.putString(Tag.MaterialID, VR.SH, obs.roiMaterialID);
            
            if ((obs.roiPhysicalProps != null) && (obs.roiPhysicalProps.length != 0))
            {
               DicomElement seqRoiPhysProp = doRtRoiObs.putSequence(Tag.ROIPhysicalPropertiesSequence);
               for (int j=0; j<obs.roiPhysicalProps.length; j++)
               {
                  DicomObject doRoiPhysProp = new BasicDicomObject();
                  doRoiPhysProp.setParent(doRtRoiObs);
                  seqRoiPhysProp.addDicomObject(doRoiPhysProp);
                  
                  ROIPhysicalProperties phys = obs.roiPhysicalProps[j];
                  
                  doRoiPhysProp.putString(Tag.ROIPhysicalProperty,      VR.CS, phys.propertyName);                 
                  doRoiPhysProp.putString(Tag.ROIPhysicalPropertyValue, VR.CS, phys.propertyValue);
                  
                  if ((phys.elementalComp != null) && (phys.elementalComp.length != 0))
                  {
                     DicomElement seqRoiElemComp = doRoiPhysProp.putSequence(Tag.ROIElementalCompositionSequence);
                     for (int k=0; j<phys.elementalComp.length; k++)
                     {
                        DicomObject doRoiElemComp = new BasicDicomObject();
                        doRoiElemComp.setParent(doRoiPhysProp);
                        seqRoiElemComp.addDicomObject(doRoiElemComp);
                        
                        doRoiElemComp.putInt(Tag.ROIElementalCompositionAtomicNumber, VR.US,
                                      phys.elementalComp[j].atomicNumber);
                        
                        doRoiElemComp.putFloat(Tag.ROIElementalCompositionAtomicMassFraction, VR.FL,
                                      phys.elementalComp[j].atomicMassFraction);
                     }
                  }
               }
            }
         }
      }
   }
}
