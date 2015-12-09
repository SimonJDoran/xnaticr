/****************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: Jul 14, 2009 at 10:35:26 AM
 *
 * Filename:      XNATDicomLoaderWorker.java
 * Package:       xnat_experiments
 ****************************************/


package obselete;

import obselete.XNATCacheElement;
import exceptions.DCM4CHEException;
import exceptions.XMLException;
import exceptions.XNATException;
import generalUtilities.Vector2D;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.w3c.dom.Document;
import obselete.XNATDicomLoaderWorker.WorkerOutput;
import xnatRestToolkit.XNATRESTToolkit;


public class XNATDicomLoaderWorker extends SwingWorker<WorkerOutput, WorkerOutput>
{
   public class WorkerOutput
   {
      private Integer nUpload;
      private Integer nWarn;
      private Integer nFail;
      private String  currentFile;

      public WorkerOutput(int nUpload, int nWarn, int nFail, String currentFile)
      {
         this.nUpload     = nUpload;
         this.nWarn       = nWarn;
         this.nFail       = nFail;
         this.currentFile = currentFile;
      }
   }

   static  Logger     logger = Logger.getLogger(XNATDicomLoaderWorker.class);
   private File[]     chosenFiles;
   private JButton    uploadButton;
   private JComboBox  projectComboBox;
   private JTextField usernameTextField;
   private JTextField passwordTextField;
   private JLabel     uploadedLabel;
   private JLabel     warningsLabel;
   private JLabel     failuresLabel;
   private JLabel     currentFileLabel;
   private Integer    nUploaded;
   private Integer    nWarnings;
   private Integer    nFailures;
   private String     currentFilename;
   private String     XNATProjectName;
   private XNATDicomImportRule xndr;
   private XNATRESTToolkit xnrt;
   private Hashtable<String, XNATCacheElement> catalogCache;
   private Throwable  theException = null;

   private XNATDicomLoaderWorker() {}  // Default constructor not used

   public XNATDicomLoaderWorker(File[]     chosenFiles,
                                JButton    uploadButton,
                                JComboBox  projectComboBox,
                                JTextField usernameTextField,
                                JTextField passwordTextField,
                                JLabel     uploadedLabel,
                                JLabel     warningsLabel,
                                JLabel     failuresLabel,
                                JLabel     currentFileLabel,
                                String     XNATProjectName,
                                XNATDicomImportRule xndr,
                                XNATRESTToolkit xnrt,
                                Hashtable<String, XNATCacheElement> catalogCache)
   {
      this.chosenFiles       = chosenFiles;
      this.uploadButton      = uploadButton;
      this.projectComboBox   = projectComboBox;
      this.usernameTextField = usernameTextField;
      this.passwordTextField = passwordTextField;
      this.uploadedLabel     = uploadedLabel;
      this.warningsLabel     = warningsLabel;
      this.failuresLabel     = failuresLabel;
      this.currentFileLabel  = currentFileLabel;
      this.nUploaded         = new Integer(uploadedLabel.getText());
      this.nWarnings         = new Integer(warningsLabel.getText());
      this.nFailures         = new Integer(failuresLabel.getText());
      this.XNATProjectName   = XNATProjectName;
      this.xndr              = xndr;
      this.xnrt              = xnrt;
      this.catalogCache      = catalogCache;
   }



   @Override
   protected WorkerOutput doInBackground() throws Exception
   {
      for (int i=0; i<chosenFiles.length; i++)
      {
         if (isCancelled()) break;
         try
         {
            traverseHierarchy(chosenFiles[i]);
         }
         catch (Exception ex)
         {
            theException = ex;
         }
      }

      return new WorkerOutput( nUploaded, nWarnings, nFailures, currentFilename);
   }





   @Override
   protected void done()
   {
      // Executes on the Event Dispatch Thread, hence references to GUI.
      uploadedLabel.setText(nUploaded.toString());
      warningsLabel.setText(nWarnings.toString());
      failuresLabel.setText(nFailures.toString());
      currentFileLabel.setText("<None>");
      uploadButton.setEnabled(true);
      projectComboBox.setEnabled(true);
      usernameTextField.setEnabled(true);
      passwordTextField.setEnabled(true);

      if (theException != null) logger.error("Error on XNATDicomLoaderWorker thread:", theException);
   }



