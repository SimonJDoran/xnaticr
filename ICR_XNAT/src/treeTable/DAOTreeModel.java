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
* Java class: DAOTreeModel.java
* First created on Apr 12, 2010 at 1:26:55 PM
* 
* Model for the tree part of the TreeTable used to display the
* database entries.
*********************************************************************/

package treeTable;

import generalUtilities.UIDGenerator;
import generalUtilities.SimpleColourTable;
import imageUtilities.DownloadIcon;
import java.awt.Color;
import java.io.IOException;
import java.util.Vector;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.apache.log4j.Logger;



public class DAOTreeModel extends DefaultTreeModel
{
   static  Logger                       logger = Logger.getLogger(DAOTreeModel.class);
	protected String[]                   columnNames;
	protected Class[]	                   columnTypes;
	int                                  nTreeCols;
   int                                  nTableCols;
   protected DAOMutableTreeNode         rootNode;
   protected static int                 ICON_SIZE = 16;
   public static final String           FETCHING = "Please wait. Fetching data...";
   public static final String           NO_DATA  = "No valid data";
   public static final String           ARTIFICIAL_UID = " (UID artificially created)";
   private static DAOIconRenderingsList irl;
   private static String                rootToolTip;
   private static Color                 rootIconColour;
   private static char                  rootIconLetter;
   private static String[]              rootTableData;
   private static DownloadIcon          rootIcon;
   static
   {
      try
      {
         irl = DAOIconRenderingsList.getSingleton();
      }
      catch (IOException exIO)
      {
         throw new RuntimeException(exIO.getMessage());
      }
      
      // This part of the static block is here only because I can't put the
      // code before the call to super().
      rootIconLetter = irl.getIconInfoProperties().get(NO_DATA).elementAt(0).charAt(0);
      String colour  = irl.getIconInfoProperties().get(NO_DATA).elementAt(1);
      rootIconColour = SimpleColourTable.getColour(colour);
      rootToolTip    = irl.getIconInfoProperties().get(NO_DATA).elementAt(2);
      rootTableData  = new String[1];
      rootTableData[0] = NO_DATA;
      rootIcon       = new DownloadIcon(ICON_SIZE, rootIconLetter, rootIconColour);
   }

   /** This is the constructor that builds the initial version of the TreeTable.
	 *  As soon as a valid database query is executed, root will change to be
	 *  the database from which the image data are drawn and both the tree and
	 *  table will be populated.
	 */
	public DAOTreeModel()
	{
      super(new DAOMutableTreeNode(
				  new DAOTreeNodeUserObject(
                NO_DATA,
                NO_DATA,
                rootToolTip,
                rootIcon,
                rootTableData)));
      rootNode = (DAOMutableTreeNode) getRoot();
	}


   /**
    * Fill the tree model with data obtained from a select query
	 * on the database. So that only a single query is made to the database, all
	 * the data for both the tree and the table are contained in a single array.
	 * The table data are put into the DAOTreeNodeUserObject, because the RowModel
    * needs to be able to get at the table just from the node.
	 *
	 * The total number of columns/depth levels in the tree is nTreeCols.
	 * This is made up of 1 column representing the root database, plus
	 * DBSearchCriteria.getNumberOfDistinctFields()) columns representing the
	 * search fields, plus a final column of leaves.
	 *
    * The number of rows in the input data is the total number of rows
    * returned from the database, which is not necessarily the same as
    * the number of rows displayed. The tree actually has one column for the
    * tree root plus one for each of the distinct fields in the database query.
    * However, the root is not included in nTreeCols.
	 *
	 * The parameter treeTableData has nTreeCols-1 columns, since we do not need
	 * to repeat root for every entry.
	 */

