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
* Java class: XndWrapper.java
* First created on Jan 17, 2012 at 5:07:02 PM
* 
* Wrapper round XNATDAO to isolate it further from the caller
* Not used at all now, but retained in case of future need.
*********************************************************************/

package xnatDAO;

import java.io.File;
import java.util.ArrayList;
import javax.swing.UIManager;

public class XndWrapper
{
   protected boolean  invokedByRun = false;
   protected XNATDAO  xndao;
   
   public XndWrapper()
	{

	}


   
   /**************************************
    * External API for client applications
    ***************************************/

   /**
    * Call the program directly from the command line. It is not intended
    * that this be the norm, but it is useful to have a main() method for use
    * with the IDE.
    * 
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (Exception exIgnore) {}

				XndWrapper xndw = new XndWrapper();
				xndw.invoke(true, true);
			}
		});
	}


   /**
    * Put the dialogue onto the screen and wait for a selection from the user.    
    * @param invokedByRun true if the application has been invoked from the run
    * command in an IDE. In normal use, false.
	 */
	public void invoke(boolean invokedByRun, boolean initiateSearch)
	{
      xndao.invoke(invokedByRun, initiateSearch);
	}
   
   
   
   
   /**
    * Attempt to run garbage collection on the XNAT_DAO object and reclaim memory.
    */
   public void destroy()
   {
      xndao = null;
   }
   
   /**
    * API for the external client application to find out the status of the
    * file-retrieve operation.
    *
    * @return the status String: possible values "Retrieving", "Succeeded", "Failed"
    */
   public String getStatus()
   {
      return xndao.getStatus();
   }


   /**
    * API for the external client application to retrieve the file names in their
    * "internal" format of an ArrayList<ArrayList<File>>.
    *
    * @return the list of files on the local filesystem corresponding to the data chosen
    */
   public ArrayList<ArrayList<File>> getOutputFileArrayList()
   {
      return xndao.getOutputFileArrayList();
   }

      
   /**
    * API for the external client application to retrieve the number of separate results
    * obtained, which is the same as the number of leaves selected in the tree table.
    * If we are downloading XNAT scans (= DICOM series), then this is the
    * number of series. Each series will, in general, have a number of image files
    * associated with it.
    *
    * @return an integer containing the number of separate results selected.
    */
   public int getNumberOfSelections()
   {
      return xndao.getNumberOfSelections();
   }
   
   
   
   /**
    * API for the external client application to retrieve the total number of files
    * selected by the user. If the datatype is one for which there is a single
    * file underlying each selection then this will be the same as the number of
    * leaf elements selected in the tree table. However, if we are downloading,
    * for example, DICOM series, then each will, in general, have a number of
    * image files associated.
    * @return the number of files as an int
    */
   public int getTotalNumberOfFiles()
   {
      return xndao.getTotalNumberOfFiles();
   }


   /**
    * API for the external client application to retrieve the number of files associated
    * with a particular result. E.g., if we are downloading XNAT scans (= DICOM studies),
    * then this is the number of individual DICOM files associated with the nth chosen
    * study.
    *
    * @return an integer containing the number of files for the nth element of the selection.
    */
   public int getNumberOfFilesForSelection(int n)
   {
      return xndao.getNumberOfFilesForSelection(n);
   }


   /**
    * API for the external client application to retrieve the file paths associated
    * with a particular result. E.g., if we are downloading XNAT scans (= DICOM studies),
    * then these are the local paths to the DICOM files for the nth scan.
    *
    * @return a String array containing the DICOM files for the nth element of the selection.
    */
   public String[] getFilePathsForSelection(int n)
   {
      return xndao.getFilePathsForSelection(n);
   }
   
   
   
   /**
    * API for the external client application to retrieve the file paths associated
    * with a particular result. E.g., if we are downloading XNAT scans (= DICOM studies),
    * then these are the local paths to the DICOM files for the nth scan.
    *
    * @return a String array containing the DICOM files for the nth element of the selection.
    */
   public String[] getAllFilePaths()
   {
      return xndao.getAllFilePaths();
   }


   /**
    * API for the external client application to retrieve the directory associated
    * with a particular result. E.g., if we are downloading XNAT scans (= DICOM studies),
    * then this is the local directory to the DICOM files for the nth scan.
    *
    * @return a String array containing the DICOM files for the nth element of the selection.
    */
   public String getDirectoryForSelection(int n)
   {
      return xndao.getDirectoryForSelection(n);
   }
   
   
   
   public void cancelPressed()
   {
      System.out.println("Cancelled");
   }
   
   
   
   public void dataSelected()
   {
      System.out.println("Data selected");
   }
}
