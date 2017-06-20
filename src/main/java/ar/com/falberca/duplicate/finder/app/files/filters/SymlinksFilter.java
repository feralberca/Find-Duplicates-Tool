package ar.com.falberca.duplicate.finder.app.files.filters;

import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Evaluates if a file is a symbolic link or not
 * 
 * @author fernando
 */
public class SymlinksFilter implements FileConditionFilter {

	/*
	 * (non-Javadoc)
	 * @see ar.com.falberca.duplicate.finder.app.files.filters.FileConditionFilter#evaluate(FileInfo)
	 */
	@Override
	public boolean evaluate(FileInfo fileInfo) {
		return !fileInfo.isLink();
	}

}
