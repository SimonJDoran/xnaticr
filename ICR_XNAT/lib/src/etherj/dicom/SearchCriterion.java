/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface SearchCriterion extends Displayable
{
	public static final int Null = 0;
	public static final int LessThan = 1;
	public static final int LessThanOrEqual = 2;
	public static final int Equal = 3;
	public static final int GreaterThanOrEqual = 4;
	public static final int GreaterThan = 5;
	public static final int NotEqual = 6;
	public static final int Like = 7;
	public static final int And = 100;
	public static final int Or = 101;
	public static final int Unspecified = 1000;
	public static final int Instance = 1001;
	public static final int Series = 1002;
	public static final int Study = 1003;

	/**
	 *
	 * @return
	 */
	public String createCombinatorSql();

	/**
	 *
	 * @return
	 */
	public String createComparatorSql();

	/**
	 *
	 * @return
	 */
	public int getCombinator();

	/**
	 *
	 * @return
	 */
	public int getComparator();

	/**
	 *
	 * @return
	 */
	public List<SearchCriterion> getCriteria();

	/**
	 *
	 * @return
	 */
	public int getDicomType();

	/**
	 *
	 * @return
	 */
	public int getTag();

	/**
	 *
	 * @return
	 */
	public String getValue();

	/**
	 *
	 * @return
	 */
	public boolean hasCriteria();

	/**
	 *
	 * @param combinator
	 */
	public void setCombinator(int combinator);

	/**
	 *
	 * @param type
	 */
	public void setDicomType(int type);
}
