/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

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
public class ImageAnnotation extends Annotation
{
	private final Map<String,Markup> markups = new HashMap<>();
	private final Map<String,ImageReference> references = new HashMap<>();

	public Markup addMarkup(Markup markup)
	{
		return markups.put(markup.getUid(), markup);
	}

	public ImageReference addReference(ImageReference reference)
	{
		return references.put(reference.getUid(), reference);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Comment: "+getComment());
		System.out.println(pad+"DateTime: "+getDateTime());
		System.out.println(pad+"Name: "+getName());
		int nMarkup = markups.size();
		System.out.println(pad+"MarkupList: "+nMarkup+" markup"+
			(nMarkup != 1 ? "s" : ""));
		int nRef = references.size();
		System.out.println(pad+"ReferenceList: "+nRef+" reference"+
			(nRef != 1 ? "s" : ""));
		if (recurse)
		{
			System.out.println(pad+"Markups:");
			List<Markup> markupList = getMarkupList();
			for (Markup markup : markupList)
			{
				markup.display(indent+"  ", true);
			}
			System.out.println(pad+"References:");
			List<ImageReference> refList = getReferenceList();
			for (ImageReference ref : refList)
			{
				ref.display(indent+"  ", true);
			}
		}
	}

	public Markup getMarkup(String uid)
	{
		return markups.get(uid);
	}

	public List<Markup> getMarkupList()
	{
		List<Markup> list = new ArrayList<>();
		Set<Map.Entry<String,Markup>> entries = markups.entrySet();
		Iterator<Map.Entry<String,Markup>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,Markup> entry = iter.next();
			list.add(entry.getValue());
		}
		return list;
	}

	public ImageReference getReference(String uid)
	{
		return references.get(uid);
	}

	public List<ImageReference> getReferenceList()
	{
		List<ImageReference> list = new ArrayList<>();
		Set<Map.Entry<String,ImageReference>> entries = references.entrySet();
		Iterator<Map.Entry<String,ImageReference>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,ImageReference> entry = iter.next();
			list.add(entry.getValue());
		}
		return list;
	}

	public Markup removeMarkup(String uid)
	{
		return markups.remove(uid);
	}

	public ImageReference removeReference(String uid)
	{
		return references.remove(uid);
	}

}
