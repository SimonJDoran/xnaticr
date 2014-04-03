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
* Java class: DAOElementsComboBox.java
* First created on Mar 26, 2008
* 
* GUI element containing the list of the XNAT database elements that
* we are allowing selection on.
*********************************************************************/

package xnatDAO;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class DAOElementsComboBox extends JComboBox
{
   private DAOSearchableElementList sel;
   private String searchRootElement = "xnat:mrScanData";
	
	/** Create a new JComboBox and populate it with database fields available for
	 *  selection.
	 */
	public DAOElementsComboBox()
	{
		super();
		
		/* Obtain the list object that contains the XNAT database elements that
       * we are allowing selection on. Note that the items we can search on
       * depends on the "root" element of the search in XNAT parlance, i.e.,
       * on the type of object we are retrieving from the XNAT database.
       */
		try
      {
         sel = DAOSearchableElementList.getSingleton();
      }
      catch (IOException exIO)
      {
         throw new RuntimeException(exIO.getMessage());
      }
      LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATAliases();
		setModel(new DefaultComboBoxModel(map.get(searchRootElement)));
	}
	
	
	/** Get the full XPath of the entry selected in the JComboBox */
	public String getFullXPath()
	{
      LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATElements();
      return map.get(searchRootElement).elementAt(getSelectedIndex());
	}


   /** Reset the combo box model if the search root element has changed. */
   public void resetModel(String searchRootElement)
   {
      this.searchRootElement = searchRootElement;
      LinkedHashMap<String, Vector<String>> map = sel.getSearchableXNATAliases();
		setModel(new DefaultComboBoxModel(map.get(searchRootElement)));
   }
	
}
