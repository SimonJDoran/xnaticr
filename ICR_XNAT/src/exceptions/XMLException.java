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
* Java class: XMLException.java
* First created on May 18, 2009 at 3:34:51 PM
* 
* Exception that allows the additional specification of a return code
* and explicitly enumerates the possible reasons for exceptions to be
* generated.
*********************************************************************/

package exceptions;

import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;


public class XMLException extends CodedException
{
	public static final int PARSE  = 0;
   public static final int OUTPUT = 1;
	
	private static final HashMap<Integer, String> messages;
	static Logger logger = Logger.getLogger(XNATException.class);
	
	static
	{
		messages = new HashMap();
	   messages.put(PARSE,  "Problem parsing XML" );
		messages.put(OUTPUT, "Problem outputting XML" );
	};


	public XMLException(int diagnosticCode)
	{
		this(diagnosticCode, null);
	}

	
   public XMLException(int diagnosticCode, String upstreamMessage)
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
