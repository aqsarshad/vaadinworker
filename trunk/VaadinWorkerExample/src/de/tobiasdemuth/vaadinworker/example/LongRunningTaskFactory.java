package de.tobiasdemuth.vaadinworker.example;

/**
 * 
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class LongRunningTaskFactory {
	
	private String name;
	
	private boolean cancelable = true;
	
	private boolean indeterminate = false;
	
	private int taskCounter = 0;
	
	private final VaadinWorkerExampleApplication app;
	
	public LongRunningTaskFactory(VaadinWorkerExampleApplication app) {
		this.app = app;
	}
	
	public String getName() {
		name = (name == null ? getTaskName() : name);
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean isCancelable() {
		return cancelable;
	}

	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}
	
	public LongRunningTask build() {
		LongRunningTask result = new LongRunningTask(getName(), 
				isCancelable(), isIndeterminate(), app);
		this.name = null;
		this.cancelable = true;
		this.indeterminate = false;
		
		return result;
	}
	
	private String getTaskName() {
		return "Task_" + Integer.toString(taskCounter++);
	}
}