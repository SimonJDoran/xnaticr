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

/*********************************************************************
* @author Simon J Doran
* Java class: XnatDependencyChecker.java
* First created on April 27, 2016 at 00:27 AM
* 
* Check whether the studies, series and SOPInstances referred to are
* by the caller are in XNAT's PostgreSQL database.
*********************************************************************/
package xnatUploader;

import exceptions.XMLException;
import exceptions.XNATException;
import java.util.LinkedHashSet;
import java.util.Map;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;

/**
 *
 * @author simond
 */
public class XnatDependencyChecker
{
   protected Map<String, AmbiguousSubjectAndExperiment> ambiguousSubjExp;
   
   
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
      XNATScanIdSet = new LinkedHashSet<>(); 
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
                  XNATScanIdSet.add(parseResult[j][0]);
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
      // that are referenced, to go in the "in" section of the assessor.
      // See the Class DICOMFileListWorker for an example of how to do this
      // both if the files are local or remote. Here, for simplicity, I don't
      // assume anything and use the REST method whether the files are local
      // or remote.       
      for (String scanId : XNATScanIdSet)
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
				{
					// Since there is a one-to-one relationship between SOPInstanceUIDs
					// and filenames, it is useful to be able to use either filename
					// or SOPInstanceUID as a key to access the other. Note: This is
					// only true because we have already specified both the project and
					// subject. In general, there is nothing to stop the same SOPInstanceUID
					// appearing in two different projects, or conceivably for two
					// different subjects within the same project - the latter being
					// possible if someone is perverse enough to upload the file twice
					// manually choosing different subject names, rather than letting
					// XNAT's automatic mechanism route the files to the correct place.
               fileSopMap.put(parseResult[j][0], parseResult[j][1]);
					sopFileMap.put(parseResult[j][1], parseResult[j][0]);
				}
            fileScanMap.put(parseResult[j][0], scanId);
         }
      }
      
      
      
 
   }

}
