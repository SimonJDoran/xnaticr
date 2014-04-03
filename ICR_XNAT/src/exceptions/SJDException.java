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
* Java class: SJDException.java
* First created on August 14, 2007, 2:46 PM
* 
* Exception that allows the additional specification of a return code
* and logging if required.
*********************************************************************/

package exceptions;

import org.apache.log4j.Logger;

public abstract class SJDException extends Exception
{	
	protected static int status;
	protected static String upstreamErrorMessage;
   static Logger logger = Logger.getLogger(SJDException.class);
	
	/**
	 * Creates a new instance of ExceptionSJD. The use of a constructor
	 * using an error code, rather than the standard string means that the
	 * actual message displayed can be changed centrally at will, whilst
	 * the code throwing the error flags the *condition* rather than the
	 * message. 
	 */
	public SJDException(int errorCode)
	{
		this(errorCode, "");
	}
	
	
	public SJDException(int errorCode, String upstream)
	{
		super("<SJD ERROR>");
		status = errorCode;
		upstreamErrorMessage = upstream;
      logger.debug(getMessage());
	}
	
	
	/** Get the error code - potentially useful for taking error dependent actions */
	public int getStatus()
	{
		return status;
	}
		
	
	/** Get a list of all the possible errors that the object can report.
	 *  This is an abstract function provided by the subclasses. */
	public abstract String[] getMessageList();
	
	
	
	/** Return the error message. */
	@Override
	public String getMessage()
	{
	/*  Note that the error message is normally set by the call to super.
	 *  Unfortunately, in this case, the abstract function getMessageList() is
	 *  not defined until after super is called. Hence, a generic message is
	 *  given to super and the getMessage() function is modified to access the
	 *  true error message.
	 */
		return getMessageList()[status] + "\n" + upstreamErrorMessage;
	}
	
	
	
	
	/** Get a detailed error message including the source of the error. */
	public String getDetailedMessage()
	{
		String message = getMessageList()[status] + upstreamErrorMessage;
		
		StackTraceElement[] st = getStackTrace();
		return "<SJD ERROR>\n" +
			"Method: " + st[0].getMethodName() + " in class " + st[0].getClassName() +
			"\n(Source: line " + st[0].getLineNumber() +
			" in file" + st[0].getFileName() + ")\n" + message;		
	}
	
}
