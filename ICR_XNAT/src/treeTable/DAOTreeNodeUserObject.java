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
* Java class: DAOTreeNodeUserObject.java
* First created on May 8, 2008
* 
* This is a utility class that allows the storage of an icon together
* with the node description for use with DBMutableTreeNode.
*********************************************************************/

package treeTable;

import imageUtilities.DownloadIcon;
import javax.swing.Icon;

public class DAOTreeNodeUserObject extends Object
{
	String   nodeName;
   String   leafName;
   String   toolTip;
	Icon     icon = null;
   String[] tableData;


	public DAOTreeNodeUserObject(String        nodeName,
                             String        leafName,
                             String        toolTip,
                             DownloadIcon  icon,
                             String[]      tableData)
	{
		this.nodeName  = nodeName;
      this.leafName  = leafName;
      this.toolTip   = toolTip;
		this.icon      = icon;
      this.tableData = tableData;
	}


	public Icon getIcon()
	{
		return icon;
	}


   public String getToolTip(boolean isLeaf)
   {
      if (!isLeaf) return toolTip + " - " + nodeName;
      
      if (leafName.equals("")) return "Leaf element '" + toolTip + "' contains no data for unique node "
              + nodeName + ".";
      else return "Leaf element displaying '" + toolTip + "' for unique node " + nodeName;
   }


	public String getDisplayName()
	{
		return nodeName;
	}
   
   
   /**
    * Normally, the name displayed in the tree is given by getDisplayName, which
    * is the node name. However, for leaves, it is important to be able to sort
    * using node name, which I force to be unique for different lines in the tree
    * table, but then to write the name described by a different column. This is
    * a fiddle that allows several leaves to have the same name. Note: this is
    * something that is normally not desirable.
    * @return a String containing the leaf name
    */
   public String getLeafName()
   {
      return leafName;
   }


   public String[] getTableData()
   {
      return tableData;
   }
}		