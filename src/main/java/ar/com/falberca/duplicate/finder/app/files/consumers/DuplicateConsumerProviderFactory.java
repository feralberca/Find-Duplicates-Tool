package ar.com.falberca.duplicate.finder.app.files.consumers;

import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Factory class of {@link ConsumerProviderFunction}. Every consumer created by this factory will share
 * state for holding information about processed files. Each consumer instance can be shared or executed
 * in a multi-threaded environment since access to shared data structures is synchronized.
 * 
 * For detecting duplicate files the following rules are applied:
 * <ul>
 *     <li> Empty files are processed by name, meaning that if the file is empty but has the same name that 
 * 		    other empty file it will treated as duplicate </li>
 *     <li> Files are compared by content regardless of the file name, unless the file is empty.</li>
 *     <li> Hardlinks pointing to the same location will treated as duplicates</li>
 *     <li> Invalid symlinks are skipped</li>
 *     <li> The first file occurrence will be logged not all of them. Meaning that if the file is duplicated 
 *          N times just the first duplication detected will be logged.</li>
 * </ul>
 * 
 * MD5 hashing is used for comparing files by content. If lower probability of collision is required a stronger
 * hashing mechanism can by used such as SHA-256 or SHA-512  
 * 
 * @author fernando
 */
public class DuplicateConsumerProviderFactory implements ConsumerProviderFunctionFactory {
	
	private final Logger logger = LoggerFactory.getLogger(DuplicateConsumerProviderFactory.class);
	
	private Map<String, String> regularHashedFilesMap = new HashMap<>();
	private Map<String, String> emptyFilesMap = new HashMap<>();
	private Map<String, String> softLinksMap = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * @see ar.com.falberca.duplicate.finder.app.files.consumers.ConsumerProviderFunctionFactory#createConsumerFunction()
	 */
	@Override
	public ConsumerProviderFunction createConsumerFunction() {
		return new DuplicateConsumerFunction();
	}
	
	/*
	 * Consumer implementation for detecting duplicates among processed files. The duplicate consumer function
	 * distinguish between regular files, empty files and symlinks. The rules that are applied to the files are 
	 * described at the top of the class. 
	 */
	private class DuplicateConsumerFunction implements ConsumerProviderFunction {

		/*
		 * (non-Javadoc)
		 * @see ar.com.falberca.duplicate.finder.app.files.consumers.ConsumerProviderFunction#accept(FileInfo)
		 */
		@Override
		public void accept(FileInfo fileInfo) {
			try {
				if (fileInfo.isLink()) {
					processLink(fileInfo);
				}
				else if (fileInfo.isEmpty()) {
					processEmptyFile(fileInfo);
				}
				else {
					processRegularFile(fileInfo);
				}
			}
			catch(Exception ex) {
				logger.error("Error ocurred while processing file:" + fileInfo.getPath(), ex.getMessage());
			}
		}
		
		/*
		 * Process a regular file calculating the hashing and searching for similar entries in the global map
		 */
		private void processRegularFile(FileInfo fileInfo) throws Exception {
			String hash = calculateHash(fileInfo);
			
			processFileKey("regular file", hash, fileInfo, regularHashedFilesMap);
		}
		
		/*
		 * Process a symlink using the target path for detecting duplication searching for similar target path entries 
		 * in the global map
		 */
		private void processLink(FileInfo fileInfo) {
			
			if (fileInfo.getRealPath() == null) {
				logger.info("Broken symlink detected: {}", fileInfo.getPath());
				return;
			}
			
			processFileKey("symbolic link", fileInfo.getRealPath(), fileInfo, softLinksMap);
		}
		
		/*
		 * Process an empty file by name searching for files that are also empty with the same name in the global map
		 */
		private void processEmptyFile(FileInfo fileInfo) {
			processFileKey("empty file", fileInfo.getName(), fileInfo, emptyFilesMap);			
		}
		
		/*
		 * Generic algorithm for searching an specific key in the map. Uses synchronization for getting map entries
		 * and adding elements to the map. Also if similar entries exists on the map will be reported as duplicate.
		 */
		private void processFileKey(String type, String key, FileInfo fileInfo, Map<String, String> map) {
			
			String processedFilePath;
			
			synchronized(map) {
				 processedFilePath = map.get(key);
				 if (processedFilePath == null) {
					 map.put(key, fileInfo.getPath());
				 }
			}
			
			if (processedFilePath != null) {
				reportDuplicatedFile(type, processedFilePath, fileInfo);
			}			
		}
		
		/*
		 * Log the duplicate file using a logger
		 */
		private void reportDuplicatedFile(String type, String filePath, FileInfo duplicatedFileInfo) {
			logger.error("Duplicate {} found: {}  with: {}", type, filePath, duplicatedFileInfo.getPath());
		}
		
		/*
		 * Calculate the hash of a file using MD5 and a 4MB file buffer stream
		 */
		private String calculateHash(FileInfo fileInfo) throws Exception {
			MessageDigest shaDigest = MessageDigest.getInstance("md5");
			try (DigestInputStream digestStream = 
					new DigestInputStream(new FileInputStream(fileInfo.getPath()), shaDigest)) {
				
				//4MB buffer size, depends on disk IO
				byte[] buffer = new byte[4000000]; 
				while (digestStream.available() > 0) {
					digestStream.read(buffer);
				}
			}
			return new String(shaDigest.digest());
		}
		
	}
}
