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
* Java class: DelayedPrettyPrinterXmlWriter.java
* First created on Jun 29, 2009 at 4:12:02 PM
* 
* Store a stack of XmlWriter commands and execute them only if the
* objects that are written actually exist.
*********************************************************************/

package xmlUtilities;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * The main purpose of this class is to store a stack of XmlWriter commands
 * and execute them only if the objects that are written actually exist. So,
 * we wait until .endElement() is called and only then do we write everything.
 * This neatens up the calling code, avoiding the need to continually test
 * for the existence of the arguments and permitting the use of a chain of calls
 * to the output routines via the ". syntax".
 *
 * Unfortunately, the requirement to do this means that the class cannot
 * implement the XmlWriter interface, because this requires that all the methods
 * return XmlWriter objects that do not include access to the enhanced methods.
 * This would break the chaining facility.
 *
 * The rules are as follows:
 *
 * 1. If no text or attribute calls are made between an invocation of
 *    delayedWriteElement and its corresponding delayedEndElement,
 *    then write nothing.
 *
 * 2. If all of the text or attribute calls made between an invocation of
 *    delayedWriteElement and its corresponding delayedEndElement are
 *    *either* null (_or start with the text "Unknown"_ this condition removed
 *    11.3.16), then write nothing for that element.
 *
 * 3. Special case relevant only to XNAT. If the element is an xnat:addParam,
 *    then it will always have an attribute that is non-null (i.e., the name).
 *    We still need to write nothing if the body of the element is null.
 */
public class DelayedPrettyPrinterXmlWriter
{
   Stack<Call>            callStack;
   PrettyPrinterXmlWriter ppXMLw;
   String                 currentEntityName;
   int                    currentParent;

   private class Call
   {
      String  command;
      String  argument;
      Object  attributeValue;
      boolean writeIt;
      int     parentWriteEntityCall;
      int     endEntityCall;
      
      private Call(String  command,
                   String  argument,
                   Object  attributeValue,
                   boolean writeIt,
                   int     parentWriteEntityCall,
                   int     endEntityCall)
      {
         this.command               = command;
         this.argument              = argument;
         this.attributeValue        = attributeValue;
         this.writeIt               = writeIt;
         this.parentWriteEntityCall = parentWriteEntityCall;
         this.endEntityCall         = endEntityCall;
      }
   }


   public DelayedPrettyPrinterXmlWriter(XmlWriter XMLw)
   {
      callStack     = new Stack<Call>();
      ppXMLw        = new PrettyPrinterXmlWriter(XMLw);
      currentParent = -1;
   }
   


   private void setParentTrue(int callNumber)
   {
      Call c = callStack.elementAt(callNumber);
      c.writeIt = true;
      if (c.parentWriteEntityCall != -1) setParentTrue(c.parentWriteEntityCall);
   }




