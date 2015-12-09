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
public abstract class GeometricShape extends Markup
{
	protected String description = "";
	protected String label = "";
	protected boolean includeFlag = false;
	protected int shapeId = 0;

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 *
	 * @return
	 */
	public boolean getIncludeFlag()
	{
		return includeFlag;
	}

	/**
	 *
	 * @return
	 */
	public int getShapeId()
	{
		return shapeId;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = (description != null) ? description : "";
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = (label != null) ? label : "";
	}

	/**
	 * @param includeFlag the includeFlag to set
	 */
	public void setIncludeFlag(boolean includeFlag)
	{
		this.includeFlag = includeFlag;
	}

	/**
	 * @param shapeId the shapeId to set
	 */
	public void setShapeId(int shapeId)
	{
		this.shapeId = shapeId;
	}

}
