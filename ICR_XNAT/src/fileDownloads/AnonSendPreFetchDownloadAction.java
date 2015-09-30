/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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
* Java class: AnonSendDownloadAction.java
* First created on Feb 6, 2015 at 9:50:35 AM
* 
* Launch the anonymisation and send GUI to allow users to route the
* downloaded session to a different XNAT instance and project.
*********************************************************************/

package fileDownloads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import sessionExporter.AnonymiseAndSend;

public class AnonSendPreFetchDownloadAction implements DownloadAction
{
@Override
	public void executeAction(FileListWorker caller) throws IOException
	{
		// Although the action is executed for every row, because of the generalised
		// nature of the performPostFetchActions method in FileListWorker.java, the anonymise
		// and send operation is actually done only once for all the table lines.
		if (caller.getOutputListAllRows().isEmpty())
		{
			// Add a dummy value to the list to avoid the routine being called
			// multiple times. Note that the property "outputCardinality" is set
			// to "None" for this type of action, so no action will be taken
			// by FileListWorker in response.
			caller.addAllToOutputListAllRows(caller.getSourceListAllRows());
		
			caller.publishFromOutsidePackage("Launching anonymise-and-send GUI ...");
			AnonymiseAndSend as = new AnonymiseAndSend(new javax.swing.JFrame(),
					                                     true,
			                                           caller.xndao.getProfile(),
			                                           caller.sessionLabelList);
			as.setVisible(true);
		}
	}
}
