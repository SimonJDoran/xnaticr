/********************************************************************
* @author Simon J Doran
* Java class: ExampleForJames.java
* First created on Jun 22, 2016 at 3:39:22 PM
*********************************************************************/

package shellCommands;

import exceptions.XMLException;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Document;
import xmlUtilities.XMLUtilities;
import xnatDAO.XNATProfile;
import xnatUploader.AimImageAnnotationCollectionDataUploader;
import xnatUploader.DataUploader;

public class AimGetInfoExample
{	
	public AimGetInfoExample() {}
	
	
	public void displayInfoFromDataUploader(XNATProfile xnprf)
	{
      Class cls         = this.getClass();
      URL   resourceUrl = cls.getResource("/examples/resources/sampleAimInstanceFile.xml");
      
      File  aimFile;
      try
      {
         aimFile     = new File(resourceUrl.toURI());
      }
      catch (URISyntaxException exUS)
      {
         System.out.println("Can't access example AIM file. Exiting.");
         return;
      }
      
      AimImageAnnotationCollectionDataUploader du
              = new AimImageAnnotationCollectionDataUploader(xnprf);
      
      du.setUploadFile(aimFile);
      
      boolean r;
      
      System.out.println("Demonstration of information available via a DataUploader");
      System.out.println("---------------------------------------------------------");
      System.out.println();
      System.out.print("Parsing test file ...  ");
      r = du.parseFile();
      System.out.println(r ? "Succeeded" : "Failed");
      if (!r)
      {
         System.out.println(du.getErrorMessage());
         return;
      }
      
      System.out.println("Uploader variables available");
      du.reportFieldValues();
     
   }
   
   
   public void resultsOfTypicalRestCalls(XNATProfile xnprf)
   {
      System.out.println();
      System.out.println("Results of some typical REST calls");
      System.out.println("----------------------------------");
      
      String RestCommand1 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments?format=xml";
      Document resultDoc1 = xnprf.getDOMDocument(RestCommand1);
      
      String RestCommand2 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments/XNAT_ROI_E00037/assessors?format=xml";
      Document resultDoc2 = xnprf.getDOMDocument(RestCommand2);
  
      String RestCommand3 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments/XNAT_ROI_E00037/assessors/AimImageAnnotationCollection_1ARRBQjET_PopxUX3/resources?format=xml";
      Document resultDoc3 = xnprf.getDOMDocument(RestCommand3);
      
      String RestCommand4 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments/XNAT_ROI_E00037/assessors/AimImageAnnotationCollection_1ARRBQjET_PopxUX3/resources/AIM-INSTANCE/files?format=xml";
      Document resultDoc4 = xnprf.getDOMDocument(RestCommand4);
      
      String RestCommand5 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments/XNAT_ROI_E00037/assessors/RegionSet_1ARRBQjET_PopxUX4/resources?format=xml";
      Document resultDoc5 = xnprf.getDOMDocument(RestCommand5);
      
      String RestCommand6 = "/data/archive/projects/BRC_RADPRIM/subjects/XNAT_ROI_S00035/experiments/XNAT_ROI_E00037/assessors/RegionSet_1ARRBQjET_PopxUX4/resources/RT-STRUCT/files?format=xml";
      Document resultDoc6 = xnprf.getDOMDocument(RestCommand6);
      try
      {
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc1));
         System.out.println();
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc2));
         System.out.println();
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc3));
         System.out.println();
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc4));
         System.out.println();
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc5));
         System.out.println();
         System.out.println(XMLUtilities.dumpDOMDocument(resultDoc6));
      }
      catch(XMLException exXML)
      {
         System.out.println(exXML.getMessage());
      }
   
   }
   
   public static void main(String args[])
   {
      ArrayList<String> projectList = new ArrayList<>();
		
		projectList.add("BRC_RADPRIM");
		URL XnatServerUrl;
		try
		{
			XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_ROI");
		}
      catch (MalformedURLException exMFU) {return;}
		
      String dicomReceiverHost    = "bifrost.icr.ac.uk";
      int    dicomReceiverPort    = 8104;
      String dicomReceiverAeTitle = "XNAT";
      
		XNATProfile xnprf = new XNATProfile("myProfile",
				                              XnatServerUrl,
				                              args[0],
				                              args[1],				                              
		                                    projectList,
		                                    System.currentTimeMillis(),
                                          dicomReceiverHost,
                                          dicomReceiverPort,
                                          dicomReceiverAeTitle);
		
      AimGetInfoExample agie = new AimGetInfoExample();
            
      agie.displayInfoFromDataUploader(xnprf);
      
      agie.resultsOfTypicalRestCalls(xnprf);
      
      System.out.println("Finished");
   }
}
