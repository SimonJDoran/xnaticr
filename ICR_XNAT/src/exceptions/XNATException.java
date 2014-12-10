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
* Java class: XNATException.java
* First created on Mar 17, 2009 at 12:11:06 PM
* 
* Exception that allows the additional specification of a return code
* and explicitly enumerates the possible reasons for exceptions to be
* generated.
*********************************************************************/

package exceptions;


public class XNATException extends SJDException
{
	public static final int SUBJECT_CREATE    = 0;
   public static final int SEARCH_CREATE     = 1;
   public static final int SEARCH            = 2;
   public static final int PARSE             = 3;
   public static final int GET               = 4;
   public static final int STREAM_CLOSE      = 5;
   public static final int CATALOG           = 6;
   public static final int QUERY_LOADED      = 7;
	public static final int SUBJ_LIST	      = 8;
   public static final int DATE              = 9;
   public static final int SESSION_CREATE    = 10;
	public static final int SCAN_CREATE       = 11;
   public static final int FILE_UPLOAD       = 12;
   public static final int CACHE             = 13;
   public static final int RESOURCE_CREATE   = 14;
   public static final int THUMB_CREATE      = 15;
   public static final int JSESSION          = 16;
   public static final int LOCKED            = 17;
   public static final int DATA_NOT_PRESENT  = 18;
   public static final int DATA_INCONSISTENT = 19;
   public static final int DATA_AMBIGUOUS    = 20;
	public static final int RETRIEVING_LIST   = 21;

	private static final String[] errorMessages =
	{
		"Unable to create XNAT subject",
      "Unable to initiate XNAT search",
      "Error during XNAT search",
      "Error parsing XNAT output",
      "Error during processing XNAT REST API GET",
      "Unable to close XNAT output stream",
      "Problem with catalog file",
      "Unable to determine whether file is already uploaded",
		"Error while trying to access XNAT subject list",
      "Unexpected date format in DICOM file",
		"Unable to create XNAT session",
		"Unable to create XNAT scan",
      "Error uploading file",
      "Unexpected cache condition - programming error",
      "Unable to create XNAT resource",
      "Unable to create thumbnail image",
      "Unable to obtain JSESSION ID from XNAT",
      "Cannot perform the request, as another search is in progress",
      "Required data not present in XNAT",
      "Inconsistent data",
      "Ambiguous data",
		"Problem retrieving list of files"
	};


	public XNATException(int errorCode)
	{
		super(errorCode);
	}

   public XNATException(int errorCode, String upstreamMessage)
   {
      super(errorCode, upstreamMessage);
   }

   @Override
	public String[] getMessageList()
	{
		return errorMessages;
	}
}