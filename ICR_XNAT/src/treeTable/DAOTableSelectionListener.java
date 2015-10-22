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
* Java class: DAOTableSelectionListener.java
* First created on Apr 22, 2010 at 4:29:11 PM
* 
* Selection listener for JTable (from which netbeans.Outline is
* extended) that ensures only leaf elements of the tree table can
* be selected.
*********************************************************************/

package treeTable;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DAOTableSelectionListener implements ListSelectionListener
{
   // We need to store the treeTable reference since the event's source
   // is a component nested several layers inside the treeTable and it
   // is thus not easy to retrieve the treeTable itself from the event.
   DAOTreeTable        treeTable;
   DAOOutline          outline;
   ListSelectionModel  model;
   boolean             ignoreCall;

   DAOTableSelectionListener(DAOTreeTable treeTable)
   {
      this.treeTable = treeTable;
      outline    = treeTable.getOutline();
      model      = outline.getSelectionModel();
      ignoreCall = false;
   }

   @Override
   public void valueChanged(ListSelectionEvent e)
   {
      if (e.getValueIsAdjusting())
      {
         // The mouse button has not yet been released - do nothing.
         return;
      }

      if (ignoreCall)
      {
         // This variable is set to prevent endless recursion, because
			// later in the method, we may alter the user's selection to
			// remove non-leaf elements.
         return;
      }


      // If cell selection is enabled, both row and column change events are fired
      if (e.getSource() == outline.getColumnModel().getSelectionModel()
                        && outline.getColumnSelectionAllowed())
      {
         // Column selection changed
         int first = e.getFirstIndex();
         int last  = e.getLastIndex();

         // Left for future expansion.
         return;
      }

      else if (e.getSource() == model && outline.getRowSelectionAllowed())
      {
         // Modify the selection to remove all null or non-leaf rows.
			int[] selTableRows = outline.getSelectedRows();
			ignoreCall = true;
			
			for (int i=0; i<selTableRows.length; i++)
         {
				int row = selTableRows[i];
            DAOMutableTreeNode nodeForRow = (DAOMutableTreeNode) outline.getValueAt(row, 0);
            if (nodeForRow == null) outline.removeRowSelectionInterval(row, row); 
				else if (!nodeForRow.isLeaf()) outline.removeRowSelectionInterval(row, row);
         }
			
			ignoreCall = false;

			selTableRows       = outline.getSelectedRows();
			int[] selModelRows = new int[selTableRows.length];		
			for (int i=0; i<selTableRows.length; i++)
				selModelRows[i] = outline.convertRowIndexToModel(selTableRows[i]);
			
			treeTable.fireRowSelectionChanged(selModelRows);
      }

   }

}
