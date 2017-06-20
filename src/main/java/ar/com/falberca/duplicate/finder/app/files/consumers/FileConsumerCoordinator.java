package ar.com.falberca.duplicate.finder.app.files.consumers;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.falberca.duplicate.finder.app.events.EventType;
import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Coordinates the execution of multiples consumer functions using a thread pool of executors.
 * The implementation of the coordinator is independent of the consumer function. Using an instance of 
 * {@link ConsumerProviderFunctionFactory} a new instance of {@link ConsumerProviderFunction} will be
 * created and assigned to a specific thread for consuming file events.
 * 
 * @author fernando
 */
public class FileConsumerCoordinator extends Observable implements Observer {
	
	private final Logger logger = LoggerFactory.getLogger(FileConsumerCoordinator.class);
	
	private volatile boolean interruptExecution = false;
	private ConsumerProviderFunctionFactory consumerProviderFunctionFactory;
	private int numberOfThreads;
	private boolean consumingEvents = false;
	private ExecutorService executor;
	private BlockingQueue<FileInfo> fileQueue;
	
	/**
	 * Creates an instance of this coordinator using a shared queue for exchanging file events and a 
	 * consumer function factory for customizing the actions associated to each file.
	 * 
	 * @param fileQueueRef Blocking queue reference
	 * @param consumerProviderRef Consumer provider factory reference
	 * @param concurrency Number of threads that will be used for processing files
	 */
	public FileConsumerCoordinator(BlockingQueue<FileInfo> fileQueueRef, 
								   ConsumerProviderFunctionFactory consumerProviderRef, 
								   int concurrency) {
		consumerProviderFunctionFactory = consumerProviderRef;
		numberOfThreads = concurrency;
		fileQueue = fileQueueRef;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object eventType) {
		if (EventType.FINISHED.equals(eventType)) {
			logger.info("Consumer execution received 'task finished' message from observable");
			interruptExecution = true;
			waitForTermination();
		}
	}
	
	/**
	 * Starts the execution of the threads that will be polling for file events
	 */
	public void consume() {
		
		if (consumingEvents)
			throw new IllegalStateException("Consumer is already in execution state.");
		
		triggerEvent(EventType.STARTED);
		
		logger.info("Starting pulling files from file queue");
		
		consumingEvents = true;
		
		executor = Executors.newFixedThreadPool(numberOfThreads);
		
		IntStream.range(0, numberOfThreads).forEach(index -> {
			
			logger.debug("Submitting consumer task. Number: {}", index);
			
			Runnable task = () -> {
				
				ConsumerProviderFunction providerFuntion = consumerProviderFunctionFactory.createConsumerFunction();
				
				while (true) {
					try {
						FileInfo fileInfo = fileQueue.poll(1,TimeUnit.SECONDS);
						if (fileInfo != null)
							logger.debug("File consumed: {}", fileInfo.getPath());
							providerFuntion.accept(fileInfo);
						
						if (interruptExecution && fileQueue.isEmpty())
							break;
					}
					catch(InterruptedException ex) {
						//Do nothing, continues execution until interruptException flag is true 
					}
				}
			};
			
			executor.submit(task);
		});
	} 
	
	/*
	 * 
	 */
	private void waitForTermination() {
		
		logger.info("Waiting for consumers to finish...");
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		}
		catch(InterruptedException ex) {
			//No need to catch this error since Interrupted exception here means that
			//the execution was stopped from external sources
		}
		
		triggerEvent(EventType.FINISHED);
		
		logger.info("All consumers finished their tasks.");
	}
	
	private void triggerEvent(EventType eventType) {
		setChanged();
		notifyObservers(eventType);
	}
	
}
