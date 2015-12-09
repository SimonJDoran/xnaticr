/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jamesd
 */
public class SearchSpecification implements Displayable
{
	private final List<SearchCriterion> criteria = new ArrayList<>();

	public SearchSpecification()
	{}

	public void addCriteria(List<SearchCriterion> criteria)
	{
		this.criteria.addAll(criteria);
	}

	public void addCriterion(SearchCriterion criterion)
	{
		criteria.add(criterion);
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
		int nCriteria = criteria.size();
		System.out.println(pad+"Criteria: "+nCriteria+" element"+
			(nCriteria != 1 ? "s" : ""));
		if (recurse)
		{
			for (SearchCriterion crit : criteria)
			{
				crit.display("  ");
			}
		}
	}

	public List<SearchCriterion> getCriteria()
	{
		return Collections.unmodifiableList(criteria);
	}
}
