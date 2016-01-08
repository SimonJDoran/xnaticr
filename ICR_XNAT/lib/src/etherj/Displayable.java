/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

/**
 * MATLAB looks for a method, display(), to output an object to the console.
 * Play nice and provide a suitable interface. Also useful for simple debugging.
 * @author jamesd
 */
public interface Displayable
{

	/**
	 *
	 */
	public void display();

	/**
	 *
	 * @param recurse
	 */
	public void display(boolean recurse);

	/**
	 *
	 * @param indent
	 */
	public void display(String indent);

	/**
	 *
	 * @param indent
	 * @param recurse
	 */
	public void display(String indent, boolean recurse);
}
