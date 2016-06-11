/********************************************************************
* Copyright (c) 2016, Institute of Cancer Research
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
* Java class: CatCatalogMdComplexType.java
* First created on Mar 2, 2016 at 5:30:08 PM
* 
* Creation of metadata XML for cat:catalog
* 
* Eventually, the plan for this whole package is to replace the
* explicit writing of the XML files with a higher level interface,
* e.g., JAXB. However, this is for a later refactoring.
*********************************************************************/


package xnatMetadataCreators;

import dataRepresentations.xnatSchema.Catalog;
import dataRepresentations.xnatSchema.CatalogEntry;
import dataRepresentations.xnatSchema.MetaField;
import exceptions.XMLException;
import java.io.IOException;
import xmlUtilities.DelayedPrettyPrinterXmlWriter;

public class CatCatalogMdComplexType extends MdComplexType
{
	protected  Catalog cat;
	
	public CatCatalogMdComplexType() {}
	
	public CatCatalogMdComplexType(Catalog cat)
	{
		this.cat = cat;
	}
	
	public CatCatalogMdComplexType(Catalog cat, DelayedPrettyPrinterXmlWriter dppXML)
	{
		this.cat    = cat;
		this.dppXML = dppXML;
	}

	@Override
	public void insertXml() throws IOException, XMLException
	{
		dppXML.delayedWriteAttribute("ID",          cat.id)
				.delayedWriteAttribute("name",        cat.name)
				.delayedWriteAttribute("description", cat.description);
		
		
		dppXML.delayedWriteEntity("metaFields");
		for (MetaField mf : cat.metaFieldList)
		{
			(new MetaFieldMdComplexType(mf, dppXML)).insertXmlAsElement("metaField");
		}
		dppXML.delayedEndEntity();
		
		
		dppXML.delayedWriteEntity("tags");
		for (String tag : cat.tagList)
		{
			dppXML.delayedWriteEntityWithText("tag", tag);
		}
		dppXML.delayedEndEntity();
		
		
		dppXML.delayedWriteEntity("sets");
		for (Catalog set : cat.setList)
		{
			(new CatCatalogMdComplexType(set, dppXML)).insertXmlAsElement("entrySet");
		}
		dppXML.delayedEndEntity();
		
		
		dppXML.delayedWriteEntity("entries");
		for (CatalogEntry entry : cat.entryList)
		{
			(new CatEntryMdComplexType(entry, dppXML)).insertXmlAsElement("entry");
		}
		dppXML.delayedEndEntity();
	}
   
   @Override
   public String getRootElementName()
   {
      return "Catalog";
   }
}


