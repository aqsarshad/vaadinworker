package de.tobiasdemuth.vaadinworker.executorserviceprovider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <p>
 * This <code>ServletContextListener</code> will create an <code>ExecutorService</code>
 * which will be accessible as context-parameter afterwards. The 
 * <code>ContextExecutorServiceProvider</code> from the VaadinWorker.jar will 
 * access this <code>ExecutorService</code> in order to enable easy background-
 * execution of tasks.
 * </p>
 * 
 * <p>
 * Add the following snippet to your web.xml and replace the defaults of the 
 * initialization-parameters with appropriate values for your application:
 * </p>
 * 
 * <code>
 * <pre>
 * <context-param>
 *   <param-name>threadCount</param-name>
 *   <param-value>20</param-value>
 * </context-param>
 * <context-param>
 *   <param-name>gracefulShutdown</param-name>
 *   <param-value>false</param-value>
 * </context-param>
 * 
 * <listener>
 *   <listener-class>de.tobiasdemuth.vaadinworker.executorserviceprovider.ExecutorServiceContextListener</listener-class>
 * </listener>
 * </pre>
 * </code>
 * 
 * <p>
 * The threads created for the <code>ExecutorService</code> are all daemons in 
 * order to make a server-shutdown possible even if there were errors that 
 * prevent a thread from finishing its work.
 * </p>
 * 
 * <p>
 * You can control the number of used threads using the initialization-parameter
 * <code>threadCount</code>. If it is not set or an invalid value is used, only 
 * one thread will be created by the <code>ExecutorService</code>.
 * </p>
 * 
 * <p>
 * The shutdown-policy of the <code>ExecutorService</code> is controlled by the 
 * parameter <code>gracefulShutdown</code>. If set to true, the 
 * <code>ExecutorService</code> will be stopped by calling <code>shutdown()</code>. 
 * This allows any already started task to finish its work before the 
 * <code>ExecutorService</code> will stop. Therefore the server- / application-
 * shutdown will be delayed until all tasks have finished.
 * </p>
 * 
 * <p>
 * The default-strategy is to just call <code>shutdownNow()</code>, which will 
 * try to stop / interrupt any currently running tasks and results in a normally 
 * much quicker shutdown of the <code>ExecutorService</code>.
 * </p>
 * 
 * <p>
 * Credits for the idea and even large parts of the implementation go to
 * StackOverflow-user "nos" for his excellent answer in this 
 * <a href="http://stackoverflow.com/questions/4907502/
 * running-a-background-java-program-in-tomcat">thread</a>.
 * </p>
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class ExecutorServiceContextListener implements ServletContextListener {
	
	private static class DaemonThreadFactory implements ThreadFactory {

		private final ThreadFactory factory;

		public DaemonThreadFactory() {
			this(Executors.defaultThreadFactory());
		}

		public DaemonThreadFactory(ThreadFactory factory) {
			if (factory == null)
				throw new NullPointerException("factory cannot be null");
			this.factory = factory;
		}

		public Thread newThread(Runnable r) {
			final Thread t = factory.newThread(r);
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * The name of the "threadCount"-initialization-parameter.
	 */
	public static final String THREAD_COUNT_INIT_PARAM = "threadCount";
	
	/**
	 * The name of the "gracefulShutdown"-initialization-parameter.
	 */
	public static final String GRACEFUL_SHUTDOWN_INIT_PARAM = "gracefulShutdown";
	
	/**
	 * The ExecutorService will be accessible under this name.
	 */
	public static final String EXECUTOR_SERVICE_CTX_PARAM = "VAADINWORKER_EXECUTOR_SERVICE";
	
	private ExecutorService executor;
	
	private boolean gracefulShutdown;

	/**
	 * Creates and initializes an ExecutorService that utilizes as much threads
	 * as configured using the "threadCount"-parameter in web.xml.
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {
		final ServletContext context = contextEvent.getServletContext();
		final ThreadFactory daemonFactory = new DaemonThreadFactory();
		
		int threadCount = 1;
		try {
			String tcInitParam = context.getInitParameter(THREAD_COUNT_INIT_PARAM);
			threadCount = Integer.parseInt(tcInitParam);
		} catch (NumberFormatException ignore) {
		}
		
		String gsInitParam = context.getInitParameter(GRACEFUL_SHUTDOWN_INIT_PARAM);
		gracefulShutdown = Boolean.valueOf(gsInitParam);

		if (threadCount <= 1) {
			executor = Executors.newSingleThreadExecutor(daemonFactory);
		} else {
			executor = Executors.newFixedThreadPool(threadCount, daemonFactory);
		}
		
		context.setAttribute(EXECUTOR_SERVICE_CTX_PARAM, executor);
	}
	
	/**
	 * Shuts down the ExecutorService. If "gracefulShutdown" is set to true, all
	 * tasks that have been submitted prior to the shutdown will be finished.
	 * This might delay the stopping of the application (and therefore of the
	 * server).
	 * If the parameter is set to false (default) the ExecutorService will try 
	 * to stop all running tasks immediately.
	 */
	public void contextDestroyed(ServletContextEvent contextEvent) {
		if(gracefulShutdown) {
			executor.shutdown();
		}
		else {
			executor.shutdownNow();
		}
	}

}
