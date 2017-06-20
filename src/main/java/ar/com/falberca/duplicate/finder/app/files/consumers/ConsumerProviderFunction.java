package ar.com.falberca.duplicate.finder.app.files.consumers;

import ar.com.falberca.duplicate.finder.app.files.FileInfo;

/**
 * Creates a consumer that will accept files for processing. 
 * 
 * @author fernando
 */
public interface ConsumerProviderFunction {
	
	void accept(FileInfo fileInfo);

}
