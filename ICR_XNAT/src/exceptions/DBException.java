/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: DBException.java
* First created on August 14, 2007, 2:11 PM
* 
* Exception that allows the additional specification of a return code
* and explicitly enumerates the possible reasons for exceptions to be
* generated.
*********************************************************************/

package exceptions;

import java.util.HashMap;
import java.util.Map;

public class DBException extends CodedException
{
	public static final int DRIVER_NOT_FOUND			 = 0;
	public static final int OPEN_ERROR					 = 1;
	public static final int UNABLE_TO_CREATE			 = 2;
	public static final int USER_DECLINED_TO_CREATE	 = 3;
	public static final int SQL_EXECUTION_ERROR		 = 4;
	public static final int ERROR_READING_RESULTSET	 = 5;
	public static final int INVALID_NAME_OR_PASSWORD = 6;
	public static final int NO_DATABASES_AVAILABLE   = 7;
	public static final int MAX_ALLOWED_PACKET		 = 8;

	private static final HashMap<Integer, String> messages;
	
	static
	{
		messages = new HashMap();
	   messages.put(DRIVER_NOT_FOUND,         "The JDBC database driver class was not found.");
		messages.put(OPEN_ERROR,               "It was not possible to connect to the database server.");
		messages.put(UNABLE_TO_CREATE,         "It was not possible to create the database." );
		messages.put(USER_DECLINED_TO_CREATE,  "The user declined to create the database." );
		messages.put(SQL_EXECUTION_ERROR,      "Execution of an SQL statement led to an exception." );
		messages.put(ERROR_READING_RESULTSET,  "There was a problem reading back the ResultSet from the SQL database.");
		messages.put(INVALID_NAME_OR_PASSWORD, "Unable to log in to the database. Please check your username and password.");
		messages.put(NO_DATABASES_AVAILABLE,   "You do not have authorisation to view any databases on the server.");
		messages.put(MAX_ALLOWED_PACKET,       "The 'SET max_allowed_packet' statement failed.");
	}


	public DBException(int diagnosticCode)
	{
		this(diagnosticCode, null);
	}

	
   public DBException(int diagnosticCode, String upstreamMessage)
   {
      super(diagnosticCode, upstreamMessage);
   }

	
   @Override
	public Map getMessagesForAllCodes()
	{
		return messages;
	}

	
	@Override
	public String getMessageForCode()
	{
		if (!messages.containsKey(code)) return CODE_NOT_FOUND;
		return messages.get(code);
	}
}
