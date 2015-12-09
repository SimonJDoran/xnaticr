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
public class Image implements Displayable
{
	private String sopClassUid = "";
	private String instanceUid = "";

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
		System.out.println(pad+"SopClassUid: "+sopClassUid);
	}

	/**
	 * @return the instanceUid
	 */
	public String getInstanceUid()
	{
		return instanceUid;
	}

	/**
	 * @return the sopClassUid
	 */
	public String getSopClassUid()
	{
		return sopClassUid;
	}

	/**
	 * @param instanceUid the instanceUid to set
	 */
	public void setInstanceUid(String instanceUid)
	{
		this.instanceUid = instanceUid;
	}

	/**
	 * @param sopClassUid the sopClassUid to set
	 */
	public void setSopClassUid(String sopClassUid)
	{
		this.sopClassUid = sopClassUid;
	}

}
