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

/********************************************************************
* @author Simon J Doran
* Java class: DAODefaultSearchesList.java
* First created on Oct 8, 2015 at 17:15:02 AM
* 
* Object representing a list of the default searches that are set
* up in the user interface of the DataChooser for different output
* types. N.B. DAO stands for Data Access Object, the old name for the
* DataChooser.
*********************************************************************/

package configurationLists;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Vector;
import xmlUtilities.SingletonListFromTwoLevelXML;

public class DAODefaultSearchesList extends SingletonListFromTwoLevelXML
{
   protected static DAODefaultSearchesList singletonList = null;
   
   /** Creates a new instance of DAOSearchableElementList
    *  Note that this is never called directly - but rather via getSingleton().
	 */
	protected DAODefaultSearchesList() throws IOException
	{
      super();
   }

   public static DAODefaultSearchesList getSingleton() throws IOException
	{
		if ( singletonList == null )
		{
         try
         {
            singletonList = new DAODefaultSearchesList();
         }
			catch (IOException exIO) {throw exIO;}
		}

		return singletonList;
	}


   @Override
   public void setVariables()
   {
      XMLResourceName = "/configurationLists/resources/DAO_defaultSearches.xml";
      rootName        = "XNAT_DAO_defaultSearches";
      outer           = "DAO_subtype";
      outAttr         = "name";
      inner           = "DAO_defaultSearchProperty";
      inAttr          = "name";

      errorMessageLog = "The default searches file did not contain the correct data.";
      errorMessageIOE = "Unable to retrieve list of definitions of the default searches";
   }

   public LinkedHashMap<String, Vector<String>> getPropertyValues()
   {
      return getInnerTextMap();
   }

   public LinkedHashMap<String, Vector<String>> getPropertyNames()
   {
      return getInnerAttrTextMap();
   }

   // Utility function for stripping out multiple occurrences in a vector.
   public Vector<String> getDistinctEntries(Vector<String> inVec)
   {
      HashSet<String> elementSet       = new HashSet<String>();
      Vector<String>  distinctElements = new Vector<String>();

		for (int i=0; i<inVec.size(); i++)
         if (elementSet.add(inVec.elementAt(i)))
            distinctElements.add(inVec.elementAt(i));

      return distinctElements;
   }
}
