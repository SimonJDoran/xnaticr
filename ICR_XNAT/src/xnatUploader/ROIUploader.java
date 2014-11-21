/********************************************************************
* Copyright (c) 2013, Institute of Cancer Research
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
* Java class: ROIUploader.java
* First created on Oct 24, 2011 at 2:02:05 PM
* 
* Note that ROIUploader is mostly a shell at present, the reason being
* that all ROIs are currently obtained from DICOM RTSTRUCT files. It is
* intended that the latter should be the only type of ROI file that is
* archived in the data repository.
* 
* When an RTSTRUCT file is uploaded, an icr:roiSetData XML file
* (potentially detailing many individual ROIs) is created and uploaded
* to the XNAT PostgreSQL database. At the same structureSetTime a
* separate icr:roiData XML file is uploaded to XNAT for each individual
* ROI. Note that, as all the ROI contour data are contained within the
* RTSTRUCT file, no additional file needs to be opened, parsed or
* uploaded to the repository for the individual ROIs. There is thus
* no need to configure the GUI interface. As a result, many of the
* methods that formally require implementation are never used
* and so are just empty.
*********************************************************************/

package xnatUploader;

import dataRepresentations.ContourRenderer;
import com.generationjava.io.xml.SimpleXmlWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import dataRepresentations.RTStruct;
import dataRepresentations.RTStruct.ROIContour;
import dataRepresentations.RTStruct.RTROIObservation;
import dataRepresentations.RTStruct.StructureSetROI;
import java.util.SortedMap;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;
import xnatDAO.XNATProfile;


public class ROIUploader extends QCAssessmentDataUploader
{
   protected RTStruct                     rts;
   protected StructureSetROI              ssRoi;
   protected ROIContour                   roiCont;
   protected RTROIObservation             roiObs;
   protected SortedMap<String, String>    fileSOPMap;
   protected ArrayList<String>            associatedRoiSetIDs;
	protected ArrayList<String>				seriesUIDs;      
   protected ArrayList<String>				SOPInstanceUIDs;
   
   /**
    * Constructor for the ROIUploader. Note that the XNAT profile
    * xnprf is not used here, but needed in the abstract class DataUploader.
    * @param xnprf 
    */
   public ROIUploader(XNATProfile xnprf)
   {
      super(xnprf);
   }
   
   
   /**
    * This constructor signature caters for the situation in which the ROI
    * to be uploaded has already been parsed by RTStructureSetUploader.
    * @param xnprf XNATProfile, as required by the parent class constructor
    * @param rts RTStruct that has already parsed the ROI data
    * @param roiPos integer corresponding to position of the required ROI
    *        in the roiList of the RTStruct 
    * @param labelPrefix String containing the prefix for the label of the
    *        uploaded data
    * @
    */
   public ROIUploader(RTStruct rts, int roiPos, String labelPrefix,
                      UploadStructure uls)
          throws Exception
   {
      super(rts.xnprf);
      this.rts = rts;
      
      // Most of the metadata are simply subsets of what has already been
      // captured when parsing the structure set and this constructor is primarily
      // an exercise in transcribing variables.      
      ssRoi = rts.roiList[roiPos];
      
      if (ssRoi.correspondingROIContour == -1)
         throw new Exception("Structure set contains no ROIContour sequence for\n"
                 + "ROI " + roiPos + ".");
      roiCont = rts.roiContourList[ssRoi.correspondingROIContour];
      

      // Don't throw an error here. DICOM specifies that each ROI should have
      // a corresponding RT ROI Observation, but we won't have this metadata for
      // non-radiotherapy ROIs.
      if (ssRoi.correspondingROIObservation != -1)
         roiObs = rts.roiObsList[ssRoi.correspondingROIObservation];
      
      uploadStructure  = uls;
      date             = rts.convertToXNATDate(rts.structureSetDate);
      time             = rts.convertToXNATTime(rts.structureSetTime);
      XNATExperimentID = rts.XNATExperimentID;
      XNATProject      = rts.XNATProjectID;
      XNATScanID       = rts.XNATScanID;
      XNATSubjectID    = rts.XNATSubjectID;
      fileSOPMap       = rts.fileSOPMap;
      
      // Create a string that is always four digits long, left-padded with
      // zeroes - makes lexicographical ordering more obvious.
      String roiNum = "000" + roiPos;
      roiNum = roiNum.substring(roiNum.length()-4);      
      setStringField("Label", labelPrefix + "_ROI" + roiNum);
      
      // Extract the subset of series and SOPInstance UIDs that are
      // related to the current ROI. Note that there is a chance that we may
      // over-report the studies for a given ROI. The structure set file has a
      // section that lists the studies referenced by the set of ROIs as a whole.
      // By contrast, the DICOM contour sequence for a given ROI lists the images
      // (not their series) referenced by the individual ROI. Theoretically, we
      // could download each image in the SOPList below and add its Series Instance
      // SOPInstanceUID to the list. However, this is very inefficient for our purposes
      // here. Most of the structureSetTime, the line below is just fine.
      seriesUIDs      = rts.seriesUIDs;      
      SOPInstanceUIDs = new ArrayList<>();
      
      for (int i=0; i<roiCont.contourList.length; i++)
         for (int j=0; j<roiCont.contourList[i].imageList.length; j++)
            SOPInstanceUIDs.add(roiCont.contourList[i].imageList[j].SOPInstanceUID);
   }
   
   
   @Override
   public void updateParseFile()
   {
      // Not needed.
   }
	
   
   /**
    * Abstract method, so must be implemented. No support for reading from file.
    * @return false 
    */
   @Override
   public boolean readFile()
   {
      return false;   
   }
   
    
   /**
    * Abstract method, so must be implemented. No support for reading from file.
    * @return false
    */
   @Override
   public boolean parseFile()
   {
      return false;   
   }
   
   
   /**
    * Abstract method, so must be implemented. No support for user interface, as
    * all currently handled via upload of DICOM-RT files.
    */
   @Override
   public void populateFields(MetadataPanel mdsp)
   {
   }
   
   
   /**
    * Abstract method, so must be implemented. No support for user interface as
    * all currently handled via upload of DICOM-RT or other files.
    */
   @Override
   public void clearFields(MetadataPanel mdsp)
   {
   }
   
   
	@Override
	public void createPrimaryResourceFile()
	{
		// There is no primary data file, because every ROI is the "child" of an
		// ROISet, which is where the primary file is uploaded.
	}
   
   
   
