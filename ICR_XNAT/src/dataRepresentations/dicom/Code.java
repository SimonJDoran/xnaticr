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

/*********************************************************************
* @author Simon J Doran
* Java class: DerivationCode.java
* First created on Feb 1, 2016 at 5:00:03 PM
* 
* Define a representation of the DICOM Code Sequence.
* Code Sequence is a ubiquitous type of sequence, defined as a macro
* in the Part 3 of the DICOM reference. Notice that the equivalent
* code sequence (0008,0121) allows part of the object to be nested.
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: DerivationCode.java
* First created on Feb 1, 2016 at 5:00:03 PM
*********************************************************************/

package dataRepresentations.dicom;

import dataRepresentations.dicom.DicomEntityRepresentation;
import java.util.List;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class Code extends DicomEntityRepresentation
{
	public  static final String SEPARATOR        = "_!DC!_";
	private static final int    TagLongCodeValue = 0x0008_0119;
	private static final int    TagUrnCodeValue  = 0x0008_0120;
	
	public String codeValue;
	public String codingSchemeDesignator;
	public String codingSchemeVersion;
	public String codeMeaning;
   public String longCodeValue;
   public String urnCodeValue;
	public List<Code> equivalentCodeList;
	public String contextIdentifier;
	public String contextUid;
	public String mappingResource;
	public String contextGroupVersion;
	public String contextGroupExtensionFlag;
	public String contextGroupLocalVersion;
	public String contextGroupExtensionCreatorUid;
	
	public Code(DicomObject cDo)
	{
		
		// Notice that there are a lot of tags that have a requirement of "1C".
		// Testing that the conditions are met for inclusion on read is somewhat
		// tricky, so simply assume that if the tag is present, then it should be
		// read.
      if (cDo.contains(Tag.CodeValue)) codeValue = readString(cDo, Tag.CodeValue, "1C");
      
      // N.B. Current version of DCM4CHE doesn't seem to support Tag.LongCodeValue
      //      or Tag.URNCodeValue.	
		if (cDo.contains(TagLongCodeValue)) longCodeValue = readString(cDo, TagLongCodeValue, "1C");
		if (cDo.contains(TagLongCodeValue)) urnCodeValue  = readString(cDo, TagUrnCodeValue, "1C");
         
		if (cDo.contains(Tag.CodingSchemeVersion))
      {
         codingSchemeDesignator = readString(cDo, Tag.CodingSchemeDesignator, "1C");
         if (cDo.contains(Tag.CodingSchemeVersion))
            codingSchemeVersion = readString(cDo, Tag.CodingSchemeVersion, "1C");
      }
      
      codeMeaning            = readString(cDo, Tag.CodeMeaning, 1);
		contextIdentifier      = readString(cDo, Tag.ContextIdentifier, 3);
		contextUid             = readString(cDo, Tag.ContextUID, 3);
		
		if (contextIdentifier != null)
		{
		   mappingResource     = readString(cDo, Tag.MappingResource, "1C");
		   contextGroupVersion = readString(cDo, Tag.MappingResource, "1C");
		}
		
		contextGroupExtensionFlag
		                       = readString(cDo, Tag.ContextGroupExtensionFlag, 3);
		if (contextGroupExtensionFlag != null)
		{
			if (contextGroupExtensionFlag.equals("Y"))
			{
				contextGroupLocalVersion
		                       = readString(cDo, Tag.ContextGroupLocalVersion, "1C");
				contextGroupExtensionCreatorUid
					              = readString(cDo, Tag.ContextGroupExtensionCreatorUID, "1C");
			}
		}		
	}
   
   
   public void writeToDicom(DicomObject dcDo)
   {
		// Note: the "1C" elements should really be tested appropriately to determine
		// validity. However, for the moment, we are assuming that these will be
		// created appropriately by the application that manipulates the object
		// and calls writeToDicom. writeString does check for null, but will flag
		// an error condition if we try to write a null.
		if (codeValue != null)
			writeString(dcDo, Tag.CodeValue, VR.SH, "1C", codeValue);
		
		if (longCodeValue != null)
			writeString(dcDo, TagLongCodeValue, VR.SH, "1C", codeValue);
		
		if (urnCodeValue != null)
			writeString(dcDo, TagUrnCodeValue, VR.SH, "1C", urnCodeValue);
		
      if (codingSchemeDesignator != null)
			writeString(dcDo, Tag.CodingSchemeDesignator, VR.SH, "1C",  codingSchemeDesignator);
      
		if (codingSchemeVersion != null)
			writeString(dcDo, Tag.CodingSchemeVersion, VR.SH, "1C",  codingSchemeVersion);
		
      writeString(dcDo, Tag.CodeMeaning,            VR.LO, 1, codeMeaning);
      writeString(dcDo, Tag.ContextIdentifier,      VR.CS, 3, contextIdentifier);
      writeString(dcDo, Tag.ContextUID,             VR.UI, 3, contextUid);
      
		if (contextIdentifier != null)
		{
			writeString(dcDo, Tag.MappingResource,     VR.CS, "1C", mappingResource);
			writeString(dcDo, Tag.ContextGroupVersion, VR.DT, "1C", contextGroupVersion);
		}
		
		writeString(dcDo, Tag.ContextGroupExtensionFlag, VR.CS, 3, contextGroupExtensionFlag);
		
      if (contextGroupExtensionFlag != null)
		{
			if (contextGroupExtensionFlag.equals("Y"))
			{
				writeString(dcDo, Tag.ContextGroupLocalVersion,         VR.DT, "1C", contextGroupLocalVersion);
				writeString(dcDo, Tag.ContextGroupExtensionCreatorUID,  VR.UI, "1C", contextGroupExtensionCreatorUid);
			}
		}		      
   }
	
	public String getAsSingleString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append((codeValue              == null) ? "" : codeValue);
		sb.append(SEPARATOR);
		sb.append((longCodeValue          == null) ? "" : longCodeValue);
		sb.append(SEPARATOR);
		sb.append((urnCodeValue           == null) ? "" : urnCodeValue);
		sb.append(SEPARATOR);
		sb.append((codingSchemeDesignator == null) ? "" : codingSchemeDesignator);
		sb.append(SEPARATOR);
		sb.append((codingSchemeVersion    == null) ? "" : codingSchemeVersion);
		sb.append(SEPARATOR);
		sb.append((codeMeaning            == null) ? "" : codeMeaning);
		sb.append(SEPARATOR);
		sb.append((contextIdentifier      == null) ? "" : contextIdentifier);
		sb.append(SEPARATOR);
		sb.append((contextUid             == null) ? "" : contextUid);
		sb.append(SEPARATOR);
		sb.append((mappingResource        == null) ? "" : mappingResource);
		sb.append(SEPARATOR);
		sb.append((contextGroupVersion    == null) ? "" : contextGroupVersion);
		sb.append(SEPARATOR);
		sb.append((contextGroupExtensionFlag == null) ? "" : contextGroupExtensionFlag);
		sb.append(SEPARATOR);
		sb.append((contextGroupLocalVersion  == null) ? "" : contextGroupLocalVersion);
		sb.append(SEPARATOR);
		sb.append((contextGroupExtensionCreatorUid == null) ? "" : contextGroupExtensionCreatorUid);
		sb.append(SEPARATOR);
		
		return sb.toString();
	}
}
