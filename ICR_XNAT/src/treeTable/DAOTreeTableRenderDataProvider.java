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
* Java class: TreeTableRenderDataProvider.java
* First created on Mar 30, 2010 at 2:54:03 PM
* 
* Return the icon renderings for different XNAT elements
* when displayed in a tree trable in a form suitable for the
* NetBeans Outline API for drawing tree tables.
*********************************************************************/


package treeTable;

import java.io.IOException;
import org.netbeans.swing.outline.RenderDataProvider;


class DAOTreeTableRenderDataProvider implements RenderDataProvider
{
   private static DAOIconRenderingsList irl;
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
   }


// Note: In all cases below, the Object o is the DAOMutableTreeNode stored
//       in the table by DAOTreeModel.populateTreeModel().

   @Override
   public java.awt.Color getBackground(Object o)
   {
      return null;
   }


   @Override
   public String getDisplayName(Object o)
   {
      DAOTreeNodeUserObject uo = ((DAOMutableTreeNode) o).getUserObject();
      
      // The user now gets the option to choose in the UI what element should be
      // user to label leaf nodes.
      if (((DAOMutableTreeNode) o).isLeaf()) return uo.getLeafName();
      else return uo.getDisplayName();
   }

   @Override
   public java.awt.Color getForeground(Object o)
   {
      return null;
   }

   @Override
   public javax.swing.Icon getIcon(Object o)
   {
      DAOTreeNodeUserObject uo = ((DAOMutableTreeNode) o).getUserObject();
      return uo.getIcon();
   }


   @Override
   public String getTooltipText(Object o)
   {
      DAOTreeNodeUserObject uo = ((DAOMutableTreeNode) o).getUserObject();
      boolean isLeaf = ((DAOMutableTreeNode) o).isLeaf();
      return uo.getToolTip(isLeaf);
   }


   @Override
   public boolean isHtmlDisplayName(Object o)
   {
      return false;
   }

}
