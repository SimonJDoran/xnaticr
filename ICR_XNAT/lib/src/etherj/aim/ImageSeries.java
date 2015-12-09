/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jamesd
 */
public class ImageSeries implements Displayable
{
	private String instanceUid = "";
	private Code modality = new Code();
	private final Map<String,Image> images = new HashMap<>();

	public Image addImage(Image image)
	{
		return images.put(image.getInstanceUid(), image);
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
		System.out.println(pad+"InstanceUid: "+instanceUid);
		modality.display(indent+"  ");
		if (recurse)
		{
			for (Image image : getImageList())
			{
				image.display(indent+"  ");
			}
		}
	}

	public Image getImage(String uid)
	{
		return images.get(uid);
	}

	public List<Image> getImageList()
	{
		List<Image> list = new ArrayList<>();
		Set<Map.Entry<String,Image>> entries = images.entrySet();
		Iterator<Map.Entry<String,Image>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,Image> entry = iter.next();
			list.add(entry.getValue());
		}
		return list;
	}

	/**
	 * @return the instanceUid
	 */
	public String getInstanceUid()
	{
		return instanceUid;
	}

	/**
	 * @return the modality
	 */
	public Code getModality()
	{
		return modality;
	}

	public Image removeImage(String uid)
	{
		return images.remove(uid);
	}

	/**
	 * @param instanceUid the instanceUid to set
	 */
	public void setInstanceUid(String instanceUid)
	{
		this.instanceUid = instanceUid;
	}

	/**
	 * @param modality the modality to set
	 */
	public void setModality(Code modality)
	{
		this.modality = modality;
	}

}
