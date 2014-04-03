/*****************************************************************************
 *
 * SiemensDictionaryElement.java
 *
 * Simon J Doran
 *
 * First created on June 22, 2007, 12:08 PM
 *
 *****************************************************************************/

package obselete;

public class SiemensDictionaryElement
{
	
	/** Creates a new instance of SiemensDictionaryElement, which consists of:
	 *
	 *	int		variableType
	 *	int		block
	 *	int		startPosition
	 *	int		endPosition
	 *	String	description
	 */
	
	private	int		variableType;
	private	int		block;
	private	int		startPosition;
	private	int		endPosition;
	private	String	description;

	
	public SiemensDictionaryElement(int variableType, int block,
		int startPosition, int endPosition, String description)
	{
		this.variableType		= variableType;
		this.block				= block;
		this.startPosition	= startPosition;
		this.endPosition		= endPosition;
		this.description		= description;
	}
	
	int getVariableType()
	{
		return variableType;
	}
	
	
	int getBlock()
	{
		return block;
	}
	
	
	int getStartPosition()
	{
		return startPosition;
	}
	
	
	int getEndPosition()
	{
		return endPosition;
	}
	
	
	String getDescription()
	{
		return description;
	}
	
}
