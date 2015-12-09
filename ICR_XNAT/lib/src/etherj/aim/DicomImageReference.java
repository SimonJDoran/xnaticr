/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

/**
 *
 * @author jamesd
 */
public class DicomImageReference extends ImageReference
{
	private ImageStudy study;

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"UID: "+uid);
		study.display(indent+"  ", recurse);
	}

	/**
	 * @return the study
	 */
	public ImageStudy getStudy()
	{
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(ImageStudy study)
	{
		this.study = study;
	}

}
