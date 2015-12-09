/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

/**
 *
 * @author jamesd
 */
public abstract class Annotation extends Entity
{
	private String comment = "";
	private String dateTime = "";
	private String name = "";

	public String getComment()
	{
		return comment;
	}

	public String getDateTime()
	{
		return dateTime;
	}

	public String getName()
	{
		return name;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(String dateTime)
	{
		this.dateTime = dateTime;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
