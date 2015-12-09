/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.PathScanContext;
import etherj.aim.ImageAnnotationCollection.FileUidPair;
import etherj.db.DatabaseException;
import etherj.db.SqliteDatabase;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class SqliteAimDatabase extends SqliteDatabase implements AimDatabase
{
	private static final Logger logger =
		LoggerFactory.getLogger(SqliteAimDatabase.class);
	// ImageAnnotationCollection
	private static final String IAC = "iac";
	private static final String IAC_DATETIME = "dateTime";
	private static final String IAC_PATH = "path";
	private static final String IAC_PK = "pk";
	private static final String IAC_VERSION = "version";
	private static final String IAC_UID = "uid";
	// ImageAnnotation
	private static final String IA = "ia";
	private static final String IA_COMMENT = "comment";
	private static final String IA_DATETIME = "dateTime";
	private static final String IA_IACFK = "iacFk";
	private static final String IA_NAME = "name";
	private static final String IA_PK = "pk";
	private static final String IA_UID = "uid";
	// Markup
	private static final String MARKUP = "markup";
	private static final String MARKUP_CLASS = "class";
	private static final String MARKUP_DIMENSIONS = "dims";
	private static final String MARKUP_IAFK = "iaFk";
	private static final String MARKUP_IMAGE_REF_UID = "imRefUid";
	private static final String MARKUP_INCLUDE = "incl";
	private static final String MARKUP_PK = "pk";
	private static final String MARKUP_REF_FRAME_NUMBER = "refFrame";
	private static final String MARKUP_SHAPE_ID = "shapeId";
	private static final String MARKUP_UID = "uid";
	// Reference
	private static final String REFERENCE = "reference";
	private static final String REFERENCE_CLASS = "class";
	private static final String REFERENCE_IAFK = "iaFk";
	private static final String REFERENCE_MODALITY_CODE = "code";
	private static final String REFERENCE_PK = "pk";
	private static final String REFERENCE_SERIES_UID = "seriesUid";
	private static final String REFERENCE_STUDY_START_DATE = "startDate";
	private static final String REFERENCE_STUDY_START_TIME = "startTime";
	private static final String REFERENCE_STUDY_UID = "studyUid";
	private static final String REFERENCE_UID = "uid";
	// Image
	private static final String IMAGE = "image";
	private static final String IMAGE_PK = "pk";
	private static final String IMAGE_REFERENCEFK = "refFk";
	private static final String IMAGE_INSTANCE_UID = "instUid";
	private static final String IMAGE_SOP_CLASS_UID = "sopClassUid";
	// Statements
	private PreparedStatement selectDicomUidStmt;
	private PreparedStatement selectIacPkStmt;
	private PreparedStatement selectIaPkStmt;
	private PreparedStatement selectImagePkStmt;
	private PreparedStatement selectMarkupPkStmt;
	private PreparedStatement selectRefPkStmt;
	//
	private int bufferMax = 256;
	private final List<ImageAnnotationCollection> iacBuffer = new ArrayList<>();
	private boolean isScanning = false;

	public SqliteAimDatabase(String filename) throws DatabaseException
	{
		this(new File(filename));
	}
	
	public SqliteAimDatabase(File file) throws DatabaseException
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
		safeClose(selectDicomUidStmt);
		safeClose(selectIacPkStmt);
		safeClose(selectIaPkStmt);
		safeClose(selectImagePkStmt);
		safeClose(selectMarkupPkStmt);
		safeClose(selectRefPkStmt);
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

	@Override
	public void importDirectory(String path) throws IOException
	{
		importDirectory(path, true);
	}

	@Override
	public void importDirectory(String path, boolean recurse) throws IOException
	{
		File targetDir = new File(path);
		AimPathScan pathScan = new AimPathScan();
		pathScan.addContext(new AimReceiver());
		pathScan.scan(targetDir.getAbsolutePath(), recurse);
	}

	@Override
	public List<FileUidPair> searchDicomUid(String uid) throws DatabaseException
	{
		List<FileUidPair> list = new ArrayList<>();
		ResultSet rs = null;
		try
		{
			selectDicomUidStmt.setString(1, uid);
			selectDicomUidStmt.setString(2, uid);
			rs = selectDicomUidStmt.executeQuery();
			while(rs.next())
			{
				String path = rs.getString(1);
				String iacUid = rs.getString(2);
				FileUidPair pair = new FileUidPair(path, iacUid);
				list.add(pair);
			}
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught:", ex);
			throw new DatabaseException(ex);
		}
		finally
		{
			safeClose(rs);
		}
		return list;
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
	public void storeCollection(ImageAnnotationCollection iac) throws DatabaseException
	{
		iacBuffer.add(iac);
		if (!isScanning || iacBuffer.size() >= bufferMax)
		{
			processBuffer();
		}
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

	private void initIndices()
	{
		// IAC
		addIndexSpec(IAC, IAC_PATH);

		// IA
		addIndexSpec(IA, IA_IACFK);

		// Markup
		addIndexSpec(MARKUP, MARKUP_IAFK);

		// Reference
		addIndexSpec(REFERENCE, REFERENCE_IAFK);
		addIndexSpec(REFERENCE, REFERENCE_STUDY_UID);
		addIndexSpec(REFERENCE, REFERENCE_SERIES_UID);

		// Image
		addIndexSpec(IMAGE, IMAGE_REFERENCEFK);
		addIndexSpec(IMAGE, IMAGE_INSTANCE_UID);
	}

	private void initTables()
	{
		String iacSql = "CREATE TABLE \""+IAC+"\" ("+
			"\""+IAC_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+IAC_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+IAC_DATETIME+"\" TEXT NOT NULL,"+
			"\""+IAC_PATH+"\" TEXT NOT NULL,"+
			"\""+IAC_VERSION+"\" TEXT NOT NULL"+
			")";
		addTableSpec(IAC, iacSql);

		String iaSql = "CREATE TABLE \""+IA+"\" ("+
			"\""+IA_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+IA_IACFK+"\" INTEGER NOT NULL,"+
			"\""+IA_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+IA_COMMENT+"\" TEXT NOT NULL,"+
			"\""+IA_DATETIME+"\" TEXT NOT NULL,"+
			"\""+IA_NAME+"\" TEXT NOT NULL,"+
			"FOREIGN KEY ("+IA_IACFK+") REFERENCES "+IAC+"("+IAC_PK+")"+
			")";
		addTableSpec(IA, iaSql);

		String markupSql = "CREATE TABLE \""+MARKUP+"\" ("+
			"\""+MARKUP_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+MARKUP_IAFK+"\" INTEGER NOT NULL,"+
			"\""+MARKUP_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+MARKUP_CLASS+"\" INTEGER NOT NULL,"+
			"\""+MARKUP_DIMENSIONS+"\" INTEGER NOT NULL,"+
			"\""+MARKUP_INCLUDE+"\" INTEGER NOT NULL,"+
			"\""+MARKUP_SHAPE_ID+"\" INTEGER NOT NULL,"+
			"\""+MARKUP_IMAGE_REF_UID+"\" TEXT NOT NULL,"+
			"\""+MARKUP_REF_FRAME_NUMBER+"\" INTEGER NOT NULL,"+
			"FOREIGN KEY ("+MARKUP_IAFK+") REFERENCES "+IA+"("+IA_PK+")"+
			")";
		addTableSpec(MARKUP, markupSql);

		String refSql = "CREATE TABLE \""+REFERENCE+"\" ("+
			"\""+REFERENCE_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+REFERENCE_IAFK+"\" INTEGER NOT NULL,"+
			"\""+REFERENCE_UID+"\" TEXT UNIQUE NOT NULL,"+
			"\""+REFERENCE_CLASS+"\" INTEGER NOT NULL,"+
			"\""+REFERENCE_MODALITY_CODE+"\" TEXT NOT NULL,"+
			"\""+REFERENCE_STUDY_START_DATE+"\" TEXT NOT NULL,"+
			"\""+REFERENCE_STUDY_START_TIME+"\" TEXT NOT NULL,"+
			"\""+REFERENCE_STUDY_UID+"\" TEXT NOT NULL,"+
			"\""+REFERENCE_SERIES_UID+"\" TEXT NOT NULL,"+
			"FOREIGN KEY ("+REFERENCE_IAFK+") REFERENCES "+IA+"("+IA_PK+")"+
			")";
		addTableSpec(REFERENCE, refSql);

		String imageSql = "CREATE TABLE \""+IMAGE+"\" ("+
			"\""+IMAGE_PK+"\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
			"\""+IMAGE_REFERENCEFK+"\" INTEGER NOT NULL,"+
			"\""+IMAGE_INSTANCE_UID+"\" TEXT NOT NULL,"+
			"\""+IMAGE_SOP_CLASS_UID+"\" TEXT NOT NULL,"+
			"FOREIGN KEY ("+IMAGE_REFERENCEFK+") REFERENCES "+REFERENCE+"("+REFERENCE_PK+")"+
			")";
		addTableSpec(IMAGE, imageSql);
	}

	private int insertIa(ImageAnnotation ia, int iacPk) throws SQLException
	{
		String uid = ia.getUid();
		String sql = "INSERT INTO "+IA+"("+IA_IACFK+","+IA_UID+","+IA_COMMENT+","+
			IA_DATETIME+","+IA_NAME+") "+"VALUES (?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int iaPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, iacPk);
			stmt.setString(2, uid);
			stmt.setString(3, ia.getComment());
			stmt.setString(4, ia.getDateTime());
			stmt.setString(5, ia.getName());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			iaPk = rs.getInt(1);
			logger.debug("IA key {} inserted with PK {}", uid, iaPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return iaPk;
	}

	private int insertIac(ImageAnnotationCollection iac) throws SQLException
	{
		String uid = iac.getUid();
		String sql = "INSERT INTO "+IAC+"("+IAC_UID+","+IAC_DATETIME+","+
			IAC_PATH+","+IAC_VERSION+") "+"VALUES (?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int iacPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setString(1, uid);
			stmt.setString(2, iac.getDateTime());
			stmt.setString(3, iac.getPath());
			stmt.setString(4, iac.getAimVersion());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			iacPk = rs.getInt(1);
			logger.debug("IAC key {} inserted with PK {}", uid, iacPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return iacPk;
	}

	private int insertImage(Image image, int refPk) throws SQLException
	{
		String uid = image.getInstanceUid();
		String sql = "INSERT INTO "+IMAGE+"("+IMAGE_REFERENCEFK+","+
			IMAGE_INSTANCE_UID+","+IMAGE_SOP_CLASS_UID+") "+"VALUES (?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int imagePk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, refPk);
			stmt.setString(2, image.getInstanceUid());
			stmt.setString(3, image.getSopClassUid());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			imagePk = rs.getInt(1);
			logger.debug("Image key {} inserted with PK {}", uid, imagePk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return imagePk;
	}

	private int insertMarkup(Markup markup, int iaPk) throws SQLException
	{
		String uid = markup.getUid();
		String markupClass = markup.getClass().getName();
		String sql = "INSERT INTO "+MARKUP+"("+MARKUP_IAFK+","+MARKUP_UID+","+
			MARKUP_CLASS+","+MARKUP_DIMENSIONS+","+MARKUP_INCLUDE+","+
			MARKUP_SHAPE_ID+","+MARKUP_IMAGE_REF_UID+","+MARKUP_REF_FRAME_NUMBER+
			") VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int markupPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, iaPk);
			stmt.setString(2, uid);
			stmt.setInt(3, Markup.getClassCode(markupClass));
			stmt.setInt(4, markup.getDimensionCount());
			switch (markupClass)
			{
				case Markup.TwoDimensionCircle:
				case Markup.TwoDimensionEllipse:
				case Markup.TwoDimensionMultiPoint:
				case Markup.TwoDimensionPoint:
				case Markup.TwoDimensionPolyline:
					TwoDimensionGeometricShape shape2D =
						(TwoDimensionGeometricShape) markup;
					stmt.setBoolean(5, shape2D.getIncludeFlag());
					stmt.setInt(6, shape2D.getShapeId());
					stmt.setString(7, shape2D.getImageReferenceUid());
					stmt.setInt(8, shape2D.getReferencedFrameNumber());
					break;

				default:
					stmt.setBoolean(5, false);
					stmt.setInt(6, -1);
					stmt.setString(7, "");
					stmt.setInt(8, -1);
					break;
			}
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			markupPk = rs.getInt(1);
			logger.debug("Markup key {} inserted with PK {}", uid, markupPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return markupPk;
	}

	private int insertReference(ImageReference ref, int iaPk) throws SQLException
	{
		String uid = ref.getUid();
		String refClass = ref.getClass().getName();
		String sql = "INSERT INTO "+REFERENCE+"("+REFERENCE_IAFK+","+REFERENCE_UID+","+
			REFERENCE_CLASS+","+REFERENCE_MODALITY_CODE+","+REFERENCE_SERIES_UID+","+
			REFERENCE_STUDY_START_DATE+","+REFERENCE_STUDY_START_TIME+","+
			REFERENCE_STUDY_UID+
			") VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int refPk = 0;
		try
		{
			stmt = prepareStatement(sql);
			stmt.setInt(1, iaPk);
			stmt.setString(2, uid);
			stmt.setInt(3, ImageReference.getClassCode(refClass));
			switch (refClass)
			{
				case ImageReference.DicomImageReference:
					DicomImageReference dcmRef = (DicomImageReference) ref;
					ImageStudy study = dcmRef.getStudy();
					ImageSeries series = study.getSeries();
					stmt.setString(4, series.getModality().getCode());
					stmt.setString(5, series.getInstanceUid());
					stmt.setString(6, study.getStartDate());
					stmt.setString(7, study.getStartTime());
					stmt.setString(8, study.getInstanceUid());
					break;

				default:
					stmt.setString(4, "");
					stmt.setString(5, "");
					stmt.setString(6, "");
					stmt.setString(7, "");
					stmt.setString(8, "");
					break;
			}
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			refPk = rs.getInt(1);
			logger.debug("ImageReference key {} inserted with PK {}", uid, refPk);
		}
		finally
		{
			safeClose(rs);
			safeClose(stmt);
		}
		return refPk;
	}

	private void prepareStatements() throws DatabaseException
	{
		try
		{
			String sql;

			// Primary key searches
			sql = "SELECT "+IAC_PK+" FROM "+IAC+" WHERE "+IAC_UID+"=(?)";
			selectIacPkStmt = prepareStatement(sql);
			sql = "SELECT "+IA_PK+" FROM "+IA+" WHERE "+IA_UID+"=(?)";
			selectIaPkStmt = prepareStatement(sql);
			sql = "SELECT "+MARKUP_PK+" FROM "+MARKUP+" WHERE "+MARKUP_UID+"=(?)";
			selectMarkupPkStmt = prepareStatement(sql);
			sql = "SELECT "+REFERENCE_PK+" FROM "+REFERENCE+" WHERE "+REFERENCE_UID+"=(?)";
			selectRefPkStmt = prepareStatement(sql);
			sql = "SELECT "+IMAGE_PK+" FROM "+IMAGE+" WHERE "+IMAGE_INSTANCE_UID+"=(?)";
			selectImagePkStmt = prepareStatement(sql);

			// DICOM UID search
			sql = "SELECT DISTINCT "+IAC+"."+IAC_PATH+","+IAC+"."+IAC_UID+
				" FROM "+REFERENCE+" AS r,"+IA+","+IAC+
				" WHERE "+
				"(r."+REFERENCE_STUDY_UID+"=(?) OR r."+REFERENCE_SERIES_UID+"=(?))"+
				" AND "+IAC+"."+IAC_PK+"="+IA+"."+IA_IACFK+
				" AND "+IA+"."+IA_PK+"=r."+REFERENCE_IAFK;
			selectDicomUidStmt = prepareStatement(sql);
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
	}

	private void processBuffer() throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = createStatement();
			stmt.executeUpdate("BEGIN TRANSACTION");
			for (ImageAnnotationCollection iac : iacBuffer)
			{
				int iacPk = getPk(selectIacPkStmt, iac.getUid());
				if (iacPk == 0)
				{
					iacPk = insertIac(iac);
				}
				List<ImageAnnotation> iaList = iac.getAnnotationList();
				for (ImageAnnotation ia : iaList)
				{
					int iaPk = getPk(selectIaPkStmt, ia.getUid());
					if (iaPk == 0)
					{
						iaPk = insertIa(ia, iacPk);
					}
					List<Markup> markupList = ia.getMarkupList();
					for (Markup markup : markupList)
					{
						int markupPk = getPk(selectMarkupPkStmt, markup.getUid());
						if (markupPk == 0)
						{
							insertMarkup(markup, iaPk);
						}
					}
					List<ImageReference> refList = ia.getReferenceList();
					for (ImageReference ref : refList)
					{
						int refPk = getPk(selectRefPkStmt, ref.getUid());
						if (refPk == 0)
						{
							refPk = insertReference(ref, iaPk);
						}
						switch (ref.getClass().getName())
						{
							case ImageReference.DicomImageReference:
								DicomImageReference dcmRef = (DicomImageReference) ref;
								ImageSeries series = dcmRef.getStudy().getSeries();
								List<Image> imageList = series.getImageList();
								for (Image image : imageList)
								{
									int imagePk = getPk(selectImagePkStmt,
										image.getInstanceUid());
									if (imagePk == 0)
									{
										insertImage(image, refPk);
									}
								}
								break;

							default:
						}
					}
				}
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
			iacBuffer.clear();
		}
		
	}

	private class AimReceiver implements PathScanContext<ImageAnnotationCollection>
	{
		@Override
		public void notifyItemFound(File file, ImageAnnotationCollection item)
		{
			try
			{
				storeCollection(item);
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
			iacBuffer.clear();
			isScanning = true;
		}
	}
}
