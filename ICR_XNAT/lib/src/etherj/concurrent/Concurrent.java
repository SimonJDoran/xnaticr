/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.concurrent;

/**
 * $Id$
 *
 * @author adminjamesd
 */
public class Concurrent
{
	public static TaskMonitor getTaskMonitor()
	{
		return getTaskMonitor(true);
	}

	public static TaskMonitor getTaskMonitor(boolean cancellable)
	{
		return new DefaultTaskMonitor(cancellable);
	}

	public static TaskMonitor getTaskMonitor(boolean cancellable, boolean noOp)
	{
		if (noOp)
		{
			return new NullTaskMonitor();
		}
		return new DefaultTaskMonitor(cancellable);
	}
}
