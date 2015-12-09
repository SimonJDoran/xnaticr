/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat.impl;

import etherj.Ether;
import etherj.IoUtils;
import etherj.PathScan;
import etherj.Xml;
import etherj.XmlException;
import etherj.dicom.DataSource;
import etherj.dicom.DicomReceiver;
import etherj.dicom.DicomToolkit;
import etherj.dicom.Modality;
import etherj.dicom.Patient;
import etherj.dicom.Series;
import etherj.dicom.Study;
import etherj.xnat.XnatException;
import etherj.xnat.XnatResultSet;
import etherj.xnat.XnatServerConnection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.dcm4che2.data.DicomObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author jamesd
 */
public class XnatDataSource implements DataSource
{
	private static final Logger logger = LoggerFactory.getLogger(
		XnatDataSource.class);
	private static final String Equals = "=";
	private static final String LessThan = "&lt;";
	private static final String IntegerType = "integer";
	private static final String StringType = "string";
	private static final String ProjectIdHeader = "ProjectId";
	private static final String ScanIdHeader = "ScanId";
	private static final String ScanLabelHeader = "ScanLabel";
	private static final String ScanTypeHeader = "ScanType";
	private static final String SessionIdHeader = "SessionId";
	private static final String SessionLabelHeader = "SessionLabel";
	private static final String SubjectIdHeader = "SubjectId";
	private static final Map<String,String> modalityMap = new HashMap<>();

	private final DicomToolkit dcmToolkit = DicomToolkit.getToolkit();
	private final XnatServerConnection xsc;
	private final String cachePath;

	static
	{
		modalityMap.put("MR", "mr");
		modalityMap.put("CT", "ct");
		modalityMap.put("PT", "pet");
	}

	XnatDataSource(XnatServerConnection xsc)
	{
		this.xsc = xsc;
		cachePath = new StringBuilder(Ether.getEtherDir()).append("xnat")
			.append(File.separator).append("cache").append(File.separator)
			.toString();
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
		xsc.display(indent+"  ");
	}

	@Override
	public Series getSeries(String uid)
	{
		logger.info("Searching for series. UID: {}", uid);
		return getSeries(uid, Modality.CT);
	}

	@Override
	public Series getSeries(String uid, String modality)
	{
		Series series = null;
		String lcModality = modalityMap.get(modality);
		if (lcModality == null)
		{
			logger.error("Unsupported modality: {}", modality);
			return series;
		}
		logger.info("Searching for {} series. UID: {}", modality, uid);
		String search = "/data/search?format=xml";
		String searchXml = createSeriesSearchXml(uid, lcModality);
		InputStream is = null;
		try
		{
			XnatResultSet searchRs = xsc.getResultSet(search, searchXml);
			if (searchRs.getRowCount() != 1)
			{
				logger.info("No results found for modality {} and UID {}",
					modality, uid);
				return series;
			}
			searchRs.display(true);
			String projectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				ProjectIdHeader));
			String subjectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				SubjectIdHeader));
			String sessionId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				SessionIdHeader));
			String scanId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				ScanIdHeader));
			String query = new StringBuilder("/data/archive/projects/")
				.append(projectId).append("/subjects/").append(subjectId)
				.append("/experiments/").append(sessionId).append("/scans/")
				.append(scanId).append("/resources/DICOM/files").toString();
			// Get list of files to compare to cache
			XnatResultSet fileRs = xsc.getResultSet(query+"?format=xml");
			fileRs.display(true);
			String scanRelPath = new StringBuilder(projectId).append(File.separator)
				.append(subjectId).append(File.separator).append(sessionId)
				.append(File.separator).append(scanId).append(File.separator)
				.toString();
			String zipPath = cachePath+scanRelPath;
			logger.info("Checking cache: {}", zipPath);
			if (checkCache(cachePath, fileRs))
			{
				logger.info("Loading from cache");
				series = parseSeriesCache(cachePath, uid);
			}
			// Not found in cache, pull data from server and cache it
			if (series == null)
			{
				Map<String,String> filePathMap = createFilePathMap(searchRs, fileRs);
				logger.info("Retrieving data");
				is = xsc.get(query+"?format=zip");
				unzip(is, zipPath, filePathMap);
				series = parseSeriesCache(cachePath, uid);
			}
		}
		catch (IOException | XnatException ex)
		{
			logger.error("Scan search error: ", ex);
		}
		finally
		{
			IoUtils.safeClose(is);
		}
		logger.info("Scan search complete: {}", (series != null) ? "OK" : "Failed");
		return series;
	}

	@Override
	public Study getStudy(String uid)
	{
		logger.info("Searching for series. UID: {}", uid);
		Study study = null;
		String search = "/data/search?format=xml";
		String searchXml = createStudySearchXml(uid, "mr");
		InputStream is = null;
		try
		{
			XnatResultSet searchRs = xsc.getResultSet(search, searchXml);
			if (searchRs.getRowCount() != 1)
			{
				return study;
			}
			searchRs.display(true);
			String projectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				ProjectIdHeader));
			String subjectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				SubjectIdHeader));
			String sessionId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				SessionIdHeader));
			String scanId = searchRs.get(0, searchRs.getColumnIndexByHeader(
				ScanIdHeader));
			String query = new StringBuilder("/data/archive/projects/")
				.append(projectId).append("/subjects/").append(subjectId)
				.append("/experiments/").append(sessionId).toString();
			String xmlQuery = query+"/resources/DICOM?format=xml";
			String zipQuery = query+"/scans/ALL/files?format=zip";
			// Get list of files to compare to cache
			Document fileDoc = xsc.getDocument(xmlQuery);
			System.out.println(Xml.toString(fileDoc));
