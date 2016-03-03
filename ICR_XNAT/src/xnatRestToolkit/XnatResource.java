/********************************************************************
* Copyright (c) 2016, Institute of Cancer Research
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
* Java class: XnatResource.java
* First created on Mar 3, 2016 at 8:29:54 AM
* 
* Definition and methods applicable to a generic resource to be
* uploaded to XNAT via the methods in XNATServerConnection
*********************************************************************/

package xnatRestToolkit;

import java.io.File;
import java.io.InputStream;
import org.w3c.dom.Document;

public class XnatResource
{
	protected File		    file;
	protected Document    doc;
	protected InputStream is;
	protected String      inOut;
	protected String	    name;
	protected String	    format;
	protected String	    content;
	protected String	    description;
	protected String      fileNameOnServer;
	
	
	public XnatResource(File file, String inOut, String name, String format,
			              String content, String description, String fileNameOnServer)
	{
		this.file             = file;
		this.inOut            = inOut;
		this.name             = name;
		this.content          = content;
		this.description      = description;
		this.fileNameOnServer = fileNameOnServer;
	}
	
	
	public XnatResource(Document doc, String inOut, String name, String format,
			              String content, String description, String fileNameOnServer)
	{
		this.doc              = doc;
		this.inOut            = inOut;
		this.name             = name;
		this.content          = content;
		this.description      = description;
		this.fileNameOnServer = fileNameOnServer;
	}
	
	
	public XnatResource(InputStream is, String inOut, String name, String format,
			              String content, String description, String fileNameOnServer)
	{
		this.is               = is;
		this.inOut            = inOut;
		this.name             = name;
		this.content          = content;
		this.description      = description;
		this.fileNameOnServer = fileNameOnServer;
	}
	
	
	/**
	 * Create the XNAT resources folder on the server that form the structure into
	 * which this resource will be placed.
	 * @param uploadRootCommand a String containing the first part of the XNAT URL
	 * containing the project, subject, experiment, etc.
    * @return a String containing the REST URL to which upload will occur
	 */
	public String getResourceDataUploadCommand(String uploadRootCommand)
   {
		StringBuilder sb = new StringBuilder(uploadRootCommand);
		
		if (!inOut.isEmpty()) sb.append("/").append(inOut);
		
		sb.append("/resources/").append(name);
		
		if (fileNameOnServer.isEmpty())
			fileNameOnServer = (file != null) ? file.getName() : "unnamed_resource.dat";
		sb.append("/files/").append(fileNameOnServer);
		
		sb.append("?inbody=true");
		
		if (format != null)  sb.append("&format=").append(format);
		
		if (content != null) sb.append("&content=").append(content);
		
		return sb.toString();
   }
	
	
	/**
	 * Return a URL that defines the XNAT resource on the server into which
	 * a given XNATResourceFile will be loaded.
	 * @param rf the XNAT resource file to be uploaded
    * @return a String containing the REST URL to which upload will occur
	 */
	public String getResourceCreationCommand(String uploadRootCommand)
	{
		StringBuilder sb = new StringBuilder(uploadRootCommand);
				
		if (!inOut.isEmpty()) sb.append("/").append(inOut);
		
		sb.append("/resources/").append(name);
		
		if (!(content.isEmpty() && description.isEmpty() && format.isEmpty()))
			sb.append("?");
		
		if (!content.isEmpty())     sb.append("content=").append(content).append("&");
		if (!description.isEmpty()) sb.append("description=").append(description).append("&");
		if (!format.isEmpty())      sb.append("format=").append(format).append("&");
		
		// This is a legacy of previous code. I'm not sure how this query string
		// parameter fits into the XNAT structure or whether it should be here,
		// since the name is already used in the earlier part of the URL.
		if (!name.isEmpty())        sb.append("name=").append(name);
		
		// Variable name should never be empty, so this condition should never be
		// satisfied. However, this line is left to remind me that it will be
		// necessary if the previous line is deleted!
		if (sb.charAt(sb.length()-1) == '&') sb.deleteCharAt(sb.length()-1);
		
		// Replace spaces in any of the query string parameters with + signs,
		// conforming to the requirements in RFC3986.
		return sb.toString().replace(" ", "+");
	}
	
		
	public String getContent()
	{
		return content;
	}
	
	
	public String getDescription()
	{
		return description;
	}
	
	
	public Document getDocument()
	{
		return doc;
	}
	
	
	public File getFile()
	{
		return file;
	}
	
	
	public String getFileNameOnServer()
	{
		return fileNameOnServer;
	}
	
	
	public String getFormat()
	{
		return format;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public String getInOut()
	{
		return inOut;
	}
	
	
	public InputStream getStream()
	{
		return is;
	}
}
