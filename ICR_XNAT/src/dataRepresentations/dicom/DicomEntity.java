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
import java.util.Arrays;
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
		
		// Variable zeroLength not relevant here, but required for compatibility.
		boolean zeroLength = !(tagValueOK);
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
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
		boolean zeroLength = true;
		if (tagValue != null) zeroLength = (tagValue.length == 0);
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
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
		
		// Variable zeroLength not relevant here, but required for compatibility.
		boolean zeroLength = !(tagValueOK);
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
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
		boolean zeroLength = true;
		if (tagValue != null) zeroLength = (tagValue.length == 0);
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
		return tagValue;	
	}
	
	
	public String readString(DicomObject dcmObj, int tag, int requirementType)
	{
		return readString(dcmObj, tag, Integer.toString(requirementType));
	}
	
	public String readString(DicomObject dcmObj, int tag, String requirementType)
	{
		String  tagValue   = null;

		// Note: we can't be certain of whether a string value is OK until
		//       we know whether we are dealing with a requirement type of 2.
		boolean tagValueOK = true;
		boolean zeroLength = true;
		
		boolean tagPresent = dcmObj.contains(tag);
		if (tagPresent) tagValue = dcmObj.getString(tag);
		

		if (tagValue != null) zeroLength = (tagValue.length() == 0);
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
		return tagValue;
	}
	
	
	public String[] readStrings(DicomObject dcmObj, int tag, int requirementType)
	{
		return readStrings(dcmObj, tag, Integer.toString(requirementType));
	}
	
	
	public String[] readStrings(DicomObject dcmObj, int tag, String requirementType)
	{
		String[] tagValue   = null;
		boolean  tagValueOK = true;
		boolean  tagPresent = dcmObj.contains(tag);
		boolean  zeroLength = true;
		
		if (tagPresent) tagValue = dcmObj.getStrings(tag);
		if (tagValue != null)
		{
			tagValueOK = (tagValue.length != 0);
			if (tagValue.length != 0)
			{
				tagValueOK = true;
				zeroLength = false;
				for (int i=0; i<tagValue.length; i++)
				{
					if (tagValue[i].length() == 0) zeroLength = true;
				}
			}
		}
		
		reportReadStatus(tag, tagPresent, tagValueOK, zeroLength, requirementType);
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
	

                   
	private void reportReadStatus(int tag, boolean tagPresent, boolean tagValueOK,
										   boolean zeroLength, String requirementType)
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
				if ((!tagPresent) || zeroLength) errorRequiredTagNotPresent(tag);
				break;
				
			
			case "2":  // Required but can have zero length
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (!tagPresent) errorRequiredTagNotPresent(tag);
				break;
				
			
			case "3":  // Optional
				if (!tagPresent) warningOptionalTagNotPresent(tag);
				break;
				
			
			case "4": // Retired - this is not an official DICOM value, but
				       // caters for the case where an element is no longer in
				       // the latest DICOM standard.
				if (tagPresent) warningRetiredTagPresent(tag);
				break;
				
				
			default: throw new RuntimeException("Programming error: invalid case in DicomEntity");			
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
				break;
				
			
			case "2":  // Required but can have zero length
			case "2C": // Conditionally required but can have zero length.
				        // This is hard to treat for the general case. Treat as
				        // required but can have zero length and handle the
				        // conditions in the calling code.
				if (isNull) errorRequiredTagNotPresent(tag);
				break;
				
			
			case "3":  // Optional
				if (isNull) warningOptionalTagNotPresent(tag);
				break;
				
			
			case "4": // Retired - this is not an official DICOM value, but
				       // caters for the case where an element is no longer in
				       // the latest DICOM standard.
				if (!isNull) warningRetiredTagPresent(tag);
				break;
				
			
			default: throw new RuntimeException("Programming error: invalid case in DicomEntity");
		}
   }
   
   
   	
	public void errorRequiredTagNotPresent(int tag)
	{
		errors.add("Required tag " + Integer.toHexString(tag) + " not found in input: "
				  + (new BasicDicomObject()).nameOf(tag));
	}
	
	
	public void errorTagContentsInvalid(int tag)
	{
		errors.add("Required tag " + Integer.toHexString(tag) + " had invalid contents: "
				  + (new BasicDicomObject()).nameOf(tag));
	}
	
	
	public void warningOptionalTagNotPresent(int tag)
	{
		warnings.add("Optional tag " + Integer.toHexString(tag) + " not found in input: "
			     + (new BasicDicomObject()).nameOf(tag));
	}


	public void warningRetiredTagPresent(int tag)
	{
		warnings.add("Retired tag "  + Integer.toHexString(tag) + " present in input: "
				 + (new BasicDicomObject()).nameOf(tag));
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
         if (!fld.getName().equals("contourImage"))
            System.out.println("Here");
			try
			{
				Object a = fld.get(this);
            Object b = deepCopyField(a);
				fld.set(dest, b);
			}
			catch (IllegalAccessException | IllegalArgumentException | NullPointerException | ExceptionInInitializerError ex)
			{
				throw new RuntimeException("Programming issue: " + ex.getMessage());
			}
		}
		return dest;
	}	
		
	private Object deepCopyField(Object a)
   {	
      // For all objects that are subtypes of DicomEntity
      // (i.e., the only ones that can call this method) the only types
      // of field present are:
      // (1) other DicomEntityRepresentations, which are copied by a
      //     call to their own deepCopy method;
      //
      // (2) primitive wrapper types or String, which can be cloned.
      //
      // (3) Lists of the allowable types 1-3;
      //
      // Any other types present will generate an error.

      if (a == null) return null;

      try
      {
         Class cla = a.getClass();
         if (a instanceof DicomEntity)
         {
            DicomEntity b = (DicomEntity) a;
            return b.deepCopy();
         }

         else if (cla == Byte.class)      return new Byte((Byte) a);
         else if (cla == Short.class)     return new Short((Short) a);
         else if (cla == Integer.class)   return new Integer((Integer) a);
         else if (cla == Long.class)      return new Long((Long) a);
         else if (cla == Float.class)     return new Float((Float) a);
         else if (cla == Double.class)    return new Double((Double) a);
         else if (cla == Character.class) return new Character((Character) a);
         else if (cla == Boolean.class)   return new Boolean((Boolean) a);
         else if (cla == String.class)    return new String((String) a);

         // Primitive wrapper types
         // The commented out version is actually longer than the explicit,
         // but also didn't work because the first test didn't seem to do
         // what I wanted!

//				else if ((cla.isPrimitive()) || (a instanceof String))
//				{
//					Class[] parameterTypes = new Class[]{cla};
//					try
//					{
//						Constructor con = cla.getConstructor(parameterTypes);
//						return con.newInstance(a);
//					}
//					catch (NoSuchMethodException | InstantiationException ex)
//					{
//						throw new RuntimeException("Programming issue: " + ex.getMessage());
//					}
//				}

         // Lists
         else if (a instanceof List)
         {
            Object a0;
            try
            {
               // Check whether a is a List of primitives, which are not handled.
               if (((List) a).isEmpty()) return null;
               a0 = ((List) a).get(0);
            }
            catch (Exception ex)
            {
               throw new RuntimeException("deepCopy() is not configured to clone objects containing primitives.");
            }
            
            return getGeneralList(a, a0);
         }
               
         else
         {
            throw new RuntimeException("Programming issue: deepCopy() may not be used with objects that do not inherit from DicomEntity.");
         }
      }
      catch (Exception ex)
      {
         System.out.println("Primitive types drop out here.");
      }
		
		return null; // This statement should be unreachable.
	}
         
   
	private List getGeneralList(Object a, Object a0)
   {
      // List of Lists
      if (a0 instanceof List)
      {
         Object aa0;
         try
         {
            // Check whether a is a List of primitives, which are not handled.
            if (((List) a0).isEmpty()) return null;
            aa0 = ((List) a0).get(0);
         }
         catch (Exception ex)
         {
            throw new RuntimeException("deepCopy() is not configured to clone objects containing primitives.");
         }
            
         List o  = new ArrayList();
         List la = (List) a;
         for (int i=0; i<la.size(); i++)
         {
            o.add(getGeneralList(la.get(i), aa0));
         }
         
         return o;
      }
      
      // List of other DicomEntities - each needs to be deep copied separately.
      if (a0 instanceof DicomEntity)
      {
         List<DicomEntity> b = (List) a;
         return deepCopyList(b);
      }

      // List of primitives - these can be copied directly.
      // It's frustrating that there doesn't seem to be a clever way
      // for doing this with generics, using something along the 
      // lines of List<? extends primitive> b.

      if (a0 instanceof Byte)
      {
         List<Byte> lb = (List<Byte>) a;
         List<Byte> b  = new ArrayList<>();
         for (Byte bb : lb) b.add(bb);
         return b;
      }

      if (a0 instanceof Short)
      {
         List<Short> ls = (List<Short>) a;
         List<Short> b  = new ArrayList<>();
         for (Short s : ls) b.add(s);
         return b;
      }

      if (a0 instanceof Integer)
      {
         List<Integer> li = (List<Integer>) a;
         List<Integer> b  = new ArrayList<>();
         for (Integer i : li) b.add(i);
         return b;
      }

      if (a0 instanceof Long)
      {
         List<Long> ll = (List<Long>) a;
         List<Long> b  = new ArrayList<>();
         for (Long l : ll) b.add(l);
         return b;
      }

      if (a0 instanceof Float)
      {
         List<Float> lf = (List<Float>) a;
         List<Float> b  = new ArrayList<>();
         for (Float f : lf) b.add(f);
         return b;
      }

      if (a0 instanceof Double)
      {
         List<Double> ld = (List<Double>) a;
         List<Double> b  = new ArrayList<>();
         for (Double d : ld) b.add(d);
         return b;
      }

      if (a0 instanceof Character)
      {
         List<Character> lc = (List<Character>) a;
         List<Character> b  = new ArrayList<>();
         for (Character c : lc) b.add(c);
         return b;
      }

      if (a0 instanceof Boolean)
      {
         List<Boolean> lb = (List<Boolean>) a;
         List<Boolean> b  = new ArrayList<>();
         for (Boolean bo : lb) b.add(bo);
         return b;
      }

      if (a0 instanceof String)
      {
         List<String> ls = (List<String>) a;
         List<String> b  = new ArrayList<>();
         for (String s : ls) b.add(s);
         return b;
      }
      
      else
      {
         throw new RuntimeException("deepCopy() is not configured to clone objects not descended from DicomEntity.");
      }
   }
   
	
	public <T extends DicomEntity> List<T> deepCopyList(List<T> srcList)
	{
		List<T> destList = new ArrayList<>();
		try
		{
			for (T srcD : srcList)
			{
				T destD = (T) srcD.deepCopy();
				destList.add(destD);
			}
		}
		catch (Exception ex)
		{
			System.out.println("At problem");
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
			// (1)  other DicomEntityRepresentations, whose text representations
			//      are obtained by a call to their own getDicomTextRepresentation method;
			// (2a) Lists of DicomEntityRepresentations;
			// (2b) Lists of primitive types or String;
			// (2c) Lists of arrays of primitive types/String;
			// (3)  primitive types or String, whose values can be obtained directly.
			if (a != null)
			{
				if (a instanceof DicomEntity)
				{
					DicomEntity b = (DicomEntity) a;
					sb.append(b.getClass().getSimpleName());
					indent.append(IND);					
					sb.append(b.getDicomTextRepresentation(indent));
					indent.delete(indent.length()-IND.length(), indent.length());
				}

				else if (a instanceof List)
				{
					List b = (List) a;
					if (!b.isEmpty())
					{
						if ((b.get(0) instanceof DicomEntity))
							sb.append(getDicomEntityListTextRepresentation((List<DicomEntity>) b, indent));
					
						else
							sb.append(getObjectListTextRepresentation((List<Object>) b, indent));
					}
				}
				
				else sb.append(a);				
			}
		}
		
		return sb.toString();
	}
	
	
	public <T extends DicomEntity> String getDicomEntityListTextRepresentation(List<T> list, StringBuilder indent)
	{
		final String  IND   = "   ";
		StringBuilder sb    = new StringBuilder();
		int           count = 0;
		
		sb.append(list.getClass().getSimpleName());
		indent.append(IND);
		if (!list.isEmpty()) sb.append("<").append(list.get(0).getClass().getSimpleName()).append(">");
		for (T item : list)
		{
			sb.append("\n").append(indent).append("Item ").append(++count);
			sb.append(item.getDicomTextRepresentation(indent));
		}
		
		indent.delete(indent.length()-IND.length(), indent.length());
		
		return sb.toString();
	}
	
	
	public String getObjectListTextRepresentation(List<Object> list, StringBuilder indent)
	{
		final String  IND   = "   ";
		StringBuilder sb    = new StringBuilder();
		int           count = 0;
		
		sb.append(list.getClass().getSimpleName());
		indent.append(IND);
		if (!list.isEmpty()) sb.append("<").append(list.get(0).getClass().getSimpleName()).append(">");
			
		for (int i=0; i<list.size(); i++)
		{
			sb.append("\n").append(indent).append("Item ").append(++count);
			Object item = list.get(i);
			
			if (item instanceof List) sb.append(getObjectListTextRepresentation((List<Object>) item, indent));
			
			if (item.getClass().isArray())
			{
				// There doesn't seem to be any clever way of boxing the output here into
				// an object of the right type. This bit is now redundant, as I have
				// changed all subclasses of DicomEntity to avoid this situation. The
				// code is left here for information.
				if (item instanceof boolean[]) sb.append(indent).append(Arrays.toString((boolean[]) item));
				if (item instanceof byte[])    sb.append(indent).append(Arrays.toString((byte[])    item));
				if (item instanceof short[])   sb.append(indent).append(Arrays.toString((short[])   item));
				if (item instanceof int[])     sb.append(indent).append(Arrays.toString((int[])     item));
				if (item instanceof long[])    sb.append(indent).append(Arrays.toString((long[])    item));
				if (item instanceof float[])   sb.append(indent).append(Arrays.toString((float[])   item));
				if (item instanceof double[])  sb.append(indent).append(Arrays.toString((double[])  item));
				if (item instanceof boolean[]) sb.append(indent).append(Arrays.toString((boolean[]) item));
				if (item instanceof String[])
				{
					String[] s = (String[]) item;
					for (int j=0; j<s.length; j++) sb.append(IND).append(s[j]);
				}
			}

			else sb.append(IND).append(item.toString());
		}
		
		indent.delete(indent.length()-IND.length()-1, indent.length()-1);
		
		return sb.toString();
	}
	
}

