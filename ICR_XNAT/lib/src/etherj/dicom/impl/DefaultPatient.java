/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Patient;
import etherj.dicom.Study;
import etherj.dicom.StudyComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author jamesd
 */
class DefaultPatient implements Patient
{
	private String birthDate;
	private String comments = "";
	private String id;
	private String name;
	private String otherId = "";
	private final Map<String,Study> studyMap = new HashMap<>();

	DefaultPatient(String name, String birthDate, String id)
	{
		this.name = (name == null) ? "" : name;
		this.birthDate = ((birthDate == null) || birthDate.isEmpty()) ? 
			"00000000" : birthDate;
		this.id = (id == null) ? "" : id;
	}

	@Override
	public Study addStudy(Study study)
	{
		return studyMap.put(study.getUid(), study);
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
		System.out.println(pad+"Name: "+name);
		System.out.println(pad+"BirthDate: "+birthDate);
		System.out.println(pad+"Id: "+id);
		System.out.println(pad+"OtherId: "+otherId);
		System.out.println(pad+"Comments: "+comments);
		int nStudies = studyMap.size();
		System.out.println(pad+"StudyList: "+nStudies+" stud"+
			(nStudies != 1 ? "ies" : "y"));
		if (recurse)
		{
			List<Study> studyList = getStudyList();
			for (Study study : studyList)
			{
				study.display(indent+"  ", true);
			}
		}
	}

	@Override
	public String getBirthDate()
	{
		return birthDate;
	}

	@Override
	public String getComments()
	{
		return comments;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getOtherId()
	{
		return otherId;
	}

	@Override
	public Study getStudy(String uid)
	{
		return studyMap.get(uid);
	}

	@Override
	public List<Study> getStudyList()
	{
		List<Study> studyList = new ArrayList<>();
		Set<Entry<String,Study>> entries = studyMap.entrySet();
		Iterator<Entry<String,Study>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Entry<String,Study> entry = iter.next();
			studyList.add(entry.getValue());
		}
		Collections.sort(studyList, StudyComparator.Natural);
		return studyList;
	}

	@Override
	public boolean hasStudy(String uid)
	{
		return studyMap.containsKey(uid);
	}

	@Override
	public Study removeStudy(String uid)
	{
		return studyMap.remove(uid);
	}

	@Override
	public void setBirthDate(String birthDate)
	{
		this.birthDate = (birthDate == null) ? "" : birthDate;
	}

	@Override
	public void setComments(String comments)
	{
		this.comments = (comments == null) ? "" : comments;
	}

	@Override
	public void setId(String id)
	{
		this.id = (id == null) ? "" : id;
	}

	@Override
	public void setName(String name)
	{
		this.name = (name == null) ? "" : name;
	}

	@Override
	public void setOtherId(String otherId)
	{
		this.otherId = (otherId == null) ? "" : otherId;
	}

}
