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
* Java class: DAOReturnTypesList.java
* First created on Mar 30, 2010 at 2:54:03 PM
* 
* Object representing a list of XNAT complex types that can be
* returned by the DataChooser, with method for reading this list from
* an XML file.
*********************************************************************/

package configurationLists;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;
import xmlUtilities.SingletonListFromTwoLevelXML;


/**
 *
 * @author Simon J Doran
 *
 * Java class: DAOReturnTypesList.java
 *
 * First created on Mar 30, 2010 at 2:54:03 PM
 *
 */
public class DAOReturnTypesList extends SingletonListFromTwoLevelXML
{
   protected static DAOReturnTypesList singletonList = null;

   /** Creates a new instance of DAOReturnTypesList.
    *  Note that this is never called directly - but rather via getSingleton().
	 */
	protected DAOReturnTypesList() throws IOException
	{
      super();
   }

   public static DAOReturnTypesList getSingleton() throws IOException
	{
		if ( singletonList == null )
		{
			try
         {
            singletonList = new DAOReturnTypesList();
         }
			catch (IOException exIO) {throw exIO;}
		}

		return singletonList;
	}


   @Override
   public void setVariables()
   {
      XMLResourceName = "projectResources/DAOreturnTypes.xml";
      rootName        = "XNAT_DAO_returnDataTypes";
      outer           = "DAO_datatype";
      outAttr         = "alias";
      inner           = "DAO_subtype";
      inAttr          = "alias";
      errorMessageIOE = "Unable to retrieve list of XNAT return types";
      errorMessageLog = "The returnDataTypes.xml file did not contain the correct data.";
   }

   public LinkedHashMap<String, Vector<String>> getDAOReturnTypes()
   {
      return getInnerTextMap();
   }

   public LinkedHashMap<String, Vector<String>> getDAOReturnAliases()
   {
      return getInnerAttrTextMap();
   }

}

