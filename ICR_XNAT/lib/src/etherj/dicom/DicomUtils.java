/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.IoUtils;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomCodingException;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class DicomUtils
{
	private static final Logger logger = LoggerFactory.getLogger(DicomUtils.class);
	private static final Set<String> imageSopClasses = new HashSet<>();

	static
	{
		imageSopClasses.add(UID.EnhancedMRImageStorage);
		imageSopClasses.add(UID.MRImageStorage);
		imageSopClasses.add(UID.MRSpectroscopyStorage);
		imageSopClasses.add(UID.PositronEmissionTomographyImageStorage);
		imageSopClasses.add(UID.CTImageStorage);
		imageSopClasses.add(UID.EnhancedCTImageStorage);
		imageSopClasses.add(UID.UltrasoundImageStorage);
		imageSopClasses.add(UID.DigitalMammographyXRayImageStorageForProcessing);
		imageSopClasses.add(UID.DigitalMammographyXRayImageStorageForPresentation);
		imageSopClasses.add(UID.DigitalXRayImageStorageForProcessing);
		imageSopClasses.add(UID.DigitalXRayImageStorageForPresentation);
		imageSopClasses.add(UID.XRayRadiofluoroscopicImageStorage);
		imageSopClasses.add(UID.ComputedRadiographyImageStorage);
		imageSopClasses.add(UID.SecondaryCaptureImageStorage);
		imageSopClasses.add(UID.NuclearMedicineImageStorage);
	}

	public static int dateToInt(String date)
	{
		if (date.length() != 8)
		{
			throw new NumberFormatException("DA must be 8 characters");
		}
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(4, 6));
		int day = Integer.parseInt(date.substring(6, 8));
	
		return year*10000+month*100+day;
	}

	/**
	 *
	 * @param sopClassUid
	 * @return
	 */
	public static boolean isImageSopClass(String sopClassUid)
	{
		return imageSopClasses.contains(sopClassUid);
	}

	/**
	 *
	 * @param patient
	 * @return
	 */
	public static String makePatientKey(Patient patient)
	{
		return patient.getName()+"_"+patient.getBirthDate()+"_"+patient.getId();
	}

	/**
	 *
	 * @param sopInst
	 * @return
	 */
	public static String makePatientKey(SopInstance sopInst)
	{
		DicomObject dcm = sopInst.getDicomObject();
		String patName = dcm.getString(Tag.PatientName);
		patName = (patName == null) ? "" : patName.replace(' ', '_');
		String birthDate = dcm.getString(Tag.PatientBirthDate);
		if ((birthDate == null) || birthDate.isEmpty())
		{
			birthDate = "00000000";
		}
		String patId = dcm.getString(Tag.PatientID);
		if (patId == null)
		{
			patId = "";
		}
		return patName+"_"+birthDate+"_"+patId;
	}	
	
	/**
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DicomObject readDicomFile(File file) throws IOException
	{
		DicomObject dcm = new BasicDicomObject();
		DicomInputStream dcmIS = null;
		try
		{
			dcmIS = new DicomInputStream(file);
			dcmIS.readDicomObject(dcm, -1);
		}
		catch (DicomCodingException exDC)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("DicomCodingException reading non-DICOM file: {}",
				file.getPath());
			dcm = null;
		}
		catch (EOFException exEOF)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("EOFException reading non-DICOM file: {}", file.getPath());
			dcm = null;
		}
		catch (IndexOutOfBoundsException exIOOB)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("IndexOutOfBoundsException reading non-DICOM file: {}",
				file.getPath());
			dcm = null;
		}
		catch (NegativeArraySizeException exNAS)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("NegativeArraySizeException reading non-DICOM file: {}",
				file.getPath());
			dcm = null;
		}
		catch (NumberFormatException exNF)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("NumberFormatException reading non-DICOM file: {}",
				file.getPath());
			dcm = null;
		}
		catch (UnsupportedOperationException exUO)
		{
			// Dcm4Che throws this on scanning some non-DICOM files
			logger.debug("UnsupportedOperationException reading non-DICOM file: {}",
				file.getPath());
			dcm = null;
		}
		catch (OutOfMemoryError erOOM)
		{
			dcm = null;
			// Dcm4Che can throw this on scanning some non-DICOM files by
			// trying to allocate a stupidly large byte array. Try and recover.
			// Generally not an advisable thing to catch :(
			logger.error("OutOfMemoryError reading file: {}", file.getPath());
		}
		catch (Error er)
		{
			dcm = null;
			// Shouldn't happen, log in case it's swallowed
			logger.error("Error reading file: "+file.getPath(), er);
			throw er;
		}
		finally
		{
			IoUtils.safeClose(dcmIS);
		}
		if ((dcm != null) && dcm.isEmpty())
		{
			dcm = null;
			logger.debug("Zero DicomElements found: {}", file.getPath());
		}

		return dcm;
	}

	/**
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static DicomObject readDicomObject(InputStream is) throws IOException
	{
		return readDicomObject(is, null);
	}

	/**
	 *
	 * @param is
	 * @param xferSyntax
	 * @return
	 * @throws IOException
	 */
	public static DicomObject readDicomObject(InputStream is, String xferSyntax)
		throws IOException
	{
		DicomObject dcm = new BasicDicomObject();
		DicomInputStream dcmIS = null;
		try
		{
			dcmIS = (xferSyntax != null)
				? new DicomInputStream(is, xferSyntax)
				: new DicomInputStream(is);
			dcmIS.readDicomObject(dcm, -1);
		}
		catch (DicomCodingException exDC)
		{
			// Dcm4Che throws this on scanning some non-DICOM streams
			logger.debug("DicomCodingException in stream", exDC);
			dcm = null;
		}
		catch (EOFException | NegativeArraySizeException | NumberFormatException |
				 UnsupportedOperationException ex)
		{
			// Dcm4Che throws this on scanning some non-DICOM streams
			logger.debug("Non-DICOM object", ex);
			dcm = null;
		}
		catch (IndexOutOfBoundsException exIOOB)
		{
			// Dcm4Che throws this on scanning some non-DICOM streams
			logger.debug("Non-DICOM file object", exIOOB);
			dcm = null;
		}
		catch (OutOfMemoryError erOOM)
		{
			dcm = null;
			// Dcm4Che can throw this on scanning some non-DICOM streams by
			// trying to allocate a stupidly large byte array. Try and recover.
			// Generally not an advisable thing to catch :(
			logger.error("OutOfMemoryError reading object", erOOM);
		}
		catch (Error er)
		{
			dcm = null;
			// Shouldn't happen, log in case it's swallowed
			logger.error("Error reading object", er);
			throw er;
		}
		finally
		{
			IoUtils.safeClose(dcmIS);
		}

		return dcm;
	}

	public static String secondsToTm(double seconds)
	{
		if (seconds > 86400.0)
		{
			throw new IllegalArgumentException("Value in excess of 24h");
		}
		double tm = seconds;
		StringBuilder sb = new StringBuilder();
		int hh = (int) Math.floor(tm/3600.0);
		tm -= 3600.0*hh;
		int mm = (int) Math.floor(tm/60.0);
		tm -= 60*mm;
		int ss = (int) Math.floor(tm);
		tm -= ss;
		sb.append(String.format("%02d", hh)).append(String.format("%02d", mm))
			.append(String.format("%02d", ss));
		if (tm > 0)
		{
			String sec = String.format("%f", tm);
			sb.append(sec.substring(1, sec.length()));
		}
		return sb.toString();
	}

	public static String tagName(int tag)
	{
		for (Field field : Tag.class.getDeclaredFields())
		{
			try
			{
				if (field.getInt(null) == tag)
				{
					return field.getName();
				}
			}
			catch (IllegalArgumentException | IllegalAccessException ignore)
			{}
		}
		return null;
	}
	/**
	 *
	 * @param tm
	 * @return
	 */
	public static double tmToSeconds(String tm)
	{
		if (tm == null)
		{
			throw new NumberFormatException("TM invalid (null)");
		}
		int nTM = tm.length();
		if (nTM < 2)
		{
			throw new NumberFormatException("TM invalid: "+tm);
		}
		double hh = 3600*Double.parseDouble(tm.substring(0,2));
		double mm = 0;
		double ss = 0;
		if (nTM >= 4)
		{
			mm = 60*Double.parseDouble(tm.substring(2,4));
			if (nTM > 4)
			{
				ss = Double.parseDouble(tm.substring(4,nTM));
			}
		}
		else
		{
			throw new NumberFormatException("TM invalid: "+tm);
		}
		return hh+mm+ss;
	}

	/**
	 *
	 * @param dcm
	 * @param file
	 * @throws IOException
	 */
	public static void writeDicomFile(DicomObject dcm, File file)
		throws IOException
	{
		DicomOutputStream dcmOS = null;
		try
		{
			dcmOS = new DicomOutputStream(file);
			dcmOS.writeDicomFile(dcm);
		}
		finally
		{
			IoUtils.safeClose(dcmOS);
		}
	}

	public static Coordinate3D imageCoordToPatientCoord3D(double[] pos,
		double[] row, double[] col, double x, double y)
	{
		if ((pos.length != 3) || (row.length != 3) || (col.length != 3))
		{
			throw new IllegalArgumentException(
				"Position and cosines must be double[3]");
		}
		double x3D = pos[0]+x*row[0]+y*col[0];
		double y3D = pos[1]+x*row[1]+y*col[1];
		double z3D = pos[2]+x*row[2]+y*col[2];

		return new Coordinate3D(x3D, y3D, z3D);
	}

	/*
	 *	Private constructor to prevent direct instantiation
	 */
	private DicomUtils()
	{}
}
