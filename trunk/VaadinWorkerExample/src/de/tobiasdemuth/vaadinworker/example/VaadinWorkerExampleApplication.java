package de.tobiasdemuth.vaadinworker.example;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.tobiasdemuth.vaadinworker.executorserviceprovider.ContextExecutorServiceProvider;
import de.tobiasdemuth.vaadinworker.ui.BackgroundExecutor;

public class VaadinWorkerExampleApplication extends Application {
	
	private static final long serialVersionUID = -5637289586541617363L;

	private Label finishLabel;
	
	private BackgroundExecutor executor;
	
	public VaadinWorkerExampleApplication() {
		this.finishLabel = new Label();
		this.finishLabel.setContentMode(Label.CONTENT_XHTML);
		
		this.executor = new BackgroundExecutor(new ContextExecutorServiceProvider());
		this.executor.setImmediate(true);
	}
	
	@Override
	public void init() {
		final Window mainWindow = new Window("VaadinWorkerExample");
		
		final Form form = new Form();
		form.setItemDataSource(new BeanItem<LongRunningTaskFactory>(
				new LongRunningTaskFactory(this)));
		form.setVisibleItemProperties(new Object[] { "name", "cancelable", "indeterminate" });
		form.setWriteThrough(true);
		form.setReadThrough(true);
				
		Button button = new Button("Do something!");
		button.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 5728843003918926892L;

			public void buttonClick(ClickEvent event) {
				Window w = new Window();
				w.addComponent(new Label("Done something!"));
				w.center();
				
				mainWindow.addWindow(w);
			}
		});
		
		Button addTask = new Button("Add a background-task!");
		addTask.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 3907581241215931392L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				LongRunningTaskFactory factory = 
						((BeanItem<LongRunningTaskFactory>) 
								form.getItemDataSource()).getBean();
				executor.submit(factory.build());
			}
			
		});
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(addTask);
		buttonLayout.addComponent(button);
		
		VerticalLayout formContainer = new VerticalLayout();
		formContainer.setSpacing(true);
		formContainer.addComponent(form);
		formContainer.addComponent(buttonLayout);
		
		final Panel configForm = new Panel("Configure the task to add");
		configForm.setContent(formContainer);
		
		VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setSpacing(true);
		
		rootLayout.addComponent(executor);
		rootLayout.addComponent(finishLabel);
		rootLayout.addComponent(configForm);
		
		mainWindow.setContent(rootLayout);		
		setMainWindow(mainWindow);
	}
	
	Label getFinishLabel() {
		return finishLabel;
	}

}
