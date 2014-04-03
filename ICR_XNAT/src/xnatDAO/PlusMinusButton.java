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
* Java class: PlusMinusButton.java
* First created on April 6, 2010, 22:38 AM
* 
* Panel subcomponent for use with DAOSearchCriterion
*********************************************************************/

package xnatDAO;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


public class PlusMinusButton extends JPanel
{
   private JButton plusButton;
   private JButton minusButton;
   private String  lastButtonPushed;

   protected EventListenerList plusMinusListenerList = new EventListenerList();

   private static final Color BG_COLOUR = DAOConstants.BG_COLOUR;

   public PlusMinusButton()
   {
      initialiseUI();
      addUIListeners();
   }

   /** Set up the layout of the various GUI components. */
	private void initialiseUI()
	{
      plusButton  = new JButton("+");
      minusButton = new JButton("-");

      plusButton.setBackground(BG_COLOUR);
      minusButton.setBackground(BG_COLOUR);

      GroupLayout panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
		this.setBackground(BG_COLOUR);

      panelLayout.setHorizontalGroup(
			panelLayout.createSequentialGroup()
			.addComponent(plusButton, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(minusButton, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
      );

      panelLayout.setVerticalGroup(
			panelLayout.createParallelGroup()
         .addComponent(plusButton, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
         .addComponent(minusButton, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
      );
   }

   /** Add a listener of the appropriate type to each element of the UI.
	 *  Each listener propagates back a ChangeEvent.
	 */
	private void addUIListeners()
	{
		plusButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
            lastButtonPushed = "+";
            buttonActionPerformed(evt);
         }
      });

		minusButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
            lastButtonPushed = "-";
            buttonActionPerformed(evt);
         }
      });
	}
   
   private void buttonActionPerformed(ActionEvent evt)
   {
      fireButtonChanged(evt.getSource());
   }
   
   
   protected void fireButtonChanged(Object source)
   {
		/* Guaranteed to return a non-null array. The array that comes back is
		 * in the form of a set of pairs of the form (ListenerType, listener) -
		 * hence the i-=2 below.
		 */ 
      Object[] listeners = plusMinusListenerList.getListenerList();
      ChangeEvent evt = null;

		// Process the listeners last to first, notifying those that interested.
      for (int i = listeners.length-2; i>=0; i-=2)
		{
			if (listeners[i]==ChangeListener.class)
			{
         // Create the event lazily.
				if (evt == null)  evt = new ChangeEvent(source);
            ((ChangeListener)listeners[i+1]).stateChanged(evt);
         }          
      }
   }


   public String getLastButtonPushed()
   {
      return lastButtonPushed;
   }


   public JButton getPlusButton()
   {
      return plusButton;
   }


   public JButton getMinusButton()
   {
      return minusButton;
   }

}
