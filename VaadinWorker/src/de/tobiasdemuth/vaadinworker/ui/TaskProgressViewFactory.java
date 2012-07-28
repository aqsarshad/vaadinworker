package de.tobiasdemuth.vaadinworker.ui;

import java.io.Serializable;

import com.vaadin.ui.Component;

import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 * A TaskProgressViewFactory is used to create the individual views for the
 * tasks currently under work. Using this mechanism you can highly customize
 * the content of the popup shown by the BackgroundExecutor.   
 *
 * @see de.tobiasdemuth.vaadinworker.ui.DefaultTaskProgressViewFactory
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public interface TaskProgressViewFactory extends Serializable {
	
	/**
	 * This method is called in order to get a view for the overgiven 
	 * VaadinWorker. The view should somehow display the progress of the task.
	 */
	public Component createTaskProgressView(VaadinWorker worker);

}
