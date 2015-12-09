/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jamesd
 */
public class StudyComparator implements Comparator<Study>
{

	@Override
	public int compare(Study a, Study b)
	{
		if (a.equals(b))
		{
			return 0;
		}
		int dateA = Integer.MAX_VALUE;
		int dateB = Integer.MAX_VALUE;
		try
		{
			dateA = Integer.parseInt(a.getDate());
		}
		catch (NumberFormatException ex)
		{}
		try
		{
			dateB = Integer.parseInt(b.getDate());
		}
		catch (NumberFormatException ex)
		{}
		int date = (int) Math.signum(dateA-dateB);
		if (date != 0)
		{
			return date;
		}
		String idA = a.getId();
		String idB = b.getId();
		if (idA.equals(idB))
		{
			return 0;
		}
		List<String> idList = Arrays.asList(idA, idB);
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		Collections.sort(idList, collator);
		return (idList.get(0).equals(idA)) ? -1 : 1;
	}

	/**
	 * Natural order for Studies is date, ID
	 */
	public static final StudyComparator Natural = new StudyComparator();
}
