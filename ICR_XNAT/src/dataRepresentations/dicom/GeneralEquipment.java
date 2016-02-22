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
* Java class: GeneralEquipment.java
* First created on Feb 22, 2016 at 11:54:31 AM
* 
* Define a partial representation of the DICOM General Equipment
* module, including all mandatory and some optional components.
*********************************************************************/

package dataRepresentations.dicom;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class GeneralEquipment extends DicomEntityRepresentation
{
	public String   manufacturer;
	public String   institutionName;
	public String   institutionAddress;
	public String   stationName;
	public String   modelName;
	public String[] softwareVersions;
	
	public GeneralEquipment() {}
	
	public GeneralEquipment(DicomObject geDo)
	{
		manufacturer       = readString(geDo,  Tag.Manufacturer,          2);
		institutionName    = readString(geDo,  Tag.InstitutionName,       3);
		institutionAddress = readString(geDo,  Tag.InstitutionAddress,    3);
		stationName        = readString(geDo,  Tag.StationName,           3);
		modelName          = readString(geDo,  Tag.ManufacturerModelName, 3);
		softwareVersions   = readStrings(geDo, Tag.SoftwareVersions,      3);
	}
	
	@Override
	public void writeToDicom(DicomObject geDo)
	{
		writeString(geDo, Tag.Manufacturer,          VR.LO, 2, manufacturer);
		writeString(geDo, Tag.InstitutionName,       VR.LO, 3, institutionName);
		writeString(geDo, Tag.InstitutionAddress,    VR.ST, 3, institutionAddress);
		writeString(geDo, Tag.StationName,           VR.SH, 3, stationName);
		writeString(geDo, Tag.ManufacturerModelName, VR.LO, 3, modelName);
		writeStrings(geDo, Tag.SoftwareVersions,     VR.LO, 3, softwareVersions);
	}
}
