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
* Java class: ColouredCellRenderer.java
* First created on Apr 1, 2010 at 2:28:50 PM
* 
* Display a list with a number of colours, corresponding to elements
* that have different meanings. Typically, a use for two colours would
* be a for a JComboBox with selectable and non-selectable (greyed-out)
* elements. A use I have for four colours is to display databases that
* are connected (green), not connected (red), not available for
* selection (grey) with several elements at the end representing
* different actions (black).
*********************************************************************/

package generalUtilities;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class ColouredCellRenderer<E> implements ListCellRenderer
{
   protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
   protected ArrayList<ArrayList<E>> elementLists;
   protected Color[]                 colours;
   

   public ColouredCellRenderer(Color... colours)
   {
      this.colours = colours;
      elementLists = new ArrayList<ArrayList<E>>();
      for (int i=0; i<colours.length; i++) elementLists.add(new ArrayList<E>());
   }
   

   @Override
   public Component getListCellRendererComponent(JList   list,
                                                 Object  value,
                                                 int     index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus)
   {
      JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
        isSelected, cellHasFocus);
      
      // Default
      if (value != null) renderer.setText(value.toString());
      renderer.setForeground(Color.BLACK);
      
      for (int i=0; i<colours.length; i++)
         if (elementLists.get(i).contains(value)) renderer.setForeground(colours[i]);

      return renderer;
   }

   
   /**
    * Set the list of elements of a particular type, to be represented in a given colour.
    * @param n an integer between 0 and number of types-1
    * @param elements a list of elements of the type specified when the renderer was created.
    */
   public void setElementsForType(int n, ArrayList<E> elements)
   {
      // Do nothing if the input type is out of range (fail silently).
      if ((n >= 0) || (n < colours.length))
         elementLists.set(n, elements);
   }
   
   
   /**
    * Get a list of the elements of the nth type
    * @param n 
    * @return an ArrayList of elements
    */
   public ArrayList<E> getElementsForType(int n)
   {
      return elementLists.get(n);
   }

 }
