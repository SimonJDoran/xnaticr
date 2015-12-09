/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * $Id$
 *
 * @author adminjdarcy
 */
class DefaultTaskMonitor implements Serializable, TaskMonitor
{
	private String title = "Title";
	private String description = "Description";
	private int minimum = 0;
	private int maximum = 100;
	private int value = 0;
	private volatile boolean indeterminate = false;
	private volatile boolean cancelled = false;
	private boolean cancellable = true;

	private PropertyChangeSupport pcs = null;

	DefaultTaskMonitor()
	{
		this(true);
	}

	DefaultTaskMonitor(boolean cancellable)
	{
		this.cancellable = cancellable;
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl)
	{
		pcs.addPropertyChangeListener(pcl);
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int getMaximum()
	{
		return maximum;
	}

	@Override
	public int getMinimum()
	{
		return minimum;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public int getValue()
	{
		return value;
	}

	@Override
	public boolean isCancellable()
	{
		return cancellable;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public boolean isIndeterminate()
	{
		return indeterminate;
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl)
	{
		pcs.removePropertyChangeListener(pcl);
	}

	@Override
	public void setIndeterminate(boolean indeterminate)
	{
		boolean oldIndeterminate = this.indeterminate;
		this.indeterminate = indeterminate;
		pcs.firePropertyChange(INDETERMINATE, oldIndeterminate, indeterminate);
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		if (!cancellable)
		{
			return;
		}
		boolean oldCancelled = this.cancelled;
		this.cancelled = cancelled;
		pcs.firePropertyChange(CANCELLED, oldCancelled, cancelled);
	}

	@Override
	public void setDescription(String description)
	{
		String oldDescription = this.description;
		this.description = description;
		pcs.firePropertyChange(DESCRIPTION, oldDescription, description);
	}

	@Override
	public void setMaximum(int max)
	{
		if (minimum > max)
		{
			setMinimum(max);
		}
		if (value > max)
		{
			setValue(max);
		}

		int oldMax = maximum;
		maximum = max;
		pcs.firePropertyChange(MAXIMUM, oldMax, max);
	}

	@Override
	public void setMinimum(int min)
	{
		if (min > maximum)
		{
			setMaximum(min);
		}
		if (min > value)
		{
			setValue(min);
		}

		int oldMin = minimum;
		minimum = min;
		pcs.firePropertyChange(MINIMUM, oldMin, minimum);
	}

	@Override
	public void setTitle(String title)
	{
		String oldTitle = this.title;
		this.title = title;
		pcs.firePropertyChange(TITLE, oldTitle, title);
	}

	@Override
	public void setValue(int value)
	{
		value = (value > maximum) ? maximum : value;
		value = (value < minimum) ? minimum : value;

		int oldValue = this.value;
		this.value = value;
		pcs.firePropertyChange(VALUE, oldValue, value);
	}
}
