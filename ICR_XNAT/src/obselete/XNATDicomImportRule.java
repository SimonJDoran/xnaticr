/******************************************************************
 * XNATDicomLoader MiniApplication
 *
 * @author        Simon J. Doran
 * Creation date: Mar 30, 2009 at 9:55:54 AM
 *
 * Filename:      XNATDicomImportRule.java
 * Package:       xnat_experiments
 *
 * This class establishes the correspondence between entries in the
 * DICOM header and the values stored in the XNAT database.
 ******************************************************************/


package obselete;

import obselete.XNATDicomImportRuleException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.dcm4che2.data.DicomObject;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import obselete.XNATDicomImportRule.XNATDicomImportElement;


public class XNATDicomImportRule  extends Hashtable<String, XNATDicomImportElement>
{
   public XNATDicomImportRule() throws XNATDicomImportRuleException
   {
      super();    // Create a new Hashtable.

      try
      {
         File importRuleFile = getImportDefinitionFile();

         // Use a standard XML parser to extract the elements. Refer to methods
         // startElement and endElement for further details.
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(importRuleFile, new XNATDicomSAXAdapter());

         // Check that all has read in correctly.
         /*System.out.println("Import rule tags");
         Enumeration<String> e = this.keys();
         while (e.hasMoreElements()) {
            String key = (e.nextElement());
            XNATDicomImportElement ie = get(key);
            System.out.println(key);
            for (int i=0; i<ie.nDICOMTags; i++)
            {
               System.out.println("DICOMTag " + ie.DICOMTags[i]);
            }
         }*/
      }
		catch (IOException exIO)
		{
			throw new XNATDicomImportRuleException(XNATDicomImportRuleException.IO);
		}

      catch (Exception ex)
      {
         throw new XNATDicomImportRuleException(XNATDicomImportRuleException.PARSING,
                 ex.toString());
      }

   }


   // Find the XML file containing the XNAT DICOM import rule.
   public File getImportDefinitionFile() throws XNATDicomImportRuleException
   {
      java.net.URL importRuleURL =
              XNATDicomParameters.class.getResource("projectResources/XNAT_DICOM_import_rule.xml");
		String s = importRuleURL.getPath();

		// When the path comes back from this call, any spaces (e.g., in Windows
		// "My Documents") come back as "%20". This can be rectified by the call below.
		try
		{
			s = URLDecoder.decode(importRuleURL.getPath(), "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new XNATDicomImportRuleException(XNATDicomImportRuleException.IO);
		}

		return new File(s);
   }



   protected class XNATDicomImportElement
   {
      private String defaultValue;
      private int    nDICOMTags;
      private int[]  DICOMTags;


      XNATDicomImportElement(String defaultValue, int nDICOMTags, int[] DICOMTags)
      {
         this.defaultValue  = defaultValue;
         this.nDICOMTags    = nDICOMTags;
         this.DICOMTags     = DICOMTags;
      }

      public int[] DICOMTags()
      {
         int[] a = new int[nDICOMTags];
         for (int i=0; i<nDICOMTags; i++)  a[i] = DICOMTags[i];
         return a;
      }

      public String getDefault()
      {
         return defaultValue;
      }

   }




   private final class XNATDicomSAXAdapter extends DefaultHandler
	{
      int      nesting        = 0;
      int      nDICOMTags     = 0;
      int[]    DICOMTags      = new int[10];
      String   tagName;
      String   defaultVal;
      Locator  locator;


      @Override
      public void setDocumentLocator(Locator locator)
      {
         this.locator = locator;
      }

		@Override
		public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
		{
         int         col   = -1;
         int         line  = -1;

         if (locator != null)
         {
            col  = locator.getColumnNumber();
            line = locator.getLineNumber();
         }

			if (qName.equals("rule"))
			{
            if (nesting != 0)
               throw new SAXParseException("Illegal nesting at line " + line, locator);
            nesting = 1;
         }

         if (qName.equals("tag"))
         {
            if (nesting != 1)
               throw new SAXParseException("Illegal nesting at line " + line, locator);
            nesting     = 2;
            nDICOMTags  = 0;
            for (int i=0; i<10; i++) DICOMTags[i]     = 0;
            tagName     = attributes.getValue("name");
            defaultVal  = attributes.getValue("defaultValue");
         }

         if (qName.equals("DICOMTag"))
         {
            if (nesting != 2)
               throw new SAXParseException("Illegal nesting at line " + line, locator);
            nesting     = 3;
            nDICOMTags += 1;
            if (nDICOMTags > 10)
               throw new SAXParseException("Too many DICOM tags at line " + line, locator);

            int group    = (int) Integer.parseInt(attributes.getValue("group"), 16);
            int element  = (int) Integer.parseInt(attributes.getValue("element"), 16);
            int priority = (int) Integer.parseInt(attributes.getValue("priority"));
            if ((priority < 1) || (priority>10) || DICOMTags[priority-1] != 0)
               throw new SAXParseException("Invalid priority value at line " + line, locator);
            DICOMTags[priority-1]     = group*65536 + element;
         }

	   }



		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException
		{
			if (qName.equals("rule")) nesting -= 1;

         if (qName.equals("tag"))
         {
            nesting -= 1;

            // Check that the DICOMTags is full of values all the way from 0 to
            // nDICOMTags-1. This will imply that the priority attributes were all
            // specified correctly.
            boolean prioritiesCorrect = true;
            for (int i=0; i<nDICOMTags; i++)
               if (DICOMTags[i] == 0) prioritiesCorrect = false;
            if (!prioritiesCorrect)
               throw new SAXParseException("Invalid set of priorities for element " + tagName, locator);

            // Note we cannot simply pass DICOMTags to the constructor below,
            // because what is saved is the pointer to the array, not the values,
            // and the pointer to the instance variable remains the same between calls.
            int[] dtags = new int[10];
            for (int i=0; i<10; i++) dtags[i] = DICOMTags[i];

            put(tagName, new XNATDicomImportElement(defaultVal, nDICOMTags, dtags));
         }

         if (qName.equals("DICOMTag")) nesting -= 1;
      }

   }  // End of class XNATDicomSAXAdapter

}  // End of class XNATDicomImportRule

