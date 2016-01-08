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
public abstract class Entity implements Displayable
{
	protected String uid = "";

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

	/**
	 *
	 * @return
	 */
	public String getUid()
	{
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid)
	{
		this.uid = uid;
	}

}
