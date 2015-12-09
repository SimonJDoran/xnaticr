/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;

/**
 *
 * @author jamesd
 */
public class ImageStudy implements Displayable
{
	private ImageSeries series;
	private String instanceUid = "";
	private String startDate = "";
	private String startTime = "";

	@Override
	public void display()
	{
		display("", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display("", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"InstanceUid: "+instanceUid);
		System.out.println(pad+"StartDate: "+startDate);
		System.out.println(pad+"StartTime: "+startTime);
		series.display(indent+"  ", recurse);
	}

	/**
	 * @return the series
	 */
	public ImageSeries getSeries()
	{
		return series;
	}

	/**
	 * @return the instanceUid
	 */
	public String getInstanceUid()
	{
		return instanceUid;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime()
	{
		return startTime;
	}

	/**
	 * @param series the series to set
	 */
	public void setSeries(ImageSeries series)
	{
		this.series = series;
	}

	/**
	 * @param instanceUid the instanceUid to set
	 */
	public void setInstanceUid(String instanceUid)
	{
		this.instanceUid = instanceUid;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

}
