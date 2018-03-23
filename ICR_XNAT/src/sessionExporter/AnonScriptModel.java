/** ******************************************************************
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
 *
 *********************************************************************
 * @author Simon J Doran
 * Java class: sessionExporter.AnonScriptModel
 * First created on 20-Mar-2018 at 08:52:53
 *
 * Model representing the data for an anonymisation script.
 *  The dialogue uses the MVC design pattern.
 ******************************************************************** */
package sessionExporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class AnonScriptModel
{   
   protected static Logger logger = Logger.getLogger(AnonScriptModel.class);

   private File    currentFile;
   private String  currentName;
   private String  currentScript;
   private String  unsavedScript;  
   private boolean approved;
   private boolean saved;
   private final Map<String, String> scriptMap;
   
   // There always needs to be a "Custom" entry, but the name might change.
   public static final String  CUSTOM = "Custom";
   
   public AnonScriptModel()
   {
      currentFile   = null;
      currentName   = null;
      currentScript = null;
      unsavedScript = null;
      scriptMap    = new LinkedHashMap<>();
      scriptMap.put("Simple (retain significant metadata)", "anonScriptSimple.das");
      scriptMap.put("Internal ICR (thorough)", "anonScriptInternal.das");
      scriptMap.put("External use (very thorough)", "anonScriptExternal.das");
      scriptMap.put(CUSTOM, "");   
   }
   
    
   public boolean canApprove()
   {
      return (!approved && ((!currentName.equals("Custom") ||
                             (currentName.equals("Custom") && saved))));
   }
   
   
   public boolean canLoad()
   {
      return currentName.equals("Custom");
   }
   
   
   public boolean canSave()
   {
      return (currentName.equals("Custom") && !isSaved() && currentFile != null);
   }
   
   
   public boolean canSaveAs()
   {
      return currentName.equals("Custom") && (unsavedScript != null);
   }
   
   
   public Map<String, String> getScriptMap()
   {
      return scriptMap;
   }
      
   
   public String getDefaultScript(String name)
   {
      String resourceErrMsg = "Couldn't read default anonymisation script.\n"
                                + "This shouldn't happen as it resource is supposed"
				                    + "to be packaged with the application jar!";
		String script = "";  
      
      String resource = scriptMap.get(name);
      if (resource == null)
      {
         logger.error("Chosen default script does not exist");
      }
      else if (!resource.equals(""))
      {
         InputStream is = AnonScriptModel.class.getResourceAsStream(resource);

         if (is == null) logger.error(resourceErrMsg);
         else
         {
            try
            {
               script = IOUtils.toString(is, "UTF-8");
            }
            catch (IOException exIO)
            {
               logger.error(resourceErrMsg + "\n" + exIO.getMessage());
            }
         }
      }
      
      return script;
   }
   
   public File getCurrentFile()
   {
      return currentFile;
   }

   public void setCurrentFile(File currentFile)
   {
      this.currentFile = currentFile;
   }

   public String getCurrentName()
   {
      return currentName;
   }

   public void setCurrentName(String currentName)
   {
      this.currentName = currentName;
   }

   public String getCurrentScript()
   {
      return currentScript;
   }

   public void setCurrentScript(String currentScript)
   {
      this.currentScript = currentScript;
   }
   
   public String getUnsavedScript()
   {
      return unsavedScript;
   }

   public void setUnsavedScript(String unsavedScript)
   {
      this.unsavedScript = unsavedScript;
   }

   public boolean isApproved()
   {
      return approved;
   }

   public void setApproved(boolean approved)
   {
      this.approved = approved;
   }
   
   public boolean isSaved()
   {
      return saved;
   }
   
   public void setSaved(boolean saved)
   {
      this.saved = saved; 
   }
}


