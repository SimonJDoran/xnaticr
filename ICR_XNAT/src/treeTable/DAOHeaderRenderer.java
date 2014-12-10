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
* Java class: DAOHeaderCellRenderer.java
* First created on April 21, 2010, 11:48 PM
* 
* Cell renderer based on a combo box, allowing the user to choose what
* is displayed in the table.
*********************************************************************/

package treeTable;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import configurationLists.DAOSearchableElementsList;

public class DAOHeaderRenderer extends JComboBox implements TableCellRenderer
{
   public DAOHeaderRenderer(String searchRootElement)
   {
      DAOSearchableElementsList sel;
      try
      {
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         throw new RuntimeException(exIO.getMessage());
      }
      LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATAliases();
		setModel(new DefaultComboBoxModel(map.get(searchRootElement)));
   }
   /**
    * Implement required method for TableCellRenderer. Return a suitably
    * customised component to provide the header.
    * @param table
    * @param value
    * @param isSelected - for a header, this is always false.
    * @param hasFocus - for a header, this is always false.
    * @param row - for a header, this is always -1.
    * @param col
    * @return Swing Component implementing the relevant rendering
    */
   @Override
   public Component getTableCellRendererComponent(JTable  table,
                                                  Object  value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int     row,
                                                  int     col)
   {
        return this;
   }
}
