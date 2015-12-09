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
public class Person implements Displayable
{
	private String birthDate = "00000000";
	private String ethnicGroup = "";
	private String id = "";
	private String name = "";
	private String sex = "";

	public Person()
	{}

	public Person(String name, String birthDate, String id)
	{
		this.name = (name == null) ? "" : name;
		this.birthDate = ((birthDate == null) || birthDate.isEmpty()) ? 
			"00000000" : birthDate;
		this.id = (id == null) ? "" : id;
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
		System.out.println(pad+"BirthDate: "+birthDate);
		System.out.println(pad+"Id: "+id);
		if (!sex.isEmpty())
		{
			System.out.println(pad+"Sex: "+sex);
		}
		if (!ethnicGroup.isEmpty())
		{
			System.out.println(pad+"EthnicGroup: "+ethnicGroup);
		}
	}

	/**
	 * @return the birthDate
	 */
	public String getBirthDate()
	{
		return birthDate;
	}

	/**
	 * @return the ethnicGroup
	 */
	public String getEthnicGroup()
	{
		return ethnicGroup;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the sex
	 */
	public String getSex()
	{
		return sex;
	}

	/**
	 * @param birthDate the birthDate to set
	 */
	public void setBirthDate(String birthDate)
	{
		this.birthDate = ((birthDate == null) || birthDate.isEmpty()) ? 
			"00000000" : birthDate;
	}

	/**
	 * @param ethnicGroup the ethnicGroup to set
	 */
	public void setEthnicGroup(String ethnicGroup)
	{
		this.ethnicGroup = (ethnicGroup == null) ? "" : ethnicGroup;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = (id == null) ? "" : id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = (name == null) ? "" : name;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex)
	{
		this.sex = (sex == null) ? "" : sex;
	}
}
