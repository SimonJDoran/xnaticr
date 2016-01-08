/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;
import java.util.List;

/**
 *
 * @author jamesd
 */
public abstract class AnnotationCollection implements Displayable
{
	protected String aimVersion = "";
	protected String dateTime = "";
	protected Equipment equipment = null;
	private String path = "";
	protected String uid = "";
	protected User user = null;

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
	 * @param annotation
	 * @return
	 */
	public abstract ImageAnnotation addAnnotation(ImageAnnotation annotation);

	/**
	 *
	 * @return
	 */
	public String getAimVersion()
	{
		return aimVersion;
	}

	/**
	 *
	 * @param uid
	 * @return
	 */
	public abstract ImageAnnotation getAnnotation(String uid);

	/**
	 *
	 * @return
	 */
	public abstract int getAnnotationCount();

	/**
	 *
	 * @return
	 */
	public abstract List<ImageAnnotation> getAnnotationList();

	/**
	 *
	 * @return
	 */
	public String getDateTime()
	{
		return dateTime;
	}

	/**
	 * @return the equipment
	 */
	public Equipment getEquipment()
	{
		return equipment;
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
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
	 *
	 * @return
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 *
	 * @param uid
	 * @return
	 */
	public abstract ImageAnnotation removeAnnotation(String uid);

	/**
	 * @param aimVersion the aimVersion to set
	 */
	public void setAimVersion(String aimVersion)
	{
		this.aimVersion = aimVersion;
	}

	/**
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(String dateTime)
	{
		this.dateTime = dateTime;
	}

	/**
	 * @param equipment the equipment to set
	 */
	public void setEquipment(Equipment equipment)
	{
		this.equipment = equipment;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid)
	{
		this.uid = uid;
	}

	/**
	 * @param user
	 */
	public void setUser(User user)
	{
		this.user = user;
	}

}
