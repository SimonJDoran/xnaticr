/*****************************************************************************
 *
 * SQLQuery.java
 *
 * Simon J Doran
 *
 * First created on August 16, 2007, 10:28 AM
 *
 *****************************************************************************/

package obselete;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** SQLQuery is a simplified wrapper round the JDBC API. In particular,
 *  it removes the need to clutter the code with try-catch-finally blocks.
 */
public class SQLQuery
{
	public static final int SQL_SUCCESS				= 0;
	public static final int SQL_EXECUTION_ERROR	= 1;
	public static final int SQL_STATEMENT_CLOSE	= 2;
	public static final int SQL_RESULT_SET_READ  = 3;
	
	public static final String STRING_SEP = "\\t";

	private static final String[] messages =
	{
		"Successful execution of SQL statement",
		"Execution of an SQL statement led to an exception.",
		"There was an error closing the SQL Statement.",
		"There was an error reading the SQL ResultSet object."
	};
	

	int			status	= SQL_SUCCESS;
	ResultSet	rs			= null;
	Statement	stmt		= null;
	String		message	= messages[status];
	String		queryString;
	
	
	
	/** Creates a new instance of SQLQuery */
	public SQLQuery(Connection con, String queryString)
	{
		this.queryString = queryString;
		
		try
		{
			stmt	= con.createStatement();
			rs		= stmt.executeQuery(queryString);
		}
		catch (SQLException ex)
		{
			status  = SQL_EXECUTION_ERROR;
			message = messages[status] + " " + ex.getMessage(); 			
		}
	}
	
	
	/** Closes the Statement associated with this query and frees any resources.
	    Note that the Statement.Close method can generate an exception, but that
	    the error should not cause problems. If the calling routine needs to know
	    whether the call was successful, the current status may be returned via
	    the getStatus method.
	 */
	public void closeStatement()
	{
		try
		{
			stmt.close();
		}
		catch (SQLException ex)
		{
			status	= SQL_STATEMENT_CLOSE;
			message	= messages[status]; 
		}
	}


	
	/** Returns a Blob from the results of the SQL query.
	 */
	public Blob getResultBlob()
	{
		Blob b = null;
		
		try
		{
			if (!rs.next()) return b;
			b = rs.getBlob(1);
		}
		catch  (SQLException ex)
		{
			status = SQL_RESULT_SET_READ;
		}
		
		return b;
	}
	
	
	
	/** Returns an array of Blob objects from the results of the SQL query.
	 */
	public Blob[] getResultBlobArray()
	{
		Blob[]	bArr	= null;
		int		nRows	= 0;
		
		// The way of getting the number of rows in the ResultSet is rather cumbersome
		try
		{
			rs.last();
			nRows = rs.getRow();
			rs.beforeFirst();
		}
		catch (SQLException ex)
		{
			status = SQL_RESULT_SET_READ;
			return bArr;
		}
		
		if (nRows == 0) return bArr;  // null
		
		bArr = new Blob[nRows];
		try
		{
			int i	= 0;
			while (rs.next())
			{
				// SQL columns start at 1 not zero.
				bArr[i] = rs.getBlob(1);
				i++;
			}
		}
		catch (SQLException ex)
		{
			status = SQL_RESULT_SET_READ;
		}		
		
		return bArr;
	}
	
	
	
	/** Returns the error message for use by the caller */
	public String getMessage()
	{
		return message;
	}
	
	
	
	/** Returns the original query string that the object was created with */
	public String getQueryString()
	{
		return queryString;
	}

	
	
	/** Returns the results of the SQL query */
	public ResultSet getResultSet()
	{
		return rs;
	}
	
	
	
	/** Return a table of results produced the SQL query as a 2-D string array. */
	public String[][] getResultStringArray2D()
	{
		String[][]	result2D	= null;
		Object		tempObj	= null;
		int			nCols		= 0;
		int			nRows		= 0;
		
		try
		{
			nCols = rs.getMetaData().getColumnCount();

			// The way of getting the number of rows in the ResultSet is rather cumbersome
			rs.last();
			nRows = rs.getRow();
			rs.beforeFirst();
		}
		catch (SQLException ex)
		{
			status = SQL_RESULT_SET_READ;
			return result2D;
		}

		if (nRows == 0) return result2D;  // null
		
		result2D = new String[nRows][nCols];
		
		try
		{
			int j	= 0;
			while (rs.next())
			{
				for (int i = 1; i<=nCols; i++)
				{
					// SQL columns start at 1 not zero.
					tempObj = rs.getObject(i);
					if (tempObj == null) result2D[j][i-1] = "<NULL>";
					else result2D[j][i-1] = tempObj.toString(); 
				}
				j++;
			}
		}
		catch (SQLException ex)
		{
			status = SQL_RESULT_SET_READ;
		}
		
		return result2D;
	}
	

	
	
	/** Return a table of results produced by the SQL query as a 1-D string array,
	 *  with multiple columns separated by the relevant string separator character.
	 */
	public String[] getResultStringArray()
	{
		String[]		result;
		String[][]	result2D = getResultStringArray2D();
		
		if (result2D == null) result = null;
		else
		{
			int nRows = result2D.length;
			int nCols = result2D[0].length;
			
			result = new String[nRows];
			
			for (int j=0; j<nRows; j++)
			{
				result[j] = "";
				for (int i=0; i<nCols-1; i++)
				{
					result[j] = result[j] + result2D[j][i] + STRING_SEP;
				}
				result[j] = result2D[j][nCols-1];
			}
		}
		
		return result;
	}
	
	

	/** Returns the error condition for the caller to react to if necessary */
	public int getStatus()
	{
		return status;
	}
	

	
	
	/** Returns true if the query runs without error */
	public boolean success()
	{
		return (status == SQL_SUCCESS);
	}
	
	
	
	/** Returns true if the query leads to an SQL error */
	public boolean fail()
	{
		return (status != SQL_SUCCESS);
	}
		
	
}
