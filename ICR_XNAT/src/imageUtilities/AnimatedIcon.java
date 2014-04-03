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
* Java class: XNATDAO.java
* Code first created by Rob Camick and downloaded from
* http://tips4java.wordpress.com/2009/06/21/animated-icon/
* Modified by Simon J Doran starting May 24, 2010
* 
* Display a series of icons in a predetermined sequence.
*********************************************************************/

package imageUtilities;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 *  The AnimatedIcon will display a series of Icons in a predetermined
 *  sequence. This animation sequence can be configured to keep repeating or
 *  to stop after a specified number of cycles.
 *
 *  The size of the Icon is determined to be the largest width or height of
 *  any Icon. All other Icons are then aligned within the space available when
 *  the Icon is painted.
 *
 *  An AnimatedIcon cannot be shared by different components. However, the Icons
 *  added to an AnimatedIcon can be shared.
 *
 * SJD: For some applications, the ability to share is necessary. Without making
 *      life too complicated, the "kludgy" solution is to have the icon displayed
 *      in both places, but the animation only in the most recently created
 *      component.
 *
 *  The animation sequence is a simple sequential display of each Icon. When
 *  the end is reached the animation restarts at the first Icon. Icons are
 *  displayed in the order in which they are added. To create custom animation
 *  sequences you will need to override the getNextIconIndex() and
 *  isCycleCompleted() methods.
 *
 *  Modifications SJD include:
 *
 *  1. The ability to create the icon without needing to know the parent component.
 *     (This is useful if you are supplying icons to be used by another code for
 *     which you can't modify the source. In my case, I am using the icons in the
 *     org.netbeans.swing.outline tree table implementation.)
 *     (Call the constructor with component = null.)
 *
 *  2. The ability to supply explicitly the ID of the component to repaint when
 *     the icon update is required.
 *     (New method: setRepaintComponent)
 *
 *  3. A separate image to display when the icon is not cycling.
 *     (New constructor and new method setNonCyclingIcon)
 *
 *  4. The ability to pass the method an ArrayList of icons rather than a list
 *     or looping round adding icons.
 */
public class AnimatedIcon implements Icon, ActionListener, Runnable
{
	private final static int DEFAULT_DELAY = 500;
	private final static int DEFAULT_CYCLES = -1;

	public final static float TOP = 0.0f;
	public final static float LEFT = 0.0f;
	public final static float CENTER = 0.5f;
	public final static float BOTTOM = 1.0f;
	public final static float RIGHT = 1.0f;

	protected JComponent component;
   protected JComponent repaintComponent;
	protected List<Icon> icons = new ArrayList<Icon>();
   protected Icon       nonCyclingIcon = null;

	private int cycles;
	private boolean showFirstIcon = false;
   private boolean isVisible     = true;

	private float alignmentX = CENTER;
	private float alignmentY = CENTER;

	//  Track the X, Y location of the Icon within its parent JComponent so we
	//  can request a repaint of only the Icon and not the entire JComponent

	private int iconX;
	private int iconY;

	//  Used for the implementation of Icon interface

	private int iconWidth;
	private int iconHeight;

	//  Use to control processing

	private int currentIconIndex;
	private int cyclesCompleted;
	private boolean animationFinished = true;
	private Timer timer;

	/**
	 *  Create an AnimatedIcon that will continuously cycle with the
	 * default (500ms).
	 *
	 *  @param component  the component the icon will be painted on
	 *  @param icons	    the Icons to be painted as part of the animation
	 */
	public AnimatedIcon(JComponent component, Icon... icons)
	{
		this(component, DEFAULT_DELAY, icons);
	}


   /**
	 *  Create an AnimatedIcon that will continuously cycle
	 *
	 *  @param component  the component the icon will be painted on
	 *  @param delay      the delay between painting each icon, in milli seconds
	 *  @param icons	    the Icons to be painted as part of the animation
	 */
	public AnimatedIcon(JComponent component, int delay, Icon... icons)
	{
		this(component, delay, DEFAULT_CYCLES, icons);
	}



	public AnimatedIcon(JComponent component, int delay, int cycles, Icon... icons)
   {
      this (component, delay, cycles, null, icons);
   }


