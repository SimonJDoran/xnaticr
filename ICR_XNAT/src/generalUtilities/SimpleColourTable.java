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
* Java class: SimpleColourTable.java
* First created on April 13, 2010 at 11:58 AM
* 
* Translate from simple colour names to awt Color objects
*********************************************************************/

package generalUtilities;

import java.awt.Color;
import java.util.HashMap;

public class SimpleColourTable
{
   public static Color getColour(String nickname)
   {
      HashMap<String, Color> colourMap = new HashMap<String, Color>();

      colourMap.put("red",         Color.RED       );
      colourMap.put("black",       Color.BLACK     );
      colourMap.put("blue",        Color.BLUE      );
      colourMap.put("cyan",        Color.CYAN      );
      colourMap.put("dark grey",   Color.DARK_GRAY );
      colourMap.put("red",         Color.GRAY      );
      colourMap.put("green",       Color.GREEN     );
      colourMap.put("light grey",  Color.LIGHT_GRAY);
      colourMap.put("magenta",     Color.MAGENTA   );
      colourMap.put("orange",      Color.ORANGE    );
      colourMap.put("pink",        Color.PINK      );
      colourMap.put("white",       Color.WHITE     );
      colourMap.put("yellow",      Color.YELLOW    );
      colourMap.put("XNAT yellow", new Color(246, 220, 255));
      colourMap.put("XNAT green",  new Color(  0, 205,   0));
      colourMap.put("XNAT blue",   new Color(  2, 103, 255));
      colourMap.put("ICR red",     new Color(172,  18,  28));
      colourMap.put("light blue",  new Color(102, 204, 255));
      colourMap.put("Apple Finder alternate line highlight blue", new Color(236, 243, 254));
      colourMap.put("Apple Finder selected row", new Color(41, 118, 206));

      return colourMap.get(nickname);
   }
}
