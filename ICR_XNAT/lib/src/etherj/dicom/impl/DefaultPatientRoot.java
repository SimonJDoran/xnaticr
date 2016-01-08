/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Patient;
import etherj.dicom.PatientComparator;
import etherj.dicom.PatientRoot;
import etherj.dicom.DicomUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jamesd
 */
class DefaultPatientRoot implements PatientRoot
{
	private final Map<String,Patient> patientMap = new HashMap<>();

	@Override
	public Patient addPatient(Patient patient)
	{
		return patientMap.put(DicomUtils.makePatientKey(patient), patient);
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
		int nPatients = patientMap.size();
		System.out.println(pad+"PatientList: "+nPatients+" patient"+
			(nPatients == 1 ? "" : "s"));
		if (recurse)
		{
			List<Patient> patientList = getPatientList();
			for (Patient patient : patientList)
			{
				patient.display(indent+"  ", true);
			}
		}
	}

	@Override
	public Patient getPatient(String key)
	{
		return patientMap.get(key);
	}

	@Override
	public List<Patient> getPatientList()
	{
		List<Patient> patientList = new ArrayList<>();
		Set<Map.Entry<String,Patient>> entries = patientMap.entrySet();
		Iterator<Map.Entry<String,Patient>> iter = entries.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String,Patient> entry = iter.next();
			patientList.add(entry.getValue());
		}
		Collections.sort(patientList, PatientComparator.Natural);
		return patientList;
	}

	@Override
	public boolean hasPatient(String key)
	{
		return patientMap.containsKey(key);
	}

	@Override
	public Patient removePatient(String key)
	{
		return patientMap.remove(key);
	}
	
}
