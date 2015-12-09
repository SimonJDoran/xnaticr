/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import etherj.concurrent.Concurrent;
import etherj.concurrent.TaskMonitor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 * @param <T>
 */
public abstract class AbstractPathScan<T> implements PathScan<T>
{
	private static final Logger logger =
		LoggerFactory.getLogger(AbstractPathScan.class);
	private final List<PathScanContext<T>> contexts = new ArrayList<>();
	private volatile boolean stopScanning = false;

	@Override
	public boolean addContext(PathScanContext<T> context)
	{
		return this.contexts.add(context);
	}

	@Override
	public List<PathScanContext<T>> getContextList()
	{
		return Collections.unmodifiableList(contexts);
	}

	@Override
	public boolean removeContext(PathScanContext<T> context)
	{
		return contexts.remove(context);
	}

	@Override
	public void scan(String path) throws IOException
	{
		scan(path, true, null);
	}

	@Override
	public void scan(String path, boolean recurse) throws IOException
	{
		scan(path, recurse, null);
	}

	@Override
	public void scan(String path, TaskMonitor taskMonitor) throws IOException
	{
		scan(path, true, taskMonitor);
	}

	@Override
	public void scan(String path, boolean recurse, TaskMonitor taskMonitor)
		throws IOException
	{
		File searchRoot = new File(path);
		if (taskMonitor == null)
		{
			taskMonitor = Concurrent.getTaskMonitor(true);
		}

		// Bail out if it's not a directory or not readable
		if (!searchRoot.isDirectory() || !searchRoot.canRead())
		{
			logger.warn("Not a directory or cannot be read: {}",
				searchRoot.getPath());
			return;
		}

		stopScanning = false;
		taskMonitor.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName().equals(TaskMonitor.CANCELLED))
				{
					stopScanning = ((TaskMonitor) evt.getSource()).isCancelled();
				}
			}
		});
		taskMonitor.setIndeterminate(true);
		taskMonitor.setDescription("Building import list...");
		logger.info(taskMonitor.getDescription());
		SortedMap<String,List<File>> tree = new TreeMap<>();
		buildTree(tree, searchRoot, recurse, taskMonitor);
		FileCountTreeWalker fileWalker = new FileCountTreeWalker();
		walkTree(tree, fileWalker);
		taskMonitor.setIndeterminate(false);
		taskMonitor.setDescription("Scanning "+Integer.toString(fileWalker.getFileCount())+
			" files from "+searchRoot.getPath()+"...");
		taskMonitor.setMinimum(0);
		taskMonitor.setMaximum(fileWalker.getFileCount());
		taskMonitor.setValue(0);
		logger.info(taskMonitor.getDescription());
		ContextTreeWalker contextWalker = new ContextTreeWalker(contexts,
			taskMonitor);
		Iterator<PathScanContext<T>> iter = contexts.iterator();
		while (iter.hasNext())
		{
			iter.next().notifyScanStart();
		}
		walkTree(tree, contextWalker);
		iter = contexts.iterator();
		while (iter.hasNext())
		{
			iter.next().notifyScanFinish();
		}
		taskMonitor.setValue(taskMonitor.getMaximum());
		logger.info("Scan complete {}", searchRoot.getPath());
	}

	private void buildTree(SortedMap<String,List<File>> tree, File path,
		boolean recurse, TaskMonitor taskMonitor)
	{
		taskMonitor.setDescription("Listing directory "+path.getName()+"...");
		logger.debug("Listing directory {}...", path.getPath());
		List<File> fileList = new ArrayList<>();
		tree.put(path.getPath(), fileList);

		if (!path.canRead())
		{
			logger.warn("Directory cannot be read: {}", path.getPath());
			return;
		}
		File[] contents = path.listFiles();
		if (contents == null)
		{
			logger.debug("Directory is empty: {}", path.getPath());
			return;
		}
		for (int i=0; i<contents.length && !stopScanning; i++)
		{
			if (contents[i].isDirectory())
			{
				if (recurse)
				{
					buildTree(tree, contents[i], recurse, taskMonitor);
				}
				continue;
			}
			fileList.add(contents[i]);
		}
	}

	private void walkTree(SortedMap<String,List<File>> tree, TreeWalker walker)
		throws IOException
	{
		Set<String> keys = tree.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext() && !stopScanning)
		{
			String path = iter.next();
			walker.onDirectory(new File(path));
			List<File> pathContents = tree.get(path);
			Iterator<File> listIter = pathContents.iterator();
			while (listIter.hasNext() && !stopScanning)
			{
				File file = listIter.next();
				walker.onFile(file);
			}
		}
	}

		interface TreeWalker
	{
		public void onDirectory(File directory) throws IOException;

		public void onFile(File file) throws IOException;
	}

	/**
	 *
	 */
	private class FileCountTreeWalker implements TreeWalker
	{
		int nFiles = 0;
		int nDirs = 0;

		public int getDirectoryCount()
		{
			return nDirs;
		}

		public int getFileCount()
		{
			return nFiles;
		}

		@Override
		public void onDirectory(File directory) throws IOException
		{
			nDirs++;
		}

		@Override
		public void onFile(File file) throws IOException
		{
			nFiles++;
		}
	}

	/**
	 *
	 */
	private class ContextTreeWalker implements TreeWalker
	{
		private List<PathScanContext<T>> contexts = null;
		private TaskMonitor taskMonitor = null;

		ContextTreeWalker(List<PathScanContext<T>> contexts, TaskMonitor taskMonitor)
		{
			this.contexts = contexts;
			this.taskMonitor = taskMonitor;
		}

		@Override
		public void onDirectory(File directory) throws IOException
		{
			logger.trace("Scanning directory: {}", directory.getPath());
		}

		@Override
		public void onFile(File file) throws IOException
		{
			logger.trace("Scanning file: {}", file.getPath());
			T item = scanFile(file);
			if (item != null)
			{
				Iterator<PathScanContext<T>> iter = contexts.iterator();
				while (iter.hasNext())
				{
					iter.next().notifyItemFound(file, item);
				}
			}
			taskMonitor.setValue(taskMonitor.getValue()+1);
		}
	}
}
