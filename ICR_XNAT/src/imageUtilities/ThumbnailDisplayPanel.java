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
* Java class: ThumbnailDisplayPanel.java
* First created on July 25, 2007, 11:08 AM
* 
* Create a region that can contain either a thumbnail image and a
* rich-text area.
*********************************************************************/

package imageUtilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import xnatDAO.XNATDAO;

public class ThumbnailDisplayPanel extends JPanel
{
   static    Logger            logger = Logger.getLogger(ThumbnailDisplayPanel.class);
   protected ImageDisplayPanel thumbnailPanel;
   protected RSyntaxTextArea   thumbnailTextArea;
   protected static final int  PANEL_SIZE = 128;
   
	
	/** Creates a new instance of ThumbnailDisplayPanel */
	public ThumbnailDisplayPanel()
	{
      initialiseUI();
	}
   
	
	/**
	 * Set the image to be displayed by the ThumbnailDisplayPanel
	 */
	public void setImage(BufferedImage bi)
	{
		thumbnailPanel.setImage(bi);
	}
   
   
   
   public void setText(String text)
   {
      thumbnailTextArea.setText(text);
   }
   
   
   public void setSyntaxStyle(String style)
   {
      thumbnailTextArea.setSyntaxEditingStyle(style);
   }
   
   
   private void initialiseUI()
   {
      thumbnailPanel = new ImageDisplayPanel();
      thumbnailPanel.setBackground(Color.BLACK);
      
      thumbnailTextArea = new RSyntaxTextArea();
      thumbnailTextArea.setFont(new Font("Courier", Font.PLAIN, 8));     
      
      GroupLayout panelLayout = new GroupLayout(this);
		this.setLayout(panelLayout);
      this.setBackground(Color.BLACK);
      
      panelLayout.setAutoCreateContainerGaps(true);
      
      panelLayout.setHorizontalGroup(
			panelLayout.createSequentialGroup()
			.addComponent(thumbnailTextArea, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE)
         .addComponent(thumbnailPanel, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE)
		);
		
		panelLayout.setVerticalGroup(
			panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addComponent(thumbnailTextArea, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE)
         .addComponent(thumbnailPanel, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE)
		);
      
      thumbnailTextArea.setVisible(false);
   }
   

	
}
