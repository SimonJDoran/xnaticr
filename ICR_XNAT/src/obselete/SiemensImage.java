/*****************************************************************************
 *
 * SiemensImage.java
 * Simon J. Doran
 *
 * First created on June 19, 2007, 11:04 AM
 *
 * Object allowing reading of a Siemens .ima MR image file
 *
 *****************************************************************************/

package obselete;

import generalUtilities.Singleton;
import obselete.SiemensDictionary;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.swing.JTextArea;

/**
 *
 * @author simond
 */
public class SiemensImage
{
	private File		dataFile;
	private byte[]		header = new byte[6144];
	private short[]	dataShort;
	private float[]	dataFloat;
	SiemensDictionary	sd;
	
	public static final int	IMAGE_DOMAIN			= 1;
	public static final int TIME_DOMAIN				= 2;
	public static final int UNRECOGNISED_DOMAIN	= 3;
	
	
	/** Creates a new instance of SiemensImage */
	public SiemensImage(File dataFile) throws SiemensImageException, SiemensDictionaryException
	{

		// Get the dictionary used to decode the file.
		try
		{			
			sd = SiemensDictionary.getSiemensDictionary();
		}
		catch (SiemensDictionaryException ex)
		{
			throw ex;
		}
		
		// Open the file an perform enough basic tests to validate it and extract the data.
		this.dataFile = dataFile;
		try
		{
			openFile();
		}
		catch (SiemensImageException ex)
		{
			throw ex; 			
		}
				

	}

	
		
	/** Given a Nema element name, extract the corresponding value from the header.
	 *  The same method will return any one of the supported data types, but the
	 *  result needs to be cast appropriately when the function is invoked.  
	 */	
	public Object extractNemaElement(int nemaGroup, int nemaElement, int siemensIndex) throws SiemensImageException
	{
	// Convert from the Siemens (block, position) description to an absolute
	//	offset from the start of the file
		long nemaKey						= ((long) nemaGroup << 32)
													+ ((long) nemaElement << 16)
													+ (long) siemensIndex;
		SiemensDictionaryElement sde	= (SiemensDictionaryElement) sd.get(nemaKey);
		int[] blockStart					= {-1, 1023, 2047, 3199, 4223, 5247};
		int elementLength					= sde.getEndPosition() - sde.getStartPosition() + 1;
		int elementStart					= blockStart[sde.getBlock()-1] + sde.getStartPosition();
		int b0;
		int b1;
		int b2;
		int b3;
		byte[] a								= new byte[4];
		
		// Each type of data has a different slightly different procedure for
		// extracting it.
		switch (sde.getVariableType())
		{
			case SiemensDictionary.SHORTINT:
				if (elementLength != 2) throw new SiemensImageException(SiemensImageException.READ_ERROR);
				System.arraycopy(header, elementStart, a, 0, elementLength);
				
				// Note that Java bytes are all *signed*. In order for standard, unsigned
				// byte arithmetic to work, we need to do a little fiddle. b0 and b1
				// are 4-byte ints.
				b0 = a[0] & 0xFF;
				b1 = a[1] & 0xFF;
				return (Integer) ( (b0<<8) + b1 );
				
				
			case SiemensDictionary.INT:
				if (elementLength != 4) throw new SiemensImageException(SiemensImageException.READ_ERROR);
				System.arraycopy(header, elementStart, a, 0, elementLength);
				b0 = a[0] & 0xFF;
				b1 = a[1] & 0xFF;
				b2 = a[2] & 0xFF;
				b3 = a[3] & 0xFF;
				return (Integer) ( (b0<<24) + (b1<<16) + (b2<<8) + b3 );
				
			
			// It is not clear how these three string terms differ in their usage.
			case SiemensDictionary.STRING_cg:
			case SiemensDictionary.STRING_cj:
			case SiemensDictionary.STRING_ct:
				byte[] stringBytes = new byte[elementLength];
				System.arraycopy(header, elementStart, stringBytes, 0, elementLength);  
				return new String(stringBytes);
				
				
			case SiemensDictionary.FLOAT:
				if (elementLength != 4) throw new SiemensImageException(SiemensImageException.READ_ERROR);
				System.arraycopy(header, elementStart, a, 0, elementLength);
				// Pre-existing method for getting a float.
				return ByteBuffer.wrap(a).order( ByteOrder.nativeOrder() ).getFloat();
				
				
			case SiemensDictionary.DOUBLE:
				if (elementLength != 8) throw new SiemensImageException(SiemensImageException.READ_ERROR);
				System.arraycopy(header, elementStart, a, 0, elementLength);
				// Pre-existing method for getting a double.
				return ByteBuffer.wrap(a).order( ByteOrder.nativeOrder() ).getDouble();
				
				
			case SiemensDictionary.SPACE:
				// The "s" descriptor identifies unused regions of the header.
				return null;
				
				
			default: throw new SiemensImageException(SiemensImageException.READ_ERROR);
		}
		
	}
	

	
	
