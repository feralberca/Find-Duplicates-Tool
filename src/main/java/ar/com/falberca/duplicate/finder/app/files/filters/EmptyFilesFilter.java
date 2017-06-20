package ar.com.falberca.duplicate.finder.app.files.filters;

import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Evaluates if a file is empty or not based on its size.
 * 
 * @author fernando
 */
public class EmptyFilesFilter implements FileConditionFilter {

	/*
	 * (non-Javadoc)
	 * @see ar.com.falberca.duplicate.finder.app.files.filters.FileConditionFilter#evaluate(FileInfo)
	 */
	@Override
	public boolean evaluate(FileInfo fileInfo) {
		return fileInfo.getSize() > 0;
	}

}
