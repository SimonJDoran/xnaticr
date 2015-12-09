/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;

/**
 *
 * @author jamesd
 */
public class ImageReference implements Displayable
{
	public final String sopClassUid;
	public final String sopInstanceUid;
	public final int referencedFrameNumber;

	public ImageReference(String sopClassUid, String sopInstanceUid)
	{
		this(sopClassUid, sopInstanceUid, 0);
	}

	public ImageReference(String sopClassUid, String sopInstanceUid,
		int referencedFrameNumber)
	{
		this.sopClassUid = sopClassUid;
		this.sopInstanceUid = sopInstanceUid;
		this.referencedFrameNumber = referencedFrameNumber;
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
		System.out.println(pad+"SopClassUid: "+sopClassUid);
		System.out.println(pad+"SopInstanceUid: "+sopInstanceUid);
		if (referencedFrameNumber != 0)
		{
			System.out.println(pad+"ReferencedFrameNumber: "+referencedFrameNumber);
		}
	}
}
