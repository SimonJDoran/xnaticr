/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generalUtilities;

import java.util.ArrayList;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author simon
 */
public class DicomAssignString
{
	public ArrayList<String> errors   = new ArrayList<>();
	public ArrayList<String> warnings = new ArrayList<>();

	
	public String assignString(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignString(dcmObj, tag, Integer.toString(requirementType));
	}
	
	public String assignString(DicomObject dcmObj, int tag, String requirementType)
	{
		String  tagValue   = null;
		boolean tagPresent = dcmObj.contains(tag);
		if (tagPresent) tagValue = dcmObj.getString(tag);
		
		switch(requirementType)
		{
			case "1":  // Required
			case "1C": // Conditionally required.
				        // This is hard to treat for the general case. 
				        // Treat as if required and handle the conditions in the
				        // calling code.
				if ((!tagPresent) || (tagValue == null) || (tagValue.length() == 0))
				{
					errorRequiredTagNotPresent(tag);
					return null;
				}
			
			case "2":  // Required but can have zero length
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (!tagPresent)
				{
					errorRequiredTagNotPresent(tag);
					return null;
				}
			
			case "3":  // Optional
				if (!tagPresent)
				{
					warningOptionalTagNotPresent(tag);
					return null;
				}
		}
		return tagValue;
	}
	
	
	public void errorRequiredTagNotPresent(int tag)
	{
		errors.add("Required tag " + Integer.toHexString(tag) + " not found in input: "
				  + Integer.toHexString(tag) + " " + (new BasicDicomObject()).nameOf(tag));
	}
	
	
	public void errorTagContentsInvalid(int tag)
	{
		errors.add("Required tag " + Integer.toHexString(tag) + " had invalid contents: "
				  + Integer.toHexString(tag) + " " + (new BasicDicomObject()).nameOf(tag));
	}
	
	
	public void warningOptionalTagNotPresent(int tag)
	{
		warnings.add("Optional tag " + Integer.toHexString(tag) + " not found in input: "
			     + Integer.toHexString(tag) + " " + (new BasicDicomObject()).nameOf(tag));
	}


	public void warningRetiredTagPresent(int tag)
	{
		warnings.add("Retired tag present in input: "
				  + Integer.toHexString(tag) + " " + (new BasicDicomObject()).nameOf(tag));
	}
}
