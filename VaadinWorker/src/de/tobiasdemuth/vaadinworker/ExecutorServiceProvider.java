package de.tobiasdemuth.vaadinworker;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

import com.vaadin.Application;

/**
 * Implementors of this interface are responsible to get an 
 * <code>ExecutorService</code> for the overgiven application. They might pull 
 * it from JNDI or elsewhere.
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public interface ExecutorServiceProvider extends Serializable {

	public ExecutorService getExecutorService(Application application);

}
