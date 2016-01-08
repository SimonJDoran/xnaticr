/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Modality;
import etherj.dicom.Series;
import etherj.dicom.SeriesComparator;
import etherj.dicom.SopInstance;
import etherj.dicom.Study;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author jamesd
 */
class DefaultStudy implements Study
{
	private String accession = "";
	private String date = "";
	private String desc = "";
	private String id = "";
	private long modality = 0;
	private String uid = "";
	private final Map<String,Series> seriesMap = new HashMap<>();

	DefaultStudy(SopInstance sopInst)
	{
		DicomObject dcm = sopInst.getDicomObject();
		String value = dcm.getString(Tag.AccessionNumber);
		if (value != null)
		{
			accession = value;
		}
		date = dcm.getString(Tag.StudyDate);
		value = dcm.getString(Tag.StudyDescription);
		if (value != null)
		{
			desc = value;
		}
		value = dcm.getString(Tag.StudyID);
		if (value != null)
		{
			id = value;
		}
		uid = dcm.getString(Tag.StudyInstanceUID);
	}

	DefaultStudy(String uid)
	{
		this.uid = uid;
	}
	
	@Override
	public Series addSeries(Series series)
	{
		// Update the study's modality bitmask
		modality |= Modality.bitmask(series.getModality());

		return seriesMap.put(series.getUid(), series);
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
		System.out.println(pad+"Date: "+date);
		System.out.println(pad+"Id: "+id);
		System.out.println(pad+"Modality: "+getModality());
		System.out.println(pad+"Description: "+desc);
		System.out.println(pad+"Accession: "+accession);
		System.out.println(pad+"Uid: "+uid);
		System.out.println(pad+"SeriesList: "+seriesMap.size()+" series");
		List<Series> seriesList = getSeriesList();
		for (Series series : seriesList)
		{
			series.display(indent+"  ");
		}
	}

	@Override
	public String getAccession()
	{
		return accession;
	}

	@Override
	public String getDate()
	{
		return date;
	}

	@Override
	public String getDescription()
	{
		return desc;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getModality()
	{
		return Modality.allStrings(modality);
	}

	@Override
	public Series getSeries(String uid)
	{
		return seriesMap.get(uid);
	}

	@Override
	public List<Series> getSeriesList()
	{
		List<Series> seriesList = new ArrayList<>();
		Set<Map.Entry<String,Series>> entries = seriesMap.entrySet();
		Iterator<Map.Entry<String,Series>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,Series> entry = iter.next();
			seriesList.add(entry.getValue());
		}
		Collections.sort(seriesList, SeriesComparator.Natural);

		return seriesList;
	}

	@Override
	public String getUid()
	{
		return uid;
	}

	@Override
	public boolean hasSeries(String uid)
	{
		return seriesMap.containsKey(uid);
	}

	@Override
	public Series removeSeries(String uid)
	{
		Series removed = seriesMap.remove(uid);
		// Recompute the study's modality bitmask
		modality = 0;
		Set<Entry<String,Series>> entries = seriesMap.entrySet();
		for (Entry<String,Series> entry : entries)
		{
			modality |= Modality.bitmask(entry.getValue().getModality());
		}

		return removed;
	}
	
	@Override
	public void setAccession(String accession)
	{
		this.accession = accession;
	}

	@Override
	public void setDate(String date)
	{
		this.date = date;
	}

	@Override
	public void setDescription(String description)
	{
		this.desc = description;
	}

	@Override
	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public void setUid(String uid)
	{
		this.uid = uid;
	}

}
