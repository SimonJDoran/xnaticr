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
* Java class: ColouredCellRenderer.java
* 
* Create an icon consisting of a letter inside a circle.
* Modified from an original algoritym by Kirill Grouchnikov.
* Snippets of code at
* http://weblogs.java.net/blog/2005/02/25/how-create-your-own-icons
*********************************************************************/

package generalUtilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;


public class FancyLetterIcon extends ImageIcon
{
   public FancyLetterIcon(int iconDimension, char letter, Color background)
   {
      this(iconDimension, letter, background, false, background, false, background);
   }


   public FancyLetterIcon(int     iconDimension,
                          char    letter,
                          Color   background,
                          boolean arrow,
                          Color   arrowColour,
                          boolean plus,
                          Color   plusColour)
   {
      super();
      setImage(getIconImage(iconDimension, letter, background, arrow, arrowColour, plus, plusColour));
   }
   
   
   public BufferedImage getIconImage(int     iconDimension,
                          char    letter,
                          Color   background,
                          boolean arrow,
                          Color   arrowColour,
                          boolean plus,
                          Color   plusColour)
   {
      BufferedImage iconImage = new BufferedImage(iconDimension,
                                                  iconDimension,
                                                  BufferedImage.TYPE_INT_ARGB);
      int nx = iconDimension;

      // Set completely transparent.
      int TRANSPARENT_WHITE = (0xFF << 16) | (0xFF << 8) | 0xFF;
      for (int col = 0; col < nx; col++)
         for (int row = 0; row < nx; row++) iconImage.setRGB(col, row, TRANSPARENT_WHITE);

      Graphics2D graphics = (Graphics2D) iconImage.getGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

      graphics.setColor(background);
      graphics.fillOval(0, 0, nx-1, nx-1);

      // Create a whitish spot in the left-top corner of the icon.
      double id4 = nx / 4.0;
      double spotX = id4;
      double spotY = id4;
      for (int col=0; col<nx; col++)
      {
         for (int row=0; row<nx; row++)
         {
            // distance to spot
            double dx = col - spotX;
            double dy = row - spotY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            // distance of 0.0 - comes 90% to Color.white
            // distance of ICON_DIMENSION - stays the same

            if (dist > nx) dist = nx;

            int currColor = iconImage.getRGB(col, row);
            int transp = (currColor >>> 24) & 0xFF;
            int oldR = (currColor >>> 16) & 0xFF;
            int oldG = (currColor >>> 8) & 0xFF;
            int oldB = (currColor >>> 0) & 0xFF;

            double coef = 0.9 - 0.9 * dist / nx;
            int dr = 255 - oldR;
            int dg = 255 - oldG;
            int db = 255 - oldB;

            int newR = (int) (oldR + coef * dr);
            int newG = (int) (oldG + coef * dg);
            int newB = (int) (oldB + coef * db);

            int newColor = (transp << 24) | (newR << 16) | (newG << 8)
               | newB;
            iconImage.setRGB(col, row, newColor);
         }
      }

      // Draw outline of the icon.
      graphics.setColor(Color.black);
      graphics.drawOval(0, 0, nx - 1, nx - 1);

      // Now, take the input letter and make it capital (this looks much better on icons).
      // Then, set font that is a few pixels smaller than the icon dimension. Compute
      // the bounds of this letter, and set the position for this letter so that it will
      // be centered in the icon's center
      letter = Character.toUpperCase(letter);
      graphics.setFont(new Font("Arial", Font.BOLD, nx-5));
      FontRenderContext frc = graphics.getFontRenderContext();
      TextLayout mLayout = new TextLayout("" + letter, graphics.getFont(), frc);

      float x = (float) (-0.5 + (nx - mLayout.getBounds().getWidth()) / 2);
      float y = nx - (float) ((nx - mLayout.getBounds().getHeight()) / 2);

      // Draw the letter.
      graphics.drawString("" + letter, x, y-(arrow?1:0));


      if (plus)
      {
         BufferedImage plusImage = getPlusImage(plusColour, nx);
         graphics.drawImage(plusImage, nx-plusImage.getWidth(), 0, null);
      }

      if (arrow)
      {
         BufferedImage arrowImage = getArrowImage(arrowColour, nx);
         graphics.drawImage(arrowImage, 0, nx-arrowImage.getHeight(), null);
      }

      return iconImage;
   }


   /**
    * Create an image of a plus sign with a surrounding halo.
    */
   private BufferedImage getPlusImage(Color plusColour, int iconDimension)
   {
      int baseDimension = 16;
      int r = iconDimension / baseDimension;
     
      BufferedImage plusImage = new BufferedImage(7*r, 7*r,
                                                   BufferedImage.TYPE_INT_ARGB);
      // Set partially transparent white.
      int transWhite = (100 << 24) | (255 << 16) | (255 << 8) | 255;
      for (int col = 0; col < 7*r; col++)
         for (int row = 0; row < 7*r; row++)
            plusImage.setRGB(col, row, transWhite);

      Graphics2D plusGraphics = (Graphics2D) plusImage.getGraphics();
      plusGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);