   @Override
   protected void process(List<WorkerOutput> publishedList)
   {
      /* The argument is a list only because calls to publish might have been
       * batched up. In fact, it is only relevant to display the last one.
       */
      WorkerOutput lastOutput = publishedList.get(publishedList.size()-1);

      /* Display the latest statistics in the output fields of the GUI.
       * Note that the process method is called from the Event Dispatch Thread,
       * so it is OK to interact with the GUI here.
       */
      uploadedLabel.setText(nUploaded.toString());
      warningsLabel.setText(nWarnings.toString());
      failuresLabel.setText(nFailures.toString());
      currentFileLabel.setText(currentFilename);
   }



   private void traverseHierarchy(File fileDir) throws Exception
   {
      // Traverse the file tree.
      if (isCancelled()) return;
      if (fileDir.isFile())
      {
         currentFilename = fileDir.getName();
         publish(new WorkerOutput(nUploaded, nWarnings, nFailures, currentFilename ));
         processFile(fileDir);
      }
      else
      {
         String[] children = fileDir.list();
         for (int i=0; i<children.length; i++)
            try
            {
               traverseHierarchy(new File(fileDir, children[i]));
            }
            catch (Exception ex)
            {
               // Pass this back down the chain, which will ultimately end at
               // doInBackground.
               throw ex;
            }
      }
   }



   private void processFile(File file)
   {
      DICOMLoader          dl;

      try
      {
         dl = new DICOMLoader(file);
      }
      catch (DCM4CHEException exDCM)
      {
         // Do nothing with the error, since the logger has previously registered it.
         return;
      }

		if (!dl.success())
		{
			logger.warn("File " + file + " is not recognised as DICOM.");
			++nWarnings;
			return;
		}

      // Get the details required for reading the scan into XNAT.
      DicomObject          dcm  = dl.getDicomObject();
      XNATDicomParameters  xndp = new XNATDicomParameters(xndr, dcm);

      try
      {
         if (fileIsAlreadyImported(xndp))
			{
            logger.warn("File " + file + " is already present in the XNAT database.");
				++nWarnings;
			}

         else
			{
//				xnrt.uploadDicomFile(XNATProjectName, xndp, catalogCache, file);
				++nUploaded;
			}
      }

      catch (XNATException exXNAT)
      {
         if (exXNAT.getReturnCode() == XNATException.QUERY_LOADED)
            logger.warn("Failed to determine whether file" + file.getAbsolutePath()
                        + "is already imported: ");

         else
            logger.error("Failed to upload DICOM file " + file.getAbsolutePath()
                 + ": " + exXNAT.getMessage());

			++nFailures;
      }
   }




