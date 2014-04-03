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

/*********************************************************************
* @author Simon J Doran
* Java class: IDLd.java
* First created on May 12, 2008, 11.52 PM
* 
* Skeleton implementation of my write_idld function in IDL. This will
* allow data to be shared between Java and IDL via the filesystem.
*********************************************************************/

package imageUtilities;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class IDLd extends Object
{
	protected FileOutputStream fos;
	
	public IDLd(File output)
	{
		super();
	}
	
	public void open(File file)
	{
		try
		{
			fos = new FileOutputStream(file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(new JFrame(), ex.getMessage(), "Error opening IDLd file",
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	/** Write a 3-D short array as an IDLd file. The file consists of:
	 * a single byte indicating the endian-ness of the data; 
	 * an identifier string;
	 * the number of parameters (here, just 1) as a (32-bit) int;
	 * a representation of the result of the IDL size() function, viz:
	 *    IDL lonarr(6) (32-bit), made up of:
	 *       nDims;
	 *       dim1Size; dim2Size; dim3Size;
	 *       type (IDL int = 2);
	 *       nElements (dim1Size * dim2Size * dim3Size)
	 * the binary data.
	 */
	public void write(short[][][] data)
	{
		if (fos == null) return; // Fail silently.
		
		byte[] b;
		
		
		try
		{
			// Endianness
		   fos.write(1);
			
			// File type identifier
			fos.write(new String("SJD_IDLd:v2.0").getBytes());
			
			// 1 as 32-bit.
			b = new byte[4];
			b[0] = 0;
			b[1] = 0;
			b[2] = 0;
			b[3] = 1;
			fos.write(b);
			
			// IDL size()
			b = new byte[24];
			b[0] = 0; // nDims;
			b[1] = 0;
			b[2] = 0;
			b[3] = 3;
			
				
				
		}
		catch (Exception ex)
		{
			// Fail silently.
		}
		
		
		
	}
}
