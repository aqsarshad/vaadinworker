package de.tobiasdemuth.vaadinworker.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 * 
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
final class TaskProgressView extends CustomComponent implements VaadinWorker.ProgressListener {

	private static final long serialVersionUID = -9128251611030074624L;
	
	private static final int POLLING_INTERVALL = 500;
	
	private final VaadinWorker workload;
	
	private final ProgressIndicator progressIndicator;
	
	private final Label stateLabel;
	
	private final Button cancel;
	
	private boolean initialized = false;
	
	public TaskProgressView(VaadinWorker vaadinWorker, String cancelCaption) {
		if(vaadinWorker == null) {
			throw new NullPointerException("Workload cannot be null!");
		}
		this.workload = vaadinWorker;
		this.workload.addListener(this);
		
		this.progressIndicator = new ProgressIndicator();
		this.progressIndicator.setPollingInterval(POLLING_INTERVALL);
		
		this.stateLabel = new Label();
		
		this.cancel = new Button(cancelCaption);
		this.cancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6760693330701499660L;

			public void buttonClick(ClickEvent event) {
				workload.cancel();
				cancel.setEnabled(false);
			}
			
		});
		this.cancel.setEnabled(workload.isCancelable());
	}
	
	public void attach() {
		if(initialized) {
			return; // Initialize only once
		}
		
		VerticalLayout progressIndicatorLayout = new VerticalLayout();
		progressIndicatorLayout.setSpacing(false);
		
		progressIndicator.setIndeterminate(workload.isIndeterminate());
		progressIndicator.setValue(0.0f);
		progressIndicator.setWidth("200px");
		
		progressIndicatorLayout.addComponent(progressIndicator);
		progressIndicatorLayout.addComponent(stateLabel);
		
		HorizontalLayout root = new HorizontalLayout();
		root.setSpacing(true);
		
		root.addComponent(progressIndicatorLayout);
		root.addComponent(cancel);
		
		setCompositionRoot(root);
		initialized = true;
	}

	public void workProgressed(int progress, String state, VaadinWorker worker) {
		progressIndicator.setValue(progress / 100f);
		stateLabel.setValue(state);
	}

}
