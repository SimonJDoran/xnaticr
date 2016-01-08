/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.PathScan;
import etherj.PathScanContext;
import etherj.dicom.DicomDatabase;
import etherj.db.DatabaseException;
import etherj.db.SqliteDatabase;
import etherj.dicom.Modality;
import etherj.dicom.Patient;
import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import etherj.dicom.Study;
import etherj.dicom.DicomToolkit;
import etherj.dicom.DicomUtils;
import etherj.dicom.PatientRoot;
import etherj.dicom.SearchCriterion;
import etherj.dicom.SearchSpecification;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
class SqliteDicomDatabase extends SqliteDatabase implements DicomDatabase
{
	private static final Logger logger =
		LoggerFactory.getLogger(SqliteDicomDatabase.class);
	private static final String INSTANCE = "instance";
	private static final String INSTANCE_PK = "pk";
	private static final String INSTANCE_SERIESFK = "serFk";
	private static final String INSTANCE_UID = "uid";
	private static final String INSTANCE_PATH = "path";
	private static final String INSTANCE_FRAMES = "frames";
	private static final String INSTANCE_SOPCLASSUID = "sopClassUid";
	private static final String INSTANCE_MODALITY = "modality";
	private static final String INSTANCE_NUMBER = "number";
	private static final String PATIENT = "patient";
	private static final String PATIENT_COMMENTS = "patComm";
	private static final String PATIENT_DOB = "patDob";
	private static final String PATIENT_ID = "patId";
	private static final String PATIENT_KEY = "patKey";
	private static final String PATIENT_NAME = "patName";
	private static final String PATIENT_OTHERID = "patOtherId";
	private static final String PATIENT_PK = "pk";
	private static final String REFERENCE = "reference";
	private static final String REFERENCE_PK = "pk";
	private static final String REFERENCE_INSTANCEFK = "instFk";
	private static final String REFERENCE_UID = "uid";
	private static final String SERIES = "series";
	private static final String SERIES_PK = "pk";
	private static final String SERIES_STUDYFK = "stuFk";
	private static final String SERIES_UID = "uid";
	private static final String SERIES_NUMBER = "number";
	private static final String SERIES_TIME = "time";
	private static final String SERIES_DESC = "desc";
	private static final String SERIES_MODALITY = "modality";
	private static final String STUDY = "study";
	private static final String STUDY_PK = "pk";
	private static final String STUDY_PATIENTFK = "patFk";
	private static final String STUDY_UID = "uid";
	private static final String STUDY_ID = "studyId";
	private static final String STUDY_DATE = "studyDate";
	private static final String STUDY_DESC = "desc";
	private static final String STUDY_ACCESSION = "accession";
	private static final String STUDY_MODALITY = "modality";
	private int bufferMax = 256;
	private PreparedStatement insertInstStmt;
	private PreparedStatement insertRefStmt;
	private boolean isScanning = false;
	private PreparedStatement selectInstInSeriesStmt;
	private PreparedStatement selectInstPkStmt;
	private PreparedStatement selectInstStmt;
	private PreparedStatement selectPatPkStmt;
	private PreparedStatement selectSeriesPkStmt;
	private PreparedStatement selectSeriesStmt;
	private PreparedStatement selectStudyPkStmt;
	private final List<SopInstance> sopInstBuffer = new ArrayList<>();

	SqliteDicomDatabase(String filename) throws DatabaseException
	{
		this(new File(filename));
	}

	SqliteDicomDatabase(File file) throws DatabaseException
	{
		super(file);
		initTables();
		initIndices();
		checkTables();
		prepareStatements();
	}

	@Override
	public void close() throws DatabaseException
	{
		safeClose(insertInstStmt);
		safeClose(selectInstInSeriesStmt);
		safeClose(selectInstPkStmt);
		safeClose(selectInstStmt);
		safeClose(selectPatPkStmt);
		safeClose(selectSeriesPkStmt);
		safeClose(selectSeriesStmt);
		safeClose(selectStudyPkStmt);
		sopInstBuffer.clear();
		super.close();
	}

