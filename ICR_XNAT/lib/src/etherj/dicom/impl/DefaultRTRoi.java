/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.RtContour;
import etherj.dicom.RtRoi;
import etherj.dicom.DicomToolkit;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author jamesd
 */
class DefaultRTRoi implements RtRoi
{
	private final DicomObject roiItem;
	private final DicomObject contourItem;

	DefaultRTRoi(DicomObject roiItem, DicomObject contourItem)
	{
		if ((roiItem == null) ||
			 !roiItem.contains(Tag.ROINumber) ||
			 !roiItem.contains(Tag.ReferencedFrameOfReferenceUID))
		{
			throw new IllegalArgumentException(
				"Requires not null and ROINumber, ReferencedFrameOfReferenceUID present");
		}
		if ((contourItem == null) ||
			 !contourItem.contains(Tag.ReferencedROINumber) ||
			 (contourItem.getInt(Tag.ReferencedROINumber) != roiItem.getInt(Tag.ROINumber)))
		{
			throw new IllegalArgumentException(
				"Requires not null and ROINumbers must match");
		}
		this.roiItem = roiItem;
		this.contourItem = contourItem;
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
		System.out.println(pad+"RoiNumber: "+getRoiNumber());
		System.out.println(pad+"ReferencedFrameOfReferenceUid: "+
			getReferencedFrameOfReferenceUid());
		String value = getRoiName();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"RoiName: "+value);
		}
		value = getRoiGenerationAlgorithm();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"RoiGenerationAlgorithm: "+value);
		}
		System.out.println(pad+"CountourCount: "+getContourCount());
		if (recurse)
		{
			List<RtContour> contourList = getContourList();
			for (RtContour contour : contourList)
			{
				contour.display(indent+"  ", true);
			}
		}
	}

	@Override
	public int getContourCount()
	{
		return contourItem.get(Tag.ContourSequence).countItems();
	}

	@Override
	public List<RtContour> getContourList()
	{
		List<RtContour> contourList = new ArrayList<>();
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		DicomElement contourSq = contourItem.get(Tag.ContourSequence);
		int nContours = contourSq.countItems();
		for (int i=0; i<nContours; i++)
		{
			RtContour contour = toolkit.createRtContour(
				contourSq.getDicomObject(i));
			contourList.add(contour);
		}

		return contourList;
	}

	@Override
	public String getRoiGenerationAlgorithm()
	{
		return roiItem.getString(Tag.ROIGenerationAlgorithm);
	}

	@Override
	public String getRoiName()
	{
		return roiItem.getString(Tag.ROIName);
	}

	@Override
	public int getRoiNumber()
	{
		return roiItem.getInt(Tag.ROINumber);
	}

	@Override
	public String getReferencedFrameOfReferenceUid()
	{
		return roiItem.getString(Tag.ReferencedFrameOfReferenceUID);
	}
}