	/** Open the input file and perform basic checks that the data contained are
	 *  in the Siemens format.
	 */
	private void openFile() throws SiemensImageException
	{
		RandomAccessFile dataIn = null;
		try
		{
			dataIn = new RandomAccessFile(dataFile, "r");			
		}
		catch (FileNotFoundException ex)
		{
			throw new SiemensImageException(SiemensImageException.FILE_NOT_FOUND);
		}
		
		// Get the file header.
		try
		{			
			dataIn.readFully(header);
		}
		catch (IOException ex)
		{
			throw new SiemensImageException(SiemensImageException.NOT_SIEMENS_FILE);
		}
		
      // -----------------------------------------------------------------		
		// Read enough of the basic image parameters to tell whether this is
		// indeed a Siemens file.
		// -----------------------------------------------------------------
		
		String manufacturerName = (String) extractNemaElement(0x0008, 0x0070, 0xFFFF);
		if (!manufacturerName.trim().equals("SIEMENS"))
			throw new SiemensImageException(SiemensImageException.NOT_SIEMENS_FILE);
		
		int domainCode = 0;
		int domain		= UNRECOGNISED_DOMAIN;
		try
		{
			domainCode = (Integer) extractNemaElement(0x0008, 0x0041, 2);
		}
		catch (SiemensImageException ex)
		{
			throw ex;
		}
		if (domainCode == 1)			domain = IMAGE_DOMAIN;
		else if (domainCode == 51)	domain = TIME_DOMAIN;

		int nCols;
		int nRows;
		
		switch (domain)
		{
			case IMAGE_DOMAIN:
				try
				{
					nCols = (Integer) extractNemaElement(0x0028, 0x0011, 0xFFFF);
					nRows = (Integer) extractNemaElement(0x0028, 0x0010, 0xFFFF);
				}
				catch (SiemensImageException ex)
				{
					throw ex;
				}
				break;
				
			case TIME_DOMAIN:
				try
				{
					nCols = (Integer) extractNemaElement(0x0019, 0x1230, 0xFFFF);
					nRows = (Integer) extractNemaElement(0x0019, 0x1221, 0xFFFF);					
				}
				catch (SiemensImageException ex)
				{
					throw ex;
				}
				break;
				
			case UNRECOGNISED_DOMAIN:
			default: throw new SiemensImageException(SiemensImageException.NOT_SIEMENS_FILE);				
		}

		
		int nDims = 0;
		try
		{
			nDims = (Integer) extractNemaElement(0x0028, 0x0005, 0xFFFF);
		}
		catch (SiemensImageException ex)
		{
			throw ex;
		}
		if (nDims != 2)
			throw new SiemensImageException(SiemensImageException.GT_2_DIMENSIONS);

		
		int nBits = 0;
		try
		{
			nBits = (Integer) extractNemaElement(0x0028, 0x0100, 0xFFFF);
		}
		catch (SiemensImageException ex)
		{
			throw ex;
		}
		if (nBits != 16)
			throw new SiemensImageException(SiemensImageException.WRONG_NO_OF_BITS);


		
		// -------------------------
		// Read the data themselves.
		// -------------------------
		
		int nDataBytesHdr = 0;
		try
		{
			nDataBytesHdr	= (Integer) extractNemaElement(0x0019, 0x1060, 0xFFFF);
		}
		catch (SiemensImageException ex)
		{
			throw ex;
		}

		int nDataBytesCalc	= nRows * nCols * ((domain == IMAGE_DOMAIN)?2:8);

		int nDataBytesFile	= 0;
		try
		{
			nDataBytesFile	= (int) dataIn.length() - 6144;			
		}
		catch (Exception ex)
		{
			throw new SiemensImageException(SiemensImageException.READ_ERROR);
		}

		if ( (nDataBytesHdr != nDataBytesCalc) || (nDataBytesHdr != nDataBytesFile) )
			throw new SiemensImageException(SiemensImageException.WRONG_NO_OF_BYTES);
		
		byte[] data = new byte[nDataBytesHdr];
		try
		{			
			dataIn.readFully(data);
		}
		catch (IOException ex)
		{
			throw new SiemensImageException(SiemensImageException.READ_ERROR);
		}
		
		// Images are made up of integer data, whilst the underlying data type for
		// Siemens raw files is complex float.
		if (domain == IMAGE_DOMAIN)
		{
			dataShort			= new short[nDataBytesHdr/2];
			ShortBuffer sbb	= ByteBuffer.wrap(data).asShortBuffer().get(dataShort);
		}
		
		if (domain == TIME_DOMAIN)
		{
			dataFloat			= new float[nDataBytesHdr/4];
			FloatBuffer fbb	= ByteBuffer.wrap(data).asFloatBuffer().get(dataFloat);
		}			

		return;		
	}
	

	
	/** Utility function to check whether the Siemens file represents
	 *  image-domain or time-domain (raw) data
	 */	
	public int getDomain()
	{
		// domainCode is an element in the header block that appears to correspond
		// to the presence of raw or image data. The following is an emprical test
		// and not all files may obey it.
		int domainCode = 0;
		try
		{
			domainCode = (Integer) extractNemaElement(0x0008, 0x0041, 2);
		}
		catch (SiemensImageException ex)
		{
			/* In this and all subsequent "getter" methods, we ignore any exception.
			 * We are forced to have a try-catch block in these procedures, because
			 * extractNemaElement can return exceptions. However, by the time that
			 * we have successfully created an instance of a SiemensImage object,
			 * it is guaranteed that all these procedures return without error.
			 */			
		}
		if (domainCode == 1)			return IMAGE_DOMAIN;
		else if (domainCode == 51)	return TIME_DOMAIN;
		else								return UNRECOGNISED_DOMAIN; 
	}

	
	
	/** Return the name of the file from which the image data were read.
	 */
	public String getFileName()
	{
	// No need to throw an error, because if the SiemensImage object has been
	// successfully created, dataFile will be valid. 
		return dataFile.getAbsolutePath();
	}	

	
	
	/** Return the image data themselves.
	 */
	public short[] getImageData()
	{
		return dataShort;
	}		

	
	
	/** Return the number of columns in the image.
	 */
	public int getNCols()
	{
		int nCols = 0;
		try
		{
			nCols = (Integer) extractNemaElement(0x0028, 0x0011, 0xFFFF);
		}
		catch (SiemensImageException ex)
		{
			// Ignore exception - see getDomain
		}
		return nCols;
	}


	
	/** Return the number of rows in the image.
	 */
	public int getNRows()
	{
		int nRows = 0;
		try
		{
			nRows = (Integer) extractNemaElement(0x0028, 0x0011, 0xFFFF);
		}
		catch (SiemensImageException ex)
		{
			// Ignore exception - see getDomain
		}
		return nRows;
	}
}
