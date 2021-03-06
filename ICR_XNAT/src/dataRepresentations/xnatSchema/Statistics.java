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

/*********************************************************************
* @author Simon J Doran
* Java class: XnatStatistics.java
* First created on Jan 20, 2016 at 4:20:18 PM
* 
* Data structure parallelling the xnat:statisticsData element and
* used in conjunction with XnatStatisticsDataMDComplexType.java
*********************************************************************/

package dataRepresentations.xnatSchema;

import dataRepresentations.xnatSchema.XnatSchemaElement;
import dataRepresentations.xnatSchema.AdditionalField;
import java.util.List;

public class Statistics extends XnatSchemaElement
{
	public Double  mean;
	public Double  snr;
	public Double  min;
	public Double  max;
	public Double  stdev;
	public Integer nVoxels;
	public List<AdditionalField> additionalStatisticsList;
	public List<AdditionalField> addFieldList;
	
	public Statistics() {}
	
	public Statistics( Double mean, Double snr, Double min, Double max,
					           Double stdev, Integer nVoxels,
								  List<AdditionalField> addStatList,
								  List<AdditionalField> addFieldList)
	{
		this.mean                     = mean;
		this.snr                      = snr;
		this.min                      = min;
		this.max                      = max;
		this.stdev                    = stdev;
		this.nVoxels                  = nVoxels;
		this.additionalStatisticsList = addStatList;
		this.addFieldList             = addFieldList;
	}
}
