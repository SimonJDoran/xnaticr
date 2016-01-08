/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class AimUtils
{
	private static final Logger logger = LoggerFactory.getLogger(AimUtils.class);

	public static Date parseDateTime(String dateTime)
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = null;
		try
		{
			date = df.parse(dateTime);
		}
		catch (ParseException ex)
		{
			logger.warn(ex.getMessage());
		}
		return date;
	}
}
