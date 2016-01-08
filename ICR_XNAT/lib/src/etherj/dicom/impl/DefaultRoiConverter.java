/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.aim.DicomImageReference;
import etherj.aim.Equipment;
import etherj.aim.GeometricShape;
import etherj.aim.Image;
import etherj.aim.ImageAnnotation;
import etherj.dicom.RoiConverter;
import etherj.aim.ImageAnnotationCollection;
import etherj.aim.ImageReference;
import etherj.aim.ImageSeries;
import etherj.aim.ImageStudy;
import etherj.aim.Markup;
import etherj.aim.Person;
import etherj.aim.TwoDimensionCircle;
import etherj.aim.TwoDimensionCoordinate;
import etherj.aim.TwoDimensionEllipse;
import etherj.aim.TwoDimensionGeometricShape;
import etherj.aim.TwoDimensionMultiPoint;
import etherj.aim.TwoDimensionPoint;
import etherj.aim.TwoDimensionPolyline;
import etherj.aim.AimUtils;
import etherj.dicom.ConversionException;
import etherj.dicom.Coordinate3D;
import etherj.dicom.DataSource;
import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.GeneralEquipmentModule;
import org.dcm4che2.iod.module.composite.GeneralSeriesModule;
import org.dcm4che2.iod.module.composite.GeneralStudyModule;
import org.dcm4che2.iod.module.composite.PatientModule;
import org.dcm4che2.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
class DefaultRoiConverter implements RoiConverter
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultRoiConverter.class);
	private final DataSource source;

	public DefaultRoiConverter(DataSource source)
	{
		this.source = source;
	}

	@Override
	public DicomObject toRtStruct(ImageAnnotationCollection iac)
		throws ConversionException
	{
		return toRtStruct(iac, 0);
	}
	
	@Override
	public DicomObject toRtStruct(ImageAnnotationCollection iac, int index)
		throws ConversionException
	{
		List<ImageAnnotation> iaList = iac.getAnnotationList();
		if (iaList.size() <= index)
		{
			throw new ConversionException("Index out of range: "+index);
		}
		DicomObject dcm = toRtStruct(iaList.get(index));
		processIac(dcm, iac);
		return dcm;
	}

	@Override
	public DicomObject toRtStruct(ImageAnnotationCollection iac, String iaUid)
		throws ConversionException
	{
		ImageAnnotation ia = iac.getAnnotation(iaUid);
		if (ia == null)
		{
			throw new ConversionException("ImageAnnotation UID not found: "+iaUid);
		}
		DicomObject dcm = toRtStruct(ia);
		processIac(dcm, iac);
		return dcm;
	}

	@Override
	public DicomObject toRtStruct(ImageAnnotation ia) throws ConversionException
	{
		List<ImageReference> imageRefs = ia.getReferenceList();
		List<DicomImageReference> dcmRefs = new ArrayList<>();
		for (ImageReference imageRef : imageRefs)
		{
			if (imageRef instanceof DicomImageReference)
			{
				dcmRefs.add((DicomImageReference) imageRef);
			}
		}
		if (dcmRefs.isEmpty())
		{
			throw new ConversionException(
				"No DicomImageReferences found in ImageAnnotation, UID: "+
					ia.getUid());
		}
		DicomImageReference dcmRef = dcmRefs.get(0);

		ImageStudy aimStudy = dcmRef.getStudy();
		DicomObject dcm = createSkeleton();
		GeneralStudyModule studyM = new GeneralStudyModule(dcm);
		studyM.setStudyInstanceUID(aimStudy.getInstanceUid());
		studyM.setStudyDateTime(AimUtils.parseDateTime(aimStudy.getStartDate()));

		processStructureSet(dcm, dcmRefs, ia);
		if (dcm.get(Tag.StructureSetROISequence).isEmpty())
		{
			throw new ConversionException(
				"No valid ROIs found in ImageAnnotation. UID: "+ia.getUid());
		}

		return dcm;
	}

	private String aimContourType(TwoDimensionGeometricShape shape)
	{
		if ((shape instanceof TwoDimensionPolyline) ||
			 (shape instanceof TwoDimensionCircle) ||
			 (shape instanceof TwoDimensionEllipse))
		{
			return CLOSED_PLANAR;
		}
		if ((shape instanceof TwoDimensionPoint) ||
			 (shape instanceof TwoDimensionMultiPoint))
		{
			return POINT;
		}
		return null;
	}

	private List<Coordinate3D> aimToDicom(List<TwoDimensionCoordinate> coords2D,
		DicomObject refDcm, int frame)
	{
		// TODO: Support multiframe
		double[] pos = refDcm.getDoubles(Tag.ImagePositionPatient);
		double[] ori = refDcm.getDoubles(Tag.ImageOrientationPatient);
		double[] row = Arrays.copyOfRange(ori, 0, 3);
		double[] col = Arrays.copyOfRange(ori, 3, 6);
		// Are AIM TwoDimesionCoordinates in pixels or mm? Assuming pixels until
		// proven otherwise
		List<Coordinate3D> coords3D = new ArrayList<>(coords2D.size());
		for (TwoDimensionCoordinate coord2D : coords2D)
		{
			coords3D.add(etherj.dicom.DicomUtils.imageCoordToPatientCoord3D(pos, row,
				col, coord2D.getX(), coord2D.getY()));
		}
		return coords3D;
	}

	private String coordsToString(List<Coordinate3D> coords3D)
	{
		StringBuilder sb = new StringBuilder();
		for (Coordinate3D coord : coords3D)
		{
			sb.append(Double.toString(coord.x)).append("\\")
				.append(Double.toString(coord.y)).append("\\")
				.append(Double.toString(coord.z)).append("\\");
		}
		// Chop off last \
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	private DicomObject createContourImageItem(DicomObject dcm)
	{
		return createContourImageItem(dcm, 0);
	}

	/*
	 *	Create an item for the ContourImageSequence in the ROI Contour Module or 
	 * ReferencedFrameOfReferenceSequence in the Structure Set Module
	 */
	private DicomObject createContourImageItem(DicomObject dcm, int frame)
	{
		DicomObject imageItem = new BasicDicomObject();
		imageItem.putString(Tag.ReferencedSOPClassUID, VR.UI,
			dcm.getString(Tag.SOPClassUID));
		imageItem.putString(Tag.ReferencedSOPInstanceUID, VR.UI,
			dcm.getString(Tag.SOPInstanceUID));
		if (frame > 0)
		{
			imageItem.putString(Tag.ReferencedFrameNumber, VR.IS,
				Integer.toString(frame));
		}
		return imageItem;
	}

	/*
	 *	Create <UID,DicomObject> map for all SOP instances referenced in
	 *	dcmRefs. Throws if any are not found.
	 */
	private Map<String, DicomObject> createDicomObjectMap(
		List<DicomImageReference> dcmRefs, ImageAnnotation ia)
		throws ConversionException
	{
		List<Series> dcmSeriesList = new ArrayList<>();
		for (DicomImageReference dcmRef : dcmRefs)
		{
			ImageSeries series = dcmRef.getStudy().getSeries();
			Series dcmSeries = source.getSeries(series.getInstanceUid());
			if (dcmSeries == null)
			{
				throw new ConversionException("Referenced series (UID: "+
					series.getInstanceUid()+") not found in ImageAnnotation. UID: "+
					ia.getUid());
			}
			dcmSeriesList.add(dcmSeries);
		}
		List<SopInstance> sopInstList = new ArrayList<>();
		for (Series dcmSeries : dcmSeriesList)
		{
			sopInstList.addAll(dcmSeries.getSopInstanceList());
		}
		if (sopInstList.isEmpty())
		{
			throw new ConversionException(
				"Referenced series contains no instances. "+
					"ImageAnnotation. UID: "+ia.getUid());
		}
		String refUid = sopInstList.get(0).getDicomObject().getString(
			Tag.FrameOfReferenceUID);
		if ((refUid == null) || refUid.isEmpty())
		{
			throw new ConversionException(
				"FrameOfReferenceUID not found in SOP instance referenced in "+
					"ImageAnnotation. UID: "+ia.getUid());
		}
		Map<String,DicomObject> sopInstDcmMap = new HashMap<>();
		for (SopInstance sopInst : sopInstList)
		{
			sopInstDcmMap.put(sopInst.getUid(), sopInst.getDicomObject());
		}
		return sopInstDcmMap;
	}

	/*
	 *	Create an item for the ROI Contour Module from Information Object
	 *	Definitions section C.8.8.6 (2015)
	 */
	private DicomObject createRoiContourItem(TwoDimensionGeometricShape shape,
		int idx, Map<String,DicomObject> dcmMap)
	{
		String contourType = aimContourType(shape);
		if (contourType == null)
		{
			logger.warn(
				"Unknown AIM TwoDimensionGeometricShape: {} in Markup. UID: {}",
				shape.getClass().getName(), shape.getUid());
			return null;
		}
		List<TwoDimensionCoordinate> coords2D = shape.getCoordinateList();
		if (coords2D.isEmpty())
		{
			logger.warn("Zero coordinates found in Markup. UID: {}",
				shape.getUid());
			return null;
		}
		if (shape instanceof TwoDimensionMultiPoint)
		{
			logger.info("TwoDimensionMultiPoint not yet supported.");
			return null;
		}
		DicomObject dcm = new BasicDicomObject();
		dcm.putString(Tag.ReferencedROINumber, VR.IS, Integer.toString(idx));
		dcm.putSequence(Tag.ContourSequence);
		DicomElement contourSq = dcm.get(Tag.ContourSequence);
		DicomObject contourItem = new BasicDicomObject();
		contourSq.addDicomObject(contourItem);
		// 2D shape has one contour by definition
		contourItem.putString(Tag.ContourNumber, VR.IS, Integer.toString(1));
		String imageRefUid = shape.getImageReferenceUid();
		DicomObject refDcm = dcmMap.get(imageRefUid);
		if (!imageRefUid.isEmpty() && (refDcm != null))
		{
			contourItem.putSequence(Tag.ContourImageSequence);
			contourItem.get(Tag.ContourImageSequence).addDicomObject(
				createContourImageItem(refDcm, shape.getReferencedFrameNumber()));
		}
		contourItem.putString(Tag.ContourGeometricType, VR.CS, contourType);
		contourItem.putString(Tag.NumberOfContourPoints, VR.IS,
			Integer.toString(coords2D.size()));
		List<Coordinate3D> coords3D = aimToDicom(coords2D, refDcm,
			shape.getReferencedFrameNumber());
		contourItem.putString(Tag.ContourData, VR.DS, coordsToString(coords3D));

		return dcm;
	}

	/*
	 *	Create an item for the RT ROI Observations Module from Information Object
	 *	Definitions section C.8.8.8 (2015)
	 */
	private DicomObject createRtRoiObservationsItem(int idx)
	{
		DicomObject dcm = new BasicDicomObject();
		dcm.putString(Tag.ObservationNumber, VR.IS, Integer.toString(idx));
		dcm.putString(Tag.ReferencedROINumber, VR.IS, Integer.toString(idx));
		return dcm;
	}

	private DicomObject createSkeleton()
	{
		DicomObject dcm = new BasicDicomObject();
		dcm.initFileMetaInformation(UID.RTStructureSetStorage,
			UIDUtils.createUID(), TransferSyntax.ExplicitVRLittleEndian.uid());
		dcm.putString(Tag.SOPClassUID, VR.UI, UID.RTStructureSetStorage);
		dcm.putString(Tag.SOPInstanceUID, VR.UI,
			dcm.getString(Tag.MediaStorageSOPInstanceUID));

		PatientModule patient = new PatientModule(dcm);
		patient.setPatientName(null);
		patient.setPatientID(null);
		patient.setPatientBirthDate(null);
		patient.setPatientSex(null);

		GeneralStudyModule study = new GeneralStudyModule(dcm);
		study.setStudyInstanceUID(null);
		study.setStudyDateTime(null);
		study.setReferringPhysiciansName(null);
		study.setStudyID(null);
		study.setAccessionNumber(null);

		GeneralSeriesModule series = new GeneralSeriesModule(dcm);
		series.setModality("RTSTRUCT");
		series.setSeriesInstanceUID(UIDUtils.createUID());
		series.setSeriesNumber(null);
		dcm.putString(Tag.OperatorsName, VR.PN, null);

		GeneralEquipmentModule equipment = new GeneralEquipmentModule(dcm);
		equipment.setManufacturer(null);

		dcm.putString(Tag.StructureSetLabel, VR.SH, "Unknown");
		Date now = new Date();
		dcm.putDate(Tag.StructureSetDate, VR.DA, now);
		dcm.putDate(Tag.StructureSetTime, VR.TM, now);

		dcm.putSequence(Tag.StructureSetROISequence);
		dcm.putSequence(Tag.ROIContourSequence);
		dcm.putSequence(Tag.RTROIObservationsSequence);

		return dcm;
	}

	private DicomObject createStructureSetRoiItem(GeometricShape shape, int idx,
		String refUid)
	{
		DicomObject dcm = new BasicDicomObject();
		dcm.putString(Tag.ROINumber, VR.IS, Integer.toString(idx));
		dcm.putString(Tag.ReferencedFrameOfReferenceUID, VR.UI, refUid);
		dcm.putString(Tag.ROIName, VR.LO, shape.getLabel());
		String value = shape.getDescription();
		if (!value.isEmpty())
		{
			dcm.putString(Tag.ROIDescription, VR.ST, value);
		}
		dcm.putString(Tag.ROIGenerationAlgorithm, VR.CS, null);
		return dcm;
	}

	private void processIac(DicomObject dcm, ImageAnnotationCollection iac)
	{
		Person person = iac.getPerson();
		PatientModule patientM = new PatientModule(dcm);
		if (person != null)
		{
			patientM.setPatientName(person.getName());
			patientM.setPatientID(person.getId());
			patientM.setPatientBirthDate(AimUtils.parseDateTime(person.getBirthDate()));
			patientM.setPatientSex(person.getSex());
		}
		Equipment aimEquipment = iac.getEquipment();
		GeneralEquipmentModule equipmentM = new GeneralEquipmentModule(dcm);
		if (aimEquipment != null)
		{
			equipmentM.setManufacturer(aimEquipment.getManufacturerName());
			String value = aimEquipment.getManufacturerModelName();
			if (!value.isEmpty())
			{
				equipmentM.setManufacturerModelName(value);
			}
			value = aimEquipment.getDeviceSerialNumber();
			if (!value.isEmpty())
			{
				equipmentM.setDeviceSerialNumber(value);
			}
			value = aimEquipment.getSoftwareVersion();
			if (!value.isEmpty())
			{
				equipmentM.setSoftwareVersions(new String[] {value});
			}
		}
	}

	/*
	 *	Process the DicomImageReferences to build the
	 * ReferencedFrameOfReferenceSequence
	 */
	private void processImageReferences(DicomObject dcm,
		List<DicomImageReference> dcmRefs, Map<String,DicomObject> sopInstDcmMap)
	{
		String firstKey = sopInstDcmMap.keySet().iterator().next();
		String refUid = sopInstDcmMap.get(firstKey).getString(
			Tag.FrameOfReferenceUID);
		dcm.putSequence(Tag.ReferencedFrameOfReferenceSequence);
		DicomElement rforSq = dcm.get(Tag.ReferencedFrameOfReferenceSequence);
		DicomObject forItem = new BasicDicomObject();
		rforSq.addDicomObject(forItem);
		forItem.putString(Tag.FrameOfReferenceUID, VR.UI, refUid);
		forItem.putSequence(Tag.RTReferencedStudySequence);
		DicomElement rtrStudySq = forItem.get(Tag.RTReferencedStudySequence);
		for (DicomImageReference dcmRef : dcmRefs)
		{
			DicomObject rtrStudyItem = new BasicDicomObject();
			rtrStudySq.addDicomObject(rtrStudyItem);
			ImageStudy study = dcmRef.getStudy();
			// Retired UID allowed see Information Object Definitions,
			// Section C.8.8.5.4 (2015)
			rtrStudyItem.putString(Tag.SOPClassUID, VR.UI,
				UID.DetachedStudyManagementSOPClassRetired);
			rtrStudyItem.putString(Tag.SOPInstanceUID, VR.UI,
				study.getInstanceUid());
			rtrStudyItem.putSequence(Tag.RTReferencedSeriesSequence);
			DicomElement rtrSeriesSq = rtrStudyItem.get(
				Tag.RTReferencedSeriesSequence);
			DicomObject rtrSeriesItem = new BasicDicomObject();
			rtrSeriesSq.addDicomObject(rtrSeriesItem);
			ImageSeries series = study.getSeries();
			rtrSeriesItem.putString(Tag.SeriesInstanceUID, VR.UI,
				series.getInstanceUid());
			rtrSeriesItem.putSequence(Tag.ContourImageSequence);
			DicomElement imageSq = rtrSeriesItem.get(Tag.ContourImageSequence);
			for (Image image : series.getImageList())
			{
				imageSq.addDicomObject(createContourImageItem(
					sopInstDcmMap.get(image.getInstanceUid())));
			}
		}
	}

	/*
	 *	Process Markup to populate StructureSetROISequence,
	 *	ROIContourSequence and RTROIObservationsSequence.
	 */
	private void processMarkup(DicomObject dcm, ImageAnnotation ia,
		Map<String,DicomObject> sopInstDcmMap)
	{
		String firstKey = sopInstDcmMap.keySet().iterator().next();
		String refUid = sopInstDcmMap.get(firstKey).getString(
			Tag.FrameOfReferenceUID);
		DicomElement ssRoiSq = dcm.get(Tag.StructureSetROISequence);
		DicomElement roiContourSq = dcm.get(Tag.ROIContourSequence);
		DicomElement rtRoiObsSq = dcm.get(Tag.RTROIObservationsSequence);
		int idx = 1;
		List<Markup> markups = ia.getMarkupList();
		for (Markup markup : markups)
		{
			if (!(markup instanceof TwoDimensionGeometricShape))
			{
				continue;
			}
			TwoDimensionGeometricShape shape = (TwoDimensionGeometricShape) markup;
			DicomObject ssRoiItem = createStructureSetRoiItem(shape, idx, refUid);
			DicomObject roiContourItem = createRoiContourItem(shape, idx,
				sopInstDcmMap);
			if ((ssRoiItem == null) || (roiContourItem == null))
			{
				continue;
			}
			DicomObject rtRoiObsItem = createRtRoiObservationsItem(idx);
			ssRoiSq.addDicomObject(ssRoiItem);
			roiContourSq.addDicomObject(roiContourItem);
			rtRoiObsSq.addDicomObject(rtRoiObsItem);
			idx++;
		}
	}

	/*
	 *	Populate the Structure Set Module from Information Object Definitions
	 *	section C.8.8.5 (2015). Throws if createDicomObjectMap throws.
	 */
	private void processStructureSet(DicomObject dcm,
		List<DicomImageReference> dcmRefs, ImageAnnotation ia)
		throws ConversionException
	{
		Map<String,DicomObject> sopInstDcmMap = createDicomObjectMap(dcmRefs, ia);
		dcm.putString(Tag.StructureSetLabel, VR.SH, ia.getName());
		String value = ia.getComment();
		if (!value.isEmpty())
		{
			dcm.putString(Tag.StructureSetDescription, VR.ST, value);
		}
		processImageReferences(dcm, dcmRefs, sopInstDcmMap);
		processMarkup(dcm, ia, sopInstDcmMap);
	}

}
