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
public class User implements Displayable
{
	private String name = "";
	private String loginName = "";
	private int numberWithinRoleOfClinicalTrial = 0;
	private String roleInTrial = "";

	public User()
	{}

	public User(String name, String username)
	{
		this.name = (name == null) ? "" : name;
		this.loginName = (username == null) ? "" : username;
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
		System.out.println(pad+"Name: "+name);
		System.out.println(pad+"LoginName: "+loginName);
		if (!roleInTrial.isEmpty())
		{
			System.out.println(pad+"RoleInTrial: "+roleInTrial);
		}
		if (numberWithinRoleOfClinicalTrial > 0)
		{
			System.out.println(pad+"NumberWithinRoleOfClinicalTrial: "+
				numberWithinRoleOfClinicalTrial);
		}
	}

	/**
	 * @return the loginName
	 */
	public String getLoginName()
	{
		return loginName;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the numberWithinRoleOfClinicalTrial
	 */
	public int getNumberWithinRoleOfClinicalTrial()
	{
		return numberWithinRoleOfClinicalTrial;
	}

	/**
	 * @return the roleInTrial
	 */
	public String getRoleInTrial()
	{
		return roleInTrial;
	}

	/**
	 * @param loginName the loginName to set
	 */
	public void setLoginName(String loginName)
	{
		this.loginName = (loginName == null) ? "" : loginName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = (name == null) ? "" : name;
	}

	/**
	 * @param numberWithinRoleOfClinicalTrial the numberWithinRoleOfClinicalTrial to set
	 */
	public void setNumberWithinRoleOfClinicalTrial(int numberWithinRoleOfClinicalTrial)
	{
		this.numberWithinRoleOfClinicalTrial = numberWithinRoleOfClinicalTrial;
	}

	/**
	 * @param roleInTrial the roleInTrial to set
	 */
	public void setRoleInTrial(String roleInTrial)
	{
		this.roleInTrial = (roleInTrial == null) ? "" : roleInTrial;
	}

}
