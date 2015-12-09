/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.PathScan;
import etherj.db.DatabaseException;
import etherj.dicom.impl.DefaultDicomFactory;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.dcm4che2.data.DicomObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class DicomToolkit
{
	private static final String Default = "default";
	private static final Logger logger = LoggerFactory.getLogger(DicomToolkit.class);
	private static final Map<String,DicomToolkit> toolkitMap = new HashMap<>();
	private final DicomFactory dicomFactory = new DefaultDicomFactory();

	static
	{
		toolkitMap.put(Default, new DicomToolkit());
	}

	/**
	 *
	 * @return
	 */
	public static DicomToolkit getDefaultToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @return
	 */
	public static DicomToolkit getToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public static DicomToolkit getToolkit(String key)
	{
		return toolkitMap.get(key);
	}

	/**
	 *
	 * @param key
	 * @param toolkit
	 * @return
	 */
	public static DicomToolkit setToolkit(String key, DicomToolkit toolkit)
	{
		DicomToolkit tk = toolkitMap.put(key, toolkit);
		logger.info(toolkit.getClass().getName()+" set with key '"+key+"'");
		return tk;
	}

	public DataSource createDataSource()
	{
		return dicomFactory.createDataSource(DataSource.DicomDatabase,
			new Properties());
	}

	public DataSource createDataSource(String key, Properties props)
	{
		return dicomFactory.createDataSource(key, props);
	}

	/**
	 *
	 * @return
	 * @throws DatabaseException
	 */
	public DicomDatabase createDicomDatabase() throws DatabaseException
	{
		return dicomFactory.createDicomDatabase();
	}

	/**
	 *
	 * @param properties
	 * @return
	 * @throws etherj.db.DatabaseException
	 */
	public DicomDatabase createDicomDatabase(Properties properties)
		throws DatabaseException
	{
		return dicomFactory.createDicomDatabase(properties);		
	}

	/**
	 *
	 * @return
	 */
	public PathScan<DicomObject> createPathScan()
	{
		return dicomFactory.createPathScan();
	}

	/**
	 *
	 * @param sopInst
	 * @return
	 */
	public Patient createPatient(SopInstance sopInst)
	{
		return dicomFactory.createPatient(sopInst);
	}

	public Patient createPatient(String name, String birthDate, String id)
	{
		return dicomFactory.createPatient(name, birthDate, id);
	}
	/**
	 *
	 * @return
	 */
	public PatientRoot createPatientRoot()
	{
		return dicomFactory.createPatientRoot();
	}

	/**
	 *
	 * @param source
	 * @return
	 */
	public RoiConverter createRoiConverter(DataSource source)
	{
		return dicomFactory.createRoiConverter(source);
	}

	/**
	 *
	 * @param dcm
	 * @return
	 */
	public RtContour createRtContour(DicomObject dcm)
	{
		return dicomFactory.createRtContour(dcm);
	}

	/**
	 *
	 * @param roiItem
	 * @param contourItem
	 * @return
	 */
	public RtRoi createRtRoi(DicomObject roiItem, DicomObject contourItem)
	{
		return dicomFactory.createRtRoi(roiItem, contourItem);
	}

	/**
	 *
	 * @param dcm
	 * @return
	 */
	public RtStruct createRtStruct(DicomObject dcm)
	{
		return dicomFactory.createRtStruct(dcm);
	}

	/**
	 *
	 * @param tag
	 * @param value
	 * @param comparator
	 * @return
	 */
	public SearchCriterion createSearchCriterion(int tag, int comparator, 
		String value)
	{
		return dicomFactory.createSearchCriterion(tag, comparator, value);
	}

	/**
	 *
	 * @param tag
	 * @param value
	 * @param comparator
	 * @param combinator
	 * @return
	 */
	public SearchCriterion createSearchCriterion(int tag, int comparator, 
		String value, int combinator)
	{
		return dicomFactory.createSearchCriterion(tag, comparator, value,
			combinator);
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public SearchCriterion createSearchCriterion(SearchCriterion a,
		SearchCriterion b)
	{
		return dicomFactory.createSearchCriterion(a, b);
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @param combinator
	 * @return
	 */
	public SearchCriterion createSearchCriterion(SearchCriterion a,
		SearchCriterion b, int combinator)
	{
		return dicomFactory.createSearchCriterion(a, b, combinator);
	}

	/**
	 *
	 * @param criteria
	 * @return
	 */
	public SearchCriterion createSearchCriterion(List<SearchCriterion> criteria)
	{
		return dicomFactory.createSearchCriterion(criteria);
	}

	/**
	 *
	 * @param criteria
	 * @param combinator
	 * @return
	 */
	public SearchCriterion createSearchCriterion(List<SearchCriterion> criteria,
		int combinator)
	{
		return dicomFactory.createSearchCriterion(criteria, combinator);
	}

	/**
	 *
	 * @return
	 */
	public SearchSpecification createSearchSpecification()
	{
		return dicomFactory.createSearchSpecification();
	}

	/**
	 *
	 * @param sopInstance
	 * @return
	 */
	public Series createSeries(SopInstance sopInstance)
	{
		return dicomFactory.createSeries(sopInstance);
	}

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Series createSeries(String uid)
	{
		return dicomFactory.createSeries(uid);
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public SopInstance createSopInstance(String path)
	{
		return dicomFactory.createSopInstance(new File(path));
	}

	/**
	 *
	 * @param file
	 * @return
	 */
	public SopInstance createSopInstance(File file)
	{
		return dicomFactory.createSopInstance(file);
	}

	/**
	 *
	 * @param file
	 * @param dcm
	 * @return
	 */
	public SopInstance createSopInstance(File file, DicomObject dcm)
	{
		return dicomFactory.createSopInstance(file, dcm);
	}

	/**
	 *
	 * @param sopInstance
	 * @return
	 */
	public Study createStudy(SopInstance sopInstance)
	{
		return dicomFactory.createStudy(sopInstance);
	}

	/**
	 *
	 * @param uid
	 * @return
	 */
	public Study createStudy(String uid)
	{
		return dicomFactory.createStudy(uid);
	}

	/*
	 *	Private constructor to prevent direct instantiation
	 */
	private DicomToolkit()
	{}

	public interface DicomFactory
	{

		/**
		 *
		 * @param key
		 * @param props
		 * @return
		 */
		DataSource createDataSource(String key, Properties props);

		/**
		 *
		 * @return
		 * @throws DatabaseException
		 */
		DicomDatabase createDicomDatabase() throws DatabaseException;

		/**
		 *
		 * @param properties
		 * @return
		 * @throws etherj.db.DatabaseException
		 */
		DicomDatabase createDicomDatabase(Properties properties)
			throws DatabaseException;

		/**
		 *
		 * @return
		 */
		PathScan<DicomObject> createPathScan();

		/**
		 *
		 * @param sopInstance
		 * @return
		 */
		Patient createPatient(SopInstance sopInstance);

		/**
		 *
		 * @param name
		 * @param id
		 * @param birthDate
		 * @return
		 */
		Patient createPatient(String name, String birthDate, String id);

		/**
		 *
		 * @return
		 */
		PatientRoot createPatientRoot();

		/**
		 *
		 * @param source
		 * @return
		 */
		RoiConverter createRoiConverter(DataSource source);

		/**
		 *
		 * @param dcm
		 * @return
		 */
		RtContour createRtContour(DicomObject dcm);

		/**
		 *
		 * @param roiItem
		 * @param contourItem
		 * @return
		 */
		RtRoi createRtRoi(DicomObject roiItem, DicomObject contourItem);

		/**
		 *
		 * @param dcm
		 * @return
		 */
		RtStruct createRtStruct(DicomObject dcm);

		/**
		 *
		 * @param tag
		 * @param value
		 * @param comparator
		 * @return
		 */
		public SearchCriterion createSearchCriterion(int tag, int comparator,
			String value);

		/**
		 *
		 * @param tag
		 * @param value
		 * @param comparator
		 * @param combinator
		 * @return
		 */
		SearchCriterion createSearchCriterion(int tag, int comparator,
			String value, int combinator);

		/**
		 *
		 * @param a
		 * @param b
		 * @return
		 */
		SearchCriterion createSearchCriterion(SearchCriterion a,
			SearchCriterion b);

		/**
		 *
		 * @param a
		 * @param b
		 * @param combinator
		 * @return
		 */
		SearchCriterion createSearchCriterion(SearchCriterion a,
			SearchCriterion b, int combinator);

		/**
		 *
		 * @param criteria
		 * @return
		 */
		SearchCriterion createSearchCriterion(
			List<SearchCriterion> criteria);

		/**
		 *
		 * @param criteria
		 * @param combinator
		 * @return
		 */
		SearchCriterion createSearchCriterion(
			List<SearchCriterion> criteria, int combinator);

		/**
		 *
		 * @return
		 */
		SearchSpecification createSearchSpecification();

		/**
		 *
		 * @param sopInstance
		 * @return
		 */
		Series createSeries(SopInstance sopInstance);

		/**
		 *
		 * @param uid
		 * @return
		 */
		Series createSeries(String uid);

		/**
		 *
		 * @param file
		 * @return
		 */
		SopInstance createSopInstance(File file);

		/**
		 *
		 * @param file
		 * @param dcm
		 * @return
		 */
		SopInstance createSopInstance(File file, DicomObject dcm);

		/**
		 *
		 * @param sopInstance
		 * @return
		 */
		Study createStudy(SopInstance sopInstance);

		/**
		 *
		 * @param uid
		 * @return
		 */
		Study createStudy(String uid);
	}
}
