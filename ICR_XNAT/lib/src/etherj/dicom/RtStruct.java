/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.util.List;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author jamesd
 */
public interface RtStruct extends Displayable
{

	/**
	 *
	 * @return
	 */
	public DicomObject getDicomObject();

	/**
	 *
	 * @return
	 */
	public int getRoiCount();

	/**
	 *
	 * @return
	 */
	public List<RtRoi> getRoiList();

	/**
	 *
	 * @return
	 */
	public String getStructureSetDescription();

	/**
	 *
	 * @return
	 */
	public String getStructureSetDate();

	/**
	 *
	 * @return
	 */
	public String getStructureSetLabel();

	/**
	 *
	 * @return
	 */
	public String getStructureSetName();

	/**
	 *
	 * @return
	 */
	public String getStructureSetTime();

	
}
