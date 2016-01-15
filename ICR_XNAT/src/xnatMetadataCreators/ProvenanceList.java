/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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
* Java class: ProvenanceList.java
* First created on Jan 14, 2016 at 8:22:03 AM
* 
* Data structure parallelling the prov:provenance element
*********************************************************************/

package xnatMetadataCreators;

import java.util.ArrayList;
import java.util.List;

public class ProvenanceList
{
	private static final String PROV_STRING = "_!PS!_";
	private static final String UNKNOWN_STRING = "Unknown";
			  
	public class Provenance
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
		
   private List<Investigator> invList;
   private int chosenInvestigator;

   public InvestigatorList(String[] titles,
                           String[] firstNames,
                           String[] lastNames,
                           String[] institutions,
                           String[] departments,
                           String[] emails,
                           String[] phoneNumbers)
   {
      // It is possible that the project has no investigators defined.
      if (titles == null)
      {
         invList = new ArrayList<>();
         invList.add(new Investigator("",
                                      "No project investigators specified",
                                      "", "", "", "", ""));
      }
      else
      {
         // It is a programming error if not all the arguments have the same
         // length, so no error checking is needed here - just allow it to fail.
         invList = new ArrayList<>();
         for (int i=0; i<titles.length; i++)
            invList.add(new Investigator(titles[i],
                                          firstNames[i],
                                          lastNames[i],
                                          institutions[i],
                                          departments[i],
                                          emails[i],
                                          phoneNumbers[i]));
      }
      
      // Start off with the default investigator being the first in the list.
      chosenInvestigator = 0;
   }
   
   
   public void setInvestigatorNumber(int n)
   {
      chosenInvestigator = n;
   }
   
   
   public int getInvestigatorNumber()
   {
      return chosenInvestigator;
   }
   
   
   public Investigator getChosenInvestigator()
   {
      return invList.get(chosenInvestigator);
   }
   
   
   public Investigator getInvestigator(int n)
   {
      return invList.get(n);
   }
   
   
   public List<String> getFullNames()
   {
      List<String> list = new ArrayList<>();
		for (Investigator inv : invList) list.add(inv.firstName + " " + inv.lastName);
      
      return list;
   }
	
	
	/**
	 * Turn a list of String entries into a single string separated by a "unique"
	 * marker. This is a kludge to solve a display problem. It is useful to have
	 * an indication in the user interface of what the provenance of an uploaded
	 * file is. However, the current display mechanism has just a single line per 
	 * uploaded entity and is not able to represent a multi-step provenance. To
	 * solve this temporarily, I create a single String catenating all the
	 * process steps together, separating them by a "unique" marker string that
	 * is unlikely (but not impossible!) to occur in the provenance input.
	 * 
	 * TODO: Refactor to change simple Strings into proper handling of complexTypes.
	 *       This is probably too major to justify the effort in this case.
	 * @param entries List of String values
	 * @return single String separated with "unique" code
	 */
	private String encodeProvenanceString(ArrayList<String> entries)
	{
		StringBuilder sb = new StringBuilder();
		for (String entry : entries) sb.append(entry).append(PROV_STRING);
		
		return sb.toString();
	}
	
	
	/**
    * Take a single String and turn it back into the List<String> originally
	 * used to create it.
    * @param provString String containing processStep entries in a provenance
    * list, separated by the "unique" marker String.
    * @return an ArrayList of the separated values
    */
   private List<String> decodeProvenanceString(String provString)
   {
		if (provString == null) return null;
		
      String       s    = provString;
		List<String> list = new ArrayList<>();
      int          ind;
				
		do
      {
         ind = s.indexOf(PROV_STRING, 0);
         if (ind != -1)
         {
            list.add(s.substring(0, ind-1));
            s = s.substring(ind + PROV_STRING.length());
         }
      }
		while (ind != -1);
      
      return list;
   }

}