   /**
	 *  Create an AnimatedIcon specifying all the properties.
	 *
	 *  @param component      the component the icon will be painted on
	 *  @param delay          the delay between painting each icon, in milli seconds
	 *  @param cycles         the number of times to repeat the animation sequence
    *  @param nonCyclingIcon the Icon to be painted when the animation is not running
	 *  @param icons	        the Icons to be painted as part of the animation
	 */
	public AnimatedIcon(JComponent component, int delay, int cycles, Icon nonCyclingIcon,
                       Icon... icons)
	{
		this.component = component;
		this.cycles    = cycles;

		for (int i = 0; i < icons.length; i++)
		{
			if (icons[i] == null)
			{
				String message = "Icon (" + i + ") cannot be null";
				throw new IllegalArgumentException( message );
			}
			else
			{
				this.icons.add(icons[i]);
				calculateIconDimensions();
			}
		}
      if (nonCyclingIcon == null) this.nonCyclingIcon = icons[0];
      else                        this.nonCyclingIcon = nonCyclingIcon;

		timer = new Timer(delay, this);
	}



   /**
    * SJD New constructor:  Create an Animated icon specifying all the properties,including a different icon to use while not cycling.
    *
    * @param component      the component the icon will be painted on (can be null)
    * @param delay          the delay between painting each icon, in milliseconds
    * @param cycles         the number of times to repeat the animation sequence (-1 is indefinite cycling)
    * @param icons          the Icons to be painted as part of the animation
    * @param nonCyclingIcon a (potentially different) Icon to display when the icons are not cycling round
    *                       If this is null or if the
    */
   public AnimatedIcon(JComponent component, int delay, int cycles, ArrayList<Icon> icons,
                       Icon nonCyclingIcon)
   {
      this.component = component;
		this.cycles    = cycles;

		for (int i = 0; i < icons.size(); i++)
      {
         if (icons.get(i) == null)
         {
            throw new IllegalArgumentException("Icon list cannot contain null values.");
         }
      }
      this.icons = icons;
      calculateIconDimensions();

      if (nonCyclingIcon == null) this.nonCyclingIcon = icons.get(0);
      else                        this.nonCyclingIcon = nonCyclingIcon;

      timer = new Timer(delay, this);
   }



	/**
	 *  Add Icons to be used in the animation.
	 *
	 *  @param icons  the icons to be added
	 */
	public void addIcon(Icon... icons)
	{
//		if (icon == null) return;

		for (Icon icon : icons)
		{
			if (icon != null)
			{
				this.icons.add( icon );
				calculateIconDimensions();
			}
		}
	}

	/**
	 *  Calculate the width and height of the Icon based on the maximum
	 *  width and height of any individual Icon.
	 */
	private void calculateIconDimensions()
	{
		iconWidth = 0;
		iconHeight = 0;

		for (Icon icon : icons)
		{
			iconWidth = Math.max(iconWidth, icon.getIconWidth());
			iconHeight = Math.max(iconHeight, icon.getIconHeight());
		}
	}


   /**
	 *  Get the alignment of the Icon on the x-axis
	 *
	 *  @return the alignment
	 */
	public float getAlignmentX()
	{
		return alignmentX;
	}


   /**
	 *  Specify the horizontal alignment of the icon.
	 *
	 *  @param alignmentX  common values are LEFT, CENTER (default)  or RIGHT
	 *                     although any value between 0.0 and 1.0 can be used
	 */
	public void setAlignmentX(float alignmentX)
	{
		this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
	}


   /**
	 *  Get the alignment of the icon on the y-axis
	 *
	 *  @return the alignment
	 */
	public float getAlignmentY()
	{
		return alignmentY;
	}


   /**
	 *  Specify the vertical alignment of the Icon.
	 *
	 *  @param alignmentY  common values TOP, CENTER (default) or BOTTOM
	 *                     although any value between 0.0 and 1.0 can be used
	 */
	public void setAlignmentY(float alignmentY)
	{
		this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
	}


   /**
	 *  Get the index of the currently visible Icon
	 *
	 *  @return the index of the Icon
	 */
	public int getCurrentIconIndex()
	{
		return currentIconIndex;
	}


   /**
	 *  Set the index of the Icon to be displayed and then repaint the Icon.
	 *
	 *  @param index  the index of the Icon to be displayed
	 */
	public void setCurrentIconIndex(int index)
	{
		currentIconIndex = index;
      component.repaint(iconX, iconY, iconWidth, iconHeight);
	}



   /**
    * SJD New method: Set the icon to be displayed when the image is not cycling round.
    * Note that, for compatibility with the original API, the default is to use the
    * first of the icons in the ArrayList icons for this purpose.
    *
    * @param icon
    */
   public void setNonCyclingIcon(Icon icon)
   {
      this.nonCyclingIcon = icon;
   }



