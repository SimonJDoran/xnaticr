/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author jamesd
 */
public abstract class TwoDimensionGeometricShape extends GeometricShape
{
	private String imageReferenceUid = "";
	private int referencedFrameNumber = 0;
	private final SortedMap<Integer,TwoDimensionCoordinate> coords = new TreeMap<>();

	public TwoDimensionGeometricShape()
	{
		dimensionCount = 2;
	}

	public TwoDimensionCoordinate addCoordinate(TwoDimensionCoordinate coord)
	{
		return coords.put(coord.getIndex(), coord);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"UID: "+uid);
		System.out.println(pad+"IncludeFlag: "+(includeFlag ? "true" : "false"));
		System.out.println(pad+"ShapeId: "+shapeId);
		System.out.println(pad+"ImageReferenceUid: "+imageReferenceUid);
		System.out.println(pad+"ReferencedFrameNumber: "+referencedFrameNumber);
		int nCoords = coords.size();
		System.out.println(pad+"CoordinateList: "+nCoords+" coordinate"+
			((nCoords != 1) ? "s" : ""));
		if (recurse)
		{
			for (TwoDimensionCoordinate coord : getCoordinateList())
			{
				coord.display(indent+"  ");
			}
		}
	}

	public TwoDimensionCoordinate getCoordinate(int index)
	{
		return coords.get(index);
	}

	public List<TwoDimensionCoordinate> getCoordinateList()
	{
		List<TwoDimensionCoordinate> list = new ArrayList<>();
		Set<Map.Entry<Integer,TwoDimensionCoordinate>> entries = coords.entrySet();
		Iterator<Map.Entry<Integer,TwoDimensionCoordinate>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<Integer,TwoDimensionCoordinate> entry = iter.next();
			list.add(entry.getValue());
		}
		return list;
	}

	/**
	 * @return the imageReferenceUid
	 */
	public String getImageReferenceUid()
	{
		return imageReferenceUid;
	}

	/**
	 * @return the referencedFrameNumber
	 */
	public int getReferencedFrameNumber()
	{
		return referencedFrameNumber;
	}

	public TwoDimensionCoordinate removeCoordinate(String uid)
	{
		return coords.remove(uid);
	}

	/**
	 * @param imageReferenceUid the imageReferenceUid to set
	 */
	public void setImageReferenceUid(String imageReferenceUid)
	{
		this.imageReferenceUid = imageReferenceUid;
	}

	/**
	 * @param referencedFrameNumber the referencedFrameNumber to set
	 */
	public void setReferencedFrameNumber(int referencedFrameNumber)
	{
		this.referencedFrameNumber = referencedFrameNumber;
	}
}
