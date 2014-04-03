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
* Java class: UploadToXNATWorker.java
* First created on May 23, 2011 at 10.25 AM
* 
* Wrapper routine to allow a separate thread to upload a file to the
* XNAT database
*********************************************************************/

package xnatUploader;
import javax.swing.SwingWorker;

public class UploadToXNATWorker extends SwingWorker<Void, Void>
{
   protected DataUploader uploader;

   public UploadToXNATWorker(DataUploader uploader)
   {
      this.uploader = uploader;
   }


   @Override
   protected Void doInBackground() throws Exception
   {
      // Uploading data to XNAT is a two-stage process. First the metadata
      // are placed in the SQL tables of the PostgreSQL database, by uploading
      // a metadata XML document using REST. Then the data file itself is
      // uploaded, together with any auxiliary files.
      uploader.uploadMetadata();
      uploader.uploadFilesToRepository();
      
      
      // Note that we have no need to return anything from this function,
      // because if we get to here the upload is a success. All error conditions
      // cause an Exception to be thrown.
      return null;
   }
}
