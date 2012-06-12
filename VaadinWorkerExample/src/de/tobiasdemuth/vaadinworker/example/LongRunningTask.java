package de.tobiasdemuth.vaadinworker.example;

import com.vaadin.ui.Label;

import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 *  
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class LongRunningTask extends VaadinWorker {
	
	private final String name;

	public LongRunningTask(String name, boolean cancelable, boolean indeterminate, 
			VaadinWorkerExampleApplication app) {
		super(app);
		this.name = name;
		setCancelable(cancelable);
		setIndeterminate(indeterminate);
	}

	@Override
	public void runInBackground() {
		for(int i = 0; i < 100 && !isCanceled(); i++) {
			updateProgress(i, i + "%");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				cancel();
			}
		}
	}

	@Override
	public void updateUI() {
		VaadinWorkerExampleApplication app = (VaadinWorkerExampleApplication) getApplication();
		Label finishLabel = app.getFinishLabel();
		finishLabel.setValue(finishLabel.getValue() + "<br />" + name);
	}

}
