package ar.com.falberca.duplicate.finder.app.directory.walkers;

import java.io.IOException;

import ar.com.falberca.duplicate.finder.app.observer.ObservableSupport;

/**
 * An object that encapsulates the logic for traversing a file system.
 * Objects that depends on updates or events from this object can be added
 * as observers.
 * 
 * @author fernando
 */
public interface DirectoryWalker extends ObservableSupport {
	
	void walkDirectory() throws IOException;
	
}
