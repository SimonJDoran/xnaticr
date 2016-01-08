/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;

/**
 *
 * @author jamesd
 */
public class TwoDimensionCoordinate implements Displayable
{
	private final int index;
	private final double x;
	private final double y;

	public TwoDimensionCoordinate(int index, double x, double y)
	{
		this.index = index;
		this.x = x;
		this.y = y;
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
		System.out.println(pad+"Index: "+index);
		System.out.println(pad+"X: "+x);
		System.out.println(pad+"Y: "+y);
	}

	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return the x coordinate
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return the y coordinate
	 */
	public double getY()
	{
		return y;
	}
}
