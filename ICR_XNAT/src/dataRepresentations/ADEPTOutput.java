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

/********************************************************************
* @author Simon J Doran
* Java class: ADEPTOutput.java
* First created on Nov 11, 2014 at 6:00:13 PM
* 
* Defines a representation of the output of the ADEPT diffusion
* imaging tool, including methods to read the data in from an XML
* file.
*********************************************************************/

package dataRepresentations;

import exceptions.XMLException;
import exceptions.XNATException;
import java.util.zip.DataFormatException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.w3c.dom.Document;
import xnatDAO.XNATProfile;

public class ADEPTOutput extends XnatUploadRepresentation implements RtStructWriter
{
	public Document doc;
	
	/**
    * This constructor is private, because the public creation of these objects
    * occurs via a call to ADEPTOutput.getInstanceFromFile.
    */
   private ADEPTOutput()
   {
		
	}
	
	 /**
    * Public creator from an MRI file source.
    * @param inputFile a File, whose contents represent a valid MRI output
    * @param xnprf an XNAT profile, already connected to an XNAT database, which
    * we can use to query the databases for image dependencies.
    * @return an MRIOutput instance populated from the inputFile
    */
   public static ADEPTOutput getInstanceFromXML(Document ADEPTDoc, XNATProfile xnprf)
                             throws DataFormatException, XMLException, XNATException
	{
		ADEPTOutput adept = new ADEPTOutput();
      adept.populateFromXML(ADEPTDoc, xnprf);
     
      return adept;
	}
	
	
	
	/**
    * Parse the ADEPT file and place the results in the
    * object's structure. Note that This implementation is currently incomplete
    * because of lack of appropriate examples of some of the tags.
    * @param ADEPTDoc
    * @param xnprf
    * @throws DataFormatException
    * @throws XMLException 
    */
   protected void populateFromXML(Document ADEPTDoc, XNATProfile xnprf)
                  throws DataFormatException, XMLException, XNATException
   {
      this.doc   = ADEPTDoc;
      this.xnprf = xnprf;
	}
	
	
	public DicomObject createDICOM() throws Exception
   {
		DicomObject odo = new BasicDicomObject();
		
		return odo;
	}
}
