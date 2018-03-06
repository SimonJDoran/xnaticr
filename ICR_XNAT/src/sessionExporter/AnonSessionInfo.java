/********************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
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
 *********************************************************************
 
 *********************************************************************
 * @author Simon J Doran
 * Java class: sessionExporter.AnonSessionInfo
 * First created on 09-Feb-2018 at 12:07:45
 *
 * Class to encapsulate information required by the anonymisation-and-
 * send process.
 *********************************************************************/

package sessionExporter;

public class AnonSessionInfo
{
   private String sessionId;
   private String sessionLabel;
   private String subjXnatId;
   private String subjLabel;
   private String subjDicomAnonName;
   private String subjDicomAnonId;

   public String getSessionId()
   {
      return sessionId;
   }

   public void setSessionId(String sessionId)
   {
      this.sessionId = sessionId;
   }

   public String getSessionLabel()
   {
      return sessionId;
   }

   public void setSessionLabel(String sessionLabel)
   {
      this.sessionLabel = sessionLabel;
   }

   public String getSubjXnatId()
   {
      return subjXnatId;
   }

   public void setSubjXnatId(String subjXnatId)
   {
      this.subjXnatId = subjXnatId;
   }

   public String getSubjLabel()
   {
      return subjLabel;
   }

   public void setSubjLabel(String subjLabel)
   {
      this.subjLabel = subjLabel;
   }

   public String getSubjDicomAnonName()
   {
      return subjDicomAnonName;
   }

   public void setSubjDicomAnonName(String subjDicomAnonName)
   {
      this.subjDicomAnonName = subjDicomAnonName;
   }

   public String getSubjDicomAnonId()
   {
      return subjDicomAnonId;
   }

   public void setSubjDicomAnonId(String subjDicomAnonId)
   {
      this.subjDicomAnonId = subjDicomAnonId;
   }
}
