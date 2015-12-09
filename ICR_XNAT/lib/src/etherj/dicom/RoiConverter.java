/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.aim.ImageAnnotation;
import etherj.aim.ImageAnnotationCollection;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author jamesd
 */
public interface RoiConverter
{
	public static final String CLOSED_PLANAR = "CLOSED_PLANAR";
	public static final String OPEN_NONPLANAR = "OPEN_NONPLANAR";
	public static final String OPEN_PLANAR = "OPEN_PLANAR";
	public static final String POINT = "POINT";

	/**
	 *
	 * @param ia
	 * @return
	 * @throws ConversionException
	 */
	DicomObject toRtStruct(ImageAnnotation ia) throws ConversionException;

	/**
	 *
	 * @param iac
	 * @return
	 * @throws etherj.dicom.ConversionException
	 */
	DicomObject toRtStruct(ImageAnnotationCollection iac)
		throws ConversionException;

	/**
	 *
	 * @param iac
	 * @param index
	 * @return
	 * @throws etherj.dicom.ConversionException
	 */
	DicomObject toRtStruct(ImageAnnotationCollection iac, int index)
		throws ConversionException;

	/**
	 *
	 * @param iac
	 * @param uid
	 * @return
	 * @throws ConversionException
	 */
	DicomObject toRtStruct(ImageAnnotationCollection iac, String uid)
		throws ConversionException;

}
