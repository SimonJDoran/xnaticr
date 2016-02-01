/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generalUtilities;

import java.util.ArrayList;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author simon
 */
public class DicomAssignString
{
	public ArrayList<String> errors   = new ArrayList<>();
	public ArrayList<String> warnings = new ArrayList<>();

	
	public String assignString(DicomObject bdo, int tag, int requirementType)
	{
		return assignString(bdo, tag, Integer.toString(requirementType));
	}
	
	public String assignString(DicomObject bdo, int tag, String requirementType)
	{
		String  tagValue   = null;
		boolean tagPresent = bdo.contains(tag);
		if (tagPresent) tagValue = bdo.getString(tag);
		
		switch(requirementType)
		{
			case "1":  // Required
			case "1C": // Conditionally required. This is hard to treat for the general
				        // case. Treat as if required and handle the conditions in the
				        // calling code.
				if ((!tagPresent) || (tagValue == null) || (tagValue.length() == 0))
				{
					errors.add("Required tag not found in input: "
					                   + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
			
			case "2":  // Required
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (!tagPresent)
				{
					errors.add("Required tag not found in input: "
							          + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
			
			case "3":  // Optional
				if (!tagPresent)
				{
					warnings.add("Optional tag not present in input: "
							         + Integer.toHexString(tag) + bdo.nameOf(tag));
					return null;
				}
		}
		return tagValue;
	}



}
