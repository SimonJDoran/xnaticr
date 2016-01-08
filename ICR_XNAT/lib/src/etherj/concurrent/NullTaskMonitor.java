/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

import java.beans.PropertyChangeListener;

/**
 * $Id: NullTaskMonitor.java 75 2012-01-09 13:04:28Z james $
 *
 * A no-op implementation of the <code>TaskMonitor</code> interface to provide
 * a minimal overhead fall back class when no full implementation is needed
 *
 * @author James d'Arcy
 */
class NullTaskMonitor implements TaskMonitor
{
	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl)
	{}

	@Override
	public String getDescription()
	{
		return "Null";
	}

	@Override
	public int getMaximum()
	{
		return 0;
	}

	@Override
	public int getMinimum()
	{
		return 0;
	}

	@Override
	public String getTitle()
	{
		return "Null";
	}

	@Override
	public int getValue()
	{
		return 0;
	}

	@Override
	public boolean isCancellable()
	{
		return false;
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public boolean isIndeterminate()
	{
		return false;
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl)
	{}

	@Override
	public void setCancelled(boolean cancelled)
	{}

	@Override
	public void setDescription(String description)
	{}

	@Override
	public void setIndeterminate(boolean indeterminate)
	{}

	@Override
	public void setMaximum(int max)
	{}

	@Override
	public void setMinimum(int min)
	{}

	@Override
	public void setTitle(String title)
	{}

	@Override
	public void setValue(int value)
	{}
}
