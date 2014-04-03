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
* Java class: DAOTreeTable.java
* First created on Apr 12, 2010 at 1:16:42 PM
* 
* Implementation of a tree table using the Netbeans Outline class
*********************************************************************/

package treeTable;


import generalUtilities.Vector2D;
import imageUtilities.DownloadIcon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import treeTable.DAOTreeTableSettings.ColumnSettings;
import xnatDAO.DAOConstants;


public class DAOTreeTable extends JPanel
{
   static    Logger                  logger = Logger.getLogger(DAOTreeTable.class);
   protected static final int        MAX_TABLE_COLUMNS  = 8;
   protected static final int        MAX_TREE_COLUMNS   = 8;
   
   protected DAOTreeModel            treeModel;
   protected DAORowModel             rowModel;
   protected GroupLayout             panelLayout;
   protected JScrollPane             pane;
   protected int                     toAnon;
   protected ArrayList<ETableColumn> eTableCols;
   
   // This is public, because it needs to be exposed to XNATDAO.java to allow
   // the tree expansion events to be forwarded.
   public    DAOOutline              outline;
   

   public DAOTreeTable()
   {
      treeModel  = new DAOTreeModel();
      rowModel   = new DAORowModel();
      eTableCols = new ArrayList<ETableColumn>();

      setTreeColumnCount(1);
      setTableColumnCount(1);

      String[] s = {DAOTreeModel.NO_DATA};

      setColumnNames(s);

      Class[] ct = new Class[1];
      ct[0] = String.class;

      rowModel.setColumnClasses(ct);

      initialiseUI();
   }

