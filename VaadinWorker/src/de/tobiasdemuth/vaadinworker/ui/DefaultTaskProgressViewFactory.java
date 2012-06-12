package de.tobiasdemuth.vaadinworker.ui;

import com.vaadin.ui.Component;

import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 * This is the default TaskProgressViewFactory. It can be used to localize the
 * caption of the cancel-button by calling the method setCancelCaption(String).
 * All subsequently created TaskProgressViews are then using the given caption. 
 *
 * @see de.tobiasdemuth.vaadinworker.ui.TaskProgressViewFactory
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public final class DefaultTaskProgressViewFactory implements
		TaskProgressViewFactory {
	
	private String cancelCaption = "Cancel";
	
	public void setCancelCaption(String cancelCaption) {
		this.cancelCaption = cancelCaption;
	}
	
	public String getCancelCaption() {
		return cancelCaption;
	}

	public Component createTaskProgressView(VaadinWorker worker) {
		return new TaskProgressView(worker, getCancelCaption());
	}

}