   private boolean fileIsAlreadyImported(XNATDicomParameters xndp)
           throws XNATException
   {
      Vector2D<String> searchResult     = null;
		XNATCacheElement           ce;
		String                     DICOMStudyUID    = xndp.getParameter("StudyUID");
		String                     DICOMSeriesUID   = xndp.getParameter("SeriesUID");
      String                     DICOMInstanceUID = xndp.getParameter("SOPInstanceUID");
	   String                     XNATExperimentID;
		String                     XNATSubjectID;
		String                     XNATSubjectLabel;
		String                     experimentURI;

		/* To minimise the number of calls to the REST API seeking this information
       * (i.e., so we don't have to do it again when the next DICOM file from the
       * same study turns up), use a cache for the results. Note the equivalence
       * between DICOM and XNAT nomenclature:
       *
       * DICOM Study        = XNAT Experiment/Session
       * DICOM Series       = XNAT Scan
       * DICOM SOP Instance = XNAT File
       */
      /* First check the cache to see whether the study that this DICOM file
		 * belongs to has already been imported. If this study hasn't yet been,
		 * added to the cache, ask XNAT directly whether it has been imported.
		 */
      ce = catalogCache.get(DICOMStudyUID);
      if (ce == null)
      {
         try
			{
				searchResult = xnrt.RESTGetResultSet("/REST/experiments?studyInstanceUID="
																	+ DICOMStudyUID + "&format=xml");
			}
         catch (Exception ex)
			{
				throw new XNATException(XNATException.QUERY_LOADED, ex.getMessage());
			}

         if (searchResult.isEmpty()) return false;

			/* N.B. There is a question still outstanding of what to do if the
			 * length of searchResult is not 1. This would mean that the given
			 * DICOM study is present in two projects. From a space point of view
			 * it is important that the same file is not uploaded multiple times,
			 * but if there are different projects, then I need to manage the
			 * sharing between them.
			 */
         XNATExperimentID = searchResult.atom(0,0);
         XNATSubjectID    = searchResult.atom(2,0);

         /* For some reason the output of XNAT appears to be inconsistent. Most of
          * the time the URI is in element 7, but sometimes it is in 6.
          */
         if (searchResult.size() == 9) experimentURI = searchResult.atom(8,0);
         else experimentURI = searchResult.atom(6, 0);
         

         /* Unfortunately, the REST call above does not return the XNAT
          * subject label. We need this, because the label is the only way
          * to decide (in XNATRESTToolkit.createSubjectIfNecessary) whether
          * the subject already exists or whether we need to create a new one.
          * Try to match the subject ID found above with an existing label
          */

			try
		   {
  				searchResult = xnrt.RESTGetResultSet("/REST/projects/" + XNATProjectName
                                               + "/subjects?format=xml");
         }
         catch (Exception ex)
         {
            throw new XNATException(XNATException.GET);
         }

         int index = searchResult.indexOfForCol(1, XNATSubjectID);
         if (index == -1) throw new XNATException(XNATException.SUBJ_LIST,
                  "Subject labels and ID's do not match"); // This shouldn't happen!

         XNATSubjectLabel = searchResult.atom(0, index);

//         try
//         {
//            Document doc = xnrt.RESTGetDoc(experimentURI+"?format=xml");
//				ce = xnrt.parseExperimentXML(doc, DICOMStudyUID, XNATExperimentID,
//						                       XNATSubjectID, XNATSubjectLabel);
//				catalogCache.put(DICOMStudyUID, ce);
//	      }
//			catch (XNATException exXNAT)
//			{
//            throw exXNAT;
//			}
//			catch (XMLException exXML)
//			{
//				throw new XNATException(XNATException.CATALOG);
//			}
      }


		/* When we get to here, we know that the DICOM file of at least one member
		 * of this study has already been imported. We now need to check whether
		 * the SOP Instance corresponding to dcm has already been imported.
		 */
		int nScans = ce.DICOMSeriesUIDs.size();
      for (int i=0; i<nScans; i++)
		{
			if (ce.DICOMSeriesUIDs.elementAt(i).equals(DICOMSeriesUID))
				if (ce.DICOMSOPInstanceUIDs.elementAt(i).contains(DICOMInstanceUID))
					return true;
		}

      return false;



/*    Reminder of syntax for a search! Not used at the moment.
      XNATReturnedField[] returnedFields = new XNATReturnedField[2];
      returnedFields[0] = new XNATReturnedField("xnat:mrScanData",
                                                "ID",
                                                "0",
                                                "string");
      returnedFields[1] = new XNATReturnedField("xnat:mrSessionData",
                                                "SESSION_ID",
                                                "1",
                                                "string");

      XNATSearchCriterion[] searchCriteria = new XNATSearchCriterion[1];
      searchCriteria[0] = new XNATSearchCriterion("xnat:mrSessionData@UID",
                                                  "=",
                                                  studyUID);
      try
      {
         searchResult = xnrt.search("xnat:mrSessionData",
                                    returnedFields,
                                    "AND",
                                    searchCriteria);
      }
      catch (Exception ex)
      {
         throw new XNATException(XNATException.SEARCH, ex.getMessage());
      }
 */

   }
}
