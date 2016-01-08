/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom.impl;

import etherj.dicom.Modality;
import etherj.dicom.SopInstance;
import etherj.dicom.DicomUtils;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * $Id$
 *
 * @author adminjamesd
 */
class DefaultSopInstance implements SopInstance
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultSopInstance.class);
	private SoftReference<DicomObject> softDcm;
	private File file;
	private int frameCount = 0;
	private String sopClassUid = "";
	private String uid = "";
	private int instanceNumber = 1;
	private String modality = "";
	private String seriesUid = "";
	private String studyUid = "";

	DefaultSopInstance(File file)
	{
		this.file = file;
		softDcm = new SoftReference<>(null);
	}

	DefaultSopInstance(File file, DicomObject dcm)
	{
		this(file, dcm, false);
	}

	DefaultSopInstance(File file, DicomObject dcm, boolean discard)
	{
		this.file = file;
		if (dcm == null)
		{
			softDcm = new SoftReference<>(null);
			return;
		}
		sopClassUid = dcm.getString(Tag.SOPClassUID);
		uid = dcm.getString(Tag.SOPInstanceUID);
		instanceNumber = dcm.getInt(Tag.InstanceNumber, 1);
		modality = dcm.getString(Tag.Modality);
		seriesUid = dcm.getString(Tag.SeriesInstanceUID);
		studyUid = dcm.getString(Tag.StudyInstanceUID);
		if (dcm.contains(Tag.NumberOfFrames))
		{
			frameCount = dcm.getInt(Tag.NumberOfFrames);
		}
		else
		{
			if (DicomUtils.isImageSopClass(sopClassUid))
			{
				frameCount = 1;
			}
		}
		/* Discard determines whether the supplied DICOM object should be retained
		* after extraction of info. Discard would be true if the caller wanted to
		* create a new SOPInstance without having to load the contents of file
		* e.g. from a database query result
		*/
		if (discard)
		{
			softDcm = new SoftReference<>(null);
		}
		else
		{
			softDcm = new SoftReference<>(dcm);
		}
	}

	@Override
	public void compact()
	{
		softDcm.clear();
		softDcm = new SoftReference<>(null);
		logger.trace("SOPInstance compacted: {}", file.getPath());
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
		System.out.println(pad+"File: "+file.getAbsolutePath());
		System.out.println(pad+"Modality: "+modality);
		System.out.println(pad+"InstanceNumber: "+instanceNumber);
		System.out.println(pad+"NumberOfFrames: "+frameCount);
		System.out.println(pad+"Uid: "+uid);
		System.out.println(pad+"SopClassUid: "+sopClassUid);
		System.out.println(pad+"SeriesUid: "+seriesUid);
		System.out.println(pad+"StudyUid: "+studyUid);
		System.out.println(pad+"Compact: "+
			(softDcm.get() == null ? "true" : "false"));
	}

	@Override
	public DicomObject getDicomObject()
	{
		return dcm();
	}

	@Override
	public File getFile()
	{
		return file;
	}

	@Override
	public int getInstanceNumber()
	{
		return instanceNumber;
	}

	@Override
	public String getModality()
	{
		return modality;
	}

	@Override
	public int getNumberOfFrames()
	{
		return frameCount;
	}

	@Override
	public String getPath()
	{
		return file.getPath();
	}

	@Override
	public Set<String> getReferencedSopInstanceUidSet()
	{
		Set<String> uids = new HashSet<>();
		DicomObject dcm = this.dcm();
		DicomElement refSq = dcm.get(Tag.ReferencedImageSequence);
		if (refSq != null)
		{
			int nItems = refSq.countItems();
			for (int i=0; i<nItems; i++)
			{
				DicomObject item = refSq.getDicomObject(i);
				uids.add(item.getString(Tag.ReferencedSOPInstanceUID));
			}
		}
		if (!modality.equals(Modality.RTSTRUCT))
		{
			return uids;
		}
		DicomElement roiContourSq = dcm.get(Tag.ROIContourSequence);
		int nRoi = roiContourSq.countItems();
		for (int i=0; i<nRoi; i++)
		{
			DicomObject roiContourItem = roiContourSq.getDicomObject(i);
			DicomElement contourSq = roiContourItem.get(Tag.ContourSequence);
			if (contourSq == null)
			{
				continue;
			}
			int nContours = contourSq.countItems();
			for (int j=0; j<nContours; j++)
			{
				DicomObject contourItem = contourSq.getDicomObject(j);
				DicomElement contourImageSq = contourItem.get(
					Tag.ContourImageSequence);
				if (contourImageSq == null)
				{
					continue;
				}
				int nContourImages = contourImageSq.countItems();
				for (int k=0; k<nContourImages; k++)
				{
					DicomObject imageItem = contourImageSq.getDicomObject(k);
					uids.add(imageItem.getString(Tag.ReferencedSOPInstanceUID));
				}
			}
		}
		return uids;
	}

	@Override
	public String getSeriesUid()
	{
		return seriesUid;
	}

	@Override
	public String getSopClassUid()
	{
		return sopClassUid;
	}

	@Override
	public String getStudyUid()
	{
		return studyUid;
	}

	@Override
	public String getUid()
	{
		return uid;
	}

	@Override
	public void setInstanceNumber(int number)
	{
		instanceNumber = number;
	}

	@Override
	public void setModality(String modality)
	{
		this.modality = modality;
	}

	@Override
	public void setNumberOfFrames(int frameCount)
	{
		this.frameCount = frameCount;
	}

	@Override
	public void setSeriesUid(String uid)
	{
		seriesUid = uid;
	}

	@Override
	public void setSopClassUid(String uid)
	{
		sopClassUid = uid;
	}

	@Override
	public void setStudyUid(String uid)
	{
		studyUid = uid;
	}

	@Override
	public void setUid(String uid)
	{
		this.uid = uid;
	}

	protected DicomObject dcm()
	{
		DicomObject dcm = softDcm.get();
		if (dcm == null)
		{
			try
			{
				dcm = DicomUtils.readDicomFile(file);
				softDcm = new SoftReference<>(dcm);
				logger.trace("Lazy load of SOPInstance: {}", file.getPath());
			}
			catch (IOException exIO)
			{
				logger.error(
					"Cannot reload soft reference target: {}", file.getPath(),
					exIO);
			}
		}
		return dcm;
	}

}