   /**
    * Create additional thumbnail files for upload with the DICOM-RT structure set.
    */
   @Override
   public void createAuxiliaryResourceFiles()
   {
      createInputCatalogueFile("DICOM", "RAW", "referenced contour image");
		
		try
      {
         ContourRenderer cr = new ContourRenderer(rts, ssRoi.correspondingROIContour);
         ArrayList<BufferedImage> thumbnails = cr.createImages();
      
         String          homeDir        = System.getProperty("user.home");
         String          fileSep        = System.getProperty("file.separator");
         String          XNAT_DAO_HOME  = homeDir + fileSep + ".XNAT_DAO" + fileSep;
         String          filePrefix     = XNAT_DAO_HOME + "temp" + fileSep 
                                          + XNATAccessionID + "_ROI_thumbnail_";
         
         for (int i=0; i<thumbnails.size(); i++)
         {
            File outputFile = new File(filePrefix + i + ".png");
            ImageIO.write(thumbnails.get(i), "png", outputFile);
				
				XNATResourceFile rf	= new XNATResourceFile();
				rf.content				= "GENERATED";
				rf.description			= "thumbnail image containing ROI contour";
				rf.format				= "PNG";
				rf.file					= outputFile;
				rf.name					= "RT_THUMBNAIL";
				rf.inOut					= "out";
            auxiliaryFiles.add(rf);
         }
      }
      catch (Exception ex)
      {
         reportError(ex, "create RT thumbnail file");
      }      
   }
	
	
	
	/**
	 * Get the list of files containing the input data used in the creation of this
	 * XNAT assessor. 
	 * @return Array list of String filenames
	 */
   protected ArrayList<String> getInputCatEntries()
	{
		ArrayList<String> fileURIs = new ArrayList<>();
		for (String s : SOPInstanceUIDs) fileURIs.add(fileSOPMap.get(s));
		
		return fileURIs;
	}
   
   
            
