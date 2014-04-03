/*****************************************************************************
 *
 * SQLUpdate.java
 *
 * Simon J Doran
 *
 * First created on September 18, 2007, 12:30 PM
 *
 *****************************************************************************/

package obselete;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** SQLUpdate is a simplified wrapper round the JDBC API. In particular,
 *  it removes the need to clutter the code with try-catch-finally blocks.
 */
public class SQLUpdate
{
	public static final int SQL_SUCCESS				= 0;
	public static final int SQL_EXECUTION_ERROR	= 1;
	public static final int SQL_STATEMENT_CLOSE	= 2;
	
	private static final String[] messages =
	{
		"Successful execution of SQL statement",
		"Execution of an SQL statement led to an exception.",
		"There was an error closing the SQL Statement."
	};
	
	int			status		= SQL_SUCCESS;
	int			updateCount = 0;
	Statement	stmt			= null;
	String		message		= messages[status];
	String		updateString;

	
	
	/** Creates a new instance of SQLUpdate */
	public SQLUpdate(Connection con, String updateString)
	{
		this.updateString = updateString;
		try
		{
			stmt					= con.createStatement();
			updateCount	= stmt.executeUpdate(updateString);
		}
		catch (SQLException ex)
		{
			status  = SQL_EXECUTION_ERROR;
			message = messages[status] + " " + ex.getMessage(); 			
		}
	}

	
	
		/** Returns the error message for use by the caller */
	public String getMessage()
	{
		return message;
	}


	
	/** Returns the original query string that the object was created with */
	public int getUpdateCount()
	{
		return updateCount;
	}
	
	
	
	
	/** Returns the original query string that the object was created with */
	public String getUpdateString()
	{
		return updateString;
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
