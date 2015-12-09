/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.DataSource;
import etherj.dicom.DicomDatabase;
import etherj.dicom.Patient;
import etherj.dicom.PatientRoot;
import etherj.dicom.SearchCriterion;
import etherj.dicom.SearchSpecification;
import etherj.dicom.Series;
import etherj.dicom.Study;
import etherj.dicom.DicomToolkit;
import java.util.List;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
class DatabaseDataSource implements DataSource
{
	private static final Logger logger = LoggerFactory.getLogger(
		DatabaseDataSource.class);
	private final DicomDatabase db;
	private final DicomToolkit toolkit = DicomToolkit.getToolkit();

	public DatabaseDataSource(DicomDatabase db)
	{
		this.db = db;
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
		db.display(indent+"  ");
	}

	@Override
	public Series getSeries(String uid)
	{
		SearchSpecification spec = new SearchSpecification();
		SearchCriterion sc = toolkit.createSearchCriterion(Tag.SeriesInstanceUID, 
			SearchCriterion.Equal, uid);
		sc.setDicomType(SearchCriterion.Series);
		spec.addCriterion(sc);
		PatientRoot root = db.search(spec);
		List<Patient> patients = root.getPatientList();
		if (patients.isEmpty())
		{
			return null;
		}
		// Series must exist but can only exist in one Patient and one Study
		Study study = patients.get(0).getStudyList().get(0);
		return study.getSeries(uid);
	}

	@Override
	public Series getSeries(String uid, String modality)
	{
		SearchSpecification spec = new SearchSpecification();
		SearchCriterion sc = toolkit.createSearchCriterion(Tag.SeriesInstanceUID, 
			SearchCriterion.Equal, uid);
		sc.setDicomType(SearchCriterion.Series);
		spec.addCriterion(sc);
		SearchCriterion modSc = toolkit.createSearchCriterion(Tag.Modality, 
			SearchCriterion.Equal, modality);
		modSc.setDicomType(SearchCriterion.Series);
		spec.addCriterion(modSc);
		PatientRoot root = db.search(spec);
		List<Patient> patients = root.getPatientList();
		if (patients.isEmpty())
		{
			return null;
		}
		// Series must exist but can only exist in one Patient and one Study
		Study study = patients.get(0).getStudyList().get(0);
		return study.getSeries(uid);
	}

	@Override
	public Study getStudy(String uid)
	{
		SearchSpecification spec = new SearchSpecification();
		SearchCriterion sc = toolkit.createSearchCriterion(Tag.StudyInstanceUID, 
			SearchCriterion.Equal, uid);
		sc.setDicomType(SearchCriterion.Study);
		spec.addCriterion(sc);
		PatientRoot root = db.search(spec);
		List<Patient> patients = root.getPatientList();
		if (patients.isEmpty())
		{
			return null;
		}
		// Study must exist but can only exist in one Patient
		return patients.get(0).getStudy(uid);
	}
}
