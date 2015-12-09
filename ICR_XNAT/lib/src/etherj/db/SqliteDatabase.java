/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.db;

import etherj.Ether;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class SqliteDatabase
{
	private static final Logger logger =
		LoggerFactory.getLogger(SqliteDatabase.class);
	private final Connection connection;
	private final File file;
	private final Map<String,String> indexSqlMap;
	private final List<String> tableNames;
	private final Map<String,String> tableSqlMap;
	private final boolean requireFk;

	public SqliteDatabase(String filename) throws DatabaseException
	{
		this(new File(filename), true);
	}

	public SqliteDatabase(String filename, boolean requireFk) throws DatabaseException
	{
		this(new File(filename), requireFk);
	}

	public SqliteDatabase(File file) throws DatabaseException
	{
		this(file, true);
	}

	public SqliteDatabase(File file, boolean requireFk) throws DatabaseException
	{
		if (file.isAbsolute())
		{
			this.file = new File(file.getPath());
		}
		else
		{
			this.file = new File(Ether.getEtherDir()+file.getPath());
		}
		this.requireFk = requireFk;
		try
		{
			connection = DriverManager.getConnection(
				"jdbc:sqlite:"+this.file.getAbsolutePath());
			this.checkForeignKeys();
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		this.indexSqlMap = new TreeMap<>();
		this.tableNames = new ArrayList<>();
		this.tableSqlMap = new TreeMap<>();
	}

	/**
	 *
	 * @throws DatabaseException
	 */
	public void close() throws DatabaseException
	{
		try
		{
			if ((connection != null) && (!connection.isClosed()))
			{
				connection.close();
				logger.info("Database connection closed");
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		try
		{
			props.setProperty("db.filename", file.getAbsolutePath());
			if (connection == null)
			{
				return props;
			}
			DatabaseMetaData metadata = connection.getMetaData();
			props.setProperty("db.driver", metadata.getDriverName());
			props.setProperty("db.product.name", metadata.getDatabaseProductName());
			props.setProperty("db.product.version", metadata.getDatabaseProductVersion());
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught:", ex);
		}
	
		return props;
	}

	/**
	 *
	 * @return
	 */
	public boolean isValid()
	{
		if (connection == null)
		{
			return false;
		}
		try
		{
			return connection.isValid(0);
		}
		catch (SQLException ex)
		{
			logger.error("Exception caught: ", ex);
		}
		return false;
	}

	/**
	 *
	 * @param table 
	 * @param column 
	 */
	protected void addIndexSpec(String table, String column)
	{
		String indexName = column+"Idx";
		String sql = "CREATE INDEX \""+indexName+"\" ON \""+table+"\" (\""+column+"\")";
		indexSqlMap.put(indexName, sql);
	}

	/**
	 *
	 * @param name
	 * @param sql
	 * @return 
	 */
	protected boolean addTableSpec(String name, String sql)
	{
		if (tableNames.contains(name))
		{
			return false;
		}
		tableNames.add(name);
		tableSqlMap.put(name, sql);
		return true;
	}

	private void checkForeignKeys() throws DatabaseException
	{
		if (!this.requireFk)
		{
			return;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("PRAGMA foreign_keys=ON");
			rs = stmt.executeQuery("PRAGMA foreign_keys");
			String column = rs.getMetaData().getColumnName(1);
			if (!column.equals("foreign_keys") || !rs.getBoolean(1))
			{
				throw new DatabaseException("Required foreign key support not present");
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (SQLException exIgnore)
			{}
		}
	}

	private void checkIndices() throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			Iterator<String> it = indexSqlMap.keySet().iterator();
			while (it.hasNext())
			{
				String indexName = it.next();
				String sql = "SELECT sql FROM sqlite_master WHERE type='index'"+
					" AND name='"+indexName+"';";
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.isAfterLast())
				{
					logger.info("Missing index: "+indexName+". Recreating index");
					rs.close();
					dropAndRecreateIndex(indexName);
					continue;
				}
				String indexSql = rs.getString(1);
				String requiredSql = getIndexSql(indexName);
				if (!requiredSql.equals(indexSql))
				{
					logger.info("SQL mismatch for index: "+indexName+
						". Recreating index");
					rs.close();
					dropAndRecreateIndex(indexName);
				}
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (Exception exIgnore)
			{}
		}
	}

	protected void checkTables() throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			for (String tableName : tableNames)
			{
				String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='"+
					tableName+"'";
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.isAfterLast())
				{
					logger.info("Missing table: "+tableName+". Recreating database");
					rs.close();
					dropAndRecreateAllTables();
					break;
				}
				String requiredSql = getTableSql(tableName);
				String tableSql = rs.getString(1);
				if (!requiredSql.equals(tableSql))
				{
					logger.info("SQL mismatch for table: "+tableName+". Recreating database");
					rs.close();
					dropAndRecreateAllTables();
					break;
				}
				rs.close();
			}
			stmt.close();
			checkIndices();
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (Exception exIgnore)
			{}
		}
	}

	protected Statement createStatement() throws SQLException
	{
		return connection.createStatement();
	}

	protected PreparedStatement prepareStatement(String sql) throws SQLException
	{
		return connection.prepareStatement(sql);
	}

	protected void safeClose(ResultSet rs)
	{
		try
		{
			if (rs != null)
			{
				rs.close();
			}
		}
		catch (SQLException exIgnore)
		{}
	}

	protected void safeClose(Statement stmt)
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
		catch (SQLException exIgnore)
		{}
	}

	private void dropAndRecreateAllTables() throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			// Disable foreign keys to prevent foreign key constraint violations
			stmt.executeUpdate("PRAGMA foreign_keys=OFF");
			// Drop
			for (String tableName : tableNames)
			{
				stmt.executeUpdate("DROP TABLE IF EXISTS "+tableName+";");
			}
			// Re-enable foreign keys
			stmt.executeUpdate("PRAGMA foreign_keys=ON");
			// Creation
			for (String tableName : tableNames)
			{
				String sql = getTableSql(tableName);
				if (sql.isEmpty())
				{
					throw new DatabaseException("No SQL found for table: "+tableName);
				}
				stmt.executeUpdate(sql);
				logger.debug("Table creation SQL: "+sql);
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (Exception exIgnore)
			{}
		}
	}

	private void dropAndRecreateIndex(String indexName) throws DatabaseException
	{
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			// Drop
			stmt.executeUpdate("DROP INDEX IF EXISTS "+indexName+";");
			// Creation
			String sql = getIndexSql(indexName);
			if (sql.isEmpty())
			{
				throw new DatabaseException("No SQL found for index: "+indexName);
			}
			stmt.executeUpdate(sql);
			logger.debug("Table creation SQL: "+sql);
		}
		catch (SQLException ex)
		{
			throw new DatabaseException(ex);
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (Exception exIgnore)
			{}
		}
	}

	private String getIndexSql(String indexName)
	{
		return indexSqlMap.containsKey(indexName) ? indexSqlMap.get(indexName) : "";
	}

	private String getTableSql(String tableName)
	{
		return tableSqlMap.containsKey(tableName) ? tableSqlMap.get(tableName) : "";
	}

}
