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
* Java class: DAOSearchCriteria.java
* First created on March 5, 2008, 09:24 AM
* 
* GUI element corresponding to the list of search criteria at the
* left of the DataChooser box.
*********************************************************************/

package xnatDAO;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.w3c.dom.Document;
import xnatRestToolkit.XNATRESTToolkit;
import xnatRestToolkit.XNATReturnedField;
import xnatRestToolkit.XNATSearchCriterion;


public class DAOSearchCriteria extends JPanel
{
	public static final int		 MAX_CRITERIA			 = 8;
	int								 nCriteriaDisplayed;
	DAOSearchCriterion[]			 criteria				 = new DAOSearchCriterion[MAX_CRITERIA];
	protected EventListenerList criteriaListenerList = new EventListenerList();

	
	
	/**
    * Create a new UI object for entering database search criteria as part
	 * of the DBChooser object. This function assembles a number of separate
	 * DAOSearchCriterion objects.
	 */
	public DAOSearchCriteria()
	{
		super();
		
		GroupLayout panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
		
		panelLayout.setAutoCreateContainerGaps(true);
		panelLayout.setAutoCreateGaps(true);
		
		JLabel criteriaCombinationLabel	= new JLabel("");
		JLabel XNATElementJLabel			= new JLabel("XNAT Element");
		JLabel comparisonLabel				= new JLabel("");
		JLabel comparisonStringLabel		= new JLabel("Comparison String");
		

		for (int i=0; i<MAX_CRITERIA; i++)
      {
         criteria[i] = new DAOSearchCriterion(i==0);

         criteria[i].addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent evt)
				{
               if (evt.getSource().equals(criteria[0].getPlusMinusButton().getPlusButton()))
                  addCriterion();
               else if (evt.getSource().equals(criteria[0].getPlusMinusButton().getMinusButton()))
                  removeCriterion();
               else fireCriteriaChanged(evt);
				}
			});

         // Set some initial defaults.
         criteria[i].setComparisonOperator("LIKE");
			criteria[i].setComparisonString("%");
			if (i != 0) criteria[i].setCombinationOperator("AND");
         if (i >  1) criteria[i].enableCriteriaCombinationComboBox(false);
      }

		nCriteriaDisplayed = 1;
			
			
			
		Group sgp = panelLayout.createSequentialGroup();
		Group pgp = panelLayout.createParallelGroup();
		
		sgp.addGroup(panelLayout.createParallelGroup()
			.addComponent(criteriaCombinationLabel)
			.addComponent(XNATElementJLabel)
			.addComponent(comparisonLabel)
			.addComponent(comparisonStringLabel)
			);
		
		pgp.addGroup(panelLayout.createSequentialGroup()
			.addComponent(criteriaCombinationLabel, 80, 80, 80)
			.addComponent(XNATElementJLabel, 50, 200, Short.MAX_VALUE)
			.addComponent(comparisonLabel, 90, 90, 90)
			.addComponent(comparisonStringLabel, 170, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			);

		
		for (int i=0; i<MAX_CRITERIA; i++)
		{
			sgp.addComponent(criteria[i]);
			pgp.addComponent(criteria[i]);
		}
		
		panelLayout.setHorizontalGroup(pgp);
		panelLayout.setVerticalGroup(sgp);
		
		/* Make the second and subsequent search criteria invisible until they are
		 * required. */
		for (int i=nCriteriaDisplayed; i<MAX_CRITERIA; i++) criteria[i].setVisible(false);


	}
	
	
	/**
    * Add a new search criterion. Since the component was already present,
	 * but just invisible, all that is needed is to register it and make
	 *  make it visible, then cascade the notification up the chain.
	 */
	public void addCriterion()
	{
		if (nCriteriaDisplayed < MAX_CRITERIA) criteria[nCriteriaDisplayed++].setVisible(true);
		fireCriteriaChanged(this);
	}
	
	
	/**
    * Remove the last search criterion. Simply make the UI element invisible
	 * and de-register the object, then cascade the notification up the chain.
	 */
	public void removeCriterion()
	{
		if (nCriteriaDisplayed > 1) criteria[--nCriteriaDisplayed].setVisible(false);
		fireCriteriaChanged(this);
	}
	
	
	public void addChangeListener(ChangeListener l)
	{
		criteriaListenerList.add(ChangeListener.class, l);
   }

	
	public void removeChangeListener(ChangeListener l)
	{
		criteriaListenerList.add(ChangeListener.class, l);
   }
	
	
	/**
    * Send a message to any registered listeners that the DB selection criteria
	 * have been altered and hence that a new query needs to be submitted and
	 * the UI updated.
	 */
	protected void fireCriteriaChanged(Object source)
	{
      Object[] listeners = criteriaListenerList.getListenerList();
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

	
	
	/**
    * Check whether all the fields are correctly specified to allow a valid
	 * database query to be submitted. If so, activate the relevant button on the
	 * UI in the parent JPanel.
	 */
	public boolean isValidForSubmitQuery()
	{
		boolean valid = true;
		
		for (int i=0; i<nCriteriaDisplayed; i++)
			valid = valid && criteria[i].isValidForSubmitQuery(); 
		
		return valid;
	}
	

   /**
    * Return the nth DAOSearchCriterion object. This is used so that the
    * calling application can query directly the underlying details of each
    * of the criteria.
    * @param n
    * @return criterion - contains information about the schema field to be
    * compared, the comparison used (=, !=, >, etc.) and the value against
    * which the field is to be compared.
    */
   public DAOSearchCriterion getDAOSearchCriterion(int n)
   {
      return criteria[n];
   }
   

   /**
    * Translate the state of the search criteria boxes into a form that can be
    * passed to XNATRESTToolkit to generate a search XML for the XNAT REST API.
    *
    * @return XNATCriteria
    */
   public XNATSearchCriterion[] getXNATCriteria()
   {
      XNATSearchCriterion[] XNATCriteria   = new XNATSearchCriterion[nCriteriaDisplayed];

      for (int i=0; i<nCriteriaDisplayed; i++)
         XNATCriteria[i] = new XNATSearchCriterion(criteria[i].getElementXPath(),
                                                   criteria[i].getComparisonOperator(),
                                                   criteria[i].getComparisonString());

      return XNATCriteria;
   }
   
   
   /**
    * Translate the state of the search criteria boxes into a form that tells
    * XNAT what data to return from the search.
    * 
    * Deprecated - originally designed for use with a previous version of
    *               the software
    * 
    * @param rootElement - the type of root element, required to tell XNAT
    *                      which element's "display document" to look in. 
    * @return information on the fields returned from search criteria
    */
   public XNATReturnedField[] getXNATReturnedFields(String rootElement)
   {
      Vector<String> distinct = getDistinctXPaths();
      Integer              sz = new Integer(distinct.size());
      XNATReturnedField[] returnedFields = new XNATReturnedField[sz+1];
      for (int i=0; i<distinct.size(); i++)
         returnedFields[i] = new XNATReturnedField(rootElement,
                                                   distinct.elementAt(i),
                                                   "string" );
      // Always add the UID as a unique column. This provides the "leaf" data
      // in the TreeTable.
      returnedFields[sz]   = new XNATReturnedField(rootElement,
                                                   rootElement+"/UID",
                                                   "string" );
      
      return returnedFields;
   }
   
   
   /**
    * Obtain the set of distinct elements involved in the current state of the
    * search criteria. This is needed for example in creating the headers for
    * the TreeTable columns.
    * 
    * @return a string vector containing the aliases of the distinct elements
    * in the criteria list
    */
   public Vector<String> getDistinctAliases()
   {
      /* Note the use of both a Set and a Vector here.
       * Using the Set.add method gives us a straightforward method for avoiding
       * duplication of elements and storing only those that are distinct.
       * Use of the Vector rather than the Set return type allows us to maintain
       * the elements in the order of their addition.
       */
      HashSet<String> elementSet       = new HashSet<String>();
      Vector<String>  distinctElements = new Vector<String>();

		for (int i=0; i<nCriteriaDisplayed; i++)
         if (elementSet.add(criteria[i].getElementAlias()))
            distinctElements.add(criteria[i].getElementAlias());

      return distinctElements;
   }
   

   /**
    * Obtain the set of distinct elements involved in the current state of the
    * search criteria. This is needed for example in creating the search XML
    * that returns the TreeTable data.
    *
    * @return a string vector containing the XPaths of the distinct elements
    * in the criteria list
    */
   public Vector<String> getDistinctXPaths()
   {
      /* Note the use of both a Set and a Vector here.
       * Using the Set.add method gives us a straightforward method for avoiding
       * duplication of elements and storing only those that are distinct.
       * Use of the Vector rather than the Set return type allows us to maintain
       * the elements in the order of their addition.
       */
      HashSet<String> elementSet       = new HashSet<String>();
      Vector<String>  distinctElements = new Vector<String>();

		for (int i=0; i<nCriteriaDisplayed; i++)
         if (elementSet.add(criteria[i].getElementXPath()))
            distinctElements.add(criteria[i].getElementXPath());

      return distinctElements;
   }


   /**
    * Update the criteria combo box lists to reflect a change in the data type
    * returned (search root element in XNAT speak). For example, if we are
    * returning PET scans, we don't expect to be able to search on MR parameters.
    *
    * @param searchRootElement
    */
   public void changeSearchRootElement(String searchRootElement)
   {
      for (int i=0; i<MAX_CRITERIA; i++)
         criteria[i].changeSearchRootElement(searchRootElement);
   }

   /** Submit the query represented by these search criteria to the
	 *  currently selected database. If getIcon is true, then we retrieve
	 *  micro-thumbnails of the data for use as the leaves of the JTree.
    * 
    *  Deprecated - originally designed for use with a previous version of
    *               the software
	 */
	public String generateQuery(Connection con, String[] tableColumns, boolean getIcon)
	{
		String			DBColumnName;
		String[]			DBColumnList	= new String[MAX_CRITERIA];
		boolean[]		isNumeric		= new boolean[MAX_CRITERIA];
		int				nDistinctFields = 0;
		
		/* Obtain the set of fields used in the search criteria.
		 * Remove multiple occurrences using a Java Set collection -
		 * the Set.add method returns true if the value can be added,
		 * which is only possible if the item does not duplicate any
		 * of the existing elements. */
		HashSet<String> set = new HashSet<String>();
		for (int i=0; i<nCriteriaDisplayed; i++)
		{
			DBColumnName = criteria[i].getElementXPath();
			if (set.add(DBColumnName))
			{
				DBColumnList[nDistinctFields] = DBColumnName;
	//			isNumeric[nDistinctFields++]  = criteria[i].isColumnNumeric();
			}
							
		}
		

		StringBuffer selectString = new StringBuffer("SELECT ");
		
		if (getIcon) selectString.append("imageIcon");
		
		else // Get the table data themselves.
		{			
			for (int i=0; i<nDistinctFields; i++)
				selectString.append(DBColumnList[i]).append(", ");

			selectString.append("SOPInstanceUID");

		/* Add the section of the query needed to obtain the data for the
		 * table part of the tree table. Column zero is not used, because
		 * this corresponds to the tree. */
		for (int i=1; i<tableColumns.length; i++)
			selectString.append(", " + tableColumns[i]);
		}
		
		selectString.append(" FROM data_imagelevel ");
		
		selectString.append("WHERE ");
		
		for (int i=0; i<nCriteriaDisplayed; i++)
		{
			selectString.append(criteria[i].getElementXPath());
			selectString.append(" ");
			selectString.append(criteria[i].getComparisonOperator());
			selectString.append(" ");
	//		if (!criteria[i].isColumnNumeric()) selectString.append("'");
			selectString.append(criteria[i].getComparisonString());
	//		if (!criteria[i].isColumnNumeric()) selectString.append("'");
			
			if (i != nCriteriaDisplayed-1)
			{
				selectString.append(" ");
				selectString.append(criteria[i+1].getCombinationOperator());
				selectString.append(" ");
			}
				
		}
		
		
		selectString.append(" ORDER BY ");
		for (int i=0; i<nDistinctFields; i++)
		{
			selectString.append(isNumeric[i] ? "ABS(" + DBColumnList[i] + ")" :
															DBColumnList[i]);
			
			if (i != nDistinctFields-1) selectString.append(", ");
		}
		
		
		return selectString.toString();
	}
	
}
