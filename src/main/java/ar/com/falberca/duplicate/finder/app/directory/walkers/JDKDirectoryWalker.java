package ar.com.falberca.duplicate.finder.app.directory.walkers;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.falberca.duplicate.finder.app.events.EventType;
import ar.com.falberca.duplicate.finder.app.files.FileInfo;
import ar.com.falberca.duplicate.finder.app.files.filters.FileConditionFilter;
import ar.com.falberca.duplicate.finder.app.files.filters.FileConditionFilterBuilder;

/**
 * DirectoryWalker implementation that use JDK Files class for visiting files. This implementation
 * executes the file visitor in a separate thread without blocking the main thread.
 * 
 * A {@link BlockingQueue} is used for receiving the files that were found traversing the directory structure. You can
 * also apply filtering to the files that the visitor finds using {@link FileConditionFilter} classes or building a 
 * list of filters using {@link FileConditionFilterBuilder}
 * 
 * @author fernando
 */
public class JDKDirectoryWalker extends Observable implements DirectoryWalker {
	
	private final Logger logger = LoggerFactory.getLogger(JDKDirectoryWalker.class);
	
	private Path rootDirectoryPath;
	private BlockingQueue<FileInfo> fileQueue;
	private List<FileConditionFilter> filters;
	
	/**
	 * Creates an instance of this walker using <code>rootDirectory</code> as base directory and 
	 * <code>fileQueueRef</code> as a shared queue for others consumers. Just the files that match filters criteria 
	 * will be added to the queue.
	 * 
	 * @param rootDirectory Base directory for searching files
	 * @param fileQueueRef Shared blocking queue
	 * @param filtersRef List of filters to be applied
	 */
	public JDKDirectoryWalker(String rootDirectory, BlockingQueue<FileInfo> fileQueueRef, 
							  List<FileConditionFilter> filtersRef) {
		rootDirectoryPath = Paths.get(rootDirectory);
		fileQueue = fileQueueRef;
		filters = filtersRef;
	}
	
	/*
	 * Walks a full directory structure without following symlinks. Hardlinks are treated as regular files.
	 *   
	 * (non-Javadoc)
	 * @see ar.com.falberca.duplicate.finder.app.directory.walkers.DirectoryWalker#walkDirectory()
	 */
	@Override
	public void walkDirectory() {
		triggerEvent(EventType.STARTED);
		logger.info("Asynchronous file system walker task was started");
		Runnable task = () -> {
			try {
				Files.walkFileTree(rootDirectoryPath, 
								   Collections.<FileVisitOption>emptySet(), 
						           Integer.MAX_VALUE, new JDKFileVisitor());
			}
			catch(IOException ex) {
				logger.error("Error traversing the file tree:" + rootDirectoryPath, ex);
			}
			triggerEvent(EventType.FINISHED);
			logger.info("File system traversing task finished.");
		};
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(task);
		executor.shutdown();
	}

	/*
	 * Adds the file to the queue only if pass the filtering criteria
	 */
	private void addFileToQueue(FileInfo fileInfo) {
		try {
			logger.debug("File received: {}", fileInfo.getPath());
			if (filters.stream().allMatch(filter -> filter.evaluate(fileInfo))) {
				fileQueue.put(fileInfo);
				logger.debug("Queued file: {}", fileInfo.getPath());
			}
		}
		catch(InterruptedException ex) {
			throw new IllegalStateException("The thread was interrupted while trying to adding a file element to the"
					+ " queue",ex);
		}
	}
	
	/**
	 * Trigger an event of type {@link EventType}
	 * 
	 * @param eventType Type of event to be triggered
	 */
	private void triggerEvent(EventType eventType) {
		setChanged();
		notifyObservers(eventType);
	}

	/*
	 * Simple file visitor implementation for adding files to the queue or reporting
	 * error about them.
	 */
	private class JDKFileVisitor extends SimpleFileVisitor<Path> {
		
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			JDKDirectoryWalker.this.addFileToQueue(new FileInfo(file, attr));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			logger.error("Error visiting file " + file.toString(), exc);
			return FileVisitResult.CONTINUE;
		}
		
		
		
	}
}
