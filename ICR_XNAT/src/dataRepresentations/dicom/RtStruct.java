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
* Java class: RtStruct.java
* First created on Jan 25, 2016 at 3:53:12 PM
* 
* Define a representation of the RT-STRUCT data structure, including
* methods to read the data in from a DICOM file and create a new
* DICOM file from an instance. Significantly refactored starting
* 25.1.16.
*********************************************************************/

package dataRepresentations.dicom;

import etherj.aim.DicomImageReference;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import exceptions.DataFormatException;
import exceptions.DataRepresentationException;
import generalUtilities.DicomXnatDateTime;
import generalUtilities.UidGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;

public class RtStruct extends DicomEntity
{
	/* The aim here is to follow the DICOM model fairly closely.
	   The IOD modules mandatory for an RT-STRUCT file are in Table A.1-4
	   of the DICOM standard and are as follows:
	   Patient
	   General Study
	   RT Series
	   General Equipment
	   Structure Set*
	   ROI Contour*
	   RT ROI Obseervations*
	   SOP Common
	  
	   On the assumption that all of the information that is not in the
	   specifically ROI-related IOD modules can be found from the referenced
		images, only the modules marked * are implemented fully with the
		optional tags. For the others, only required tags and a subset of
	   the optional tags are considered here.
	
		There are other IOD modules that are not mandatory for a valid structure
	   set. These are not implemented here to avoid the code becoming too large
	   and unwieldy.
	*/
	
	public SopCommon              sopCommon;
	public Patient                patient;
	public GeneralStudy           generalStudy;
	public RtSeries               rtSeries;
	public GeneralEquipment       generalEquipment;
	public StructureSet           structureSet;
	public List<RoiContour>       roiContourList;
	public List<RtRoiObservation> rtRoiObservationList;

	
	protected RtStruct()
	{
		// The empty constructor is necessary as part of the process for the
		// deepCopy() method and RtStructBuilder class.
	}
	
