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
* Java class: AbstractResource.java
* First created on Jan 20, 2016 at 10:24:37 AM
* 
* Representation an XNAT AbstractResource
* 
*********************************************************************/


package dataRepresentations.xnatSchema;

import java.util.List;

public class AbstractResource extends XnatSchemaElement
{
	public static final class Tag
	{
		public String name;
		public String value;
		
	   public Tag(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
		
		public void setName(String s)
		{
			name = s;
		}
		
		public void setValue(String s)
		{
			value = s;
		}
	}
	
	public String    label;
	public Integer   fileCount;
	public Long      fileSize;
	public String    note;
	public List<Tag> tagList;

	// Give users the option to use either a single-line constructor (with
	// possible nulls). Given that there is no "implementation" as such -
	// these are just variables - there seems no reason to invoke setter
	// methods and I will just expose the variables publicly.
	public AbstractResource() {}
	
	public AbstractResource(String label, Integer fileCount, Long fileSize,
			                  String note, List<Tag> tags)
	{
		this.label     = label;
		this.fileCount = fileCount;
		this.fileSize  = fileSize;
		this.note      = note;
		this.tagList   = tags;
	}	
}
