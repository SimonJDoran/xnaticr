
/****************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: Mar 10, 2009 at 1:55:07 PM
 *
 * Filename:      DICOMLoader.java
 * Package:       xnat_experiments
 *
 * This is a wrapper class to take code
 * snippets out of the main routines.
 *
 ****************************************/

package obselete;

import exceptions.DCM4CHEException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

public class DICOMLoader
{
   static Logger logger = Logger.getLogger(DICOMLoader.class);

   private boolean            successful;
   private DicomObject        bdo;

   public DICOMLoader(File file) throws DCM4CHEException
   {
      // 

      bdo         = new BasicDicomObject();
      successful  = false;

		// Temporary fix for StackOverflowError.
      // I'm not quite sure the context in which James found this error.
		String name = file.getName();
		if (name.indexOf(".ras") == name.length()-4) return;


      try
      {
         DicomInputStream  dis = new DicomInputStream(file);

         try
         {
            /* The strategy for determining whether a file is DICOM or not is in
             * two parts. Some non-DICOM files just cause DCM4CHE's readDicomObject
             * method to generate an exception (e.g., Word .doc files cause a
             * NegativeArraySize exception). Thanks go to James D'Arcy for working
             * through the list of possible exceptions that can be thrown. However,
             * there are non-DICOM files for which readDicomObject does not return
             * an error and in this case, we need to check explicitly that the
             * BasicDicomObject bdo is giving sensible values. */
            dis.readDicomObject(bdo, -1);
            
            String s = bdo.getString(Tag.SOPInstanceUID);
            successful = (s != null);
         }

         // All the errors below are thrown by DCM4CHE upon encountering different
         // types of file.
         catch (EOFException exEOF)
         {
            throw new DCM4CHEException(DCM4CHEException.EOF);
		   }

         catch (IndexOutOfBoundsException exIOOB)
         {
            throw new DCM4CHEException(DCM4CHEException.IOOB);
         }

         catch (NegativeArraySizeException exNAS)
         {
            throw new DCM4CHEException(DCM4CHEException.NAS);
         }

         catch (NumberFormatException exNAS)
         {
            throw new DCM4CHEException(DCM4CHEException.NF);
         }

         catch (UnsupportedOperationException exUO)
         {
            throw new DCM4CHEException(DCM4CHEException.UO);
         }
         
         catch (Error e)
         {
            logger.error("DCM4CHE generated a serious error.", e);
            throw e;
         }

         finally
         {
            // Since this outer of the two nested try-catch blocks detects any
            // file-not-found error, it is safe to assume that we can close the
            // input stream.
            dis.close();
   		}
      }
      catch (IOException ex)
      {
         logger.warn(file.getAbsolutePath() + " could not be opened.");
      }

   }



   public boolean success()
   {
      return successful;
   }


   public DicomObject getDicomObject()
   {
      return bdo;
   }
}
	