	@Override
	public void display()
	{
		display("", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display("", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Valid: "+(isValid() ? "true" : "false"));
		Properties props = getProperties();
		Set<String> names = props.stringPropertyNames();
		for (String name : names)
		{
			System.out.println(pad+name+": "+props.getProperty(name));
		}
	}

	public int getImportQueueLength()
	{
		return bufferMax;
	}

	@Override
	public void importDirectory(String path) throws IOException
	{
		importDirectory(path, true);
	}

	@Override
	public void importDirectory(String path, boolean recurse) throws IOException
	{
		File targetDir = new File(path);
		PathScan<DicomObject> pathScan = new DefaultPathScan();
		pathScan.addContext(new DicomReceiver());
		pathScan.scan(targetDir.getAbsolutePath(), recurse);
	}

	@Override
	public PatientRoot search(SearchSpecification spec)
	{
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		PatientRoot root = toolkit.createPatientRoot();
		Statement stmt = null;
		long tick = System.currentTimeMillis();
		try
		{
			String sql = "SELECT "+
				"p."+PATIENT_KEY+","+"p."+PATIENT_NAME+","+"p."+PATIENT_ID+","+
				"p."+PATIENT_DOB+","+"p."+PATIENT_OTHERID+","+"p."+PATIENT_COMMENTS+","+
				"st."+STUDY_PK+","+"st."+STUDY_UID+","+"st."+STUDY_ID+","+
				"st."+STUDY_DATE+","+"st."+STUDY_DESC+","+"st."+STUDY_ACCESSION+","+
				"st."+STUDY_MODALITY+
				" FROM "+STUDY+" AS st JOIN "+PATIENT+" AS p"+
				" ON p."+PATIENT_PK+"=st."+STUDY_PATIENTFK+
				" JOIN "+SERIES+" AS se ON se."+SERIES_STUDYFK+"=st."+STUDY_PK;
			sql += processSpec(spec);
			stmt = createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				String patKey = rs.getString(1);
				Patient patient;
				if (root.hasPatient(patKey))
				{
					patient = root.getPatient(patKey);
				}
				else
				{
					String name = rs.getString(2);
					String patId = rs.getString(3);
					String birthDate = rs.getString(4);
					patient = toolkit.createPatient(name, birthDate, patId);
					patient.setOtherId(rs.getString(5));
					patient.setComments(rs.getString(6));
					root.addPatient(patient);
				}

				// Study is unique in this query
				int stuPk = rs.getInt(7);
				Study study = toolkit.createStudy(rs.getString(8));
				study.setId(rs.getString(9));
				study.setDate(rs.getString(10));
				study.setDescription(rs.getString(11));
				study.setAccession(rs.getString(12));
				patient.addStudy(study);
				fillStudy(study, stuPk);
			}
			logger.debug("Time taken for query: "+
				(System.currentTimeMillis()-tick)+"ms");
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught", ex);
		}
		finally
		{
			safeClose(stmt);
		}
		return root;
	}

	@Override
	public PatientRoot search(String query)
	{
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		// Search for common items
		SearchSpecification spec = new SearchSpecification();
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.PatientName, SearchCriterion.Like, query, SearchCriterion.Or));
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.PatientID, SearchCriterion.Like, query, SearchCriterion.Or));
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.OtherPatientIDs, SearchCriterion.Like, query, SearchCriterion.Or));
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.PatientComments, SearchCriterion.Like, query, SearchCriterion.Or));
		try
		{
			Integer.parseInt(query);
			spec.addCriterion(toolkit.createSearchCriterion(
				Tag.StudyDate, SearchCriterion.Like, query, SearchCriterion.Or));
		}
		catch (NumberFormatException exIgnore)
		{}
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.StudyDescription, SearchCriterion.Like, query, SearchCriterion.Or));
		spec.addCriterion(toolkit.createSearchCriterion(
			Tag.AccessionNumber, SearchCriterion.Like, query, SearchCriterion.Or));

		return search(spec);
	}

	@Override
	public SopInstance searchInstance(String uid) throws DatabaseException
	{
		SopInstance sopInst = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			InstanceKeyPair ikp = getInstance(uid);
			sopInst = ikp.sopInstance;
			if (sopInst == null)
			{
				return sopInst;
			}
			int seriesFk = ikp.key;
			stmt = createStatement();
			String seriesSql = "SELECT "+SERIES_UID+","+SERIES_STUDYFK+" FROM "+
				SERIES+" WHERE "+SERIES_PK+"="+seriesFk;
			rs = stmt.executeQuery(seriesSql);
			if (rs.isAfterLast())
			{
				throw new DatabaseException("Missing series PK");
			}
			sopInst.setSeriesUid(rs.getString(1));
			int studyFk = rs.getInt(2);
			rs.close();
			String studySql = "SELECT "+STUDY_UID+" FROM "+STUDY+" WHERE "+
				STUDY_PK+"="+studyFk;
			rs = stmt.executeQuery(studySql);
			if (rs.isAfterLast())
			{
				throw new DatabaseException("Missing study PK");
			}
			sopInst.setStudyUid(rs.getString(1));
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}

		return sopInst;
	}

	@Override
	public List<SopInstance> searchInstance(SearchSpecification spec) throws DatabaseException
	{
		List<SopInstance> sopInstList = new ArrayList<>();
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		String sql = "SELECT DISTINCT i."+INSTANCE_PATH+",i."+INSTANCE_UID+",i."+
			INSTANCE_FRAMES+",i."+INSTANCE_MODALITY+",i."+
			INSTANCE_SOPCLASSUID+",i."+INSTANCE_NUMBER+",se."+SERIES_UID+",st."+
			STUDY_UID+" FROM ";
		if (requiresReference(spec))
		{
			sql += REFERENCE+" AS r,";
		}
		sql += INSTANCE+" AS i JOIN "+SERIES+" AS se ON i."+INSTANCE_SERIESFK+
			"=se."+SERIES_PK+" JOIN "+STUDY+" AS st ON se."+SERIES_STUDYFK+
			"=st."+STUDY_PK;
		sql += processSpec(spec);
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				SopInstance sopInst = toolkit.createSopInstance(
					new File(rs.getString(1)));
				sopInst.setUid(rs.getString(2));
				sopInst.setNumberOfFrames(rs.getInt(3));
				sopInst.setModality(Modality.string(rs.getLong(4)));
				sopInst.setSopClassUid(rs.getString(5));
				sopInst.setInstanceNumber(rs.getInt(6));
				sopInst.setSeriesUid(rs.getString(7));
				sopInst.setStudyUid(rs.getString(8));
				sopInstList.add(sopInst);
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
	
		return sopInstList;
	}

	@Override
	public List<SopInstance> searchInstance(SearchSpecification inSpec,
		Collection<String> refUids) throws DatabaseException
	{
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		Map<String,SopInstance> rtMap = new HashMap<>();
		Iterator<String> iter = refUids.iterator();
		while (iter.hasNext())
		{
			String refUid = iter.next();
			SearchSpecification refSpec = new SearchSpecification();
			refSpec.addCriteria(inSpec.getCriteria());
			SearchCriterion refUidSc = toolkit.createSearchCriterion(
				Tag.ReferencedSOPInstanceUID, SearchCriterion.Equal, refUid);
			refSpec.addCriterion(refUidSc);
			List<SopInstance> instList = searchInstance(refSpec);
			for (SopInstance rtInst : instList)
			{
				rtMap.put(rtInst.getUid(), rtInst);
			}
		}
		List<SopInstance> sopInstList = new ArrayList<>();
		iter = rtMap.keySet().iterator();
		while (iter.hasNext())
		{
			sopInstList.add(rtMap.get(iter.next()));
		}
		return sopInstList;
	}

	public void setImportQueueLength(int length)
	{
		if (length < 1)
		{
			throw new IllegalArgumentException(
				"Queue length must be greater than one");
		}
		bufferMax = length;
	}

	@Override
	public void shutdown() throws DatabaseException
	{
		close();
	}

	@Override
	public void storeInstance(SopInstance sopInst) throws DatabaseException
	{
		// Ignore presentation state
		if (sopInst.getSopClassUid().startsWith("1.2.840.10008.5.1.4.1.1.11"))
		{
			return;
		}
		sopInstBuffer.add(sopInst);
		if (!isScanning || sopInstBuffer.size() >= bufferMax)
		{
			processBuffer();
		}
	}

	@Override
	public void storePatient(Patient patient) throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = createStatement();
			stmt.executeUpdate("BEGIN TRANSACTION");
			int patPk = getPk(selectPatPkStmt, DicomUtils.makePatientKey(patient));
			if (patPk == 0)
			{
				patPk = insertPatient(patient);
			}
			List<Study> studyList = patient.getStudyList();
			for (Study study : studyList)
			{
				int studyPk = getPk(selectStudyPkStmt, study.getUid());
				if (studyPk == 0)
				{
					studyPk = insertStudy(study, patPk);
				}
				List<Series> seriesList = study.getSeriesList();
				for (Series series : seriesList)
				{
					int seriesPk = getPk(selectSeriesPkStmt, series.getUid());
					if (seriesPk == 0)
					{
						seriesPk = insertSeries(series, studyPk);
					}
					List<SopInstance> sopInstList = series.getSopInstanceList();
					for (SopInstance sopInst : sopInstList)
					{
						int sopInstPk = getPk(selectInstPkStmt, sopInst.getUid());
						if (sopInstPk == 0)
						{
							insertInstance(sopInst, seriesPk);
						}
					}
				}
			}
			stmt.executeUpdate("COMMIT TRANSACTION");
			stmt.close();
		}
		catch (SQLException ex)
		{
			logger.warn("Rolling back transaction: "+ex.getMessage());
			try
			{
				if (stmt != null)
				{
					stmt.executeUpdate("ROLLBACK TRANSACTION");
				}
			}
			catch (Exception exIgnore)
			{}
			finally
			{
				safeClose(stmt);
			}
			throw new DatabaseException(ex);
		}
	}

	private String createCriterionSql(SearchCriterion crit)
	{
		StringBuilder sb = new StringBuilder();
		if (crit.hasCriteria())
		{
			sb.append("(");
			List<SearchCriterion> critList = crit.getCriteria();
			sb.append(createCriterionSql(critList.get(0)));
			for (int i=1; i<critList.size(); i++)
			{
				SearchCriterion sc = critList.get(i);
				sb.append(" ").append(sc.createCombinatorSql()).append(" ")
					.append(createCriterionSql(sc));
			}
			sb.append(")");
			return sb.toString();
		}

		String value = crit.getValue();
		String compSql = crit.createComparatorSql();
		if (value.isEmpty() || compSql.isEmpty())
		{
			return "";
		}
		int tag = crit.getTag();
		switch (tag)
		{
			case Tag.PatientName:
				sb.append("p.").append(PATIENT_NAME).append(" ").append(compSql);
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"%").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.PatientID:
				sb.append("p.").append(PATIENT_ID).append(" ").append(compSql);
				// PatientID is naturally searched from the beginning
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.OtherPatientIDs:
				sb.append("p.").append(PATIENT_OTHERID).append(" ").append(compSql);
				// OtherPatientID is naturally searched from the beginning
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.PatientComments:
				sb.append("p.").append(PATIENT_COMMENTS).append(" ").append(compSql);
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"%").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.StudyDate:
				sb.append("st.").append(STUDY_DATE).append(" ").append(compSql);
				// StudyDate is naturally searched from the beginning
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.StudyDescription:
				sb.append("st.").append(STUDY_DESC).append(" ").append(compSql);
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"%").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.AccessionNumber:
				sb.append("st.").append(STUDY_ACCESSION).append(" ").append(compSql);
				// Accession is naturally searched from the beginning
				if (crit.getComparator() == SearchCriterion.Like)
				{
					sb.append(" \"").append(value).append("%\"");
				}
				else
				{
					sb.append(" \"").append(value).append("\"");
				}
				break;

			case Tag.Modality:
				sb = createModalitySql(sb, crit);
				break;

			case Tag.SeriesInstanceUID:
				sb = createSeriesUidSql(sb, crit);
				break;

			case Tag.StudyInstanceUID:
				sb = createStudyUidSql(sb, crit);
				break;

			case Tag.ReferencedSOPInstanceUID:
				sb.append("(r.").append(REFERENCE_UID).append(" ").append(compSql)
					.append(" \"").append(value).append("\"").append(" AND r.")
					.append(REFERENCE_INSTANCEFK).append("=i.").append(INSTANCE_PK)
					.append(")");
				break;

			default:
				break;
		}
		return sb.toString();
	}

	private StringBuilder createModalitySql(StringBuilder sb,
		SearchCriterion crit)
	{
		long longValue = Modality.bitmask(crit.getValue());
		if (longValue == 0L)
		{
			return sb;
		}
		String compSql = crit.createComparatorSql();
		switch (crit.getDicomType())
		{
			case SearchCriterion.Study:
				sb.append("(st.").append(STUDY_MODALITY).append(" & ")
					.append(longValue).append(") ").append(compSql).append(" ")
					.append(longValue);
				break;

			case SearchCriterion.Series:
				sb.append("se.").append(SERIES_MODALITY).append(compSql)
					.append(longValue);
				break;

			case SearchCriterion.Instance:
				sb.append("i.").append(INSTANCE_MODALITY).append(compSql)
					.append(longValue);
				break;

			default:
		}
		return sb;
	}

	private StringBuilder createSeriesUidSql(StringBuilder sb,
		SearchCriterion crit)
	{
		String value = crit.getValue();
		if ((value == null) || value.isEmpty())
		{
			return sb;
		}
		String compSql = crit.createComparatorSql();
		switch (crit.getDicomType())
		{
			case SearchCriterion.Instance:
			case SearchCriterion.Series:
				sb.append("se.").append(SERIES_UID).append(compSql).append("\"")
					.append(value).append("\"");
				break;

			default:
		}
		return sb;
	}

	private StringBuilder createStudyUidSql(StringBuilder sb,
		SearchCriterion crit)
	{
		String value = crit.getValue();
		if ((value == null) || value.isEmpty())
		{
			return sb;
		}
		String compSql = crit.createComparatorSql();
		switch (crit.getDicomType())
		{
			case SearchCriterion.Instance:
			case SearchCriterion.Series:
			case SearchCriterion.Study:
				sb.append("st.").append(STUDY_UID).append(compSql).append("\"")
					.append(value).append("\"");
				break;

			default:
		}
		return sb;
	}

	private void fillSeries(Series series, int seriesPk)
	{
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		try
		{
			selectInstInSeriesStmt.setInt(1, seriesPk);
			ResultSet rs = selectInstInSeriesStmt.executeQuery();
			while (rs.next())
			{
				String path = rs.getString(2);
				SopInstance sopInst = toolkit.createSopInstance(new File(path));
				sopInst.setUid(rs.getString(1));
				sopInst.setNumberOfFrames(rs.getInt(3));
				sopInst.setModality(Modality.string(rs.getLong(4)));
				sopInst.setSopClassUid(rs.getString(5));
				sopInst.setInstanceNumber(rs.getInt(6));
				sopInst.setSeriesUid(series.getUid());
				sopInst.setStudyUid(series.getStudyUid());
				series.addSopInstance(sopInst);
			}
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught", ex);
		}
	}

	private void fillStudy(Study study, int stuPk)
	{
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		try
		{
			selectSeriesStmt.setInt(1, stuPk);
			ResultSet rs = selectSeriesStmt.executeQuery();
			while (rs.next())
			{
				int seriesPk = rs.getInt(1);
				Series series = toolkit.createSeries(rs.getString(2));
				series.setDescription(rs.getString(3));
				series.setNumber(rs.getInt(4));
				series.setModality(Modality.string(rs.getLong(5)));
				series.setTime(rs.getDouble(6));
				series.setStudyUid(study.getUid());
				study.addSeries(series);
				fillSeries(series, seriesPk);
			}
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught", ex);
		}
	}

	private InstanceKeyPair getInstance(String uid) throws SQLException
	{
		InstanceKeyPair result = new InstanceKeyPair();
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		ResultSet rs = null;
		try
		{
			selectInstStmt.setString(1, uid);
			rs = selectInstStmt.executeQuery();
			if (rs.isAfterLast())
			{
				rs.close();
				return result;
			}
			SopInstance sopInst = toolkit.createSopInstance(
				new File(rs.getString(1)));
			sopInst.setUid(uid);
			sopInst.setNumberOfFrames(rs.getInt(2));
			sopInst.setModality(Modality.string(rs.getLong(3)));
			sopInst.setSopClassUid(rs.getString(4));
			sopInst.setInstanceNumber(rs.getInt(5));
			result.sopInstance = sopInst;
			result.key = rs.getInt(6);
		}
		finally
		{
			safeClose(rs);
		}
	
		return result;
	}

	private int getPk(PreparedStatement stmt, String uid) throws SQLException
	{
		stmt.setString(1, uid);
		ResultSet rs = stmt.executeQuery();
		int pk = 0;
		if (!rs.isAfterLast())
		{
			pk = rs.getInt(1);
		}
		rs.close();

		return pk;
	}

	private int insertInstance(SopInstance sopInst, int seriesFk) throws SQLException
	{
		String uid = sopInst.getUid();
		ResultSet rs = null;
		int sopInstPk = 0;
		try
		{
			insertInstStmt.setInt(1, seriesFk);
			insertInstStmt.setString(2, uid);
			insertInstStmt.setString(3, sopInst.getFile().getAbsolutePath());
			insertInstStmt.setInt(4, sopInst.getNumberOfFrames());
			insertInstStmt.setLong(5, Modality.bitmask(sopInst.getModality()));
			insertInstStmt.setString(6, sopInst.getSopClassUid());
			insertInstStmt.setInt(7, sopInst.getInstanceNumber());
			insertInstStmt.executeUpdate();
			rs = insertInstStmt.getGeneratedKeys();
			sopInstPk = rs.getInt(1);
			rs.close();
			Set<String> uids = sopInst.getReferencedSopInstanceUidSet();
			Iterator<String> iter = uids.iterator();
			while (iter.hasNext())
			{
				insertReference(iter.next(), sopInstPk);
			}
		}
		finally
		{
			safeClose(rs);
		}

		return sopInstPk;
	}

	private int insertPatient(Patient patient) throws SQLException
	{
		String patKey = DicomUtils.makePatientKey(patient);
		String sql = "INSERT INTO "+PATIENT+"("+PATIENT_KEY+","+PATIENT_NAME+","+
			PATIENT_ID+","+PATIENT_DOB+","+PATIENT_OTHERID+","+PATIENT_COMMENTS+") "+
			"VALUES (?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int patPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setString(1, patKey);
			stmt.setString(2, patient.getName());
			stmt.setString(3, patient.getId());
			stmt.setInt(4, DicomUtils.dateToInt(patient.getBirthDate()));
			stmt.setString(5, patient.getOtherId());
			stmt.setString(6, patient.getComments());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			patPk = rs.getInt(1);
			logger.debug("Patient key {} inserted with PK {}", patKey, patPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return patPk;
	}

	private int insertReference(String uid, int instFk) throws SQLException
	{
		ResultSet rs = null;
		int refPk = 0;
		try
		{
			insertRefStmt.setInt(1, instFk);
			insertRefStmt.setString(2, uid);
			insertRefStmt.executeUpdate();
			rs = insertRefStmt.getGeneratedKeys();
			refPk = rs.getInt(1);
		}
		finally
		{
			safeClose(rs);
		}

		return refPk;
	}

	private int insertSeries(Series series, int studyFk) throws SQLException
	{
		String uid = series.getUid();
		String sql = "INSERT INTO "+SERIES+"("+SERIES_STUDYFK+","+SERIES_UID+","+
			SERIES_DESC+","+SERIES_NUMBER+","+SERIES_MODALITY+","+SERIES_TIME+") "+
			"VALUES (?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int seriesPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, studyFk);
			stmt.setString(2, uid);
			stmt.setString(3, series.getDescription());
			stmt.setInt(4, series.getNumber());
			long seModality = Modality.bitmask(series.getModality());
			stmt.setLong(5, seModality);
			stmt.setDouble(6, series.getTime());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			seriesPk = rs.getInt(1);
			rs.close();
			stmt.close();
			// Triggers in SQL can't use variables, have to do update explicitly
			String stuModSql = "SELECT "+STUDY_MODALITY+" FROM "+STUDY+" WHERE "+
				STUDY_PK+"="+studyFk;
			stmt = prepareStatement(stuModSql);
			rs = stmt.executeQuery();
			long stuModality = rs.getLong(1);
			rs.close();
			stmt.close();
			if ((stuModality & seModality) == 0L)
			{
				String updateSql = "UPDATE "+STUDY+" SET "+STUDY_MODALITY+"="+
					(seModality | stuModality)+" WHERE "+STUDY_PK+"="+studyFk;
				stmt = prepareStatement(updateSql);
			}
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}

		return seriesPk;
	}

	private int insertStudy(Study study, int patFk) throws SQLException
	{
		String uid = study.getUid();
		String sql = "INSERT INTO "+STUDY+"("+STUDY_PATIENTFK+","+STUDY_UID+","+
			STUDY_ID+","+STUDY_DATE+","+STUDY_DESC+","+STUDY_ACCESSION+
			","+STUDY_MODALITY+") "+
			"VALUES (?,?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int studyPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, patFk);
			stmt.setString(2, uid);
			stmt.setString(3, study.getId());
			stmt.setInt(4, DicomUtils.dateToInt(study.getDate()));
			stmt.setString(5, study.getDescription());
			stmt.setString(6, study.getAccession());
			// Modality is zero here, inserting series will update it
			stmt.setLong(7, 0);
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			studyPk = rs.getInt(1);
			logger.debug("Study UID {} inserted with PK {}", uid, studyPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}

		return studyPk;
	}

	private void initIndices()
	{
		// Patient
		addIndexSpec(PATIENT, PATIENT_NAME);
		addIndexSpec(PATIENT, PATIENT_ID);
		addIndexSpec(PATIENT, PATIENT_DOB);
		addIndexSpec(PATIENT, PATIENT_OTHERID);

		// Study
		addIndexSpec(STUDY, STUDY_PATIENTFK);
		addIndexSpec(STUDY, STUDY_ID);
		addIndexSpec(STUDY, STUDY_DATE);

		// Series
		addIndexSpec(SERIES, SERIES_STUDYFK);
		addIndexSpec(SERIES, SERIES_MODALITY);

		// Instance
		addIndexSpec(INSTANCE, INSTANCE_SERIESFK);
		addIndexSpec(INSTANCE, INSTANCE_MODALITY);

		// Reference
		addIndexSpec(REFERENCE, REFERENCE_INSTANCEFK);
		addIndexSpec(REFERENCE, REFERENCE_UID);
	}

	private void initTables()
	{
		String patientSql = "CREATE TABLE \""+PATIENT+"\" ("+
			"\""+PATIENT_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+PATIENT_KEY+"\" TEXT UNIQUE NOT NULL,"+
			"\""+PATIENT_NAME+"\" TEXT NOT NULL,"+
			"\""+PATIENT_ID+"\" TEXT NOT NULL,"+
			"\""+PATIENT_DOB+"\" INTEGER NOT NULL,"+
			"\""+PATIENT_OTHERID+"\" TEXT NOT NULL,"+
			"\""+PATIENT_COMMENTS+"\" TEXT NOT NULL"+
			")";
		addTableSpec(PATIENT, patientSql);

		String studySql = "CREATE TABLE \""+STUDY+"\" ("+
			"\""+STUDY_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+STUDY_PATIENTFK+"\" INTEGER NOT NULL,"+
			"\""+STUDY_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+STUDY_ID+"\" TEXT NOT NULL,"+
			"\""+STUDY_DATE+"\" INTEGER NOT NULL,"+
			"\""+STUDY_DESC+"\" TEXT NOT NULL,"+
			"\""+STUDY_ACCESSION+"\" TEXT NOT NULL,"+
			"\""+STUDY_MODALITY+"\" INTEGER NOT NULL,"+
			"FOREIGN KEY ("+STUDY_PATIENTFK+") REFERENCES "+PATIENT+"("+PATIENT_PK+")"+
			")";
		addTableSpec(STUDY, studySql);

		String seriesSql = "CREATE TABLE \""+SERIES+"\" ("+
			"\""+SERIES_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+SERIES_STUDYFK+"\" INTEGER NOT NULL,"+
			"\""+SERIES_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+SERIES_DESC+"\" TEXT NOT NULL,"+
			"\""+SERIES_NUMBER+"\" INTEGER NOT NULL,"+
			"\""+SERIES_MODALITY+"\" INTEGER NOT NULL,"+
			"\""+SERIES_TIME+"\" REAL NOT NULL,"+
			"FOREIGN KEY ("+SERIES_STUDYFK+") REFERENCES "+STUDY+"("+STUDY_PK+")"+
			")";
		addTableSpec(SERIES, seriesSql);

		String instanceSql = "CREATE TABLE \""+INSTANCE+"\" ("+
			"\""+INSTANCE_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+INSTANCE_SERIESFK+"\" INTEGER NOT NULL,"+
			"\""+INSTANCE_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+INSTANCE_PATH+"\" TEXT NOT NULL,"+
			"\""+INSTANCE_FRAMES+"\" INTEGER NOT NULL,"+
			"\""+INSTANCE_MODALITY+"\" INTEGER NOT NULL,"+
			"\""+INSTANCE_SOPCLASSUID+"\" TEXT NOT NULL,"+
			"\""+INSTANCE_NUMBER+"\" INTEGER NOT NULL,"+
			"FOREIGN KEY ("+INSTANCE_SERIESFK+") REFERENCES "+SERIES+"("+SERIES_PK+")"+
			")";
		addTableSpec(INSTANCE, instanceSql);

		String refSql = "CREATE TABLE \""+REFERENCE+"\" ("+
			"\""+REFERENCE_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+REFERENCE_INSTANCEFK+"\" INTEGER NOT NULL,"+
			"\""+REFERENCE_UID+"\" TEXT NOT NULL,"+
			"FOREIGN KEY ("+REFERENCE_INSTANCEFK+") REFERENCES "+INSTANCE+"("+INSTANCE_PK+")"+
			")";
		addTableSpec(REFERENCE, refSql);
	}

	private void prepareStatements() throws DatabaseException
	{
		try
		{
			// Instance searches
			String sql = "INSERT INTO "+INSTANCE+"("+INSTANCE_SERIESFK+","+
				INSTANCE_UID+","+INSTANCE_PATH+","+INSTANCE_FRAMES+","+
				INSTANCE_MODALITY+","+INSTANCE_SOPCLASSUID+","+
				INSTANCE_NUMBER+") "+"VALUES (?,?,?,?,?,?,?)";
			insertInstStmt = prepareStatement(sql);
			sql = "SELECT "+INSTANCE_PATH+","+INSTANCE_FRAMES+","+
				INSTANCE_MODALITY+","+INSTANCE_SOPCLASSUID+","+INSTANCE_NUMBER+","+
				INSTANCE_SERIESFK+" FROM "+INSTANCE+" WHERE "+INSTANCE_UID+"=(?)";
			selectInstStmt = prepareStatement(sql);
			sql = "SELECT "+INSTANCE_UID+","+INSTANCE_PATH+","+
				INSTANCE_FRAMES+","+INSTANCE_MODALITY+","+INSTANCE_SOPCLASSUID+","+
				INSTANCE_NUMBER+" FROM "+INSTANCE+" WHERE "+INSTANCE_SERIESFK+"=(?)";
			selectInstInSeriesStmt = prepareStatement(sql);

			// Primary key searches
			sql = "SELECT "+INSTANCE_PK+" FROM "+INSTANCE+" WHERE "+
				INSTANCE_UID+"=(?)";
			selectInstPkStmt = prepareStatement(sql);
			sql = "SELECT "+PATIENT_PK+" FROM "+PATIENT+" WHERE "+
				PATIENT_KEY+"=(?)";
			selectPatPkStmt = prepareStatement(sql);
			sql = "SELECT "+STUDY_PK+" FROM "+STUDY+" WHERE "+STUDY_UID+"=(?)";
			selectStudyPkStmt = prepareStatement(sql);
			sql = "SELECT "+SERIES_PK+" FROM "+SERIES+" WHERE "+SERIES_UID+"=(?)";
			selectSeriesPkStmt = prepareStatement(sql);

			// Series search
			sql = "SELECT "+SERIES_PK+","+SERIES_UID+","+SERIES_DESC+","+
				SERIES_NUMBER+","+SERIES_MODALITY+","+SERIES_TIME+
				" FROM "+SERIES+" WHERE "+SERIES_STUDYFK+"=(?)";
			selectSeriesStmt = prepareStatement(sql);

			sql = "INSERT INTO "+REFERENCE+"("+REFERENCE_INSTANCEFK+","+
				REFERENCE_UID+") "+"VALUES (?,?)";
			insertRefStmt = prepareStatement(sql);
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
	}

	private void processBuffer() throws DatabaseException
	{
		DicomToolkit toolkit = DicomToolkit.getDefaultToolkit();
		Statement stmt = null;
		try
		{
			stmt = createStatement();
			stmt.executeUpdate("BEGIN TRANSACTION");
			for (SopInstance sopInst : sopInstBuffer)
			{
				if (getPk(selectInstPkStmt, sopInst.getUid()) != 0)
				{
					continue;
				}
				int patPk = getPk(selectPatPkStmt, DicomUtils.makePatientKey(sopInst));
				if (patPk == 0)
				{
					patPk = insertPatient(toolkit.createPatient(sopInst));
				}
				int studyPk = getPk(selectStudyPkStmt, sopInst.getStudyUid());
				if (studyPk == 0)
				{
					studyPk = insertStudy(toolkit.createStudy(sopInst), patPk);
				}
				int seriesPk = getPk(selectSeriesPkStmt, sopInst.getSeriesUid());
				if (seriesPk == 0)
				{
					seriesPk = insertSeries(toolkit.createSeries(sopInst), studyPk);
				}
				insertInstance(sopInst, seriesPk);
			}
			stmt.executeUpdate("COMMIT TRANSACTION");
			stmt.close();
		}
		catch (SQLException exSql)
		{
			logger.warn("Rolling back transaction: "+exSql.getMessage());
			try
			{
				if (stmt != null)
				{
					stmt.executeUpdate("ROLLBACK TRANSACTION");
				}
			}
			catch (Exception exIgnore)
			{}
			finally
			{
				safeClose(stmt);
			}
			throw new DatabaseException(exSql);
		}
		finally
		{
			sopInstBuffer.clear();
		}
	}

	private String processSpec(SearchSpecification spec)
	{
		String sql = "";
		List<SearchCriterion> criteria = spec.getCriteria();
		if (criteria.isEmpty())
		{
			return sql;
		}
		List<String> critSqlList = new ArrayList<>();
		List<String> combineSqlList = new ArrayList<>();
		for (SearchCriterion crit : criteria)
		{
			String critSql = createCriterionSql(crit);
			String combineSql = crit.createCombinatorSql();
			if (!critSql.isEmpty() && !combineSql.isEmpty())
			{
				critSqlList.add(critSql);
				combineSqlList.add(combineSql);
			}
		}
		if (!critSqlList.isEmpty())
		{
			int nCriteria = critSqlList.size();
			String critSql = critSqlList.get(0);
			for (int i=1; i<nCriteria; i++)
			{
				critSql += " "+combineSqlList.get(i)+" "+critSqlList.get(i);
			}
			if (!critSql.isEmpty())
			{
				sql += " WHERE ("+critSql+")";
			}
		}
		return sql;
	}

	private boolean requiresReference(SearchSpecification spec)
	{
		List<SearchCriterion> criteria = spec.getCriteria();
		for (SearchCriterion crit : criteria)
		{
			if (crit.getTag() == Tag.ReferencedSOPInstanceUID)
			{
				return true;
			}
		}
		return false;
	}

	private class DicomReceiver implements PathScanContext<DicomObject>
	{
		private final DicomToolkit toolkit = DicomToolkit.getDefaultToolkit();

		@Override
		public void notifyItemFound(File file, DicomObject dcm)
		{
			try
			{
				// Ignore null or presentation state
				String uid = dcm.getString(Tag.SOPClassUID);
//				if ((uid == null) || uid.startsWith("1.2.840.10008.5.1.4.1.1.11"))
//				{
//					return;
//				}
				storeInstance(toolkit.createSopInstance(file, dcm));
			}
			catch (DatabaseException ex)
			{
				logger.error("Exception caught", ex);
			}
		}

		@Override
		public void notifyScanFinish()
		{
			try
			{
				processBuffer();
			}
			catch (DatabaseException ex)
			{
				logger.error("Exception caught", ex);
			}
			isScanning = false;
		}

		@Override
		public void notifyScanStart()
		{
			sopInstBuffer.clear();
			isScanning = true;
		}
	}

	/*
	 *	Structure to allow returning multiple values
	 */
	private class InstanceKeyPair
	{
		public SopInstance sopInstance = null;
		public int key = 0;
	}
}
