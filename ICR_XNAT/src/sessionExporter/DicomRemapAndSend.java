/********************************************************************
* Copyright (c) 2015, Institute of Cancer Research
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
* Java class: DicomRemapAndSend.java
* First created on Sep 22, 2015 at 5:15:24 PM
* 
* Class based (loosely) on elements extracted from CSVRemapper by
* Kevin Archie for applying a DicomEdit script to a DICOM file,
* primarily for the purpose of anonymisation and exporting to a new
* DICOM node.
*********************************************************************/

package sessionExporter;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.TagUtils;
import org.dom4j.DocumentException;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.Statement;
import org.nrg.dcm.edit.ConfigurableDirectoryRecordFactory;
import org.nrg.dcm.edit.DicomTableEntry;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.nrg.dcm.edit.Value;
import org.nrg.dcm.edit.Variable;
import org.nrg.dcm.io.BatchExporter;
import org.nrg.dcm.io.CStoreExporter;
import org.nrg.dcm.io.DicomObjectExporter;
import org.nrg.dcm.io.NewRootFileExporter;
import org.nrg.dcm.io.TransferCapabilityExtractor;
import org.nrg.io.FileWalkIterator;

public class DicomRemapAndSend
{
	private static final int              DICOM_DEFAULT_PORT = 104;
	private static final String           AE_TITLE           = "DicomRemap";
	protected static     Logger           logger             = Logger.getLogger(DicomRemapAndSend.class);
	private final List<Statement>         globalStatements   = Lists.newArrayList();
	private final PrintStream             messages           = System.err;
	private final DicomObject             template           = new BasicDicomObject();
	private final Collection<RemapColumn> remaps;
	private final Map<String,Map<Integer,Integer>> selectionKeysToCols;
	
	private JTextArea                     logJTextArea;
	
	
	private static final class RemapColumn
	{
		final String level;
		final int index;
		final int tag;
		 
		RemapColumn(final String level, final int index, final int tag)
		{
			this.level = level;
			this.index = index;
			this.tag   = tag;
		}
		
		String getLevel() { return level; }
		int    getIndex() { return index; }
		int    getTag()   { return tag; }

		public String toString()
		{
			final StringBuilder sb = new StringBuilder("RemapColumn ");
			sb.append(index);
			sb.append(" ");
			sb.append(TagUtils.toString(tag));
			sb.append(" (level ");
			sb.append(level);
			sb.append(")");
			return sb.toString();
		}
	}
	 

	private static final class RemapContext
	{
		final String level;
		final Map<Integer,String> selectionKeys;

		RemapContext(final String level, final Map<Integer,String> keys)
		{
			this.level         = level;
			this.selectionKeys = ImmutableMap.copyOf(keys);
		}

		public boolean equals(final Object o)
		{
			if (!(o instanceof RemapContext)) return false;
			final RemapContext other = (RemapContext) o;
			return level.equals(other.level) && selectionKeys.equals(other.selectionKeys);
		}

		public int hashCode()
		{
			return Objects.hashCode(level, selectionKeys);
		}

		public String toString()
		{
			final StringBuilder sb = new StringBuilder("RemapContext (level ");
			sb.append(level);
			sb.append("): ");
			
			for (final Map.Entry<Integer,String> e : selectionKeys.entrySet())
			{
				sb.append(TagUtils.toString(e.getKey()));
				sb.append("=\"");
				sb.append(e.getValue());
				sb.append("\" ");
			}
			
			return sb.toString();
		}
	}

	private static final class RemapWithContext
	{
		final RemapContext context;
		final RemapColumn column;

		RemapWithContext(final RemapContext context, final RemapColumn column)
		{
			this.context = context;
			this.column = column;
		}

		public boolean equals(final Object o)
		{
			if (!(o instanceof RemapWithContext)) return false;
			final RemapWithContext other = (RemapWithContext)o;
			return context.equals(other.context) && column.equals(other.column);
		}

		public int hashCode()
		{
			return Objects.hashCode(context, column);
		}

		public String toString()
		{
			final StringBuilder sb = new StringBuilder("RemapWithContext: ");
			sb.append(column);
			sb.append(" in ");
			sb.append(context);
			return sb.toString();
		}
	}
	
	private static final class InvalidRemapsException extends Exception {
        private static final long serialVersionUID = 1L;
        private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        final Collection<RemapWithContext> underspecified;
        final Multimap<RemapWithContext,String> overspecified;

        InvalidRemapsException(Iterable<RemapWithContext> underspecified, Multimap<RemapWithContext,String> overspecified) {
            this.underspecified = Lists.newArrayList(underspecified);
            this.overspecified = overspecified;
            assert !this.underspecified.isEmpty() || !overspecified.isEmpty();           
        }
	}
	
