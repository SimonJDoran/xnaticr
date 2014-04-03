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
* Java class: DAOOutline.java
* First created on Apr 20, 2010 at 2:49:40 PM
* 
* Subclass of the NetBeans Outline tree table to give extra function-
* ality, such as alternate row shading.
*********************************************************************/

package treeTable;

import generalUtilities.SimpleColourTable;
import java.awt.Component;
import java.util.EventObject;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.netbeans.swing.outline.Outline;

/**
 *
 * @author Simon J Doran
 *
 * Java class: DAOOutline.java
 * Subclass of the NetBeans Outline tree table to give extra functionality
 * such as alternate row shading.
 *
 * First created on Apr 20, 2010 at 2:49:40 PM
 *
 */


public class DAOOutline extends Outline
{
   static    Logger  logger = Logger.getLogger(DAOOutline.class);
   protected int[]   lastValidRowSelection;
   protected int     bypassOutlineRow;
   protected boolean isDummyExpand = false;

   public DAOOutline()
   {
      lastValidRowSelection = null;
      
   }



   @Override
   public Component prepareRenderer(TableCellRenderer renderer, int row, int col)
   {
      Component comp = super.prepareRenderer(renderer, row, col);

      // Even row color
      if (row % 2 == 0  && !isCellSelected(row, col)) comp.setBackground(
              SimpleColourTable.getColour("Apple Finder alternate line highlight blue"));

      // Odd row color
      if (row % 2 != 0  && !isCellSelected(row, col)) comp.setBackground(
              SimpleColourTable.getColour("white"));

      // Selected cell
      if (isCellSelected(row, col)) comp.setBackground(
              SimpleColourTable.getColour("Apple Finder selected row"));

      return comp;
   }
   
   @Override
   public boolean editCellAt(int row, int col, EventObject e)
   {
      bypassOutlineRow = row;
      return super.editCellAt(row, col, e);
   }


   public int[] getPreviousValidRowSelection()
   {
      return lastValidRowSelection;
   }


   public void setLastValidRowSelection(int[] rowSelection)
   {
      lastValidRowSelection = rowSelection;
   }
   
   
   public void setDummyExpand(boolean isDummy)
   {
      isDummyExpand = isDummy;
   }
   
   
   public boolean isDummyExpand()
   {
      return isDummyExpand;
   }

}
