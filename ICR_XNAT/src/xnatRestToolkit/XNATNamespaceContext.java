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
 ********************************************************************/

/*********************************************************************
* @author Simon J Doran
* Java class: XNATNamespaceContext.java
* First created on March 21, 2010, 12:55 AM
* 
* In order to use an XPath parser on the XML output from XNAT, the
* system needs to be aware of the different namespaces that might be
* present in the output. This class provides the link between the
* prefix and namespace.
*********************************************************************/

package xnatRestToolkit;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


public class XNATNamespaceContext implements NamespaceContext
{

   @Override
   public String getNamespaceURI(String prefix)
   {
      if (prefix == null) throw new IllegalArgumentException("Null prefix");

      else if (prefix.equals("arc"))    return "http://nrg.wustl.edu/arc";
      else if (prefix.equals("cat"))    return "http://nrg.wustl.edu/catalog";
      else if (prefix.equals("pipe"))   return "http://nrg.wustl.edu/pipe";
      else if (prefix.equals("prov"))   return "http://www.nbirn.net/prov";
      else if (prefix.equals("wrk"))    return "http://nrg.wustl.edu/workflow";
      else if (prefix.equals("xdat"))   return "http://nrg.wustl.edu/security";
      else if (prefix.equals("xnat"))   return "http://nrg.wustl.edu/xnat";
      else if (prefix.equals("xnat_a")) return "http://nrg.wustl.edu/xnat_assessments";
      else if (prefix.equals("xsi"))    return "http://www.w3.org/2001/XMLSchema-instance";
      else if (prefix.equals("rpacs"))  return "http://domain.com/ResearchPACS";
		else if (prefix.equals("icr"))    return "http://www.icr.ac.uk/icr";
      else if (prefix.equals("xml"))    return XMLConstants.XML_NS_URI;

      else return XMLConstants.NULL_NS_URI;
   }

   // This method isn't necessary for XPath processing.
   @Override
   public String getPrefix(String uri)
   {
      throw new UnsupportedOperationException();
   }

   // This method isn't necessary for XPath processing either.
   @Override
   public Iterator getPrefixes(String uri)
   {
      throw new UnsupportedOperationException();
   }
   
}
