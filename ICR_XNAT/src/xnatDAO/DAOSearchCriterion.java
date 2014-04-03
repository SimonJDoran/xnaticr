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
* Java class: DAOSearchCriterion.java
* First created on March 4, 2008, 09:24 AM
* 
* GUI object representing a database search criterion
*********************************************************************/

package xnatDAO;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


public class DAOSearchCriterion extends JPanel
{
	private	boolean              isFirst;
	private	DAOSearchCriteria    parent;
	private	JComboBox            criteriaCombinationComboBox;
	private	DAOElementsComboBox	XNATElementComboBox;
	private	JComboBox            comparisonComboBox;
	private	JTextField           comparisonTextField;
   private  PlusMinusButton      criteriaPlusMinusButton;
	
	protected EventListenerList   criterionListenerList = new EventListenerList();
		
	/** Create a new object for implementing a search criterion on the database.
	 *  Initially, the object is primarily UI-related. The isFirst argument
	 *  is used because the behaviour is slightly different for the first
	 *  object added, inasmuch as no criteriaCombination object is needed.
	 */
	public DAOSearchCriterion(boolean isFirst)
	{
		this.isFirst	= isFirst;
		parent			= (DAOSearchCriteria) getParent();
		
		initialiseUI();
		populateUI();
		addUIListeners();
	}
	

	
	public void addChangeListener(ChangeListener l)
	{
		criterionListenerList.add(ChangeListener.class, l);
   }


	
	public void removeChangeListener(ChangeListener l)
	{
		criterionListenerList.add(ChangeListener.class, l);
   }

	
	/** Notify all listeners that have registered interest for notification
	 * of this event type.  The event instance is lazily created using the
	 * parameters passed into the various fire methods below.
    * See class EventListenerList for more details.
	 */
   protected void fireCriterionChanged(Object source)
	{
		/* Guaranteed to return a non-null array. The array that comes back is
		 * in the form of a set of pairs of the form (ListenerType, listener) -
		 * hence the i-=2 below.
		 */ 
      Object[] listeners = criterionListenerList.getListenerList();
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
	
	/** Respond to the modified state of the comparisonTextField by activating
	 *  the "Submit DB query" button. */
	private void comparisonTextFieldFocusLost(FocusEvent evt)
	{
		fireCriterionChanged(evt.getSource());
	}
	
	
	/** Respond to the modified state of the comparisonComboBox by activating the
	 *  "Submit DB query" button. */	
	private void comparisonComboBoxItemStateChanged(ItemEvent evt)
	{
		fireCriterionChanged(evt.getSource());
	}
	

	/** Respond to the modified state of the XNATElementComboBox by activating the
	 *  "Submit DB query" button. */
	private void XNATElementComboBoxItemStateChanged(ItemEvent evt)
	{
		fireCriterionChanged(evt.getSource());
	}


   /** Respond to pressing either the plus or minus buttons by increasing
    *  or decreasing the number of criteria displayed. */
   private void plusMinusButtonPressed(ActionEvent evt)
   {
      fireCriterionChanged(evt.getSource());
   }
	
	
	/** Get the XPATH for the XNAT element selected. */
	public String getElementXPath()
	{
		return (String) XNATElementComboBox.getFullXPath();
	}


   /** Get the alias (short, easy to remember, form) of the XNAT element currently selected. */
   public String getElementAlias()
   {
      return (String) XNATElementComboBox.getSelectedItem();
   }
	
	
	/** Get the column index of the XNAT element selected. */
	public int getElementSelectedIndex()
	{
		return XNATElementComboBox.getSelectedIndex();
	}
   
   
   /** Get the aliases of all the XNAT elements in the ComboBox. */
   public ArrayList<String> getElementAliases()
   {
      int n = XNATElementComboBox.getItemCount();
      ArrayList<String> aliases = new ArrayList<String>();
      
      for (int i=0; i<n; i++) aliases.add((String) XNATElementComboBox.getItemAt(i));
      
      return aliases;
   }
	

	
	/** Get the operator to be used for comparing the DB field and string. */
	public String getComparisonOperator()
	{
      // We need to be careful to return the comparison in an XML-compatible
      // form, as it will be used in XNAT search XML documents.
      String cOp = (String) comparisonComboBox.getSelectedItem();
      return cOp.replace(">", "&gt;").replace("<", "&lt;");
	}
	
	
	
	/** Get the string to be used for comparison with the XNAT element. */
	public String getComparisonString()
	{
		return comparisonTextField.getText();
	}
	
	
	
	/** Get the operator to be used for combining successive criteria. */
	public String getCombinationOperator()
	{
		if (this.isFirst) return null;
		else return (String) criteriaCombinationComboBox.getSelectedItem();
	}

	
	
	/** set the operator to be used for comparing the DB field and string. */
	public void setComparisonOperator(String comparisonOperator)
	{
		comparisonComboBox.setSelectedItem(comparisonOperator);
	}
	
	
	
	/** Get the string to be used for comparison with the DB field. */
	public void setComparisonString(String comparisonString)
	{
		comparisonTextField.setText(comparisonString);
	}
	
	
	
	/** Get the operator to be used for combining successive criteria. */
	public void setCombinationOperator(String combinationOperator)
	{
		criteriaCombinationComboBox.setSelectedItem(combinationOperator);
	}
   
   
   /** Set the index of the XNAT element selected. */
	public void setElementSelectedIndex(int n)
	{
		XNATElementComboBox.setSelectedIndex(n);
	}
   
   
   /** Set the name of the XNAT element selected. */
	public void setElementSelectedItem(String s)
	{
		XNATElementComboBox.setSelectedItem(s);
	}

	
	
	/** Set up the layout of the various GUI components. */
	private void initialiseUI()
	{
		XNATElementComboBox				= new DAOElementsComboBox();
		comparisonComboBox				= new JComboBox();
		criteriaCombinationComboBox	= new JComboBox();
		comparisonTextField				= new JTextField();
      criteriaPlusMinusButton       = new PlusMinusButton();
		
		XNATElementComboBox.setBackground(DAOConstants.BG_COLOUR);
		comparisonComboBox.setBackground(DAOConstants.BG_COLOUR);
		criteriaCombinationComboBox.setBackground(DAOConstants.BG_COLOUR);
		comparisonTextField.setBackground(DAOConstants.EDITABLE_COLOUR);
		
	
		GroupLayout panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
		this.setBackground(DAOConstants.BG_COLOUR);
		
		panelLayout.setAutoCreateContainerGaps(true);

		panelLayout.setHorizontalGroup(
			panelLayout.createSequentialGroup()
			.addComponent(isFirst ? criteriaPlusMinusButton:criteriaCombinationComboBox, 80, 80, 80)
			.addComponent(XNATElementComboBox, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(comparisonComboBox, 90, 90, 90)
			.addComponent(comparisonTextField, 110, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		panelLayout.setVerticalGroup(
			panelLayout.createParallelGroup()
			.addComponent(isFirst ? criteriaPlusMinusButton:criteriaCombinationComboBox, 30, 30, 30)
			.addComponent(XNATElementComboBox, 30, 30, 30)
			.addComponent(comparisonComboBox, 30, 30, 30)
			.addComponent(comparisonTextField, 30, 30, 30)
		);

	}
	

	
	/** Check whether the given column is numeric (for the purposes of sorting, etc.) */
	public boolean isColumnNumeric()
	{
      // TODO Provide means of checking this

		return false;
	}


   /**
    * Return the object for the plusMinus button group for use in interpreting
    * where events come from.
    */
   public PlusMinusButton getPlusMinusButton()
   {
      return criteriaPlusMinusButton;
   }


   /**
    * Enable or disable the criteriaCombinationJComboBox, depending on whether or not
    * we are allowed to chose this item.
    */
   public void enableCriteriaCombinationComboBox(boolean enable)
   {
      criteriaCombinationComboBox.setEnabled(enable);
   }
	
	
	
	/** Set the contents of the various GUI components. */
	private void populateUI()
	{
		String[] boolOpList = {"AND", "OR", "NOT", "XOR"};
		criteriaCombinationComboBox.setModel(new DefaultComboBoxModel(boolOpList));
		
		String[] comparisonOpList = {"<", "<=", "=", ">=", ">", "!=", " IS DISTINCT FROM",
                                   "LIKE",  "REGEXP"};
		comparisonComboBox.setModel(new DefaultComboBoxModel(comparisonOpList));
		comparisonComboBox.setSelectedIndex(2);
	}


   /** Change the contents of the combo box when the searchRootElement changes. */
   public void changeSearchRootElement(String searchRootElement)
   {
      XNATElementComboBox.resetModel(searchRootElement);
   }
	

	

	
	/** Add a listener of the appropriate type to each element of the UI.
	 *  Each listener propagates back a ChangeEvent.
	 */
	private void addUIListeners()
	{
		XNATElementComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
            XNATElementComboBoxItemStateChanged(evt);
         }
      });
		
		comparisonComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent evt)
			{
				comparisonComboBoxItemStateChanged(evt);
			}
		});
		
		comparisonTextField.addFocusListener(new FocusAdapter() {
			@Override
         public void focusLost(FocusEvent evt)
			{
            comparisonTextFieldFocusLost(evt);
         }
      });

      criteriaPlusMinusButton.getPlusButton().addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            plusMinusButtonPressed(evt);
         }
      });

      criteriaPlusMinusButton.getMinusButton().addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            plusMinusButtonPressed(evt);
         }
      });
	}
	
	
	
	/** Check whether the various elements of this compound UI object are
	 *  in the correct state for a valid database query to be made. In
	 *  particular, this means ensuring that a column of the database has been
	 *  selected and an appropriate string has been entered in the comparison
	 *  text field.
	 */
	public boolean isValidForSubmitQuery()
	{
		boolean valid = true;
		
		String s = comparisonTextField.getText();
		
		if (comparisonComboBox.getSelectedIndex() < 6)
		{
			/* If a straightforward comparison, then only alphanumeric characters
			 * and a few punctuation marks are allowed. */
			Pattern p = Pattern.compile("[-. \\w]+");
			Matcher m = p.matcher(s);
			valid = valid && m.matches();
		}
		
		if (comparisonComboBox.getSelectedIndex() == 7)
		{
			/* mySQL "LIKE" selected. The pattern may additionally contain "%"
			 * and "_". Initially, I will not implement the possibility of
			 * escaping these two characters, so we will not be able to match these.
			 */
			Pattern p = Pattern.compile("[-. %_\\w]+");
			Matcher m = p.matcher(s);
			valid = valid && m.matches();
		}
		
		
		if (comparisonComboBox.getSelectedIndex() == 8)
		{
			/* mySQL "REGEXP" selected. I have not yet found a way to test
			 * the validity of the regular expression entered. Two possibilities
			 * spring to mind:
			 * 
			 * 1. Use the Java regular expression checker and don't worry about
			 *    mySQL-specific syntax.
			 * 
			 * 2. Create a dummy database table specifically for checking the
			 *    syntax of the regexp, issue a query to that and look at the
			 *    response.
			 * 
			 * Currently, I am going for option 1, although unsatisfactory.
			 */
			try
			{ 
				Pattern.compile(s);
			} 
			catch (Exception ex)
			{ 
				valid = false; 			
			}
		
		} 
		
		return valid;
	}
		
}