      Polygon pol = new Polygon();
//      pol.addPoint(0, 0);
//      pol.addPoint(5*r, 0);
//      pol.addPoint(5*r, 5*r);
//      pol.addPoint(0, 5*r);
//      plusGraphics.setColor(Color.WHITE);
//      plusGraphics.drawPolygon(pol);
//      plusGraphics.fill(pol);

      pol = new Polygon();
      pol.addPoint(3*r, 0*r);
      pol.addPoint(4*r, 0*r);
      pol.addPoint(4*r, 2*r);
      pol.addPoint(6*r, 2*r);
      pol.addPoint(6*r, 3*r);
      pol.addPoint(4*r, 3*r);
      pol.addPoint(4*r, 5*r);
      pol.addPoint(3*r, 5*r);
      pol.addPoint(3*r, 3*r);
      pol.addPoint(1*r, 3*r);
      pol.addPoint(1*r, 2*r);
      pol.addPoint(3*r, 2*r);

      plusGraphics.setColor(plusColour);
      plusGraphics.drawPolygon(pol);
      plusGraphics.fill(pol);



      return plusImage;
   }

   /**
    * Create an image of an arrow with a surrounding halo.
    */
   private BufferedImage getArrowImage(Color arrowColour, int iconDimension)
   {
      int height = 6;
      int width  = iconDimension;
      BufferedImage arrowImage = new BufferedImage(width, height,
                                                   BufferedImage.TYPE_INT_ARGB);
      // Set completely transparent
      for (int col = 0; col < width; col++)
         for (int row = 0; row < height; row++)
            arrowImage.setRGB(col, row, 0x0);

      Graphics2D arrowGraphics = (Graphics2D) arrowImage.getGraphics();
      arrowGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);

      Polygon pol = new Polygon();
      int ya = 3;
      pol.addPoint(1, ya);
      pol.addPoint(width / 2 + 3, ya);
      pol.addPoint(width / 2 + 3, ya + 2);
      pol.addPoint(width - 1, ya);
      pol.addPoint(width / 2 + 3, ya - 2);
      pol.addPoint(width / 2 + 3, ya);
      arrowGraphics.setColor(arrowColour);
      arrowGraphics.drawPolygon(pol);

      /*
       * And now for the tricky part - we have to compute the halo.
       * Here, if an arrow pixel was completely opaque, it should have
       * less transparent halo than arrow pixel that was only partly opaque
       * (as on arrow's head for example). Here, we create another image
       * with the halo footprint, and then draw the original arrow on top
       * of it. Each arrow pixel contributes to its 8 neighbouring pixels.
       * The final opacity of the halo footprint is the maximal opacity of
       * all neighbouring arrow pixels
       */
      BufferedImage haloImage = new BufferedImage(width, height,
                                                  BufferedImage.TYPE_INT_ARGB);

      //Set completely transparent.
      for (int col = 0; col<width; col++)
         for (int row = 0; row < height; row++)
            haloImage.setRGB(col, row, 0x0);

      Graphics2D haloGraphics = (Graphics2D) haloImage.getGraphics();
      for (int col = 0; col < width; col++)
      {
         int xs = Math.max(0, col - 1);
         int xe = Math.min(width - 1, col + 1);
         for (int row = 0; row < height; row++)
         {
            int ys = Math.max(0, row - 1);
            int ye = Math.min(height - 1, row + 1);
            int currColor = arrowImage.getRGB(col, row);
            int opacity = (currColor >>> 24) & 0xFF;
            if (opacity > 0)
            {
               // Mark all pixels in 3*3 area.
               for (int x = xs; x <= xe; x++)
               {
                  for (int y = ys; y <= ye; y++)
                  {
                     int oldOpacity = (haloImage.getRGB(x, y) >>> 24) & 0xFF;
                     int newOpacity = Math.max(oldOpacity, opacity);
                     // Set semi-transparent white.
                     int newColor = (newOpacity << 24) | (255 << 16) | (255 << 8) | 255;
                     haloImage.setRGB(x, y, newColor);
                  }
               }
            }
         }
      }

      // The final step - reduce the opacity of the halo by 30%.
      // This is needed to reduce complete opacity around vertical and horizontal lines:
      for (int col = 0; col < width; col++)
      {
         for (int row = 0; row < height; row++)
         {
            int oldOpacity = (haloImage.getRGB(col, row) >>> 24) & 0xFF;
            int newOpacity = (int)(0.7*oldOpacity);
            int newColor = (newOpacity << 24) | (255 << 16) | (255 << 8) | 255;
            haloImage.setRGB(col, row, newColor);
         }
      }
      // Draw the original arrow image on top of the halo.
      haloGraphics.drawImage(arrowImage, 0, 0, null);

      return haloImage;
   }
}
