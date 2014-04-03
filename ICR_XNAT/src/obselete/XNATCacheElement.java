/********************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: May 21, 2009 at 11:01:57 AM
 *
 * Filename:      XNATCacheElement.java
 * Package:       xnat_experiments
 ********************************************/


package obselete;

import generalUtilities.Vector2D;
import java.util.Vector;


/**
 * This class contains no methods and has been created as a convenient
 * place to store items in a hash table.
 */
public class XNATCacheElement
{
   public String                       DICOMStudyUID;
   public String                       XNATExperimentID;
   public String                       XNATSubjectID;
	public String                       XNATSubjectLabel;
   public Vector<String>               DICOMSeriesUIDs;
   public Vector<String>               XNATScanLabels;
   public Vector2D<String>   DICOMSOPInstanceUIDs;

  

   public XNATCacheElement(
             String                       DICOMStudyUID,
             String                       XNATExperimentID,
             String                       XNATSubjectID,
				 String                       XNATSubjectLabel,
             Vector<String>               DICOMSeriesUID,
             Vector<String>               XNATScanIDs,
             Vector2D<String>   DICOMSOPInstanceUIDs)
   {
      this.DICOMStudyUID        = DICOMStudyUID;
      this.XNATExperimentID     = XNATExperimentID;
      this.XNATSubjectID        = XNATSubjectID;
		this.XNATSubjectLabel	  = XNATSubjectLabel;
      this.DICOMSeriesUIDs	  	  = DICOMSeriesUID;
      this.XNATScanLabels       = XNATScanIDs;
      this.DICOMSOPInstanceUIDs = DICOMSOPInstanceUIDs;
   }
}


