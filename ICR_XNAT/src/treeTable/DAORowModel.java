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
* Java class: DAORowModel.java
* First created on Apr 12, 2010 at 1:16:42 PM
* 
* Model for the table part of the TreeTable used to display the
* database entries.
*********************************************************************/

package treeTable;

import org.netbeans.swing.outline.RowModel;
import treeTable.DAOMutableTreeNode;

public class DAORowModel implements RowModel
{
	protected String[]	columnNames;
	protected Class[]		columnClasses;
   protected int        nTableCols;

   /**
    * This is the constructor that builds the initial version of the RowModel
    * for a blank tree table. As soon as a valid database query is executed,
    * root will change to be the database from which the image data are drawn
    * and both the tree and table will be populated.
	 */
   public DAORowModel()
   {
      super();
   }


   public void setTableColumnCount(int nTableCols)
   {
      this.nTableCols = nTableCols;
   }


   public void setColumnNames(String[] columnNames)
   {
      this.columnNames = columnNames;
   }


   public void setColumnClasses(Class[] columnClasses)
   {
      this.columnClasses = columnClasses;
   }


   @Override
	public Class getColumnClass(int column)
	{
		return columnClasses[column];
	}


	@Override
	public int getColumnCount()
	{
		return nTableCols;
	}


	@Override
	public String getColumnName(int column)
	{
		return columnNames[column];
	}


	@Override
	public Object getValueFor(Object node, int column)
	{
		DAOTreeNodeUserObject uo = ((DAOMutableTreeNode) node).getUserObject();
      return (uo.getTableData())[column];
	}


	@Override
	public boolean isCellEditable(Object node, int column)
	{
		return false;
	}


	@Override
	public void setValueFor(Object node, int column, Object value)
	{
		/* Neither the tree nor the table are editable, except for the column
		 * headings, which will be dealt with at a later date.
		 */
	}
}
