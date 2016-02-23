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
* Java class: DicomEntityRepresentation.java
* First created on Jan 29, 2016 at 12:08:00 PM
* 
* Provide the common features that will be used by all classes that
* represent DICOM entities (this will normally relate to a
* representation of a DICOM sequence.
*********************************************************************/
package dataRepresentations.dicom;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import xnatUploader.TextRepresentation;

public abstract class DicomEntity implements TextRepresentation
{
   protected static final int   DUMMY_INT   = -9909;
   protected static final float DUMMY_FLOAT = -9909.9f;
	public ArrayList<String>     errors      = new ArrayList<>();
	public ArrayList<String>     warnings    = new ArrayList<>();
	
	protected DicomEntity(){}
	protected DicomEntity(DicomObject src){}
   
   protected abstract void writeToDicom(DicomObject dcmObj);
   
   public int readInt(DicomObject dcmObj, int tag, int requirementType)
	{
		return readInt(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public int readInt(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		
		// Note that dcm4che's getInt function always returns a value.
		// I use DUMMY_INT as an "unlikely" value that can be taken to indicate
		// failure.
		int     tagValue   = dcmObj.getInt(tag, DUMMY_INT);
		boolean tagValueOK = (tagValue != DUMMY_INT);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public int[] readInts(DicomObject dcmObj, int tag, int requirementType)
	{
		return readInts(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public int[] readInts(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		int[]   tagValue   = dcmObj.getInts(tag);
		boolean tagValueOK = (tagValue != null);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public float readFloat(DicomObject dcmObj, int tag, int requirementType)
	{
		return readFloat(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public float readFloat(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		
		// Note that dcm4che's getFloat function always returns a value.
		// I use DUMMY_FLOAT as an "unlikely" value that can be taken to indicate
		// failure.
		float   tagValue   = dcmObj.getFloat(tag, DUMMY_FLOAT);
		boolean tagValueOK = (tagValue != DUMMY_FLOAT);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public float[] readFloats(DicomObject dcmObj, int tag, int requirementType)
	{
		return readFloats(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public float[] readFloats(DicomObject dcmObj, int tag, String requirementType)
	{
		boolean tagPresent = dcmObj.contains(tag);
		float[]   tagValue = dcmObj.getFloats(tag);
		boolean tagValueOK = (tagValue != null);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;	
	}
	
	
	public String readString(DicomObject dcmObj, int tag, int requirementType)
	{
		return readString(dcmObj, tag, Integer.toString(requirementType));
	}
	
	public String readString(DicomObject dcmObj, int tag, String requirementType)
	{
		String  tagValue   = null;
		boolean tagValueOK = false;
		boolean tagPresent = dcmObj.contains(tag);
		if (tagPresent)       tagValue   = dcmObj.getString(tag);
		if (tagValue != null) tagValueOK = (tagValue.length() != 0);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;
	}
	
	
	public String[] readStrings(DicomObject dcmObj, int tag, int requirementType)
	{
		return readStrings(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public String[] readStrings(DicomObject dcmObj, int tag, String requirementType)
	{
		String[]  tagValue   = null;
		boolean   tagValueOK = false;
		boolean   tagPresent = dcmObj.contains(tag);
		if (tagPresent)       tagValue   = dcmObj.getStrings(tag);
		if (tagValue != null) tagValueOK = (tagValue.length != 0);
		
		reportReadStatus(tag, tagPresent, tagValueOK, requirementType);
		return tagValue;
	}
	
	
	public <T extends DicomEntity> List<T> readSequence(Class<T> cls, DicomObject dcmObj, int tag, int requirementType)
	{
		return readSequence(cls, dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public <T extends DicomEntity> List<T> readSequence(Class<T> cls, DicomObject dcmObj, int tag, String requirementType)
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
						if (item.errors.isEmpty()) list.add(item);
						errors.addAll(item.errors);
						warnings.addAll(item.warnings); 
					}     
				}
			}
		}
		
		return list;
	}
	

                   
	private void reportReadStatus(int tag, boolean tagPresent, boolean tagValueOK, String requirementType)
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
	

   
   	
   public void writeInt(DicomObject dcmObj, int tag, VR vr,
                        int requirementType, int value)
   {
      writeInt(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeInt(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, int value)
   {
      if (value != DUMMY_INT) dcmObj.putInt(tag, vr, value);
      reportWriteStatus(tag, (value == DUMMY_INT), requirementType);
   }
   
   
   public void writeInts(DicomObject dcmObj, int tag, VR vr,
                        int requirementType, int[] value)
   {
      writeInts(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeInts(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, int[] value)
   {
      if (value != null) dcmObj.putInts(tag, vr, value);
      reportWriteStatus(tag, (value==null), requirementType);
   }
   
   
   
   
   public void writeFloat(DicomObject dcmObj, int tag, VR vr,
                          int requirementType, float value)
   {
      writeFloat(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeFloat(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, float value)
   {
      if (value != DUMMY_INT) dcmObj.putFloat(tag, vr, value);
      reportWriteStatus(tag, (value == DUMMY_FLOAT), requirementType);
   }
   
   
   
   public void writeFloats(DicomObject dcmObj, int tag, VR vr,
                        int requirementType, float[] value)
   {
      writeFloats(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeFloats(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, float[] value)
   {
      if (value != null) dcmObj.putFloats(tag, vr, value);
      reportWriteStatus(tag, (value == null), requirementType);
   }
   
   
   
   public void writeString(DicomObject dcmObj, int tag, VR vr,
                          int requirementType, String value)
   {
      writeString(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeString(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, String value)
   {
      if (value != null) dcmObj.putString(tag, vr, value);
      reportWriteStatus(tag, (value == null), requirementType);
   }
   

	public void writeStrings(DicomObject dcmObj, int tag, VR vr,
                          int requirementType, String[] value)
   {
      writeStrings(dcmObj, tag, vr, Integer.toString(requirementType), value);
   }
   
   
   public void writeStrings(DicomObject dcmObj, int tag, VR vr,
                        String requirementType, String[] value)
   {
      if (value != null) dcmObj.putStrings(tag, vr, value);
      reportWriteStatus(tag, (value == null), requirementType);
   }
	
   
   public <T extends DicomEntity> void
                           writeSequence(DicomObject dcmObj, int tag, VR vr,
                                         int requirementType, List<T> list)
	{
		writeSequence(dcmObj, tag, vr, Integer.toString(requirementType), list);
	}
	
	
	public <T extends DicomEntity> void
                           writeSequence(DicomObject dcmObj, int tag, VR vr,
                                         String requirementType, List<T> list)
	{
		if (list != null)
		{
         if (!list.isEmpty())
         {
            DicomElement dcmEl = dcmObj.putSequence(tag);
            for (T item : list)
            {
               DicomObject itemDo = new BasicDicomObject();
               itemDo.setParent(dcmObj);
               dcmEl.addDicomObject(itemDo);
               item.writeToDicom(itemDo);
            }
         }
      }
      
      reportWriteStatus(tag, ((list == null) || (list.isEmpty())), requirementType);
			
	}
   
   
   
   public void reportWriteStatus(int tag, boolean isNull, String requirementType)
   {
      switch(requirementType)
		{
			case "1":  // Required
			case "1C": // Conditionally required.
				        // This is hard to treat for the general case. 
				        // Treat as if required and handle the conditions in the
				        // calling code.
				if (isNull) errorRequiredTagNotPresent(tag);
				
			
			case "2":  // Required but can have zero length
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (isNull) errorRequiredTagNotPresent(tag);
				
			
			case "3":  // Optional
				if (isNull) warningOptionalTagNotPresent(tag);
				
			
			case "4": // Retired - this is not an official DICOM value, but
				       // caters for the case where an element is no longer in
				       // the latest DICOM standard.
				if (!isNull) warningRetiredTagPresent(tag);
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
	
	
	public <T extends DicomEntity> T deepCopy()
	{
		// Create the empty object.
		Class cls = this.getClass();
		T dest;
		try
		{
			dest = (T) cls.newInstance();
		}
		catch (InstantiationException exIE)
		{
			errors.add("Unable to instantiate object of type " + cls.getName()
							+ ". Suspected error in input DICOM.");
			return null;
		}
		catch (IllegalAccessException exIA)
		{
			throw new RuntimeException("Programming issue: " + exIA.getMessage());
		}
		
		// Now fill all the fields of the object.
		Field[] fields = cls.getDeclaredFields();
		for (Field fld : fields)
		{
			Object a;
			try
			{
				a = fld.get(this);
			}
			catch (IllegalAccessException exIA)
			{
				throw new RuntimeException("Programming issue: " + exIA.getMessage());
			}
			
			// For all objects that are subtypes of DicomEntity
			// (i.e., the only ones that can call this method) the only types
			// of field present are:
			// (1) other DicomEntityRepresentations, which are copied by a
			//     call to their own deepCopy method;
			// (2) Lists of DicomEntityRepresentations;
			// (3) primitive types or String, which can be cloned.
			if (a != null)
			{
				if (a instanceof DicomEntity)
				{
					DicomEntity b = (DicomEntity) a;

					try
					{
						fld.set(dest, b.deepCopy());
					}
					catch (IllegalAccessException exIA)
					{
						throw new RuntimeException("Programming issue: " + exIA.getMessage());
					}
				}

				else if (a instanceof List)
				{
					List<DicomEntity> b = (List) a;
					try
					{
						fld.set(dest, deepCopyList(b));
					}
					catch (IllegalAccessException exIA)
					{
						throw new RuntimeException("Programming issue: " + exIA.getMessage());
					}
				}
				
				else
				{
					try
					{
						fld.set(dest, a);
					}
					catch (IllegalAccessException exIA)
					{
						throw new RuntimeException("Programming issue: " + exIA.getMessage());
					}
				}
				
			}
		}
		
		return dest;
	}
	
	
	public <T extends DicomEntity> List<T> deepCopyList(List<T> srcList)
	{
		List<T> destList = new ArrayList<>();
		for (T srcD : srcList)
		{
			T destD = (T) srcD.deepCopy();
			destList.add(destD);
		}
		
		return destList;
	}
	

	
	@Override
	public String getTextRepresentation()
	{
		return getDicomTextRepresentation();
	}
	
	
	public <T extends DicomEntity> String getDicomTextRepresentation()
	{
		return getDicomTextRepresentation(new StringBuilder());
	}
	
	
	public <T extends DicomEntity> String getDicomTextRepresentation(StringBuilder indent)
	{
		final String  IND    = "   ";
		StringBuilder sb     = new StringBuilder();
		Class         cls    = this.getClass();
		Field[]       fields = cls.getDeclaredFields(); 
		
		for (Field fld : fields)
		{
			sb.append("\n").append(indent).append(fld.getName()).append(IND);
			
			Object a;
			try
			{
				a = fld.get(this);
			}
			catch (IllegalAccessException exIA)
			{
				throw new RuntimeException("Programming issue: " + exIA.getMessage());
			}
			
			// For all objects that are subtypes of DicomEntity
			// (i.e., the only ones that can call this method) the only types
			// of field present are:
			// (1) other DicomEntityRepresentations, whose text representations
			//     are obtained by a call to their own getDicomTextRepresentation method;
			// (2) Lists of DicomEntityRepresentations;
			// (3) primitive types or String, whose values can be obtained directly.
			if (a != null)
			{
				if (a instanceof DicomEntity)
				{
					DicomEntity b = (DicomEntity) a;
					sb.append(b.getClass().getSimpleName());
					indent.append(IND);					
					sb.append(b.getDicomTextRepresentation(indent));
					indent.delete(indent.length()-IND.length(), indent.length()-1);
				}

				else if (a instanceof List)
				{
					List<DicomEntity> b = (List<DicomEntity>) a;
					sb.append(getListTextRepresentation(b, indent));
				}
				
				else sb.append(a);				
			}
		}
		
		return sb.toString();
	}
	
	
	public <T extends DicomEntity> String getListTextRepresentation(List<T> list, StringBuilder indent)
	{
		final String  IND   = "   ";
		StringBuilder sb    = new StringBuilder();
		int           count = 0;
		
		sb.append(list.getClass().getSimpleName());
		indent.append(IND);
		
		for (T item : list)
		{
			sb.append("Item ").append(++count);
			sb.append(item.getDicomTextRepresentation(indent));
		}
		
		indent.delete(indent.length()-IND.length(), indent.length()-1);
		
		return sb.toString();
	}
	
}

