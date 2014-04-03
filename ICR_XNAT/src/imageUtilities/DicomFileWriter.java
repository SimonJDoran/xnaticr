/*******************************************************************
* Copyright (c) 2013, Institute of Cancer Research
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

/********************************************************************
* @author Simon J Doran
* Java class: DicomFileWriter.java
* First created on Dec 4, 2013 at 10:17:00 AM
*********************************************************************/

package imageUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

public class DicomFileWriter
{
   protected File        outputFile;
   protected File        templateFile;
   protected DicomObject odo;
   protected DicomObject tdo;
           
   
   public DicomFileWriter(File outputFile, File templateFile)
   {
      this.outputFile   = outputFile;
      this.templateFile = templateFile;
      
      tdo = readDicomFile(templateFile);
      odo = new BasicDicomObject();
      
      if (tdo != null) tdo.copyTo(odo);
      else odo = null;
   }
   
   
   private DicomObject readDicomFile(File inputFile)
   {
      DicomObject bdo = new BasicDicomObject();
      try
      {
         BufferedInputStream bis
            = new BufferedInputStream(new FileInputStream(inputFile));
         DicomInputStream dis = new DicomInputStream(bis);
         dis.readDicomObject(bdo, -1);
      }
      catch (IOException exIO)
      {
         return null;
      }

      return bdo;
   }
   
   
   protected int[] getPixelData()
   {
      int[] pixelData = tdo.getInts(Tag.PixelData);
      return pixelData;
   }
           
   
}
