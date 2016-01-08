/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.Ether;
import etherj.PathScan;
import etherj.db.DatabaseException;
import etherj.dicom.DataSource;
import etherj.dicom.DicomDatabase;
import etherj.dicom.DicomToolkit.DicomFactory;
import etherj.dicom.Patient;
import etherj.dicom.PatientRoot;
import etherj.dicom.RtContour;
import etherj.dicom.RtRoi;
import etherj.dicom.RtStruct;
import etherj.dicom.RoiConverter;
import etherj.dicom.SearchCriterion;
import etherj.dicom.SearchSpecification;
import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import etherj.dicom.Study;
import java.io.File;
import java.util.List;
import java.util.Properties;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class DefaultDicomFactory implements DicomFactory
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultDicomFactory.class);

	@Override
	public DataSource createDataSource(String key, Properties props)
	{
		DataSource source = null;
		DicomDatabase db;
		try
		{
			switch (key)
			{
				case DataSource.DicomDatabase:
					db = createDicomDatabase(props);
					source = new DatabaseDataSource(db);
					break;

				default:
					logger.warn("Unknown DataSource key: "+key);
					db = createDicomDatabase(props);
					source = new DatabaseDataSource(db);
			}
		}
		catch (DatabaseException ex)
		{
			logger.warn(ex.getMessage());
		}
		return source;
	}

	@Override
	public DicomDatabase createDicomDatabase() throws DatabaseException
	{
		return new SqliteDicomDatabase(Ether.getEtherDir()+"dicom.db");
	}

	@Override
	public DicomDatabase createDicomDatabase(Properties props) throws DatabaseException
	{
		String path = props.getProperty("db.filename", "dicom.db");
		return new SqliteDicomDatabase(path);
	}

	@Override
	public PathScan<DicomObject> createPathScan()
	{
		return new DefaultPathScan();
	}

	@Override
	public Patient createPatient(SopInstance sopInst)
	{
		DicomObject dcm = sopInst.getDicomObject();
		String name = dcm.getString(Tag.PatientName);
		name = (name == null) ? "" : name.replace(' ', '_');
		String birthDate = dcm.getString(Tag.PatientBirthDate);
		String id = dcm.getString(Tag.PatientID);
		Patient patient = new DefaultPatient(name, birthDate, id);
		patient.setOtherId(dcm.getString(Tag.OtherPatientIDs));
		String comments = dcm.getString(Tag.PatientComments);
		patient.setComments((comments == null) ? "" : comments);

		return patient;
	}

	@Override
	public Patient createPatient(String name, String birthDate, String id)
	{
		return new DefaultPatient(name, birthDate, id);
	}

	@Override
	public PatientRoot createPatientRoot()
	{
		return new DefaultPatientRoot();
	}

	@Override
	public RoiConverter createRoiConverter(DataSource source)
	{
		return new DefaultRoiConverter(source);
	}

	@Override
	public RtContour createRtContour(DicomObject dcm)
	{
		return new DefaultRTContour(dcm);
	}

	@Override
	public RtRoi createRtRoi(DicomObject roiItem, DicomObject contourItem)
	{
		return new DefaultRTRoi(roiItem, contourItem);
	}

	@Override
	public RtStruct createRtStruct(DicomObject dcm)
	{
		return new DefaultRTStruct(dcm);
	}

	@Override
	public SearchCriterion createSearchCriterion(int tag, int comparator,
		String value)
	{
		return new SimpleSearchCriterion(tag, comparator, value);
	}

	@Override
	public SearchCriterion createSearchCriterion(int tag, int comparator,
		String value, int combinator)
	{
		return new SimpleSearchCriterion(tag, comparator, value, combinator);
	}

	@Override
	public SearchCriterion createSearchCriterion(SearchCriterion a,
		SearchCriterion b)
	{
		return new CompoundSearchCriterion(a, b);
	}

	@Override
	public SearchCriterion createSearchCriterion(SearchCriterion a,
		SearchCriterion b, int combinator)
	{
		return new CompoundSearchCriterion(a, b, combinator);
	}

	@Override
	public SearchCriterion createSearchCriterion(List<SearchCriterion> criteria)
	{
		return new CompoundSearchCriterion(criteria);
	}

	@Override
	public SearchCriterion createSearchCriterion(List<SearchCriterion> criteria,
		int combinator)
	{
		return new CompoundSearchCriterion(criteria, combinator);
	}

	@Override
	public SearchSpecification createSearchSpecification()
	{
		return new SearchSpecification();
	}

	@Override
	public Series createSeries(SopInstance sopInst)
	{
		return new DefaultSeries(sopInst);
	}

	@Override
	public Series createSeries(String uid)
	{
		return new DefaultSeries(uid);
	}

	@Override
	public SopInstance createSopInstance(File file)
	{
		return new DefaultSopInstance(file);
	}

	@Override
	public SopInstance createSopInstance(File file, DicomObject dcm)
	{
		return new DefaultSopInstance(file, dcm);
	}

	@Override
	public Study createStudy(SopInstance sopInst)
	{
		return new DefaultStudy(sopInst);
	}

	@Override
	public Study createStudy(String uid)
	{
		return new DefaultStudy(uid);
	}

}
