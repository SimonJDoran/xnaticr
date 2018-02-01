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
* Java class: AnonSendPreFetchAction.java
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

public class AnonSendPreFetchAction implements PreFetchAction
{
@Override
	public PreFetchStore executeAction(FileListWorker caller) throws IOException
	{				
		caller.publishFromOutsidePackage("Launching anonymise-and-send GUI ...");
			
      AnonSendPreFetchStore pfs = new AnonSendPreFetchStore();
      AnonymiseAndSend as = new AnonymiseAndSend(new javax.swing.JFrame(),
					                                     true,
			                                           caller.xndao.getProfile(),
			                                           caller.sessionIDList,
					                                     caller.sessionLabelList,
			                                           caller.sessionSubjectList,
                                                    pfs);
		as.setVisible(true);
      
      return pfs;
	}
}
