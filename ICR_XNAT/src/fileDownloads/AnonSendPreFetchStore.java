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
* Java class: PreFetchStore.java
* First created on Jan 31, 2018
* 
* Container class for return data from the anonymise-and-send GUI
*********************************************************************/

package fileDownloads;


import exceptions.DataFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sessionExporter.AnonSessionInfo;
import xnatDAO.XNATProfile;

public class AnonSendPreFetchStore extends PreFetchStore
{
   private List<AnonSessionInfo> asiList;
   private XNATProfile           destProf;
   private String                destProj;
   private String                anonScriptTemplate; 

   public String getAnonScriptTemplate()
   {
      return anonScriptTemplate;
   } 
   
   public void setAnonScriptTemplate(String template)
   {
      anonScriptTemplate = template;
   }
   
   
   public void setAnonSessionInfo(List<AnonSessionInfo> asiList)
   {
      this.asiList = asiList; 
   }
   
   public List<AnonSessionInfo> getAnonSessionInfo()
   {
      return asiList; 
   }
   
   
   public XNATProfile getDestProfile()
   {
      return destProf;
   } 
   
   public void setDestProfile(XNATProfile xnprf)
   {
      destProf = xnprf;
   }
   
   
   public String getDestProject()
   {
      return destProj;
   } 
   
   public void setDestProject(String proj)
   {
      destProj = proj;
   }
   
}
