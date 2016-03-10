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
* Java class: InvestigatorList.java
* First created on Nov 15, 2010 at 10:11:17 AM
* 
* Data structure parallelling the xnat:investigator element
*********************************************************************/

package dataRepresentations.xnatSchema;

import exceptions.XMLException;
import exceptions.XNATException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatRestToolkit.XNATNamespaceContext;
import xnatRestToolkit.XNATRESTToolkit;

public class InvestigatorList extends XnatSchemaElement
{
	public static class Investigator
   {
      public String title;
      public String firstName;
      public String lastName;
      public String institution;
      public String department;
      public String email;
      public String phoneNumber;
		
		public Investigator() {}
      
      public Investigator(String title,
                          String firstName,
                          String lastName,
                          String institution,
                          String department,
                          String email,
                          String phoneNumber)
      {
         this.title       = title;
         this.firstName   = firstName;
         this.lastName    = lastName;
         this.institution = institution;
         this.department  = department;
         this.email       = email;
         this.phoneNumber = phoneNumber;
      }
   }
		
	public Investigator          pi      = new Investigator();;
	public List<Investigator>    invList = new ArrayList<>();  
	private XNATNamespaceContext xnatNs  = new XNATNamespaceContext();
	private Document             invDoc;
	
	
	
	/**
	 * Initialisation of object directly from an XNAT project
	 * @param xnatProject a String containing the project name
	 * @param xnprf the XNAT connection profile
	 * @throws XNATException
	 * @throws exceptions.XMLException
	 */
	public InvestigatorList(String xnatProject, XNATProfile xnprf)
			 throws XNATException, XMLException
	{
		String          restCommand = "/data/archive/projects/" + xnatProject + "?format=xml";
		XNATRESTToolkit xnrt        = new XNATRESTToolkit(xnprf);		

		invDoc = xnrt.RESTGetDoc(restCommand);
		NodeList ndlPI     = XMLUtilities.getElement(invDoc, xnatNs, "xnat:PI");
		NodeList ndlInv    = XMLUtilities.getElement(invDoc, xnatNs, "xnat:investigator");

		int      nPI       = (ndlPI  == null) ? 0 : ndlPI.getLength();
		int      nInv      = (ndlInv == null) ? 0 : ndlInv.getLength();
		
		if (nPI != 0)
		{			
			pi.title        = getPiProperty("title");
			pi.firstName    = getPiProperty("firstname");
			pi.lastName     = getPiProperty("lastname");
			pi.institution  = getPiProperty("institution");
			pi.department   = getPiProperty("department");
			pi.email        = getPiProperty("email");
			pi.phoneNumber  = getPiProperty("phone");
		}
		
		
		for (int i=0; i<nInv; i++)
		{
			Investigator inv = new Investigator();
			
			inv.title        = getInvestigatorProperty(i, "title");
			inv.firstName    = getInvestigatorProperty(i, "firstname");
			inv.lastName     = getInvestigatorProperty(i, "lastname");
			inv.institution  = getInvestigatorProperty(i, "institution");
			inv.department   = getInvestigatorProperty(i, "department");
			inv.email        = getInvestigatorProperty(i, "email");
			inv.phoneNumber  = getInvestigatorProperty(i, "phone");
			
			invList.add(inv);
		}
		
	}
	
	
	// The assumption for the investigator properties is that either there will
	// be no such element or that there is only one element.
	// If, for some strange reason, there is more than one value for a property,
	// we return only the first.
	
	private String getPiProperty(String propertyName)
			  throws XMLException
	{
		String xpe    = "/xnat:Project/xnat:PI/xnat:" + propertyName;
		return XMLUtilities.getFirstXPathResult(invDoc, xnatNs, xpe);
	}
	
	
	private String getInvestigatorProperty(int invNum, String propertyName)
			  throws XMLException
	{

		String xpe    = "/xnat:Project/xnat:investigators[1]//xnat:investigator["
				                  + (invNum+1) + "]/xnat:" + propertyName;
		return XMLUtilities.getFirstXPathResult(invDoc, xnatNs, xpe);
	}
	
	
	public InvestigatorList(Investigator pi, List<Investigator> invList)
   {
		this.pi      = pi;
      this.invList = invList;
   }
   
   
   public Investigator getInvestigator(int n) throws IllegalArgumentException 
   {
      if ((n < 0) || (n>invList.size()-1))
			throw new IllegalArgumentException("Invalid investigator number");

		return invList.get(n);
   }
   
   
   public List<String> getFullNames()
   {
      List<String> list = new ArrayList<>();
		for (Investigator inv : invList) list.add(inv.firstName + " " + inv.lastName);
      
      return list;
   }
	
}
