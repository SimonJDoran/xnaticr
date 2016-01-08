/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.AbstractSearchCriterion;
import etherj.dicom.SearchCriterion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jamesd
 */
class CompoundSearchCriterion extends AbstractSearchCriterion
{
	private int combinator;
	private final List<SearchCriterion> criteria = new ArrayList<>();

	/**
	 *
	 * @param a
	 * @param b
	 * @throws java.lang.IllegalArgumentException
	 */
	public CompoundSearchCriterion(SearchCriterion a, SearchCriterion b)
	{
		this(a, b, SearchCriterion.And);
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @param combinator
	 * @throws java.lang.IllegalArgumentException
	 */
	public CompoundSearchCriterion(SearchCriterion a, SearchCriterion b,
		int combinator)
	{
		if ((a == null) || (b == null))
		{
			throw new IllegalArgumentException("Null criteria not permitted");
		}
		criteria.add(a);
		criteria.add(b);
		this.combinator = combinator;
	}

	/**
	 *
	 * @param criteria
	 * @throws java.lang.IllegalArgumentException
	 */
	public CompoundSearchCriterion(List<SearchCriterion> criteria)
	{
		this(criteria, SearchCriterion.And);
	}

	/**
	 *
	 * @param criteria
	 * @param combinator
	 * @throws java.lang.IllegalArgumentException
	 */
	public CompoundSearchCriterion(List<SearchCriterion> criteria, int combinator)
	{
		if (criteria.size() < 2)
		{
			throw new IllegalArgumentException(
				"Criteria list must have at least two elements");
		}
		this.criteria.addAll(criteria);
		this.combinator = combinator;
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Combinator: "+createCombinatorSql());
		StringBuilder sb = new StringBuilder(pad);
		sb = buildString(sb, this);
		System.out.println(sb.toString());
	}

	@Override
	public int getCombinator()
	{
		return combinator;
	}

	@Override
	public int getComparator()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDicomType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTag()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SearchCriterion> getCriteria()
	{
		return Collections.unmodifiableList(criteria);
	}

	@Override
	public boolean hasCriteria()
	{
		return criteria.size() > 0;
	}

	@Override
	public void setCombinator(int combinator)
	{
		this.combinator = combinator;
	}

	@Override
	public void setDicomType(int type)
	{
		throw new UnsupportedOperationException();
	}
}
