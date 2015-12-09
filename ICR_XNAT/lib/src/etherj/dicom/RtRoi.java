/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import etherj.Displayable;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface RtRoi extends Displayable
{

	/**
	 *
	 * @return
	 */
	public int getContourCount();

	/**
	 *
	 * @return
	 */
	public List<RtContour> getContourList();

	/**
	 *
	 * @return
	 */
	public String getRoiGenerationAlgorithm();

	/**
	 *
	 * @return
	 */
	public String getRoiName();

	/**
	 *
	 * @return
	 */
	public int getRoiNumber();

	/**
	 *
	 * @return
	 */
	public String getReferencedFrameOfReferenceUid();
}