	public void populateTreeModel(String[][]     treeTableData,
                                 Vector<String> treeColTypes,
                                 boolean        isLazySearch)
	{
      /* Every time a new node is inserted into the tree, the tree expansion
		 * listener is notified. During the initial tree-building phase, we don't
		 * want to trigger a table recalculation on each of these occasions.
		 * The instance variable noTableRecalculate is set here and used in
		 * populateTableModel.
		 */
      String IRL_ROOT_NAME = "XNAT database";

		int nRows = treeTableData.length;
		boolean newNodeNeeded;

		// Before we start, erase the old model.
		rootNode.removeAllChildren();
      
      // Setup the new root node user object with text, icon and tooltip.
      char     rIconLetter  = irl.getIconInfoProperties().get(IRL_ROOT_NAME).elementAt(0).charAt(0);
      String   rColour      = irl.getIconInfoProperties().get(IRL_ROOT_NAME).elementAt(1);
      Color    rIconColour  = SimpleColourTable.getColour(rColour);
      String   rToolTip     = irl.getIconInfoProperties().get(IRL_ROOT_NAME).elementAt(2);
      String[] rTableData   = new String[nTableCols];
      for (int i=0; i<nTableCols; i++) rTableData[i] = "";
      DownloadIcon rIcon = new DownloadIcon(ICON_SIZE, rIconLetter, rIconColour);

      rootNode.setUserObject(new DAOTreeNodeUserObject(
                    IRL_ROOT_NAME, IRL_ROOT_NAME, rToolTip, rIcon, rTableData));

		DAOMutableTreeNode[] curColNode = new DAOMutableTreeNode[nTreeCols];
		int[] colNodeCount = new int[nTreeCols-1];
		for (int i=0; i<nTreeCols-1; i++) colNodeCount[i] = 0;

		for (int j=0; j<nRows; j++)
		{
         int maxTreeCol;
         maxTreeCol = isLazySearch ? 1 : nTreeCols-1;
			for (int i=0; i<maxTreeCol; i++)
			{
            boolean isLeaf         = (i == nTreeCols-2);
            boolean needsDummyLeaf = (i == maxTreeCol-1 && isLazySearch);
				newNodeNeeded = true;
				if (curColNode[i] != null)
				{
               DAOTreeNodeUserObject uo = curColNode[i].getUserObject();
					if (uo.getDisplayName().equals(treeTableData[j][i]))
                  newNodeNeeded = false;
				}

				if (newNodeNeeded)
				{
               String nodeName = treeTableData[j][i];
               String leafName = isLeaf ? treeTableData[j][i+1] : "";
               String nodeType = isLeaf ? treeColTypes.elementAt(i+1)
                                        : treeColTypes.elementAt(i);

               // Select the tableData from the data columns after the tree, but
               // only for leaf nodes.
               String[] tableData = new String[nTableCols];
               if (isLeaf)
                  for (int k=0; k<nTableCols; k++) tableData[k] = treeTableData[j][nTreeCols+k];

               // Retrieve the tooltip and create icons from the render list.
               Vector<String>  nodeProperties = irl.getIconInfoProperties().get(nodeType);
               char            letter         = nodeProperties.elementAt(0).charAt(0);
               Color           colour         = SimpleColourTable.getColour(nodeProperties.elementAt(1));
               String          nodeToolTip    = nodeProperties.elementAt(2);
               DownloadIcon    nodeIcon       = new DownloadIcon(ICON_SIZE, letter, colour);

               curColNode[i] = new DAOMutableTreeNode(new DAOTreeNodeUserObject(
                         nodeName, leafName, nodeToolTip, nodeIcon, tableData));

					if (i == 0)
						this.insertNodeInto(curColNode[i], rootNode, colNodeCount[i]++);
					else
						this.insertNodeInto(curColNode[i], curColNode[i-1], colNodeCount[i]++);

					if (needsDummyLeaf)
               {
                  nodeName       = FETCHING;
                  leafName       = FETCHING;
                  nodeType       = FETCHING;
                  nodeProperties = irl.getIconInfoProperties().get(nodeType);
                  letter         = nodeProperties.elementAt(0).charAt(0);
                  colour         = SimpleColourTable.getColour(nodeProperties.elementAt(1));
                  nodeToolTip    = nodeProperties.elementAt(2);
                  nodeIcon       = new DownloadIcon(ICON_SIZE, letter, colour);
                  tableData      = new String[nTableCols];
                  
                  DAOMutableTreeNode dummyLeaf = new DAOMutableTreeNode(
                     new DAOTreeNodeUserObject(nodeName, leafName, nodeToolTip, nodeIcon, tableData)); 
                  this.insertNodeInto(dummyLeaf, curColNode[i], 0);
               }
               
               /* Reset the number of children of this node to zero. Otherwise
					 * on the next iteration of i, we would try to add the children
					 * at the index reached for the last child of the previous node
					 * of this level. */
					for (int k=i+1; k<nTreeCols-1; k++)
					{
						colNodeCount[k]	= 0;
						curColNode[k]		= null;
					}    
				}
			}
		}
//		noTableRecalculate = false;
		this.nodeStructureChanged(root);
	}
   
   
   /**
    * Modify the Tree Table to reflect changes made during the latest iteration of
    * the lazy search. Notice that this is a significantly easier task than
    * populating the complete tree from scratch, as all we have to do is populate
    * a single level, given the parent node path.
    * @param treeTableData a 2D String array, containing the data with which to
    * fill both the tree and table (if this is a leaf)
    * @param treeColTypes a list of the column types for the tree and the table
    * @param expansionNodePath a DAOMutableTreeNode array describing the path to
    * the node that is being expanded
    */
   public void populateTreeModelNode(String[][]           treeTableData,
                                     Vector<String>       treeColTypes,
                                     TreeNode[]           expansionNodePath)
   {
      int                   i      = expansionNodePath.length-1;
      boolean               isLeaf = (i == nTreeCols-2);
      DAOMutableTreeNode    nd     = (DAOMutableTreeNode) expansionNodePath[i];
      DAOTreeNodeUserObject uo     = nd.getUserObject();
      DAOMutableTreeNode    child  = (DAOMutableTreeNode) nd.getFirstChild();
      
      // The original first child needs to be deleted, because it was set at the
      // previous stage of the lazy search to be FETCHING.
      nd.remove(child);
  
      for (int j=0; j<treeTableData.length; j++)
		{         
         boolean newNodeNeeded;
         if (j == 0) newNodeNeeded = true;
         else
         {
            newNodeNeeded = true;
            for (int k=0; k<nd.getChildCount(); k++)
            {
               DAOMutableTreeNode ch = (DAOMutableTreeNode) nd.getChildAt(k);
               if (ch.getUserObject().getDisplayName().equals(treeTableData[j][i]))
                  newNodeNeeded = false;
            }
         }

         if (newNodeNeeded)
         {
            String nodeName = treeTableData[j][i];
            String leafName = isLeaf ? treeTableData[j][i+1] : "";
            String nodeType = isLeaf ? treeColTypes.elementAt(i+1)
                                       : treeColTypes.elementAt(i);

            // Select the tableData from the data columns after the tree, but
            // only for leaf nodes.
            String[] tableData = new String[nTableCols];
            if (isLeaf)
               for (int k=0; k<nTableCols; k++) tableData[k] = treeTableData[j][nTreeCols+k];

            // If 
            if (isLeaf && nodeName.equals(""))
               nodeName = UIDGenerator.createNewDicomUID(UIDGenerator.XNAT_DAO,
                                                         UIDGenerator.ORIGINAL_UID_UNAVAILABLE,
                                                         UIDGenerator.GENERIC_DATA_TYPE)
                                                         + ARTIFICIAL_UID;
            // Retrieve the tooltip and create icons from the render list.
            Vector<String>  nodeProperties = irl.getIconInfoProperties().get(nodeType);
            char            letter         = nodeProperties.elementAt(0).charAt(0);
            Color           colour         = SimpleColourTable.getColour(nodeProperties.elementAt(1));
            String          nodeToolTip    = nodeProperties.elementAt(2);
            DownloadIcon    nodeIcon       = new DownloadIcon(ICON_SIZE, letter, colour);

            child = new DAOMutableTreeNode(new DAOTreeNodeUserObject(
                         nodeName, leafName, nodeToolTip, nodeIcon, tableData));
            nd.add(child);

            if (!isLeaf)
            {
               nodeName       = FETCHING;
               leafName       = FETCHING;
               nodeType       = FETCHING;
               nodeProperties = irl.getIconInfoProperties().get(nodeType);
               letter         = nodeProperties.elementAt(0).charAt(0);
               colour         = SimpleColourTable.getColour(nodeProperties.elementAt(1));
               nodeToolTip    = nodeProperties.elementAt(2);
               nodeIcon       = new DownloadIcon(ICON_SIZE, letter, colour);
               tableData      = new String[nTableCols];

               DAOMutableTreeNode dummyLeaf = new DAOMutableTreeNode(
                                                new DAOTreeNodeUserObject(
                         nodeName, leafName, nodeToolTip, nodeIcon, tableData)); 
               child.add(dummyLeaf);
            }
         }        
      }
      
   }
   
   
   public void traverseTreeChangingLeaves(DAOMutableTreeNode nd, String leafXPath,
                                          int leafIndex)
   {
      DAOTreeNodeUserObject uo = (DAOTreeNodeUserObject) nd.getUserObject();
      if (nd.isLeaf() && !uo.getDisplayName().equals(DAOTreeModel.FETCHING)
                      && !uo.getDisplayName().equals(NO_DATA) )
      {
         // We already have all the data we need. It is just a case of replacing
         // the icon and changing the leaf data for the value from the
         // appropriate data column.
         logger.debug("Leaf XPath = " + leafXPath);
         Vector<String> nodeProperties = irl.getIconInfoProperties().get(leafXPath);
         char           letter         = nodeProperties.elementAt(0).charAt(0);
         Color          colour         = SimpleColourTable.getColour(nodeProperties.elementAt(1));
         String         nodeToolTip    = nodeProperties.elementAt(2);
         uo.icon     = new DownloadIcon(ICON_SIZE, letter, colour);
         uo.leafName = uo.tableData[leafIndex];
         uo.toolTip  = nodeToolTip;
      }
      else
         for (int i=0; i<nd.getChildCount(); i++)
            traverseTreeChangingLeaves((DAOMutableTreeNode) nd.getChildAt(i),
                                        leafXPath, leafIndex);
   }
   


   public DAOMutableTreeNode getRootNode()
   {
      return rootNode;
   }
   

   public int getTreeColumnCount()
   {
      return nTreeCols;
   }


   public void setTreeColumnCount(int nTreeCols)
   {
      this.nTreeCols = nTreeCols;
   }


   public void setTableColumnCount(int nTableCols)
   {
      this.nTableCols = nTableCols;
   }
}
