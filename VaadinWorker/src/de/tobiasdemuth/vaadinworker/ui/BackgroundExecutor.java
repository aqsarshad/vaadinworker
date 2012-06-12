package de.tobiasdemuth.vaadinworker.ui;

import java.util.concurrent.ExecutorService;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import de.tobiasdemuth.vaadinworker.ExecutorServiceProvider;
import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 * <p>
 * This class serves two purposes - on the one hand its the UI for displaying
 * towards the user that there are some tasks executed in the background. Part 
 * of this being the UI is also that this class does the more difficult type of
 * synchronization necessary in Vaadin - it keeps the client polling for updates
 * as long as needed. This ensures that changes done by a background-thread
 * actually become visible to the user.
 * </p>
 * 
 * <p>
 * The polling is done using the only core-Vaadin-solution to this problem: A
 * <code>ProgressIndicator</code>. Actually the UI displays several 
 * <code>ProgressIndicator</code>s - one for each running task plus an 
 * indeterminate one that keeps spinning until all tasks are done. In order to 
 * keep the UI clean, the task-specific <code>ProgressIndicator</code>s are 
 * placed in a <code>PopupView</code> and get only visible if the user clicks 
 * the "Loading ..."-link.
 * </p>
 * 
 * <p>
 * The second purpose of this class is to provide an interface for submitting
 * <code>VaadinWorker</code>s and ensuring that those get run in a separate
 * thread. A <code>VaadinWorker</code> can be handed out for getting worked on
 * by calling the method <code>submit(VaadinWorker)</code>.
 * </p>
 * 
 * <p>
 * Internally the <code>BackgroundExecutor</code> maintains a reference to an
 * <code>ExecutorService</code>. Most ApplicationServers provide some kind of 
 * <code>ExecutorService</code> via JNDI or other ways. Because of this and 
 * because <code>ExecutorService</code>s are normally not serializable, the 
 * retrieval of the <code>ExecutorService</code> is indirected to an
 * <code>ExecutorServiceProvider</code>. This simple interface is just 
 * responsible for getting an <code>ExecutorService</code>.
 * </p>
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class BackgroundExecutor extends CustomComponent {
	
	private static final long serialVersionUID = -2524675072804796156L;
	
	private static final String DEFAULT_CAPTION = "Loading ...";
	
	private static final TaskProgressViewFactory DEFAULT_TPV_FACTORY = 
			new DefaultTaskProgressViewFactory();
	
	private final ExecutorServiceProvider executorServiceProvider;
	
	private final String caption;
	
	private final VerticalLayout popupRoot;
	
	private final TaskProgressViewFactory tpvFactory;
	
	private boolean initialized = false;
	
	private int workItemCounter = 0;
	
	/**
	 * Creates a new <code>BackgroundExecutor</code> retrieving its 
	 * <code>ExecutorService</code> from the given <code>ExecutorServiceProvider</code>.
	 */
	public BackgroundExecutor(ExecutorServiceProvider executorServiceProvider) {
		this(executorServiceProvider, DEFAULT_CAPTION);
	}
	
	/**
	 * Creates a new <code>BackgroundExecutor</code> retrieving its 
	 * <code>ExecutorService</code> from the given <code>ExecutorServiceProvider</code>
	 * and displaying instead of the default "Loading ..."-text <code>caption</code>.
	 */
	public BackgroundExecutor(ExecutorServiceProvider executorServiceProvider, 
			String caption) {
		this(executorServiceProvider, caption, null);
	}
	
	/**
	 * Just as <code>BackgroundExecutor(ExecutorServiceProvider,String)</code>,
	 * but the progress-views for the individual tasks is retrieved by the
	 * overgiven <code>TaskProgressViewFactory</code>.
	 */
	public BackgroundExecutor(ExecutorServiceProvider executorServiceProvider, 
			String caption, TaskProgressViewFactory tpvFactory) {
		if(executorServiceProvider == null) {
			throw new NullPointerException("ExecutorServiceProvider is not allowed to be null!");
		}
		
		this.executorServiceProvider = executorServiceProvider;
		this.caption = (caption != null ? caption : DEFAULT_CAPTION);
		this.tpvFactory = (tpvFactory != null) ? tpvFactory : DEFAULT_TPV_FACTORY;
		
		this.popupRoot = new VerticalLayout();
		this.popupRoot.setSizeUndefined();
		this.popupRoot.setSpacing(true);
		
		setVisible(false);
	}
	
	/**
	 * Hands the overgiven <code>VaadinWorker</code> over to the 
	 * <code>ExecutorService</code> for background-processing.
	 * 
	 * @throws IllegalStateException if either the <code>VaadinWorker</code>
	 * synchronizes against another <code>Application</code> than the one, this
	 * component is added to or if the <code>ExecutorService</code> has been 
	 * told to shutdown itself. 
	 */
	public void submit(final VaadinWorker vaadinWorker) throws IllegalStateException {
		if(vaadinWorker.getApplication() != getApplication() || 
				vaadinWorker.getApplication() == null) {
			throw new IllegalStateException("The worker's application and my " +
					"Application must be the same!");
		}
		
		final ExecutorService executor = executorServiceProvider.
				getExecutorService(vaadinWorker.getApplication());
		if(executor.isShutdown() || executor.isTerminated()) {
			throw new IllegalStateException("Cannot handle tasks while " +
					"shutting down!");
		}
		
		final Component progressView = tpvFactory.createTaskProgressView(vaadinWorker);
		
		popupRoot.addComponent(progressView);
		executor.execute(new Runnable() {

			public void run() {
				try {
					vaadinWorker.run();
				}
				catch(Exception e) {
					throw new RuntimeException(e);
				}
				finally {
					synchronized(vaadinWorker.getApplication()) {
						workItemCounter--;
						
						// View-Updates
						popupRoot.removeComponent(progressView);
						if(workItemCounter <= 0) {
							setVisible(false);
						}
					}
				}
			}
			
		});
		
		synchronized(vaadinWorker.getApplication()) {
			workItemCounter++;
			if(workItemCounter > 0) {
				setVisible(true);
			}
		}
	}
	
	/**
	 * @return the used <code>TaskProgressViewFactory</code>
	 */
	public TaskProgressViewFactory getTaskProgressViewFactory() {
		return tpvFactory;
	}
	
	@Override
	public void attach() {
		if(initialized) {
			return; // Initialize only once
		}
		
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setIndeterminate(true);
		progressIndicator.setImmediate(true);
		progressIndicator.setPollingInterval(500);
		
		PopupView popup = new PopupView(new PopupView.Content() {

			private static final long serialVersionUID = -1332375457317966834L;

			public String getMinimizedValueAsHTML() {
				return caption;
			}

			public Component getPopupComponent() {
				return popupRoot;
			}
			
		});
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		layout.addComponent(progressIndicator);
		layout.addComponent(popup);
		
		setCompositionRoot(layout);
	}

}
