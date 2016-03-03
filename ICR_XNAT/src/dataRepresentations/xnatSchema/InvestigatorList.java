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
		
	public Investigator       pi;
	public List<Investigator> invList = new ArrayList<>();
   private int chosenInvNum;
	
	
	
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
		String               restCommand = "/data/archive/projects/" + xnatProject + "?format=xml";
		XNATRESTToolkit      xnrt        = new XNATRESTToolkit(xnprf);		

		Document             resultDoc   = xnrt.RESTGetDoc(restCommand);
		XNATNamespaceContext xnatNs      = new XNATNamespaceContext();
		NodeList             ndlPI       = XMLUtilities.getElement(resultDoc, xnatNs, "xnat:PI");
		NodeList             ndlInv      = XMLUtilities.getElement(resultDoc, xnatNs, "xnat:investigator");

		int      nPI    = (ndlPI  == null) ? 0 : ndlPI.getLength();
		int      nInv   = (ndlInv == null) ? 0 : ndlInv.getLength();
		int      nTot   = nPI + nInv;
		
		System.out.println("Extracting investigator list ...");
	}
	
	
	public InvestigatorList(Investigator pi, List<Investigator> invList)
   {
		this.pi      = pi;
      this.invList = invList;

   }
   
   
   public void setChosenInvestigatorNumber(int n) throws IllegalArgumentException 
   {
      if ((n < 0) || (n>invList.size()-1))
			throw new IllegalArgumentException("Invalid investigator number");
		
      chosenInvNum = n;
   }
   
   
   public int getChosenInvestigatorNumber()
   {
      return chosenInvNum;
   }
   
   
   public Investigator getChosenInvestigator()
   {
      return invList.get(chosenInvNum);
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
