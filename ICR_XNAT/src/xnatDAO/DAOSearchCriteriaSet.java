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
* Java class: DAOSearchCriteriaSet.java
* First created on Mar 6, 2012 at 11:39:04 AM
* 
* This class implements the external API (for non-Java environments
* like MATLAB and IDL) of the ICR DataChooser (a.k.a. XNATDAO).
*********************************************************************/

package xnatDAO;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DAOSearchCriteriaSet
{
   int               nCriteria;
   int               MAX_CRITERIA = DAOSearchCriteria.MAX_CRITERIA;
   String            comparisonElements[];
   String            comparisonOperators[];
   String            comparisonStrings[];
   DAOSearchCriteria searchCriteriaUI;

   
   protected DAOSearchCriteriaSet(DAOSearchCriteria dsc)
   {
      nCriteria = 0;
      comparisonElements  = new String[MAX_CRITERIA];
      comparisonOperators = new String[MAX_CRITERIA];
      comparisonStrings   = new String[MAX_CRITERIA];
      searchCriteriaUI    = dsc;
   }
   
   
   protected void addSearchCriterion(String element, String operator, String str)
   {
      ++nCriteria;
      comparisonElements[nCriteria-1]  = element;
      comparisonOperators[nCriteria-1] = operator;
      comparisonStrings[nCriteria-1]   = str;
   }
   
   
   protected boolean searchCriteriaValid()
   {      
      if ((nCriteria == 0 || nCriteria > MAX_CRITERIA)) return false;
      
      ArrayList<String> comparisonOpList = new ArrayList<String>();
      comparisonOpList.add("<");
      comparisonOpList.add("<=");
      comparisonOpList.add("=");
      comparisonOpList.add(">=");
      comparisonOpList.add(">");
      comparisonOpList.add("!=");
      comparisonOpList.add(" IS DISTINCT FROM");
      comparisonOpList.add("LIKE");
      comparisonOpList.add("REGEXP");
      
      ArrayList<String> aliasList = searchCriteriaUI.criteria[0].getElementAliases();
      
      for (int i=0; i<nCriteria; i++)
      {
         if (!comparisonOpList.contains(comparisonOperators[i])) return false;
         if (!aliasList.contains(comparisonElements[i]))         return false;
         
         // Now follow the method in DAOSearchCriterion.isValidForSubmitQuery()
         // to check the comparison string, accepting that there are some
         // limitations.
         
         String  s  = comparisonStrings[i];
         String  op = comparisonOperators[i];
         
         if (op.equals("LIKE")) // Is this still needed?
         {
            Pattern p = Pattern.compile("[%-. \\w]+");
			   Matcher m = p.matcher(s);
			   if (!m.matches()) return false;
         }
         
         else if (op.equals("REGEXP"))
         {
            try
            { 
               Pattern.compile(s);
            } 
            catch (Exception ex)
            { 
               return false; 			
            }
         }
         
         else
         {
            Pattern p = Pattern.compile("[-. \\w]+");
			   Matcher m = p.matcher(s);
			   if (!m.matches()) return false;
         }                     
      }
      return true;
   }
  
   
   protected void applySearchCriteria()
   {
      if (!searchCriteriaValid()) return;
      
      searchCriteriaUI.nCriteriaDisplayed = nCriteria;
      
      for (int i=0; i<nCriteria; i++)
      {
         DAOSearchCriterion sc = searchCriteriaUI.criteria[i];
         sc.setElementSelectedItem(comparisonElements[i]);
         sc.setCombinationOperator(comparisonOperators[i]);
         sc.setComparisonString(comparisonStrings[i]);
         sc.setVisible(true);
      }
      
      for (int i=nCriteria; i<DAOSearchCriteria.MAX_CRITERIA; i++)
      {
         DAOSearchCriterion sc = searchCriteriaUI.criteria[i];
         sc.setVisible(false);
      }
   }
}


