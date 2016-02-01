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
* Define a representation of the Derivation Code DICOM sequence
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: DerivationCode.java
* First created on Feb 1, 2016 at 5:00:03 PM
*********************************************************************/

package dataRepresentations;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class DerivationCode extends DicomEntityRepresentation
{
	public static final String SEPARATOR = "_!DC!_"; 
	public String codeValue;
	public String codingSchemeDesignator;
	public String codingSchemeVersion;
	public String codeMeaning;
	public String contextIdentifier;
	public String contextUid;
	public String mappingResource;
	public String contextGroupVersion;
	public String contextGroupExtensionFlag;
	public String contextGroupLocalVersion;
	public String contextGroupExtensionCreatorUid;
	
	public DerivationCode(DicomObject dcDo)
	{
		codeValue              = das.assignString(dcDo, Tag.CodeValue, 1);
		codingSchemeDesignator = das.assignString(dcDo, Tag.CodingSchemeDesignator, 1);
		codingSchemeVersion    = das.assignString(dcDo, Tag.CodingSchemeVersion, "1C");
		codeMeaning            = das.assignString(dcDo, Tag.CodeMeaning, 1);
		contextIdentifier      = das.assignString(dcDo, Tag.ContextIdentifier, 3);
		contextUid             = das.assignString(dcDo, Tag.ContextUID, 3);
		
		if (contextIdentifier != null)
		   mappingResource     = das.assignString(dcDo, Tag.MappingResource, 1);
		
		if (contextIdentifier != null)
		   contextGroupVersion = das.assignString(dcDo, Tag.MappingResource, 1);
		
		contextGroupExtensionFlag
		                       = das.assignString(dcDo, Tag.ContextGroupExtensionFlag, 3);
		if (contextGroupExtensionFlag != null)
		{
			if (contextGroupExtensionFlag.equals("Y"))
			{
				contextGroupLocalVersion
		                       = das.assignString(dcDo, Tag.ContextGroupLocalVersion, "1C");
				contextGroupExtensionCreatorUid
					              = das.assignString(dcDo, Tag.ContextGroupExtensionCreatorUID, "1C");
			}
		}		
	}
	
	public String getAsSingleString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append((codeValue == null) ? "" : codeValue);
		sb.append(SEPARATOR);
		sb.append((codingSchemeDesignator == null) ? "" : codingSchemeDesignator);
		sb.append(SEPARATOR);
		sb.append((codingSchemeVersion == null) ? "" : codingSchemeVersion);
		sb.append(SEPARATOR);
		sb.append((codeMeaning == null) ? "" : codeMeaning);
		sb.append(SEPARATOR);
		sb.append((contextIdentifier == null) ? "" : contextIdentifier);
		sb.append(SEPARATOR);
		sb.append((contextUid == null) ? "" : contextUid);
		sb.append(SEPARATOR);
		sb.append((mappingResource == null) ? "" : mappingResource);
		sb.append(SEPARATOR);
		sb.append((contextGroupVersion == null) ? "" : contextGroupVersion);
		sb.append(SEPARATOR);
		sb.append((contextGroupExtensionFlag == null) ? "" : contextGroupExtensionFlag);
		sb.append(SEPARATOR);
		sb.append((contextGroupLocalVersion == null) ? "" : contextGroupLocalVersion);
		sb.append(SEPARATOR);
		sb.append((contextGroupExtensionCreatorUid == null) ? "" : contextGroupExtensionCreatorUid);
		sb.append(SEPARATOR);
		
		return sb.toString();
	}
}
