package ar.com.falberca.duplicate.finder.app.files.filters;

import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Convenient interface for defining filter associated to file attributes
 * 
 * @author fernando
 */
public interface FileConditionFilter {
	
	/**
	 * Evaluate if the file pass the filter criteria or not
	 * 
	 * @param fileInfo File reference
	 * @return Returns true if the criteria evaluation match the files properties, false otherwise
	 */
	boolean evaluate(FileInfo fileInfo);

}
