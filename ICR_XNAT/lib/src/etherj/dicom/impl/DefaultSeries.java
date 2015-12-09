/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import etherj.dicom.DicomUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author jamesd
 */
class DefaultSeries implements Series
{
	private String desc = "";
	private String modality = "";
	private int number = 0;
	private String studyUid = "";
	private double time = 0;
	private String uid = "";
	private final Map<String,SopInstance> sopInstMap = new HashMap<>();

	DefaultSeries(SopInstance sopInst)
	{
		DicomObject dcm = sopInst.getDicomObject();
		if (dcm.contains(Tag.SeriesDescription))
		{
			desc = dcm.getString(Tag.SeriesDescription);
		}
		modality = dcm.getString(Tag.Modality);
		number = dcm.getInt(Tag.SeriesNumber);
		studyUid = dcm.getString(Tag.StudyInstanceUID);
		try
		{
			time = DicomUtils.tmToSeconds(dcm.getString(Tag.SeriesTime));
		}
		catch (NumberFormatException exIgnore)
		{
			time = 0;
		}
		uid = dcm.getString(Tag.SeriesInstanceUID);
	}

	DefaultSeries(String uid)
	{
		this.uid = uid;
	}

	@Override
	public SopInstance addSopInstance(SopInstance sopInstance)
	{
		return sopInstMap.put(sopInstance.getUid(), sopInstance);
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
		System.out.println(pad+"Number: "+number);
		System.out.println(pad+"Modality: "+modality);
		System.out.println(pad+"Description: "+desc);
		System.out.println(pad+"Time: "+DicomUtils.secondsToTm(time));
		System.out.println(pad+"Uid: "+uid);
		System.out.println(pad+"StudyUid: "+studyUid);
		int nInstances = sopInstMap.size();
		System.out.println(pad+"InstanceList: "+nInstances+" SOP instance"+
			((nInstances != 1) ? "s" : ""));
	}

	@Override
	public String getDescription()
	{
		return desc;
	}

	@Override
	public String getModality()
	{
		return modality;
	}

	@Override
	public int getNumber()
	{
		return number;
	}

	@Override
	public SopInstance getSopInstance(String uid)
	{
		return sopInstMap.get(uid);
	}

	@Override
	public List<SopInstance> getSopInstanceList()
	{
		List<SopInstance> sopInstList = new ArrayList<>();
		Set<Map.Entry<String,SopInstance>> entries = sopInstMap.entrySet();
		Iterator<Map.Entry<String,SopInstance>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,SopInstance> entry = iter.next();
			sopInstList.add(entry.getValue());
		}
		return sopInstList;
	}

	@Override
	public String getStudyUid()
	{
		return studyUid;
	}

	@Override
	public double getTime()
	{
		return time;
	}

	@Override
	public String getUid()
	{
		return uid;
	}

	@Override
	public boolean hasSopInstance(String uid)
	{
		return sopInstMap.containsKey(uid);
	}

	@Override
	public SopInstance removeSopInstance(String uid)
	{
		return sopInstMap.remove(uid);
	}
	
	@Override
	public void setDescription(String description)
	{
		this.desc = description;
	}

	@Override
	public void setModality(String modality)
	{
		this.modality = modality;
	}

	@Override
	public void setNumber(int number)
	{
		this.number = number;
	}

	@Override
	public void setStudyUid(String studyUid)
	{
		this.studyUid = studyUid;
	}

	@Override
	public void setTime(double time)
	{
		this.time = time;
	}

	@Override
	public void setUid(String uid)
	{
		this.uid = uid;
	}

}