	public DicomRemapAndSend(JTextArea logJTextArea)
			 throws IOException, ParseException, DocumentException, Exception
	{
		this.logJTextArea = logJTextArea;
		InputStream is    = new ByteArrayInputStream(logJTextArea.getText()
				                                                   .getBytes(StandardCharsets.UTF_8));
		
		File configFile = new File("dummy");
		final ConfigurableDirectoryRecordFactory factory = new ConfigurableDirectoryRecordFactory(configFile);
		final List<DicomTableEntry> columns = Lists.newArrayList(factory.getColumns());

		// Collect all remappings, and all levels for which a remapping is defined
		final Collection<String> levels = Sets.newHashSet();
		remaps = Lists.newArrayList();
		
		for (int i = 0; i < columns.size(); i++)
		{
			final DicomTableEntry col = columns.get(i);
			if (col.isSubstitution())
			{
				final String level = col.getLevel();
				remaps.add(new RemapColumn(level, i, col.getTag()));
				levels.add(level);
			}
		}

		// For each level for which a remapping is defined, figure out which columns hold the selection keys.
		selectionKeysToCols = Maps.newHashMap();
		for (final String level : levels)
		{
			final Collection<Integer>  selectionTags = factory.getSelectionTags(level);
			final Map<Integer,Integer> keysToCols    = Maps.newLinkedHashMap();
			selectionKeysToCols.put(level, keysToCols);

			for (final int tag : selectionTags)
			{
				boolean foundTag = false;
				for (int i = 0; i < columns.size(); i++)
				{
					final DicomTableEntry col = columns.get(i);
					if (!col.isSubstitution() && tag == col.getTag())
					{
						foundTag = true;
						keysToCols.put(tag, i);
						break;
					}
				}
				if (!foundTag)
               throw new Exception("Unable to find column for level " + level + " required tag " + TagUtils.toString(tag));
			}
		}
	}
	

	public Map<?,?> apply(final URI out, final Collection<File> files)
                   throws IOException, AttributeException, InvalidRemapsException, SQLException
	{
		final List<Statement> statements = Lists.newArrayList(globalStatements);
		final DicomObjectExporter exporter;
		int   count;

		if (!out.isAbsolute())
			throw new IllegalArgumentException("destination URI must be absolute");
        
		if ("file".equals(out.getScheme())) {
            final Set<File> roots = Sets.newLinkedHashSet();	// only directories can be roots
            for (final File file : files) {
                if (file.isDirectory()) {
                    roots.add(file);
                } else if (file.exists()) {
                    roots.add(file.getAbsoluteFile().getParentFile());
                }
            }
            exporter = new NewRootFileExporter(AE_TITLE, new File(out), roots);
            count = 0;
        } else if ("dicom".equals(out.getScheme())) {
            final String locAETitle = out.getUserInfo();
            final String destHost = out.getHost();
            final int destPort = -1 == out.getPort() ? DICOM_DEFAULT_PORT : out.getPort();
            final String destAETitle = out.getPath().replaceAll("/", "");
            final FileWalkIterator walker = new FileWalkIterator(files,
                    new StreamProgressMonitor(messages, "Searching", "original DICOM"));
            final TransferCapability[] tcs = TransferCapabilityExtractor.getTransferCapabilities(walker, TransferCapability.SCU);
            count = walker.getCount();
            exporter = new CStoreExporter(destHost, Integer.toString(destPort), false,
                    destAETitle, locAETitle, tcs);
        } else {
            throw new UnsupportedOperationException("no exporter defined for URI scheme " + out.getScheme());
        }

        final BatchExporter batch = new BatchExporter(exporter, statements, new FileWalkIterator(files, null));
        batch.setProgressMonitor(new StreamProgressMonitor(messages, "Processing", "modified DICOM", count), 0);
        batch.run();
        return batch.getFailures();
    }
	
	public void includeStatements(final InputStream in)
			      throws IOException, ScriptEvaluationException
	{
		try
		{
			final ScriptApplicator applicator = new ScriptApplicator(in);
			globalStatements.addAll(applicator.getStatements());
			
			// Note: A very large section of the original CSVRemapper code, concerning
			// input of DicomEdit variables from the command line, is missed out of
			// the modified code here. As a result, this version does not work with
			// scripts containing DicomEdit variables designed to be set interactively.
			
      }
		catch (IOException | ScriptEvaluationException  e)
		{
			logger.error(e.getMessage());
			throw e;
		}		
	}

}
