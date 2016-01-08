/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.RtRoi;
import etherj.dicom.RtStruct;
import etherj.dicom.DicomToolkit;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

/**
 *
 * @author jamesd
 */
class DefaultRTStruct implements RtStruct
{
	private final DicomObject dcm;

	DefaultRTStruct(DicomObject dcm)
	{
		String sopClassUid = dcm.getString(Tag.SOPClassUID);
		if ((sopClassUid == null) ||
			 !sopClassUid.equals(UID.RTStructureSetStorage))
		{
			throw new IllegalArgumentException(
				"Requires SOP Class of RTStructureSetStorage, found: "+sopClassUid);
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
		System.out.println(pad+"StructureSetLabel: "+getStructureSetLabel());
		String value = getStructureSetName();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"StructureSetName: "+value);
		}
		value = getStructureSetDescription();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"StructureSetDescription: "+value);
		}
		value = getStructureSetDate();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"StructureSetDate: "+value);
		}
		value = getStructureSetTime();
		if ((value != null) && !value.isEmpty())
		{
			System.out.println(pad+"StructureSetTime: "+value);
		}
		System.out.println(pad+"RoiCount: "+getRoiCount());
		if (recurse)
		{
			List<RtRoi> roiList = getRoiList();
			for (RtRoi roi : roiList)
			{
				roi.display(indent+"  ", true);
			}
		}
	}

	@Override
	public DicomObject getDicomObject()
	{
		return dcm;
	}

	@Override
	public int getRoiCount()
	{
		return dcm.get(Tag.StructureSetROISequence).countItems();
	}

	@Override
	public List<RtRoi> getRoiList()
	{
		List<RtRoi> rois = new ArrayList<>();
		DicomToolkit toolkit = DicomToolkit.getToolkit();
		DicomElement ssrSq = dcm.get(Tag.StructureSetROISequence);
		DicomElement roiSq = dcm.get(Tag.ROIContourSequence);
		int nRoi = getRoiCount();
		for (int i=0; i<nRoi; i++)
		{
			RtRoi roi = toolkit.createRtRoi(ssrSq.getDicomObject(i),
				roiSq.getDicomObject(i));
			rois.add(roi);
		}
		return rois;
	}

	@Override
	public String getStructureSetDescription()
	{
		return dcm.getString(Tag.StructureSetDescription);
	}

	@Override
	public String getStructureSetDate()
	{
		return dcm.getString(Tag.StructureSetDate);
	}

	@Override
	public String getStructureSetLabel()
	{
		return dcm.getString(Tag.StructureSetLabel);
	}

	@Override
	public String getStructureSetName()
	{
		return dcm.getString(Tag.StructureSetName);
	}

	@Override
	public String getStructureSetTime()
	{
		return dcm.getString(Tag.StructureSetTime);
	}

}
