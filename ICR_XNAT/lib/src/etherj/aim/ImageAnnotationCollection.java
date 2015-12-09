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
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author jamesd
 */
public class ImageAnnotationCollection extends AnnotationCollection
{
	private final Map<String,ImageAnnotation> annotations = new HashMap<>();
	private Person person = null;

	@Override
	public ImageAnnotation addAnnotation(ImageAnnotation annotation)
	{
		return annotations.put(annotation.getUid(), annotation);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"AimVersion: "+aimVersion);
		System.out.println(pad+"DateTime: "+dateTime);
		if (user != null)
		{
			user.display(indent+"  ");
		}
		if (equipment != null)
		{
			equipment.display(indent+"  ");
		}
		if (person != null)
		{
			person.display(indent+"  ");
		}
		System.out.println(pad+"Uid: "+uid);
		int nAnnotations = annotations.size();
		System.out.println(pad+"AnnotationList: "+((nAnnotations != 1) ? "s" : ""));
		if (recurse)
		{
			for (ImageAnnotation annotation : getAnnotationList())
			{
				annotation.display(indent+"  ", recurse);
			}
		}
	}

	@Override
	public ImageAnnotation getAnnotation(String uid)
	{
		return annotations.get(uid);
	}

	@Override
	public int getAnnotationCount()
	{
		return annotations.size();
	}

	@Override
	public List<ImageAnnotation> getAnnotationList()
	{
		List<ImageAnnotation> list = new ArrayList<>();
		Set<Entry<String,ImageAnnotation>> entries = annotations.entrySet();
		Iterator<Entry<String,ImageAnnotation>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Entry<String,ImageAnnotation> entry = iter.next();
			list.add(entry.getValue());
		}
		return list;
	}

	/**
	 * @return the person
	 */
	public Person getPerson()
	{
		return person;
	}

	@Override
	public ImageAnnotation removeAnnotation(String uid)
	{
		return annotations.remove(uid);
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person)
	{
		this.person = person;
	}

	public static class FileUidPair implements Displayable
	{
		private final String path;
		private final String uid;

		public FileUidPair(String path, String uid)
		{
			this.path = path;
			this.uid = uid;
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
		System.out.println(pad+"Path: "+path);
		System.out.println(pad+"Uid: "+uid);
	}

		/**
		 * @return the path
		 */
		public String getPath()
		{
			return path;
		}

		/**
		 * @return the uid
		 */
		public String getUid()
		{
			return uid;
		}
	}
}
