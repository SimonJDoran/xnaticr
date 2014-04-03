/*****************************************************************************
 *
 * MySQLServerConnection.java
 *
 * Simon J Doran
 *
 * First created on September 13, 2007, 5:04 PM
 * 
 * N.B. This class is temporary only and is expected to be replaced by
 *      DBManager when the first major reorganisation of the code takes place.
 *
 *****************************************************************************/

package obselete;

import java.sql.Connection;
import exceptions.DBException;
import java.sql.DriverManager;
import java.sql.SQLException;
import exceptions.*;

public class DBServerConnection
{
	private static final String DB_UNIQUE_ID = "DUMMYTABLE_SJD_UNIQUE_IDENTIFIER";
	private Connection	serverConnection;
	private String			username;
	private String			password;
	private String			currentDB;
	
	/**
	 * Creates a new instance of DBServerConnection
	 */
	public DBServerConnection()
	{
		serverConnection = null;
	}

	
	
	public void login(String username, String password, String url, String driver)
		throws DBException
	{

      try
      {
			Class.forName(driver);
      }
      catch(java.lang.ClassNotFoundException e)
      {
			throw new DBException(DBException.DRIVER_NOT_FOUND);
      }
      
		
      try
      {
			serverConnection = (Connection) DriverManager.getConnection(url, username, password);	 
      }
      catch(SQLException e)
      {
			throw new DBException(DBException.OPEN_ERROR);
      }
		
		this.username = username;
		this.password = password;
		
		SQLUpdate u = new SQLUpdate(serverConnection, "SET max_allowed_packet = 16777216" );
		if (u.fail()) throw new DBException(DBException.MAX_ALLOWED_PACKET); 		
	}
		
	
	
	/** Obtain the database connection object. */
	public Connection getConnection()
	{
		return serverConnection;
	}
	
	
	/** Obtain the username associated with the current connection object. */
	public String getUsername()
	{
		return username;
	}

	
	/** Obtain the password associated with the current connection object. */
	public String getPassword()
	{
		return password;
	}
	

	
	/** Return the name of the currently selected database. */
	public String getCurrentDB()
	{
		/* The method below is what to use if the thing we genuinely
		 * want is the name of the database to which the connection is
		 * currently pointing. However, what is actually required here
		 * is the name of the database that the application is currently
		 * expecting to use. This might be different, for example if the
		 * user has just chosen a new database, but no SQL has yet been
		 * issued to change to this DB.
		SQLQuery q = new SQLQuery( serverConnection, "SELECT DATABASE()");
		if (q.fail())
		{
			q.closeStatement();
			return null;
		}
		return q.getResultStringArray()[0];
		*/
		return currentDB;  
	}
	
	
	/** Register the name of the currently selected database.
	 *  This is a temporary kludge to allow the name to arrive at
	 *  this object. When the object is replaced by a proper DBManager
	 *  object, then something more sophisticated will be used. */
	public void setCurrentDB(String DBName)
	{
		currentDB = DBName;
	}
	
	
	/** Return the list of databases on the server. The boolean argument
	    is true if we want to return all databases and false if we wish
	    to return only those that are compatible image databases.*/
	public String[] getDBList(Boolean returnAll)
	{
		/* Note: SQL errors arising in the process of listing the databases are not
		 * identified separately to the user. If these do occur, they are
		 * indistinguishable from the non-existence of a suitable database for
		 * simplicity in the code.
		 */
		SQLQuery q = new SQLQuery(serverConnection, "SHOW DATABASES");
		if (q.fail())
		{
			q.closeStatement();
			return null;
		}
		String[] dbl	= q.getResultStringArray();
		q.closeStatement();
		String[] temp	= new String[dbl.length];
		
		/* Databases of the correct type have a unique identifier in the form of
		 * a table of a specific name. So we need to check each database returned.
		 */
		if (returnAll) return dbl;
		
		int count = 0;
		for (int i=0; i<dbl.length; i++)
		{
			q = new SQLQuery(serverConnection, "USE " + dbl[i] + ";");
			boolean fail = q.fail();
			q.closeStatement();
			if (fail) return null;

			
			q = new SQLQuery(serverConnection, "SHOW TABLES;" );
			if (q.fail())
			{
				q.closeStatement();
				return null;
			}
			String[] tbl = q.getResultStringArray();
			q.closeStatement();
			
			boolean exists = false;
			if (tbl != null)
			{
				for (int j=0; j<tbl.length; j++)
				{
					if (tbl[j].toUpperCase().equals(DB_UNIQUE_ID))
					{
						exists = true;
						break;
					}
				}
			}
			if (exists) temp[count++] = dbl[i];
		}
		
		String[] DBList = null;
		if (count != 0)
		{
			DBList = new String[count];
			System.arraycopy(temp, 0, DBList, 0, count);
		}
		
		return DBList;					
	}


}
