/********************************************************************
* Copyright (c) 2014, Institute of Cancer Research
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
* Java interface: GenerateZipDownloadAction.java
* First created on December 16, 2014 at 12.07 PM
* 
* Generate a zip file from the input file list and place it in the
* cache.
*********************************************************************/

package fileDownloads;

import generalUtilities.UidGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerateZipPostFetchAction implements PostFetchAction
{

	@Override
	public void executeAction(FileListWorker caller, Map<Class, PreFetchStore> pfsList) throws IOException
	{
      // Note that this action does not need to make use of any information
      // from the pre-fetch stage, so the pfsList argument is entirely formal,
      // to comply with the general interface, and is unused.
      
		// Although the action is executed for every row, because of the generalised
		// nature of the performPostFetchActions method in FileListWorker.java, the zip
		// generation is actually done only once for all the table lines.
		if (caller.getOutputListAllRows().isEmpty())
		{
			caller.publishFromOutsidePackage("Generating ZIP file ...");
			// Zip files may be composites of multiple different files and may
			// contain different patients, scans, assessors, etc. Hence, they do
			// not have an obvious "unique" place in the hierarchy. So, place
			// at the top level of the cache directory.
			String       filesep = File.separator;
			StringBuffer zipName = new StringBuffer(caller.getCacheDirName());
			
			zipName.append(filesep)
					 .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()))
					 .append(".zip");
			
			File            zipFile = new File(zipName.toString());			
			ZipOutputStream zos     = new ZipOutputStream(new FileOutputStream(zipFile));
			ArrayList<File> zipList = new ArrayList<>();
			
			for (ArrayList<File> alf : caller.getSourceListAllRows()) zipList.addAll(alf);
			
			byte[] buffer = new byte[128];
			File   cache  = new File(caller.getCacheDirName());

			for (int i=0; i<zipList.size(); i++)
			{
				caller.setProgressFromOutsidePackage((DAOOutput.STOP_ICON - 1) * i / zipList.size());
				File f = zipList.get(i);
				
				if (!f.isDirectory())
				{
					// We want the zipEntry's path to be a relative path (relative
					// to top of the cache file hierarchy), so chop off the rest of
					//the path
					String          cacheCP  = cache.getCanonicalPath();
					String          fCP      = f.getCanonicalPath();
					String          fRelPath = fCP.substring(cacheCP.length()+1, fCP.length());
					ZipEntry        entry    = new ZipEntry(fRelPath);
					FileInputStream fis      = new FileInputStream(f);
					
					zos.putNextEntry(entry);
					int read = 0;
					while ((read=fis.read(buffer)) != -1) zos.write(buffer, 0, read);
					zos.closeEntry();
          
					fis.close();
				}
			}
			
			zos.close();
			ArrayList<File> alf = new ArrayList<>();
			alf.add(zipFile);
			caller.addToOutputListAllRows(alf);
		}
	}
}
