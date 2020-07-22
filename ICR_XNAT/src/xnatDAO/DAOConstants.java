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
* Java class: DAOConstants.java
* First created on Sep 28, 2010 at 9:46:12 AM
* 
* Defaults for miscellaneous configuration parameters. Some of these
* might move to a config file in due course.
*********************************************************************/

package xnatDAO;

import generalUtilities.SimpleColourTable;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.UIManager;


public class DAOConstants
{
   public static final Color BG_COLOUR             = SystemColor.control;
   public static final Color EDITABLE_COLOUR       = Color.WHITE;
   public static final Color ACTION_COLOUR         = Color.BLACK;
   public static final Color NON_SELECTABLE_COLOUR = Color.GRAY;
   public static final Color CONNECTED_COLOUR      = new Color(  0, 205,   0);
   public static final Color DISCONNECTED_COLOUR   = new Color(172,  18,  28);

   public static final int  AUTHENTICATION_EXPIRY  = 100*60*60*1000; // 100 hours

   public DAOConstants()
   {
      
   }
}
