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
public abstract class Markup extends Entity
{
	public static final String TwoDimensionCircle = "etherj.aim.TwoDimensionCircle";
	public static final String TwoDimensionEllipse = "etherj.aim.TwoDimensionEllipse";
	public static final String TwoDimensionMultiPoint = "etherj.aim.TwoDimensionMultiPoint";
	public static final String TwoDimensionPoint = "etherj.aim.TwoDimensionPoint";
	public static final String TwoDimensionPolyline = "etherj.aim.TwoDimensionPolyline";
	private static final int IntTwoDimensionCircle = 0;
	private static final int IntTwoDimensionEllipse = 1;
	private static final int IntTwoDimensionMultiPoint = 2;
	private static final int IntTwoDimensionPoint = 3;
	private static final int IntTwoDimensionPolyline = 4;
	private static final Map<Integer,String> intToName = new HashMap<>();
	private static final Map<String,Integer> nameToInt = new HashMap<>();

	protected int dimensionCount = 0;

	static
	{
		intToName.put(IntTwoDimensionCircle, TwoDimensionCircle);
		intToName.put(IntTwoDimensionEllipse, TwoDimensionEllipse);
		intToName.put(IntTwoDimensionMultiPoint, TwoDimensionMultiPoint);
		intToName.put(IntTwoDimensionPoint, TwoDimensionPoint);
		intToName.put(IntTwoDimensionPolyline, TwoDimensionPolyline);

		nameToInt.put(TwoDimensionCircle, IntTwoDimensionCircle);
		nameToInt.put(TwoDimensionEllipse, IntTwoDimensionEllipse);
		nameToInt.put(TwoDimensionMultiPoint, IntTwoDimensionMultiPoint);
		nameToInt.put(TwoDimensionPoint, IntTwoDimensionPoint);
		nameToInt.put(TwoDimensionPolyline, IntTwoDimensionPolyline);
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

	/**
	 * @return the dimensionCount
	 */
	public int getDimensionCount()
	{
		return dimensionCount;
	}

}