   /**
	 *  Specify the number of times to repeat each animation sequence, or cycle.
	 *
	 *  @param cycles the number of cycles to complete before the animation
	 *                stops. The default is -1, which means the animation is
	 *                continuous.
	 */
	public void setCycles(int cycles)
	{
		this.cycles = cycles;
	}


   /**
	 *  Specify the delay
	 *
	 *  @param delay  the delay between painting eachIcon (in milli seconds)
	 */
	public void setDelay(int delay)
	{
		timer.setDelay(delay);
	}



   /**
	 *  Display the first icon when animation is finished. Otherwise the Icon
	 *  that was visible when the animation stopped will remain visible.
	 *
	 *  @param showFirstIcon  true when the first icon is to be displayed,
	 *                        false otherwise
	 */
	public void setShowFirstIcon(boolean showFirstIcon)
	{
		this.showFirstIcon = showFirstIcon;
	}


   /**
    * Specify whether the icon should be visible or invisible.
    * @param isVisible the visibility status
    */
   public void setVisible(boolean isVisible)
   {
      this.isVisible = isVisible;
   }



   /**
    * SJD new method: Set the component to be repainted when a paint request to
    * the icon comes direct (e.g., from the timer's ActionPerformed method) rather
    * than from the parent component itself.
    * @param c  the component that should be repainted
    */
   public void setRepaintComponent(JComponent c)
   {
      repaintComponent = c;
   }


	/**
	 *  Get the cycles to complete before animation stops.
	 *
	 *  @return the number of cycles
	 */
	public int getCycles()
	{
		return cycles;
	}


   /**
	 *  Get the delay between painting each Icon
	 *
	 *  @return the delay
	 */
	public int getDelay()
	{
		return timer.getDelay();
	}


	/**
	 *  Get the Icon at the specified index.
	 *
	 *  @param index  the index of the Icon to be returned
	 *  @return  the Icon at the specifed index
	 *  @exception IndexOutOfBoundsException  if the index is out of range
	 */
	public Icon getIcon(int index)
	{
		return icons.get( index );
	}


   /**
	 *  Get the number of Icons contained in this AnimatedIcon.
	 *
	 *  @return the total number of Icons
	 */
	public int getIconCount()
	{
		return icons.size();
	}


   /**
	 *  Get the showFirstIcon
	 *
	 *  @return the showFirstIcon value
	 */
	public boolean isShowFirstIcon()
	{
		return showFirstIcon;
	}


	/**
	 *  Pause the animation. The animation can be restarted from the
	 *  current Icon using the restart() method.
	 */
	public void pause()
	{
		timer.stop();
	}

	/**
	 *  Start the animation from the beginning.
	 */
	public void start()
	{
		if (!timer.isRunning())
		{
			setCurrentIconIndex(0);
			animationFinished = false;
			cyclesCompleted = 0;
            timer.start();
		}
	}

	/**
	 *  Restart the animation from where the animation was paused. Or, if the
	 *  animation has finished, it will be restarted from the beginning.
	 */
	public void restart()
	{
		if (!timer.isRunning())
		{
			if (animationFinished)
				start();
			else
				timer.restart();
		}
	}

	/**
	 *  Stop the animation. The first icon will be redisplayed.
	 */
	public void stop()
	{
		timer.stop();
		setCurrentIconIndex(0);
		animationFinished = true;
      if (repaintComponent != null) repaintComponent.repaint();
      else if (component != null)
         component.repaint(iconX, iconY, iconWidth, iconHeight);
	}



//
//  Implement the Icon Interface
//

	/**
	 *  Gets the width of this icon.
	 *
	 *  @return the width of the icon in pixels.
	 */
	@Override
	public int getIconWidth()
	{
		return iconWidth;
	}


	/**
	 *  Gets the height of this icon.
	 *
	 *  @return the height of the icon in pixels.
	 */
	@Override
	public int getIconHeight()
	{
		return iconHeight;
	}


   /**
	*  Paint the icons of this compound icon at the specified location
	*
	*  @param c The component on which the icon is painted
	*  @param g the graphics context
	*  @param x the X coordinate of the icon's top-left corner
	*  @param y the Y coordinate of the icon's top-left corner
	*/
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		// Saving the x, y coordinates allows us to only repaint the icon and
		// not the entire component for each animation. The variable
      // component is used by actionPerformed to update the icon
      // at a timer event. Since that redraw event is not being
      // controlled by the containing component, we don't have this
      // object unless we save it here for later.
      component = (JComponent) c;
      
