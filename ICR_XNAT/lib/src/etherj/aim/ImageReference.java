/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jamesd
 */
public abstract class ImageReference extends Entity
{
	public static final String UriImageReference = "etherj.aim.UriImageReference";
	public static final String DicomImageReference = "etherj.aim.DicomImageReference";
	private static final int IntUriImageReference = 0;
	private static final int IntDicomImageReference = 1;
	private static final Map<Integer,String> intToName = new HashMap<>();
	private static final Map<String,Integer> nameToInt = new HashMap<>();
	
	static
	{
		intToName.put(IntUriImageReference, UriImageReference);
		intToName.put(IntDicomImageReference, DicomImageReference);

		nameToInt.put(UriImageReference, IntUriImageReference);
		nameToInt.put(DicomImageReference, IntDicomImageReference);
	}

	public static int getClassCode(String name)
	{
		Integer value = nameToInt.get(name);
		return (value == null) ? -1 : value;
	}

	public static String getClassName(int code)
	{
		return intToName.get(code);
	}

}