   private void initialiseUI()
   {
      OutlineModel omdl = DefaultOutlineModel.createOutlineModel(treeModel,
                                                                 rowModel,
                                                                 true,
                                                                 "XNAT virtual directory");
      outline = new DAOOutline();
      outline.setModel(omdl);
      outline.setRenderDataProvider(new DAOTreeTableRenderDataProvider() );


      // Set the width of the tree column.
      outline.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      TableColumn col = outline.getColumnModel().getColumn(0);
      col.setPreferredWidth(160);

      pane = new JScrollPane(outline);
      pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

      panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
		this.setBackground(DAOConstants.BG_COLOUR);

      panelLayout.setHorizontalGroup(
			panelLayout.createParallelGroup()
			.addComponent(pane, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
      );

      panelLayout.setVerticalGroup(
			panelLayout.createSequentialGroup()
         .addComponent(pane, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
      );

   }

   public int getTreeColumnCount()
   {
      return treeModel.getTreeColumnCount();
   }


   public int getMaxTreeColumns()
   {
      return MAX_TREE_COLUMNS;
   }


   public int getTableColumnCount()
   {
      return rowModel.getColumnCount();
   }


   public int getMaxTableColumns()
   {
      return MAX_TABLE_COLUMNS;
   }


   public DAOOutline getOutline()
   {
      return outline;
   }
   
   
   public DAOTreeModel getTreeModel()
   {
      return treeModel;
   }


   public void setTreeColumnCount(int nTreeCols)
   {
      treeModel.setTreeColumnCount(nTreeCols);
   }


   public void setTableColumnCount(int nTableCols)
   {
      rowModel.setTableColumnCount(nTableCols);
      treeModel.setTableColumnCount(nTableCols);
//      if (outline != null)
//         outline.tableChanged(new TableModelEvent(outline.getModel()));
   }


   public void setColumnNames(String[] columnNames)
   {
      rowModel.setColumnNames(columnNames);
   }


   public void refresh(Vector2D<String>          unsortedTreeTableData,
                       Vector<String>            treeColTypes,
                       Vector<String>            tableColHeadings,
                       String                    searchRootElement,
                       String                    dataSubtypeAlias,
                       DAOTreeTableSettingsList  settingsList,
                       boolean                   isLazySearch,
                       TreeNode[]                expansionNodePath)
   {
		/* The total number of columns in the tree is the number of distinct fields
		 * in the database query, plus the root column, plus the leaf column. For
		 * the leaf column, queryResult passes the value of some form of UID that
       * gives all the leaves a unique name (typically SOPInstanceUID for DICOM) into
		 * populateTreeModel() below, but this value is not actually displayed
		 * next to the leaf icon. Instead, what is displayed is the last column
       * of the tree part of the query result, the leafDisplay field. This is the
       * reason for the -1 in the assignment of nTreeCols.
		 */
      int        nTreeCols     = treeColTypes.size();
      int        nTableCols    = tableColHeadings.size();
      String[][] treeTableData = sortData(unsortedTreeTableData);
      
      if (expansionNodePath.length == 1)
      {
         // Root node: First setup with new tree and table structure.
         setTreeColumnCount(nTreeCols);
         setTableColumnCount(nTableCols);

         Class[] ct = new Class[nTableCols];
         for (int i=0; i<nTableCols; i++) ct[i] = String.class;
         rowModel.setColumnClasses(ct);
         String[] columnNames = new String[nTableCols];
         tableColHeadings.toArray(columnNames);
         setColumnNames(columnNames);

         treeModel.populateTreeModel(treeTableData, treeColTypes, isLazySearch);

         // When the table model changes, we need to recreate the Outline from
         // scratch, as the table can't update itself.
         OutlineModel omdl = DefaultOutlineModel.createOutlineModel(
                               treeModel, rowModel, true, "XNAT virtual directory");
         outline = new DAOOutline();
         outline.setModel(omdl);
         outline.setRenderDataProvider(new DAOTreeTableRenderDataProvider() );
         outline.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         outline.setPopupUsedFromTheCorner(true);

         // These width settings are the backstop position. There should always
         // be some proper defaults (see below), but in case there is an error
         // with those, then this should always produce something that works.
         for (int i=0; i<nTableCols+1; i++)
         {
            TableColumn col = outline.getColumnModel().getColumn(i);
            eTableCols.add((ETableColumn) col);
            // The tree column has a different width from the rest.
            col.setPreferredWidth((i==0) ? 160 : 100);
         }

         // To minimise the number of calls to get data from XNAT, the table is
         // initially set up as above with columns for *all* the possible data that can be
         // displayed. Now we make only the required fields visible, using settings
         // that have previously been saved in a DAOTreeTableSettings object.
         // The "column selection corner" can then be used by the operator to select
         // different fields.
         implementSettings(settingsList, dataSubtypeAlias);



         // Add an appropriate selection listener to the JTable such that only leaf
         // elements can be selected
         outline.getSelectionModel()
               .addListSelectionListener(new DAOTableSelectionListener(this));

         JScrollPane newPane = new JScrollPane(outline);
         newPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         newPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

         panelLayout.replace(pane, newPane);
      
         // Don't forget to update instance variable pane with newPane,
         // as well as doing the actual Swing replace operation, because
         // otherwise the next time we get round here, we will be trying to
         // replace a component that no longer exists!
         pane = newPane;
      }
      if ((expansionNodePath.length > 1) && isLazySearch)
      {         
         treeModel.populateTreeModelNode(treeTableData, treeColTypes, expansionNodePath);
      
         // Generate dummy collapse and expansion events, as this seems to be
         // required to make the new nodes show up correctly.
         TreePath tp = new TreePath(expansionNodePath);
         outline.collapsePath(tp);
         outline.setDummyExpand(true);
         outline.expandPath(tp);
         outline.validate();
         outline.repaint();
         outline.setDummyExpand(false);
      }
   }
   
   public void clearTreeTable()
   {
      treeModel  = new DAOTreeModel();
      rowModel   = new DAORowModel();
      
      setTreeColumnCount(1);
      setTableColumnCount(1);

      String[] s = {DAOTreeModel.NO_DATA};

      setColumnNames(s);

      Class[] ct = new Class[1];
      ct[0] = String.class;

      rowModel.setColumnClasses(ct);
      
      OutlineModel omdl = DefaultOutlineModel.createOutlineModel(treeModel,
                                                                 rowModel,
                                                                 true,
                                                                 "XNAT virtual directory");
      outline = new DAOOutline();
      outline.setModel(omdl);
      outline.setRenderDataProvider(new DAOTreeTableRenderDataProvider());
      
      // Add an appropriate selection listener to the JTable such that only leaf
      // elements can be selected
      outline.getSelectionModel()
            .addListSelectionListener(new DAOTableSelectionListener(this));

      JScrollPane newPane = new JScrollPane(outline);
      newPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      newPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

      panelLayout.replace(pane, newPane);

      // Don't forget to update instance variable pane with newPane,
      // as well as doing the actual Swing replace operation, because
      // otherwise the next time we get round here, we will be trying to
      // replace a component that no longer exists!
      pane = newPane;
         
      // Generate dummy collapse and expansion events, as this seems to be
      // required to make the new nodes show up correctly.
      TreePath tp = new TreePath(treeModel.rootNode);
      outline.collapsePath(tp);
      outline.setDummyExpand(true);
      outline.expandPath(tp);
      outline.validate();
      outline.repaint();
      outline.setDummyExpand(false);
   }
   
   
   /**
    * Set the appropriate width and visibility of columns according to the 
    * selected table settings.
    * @param settingsList
    * @param dataSubtypeAlias
    */
   public void implementSettings(DAOTreeTableSettingsList settingsList,
                                 String dataSubtypeAlias)
   {
      DAOTreeTableSettings      settings        = settingsList.getCurrentTableSettings();
      ArrayList<ColumnSettings> subtypeSettings = settings.get(dataSubtypeAlias);
      ETableColumnModel         tcm             = (ETableColumnModel) outline.getColumnModel();

      // If the table has not yet been populated, then we cannot implement our
      // settings.
      if (tcm.getColumnCount() == 2)
      {
         TableColumn tc = tcm.getColumn(1);
         if (tc.getIdentifier().toString().equals(DAOTreeModel.NO_DATA)) return;
      }

      // Start out with none of the columns hidden.
      for (ETableColumn itc : eTableCols) tcm.setColumnHidden(itc, false);



      // There is still a chance that the defaults have not yet been set up to
      // cater for the chosen data subtype. In this case, just use the backstop
      // defined above and skip this section.
      if (subtypeSettings != null)
      {
         // Set the width, visibility and ordering of all the columns in the settings.
         int visibleCount = 0;

         for (ColumnSettings ics : subtypeSettings)
         {
            TableColumn tc = tcm.getColumn(tcm.getColumnIndex(ics.identifier));
            tc.setPreferredWidth(ics.width);

            // Move the column from its initial position to the one decided by
            // the ordering in the DAOTreeTableSettings object.
            int colInd = tcm.getColumnIndex(ics.identifier);
            tcm.moveColumn(colInd, visibleCount++);
         }

         // By now, colCount should contain the total number of visible columns in
         // subTypeSettings. We now need to iterate through all the other columns and
         // make them invisible. The rather convoluted way of doing this occurs because
         // the enumeration changes dynamically, so if we try to hide columns while
         // looping round the enumeration's elements, it changes the number of elements
         // over which the loop will occur.
         Enumeration<TableColumn> tcEnum        = tcm.getColumns();
         ArrayList<ETableColumn>  hiddenColumns = new ArrayList<ETableColumn>();
         int                      nCols         = tcm.getColumnCount();
         for (int i=0; i<nCols; i++)
         {
            TableColumn tc = tcEnum.nextElement();
            if (i >= visibleCount) hiddenColumns.add((ETableColumn) tc);
         }

         for (ETableColumn itc : hiddenColumns)
         {
            tcm.setColumnHidden(itc, true);
         }
      }
      
   }



   /**
    * Unfortunately, although XNAT returns data that are sorted on the first
    * element of the tree column 0 of treeTableData, if we have one or more
    * sub-levels, consecutive entries in the columns 1 to nTreeCols-1 are not
    * ordered. This routine swaps the rows of the data around in order to
    * achieve this. The comparison method is just like that for lexicographically
    * comparing words in English. In that case, we move through the words, comparing
    * corresponding letters. In this case, regard
    * the set of rows as a set of "words", where each "letter" in a word
    * corresponds to element(i) of the row, which is a String.
    * Corresponding "letters" in the "words" can be compared with String.compareTo.
    * We work our way along the rows: if at any column the "letters" are different,
    * the comparison ends straight away; if the "letters" are the same, then we
    * have to move on to the next letter to discriminate the two words.
    * @param treeTableData
    * @return sorted version of treeTableData
    */
   private String[][] sortData(Vector2D<String> dataS2DV)
                      throws ClassCastException
   {
      dataS2DV.sortRows(new Comparator<Vector<String>>()
      {
         int col;
         Vector<String> o1;
         Vector<String> o2;

         @Override
         public int compare(Vector<String> o1, Vector<String> o2)
         {
            this.o1 = o1;
            this.o2 = o2;
            if (o1.size() != o2.size())
               throw new ClassCastException("Not all rows in the Searchable2DVector have the same size.");
            col = 0;

            return compareVectors();
         }

         private int compareVectors()
         {
            int colComparison = 0;
            try
            {
               colComparison = o1.elementAt(col).compareTo(o2.elementAt(col));
            }
            catch (Exception ex)
            {
               logger.warn("Error in comparing vectors for sorting tree table.");
            }
            if (colComparison != 0) return colComparison;
            else if (col == o1.size()-1) return 0;
            else
            {
               col++;
               return compareVectors();
            }
         }
      });

      int nx = dataS2DV.getRow(0).size();
      int ny = dataS2DV.size();
      String[][] treeTableData = new String[ny][nx];
      for (int j=0; j<ny; j++)
         for (int i=0; i<nx; i++)
            treeTableData[j][i] = dataS2DV.atom(i, j);

      return treeTableData;
   }


   /**
    * Adds a listener for DAOTreeTable row selection events.
    * @param x  a <code>DAOTreeTableSelectionListener</code> object
    */
   public void addDAOTreeTableSelectionListener(DAOTreeTableSelectionListener x)
   {
      listenerList.add(DAOTreeTableSelectionListener.class, x);
   }


   /**
    * Removes a listener for for DAOTreeTable row selection events.
    * @param x  a <code>DAOTreeTableSelectionListener</code> object
    */
   public void removeDAOTreeTableSelectionListener(DAOTreeTableSelectionListener x)
   {
      listenerList.remove(DAOTreeTableSelectionListener.class, x);
   }


   /**
    * Notifies all listeners that have registered interest for
    * notification on this event type.  The event instance
    * is created using the parameters passed into the fire method.
    *
    * @param first - the start row index of the new selection
    * @param last  - the end row index
    * @see EventListenerList
    */
   protected void fireRowSelectionChanged(int first, int last)
   {
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();

      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2)
      {
         if (listeners[i]==DAOTreeTableSelectionListener.class)
         {
            DAOTreeTableSelectionEvent e
                    = new DAOTreeTableSelectionEvent(this, first, last);
            ((DAOTreeTableSelectionListener) listeners[i+1]).rowSelectionChanged(e);
         }
	   }
	}
}
