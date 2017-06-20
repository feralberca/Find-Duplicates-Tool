package ar.com.falberca.duplicate.finder.app.files.filters;

import java.util.LinkedList;
import java.util.List;

/**
 * Creates a list of filters based on information consumed using a builder pattern.
 * 
 * @author fernando
 */
public class FileConditionFilterBuilder {
	
	private List<FileConditionFilter> filters = new LinkedList<>();
	
	/**
	 * Creates an instance of this builder
	 * @return Returns a new instance of the builder for continuing filters configuration
	 */
	public static FileConditionFilterBuilder create() {
		return new FileConditionFilterBuilder();
	}
	
	/**
	 * Indicates if symlinks should be processed or not depending on <code>skipLinks</code> flag
	 * @param skipLinks Flag that indicates that symlinks must be skipped or not
	 * @return Returns a reference to the current builder
	 */
	public FileConditionFilterBuilder links(boolean skipLinks) {
		if (skipLinks) {
			filters.add(new SymlinksFilter());
		}
		return this;
	}
	
	/**
	 * Indicates if empty files should be processed or not depending on <code>skipEmptyFiles</code> flag
	 * @param skipEmptyFiles Flag that indicates that empty files must be skipped or not
	 * @return Returns a reference to the current builder
	 */	
	public FileConditionFilterBuilder emptyFiles(boolean skipEmptyFiles) {
		if (skipEmptyFiles) {
			filters.add(new EmptyFilesFilter());
		}
		return this;
	}
	
	/**
	 * Creates a list of filters based on the configuration given to this builder instance
	 * @return Returns a list of the established filters
	 */
	public List<FileConditionFilter> build() {
		return filters;
	}

}
