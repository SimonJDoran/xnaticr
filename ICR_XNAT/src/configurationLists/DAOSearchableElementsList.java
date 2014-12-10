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
* Java class: DAOSearchableElementList.java
* First created on March 29, 2010, 11:04 AM
* 
* Object representing a list of the fields that can be searched in
* the XNAT database, with method for reading this list from an XML
* file.
*********************************************************************/

package configurationLists;


import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Vector;
import xmlUtilities.SingletonListFromTwoLevelXML;

	
public class DAOSearchableElementsList extends SingletonListFromTwoLevelXML
{
   protected static DAOSearchableElementsList singletonList = null;
   
   /** Creates a new instance of DAOSearchableElementList
    *  Note that this is never called directly - but rather via getSingleton().
	 */
	protected DAOSearchableElementsList() throws IOException
	{
      super();
   }

   public static DAOSearchableElementsList getSingleton() throws IOException
	{
		if ( singletonList == null )
		{
         try
         {
            singletonList = new DAOSearchableElementsList();
         }
			catch (IOException exIO) {throw exIO;}
		}

		return singletonList;
	}


   @Override
   public void setVariables()
   {
      XMLResourceName = "projectResources/searchableXNATElements.xml";
      rootName        = "XNATSearchRootElements";
      outer           = "XNATSearchRootElement";
      outAttr         = "element";
      inner           = "searchableXNATElement";
      inAttr          = "alias";

      errorMessageLog = "The searchableXNATElements file did not contain the correct data.";
      errorMessageIOE = "Unable to retrieve list of XNAT elements available for selection";
   }

   public LinkedHashMap<String, Vector<String>> getSearchableXNATElements()
   {
      return getInnerTextMap();
   }

   public LinkedHashMap<String, Vector<String>> getSearchableXNATAliases()
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



