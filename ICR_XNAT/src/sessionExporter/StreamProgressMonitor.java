package sessionExporter;

/**
 * Copyright (c) 2008-2009 Washington University
 * Should be in package org.nrg.dcm.edit, but for some reason it exists in
 * DicomBrowser-1.5.2 but is not public??
 */

import java.io.PrintStream;

import org.nrg.dcm.ProgressMonitorI;
import org.nrg.util.EditProgressMonitor;

/**
 * Sends progress messages to a PrintStream.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 */
final class StreamProgressMonitor
implements ProgressMonitorI,EditProgressMonitor {
  private final static String LINE_SEPARATOR = System.getProperty("line.separator");
  private final PrintStream os;
  private final String desc, startNote;
  private String note;
  private int min = 0, max = 0, lastReport = 0;
  private int reportCutoff = 200, reportIntervalCoarse = 100, reportIntervalFine = 20;
  
  public StreamProgressMonitor(final PrintStream os, final String desc, final String startNote, final int max) {
    this.os = os;
    this.desc = desc;
    this.note = this.startNote = startNote;
    this.max = max;
   }
  
  public StreamProgressMonitor(final PrintStream os, final String desc, final String startNote) {
      this(os, desc, startNote, 0);
  }
  
  public void setReportIntervals(final int cutoff, final int coarse, final int fine) {
    this.reportCutoff = cutoff;
    this.reportIntervalCoarse = coarse;
    this.reportIntervalFine = fine;
  }
  
  public void setMinimum(final int min) {
    this.min = min;
  }
  
  public void setMaximum(final int max) {
    this.max = max;
  }
  
  public void setProgress(final int current) {
      final int range = max - min;
      final int reportInterval = (range > reportCutoff) ? reportIntervalCoarse : reportIntervalFine;
      if (current - lastReport >= reportInterval) {
	  final StringBuilder sb = new StringBuilder(note);
	  sb.append(" (").append(current-min).append("/");
	  sb.append(0 >= range ? "?" : range);
	  sb.append(")").append(LINE_SEPARATOR);
	  os.append(sb.toString());
	  lastReport = current;
      }
  }
  
  public void setNote(final String note) {
    this.note = note;
  }
  
  public void close() {
    os.format("%s %s (done)%s", desc, startNote, LINE_SEPARATOR);
  };
  
  public boolean isCanceled() { return false; };
}
