/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

import java.beans.PropertyChangeListener;

/**
 * $Id$
 *
 * @author adminjdarcy
 */
public interface TaskMonitor
{
	public static final String CANCELLED = "cancelled";
	public static final String DESCRIPTION = "description";
	public static final String INDETERMINATE = "indeterminate";
	public static final String MAXIMUM = "maximum";
	public static final String MINIMUM = "minimum";
	public static final String TITLE = "title";
	public static final String VALUE = "value";

	void addPropertyChangeListener(PropertyChangeListener pcl);

	String getDescription();

	int getMaximum();

	int getMinimum();

	String getTitle();

	int getValue();

	boolean isCancellable();

	boolean isCancelled();

	boolean isIndeterminate();

	void removePropertyChangeListener(PropertyChangeListener pcl);

	void setCancelled(boolean cancelled);

	void setDescription(String description);

	void setIndeterminate(boolean indeterminate);

	void setMaximum(int max);

	void setMinimum(int min);

	void setTitle(String title);

	void setValue(int value);
}
