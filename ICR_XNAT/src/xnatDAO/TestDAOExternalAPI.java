/*******************************************************************
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

/********************************************************************
* @author Simon J Doran
* Java class: TestDAOExternalAPI.java
* First created on Jan 7, 2013 at 12:56:25 PM
* 
* Although this is far from a complete unit test package, the aim of
* this class is to provide a first, quick verification of the
* capabilities of the external API of the DataChooser (a.k.a. XNAT
* Data Access Object a.k.a. XNAT_DAO)
*********************************************************************/

package xnatDAO;

public class TestDAOExternalAPI
{

   public static void runTest(XNATDAO xndao)
   {
      System.out.println("*******************************************************");
      System.out.println("XNAT DataChooser API test  Version " + xndao.getVersion());
      System.out.println("*******************************************************");
      System.out.println(" ");

      System.out.println("An XNATDataChooser object has been created with the"
                          + " following properties:");
      System.out.println(xndao.toString());
      System.out.println(" ");

      // If the specified profile has been deleted from the current list,
      // then this call will return silently.
      xndao.setProfile("Central");
      System.out.println("XNAT login profile has been set to \""
                          + xndao.getProfile() + "\".");
      System.out.println(" ");

      DAOSearchCriteriaSet scs = xndao.createNewSearchCriteriaSet();
      xndao.addSearchCriterion(scs, "Scanner manufacturer", "LIKE", "%");
      xndao.addSearchCriterion(scs, "Scanner model", "LIKE", "%");
      xndao.applySearchCriteria(scs);
      System.out.println("Non-default search criteria have been entered and "
                           + "validated (return code: "
                           + xndao.searchCriteriaValid(scs) + ")."); 
      System.out.println(" ");

      // Now show the GUI and perform a search automatically.
      xndao.invoke(true);

      System.out.println("The status of this data retrieval operation is "
                         + xndao.getStatus() + ".");
      System.out.print(" ");
      

      int nSel = xndao.getNumberOfSelections();
      System.out.println("The number of selections made by the DataChooser was "
                          + nSel + ".");
      System.out.println(" ");
      if (nSel != 0)
      {
         for (int i=0; i<nSel; i++)
         {
            System.out.println("The number of files contained in selection "
                               + i + " is "
                               + xndao.getNumberOfFilesForSelection(i) + ".");
            System.out.println(" ");
            System.out.println("The directory for selection " + i + " was:");
            System.out.println(xndao.getDirectoryForSelection(i));
            System.out.println(" ");
            System.out.println("List of all file paths for selection " + i + ":");
            String[] paths = xndao.getFilePathsForSelection(i);
            for (String s : paths) System.out.println(s);
            System.out.println(" ");
         }

         System.out.println("The total number of files selected was "
                             + xndao.getTotalNumberOfFiles() + "."); 

         System.out.println("List of all file paths for the selections made by the user:");
         String[] paths = xndao.getAllFilePaths();
         for (String s : paths) System.out.println(s);
         System.out.println(" ");

         System.out.println("We can also retrieve the file paths in an ArrayList<File>:");
         System.out.println(xndao.getOutputFileArrayList().toString());
      }
      
      System.out.println(" ");

      System.out.println("Resetting the DataChooser to the default and re-invoking,"
                          + " this time without an automatic search.");
      xndao.reset();
      System.out.println(" ");
      
      xndao.setCurrentSettings("test table settings");
      System.out.println("TreeTable settings changed to " + xndao.getCurrentSettings());
      System.out.println(" ");
      
      xndao.setLeafElement("XNAT scan ID");
      System.out.println("Leaf element changed to " + xndao.getLeafElement());
      System.out.println(" ");
      
      xndao.invoke(false);

      System.out.println("Status of second retrieval operation is "
                          + xndao.getStatus());
      
      System.exit(0);
   }
}