   /**
    * Create parts of the metadata XML file that are specific to this
    * particular subclass. Common parts of the XML file are handled by
    * various methods in the parent QCAssessmentUploader.
    * Notice that the one thing that doesn't appear here is a structureSetDescription
    * of the contour data themselves DICOM tag (3006,0050). The purpose of the
    * XNAT database is to store metadata that allow us to access quickly and
    * efficiently the data in the image repository. We do not want to clutter
    * up the PostgreSQL database with large quantities of "image" data.
    */
   @Override
   public void createSpecificMetadataXML()
   {
      // Establish whether the ROI is represented by all the same type
      // of contour of a mixed type.
      String contourType = roiCont.contourList[0].geometricType;
      for (int i=1; i<roiCont.contourList.length; i++)
      {
         if (!contourType.equals(roiCont.contourList[i].geometricType))
         {
            contourType = "MIXED";
            break;
         }
      }
      try
      {
         int[]  c = roiCont.roiDisplayColour;
         String colString = c[0] + "/" + c[1] + "/" + c[2];
         
         if (associatedRoiSetIDs != null)
         {
            dppXML.delayedWriteEntity("associatedRoiSetIDs");
            for (String s : associatedRoiSetIDs)
            {
               dppXML.delayedWriteEntity("assocRoiSetID")
                        .delayedWriteText(s)
                     .delayedEndEntity();
            }
            dppXML.delayedEndEntity();
         }
         
         dppXML.delayedWriteEntity("subjectID")
                  .delayedWriteText(XNATSubjectID)
               .delayedEndEntity() 
                                 
               .delayedWriteEntity("dcmPatientName")
                  .delayedWriteText(rts.patientName)
               .delayedEndEntity()
                 
               .delayedWriteEntity("definingDICOMStructureSetUID")
                  .delayedWriteText(rts.structureSetUID)
               .delayedEndEntity()

               .delayedWriteEntity("definingDICOMStructureSetID")
                  .delayedWriteText(rts.roiSetID)
               .delayedEndEntity()
                 
               .delayedWriteEntity("definingDICOMStructureSetLabel")
                  .delayedWriteText(rts.roiSetLabel)
               .delayedEndEntity()

               .delayedWriteEntity("roiNumberInStructureSet")
                  .delayedWriteText((new Integer(ssRoi.roiNumber)).toString())
               .delayedEndEntity()

               .delayedWriteEntity("roiDisplayColorInStructureSet")
                  .delayedWriteText(colString)
               .delayedEndEntity()

               .delayedWriteEntity("referencedFrameOfReferenceUID")
                  .delayedWriteText(ssRoi.referencedFrameOfReferenceUID)
               .delayedEndEntity()

               .delayedWriteEntity("roiName")
                  .delayedWriteText(ssRoi.roiName)
               .delayedEndEntity()
         
               .delayedWriteEntity("roiDescription")
                  .delayedWriteText(ssRoi.roiDescription)
               .delayedEndEntity()
         
               .delayedWriteEntity("roiGenerationAlgorithm")
                  .delayedWriteText(ssRoi.roiGenerationAlgorithm)
               .delayedEndEntity()
         
               .delayedWriteEntity("roiGenerationDescription")
                  .delayedWriteText(ssRoi.roiGenerationDescription)
               .delayedEndEntity()
         
               .delayedWriteEntity("contourGeometricType")
                  .delayedWriteText(contourType)
               .delayedEndEntity()
         
               .delayedWriteEntity("nDICOMContours")
                  .delayedWriteText((new Integer(roiCont.contourList.length)).toString())
               .delayedEndEntity()
         
               .delayedWriteEntity("derivationCode")
                  .delayedWriteText(ssRoi.derivationCode)
               .delayedEndEntity();
                 
               if (roiObs.relatedROIs != null)
               {
                  dppXML.delayedWriteEntity("rtRelatedRois");
                  for (int i=0; i<roiObs.relatedROIs.length; i++)
                  {
                     dppXML.delayedWriteEntity("rtRelatedRoi")
                        .delayedWriteEntity("referencedRoiNumber")
                           .delayedWriteText((new Integer(roiObs.relatedROIs[i].referencedRoiNumber)).toString())
                        .delayedEndEntity()
                        .delayedWriteEntity("rtRoiRelationship")
                           .delayedWriteText(roiObs.relatedROIs[i].relationship)
                        .delayedEndEntity()
                     .delayedEndEntity();
                  }
               }
               
         dppXML.delayedEndEntity();
               

         dppXML.delayedWriteEntity("rtRoiInterpretedType")
                  .delayedWriteEntity(roiObs.rtRoiInterpretedType)
               .delayedEndEntity()
                 
               
               .delayedWriteEntity("roiInterpreter")
                  .delayedWriteText(roiObs.roiInterpreter)
               .delayedEndEntity()
         
               .delayedWriteEntity("roiMaterialID")
                  .delayedWriteText(roiObs.roiInterpreter)
               .delayedEndEntity()
               
               .delayedWriteEntity("roiPhysicalProperties");
         
               if (roiObs.roiPhysicalProps != null)
               {
                  for (int i=0; i<roiObs.roiPhysicalProps.length; i++)
                  {
                     dppXML.delayedWriteEntity("roiPhysicalProperty")

                        .delayedWriteEntity("propertyName")
                           .delayedWriteText(roiObs.roiPhysicalProps[i].propertyName)
                        .delayedEndEntity()

                        .delayedWriteEntity("propertyValue")
                           .delayedWriteText(roiObs.roiPhysicalProps[i].propertyValue)
                        .delayedEndEntity()

                        .delayedWriteEntity("elementalCompositionList");

                           for (int j=0; j<roiObs.roiPhysicalProps[i].elementalComp.length; j++)
                           {
                              dppXML.delayedWriteEntity("atomicNumber")
                                 .delayedWriteText((new Integer(roiObs.roiPhysicalProps[i].elementalComp[j].atomicNumber)).toString())
                              .delayedEndEntity()

                              .delayedWriteEntity("atomicMassFraction")
                                 .delayedWriteText((new Float(roiObs.roiPhysicalProps[i].elementalComp[j].atomicNumber)).toString())
                              .delayedEndEntity();
                           }
                        dppXML.delayedEndEntity()

                     .endEntity();
                  }
               }

         
      }
      catch (IOException exIO){{reportError(exIO, "write RT-STRUCT specific elements");}}
   }
   
	 
   
   @Override
   public String getRootElement()
   {
      return "ROI";
   }
   
   
   @Override
   public String getRootComplexType()
   {
      return "icr:roiData";
   }

}
