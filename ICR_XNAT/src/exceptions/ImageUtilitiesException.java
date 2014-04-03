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
* Java class: ImageUtilitiesException.java
* First created on July 19, 2007, 9:16 AM
* 
* Exception that allows the additional specification of a return code
* and explicitly enumerates the possible reasons for exceptions to be
* generated.
*********************************************************************/

package exceptions;

public class ImageUtilitiesException extends SJDException
{
	public static final int INCORRECT_WINDOW_RANGE	= 0;
	public static final int ZERO_COLUMNS				= 1;
	public static final int NON_RECTANGULAR_IMAGE	= 2;
   public static final int THUMBNAIL_CREATION      = 3;

	private int imageUtilityStatus;
	
	private static final String[] errorMessages =
	{
		"One or both of the minimum and maximum values of the imaging window range is invalid.",
		"The number of image columns may not be zero.",
		"The data do not represent a valid rectangular image.",
      "Unable to create the thumbnail image."
	}; 
	
	
	
	/** Creates a new instance of ImageUtilitiesException */
	public ImageUtilitiesException(int errorCode)
	{
		super(errorCode);
	}
	
	public ImageUtilitiesException(int errorCode, String upstreamMessage)
   {
      super(errorCode, upstreamMessage);
   }

   @Override
	public String[] getMessageList()
	{
		return errorMessages;
	}
}
