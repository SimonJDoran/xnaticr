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

/**
 *
 * @author Simon J Doran
 *
 * Java class: DAOTableSelectionListener.java
 *
 * First created on Apr 22, 2010 at 4:29:11 PM
 *
 */


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
         ignoreCall = false;
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
         // Row selection changed - note that the standard JTable on which the
         // Outline class is based allows only contiguous selections. Code is
         // available "out there" to cater for non-contiguous, but I have not
         // yet implemented this.
         int first      = model.getMinSelectionIndex();
         int last       = model.getMaxSelectionIndex();
         int [] prevSel = outline.getPreviousValidRowSelection();
         
         // The Netbeans Outline object returns an incorrect value for the
         // selection index if a non-leaf is clicked on. My DAOOutline object
         // intercepts the correct value of the row in the case where a tree
         // expansion is occurring and quietly ignores the event.
         DAOMutableTreeNode nd = (DAOMutableTreeNode) outline.getValueAt(outline.bypassOutlineRow, 0);
         if (!nd.isLeaf())
         {
            return;
         }

         for (int j=first; j<=last; j++)
         {
            DAOMutableTreeNode nodeForRow = (DAOMutableTreeNode) outline.getValueAt(j, 0);
            if (nodeForRow == null) return;
            if (nodeForRow.isLeaf())
            {
               // Selection is OK. Notify the application object (XNAT_DAO) via
               // a listener, so that it can take appropriate action.
               treeTable.fireRowSelectionChanged(first, last);

            }
            else
            {
               // Return selection to previous state.
               ignoreCall = true;
               if (prevSel == null) model.clearSelection();
               else model.setSelectionInterval(prevSel[0], prevSel[1] );
               return;
            }
         }
         // If we get to the end of the loop, then all the rows were valid for
         // selection. Leave the Outline table with selection untouched, but
         // update the variable storing the selection.
         outline.setLastValidRowSelection(new int[]{first, last});
      }

   }

}