   public DelayedPrettyPrinterXmlWriter
            delayedWriteAttribute(String argument, Object attributeValue)
   {
      delayedWriteAttribute(argument, attributeValue, false);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            delayedWriteAttribute(String argument, Object attributeValue, boolean forceWrite)
   {
      boolean writeIt = false;
      if (forceWrite ||
          !((attributeValue == null) ||
 //           (attributeValue.toString().startsWith("Undefined")) ||
            (currentEntityName.equals("xnat:addParam"))))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      /* Note that we don't need to know the endElement call for anything
       * except the writeEntity entries, so -1 is inserted here.
       */
      callStack.push(new Call("writeAttribute",
                              argument,
                              attributeValue,
                              writeIt,
                              currentParent,
                              -1));

      return this;
   }
   



   public DelayedPrettyPrinterXmlWriter delayedWriteCData(String s)
   {
      delayedWriteCData(s, false);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter delayedWriteCData(String s, boolean forceWrite)
   {
      boolean writeIt = false;
      if (forceWrite || (s != null))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      callStack.push(new Call("writeCData",
                              s,
                              null,
                              writeIt,
                              currentParent,
                              -1));
      
      return this;
   }
   



   public DelayedPrettyPrinterXmlWriter delayedWriteComment(String s)
   {
      delayedWriteComment(s, false);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter delayedWriteComment(String s, boolean forceWrite)
   {
      boolean writeIt = false;
      if (forceWrite || (s != null))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      callStack.push(new Call("writeComment",
                              s,
                              null,
                              writeIt,
                              currentParent,
                              -1));

      return this;
   }
   
   




   public DelayedPrettyPrinterXmlWriter delayedWriteEmptyEntity(String argument)
   {
      delayedWriteEmptyEntity(argument, false);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter delayedWriteEmptyEntity(String argument, boolean forceWrite)
   {
      boolean writeIt = false;
      if (forceWrite || (argument != null))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      callStack.push(new Call("writeEmptyEntity",
                              argument,
                              null,
                              writeIt,
                              currentParent,
                              -1));

      return this;
   }


   
   
   
   public DelayedPrettyPrinterXmlWriter delayedWriteEntity(String argument)
   {
      delayedWriteEntity(argument, false);
      return this;
   }
   
   
   public DelayedPrettyPrinterXmlWriter delayedWriteEntity(String argument, boolean forceWrite)
   {
      /* We *do* need to know the position in the stack of the corresponding
       * endEntity call, but can't know it until the corresponding delayedEndEntity
       * method is executed. So place -1 here for the moment and then get
       * delayedEndEntity to insert a value later.
       */
      callStack.push(new Call("writeEntity",
                              argument,
                              null,
                              forceWrite,
                              currentParent,
                              -1));

      currentEntityName = argument;
      currentParent = callStack.size()-1;
      
      return this;
   }




   public DelayedPrettyPrinterXmlWriter delayedWriteEntityWithText(
                                             String argument, Object text)
   {
      delayedWriteEntityWithText(argument, text, false);
      return this;
   }

   public DelayedPrettyPrinterXmlWriter delayedWriteEntityWithText(
                                             String argument, Object text, boolean forceWrite)
   {
      boolean writeIt = false;
      if (forceWrite || (text != null))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      /* Note: text.toString() is not an attribute name, but it does not seem
       * worth creating another field in the Call object just for this one case.
       */
		String textString = null;
		if (text != null) textString = text.toString();
      callStack.push(new Call("writeEntityWithText",
                              argument,
                              textString,
                              writeIt,
                              currentParent,
                              -1));

      currentEntityName = argument;

      return this;
   }




   public DelayedPrettyPrinterXmlWriter delayedWriteText(String argument)
   {
      delayedWriteText(argument, false);
      return this;
   }

   public DelayedPrettyPrinterXmlWriter delayedWriteText(String argument, boolean forceWrite)
   {
      boolean writeIt = false;
   //   if ( forceWrite || ((argument != null) && (!argument.startsWith("Undefined"))))
		if ( forceWrite || (argument != null))
      {
         writeIt = true;
         setParentTrue(currentParent);
      }

      callStack.push(new Call("writeText",
                              argument,
                              null,
                              writeIt,
                              currentParent,
                              -1));

      return this;
   }




   public DelayedPrettyPrinterXmlWriter delayedEndEntity() throws IOException
   {
      callStack.push(new Call("endEntity",
                              null,
                              null,
                              false,
                              currentParent,
                              -1));
      
      /* Note how the writeIt field of the endEntity call above is currently
       * false. This is because, in the end, the entity may never be written,
       * so we might not want to end it. We now need to let the corresponding
       * writeEntityCall know the location of this endEntity, so that it can
       * reset the writeIt field to true later as necessary.
       */
      Call parentWriteEntityCall = callStack.elementAt(currentParent);
      parentWriteEntityCall.endEntityCall = callStack.size()-1;

      /* If this is not the top-level parent, then simply reset the parent
       * to the one from the previous level.
       */
      if (currentParent != 0)
      {
         currentParent = callStack.elementAt(currentParent).parentWriteEntityCall;
         return this;
      }


      /* At the top level parent, simply go through the command stack and use
       * the methods from the underlying PrettyPrinterXmlWriter to create the
       * XML, but only for those elements where writeIt is set.
       */
      for (int i=0; i<callStack.size(); i++)
      {
         Call c = callStack.elementAt(i);
         
         if (c.writeIt)
         {
				System.out.println(c.command + "  |  " + c.argument);
            if (c.command.equals("writeAttribute"))
               ppXMLw.writeAttribute(c.argument, c.attributeValue);

            if (c.command.equals("writeCData"))
               ppXMLw.writeCData(c.argument);

            if (c.command.equals("writeComment"))
               ppXMLw.writeComment(c.argument);

            if (c.command.equals("writeEmptyEntity"))
               ppXMLw.writeEmptyEntity(c.argument);

            if (c.command.equals("writeEntity"))
            {
               ppXMLw.writeEntity(c.argument);
               Call endCall = callStack.elementAt(c.endEntityCall);
               endCall.writeIt = true;
            }

            if (c.command.equals("writeEntityWithText"))
               ppXMLw.writeEntityWithText(c.argument, c.attributeValue);

            if (c.command.equals("writeText"))
               ppXMLw.writeText(c.argument);

            if (c.command.equals("endEntity"))
               ppXMLw.endEntity();
         }
      }

      return this;
   }

   /* -----------------------------------------------------------
    * Now implement all the methods from the XmlWriter interface,
    * but with the difference that they all return an object of
    * type DelayedPrettyPrinterXmlWriter.
    *
    * Note that we need to retain all the write, as opposed to
    * delayedWrite methods, as there may be cases where we want
    * to force the writing of objects that don't follow the rules
    * given at the top of the object definition.
    * -----------------------------------------------------------
    */

   public DelayedPrettyPrinterXmlWriter writeXmlVersion() throws IOException
   {
      ppXMLw.writeXmlVersion();
      return this;
   }


   public DelayedPrettyPrinterXmlWriter setIndent(String s) throws IOException
   {
      ppXMLw.setIndent(s);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            writeXmlVersion(String version, String encoding) throws IOException
   {
      ppXMLw.writeXmlVersion(version, encoding);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            writeXmlVersion(String version, String encoding, String standalone) throws IOException {
      ppXMLw.writeXmlVersion(version, encoding, standalone);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            writeEntityWithText(String name, Object text) throws IOException
   {
      delayedWriteEntityWithText(name, text, true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            writeEmptyEntity(String name) throws IOException
   {
      delayedWriteEmptyEntity(name, true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
            writeEntity(String name) throws IOException
   {
      delayedWriteEntity(name, true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter
             writeAttribute(String attr, Object value) throws IOException
   {
      delayedWriteAttribute(attr, value, true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter endEntity() throws IOException
   {
      delayedEndEntity();
      return this;
   }


   public void close() throws IOException
   {
      ppXMLw.close();
   }


   public DelayedPrettyPrinterXmlWriter writeText(Object text) throws IOException
   {
      delayedWriteText(text.toString(), true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter writeCData(String cdata) throws IOException
   {
      delayedWriteCData(cdata, true);
      return this;
   }


   public DelayedPrettyPrinterXmlWriter writeComment(String comment) throws IOException
   {
      delayedWriteComment(comment, true);
      return this;
   }


   public Writer getWriter()
   {
      return ppXMLw.getWriter();
   }

}