//			String scanRelPath = new StringBuilder(projectId).append(File.separator)
//				.append(subjectId).append(File.separator).append(sessionId)
//				.append(File.separator).append(scanId).append(File.separator)
//				.toString();
//			String cachePath = zipPath+scanRelPath;
//			logger.info("Checking cache: {}", cachePath);
//			if (checkCache(cachePath, fileRs))
//			{
//				logger.info("Loading from cache");
//				study = parseStudyCache(cachePath, uid);
//			}
//			// Not found in cache, pull data from server and cache it
//			if (study == null)
//			{
//				Map<String,String> filePathMap = createFilePathMap(searchRs, fileRs);
//				logger.info("Retrieving data");
				is = xsc.get(zipQuery);
				unzip(is, cachePath, null);
//				study = parseSeriesCache(cachePath, uid);
//			}
		}
		catch (IOException | XnatException | XmlException ex)
		{
			logger.error("Study search error: ", ex);
		}
		finally
		{
			IoUtils.safeClose(is);
		}
		logger.info("Study search complete: {}", (study != null) ? "OK" : "Failed");
		return study;
	}

	private StringBuilder addRootElement(StringBuilder sb, String name)
	{
		sb.append("	<xdat:root_element_name>").append(name)
			.append("</xdat:root_element_name>\n");
		return sb;
	}

	private StringBuilder addSearchField(StringBuilder sb, String name,
		String fieldId, int seq, String type, String header)
	{
		sb.append("	<xdat:search_field>\n");
		sb.append("		<xdat:element_name>").append(name)
			.append("</xdat:element_name>\n");
		sb.append("		<xdat:field_ID>").append(fieldId).append("</xdat:field_ID>\n");
		sb.append("		<xdat:sequence>").append(seq).append("</xdat:sequence>\n");
		sb.append("		<xdat:type>").append(type).append("</xdat:type>\n");
		sb.append("		<xdat:header>").append(header).append("</xdat:header>\n");
		sb.append("	</xdat:search_field>\n");
		return sb;
	}

	private StringBuilder addCriterion(StringBuilder sb, String schemaField,
		String comparator, String value)
	{
		sb.append("		<xdat:criteria override_value_formatting=\"0\">\n");
		sb.append("			<xdat:schema_field>").append(schemaField)
			.append("</xdat:schema_field>\n");
		sb.append("			<xdat:comparison_type>").append(comparator)
			.append("</xdat:comparison_type>\n");
		sb.append("			<xdat:value>").append(value).append("</xdat:value>\n");
		sb.append("		</xdat:criteria>\n");
		return sb;
	}

	private StringBuilder addCriterion(StringBuilder sb, String schemaField,
		String comparator, int value)
	{
		sb.append("		<xdat:criteria override_value_formatting=\"0\">\n");
		sb.append("			<xdat:schema_field>").append(schemaField)
			.append("</xdat:schema_field>\n");
		sb.append("			<xdat:comparison_type>").append(comparator)
			.append("</xdat:comparison_type>\n");
		sb.append("			<xdat:value>").append(value).append("</xdat:value>\n");
		sb.append("		</xdat:criteria>\n");
		return sb;
	}

	private boolean checkCache(String path, XnatResultSet rs)
	{
		int nameIdx = rs.getColumnIndex("Name");
		int sizeIdx = rs.getColumnIndex("Size");
		int nFiles = rs.getRowCount();
		if ((nameIdx < 0) || (sizeIdx < 0) || (nFiles == 0))
		{
			logger.warn("Malformed XnatResultSet for files");
			return false;
		}
		for (int i=0; i<nFiles; i++)
		{
			String name = rs.get(i, nameIdx);
			String sizeStr = rs.get(i, sizeIdx);
			long size = -1;
			try
			{
				size = Long.parseLong(sizeStr);
			}
			catch (NumberFormatException exIgnore)
			{}
			File file = new File(path+name);
			if (!(file.isFile() && file.canRead() && (file.length() == size)))
			{
				// Fail immediately if any file missing
				logger.info("File not found in cache: {}", name);
				return false;
			}
		}
		return true;
	}

	private StringBuilder createInitialXml(String desc)
	{
		StringBuilder sb = new StringBuilder(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<xdat:search ID=\"\"")
			.append(" allow-diff-columns=\"0\"")
			.append(" secure=\"false\"")
			.append(" brief-description=\"").append(desc).append("\"")
			.append(" xmlns:xdat=\"http://nrg.wustl.edu/security\"")
			.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
		return sb;
	}

	private String createSeriesSearchXml(String uid, String modality)
	{
		StringBuilder sb = createInitialXml("Series By UID");

		String sessionData = sessionData(modality);
		String scanData = scanData(modality);
		addRootElement(sb, sessionData);
		addSearchField(sb, sessionData, "PROJECT", 0, StringType,
			ProjectIdHeader);
		addSearchField(sb, sessionData, "subject_ID", 1, StringType,
			SubjectIdHeader);
		addSearchField(sb, sessionData, "ID", 2, StringType, SessionIdHeader);
		addSearchField(sb, sessionData, "LABEL", 2, StringType, SessionLabelHeader);
		addSearchField(sb, scanData, "ID", 3, StringType, ScanIdHeader);
		addSearchField(sb, scanData, "TYPE", 3, StringType, ScanTypeHeader);
		sb.append("	<xdat:search_where >\n");
		addCriterion(sb, scanData+"/UID", Equals, uid);
		sb.append("	</xdat:search_where>\n");

		sb.append("</xdat:search>\n");

		return sb.toString();
	}

	private String createStudySearchXml(String uid, String modality)
	{
		StringBuilder sb = createInitialXml("Study By UID");

		String sessionData = sessionData(modality);
		String scanData = scanData(modality);
		addRootElement(sb, sessionData);
		addSearchField(sb, sessionData, "PROJECT", 0, StringType,
			ProjectIdHeader);
		addSearchField(sb, sessionData, "subject_ID", 1, StringType,
			SubjectIdHeader);
		addSearchField(sb, sessionData, "ID", 2, StringType, SessionIdHeader);
		addSearchField(sb, sessionData, "LABEL", 2, StringType, SessionLabelHeader);
		addSearchField(sb, scanData, "ID", 3, StringType, ScanIdHeader);
		addSearchField(sb, scanData, "TYPE", 3, StringType, ScanTypeHeader);
		sb.append("	<xdat:search_where >\n");
		addCriterion(sb, sessionData+"/UID", Equals, uid);
		sb.append("	</xdat:search_where>\n");

		sb.append("</xdat:search>\n");

		return sb.toString();
	}

	private void extract(ZipInputStream zis, File entryFile) throws IOException
	{
		BufferedOutputStream bos = null;
		try
		{
			entryFile.getParentFile().mkdirs();
			bos = new BufferedOutputStream(new FileOutputStream(entryFile));
			byte[] buffer = new byte[4096];
			int nRead;
			while ((nRead = zis.read(buffer)) != -1)
			{
				bos.write(buffer, 0, nRead);
			}
		}
		finally
		{
			IoUtils.safeClose(bos);
		}
	}

	private String scanData(String modality)
	{
		return new StringBuilder("xnat:").append(modality).append("ScanData")
			.toString();
	}

	private String sessionData(String modality)
	{
		return new StringBuilder("xnat:").append(modality).append("SessionData")
			.toString();
	}

	private Series parseSeriesCache(String cachePath, String uid)
		throws IOException
	{
		DicomReceiver rx = new DicomReceiver();
		PathScan<DicomObject> scanner = dcmToolkit.createPathScan();
		scanner.addContext(rx);
		scanner.scan(cachePath, true);
		// Return first instance of the desired study, should only be one but
		// not checking
		List<Patient> patients = rx.getPatientRoot().getPatientList();
		for (Patient patient : patients)
		{
			List<Study> studies = patient.getStudyList();
			for (Study study : studies)
			{
				Series series = study.getSeries(uid);
				if (series != null)
				{
					return series;
				}
			}
		}
		logger.warn("Load from cache failed. Possible corrupt cache.");
		return null;
	}

	private void unzip(InputStream is, String targetPath,
		Map<String,String>filePathMap) throws IOException
	{
		ZipInputStream zis = null;
		try
		{
			File cache = new File(targetPath);
			cache.mkdir();
			zis = new ZipInputStream(is);
			ZipEntry entry = zis.getNextEntry();
			while (entry != null)
			{
				String entryPath = targetPath+(new File(entry.getName()).getName());
				System.out.println(entryPath);
				if (!entry.isDirectory())
				{
					File entryFile = new File(entryPath);
					extract(zis, entryFile);
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
		}
		finally
		{
			IoUtils.safeClose(zis);
		}
	}

	private Map<String, String> createFilePathMap(XnatResultSet searchRs,
		XnatResultSet fileRs)
	{
		Map<String,String> map = new HashMap<>();
		String projectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
			ProjectIdHeader));
		String subjectId = searchRs.get(0, searchRs.getColumnIndexByHeader(
			SubjectIdHeader));
		String sessionId = searchRs.get(0, searchRs.getColumnIndexByHeader(
			SessionIdHeader));
		String scanId = searchRs.get(0, searchRs.getColumnIndexByHeader(
			ScanIdHeader));
		
		return map;
	}

}
