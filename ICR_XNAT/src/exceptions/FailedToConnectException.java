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
* Java class: FailedToConnectException.java
* First created on March 23, 2010, 23:39 PM
* 
* Exception that allows the additional specification of a return code
* and explicitly enumerates the possible reasons for exceptions to be
* generated.
*********************************************************************/

package exceptions;

/**
 * @author Simon J Doran
 */
public class FailedToConnectException extends SJDException
{
	public static final int SOCKET_TIMEOUT      = 0;
   public static final int IO                  = 1;
   public static final int NULL_AUTH           = 2;
   public static final int AUTH_FAILURE        = 3;
   public static final int BAD_XML             = 4;
   public static final int JSESSION            = 5;
   public static final int MALFORMED_URL       = 6;
   public static final int WRONG_HTTP_RESPONSE = 7;



	private static final String[] errorMessages =
	{
		"Connection timed out",
      "I/O error",
      "Null pointer given as authorisation",
      "Authorisation failure: incorrect userid or password",
      "Connection did not return valid XML",
      "Unable to obtain JSESSION",
      "The REST URL was malformed.",
      "An unexpected HTTP response (i.e., not 200) came back from the server."
	};


	public FailedToConnectException(int errorCode)
	{
		super(errorCode);
	}

   public FailedToConnectException(int errorCode, String upstreamMessage)
   {
      super(errorCode, upstreamMessage);
   }

   @Override
	public String[] getMessageList()
	{
		return errorMessages;
	}

}
