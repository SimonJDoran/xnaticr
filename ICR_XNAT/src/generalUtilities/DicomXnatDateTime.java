/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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

/********************************************************************
* @author Simon J Doran
* Java class: DicomXnatDateTime.java
* First created on Dec 9, 2015 at 10:16:52 AM
* 
* Conversion between DICOM and XNAT formats for date and time
*********************************************************************/

package generalUtilities;

import exceptions.DataFormatException;

public class DicomXnatDateTime
{
	public static String convertDicomToXnatDateTime(String date, String time)
			        throws DataFormatException
   {
      String month;
      String day;
      String year;
      String hour;
      String minute;
      String second;
      
      try
      {
         month  = date.substring(4, 6);
         day    = date.substring(6, 8);
         year   = date.substring(0, 4);
         hour   = time.substring(0, 2);
         minute = time.substring(2, 4);
         second = time.substring(4, 6);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return new String(year + "-" + month + "-" + day
                        + "T" + hour + ":" + minute + ":" + second);
   }
   
   
	
	public static String convertXnatDateTimeToDicomDate(String dateTime)
			        throws DataFormatException
   {
      String day;
      String month;
      String year;
      
      try
      {
         day    = dateTime.substring(8, 10);
         month  = dateTime.substring(5, 7);
         year   = dateTime.substring(0, 4);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return year + month + day;
   }
   
   
   
   public static String convertXnatDateTimeToDicomTime(String dateTime)
			        throws DataFormatException
   {
      String hour;
      String minute;
      String second;
      
      try
      {
         hour   = dateTime.substring(11, 13);
         minute = dateTime.substring(14, 16);
         second = dateTime.substring(17, 19);
      }
      catch (Exception ex)
      {
         throw new DataFormatException(DataFormatException.DATE);
      }
      
      return  hour + minute + second;
   }
   
   
   /**
    * Take a String variable in the form that is used by XNAT and
    * convert it to the DICOM time format hhmmss.
    * @param time and input structureSetTime String
    * @return A String containing the structureSetTime formatted for DICOM
    * @throws DataFormatException
    */
   public static String convertXnatToDicomTime(String time) throws DataFormatException
   {
      String hour;
      String minute;
      String second;
      
      if (time.length() < 19)
			throw new DataFormatException(DataFormatException.TIME, time);
      
		//TODO Better input checking: will return non-valid times
		//     (currently catches only string length exceptions to avoid actual
		//     program crash).
		
		hour   = time.substring(11, 13);
      minute = time.substring(14, 16);
      second = time.substring(17, 19);

      return hour + minute + second;
   }
	
	
	/**
    * Take a dateTime String variable in the form that is output by MRIW_RECORD
	 * and convert it to the DICOM date format yyyymmdd.
    * @param dateTime an input dateTime String, originating from the application MRIW_RECORD
    * @return an output String containing the structureSetDate formatted for DICOM
    * @throws DataFormatException
    */
   public static String convertMriwToDicomDate(String dateTime) throws DataFormatException
   {
      String day;
      String monthNumeric;
      String year;
      String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                         "Aug", "Sep", "Oct", "Nov", "Dec"};
		
		
		if (dateTime.length() < 24)
			throw new DataFormatException(DataFormatException.DATE, dateTime);
		
		String month = dateTime.substring(4, 7);
		monthNumeric = "";
		for (int i=0; i<12; i++)
		{
			if (month.equals(months[i]))
			{
				if (i<9) monthNumeric = "0" + Integer.toString(i+1);
				else     monthNumeric = Integer.toString(i+1);
			}
		}
		if (monthNumeric.equals(""))
			throw new DataFormatException(DataFormatException.DATE, dateTime);
 
      year  = dateTime.substring(20, 24);
      day   = dateTime.substring(8, 10);
		//TODO Better input checking: will currently return non-valid dates
		//     if day and year are not valid.
		
      if (day.charAt(0) == ' ') day = "0" + day.substring(1, 2);
     
      return year + monthNumeric + day;
   }


   
	
	/**
    * Take a String variable in the form that is used by the MRIW XML file and
    * convert it to the XNAT date format yyyy-mm-dd.
    * @param dateTime
    * @return A String containing the date
    * @throws DataFormatException
    */
   public static String convertMriwToXnatDate(String dateTime) throws DataFormatException
   {
      String month;
      String day;
      String year;
      
      if (dateTime.length() < 24) throw new DataFormatException(DataFormatException.DATE);
      month = dateTime.substring( 4,  7);
      
		//TODO Better input checking: will currently return non-valid dates
		//     if day and year are not valid.
		day   = dateTime.substring( 8, 10);
      year  = dateTime.substring(20, 24);
      
      String[] months    = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                         "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
      String[] m2Digit   = new String[] {"01", "02", "03", "04", "05", "06",
                                         "07", "08", "09", "10", "11", "12"};
      String month2Digit = null;
      for (int i=0; i<12; i++) if (months[i].equals(month)) month2Digit = m2Digit[i];
      if (month2Digit == null) throw new DataFormatException(DataFormatException.DATE);

      return year + "-" + month2Digit + "-" + day.replaceFirst(" ", "0");
   }


   
   /**
    * Take a String variable in the form that is used by the MRIW XML file and
    * convert it to the XNAT date format yyyy-mm-dd.
    * @param dateTime
    * @return A String containing the date
    * @throws DataFormatException
    */
   public static String convertMriwToXnatTime(String dateTime) throws DataFormatException
   {
		if (dateTime.length() < 9) throw new DataFormatException(DataFormatException.TIME);
      //TODO Better input checking: will currently return non-valid dates
		//     if input is not a bona fide MRIW time.
		
		return dateTime.substring(1, 9);
   }
   
   
   
   /**
    * Take a String variable in the form that is used by the AIM XML file and
    * convert it to the XNAT date format yyyy-mm-dd.
    * @param dateTime
    * @return A String containing the date
    * @throws DataFormatException
    */
   public static String convertAimToXnatDate(String dateTime) throws DataFormatException
   {
		// ePAD v1 formatting
		if (dateTime.length() == 19) return dateTime.substring(0, 10);
		
		// ePAD v2 formatting
		if (dateTime.length() == 14)
		{
			String year  = dateTime.substring(0, 4);
			String month = dateTime.substring(4, 6);
			String day   = dateTime.substring(6, 8);
			return year + "-" + month + "-" + day;
		}
		
      //TODO Better input checking: will currently return non-valid dates
		//     if input is not a bona fide AIM time.
			
		throw new DataFormatException(DataFormatException.DATE);
   }
   
   
   
   /**
    * Take a String variable in the form that is used by the AIM XML file and
    * convert it to the XNAT date format hh:MM:ss.
    * @param dateTime
    * @return A String containing the time 
    * @throws DataFormatException
    */
   public static String convertAimToXnatTime(String dateTime) throws DataFormatException
   {
		// ePAD v1 formatting
		if (dateTime.length() == 19) return dateTime.substring(11, 19);
		
		// ePAD v2 formatting
		if (dateTime.length() == 14)
		{
			String hour  = dateTime.substring(8,  10);
			String min   = dateTime.substring(10, 12);
			String sec   = dateTime.substring(12, 14);
			return hour + ":" + min + ":" + sec;
		}
		
      //TODO Better input checking: will currently return non-valid dates
		//     if input is not a bona fide AIM time.
			
		throw new DataFormatException(DataFormatException.TIME);
   }
     
}
