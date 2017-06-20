package ar.com.falberca.duplicate.finder.app.observer;

import java.util.Observer;

/**
 * Helper interface for defining observable methods on classes that extends from 
 * the observable concrete class.
 * 
 * @author fernando
 *
 */
public interface ObservableSupport {
	
	void addObserver(Observer o);
	
	void deleteObserver(Observer o);
	
	void notifyObservers();
	
	void notifyObservers(Object arg);

}
