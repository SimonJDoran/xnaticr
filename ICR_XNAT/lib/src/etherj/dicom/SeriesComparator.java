/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import java.util.Comparator;

/**
 *
 * @author jamesd
 */
public class SeriesComparator implements Comparator<Series>
{
	@Override
	public int compare(Series a, Series b)
	{
		if (a.equals(b))
		{
			return 0;
		}
		int numeric = (int) Math.signum(a.getNumber()-b.getNumber());
		if (numeric != 0)
		{
			return numeric;
		}
		return (int) Math.signum(a.getTime()-b.getTime());
	}

	/**
	 * Natural ordering for Series is number, time.
	 */
	public static final SeriesComparator Natural = new SeriesComparator();
}
