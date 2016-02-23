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
* Java class: UploadStructure.java
* First created on Nov 16, 2010 at 9:04:17 AM
* 
* This class stores metadata variables that will be uploaded to XNAT.
* Initially, the metadata to be stored will be mainly in the
* form of simple string fields, with a few exceptions. However, there
* is the possibility in the future for this to extend to a more
* general parsing of the XNAT schema and this will require a
* significant refactoring.
*********************************************************************/

package xnatUploader;

import dataRepresentations.xnatSchema.InvestigatorList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import configurationLists.DAOSearchableElementsList;


public class UploadStructure
{
   static  Logger                   logger = Logger.getLogger(XNATUploader.class);
   private String                   rootElement;
   private HashMap<String, Object>  fieldMap;
   private DAOSearchableElementsList sel = null;
   
  
   /**
    * Set up an appropriate structure corresponding to a particular data type.
    *
    * @param rootElement a String containing the XML schema element name of
    * the XNAT data type being uploaded
    */
   public UploadStructure(String rootElement)
   {
      this.rootElement = rootElement;
      
      fieldMap = new HashMap<String, Object>();
      
      try
      {
         // The DAOSearchableElementsList (sel) is an object that maps the contents of the
         // searchableXNATElements.xml file into an easy format for the program to
         // interrogate. It is used here to model the fields to be uploaded, and
         // in a variety of other places around XNAT_DAO.
         sel = DAOSearchableElementsList.getSingleton();
      }
      catch (IOException exIO)
      {
         logger.error("Can't load the DAOSearchableElement list from SearchableXNATElements.xml");
         throw new RuntimeException("Can't load the DAOSearchableElement list from SearchableXNATElements.xml");
      }
   }
   
   
   /**
    * Get the value of a metadata field to be uploaded. 
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @return an Object representing metadata. Note that this method does not
    * provide any check that the class that is returned is the one the programmer
    * was expecting and the result needs to be explicitly cast to the correct
    * type. This may lead to programming errors that are not detected at
    * compile time or by the IDE.
    */
   public Object getField(String fieldAlias)
   {
      checkAccess(fieldAlias);
      return fieldMap.get(fieldAlias);
   }
   
   
   /**
    * Set the value of a metadata field to be uploaded. 
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @param value an Object containing the data to be uploaded. N.B. No checks
    * on the data type are performed.
    */
   public void setField(String fieldAlias, Object value)
   {
      checkAccess(fieldAlias);
      if (fieldMap.containsKey(fieldAlias))
      {
         fieldMap.remove(fieldAlias);
      }
      fieldMap.put(fieldAlias, value);
   }
   
   
   /**
    * Set the value of a metadata String field to be uploaded. 
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    * @param value a String to which the field contents are set
    */
   public void setStringField(String fieldAlias, String value)
   {
      if (value == null)
      {
         logger.warn("Tried to call setStringField with a null value");
         return;
      }
      
      if (fieldMap.containsKey(fieldAlias)) checkAccess(fieldAlias, String.class);
      fieldMap.put(fieldAlias, value);
   }
   
   
   /**
    * Get the value of a metadata field to be uploaded. 
    * @param fieldAlias a String containing the title of the field. It is an
    * alias in the sense that the "full field descriptor" is the XML schema
    * name of the XNAT element, whereas this is the "user-friendly" form that
    * appears in the GUI.
    */
   public String getStringField(String fieldAlias)
   {
      checkAccess(fieldAlias, String.class);
      String s = (String) fieldMap.get(fieldAlias);
      if (s == null) return "";
      else           return s;
   }
   
   
   
   
   /**
    * Store a list of the possible values for the investigator who created
    * the piece of data to be uploaded. This list will be populated from
    * XNAT's list of investigators with access to a particular project.
    * @param titles
    * @param firstNames
    * @param lastNames
    * @param institutions
    * @param departments
    * @param emails
    * @param phoneNumbers
    */
   public void setInvestigatorList(String[] titles,
                                   String[] firstNames,
                                   String[] lastNames,
                                   String[] institutions,
                                   String[] departments,
                                   String[] emails,
                                   String[] phoneNumbers)
   {
//      InvestigatorList inv = new InvestigatorList(titles, firstNames, lastNames,
//                                             institutions, departments, emails,
//                                             phoneNumbers);
//      fieldMap.put("InvestigatorList", inv);
   }
   
   
      
   /**
    * Retrieve the list of allowable investigators for a given data upload. 
    * @return InvestigatorList object
    */
   public InvestigatorList getInvestigatorList()
   {
      return (InvestigatorList) fieldMap.get("InvestigatorList");
   }
      
   
   
   
   /**
    * Check whether the given field alias is in the list of allowable items to
    * upload to XNAT.
    * 
    * Fail with a runtime error and an entry to the log. The only reason for
    * the call to fail is a programming error (like trying to set or get a non-
    * existent variable, or trying to set a field to the wrong type,
    * which would normally be caught in compilation by the IDE).
    * 
    * @param fieldAlias
    */
   private void checkAccess(String fieldAlias, Class cl)
   {
      Vector<String> allowableAliases = sel.getSearchableXNATAliases().get(rootElement);
      if (!allowableAliases.contains(fieldAlias))
      {
         logger.error("Trying to access the field corresponding to an illegal "
                      + "alias: " + fieldAlias);
         throw new RuntimeException("Trying to get the string field corresponding "
                      + "to an illegal alias: " + fieldAlias);
      }
      
      Object field = fieldMap.get(fieldAlias);
      if (field == null) return;
      if (!(cl.isInstance(field)))
      {
         logger.error("Trying to access field, " + fieldAlias
                      + "of type " + cl.getName() + ", with incompatible type "
                      + field.getClass().getName());
         throw new RuntimeException("Trying to access field, " + fieldAlias
                      + "of type " + cl.getName() + ", with incompatible type "
                      + field.getClass().getName());
      }
   }
   
   
   /**
    * Check whether the given field alias is in the list of allowable items to
    * upload to XNAT.
    * 
    * This method does not provide any check that the class that is returned
    * is the one the programmer was expecting.
    * @param fieldAlias
    */
   private void checkAccess(String fieldAlias)
   {
      Vector<String> allowableAliases = sel.getSearchableXNATAliases().get(rootElement);
      if (!allowableAliases.contains(fieldAlias))
      {
         logger.error("Trying to access the field corresponding to an illegal "
                      + "alias: " + fieldAlias);
         throw new RuntimeException("Trying to get the string field corresponding "
                      + "to an illegal alias: " + fieldAlias);
      }
   }
           
}
