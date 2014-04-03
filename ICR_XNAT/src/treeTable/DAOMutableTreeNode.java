/********************************************************************
* Copyright (c) 2012, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: DAOMutableTreeNode.java
* First created on March 27, 2008
* 
* This class is a wrapper round DefaultMutableTreeNode, whose sole
* purpose is to modify the toString() method such that, if the node is
* a leaf, it returns a blank. The idea is that in the database model,
* each leaf element in the TreeTable is specified by the properties of
* the table, rather than an individual "name" - although the latter
* exists in the form of a UID or database primary key, we don't want
* to be forced to see this.
*********************************************************************/

package treeTable;

/**
 *
 * @author Simon J Doran
 *
 * Java class: DAOMutableTreeNode.java
 *
 * First created on March 27, 2008
 *
 */

import javax.swing.tree.DefaultMutableTreeNode;


public class DAOMutableTreeNode extends DefaultMutableTreeNode
{
	public DAOMutableTreeNode()
	{
		super();
	}


	public DAOMutableTreeNode(Object userObject)
	{
		super(userObject);
	}


	public DAOMutableTreeNode(Object userObject, boolean allowsChildren)
	{
		super(userObject, allowsChildren);
	}


	@Override
	public String toString()
	{
      // I'm not sure whether this code is ever used now.
      // The leaf node text now seems to be provided by DAOTreeTableRenderDataProvider.
		if (isLeaf()) return "";
		else return super.toString();
	}


	public String toStringEvenIfLeaf()
	{
		return super.toString();
	}
   
   
   @Override
   public DAOTreeNodeUserObject getUserObject()
   {
      return (DAOTreeNodeUserObject) super.getUserObject();
   }
}
