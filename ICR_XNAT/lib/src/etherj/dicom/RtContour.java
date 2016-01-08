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
public interface RtContour extends Displayable
{

	/**
	 *
	 * @return
	 */
	public String getContourGeometricType();

	/**
	 *
	 * @return
	 */
	public int getContourNumber();

	/**
	 *
	 * @return
	 */
	public List<Coordinate3D> getContourPointsList();

	/**
	 *
	 * @return
	 */
	public int getNumberOfContourPoints();

	/**
	 *
	 * @return
	 */
	public List<ImageReference> getImageReferenceList();
}
