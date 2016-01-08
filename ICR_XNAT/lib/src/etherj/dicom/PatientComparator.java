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
public class PatientComparator implements Comparator<Patient>
{

	@Override
	public int compare(Patient a, Patient b)
	{
		if (a.equals(b))
		{
			return 0;
		}
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		String nameA = a.getName();
		String nameB = b.getName();
		if (!nameA.equals(nameB))
		{
			List<String> nameList = Arrays.asList(nameA, nameB);
			Collections.sort(nameList, collator);
			return (nameList.get(0).equals(nameA)) ? -1 : 1;
		}
		int dateA = Integer.MAX_VALUE;
		int dateB = Integer.MAX_VALUE;
		try
		{
			dateA = Integer.parseInt(a.getBirthDate());
		}
		catch (NumberFormatException ex)
		{}
		try
		{
			dateB = Integer.parseInt(b.getBirthDate());
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
		Collections.sort(idList, collator);
		return (idList.get(0).equals(idA)) ? -1 : 1;
	}

	/**
	 * Natural ordering for Patients is by name, birth date, ID.
	 */
	public static final PatientComparator Natural = new PatientComparator();
}
