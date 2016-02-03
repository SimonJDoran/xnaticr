/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generalUtilities;

import dataRepresentations.ContourImage;
import dataRepresentations.DicomEntityRepresentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author simon
 */
public class DicomAssignVariable
{
	protected static final int   DUMMY_INT   = -9909;
   protected static final float DUMMY_FLOAT = -9909.9f;
	public ArrayList<String>     errors      = new ArrayList<>();
	public ArrayList<String>     warnings    = new ArrayList<>();

	
	public int assignInt(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignInt(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public int assignInt(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		
		// Note that dcm4che's getInt function always returns a value.
		// I use DUMMY_INT as an "unlikely" value that can be taken to indicate
		// failure.
		int     tagValue   = dcmObj.getInt(tag, DUMMY_INT);
		boolean tagValueOK = (tagValue != DUMMY_INT);
		
		reportStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public int[] assignInts(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignInts(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public int[] assignInts(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		int[]   tagValue   = dcmObj.getInts(tag);
		boolean tagValueOK = (tagValue != null);
		
		reportStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public float assignFloat(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignFloat(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public float assignFloat(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		
		// Note that dcm4che's getFloat function always returns a value.
		// I use DUMMY_FLOAT as an "unlikely" value that can be taken to indicate
		// failure.
		float   tagValue   = dcmObj.getFloat(tag, DUMMY_FLOAT);
		boolean tagValueOK = (tagValue != DUMMY_FLOAT);
		
		reportStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public int[] assignFloats(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignInts(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public float[] assignFloats(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		float[]   tagValue = dcmObj.getFloats(tag);
		boolean tagValueOK = (tagValue != null);
		
		reportStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public String assignString(DicomObject dcmObj, int tag, int requirementType)
	{
		return assignString(dcmObj, tag, Integer.toString(requirementType));
	}
	
	public String assignString(DicomObject dcmObj, int tag, String requirementType)
	{
		String  tagValue   = null;
		boolean tagValueOK = false;
		boolean tagPresent = dcmObj.contains(tag);
		if (tagPresent)       tagValue   = dcmObj.getString(tag);
		if (tagValue != null) tagValueOK = (tagValue.length() != 0);
		
		reportStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;
	}
	
	
	public <T extends DicomEntityRepresentation> List<T> assignSequence(Class<T> cls, DicomObject dcmObj, int tag, int requirementType)
	{
		return assignSequence(cls, dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public <T extends DicomEntityRepresentation> List<T> assignSequence(Class<T> cls, DicomObject dcmObj, int tag, String requirementType)
	{
		List<T>      list       = new ArrayList<>();
		boolean      tagPresent = dcmObj.contains(tag);
		boolean      tagValueOK = false;
		DicomElement seq;
		if (tagPresent)
		{
			seq = dcmObj.get(tag);
			if (seq != null)
			{
				for (int i=0; i<seq.countItems(); i++)
				{
					DicomObject  itemDcmObj = seq.getDicomObject(i);
					
					// Now build a new instance of the class cls with the DICOM object
					// supplied in itemDcmObj.
					T           item = null;
					Constructor con;
					Class       dcmClass = DicomObject.class;

					try
					{
						con = cls.getDeclaredConstructor(dcmClass);
					}
					catch (NoSuchMethodException | SecurityException ex)
					{
						throw new RuntimeException("Programming issue: " + ex.getMessage());
					}
					
					try
					{
						item = (T) con.newInstance(itemDcmObj);
					}
					catch (InstantiationException | InvocationTargetException ex)
					{
						errors.add("Unable to instantiate object of type " + cls.getName()
						            + ". Suspected error in input DICOM.");								  
					}
					catch (IllegalAccessException exIA)
					{
						throw new RuntimeException("Programming issue: " + exIA.getMessage());
					}
					
					if (item != null)
					{
						if (item.dav.errors.isEmpty()) list.add(item);
						errors.addAll(item.dav.errors);
						warnings.addAll(item.dav.warnings); 
					}     
				}
			}
		}
		
		return list;
	}
	
	
	private void reportStatus(int tag, boolean tagPresent, boolean tagValueOK, String requirementType)
	{
		if (tagPresent && !tagValueOK)
		{
			errorTagContentsInvalid(tag);
			return;
		} 
		
		switch(requirementType)
		{
			case "1":  // Required
			case "1C": // Conditionally required.
				        // This is hard to treat for the general case. 
				        // Treat as if required and handle the conditions in the
				        // calling code.
				if (!tagPresent) errorRequiredTagNotPresent(tag);
				
			
			case "2":  // Required but can have zero length
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (!tagPresent) errorRequiredTagNotPresent(tag);
				
			
			case "3":  // Optional
				if (!tagPresent) warningOptionalTagNotPresent(tag);
				
			
			case "4": // Retired - this is not an official DICOM value, but
				       // caters for the case where an element is no longer in
				       // the latest DICOM standard.
				if (tagPresent) warningRetiredTagPresent(tag);
		}
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
