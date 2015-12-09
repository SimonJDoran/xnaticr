/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Coordinate3D;
import etherj.dicom.RtContour;
import etherj.dicom.ImageReference;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author jamesd
 */
class DefaultRTContour implements RtContour
{
	private final DicomObject dcm;

	DefaultRTContour(DicomObject dcm)
	{
		if (!dcm.contains(Tag.ContourNumber))
		{
			throw new IllegalArgumentException("Requires ContourNumber");
		}
		this.dcm = dcm;
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
		String subIndent = indent+"  ";
		System.out.println(pad+"ContourNumber: "+getContourNumber());
		List<ImageReference> refList = getImageReferenceList();
		System.out.println(pad+"ImageReferenceList: "+refList.size()+" reference"+
			((refList.size() != 1) ? "s" : ""));
		for (ImageReference ref : refList)
		{
			ref.display(subIndent);
		}
		System.out.println(pad+"ContourGeometricType: "+getContourGeometricType());
		System.out.println(pad+"NumberOfContourPoints: "+getNumberOfContourPoints());
		List<Coordinate3D> coords = getContourPointsList();
		for (Coordinate3D coord : coords)
		{
			coord.display(subIndent);
		}
	}

	@Override
	public int getContourNumber()
	{
		return dcm.getInt(Tag.ContourNumber);
	}

	@Override
	public String getContourGeometricType()
	{
		return dcm.getString(Tag.ContourGeometricType);
	}

	@Override
	public List<Coordinate3D> getContourPointsList()
	{
		List<Coordinate3D> coords = new ArrayList<>();
		String[] points = dcm.getStrings(Tag.ContourData);
		int nPoints = points.length;
		if (nPoints != 3*getNumberOfContourPoints())
		{
			throw new IllegalArgumentException(
				"ContourData must be complete triplets");
		}
		for (int i=0; i<nPoints; i+=3)
		{
			try
			{
				double x = Double.parseDouble(points[i]);
				double y = Double.parseDouble(points[i+1]);
				double z = Double.parseDouble(points[i+2]);
				Coordinate3D coord = new Coordinate3D(x, y, z);
				coords.add(coord);
			}
			catch (NumberFormatException ex)
			{
				throw new IllegalArgumentException("ContourData invalid");
			}
		}
		return coords;
	}

	@Override
	public int getNumberOfContourPoints()
	{
		return dcm.getInt(Tag.NumberOfContourPoints);
	}

	@Override
	public List<ImageReference> getImageReferenceList()
	{
		List<ImageReference> refs = new ArrayList<>();
		DicomElement sq = dcm.get(Tag.ContourImageSequence);
		int nItems = sq.countItems();
		for (int i=0; i<nItems; i++)
		{
			DicomObject item = sq.getDicomObject(i);
			String sopClassUid = item.getString(Tag.ReferencedSOPClassUID);
			String sopInstUid = item.getString(Tag.ReferencedSOPInstanceUID);
			int frame = 0;
			if (item.contains(Tag.ReferencedFrameNumber))
			{
				frame = item.getInt(Tag.ReferencedFrameNumber);
			}
			ImageReference ref = new ImageReference(sopClassUid, sopInstUid, frame);
			refs.add(ref);
		}
		return refs;
	}
}
