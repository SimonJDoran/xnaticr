/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.AbstractSearchCriterion;
import etherj.dicom.SearchCriterion;
import etherj.dicom.DicomUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.Tag;

/**
 *
 * @author jamesd
 */
class SimpleSearchCriterion extends AbstractSearchCriterion
{
	private static final Map<Integer,Set<Integer>> types = new HashMap<>();
	private int combinator;
	private final int comparator;
	private final int tag;
	private final String value;
	private int dicomType = Unspecified;

	static
	{
		Set<Integer> instTags = new HashSet<>();
		instTags.add(Tag.Modality);
		instTags.add(Tag.SeriesInstanceUID);
		instTags.add(Tag.StudyInstanceUID);
		types.put(SearchCriterion.Instance, instTags);
		Set<Integer> seTags = new HashSet<>();
		seTags.add(Tag.Modality);
		seTags.add(Tag.SeriesInstanceUID);
		types.put(SearchCriterion.Series, seTags);
		Set<Integer> stTags = new HashSet<>();
		stTags.add(Tag.Modality);
		stTags.add(Tag.StudyInstanceUID);
		types.put(SearchCriterion.Study, stTags);
	}

	public SimpleSearchCriterion(int tag, int comparator, String value)
	{
		this(tag, comparator, value, And);
	}

	public SimpleSearchCriterion(int tag, int comparator, String value,
		int combinator)
	{
		this.tag = tag;
		this.value = value;
		this.comparator = comparator;
		this.combinator = combinator;
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Combinator: "+createCombinatorSql());
		System.out.println(pad+buildString(new StringBuilder(), this).toString());
	}

	@Override
	public int getCombinator()
	{
		return combinator;
	}

	@Override
	public int getComparator()
	{
		return comparator;
	}

	@Override
	public List<SearchCriterion> getCriteria()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDicomType()
	{
		return dicomType;
	}

	@Override
	public int getTag()
	{
		return tag;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean hasCriteria()
	{
		return false;
	}

	@Override
	public void setCombinator(int combinator)
	{
		this.combinator = combinator;
	}

	@Override
	public void setDicomType(int type)
	{
		if (type == Unspecified)
		{
			dicomType = type;
			return;
		}
		Set<Integer> tags = types.get(type);
		if (tags == null)
		{
			throw new IllegalArgumentException("Invalid DICOM type: "+type);
		}
		if (!tags.contains(tag))
		{
			throw new IllegalArgumentException("DICOM type "+type+
				" not compatible with Tag "+DicomUtils.tagName(tag));
		}
		dicomType = type;
	}
}
