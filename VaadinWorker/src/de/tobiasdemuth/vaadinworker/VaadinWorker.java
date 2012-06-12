package de.tobiasdemuth.vaadinworker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.vaadin.Application;

/**
 * This class offers an easy way for encapsulating background-tasks. The long-
 * running task must be done in <code>runInBackground()</code>, while resulting
 * updates of the UI should be done in <code>updateGUI()</code>. This separation
 * is necessary in order to allow parallel updates of the UI from other threads.
 * 
 * For intermediate UI-updates you can register an <code>ProgressListener</code>,
 * which will always be triggered when <code>setProgress(float)</code> is called.
 * Any <code>ProgressListener</code> will be called while the lock off the
 * <code>Application</code> is held by this thread. Therefore it is absolutely 
 * safe to do arbitrary UI-updates in those Listeners. Note that no other threads
 * might do any UI-updates at the same time, as they cannot get the lock. This 
 * might bring for example other <code>VaadinWorker</code>s to halt, resulting
 * in less parallelism as you wanted it to be. To prevent this from happening it
 * is important that all actions in a <code>ProgressListener</code> (and also in
 * <code>updateGUI()</code>) are finished quickly.
 * 
 * The Vaadin-Worker implements <code>Runnable</code>, so it's instances can be
 * easily overgiven to any thread.
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public abstract class VaadinWorker implements Runnable {
	
	public static interface ProgressListener {
		
		/**
		 * This method gets called when the progress-attribute gets updated.
		 * The call happens with the lock of the Application-instance held, so
		 * it is absolutely legal to update the UI from here. Note that any 
		 * ProgressListener must return as quick as possible in order to prevent
		 * the UI from freezing.
		 * 
		 * @param progress a value between 0 and 100, representing the made 
		 * progress in percent.
		 * @param state a description for what is currently getting done.
		 * @param worker the progressed VaadinWorker-instance.
		 */
		public void workProgressed(int progress, String state, VaadinWorker worker);
		
	}
	
	public final static int INDETERMINATE = -1;
	
	public final static int MAX = 100;
	
	private final Set<ProgressListener> progressListeners; 
	
	private volatile boolean canceled = false;
	
	private volatile boolean finished = false;
	
	private boolean indeterminate;
	
	private boolean cancelable;
	
	private String state;
	
	private final Application app;
	
	/**
	 * @param app the current Application-object.
	 * @throws NullPointerException if <code>app</code> is null.
	 */
	public VaadinWorker(Application app) {
		this(app, (Collection<ProgressListener>) null);
	}
	
	/**
	 * @param app the current Application-object.
	 * @param listeners the <code>ProgressListener</code>s to register.
	 * @throws NullPointerException if <code>app</code> is null.
	 */
	public VaadinWorker(Application app, ProgressListener ... listeners) {
		this(app, Arrays.asList(listeners));
	}
	
	/**
	 * @param app the current Application-object.
	 * @param listeners the <code>ProgressListener</code>s to register, might 
	 * be null
	 * @throws NullPointerException if <code>app</code> is null.
	 */
	public VaadinWorker(Application app, Collection<ProgressListener> listeners) {
		if(app == null) {
			throw new NullPointerException("Application is not allowed to be null!");
		}
		this.app = app;
		this.cancelable = true;
		this.indeterminate = false;
		this.state = "";
		
		if(listeners != null && !listeners.isEmpty()) {
			this.progressListeners = new CopyOnWriteArraySet<ProgressListener>(listeners);
		}
		else {
			this.progressListeners = new CopyOnWriteArraySet<ProgressListener>();
		}
	}
	
	/**
	 * Adds a ProgressListener. Nothing happens if the listener is <code>null</code>
	 * or is already registered.
	 */
	public void addListener(ProgressListener l) {
		if(l != null) {
			this.progressListeners.add(l);
		}
	}
	
	/**
	 * Removes a ProgressListener. Nothing happens if the listener is 
	 * <code>null</code> or is not known.
	 */
	public void removeListener(ProgressListener l) {
		if(l != null) {
			this.progressListeners.remove(l);
		}
	}
	
	/**
	 * Controls whether the task can be canceled by the user or not.
	 */
	protected final void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	/**
	 * Returns true if the user can cancel this task manually.
	 */
	public final boolean isCancelable() {
		return this.cancelable;
	}
	
	/**
	 * Controls whether you can tell how long the task will run or not.
	 */
	protected final void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}
	
	/**
	 * Returns true if you cannot tell in advance how long the task will 
	 * probably take to execute.
	 */
	public final boolean isIndeterminate() {
		return this.indeterminate;
	}
	
	/**
	 * Requests Cancellation of this VaadinWorker. All subclasses need to check 
	 * for cancel-requests on a regular basis for this to have any effect.
	 * 
	 * @throws IllegalStateException if the task is not cancelable.
	 */
	public final void cancel() throws IllegalStateException {
		if(!isCancelable()) {
			throw new IllegalStateException("Cannot cancel this task!");
		}
		canceled = true;
	}
	
	/**
	 * Returns true if cancellation of this VaadinWorker has been requested.
	 * This must not mean that the work has effectively stopped already. Use
	 * <code>isFinished()</code> for checking that.
	 */
	public final boolean isCanceled() {
		return canceled;
	}
	
	/**
	 * Returns true if the work has effectively stopped, either due to a cancel-
	 * request by the user or because there is no more work to do.
	 */
	public final boolean isFinished() {
		return finished;
	}
	
	/**
	 * @return the Application-instance which was used during initialization.
	 */
	public final Application getApplication() {
		return app;
	}
	
	public final void run() {
		try {
			runInBackground();
			synchronized(app) {
				updateUI();
			}
			finished = true;
		}
		catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * Do your long-running tasks that should run in a background-thread here.
	 */
	public abstract void runInBackground();
	
	/**
	 * Publish your result to the UI using this method. It will be called after
	 * <code>doInBackground()</code> finishes.
	 */
	public abstract void updateUI();
	
	/**
	 * Call this method to communicate your progress. It informs all registered
	 * <code>ProgressListener<//code>s and lets them do their work guarded by 
	 * the <code>Application</code>-lock. Note that any task done by a 
	 * <code>ProgressListener</code> must finish quickly in order to not freeze
	 * the UI.
	 * 
	 * @param progress a value between 0 and 100, mapping to 0% to 100%.
	 * If you do not know how long the task will take, an negative value should 
	 * be overgiven. The param will be ignored when <code>isProgressIndeterminate()</code>
	 * returns true. In this case always <code>INDETERMINATE</code> will be 
	 * communicated to the listeners.
	 */
	protected final void updateProgress(int progress) {
		updateProgress(progress, state);
	}
	
	protected final void updateProgress(int progress, String state) {
		if(isIndeterminate() || progress < 0) {
			progress = INDETERMINATE;
		}
		else if(progress > 100) {
			progress = MAX;
		}
		
		if(state == null) {
			state = "";
		}
		this.state = state;
		
		for(ProgressListener listener : progressListeners) {
			synchronized(app) {
				listener.workProgressed(progress, state, this);
			}
		}
	}

}