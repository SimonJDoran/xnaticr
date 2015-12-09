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
public class Equipment implements Displayable
{
	private String deviceSerialNumber = "";
	private String manufacturerName = "";
	private String manufacturerModelName = "";
	private String softwareVersion = "";

	public Equipment()
	{}

	public Equipment(String manufacturer, String modelName)
	{
		manufacturerName = (manufacturer == null) ? "" : manufacturer;
		manufacturerModelName = (modelName == null) ? "" : modelName;
	}

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
		System.out.println(pad+"ManufacturerName: "+manufacturerName);
		System.out.println(pad+"ManufacturerModelName: "+manufacturerModelName);
		if (!deviceSerialNumber.isEmpty())
		{
			System.out.println(pad+"DeviceSerialNumber: "+deviceSerialNumber);
		}
		if (!softwareVersion.isEmpty())
		{
			System.out.println(pad+"SoftwareVersion: "+softwareVersion);
		}
	}

	/**
	 * @return the deviceSerialNumber
	 */
	public String getDeviceSerialNumber()
	{
		return deviceSerialNumber;
	}

	/**
	 * @return the manufacturerName
	 */
	public String getManufacturerName()
	{
		return manufacturerName;
	}

	/**
	 * @return the manufacturerModelName
	 */
	public String getManufacturerModelName()
	{
		return manufacturerModelName;
	}

	/**
	 * @return the softwareVersion
	 */
	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	/**
	 * @param deviceSerialNumber the deviceSerialNumber to set
	 */
	public void setDeviceSerialNumber(String deviceSerialNumber)
	{
		this.deviceSerialNumber = (deviceSerialNumber == null) ?
			"" : deviceSerialNumber;
	}

	/**
	 * @param manufacturerName the manufacturerName to set
	 */
	public void setManufacturerName(String manufacturerName)
	{
		this.manufacturerName = (manufacturerName == null) ?
			"" : manufacturerName;
	}

	/**
	 * @param manufacturerModelName the manufacturerModelName to set
	 */
	public void setManufacturerModelName(String manufacturerModelName)
	{
		this.manufacturerModelName = (manufacturerModelName == null) ?
			"" : manufacturerModelName;
	}

	/**
	 * @param softwareVersion the softwareVersion to set
	 */
	public void setSoftwareVersion(String softwareVersion)
	{
		this.softwareVersion = (softwareVersion == null) ? "" : softwareVersion;
	}


}