		iconX = x;
		iconY = y;

		//  Determine the proper alignment of the Icon, then paint it

		Icon icon = (timer.isRunning()) ? icons.get(currentIconIndex):
                                        nonCyclingIcon;
      int width  = getIconWidth();
   	int height = getIconHeight();

		int iconX  = getOffset(width, icon.getIconWidth(), alignmentX);
		int iconY  = getOffset(height, icon.getIconHeight(), alignmentY);

		if (isVisible) icon.paintIcon(c, g, x + iconX, y + iconY);

      // Invisible icons are implemented by painting a completely transparent
      // icon of the same size instead.
      if (!isVisible)
      {
         BufferedImage invisible = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         for (int j=0; j<height; j++)
            for (int i=0; i<width; i++)
               invisible.setRGB(i, j, 0x00);
         Icon invisibleIcon = new ImageIcon(invisible);
         invisibleIcon.paintIcon(c, g, x + iconX, y + iconY);
      }
	}

	/*
	 *  When the icon value is smaller than the maximum value of all icons the
	 *  icon needs to be aligned appropriately. Calculate the offset to be used
	 *  when painting the icon to achieve the proper alignment.
	 */
	private int getOffset(int maxValue, int iconValue, float alignment)
	{
		float offset = (maxValue - iconValue) * alignment;
		return Math.round(offset);
	}

//
//  Implement the ActionListener interface
//
	/**
	 *  Control the animation of the Icons when the Timer fires.
	 */
	public void actionPerformed(ActionEvent e)
	{
		//	Display the next Icon in the animation sequence. Note, it is preferable
      // simply to repaint the icon inside its parent component, but sometimes
      // this seems to not work, or the correct parent is not known. In the
      // latter case, set the variable repaintComponent to a something further
      // up the hierarchy and repaint this in preference.
		setCurrentIconIndex( getNextIconIndex(currentIconIndex, icons.size()) );

		if (repaintComponent != null) repaintComponent.repaint();
      else if (component != null)
         component.repaint(iconX, iconY, iconWidth, iconHeight);
      

		//  Track the number of cycles that have been completed

		if (isCycleCompleted(currentIconIndex, icons.size()))
		{
			cyclesCompleted++;
		}

		//  Stop the animation when the specified number of cycles is completed

		if (cycles > 0
		&&  cycles <= cyclesCompleted)
		{
			timer.stop();
			animationFinished = true;

			//  Display the first Icon when required

			if (isShowFirstIcon()
			&&  getCurrentIconIndex() != 0)
			{
				new Thread(this).start();
			}
		}
	}

//
//  Implement the Runnable interface
//
	public void run()
	{
		//  Wait one more delay interval before displaying the first Icon

		try
		{
			Thread.sleep( timer.getDelay() );
			setCurrentIconIndex(0);
		}
		catch(Exception e) {}
	}

	/**
	 *  Get the index of the next Icon to be displayed.
	 *
	 *  This implementation displays the Icons in the order in which they were
	 *  added to this class. When the end is reached it will start back at the
	 *  first Icon.
	 *
	 *  Typically this method, along with the isCycleCompleted() method, would
	 *  be extended to provide a custom animation sequence.
	 *
	 *  @param currentIndex  the index of the Icon currently displayed
	 *  @param iconCount  the number of Icons to be displayed
	 *  @return  the index of the next Icon to be displayed
	 */
	protected int getNextIconIndex(int currentIndex, int iconCount)
	{
		return ++currentIndex % iconCount;
	}

	/**
	 *  Check if the currently visible Icon is the last Icon to be displayed
	 *  in the animation sequence. If so, this indicates the completion of a
	 *  single cycle. The animation can continue for an unlimited number of
	 *  cycles or for a specified number of cycles.
	 *
	 *  This implemention checks if the last icon is currently displayed.
	 *
	 *  Typically this method, along with the getNextIconIndex() method, would
	 *  be extended to provide a custom animation sequence.
	 *
	 *  @param currentIndex  the index of the Icon currently displayed
	 *  @param iconCount the number of Icons to be displayed
	 *  @return  the index of the next Icon to be displayed
	 */
	protected boolean isCycleCompleted(int currentIndex, int iconCount)
	{
		return currentIndex == iconCount - 1;
	}


   public Component getComponent()
   {
      return component;
   }  

}
