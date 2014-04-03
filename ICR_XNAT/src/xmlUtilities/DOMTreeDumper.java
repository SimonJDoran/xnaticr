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
* Java class: DOMTreeDumper.java
* First created on May 18, 2009 at 12:05:19 PM
* 
* Print to System.out a dump of a DOM document. Code modified from
* http://www.java2s.com/Code/Java/XML/
*                           UsingtheDOMParsertoBuildaDocumentTree.htm
*********************************************************************/

package xmlUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMTreeDumper
{
    public void dump(Document doc)
    {
        dumpLoop((Node) doc,"");
    }
    private void dumpLoop(Node node, String indent)
    {
       switch(node.getNodeType())
       {
          case Node.CDATA_SECTION_NODE:
              System.out.println(indent + "CDATA_SECTION_NODE");
              break;

          case Node.COMMENT_NODE:
              System.out.println(indent + "COMMENT_NODE");
              break;

          case Node.DOCUMENT_FRAGMENT_NODE:
              System.out.println(indent + "DOCUMENT_FRAGMENT_NODE");
              break;

          case Node.DOCUMENT_NODE:
              System.out.println(indent + "DOCUMENT_NODE");
              break;

          case Node.DOCUMENT_TYPE_NODE:
              System.out.println(indent + "DOCUMENT_TYPE_NODE");
              break;

          case Node.ELEMENT_NODE:
              System.out.println(indent + "ELEMENT_NODE:  " + node.getNodeName());
              if (node.hasAttributes())
              {
                 NamedNodeMap attrs = node.getAttributes();
                 for (int i=0; i<attrs.getLength(); i++)
                 {
                    System.out.println(indent + "   ATTRIBUTE:  " +
                            attrs.item(i).getNodeName() + "  " +
                            attrs.item(i).getNodeValue());
                 }
              }
              break;

          case Node.ENTITY_NODE:
              System.out.println(indent + "ENTITY_NODE");
              break;

          case Node.ENTITY_REFERENCE_NODE:
              System.out.println(indent + "ENTITY_REFERENCE_NODE");
              break;

          case Node.NOTATION_NODE:
              System.out.println(indent + "NOTATION_NODE");
              break;

          case Node.PROCESSING_INSTRUCTION_NODE:
              System.out.println(indent + "PROCESSING_INSTRUCTION_NODE");
              break;

          case Node.TEXT_NODE:
              System.out.println(indent + "TEXT_NODE:  "  + node.getTextContent());
              break;

          default:
              System.out.println(indent + "Unknown node");
              break;
        }

        NodeList list = node.getChildNodes();

        for(int i=0; i<list.getLength(); i++)
            dumpLoop(list.item(i), indent + "  ");
    }
}
