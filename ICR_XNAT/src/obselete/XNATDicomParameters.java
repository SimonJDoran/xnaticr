/************************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: Mar 24, 2009 at 11:37:15 AM
 *
 * Filename:      XNATDicomParameters.java
 *
 * This class extracts parameters from a supplied
 * DICOM object using an XML import rule.
 ************************************************/


package obselete;

import org.dcm4che2.data.DicomObject;
import obselete.XNATDicomImportRule.XNATDicomImportElement;
import org.apache.log4j.Logger;
import java.awt.image.BufferedImage;


public class XNATDicomParameters 
{
   static  Logger                logger = Logger.getLogger(XNATDicomParameters.class);
   private XNATDicomImportRule   xndr;
   private DicomObject           dcm;

   public XNATDicomParameters(XNATDicomImportRule xndr, DicomObject dcm)
   {
      this.xndr   = xndr;
      this.dcm    = dcm;
   }


   /* Extract the required XNAT parameter from the DICOM object. This call
    * returns null if the tag was not present in the XNAT DICOM import rule.
    * Otherwise, it returns the value of the parameter from the DICOM header
    * or an appropriate default if the DICOM data do not contain anything.
    */
   public String getParameter(String tag)
   {
      XNATDicomImportElement ie;
      try
      {
         ie = xndr.get(tag);
      }
      catch (NullPointerException exNP )
      {
         return null;
      }

		/* This happens only if getParameter was called with a tag that is not
		 * present in the XNAT_DICOM_import_rule.xml.
		 */
		if (ie == null)
      {
         logger.warn("XNATDicomParameters.getParameter called with unknown parameter "
                      + tag);
         return null;
      }

		/* The import element contains a list of DICOM fields from which to try and
       * extract the required information, and a default value to use if none of
       * the fields yield anything.
       */
      String result = null;
      int[] DICOMTags = ie.DICOMTags();
      for (int i=0; i<DICOMTags.length; i++)
      {
         // This is temporary. I'm not sure what happens when the element being
         // extracted is not a string itself. Need to revisit, for example, dates
         // and possible confusion with months/days when converting dates to string.
         String[] s = dcm.getStrings(DICOMTags[i]);
         if (s !=null)
         {
            for (int j=0; j<s.length; j++)
            {
               if ((s[j] == null) || (s[j].matches("\\s+"))) break;
               if (j == 0) result = s[j];
               else        result = result + "\\" + s[j];
            }
            if (result != null) break;
         }
      }

      return (result == null) ? ie.getDefault() : result;
   }



   public float[] getVoxelSize()
   {
      float[] voxelSize   = {-1, -1, -1};
      
      try
      {
         String s            = getParameter("Pixel_Spacing");
         int    delimiterPos = s.indexOf("\\");
         Float  xSize        = new Float(s.substring(0, delimiterPos-1));
         Float  ySize        = new Float(s.substring(delimiterPos+1));

         s = getParameter("Slice_Thickness");
         Float  zSize        = new Float(s);

         voxelSize[0] = xSize;
         voxelSize[1] = ySize;
         voxelSize[2] = zSize;
      }
      catch (Exception ex)
      {
         logger.warn("XNATDicomParameters.getVoxelSize failed with exception "
                     + ex.getMessage());
      }
      return voxelSize;
   }




   public float[] getFOV()
   {
      float[] FOV = {-1, -1, -1};
      Float FOVz;
      
      try
      {
         String slab         = getParameter("Slab_Thickness");
         String s            = getParameter("FOV");
         int    delimiterPos = s.indexOf("\\");
         if (delimiterPos != -1)
         {
            FOV[0] = new Float(s.substring(0, delimiterPos-1));
            FOV[1] = new Float(s.substring(delimiterPos+1));

            if (slab != null && !slab.startsWith("Unknown"))
               FOV[2] = new Float(slab);
         }
         else
         {
            float[] vs    = getVoxelSize();
            int[]   ms    = getMatrixSize();

            if (vs[0]>0 || ms[0]>0) FOV[0] = vs[0]*ms[0];
            if (vs[1]>0 || ms[1]>0) FOV[1] = vs[1]*ms[1];
            if (vs[2]>0 || ms[2]>0) FOV[2] = vs[2]*ms[2];
         }

      }
      catch (Exception ex)
      {
         logger.warn("XNATDicomParameters.getFOV failed with exception "
                     + ex.getMessage());
      }

      return FOV;
   }



   public int[] getMatrixSize()
   {
      int[]  matrixSize = {-1, -1, -1};
      String s;

      try
      {
         s = getParameter("Number_of_Columns");
         matrixSize[0] = new Integer(s);

         s = getParameter("Number_of_Rows");
         matrixSize[1] = new Integer(s);

         s = getParameter("Number_of_Planes");
         if ((s != null) && (!s.startsWith("Undefined")))
                 matrixSize[2] = new Integer(s);
      }
      catch (Exception ex)
      {
         logger.warn("XNATDicomParameters.getMatrixSize failed with exception "
                     + ex.getMessage());
      }

      return matrixSize;
   }



   public String getDiffusionDirectionality()
   {
      return new String("Not implemented yet");
   }


   public String getDiffusionDirection()
   {
      return new String("Not implemented yet");
   }

   
   public String getBValue()
   {
      return new String("Not implemented yet");
   }


   public String getXNATOrientation()
   {
      return new String("Not implemented yet");
   }


   public BufferedImage getThumbnail(int x, int y)
   {
       return new BufferedImage(x, y, BufferedImage.TYPE_USHORT_GRAY);
   }
}