   /**
    * Constructor with data from an AIM image annotation collection (normally
    * derived from an AIM XML instance file). Note that most of the data needed
    * for the RT-STRUCT is not contained within the AIM source file and we have
    * to seek it out from on of the original image source DICOM files.
    * @param iac an AIM ImageAnnotationCollection parsed from the source XML
    * by James d'Arcy's Etherj package.
	 * @param seriesDoMap a Map that links a DICOM series to one representative
	 * image from that series, thus allowing us to extract the various header
    * parameters to supplement the information in the image annotation.
	 * @throws exceptions.DataFormatException 
    */
   public RtStruct(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
                  throws DataFormatException
   {
      iacCreateSopCommon(iac);
		iacCreatePatient(iac, seriesDoMap);
		iacCreateGeneralStudy();
		iacCreateGeneralEquipment(iac);
		iacCreateRtSeries(iac);
		iacCreateStructureSet(iac, seriesDoMap);
      
		

		
		
		
		
		
     
      
      // Create the specific structure set items from information in the AIM
      // image annotation.
      
      
      
      ReferencedFrameOfReference rfor = new ReferencedFrameOfReference();
      
      // Note: At present, the case of an annotation collection with images
      // from DICOM studies with different frames of reference is not supported.
      // This is because of the expense involved in downloading lots of files
      // from the repository and checking the frame of reference UID.
      rfor.frameOfReferenceUid = rfor.readString(iacDo, Tag.FrameOfReferenceUID, 1);
      
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
      List<ReferencedFrameOfReference> rforList   = new ArrayList<>();
      rforList.add(rfor);
      
      structureSet.referencedFrameOfReferenceList = rforList;
      
   }
   
   private void iacCreateSopCommon(ImageAnnotationCollection iac)
			  throws DataFormatException
	{
		sopCommon = new SopCommon();
      String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      sopCommon.instanceCreationDate = DicomXnatDateTime.convertXnatDateTimeToDicomDate(dateTime);
      sopCommon.instanceCreationTime = DicomXnatDateTime.convertXnatDateTimeToDicomTime(dateTime);
      sopCommon.sopClassUid = UID.RTStructureSetStorage;
      sopCommon.sopInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DAO,
		                                                          UidGenerator.RT_STRUCT,
                                                                UidGenerator.SOPInstanceUID);  
	}
	
	
	private void iacCreatePatient(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
			  throws DataFormatException
	{
		// Arbitrarily, pick the last series in the following loop to provide
		// the template DICOM file from which to pick various patient parameters.
		DicomObject templateDo = null;
		
		// Check that the same subject is referenced in all the images. This is
		// potentially tricky, given that different variants of the name might
		// have been entered differently by the scanner operators and that the
		// images might have been subject to anonymisation.
		String      name1      = null;
		for (String series : seriesDoMap.keySet())
		{
			DicomObject bdo   = seriesDoMap.get(series);
			String      name2 = readString(bdo, Tag.PatientName, 1);
			if ((!name2.equals(name1)) && (name1 != null))
			{
				String msg = "Trying to construct an RT-STRUCT from two sets of "
						       + "images with different patient names. Ensure that "
						       + "source images are uploaded to XNAT with compatible"
						       + "values in the Patient Name field of the DICOM headers.";
				
				throw new DataFormatException(DataFormatException.RTSTRUCT, msg);
			}
			name1      = name2;
			templateDo = bdo;
		}
		
		patient          = new Patient(templateDo);
	}
	
	
	private void iacCreateGeneralStudy()
	{
		generalStudy     = new GeneralStudy();
		generalStudy.studyInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                         UidGenerator.RT_STRUCT,
																							UidGenerator.StudyInstanceUID);
		generalStudy.studyDate = sopCommon.instanceCreationDate;
		generalStudy.studyTime = sopCommon.instanceCreationTime;
	}
	
	
	private void iacCreateGeneralEquipment(ImageAnnotationCollection iac)
	{
		final String DEFAULT = "Unknown";
		generalEquipment = new GeneralEquipment();
      generalEquipment.institutionAddress = DEFAULT;
      generalEquipment.institutionName    = DEFAULT;
      generalEquipment.manufacturer       = "Institute of Cancer Research";
      generalEquipment.modelName          = "ICR XNAT DataUploader";
      List<String> sv = new ArrayList<>();
      sv.add(iac.getAimVersion());
      sv.add("Converted to RT-STRUCT by ICR XNAT DataUploader");
      generalEquipment.softwareVersions   = sv;
      try
		{
			generalEquipment.stationName = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException exUH)
		{
			generalEquipment.stationName = DEFAULT;
		}
	}
	
	
	private void iacCreateRtSeries(ImageAnnotationCollection iac)
	{
		rtSeries                   = new RtSeries();
		rtSeries.modality          = "RTSTRUCT";
		rtSeries.seriesInstanceUid = UidGenerator.createNewDicomUID(UidGenerator.XNAT_DATA_UPLOADER,
				                                                      UidGenerator.RT_STRUCT,
																						UidGenerator.SeriesInstanceUID);
		rtSeries.seriesNumber      = 1;
		rtSeries.seriesDate        = sopCommon.instanceCreationDate;
		rtSeries.seriesTime        = sopCommon.instanceCreationTime;
		rtSeries.seriesDescription = "Image markup from AIM instance with UID " + iac.getUid()
				                       + " converted RT-STRUCT by XNAT DataUploader";
		rtSeries.operatorName      = System.getProperty("user.name");
	}
	
	
	private void iacStructureSet(ImageAnnotationCollection iac, Map<String, DicomObject> seriesDoMap)
			  throws DataFormatException
	{
		structureSet                         = new StructureSet();
      structureSet.structureSetLabel       = "Auto-created structure set";
      structureSet.structureSetName        = iac.getUid() + "_RT-STRUCT";
      structureSet.structureSetDescription = "Image markup from AIM instance document with UID "
                                             + iac.getUid() + " converted RT-STRUCT by ICR XNAT DataUploader";
      structureSet.instanceNumber          = "1";
		structureSet.structureSetDate        = sopCommon.instanceCreationDate;
		structureSet.structureSetTime        = sopCommon.instanceCreationTime;
		
	}
	
	
	/**
	 * Constructor with data from a previously constructed RtStruct object,
	 * and choosing a subset of the existing regions-of-interest
	 * @param src a source RtStruct.
	 * @param rois 
	 */
	public RtStruct(RtStruct src, Set<Integer> rois)
	{
						 
	@Override
	public void writeToDicom(DicomObject rtsDo)
	{
		sopCommon.writeToDicom(rtsDo);
		patient.writeToDicom(rtsDo);
		generalStudy.writeToDicom(rtsDo);
		rtSeries.writeToDicom(rtsDo);
		generalEquipment.writeToDicom(rtsDo);
		structureSet.writeToDicom(rtsDo);
		
		writeSequence(rtsDo, Tag.ROIContourSequence,        VR.SQ, 1, roiContourList);
		writeSequence(rtsDo, Tag.RTROIObservationsSequence, VR.SQ, 1, rtRoiObservationList);
	}
}

