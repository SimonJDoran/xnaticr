/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import java.util.List;

/**
 *
 * @author jamesd
 */
public abstract class AbstractSearchCriterion implements SearchCriterion
{
	@Override
	public String createCombinatorSql()
	{
		String sql = "";
		switch (getCombinator())
		{
			case SearchCriterion.And:
				return "AND";
			case SearchCriterion.Or:
				return "OR";
			default:
				break;
		}
		return sql;
	}

	@Override
	public String createComparatorSql()
	{
		String sql = "";
		switch (getComparator())
		{
			case SearchCriterion.LessThan:
				return "<";
			case SearchCriterion.LessThanOrEqual:
				return "<=";
			case SearchCriterion.Equal:
				return "=";
			case SearchCriterion.GreaterThanOrEqual:
				return ">=";
			case SearchCriterion.GreaterThan:
				return ">";
			case SearchCriterion.NotEqual:
				return "!=";
			case SearchCriterion.Like:
				return "LIKE";
			default:
				break;
		}
		return sql;
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

	/**
	 *
	 * @param sb
	 * @param crit
	 * @return
	 */
	protected final StringBuilder buildString(StringBuilder sb,
		SearchCriterion crit)
	{
		if (crit.hasCriteria())
		{
			sb.append("(");
			List<SearchCriterion> critList = crit.getCriteria();
			buildString(sb, critList.get(0));
			for (int i=1; i<critList.size(); i++)
			{
				SearchCriterion sc = critList.get(i);
				sb.append(" ").append(sc.createCombinatorSql()).append(" ");
				buildString(sb, sc);
			}
			sb.append(")");
		}
		else
		{
			sb.append("(");
			sb = buildType(sb, crit).append(DicomUtils.tagName(crit.getTag()))
				.append(" ").append(crit.createComparatorSql()).append(" ")
				.append(crit.getValue()).append(")");

		}
		return sb;
	}

	private StringBuilder buildType(StringBuilder sb, SearchCriterion crit)
	{
		switch (crit.getDicomType())
		{
			case Instance:
				sb.append("Instance.");
				break;

			case Series:
				sb.append("Series.");
				break;

			case Study:
				sb.append("Study.");
				break;

			default:
		}
		return sb;
	}
}
