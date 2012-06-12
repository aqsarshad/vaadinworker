package de.tobiasdemuth.vaadinworker.executorserviceprovider;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

import de.tobiasdemuth.vaadinworker.ExecutorServiceProvider;

/**
 * This <code>ExecutorServiceProvider</code> retrieves the application-scoped
 * (application as in WebApplication, not as in Vaadin-application) 
 * <code>ExecutorService</code>, that was set as an attribute to the 
 * <code>ServletContext</code> by the <code>ExecutorServiceContextListener</code>.
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public final class ContextExecutorServiceProvider implements
		ExecutorServiceProvider {

	private static final long serialVersionUID = -814102205800131601L;

	public ExecutorService getExecutorService(Application application) {
		WebApplicationContext webAppCtx = (WebApplicationContext) application.getContext();
		ServletContext servletContext = webAppCtx.getHttpSession().getServletContext();
		
		return (ExecutorService) servletContext.getAttribute(
				ExecutorServiceContextListener.EXECUTOR_SERVICE_CTX_PARAM);
	}

}
