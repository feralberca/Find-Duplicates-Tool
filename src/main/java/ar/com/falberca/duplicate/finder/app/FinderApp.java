package ar.com.falberca.duplicate.finder.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.falberca.duplicate.finder.app.directory.walkers.DirectoryWalker;
import ar.com.falberca.duplicate.finder.app.directory.walkers.JDKDirectoryWalker;
import ar.com.falberca.duplicate.finder.app.events.EventType;
import ar.com.falberca.duplicate.finder.app.files.FileInfo;
import ar.com.falberca.duplicate.finder.app.files.consumers.ConsumerProviderFunctionFactory;
import ar.com.falberca.duplicate.finder.app.files.consumers.DuplicateConsumerProviderFactory;
import ar.com.falberca.duplicate.finder.app.files.consumers.FileConsumerCoordinator;
import ar.com.falberca.duplicate.finder.app.files.filters.FileConditionFilterBuilder;

/**
 * Application main class, will be responsible for managing the directory walker and file consumer.
 * 
 * @author fernando
 *
 */
public class FinderApp implements Observer {
	
	private static final String ROOT_DIR_PARAM = "rootDir";
	private static final String PARALLEL_PARAM = "parallel";
	private static final String SKIP_LINKS = "skipLinks";
	private static final String SKIP_EMPTY_FILES = "skipEmpty";
	
	private CountDownLatch countDown = new CountDownLatch(1);
	private final Logger logger = LoggerFactory.getLogger(FinderApp.class);

	public static void main(String[] args) throws Exception {
		new FinderApp().execute(args);
	}
	
	/**
	 * Initiates the execution using the command line parameters.
	 * 
	 * @param args List of arguments in the form: -paramName paramValue
	 * 
	 * @throws Exception Throws {@link IllegalArgumentException} if there are problems parsing the command line 
	 * options.
	 */
	protected void execute(String[] args) throws Exception{
		CommandLineParser parser = new DefaultParser();
		Options commandLineOptions = createCLIParameters();
		
		if (args.length == 0) {
			printUsage(commandLineOptions);
			throw new IllegalArgumentException();
		}
		
		CommandLine line = null;
		try {
			line = parser.parse( commandLineOptions, args);
		}
		catch(ParseException pEx) {
			printUsage(commandLineOptions);
			throw new IllegalArgumentException(pEx);
		}
		
		String rootDirectory = line.getOptionValue(ROOT_DIR_PARAM);
		Number numberOfThreads = (Number) line.getParsedOptionValue(PARALLEL_PARAM);
		boolean skipLinks = line.hasOption(SKIP_LINKS);
		boolean skipEmptyFiles = line.hasOption(SKIP_EMPTY_FILES);
		
		Supplier<String> messageSupplier = () -> {
			printUsage(commandLineOptions);
			return "Missing required parameter.";
		};

		Objects.requireNonNull(rootDirectory, messageSupplier);
		Objects.requireNonNull(numberOfThreads, messageSupplier);
		
		processFileSystem(rootDirectory, numberOfThreads.intValue(), skipLinks, skipEmptyFiles);		
	}
	
	/**
	 * Initiates the processing of the given <code>rootDirecotry</code> directory using 
	 * <code>numberOfThreads</code>. Additionally, symlinks and empty files can be skipped setting
	 * <code>skipLinks</code> and <code>skipEmptyFiles</code> options.
	 *   
	 * @param rootDirectory Base directory for searching duplicates
	 * @param numberOfThreads Number of threads for processing files
	 * @param skipLinks Set to true for skipping symbolic links
	 * @param skipEmptyFiles Set to true for skipping empty files
	 * 
	 * @throws Exception Throws {@link IOException} if there are problems reading the directory or 
	 * {@link InterruptedException} if the main thread execution is interrupted.
	 */
	protected void processFileSystem(String rootDirectory, int numberOfThreads, boolean skipLinks, 
									 boolean skipEmptyFiles) throws Exception {
		
		long startTime = System.nanoTime();

		checkArgumentValues(rootDirectory, numberOfThreads);
		
		logger.info("Executing search of duplicate file under directory: {} with number of threads: {}", 
					rootDirectory, 
					numberOfThreads);
		
		//Shared queue between the producer (directory walker) and the consumer (file coordinator)
		BlockingQueue<FileInfo> fileQueue = new LinkedBlockingQueue<>();

		ConsumerProviderFunctionFactory consumerProviderFunctionFactory = new DuplicateConsumerProviderFactory();
		
		FileConsumerCoordinator fileConsumerCoordinator = new FileConsumerCoordinator(fileQueue, 
																					  consumerProviderFunctionFactory, 
																					  numberOfThreads);
		fileConsumerCoordinator.addObserver(this);
		fileConsumerCoordinator.consume();

		DirectoryWalker dirWalker = new JDKDirectoryWalker(rootDirectory, 
														   fileQueue, 
														   FileConditionFilterBuilder.create()
														   							 .links(skipLinks)
														   							 .emptyFiles(skipEmptyFiles)
														   							 .build());
		dirWalker.addObserver(fileConsumerCoordinator);
		dirWalker.walkDirectory();
		
		logger.info("Waiting for results...");
		
		countDown.await();
		
		long estimatedTime = System.nanoTime() - startTime;
		
		logger.info("Operation done. Time elapsed: {} seconds", 
					TimeUnit.SECONDS.convert(estimatedTime, 
					TimeUnit.NANOSECONDS));
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object eventType) {
		if (EventType.FINISHED.equals(eventType)) {
			countDown.countDown();
		}
	}
	
	private void checkArgumentValues(String rootDirectory, int numberOfThreads) {
		
		File rootDirectoryFile = new File(rootDirectory);
		if (!rootDirectoryFile.exists() || !rootDirectoryFile.isDirectory())
			throw new IllegalArgumentException("Directory " + rootDirectory + " do not exist or is not a directory");
		
		if (numberOfThreads < 1)
			throw new IllegalArgumentException("Invalid number of threads: " + numberOfThreads);
	}
	
	/*
	 * Create a list of the command lines parameter options
	 */
	private Options createCLIParameters() {
		Options options = new Options();
		options.addOption( Option.builder(ROOT_DIR_PARAM)
								 .desc("Root Directory for searching duplicates.")
								 .hasArg(true)
								 .build());		
		
		options.addOption( Option.builder(PARALLEL_PARAM)
								 .desc("Number of threads for searching duplicates.")
								 .hasArg(true)
								 .type(Number.class)
								 .build());
		
		options.addOption( Option.builder(SKIP_LINKS)
								 .desc("Skip soft and hark links.")
								 .hasArg(false)
								 .build());		
		
		options.addOption( Option.builder(SKIP_EMPTY_FILES)
								 .desc("Skip empty files")
								 .hasArg(false)
								 .build());			
		
		return options;
	}
	
	/*
	 * Prints the command line usage
	 */
	private void printUsage(final Options options) {
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter usageFormatter = new HelpFormatter();
		usageFormatter.printUsage(writer, 100, "java -jar <jar location>", options);
		writer.close();
	}

}
