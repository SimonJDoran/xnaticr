/********************************************************************
* @author Simon J Doran
* Java class: ExampleForJames.java
* First created on Jun 22, 2016 at 3:39:22 PM
*********************************************************************/

package xnatUploader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import xnatDAO.XNATProfile;

public class ExampleForJames
{
	String userid;
	String password;
	File   fileToCheck;
	
	public ExampleForJames(String userid, String password, File file)
	{
		super();
		this.userid   = userid;
		this.password = password;
		fileToCheck   = file;
	}
	
	
	public void getDataFromXnat()
	{
		ArrayList<String> projectList = new ArrayList<>();
		
		projectList.add("BRC_RADPRIM");
		URL XnatServerUrl;
		try
		{
			XnatServerUrl = new URL("https://bifrost.icr.ac.uk:8443/XNAT_ROI");
		}
		catch (MalformedURLException exMFU) {return;}
		
		XNATProfile xnprf = new XNATProfile("myProfile",
				                              XnatServerUrl,
				                              userid,
				                              password,				                              
		                                    projectList,
		                                    System.currentTimeMillis());
		
	DataUploader du = new AimImageAnnotationCollectionDataUploader();
	}
}
