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
* Java class: DAOTreeTableSettings.java
* First created on Jan 28, 2011 at 12:29 AM
* 
* DAOTreeTableSettings describes how the full tree table model will
* be displayed. Specifically, for any given data type, what selection
* of the complete set of columns will be used, what order they will
* be presented in and how wide the columns will be.
*********************************************************************/

package treeTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import treeTable.DAOTreeTableSettings.ColumnSettings;
import treeTable.DAOTreeTableSettings.SettingsForSubtype;


public class DAOTreeTableSettings extends HashMap<String, SettingsForSubtype>
{
   public class ColumnSettings
   {
      public String identifier;
      public int    jTableIndex;
      public int    modelIndex;
      public int    width;

      public ColumnSettings(String identifier, int jTableIndex, int modelIndex, int width)
      {
         this.identifier  = identifier;
         this.jTableIndex = jTableIndex;
         this.modelIndex  = modelIndex;
         this.width       = width;
      }
   }
   
   
   public class SettingsForSubtype extends ArrayList<ColumnSettings>
   {
      public String leafDisplayAlias;
      
      public SettingsForSubtype(String leafDisplayAlias)
      {
         this.leafDisplayAlias = leafDisplayAlias;
      }
      
      public String getLeafDisplayAlias()
      {
         return leafDisplayAlias;
      }
      
   }

   static Logger logger = Logger.getLogger(DAOTreeTableSettings.class);
   public String name;   

   public DAOTreeTableSettings(String name)
   {
      this.name = name;
   }
   
   
   public DAOTreeTableSettings(String name, String dataSubtype, DAOOutline outline,
                               String leafDisplayAlias)
   {
      this.name = name;    
      addSettingsForDataSubtype(dataSubtype, outline, leafDisplayAlias);
   }
   

   /**
    * Insert information on the tree table settings for a given data type
    * into the DAOTreeTableSettings object.
    * This version of the method takes the settings from those currently
    * in use in the displayed tree table.
    *
    * @param dataSubtype a String containing the name of the data type currently
    * being looked at in the tree table
    * @param outline the tree table object
    */
   public void addSettingsForDataSubtype(String dataSubtype, DAOOutline outline,
                                         String leafDisplayAlias)
   {
      SettingsForSubtype cs = new SettingsForSubtype(leafDisplayAlias);
      put(dataSubtype, cs);
      
      TableColumnModel tcm = outline.getColumnModel();
      int count = -1;
      for (Enumeration<TableColumn> e = tcm.getColumns(); e.hasMoreElements();)
      {
         count++;
         TableColumn col = e.nextElement();
         addCol(cs,
                col.getIdentifier().toString(),
                count,
                col.getModelIndex(),
                col.getWidth());
      }
   }

   
   /**
    * Insert information on the tree table settings for a given data type
    * into the DAOTreeTableSettings object.
    * This version of the method takes the settings directly from its input
    * variables.
    * 
    * @param dataSubtype a String containing the name of the data type currently
    * being looked at in the tree table
    * @param identifiers a String array containing a list of the column
    * identifiers (headings) currently being displayed
    * @param jTableIndices an int array containing a list of the column
    * positions in the displayed JTable for all the columns currently displayed
    * @param modelIndices an int array containing a corresponding list of the
    * column positions in the underlying TableModel
    * @param widths an int array containing a corresponding list of the
    * column widths in the displayed JTable
    */
   public void addSettingsForDataSubtype(String   dataSubtype,
                                         String[] identifiers,
                                         int[]    jTableIndices,
                                         int[]    modelIndices,
                                         int[]    widths,
                                         String   leafDisplayAlias)
   {
      SettingsForSubtype cs = new SettingsForSubtype(leafDisplayAlias);
      put(dataSubtype, cs);
      for (int i=0; i<identifiers.length; i++)
         addCol(cs, identifiers[i], jTableIndices[i], modelIndices[i], widths[i]);
   }
   


   public void removeSettingsForDataSubtype(String dataSubtype)
   {
      try
      {
         remove(dataSubtype);
      }
      catch (NullPointerException exNP)
      {
         // This should be safe to ignore.
         logger.warn("Tried to remove non-existent key " + dataSubtype);
      }
   }



   /**
    * Add the settings of a new column to the current descriptor. 
    * @param cs a ColumnSettings descriptor
    * @param identifier the identifier (heading) of a column being displayed
    * @param jTableIndex the index in the displayed JTable of the column
    * @param modelIndex the index in the underlying TableModel of the column
    * @param width the current display width of the column.
    */
   public void addCol(ArrayList<ColumnSettings> cs,
                      String identifier,
                      int jTableIndex,
                      int modelIndex,
                      int width)
   {
      cs.add(new ColumnSettings(identifier, jTableIndex, modelIndex, width));
   }
   
   
   public String getName()
   {
      return name;
   }
   
   
   
}
