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

/********************************************************************
* @author Simon J Doran
* Java class: TwoColourJTable.java
* First created on Oct 22, 2015 at 9:43:04 AM
* 
* Simple extension of JTable with alternate row colours.
*********************************************************************/

package generalUtilities;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TwoColourJTable extends JTable
{
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
      if (isColumnSelected(col) && isRowSelected(row))
      {
         comp.setBackground(SimpleColourTable.getColour("Apple Finder selected row"));
      
         if (isCellEditable(row, col))
         {
            comp.setBackground(SimpleColourTable.getColour("white"));
            comp.setForeground(SimpleColourTable.getColour("black"));
         }
      }

      return comp;
   }
}
