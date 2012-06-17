package de.tobiasdemuth.vaadinworker.tutorial;

import java.math.BigInteger;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.tobiasdemuth.vaadinworker.executorserviceprovider.ContextExecutorServiceProvider;
import de.tobiasdemuth.vaadinworker.ui.BackgroundExecutor;

/**
 *  
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class VaadinWorkerTutorialApplication extends Application {
	
	public static final String COUNTER = "Counter";
	
	public static final String DATA = "Data";
	
	private final class StartComputationClickListener implements ClickListener {

		public void buttonClick(ClickEvent event) {
			if(!upperBoundComponent.isValid()) {
				return;
			}
			
			String upperBoundValue = (String) upperBoundComponent.getValue();
			int numberCount = Integer.valueOf(upperBoundValue);
			
			upperBoundComponent.setEnabled(false);
			computationStarter.setEnabled(false);
			
			executor.submit(new FibonacciComputer(
					VaadinWorkerTutorialApplication.this, 
					numberCount));
		}

	}
	
	private final class UpperBoundValidator implements Validator {

		public void validate(Object value) throws InvalidValueException {
			try {
				int intValue = Integer.valueOf((String) value);
				
				if(intValue < 3 || intValue > 1000) {
					throw new InvalidValueException("Please specify a value between 3 and 1000!");
				}
			}
			catch(NumberFormatException e) {
				throw new InvalidValueException("Only numbers are allowed here!");
			}
		}

		public boolean isValid(Object value) {
			try {
				validate(value);
				return true;
			}
			catch(InvalidValueException e) {
				return false;
			}
		}
		
	}

	private final BackgroundExecutor executor;
	
	private final Table numbersTable;
	
	private final TextField upperBoundComponent;
	
	private final Button computationStarter;
	
	public VaadinWorkerTutorialApplication() {
		this.executor = new BackgroundExecutor(new ContextExecutorServiceProvider());
		
		final Container initialDatasource = new IndexedContainer();
		initialDatasource.addContainerProperty(COUNTER, Integer.class, 0);
		initialDatasource.addContainerProperty(DATA, BigInteger.class, BigInteger.ZERO);
		
		this.numbersTable = new Table();
		this.numbersTable.setContainerDataSource(initialDatasource);
		this.numbersTable.setWidth("100%");
		this.numbersTable.setPageLength(5);
		
		this.upperBoundComponent = new TextField("How many Fibonacci-Numbers " +
				"should be computed?");
		this.upperBoundComponent.setNullRepresentation("0");
		this.upperBoundComponent.setRequired(true);
		this.upperBoundComponent.setRequiredError("Please specify, how many " +
				"Fibonacci-numbers should be computed!");
		this.upperBoundComponent.addValidator(new UpperBoundValidator());
		
		this.computationStarter = new Button("Start Computation", 
				new StartComputationClickListener());
	}

	@Override
	public void init() {
		final Window mainWindow = new Window("VaadinWorkerTutorial");
		final VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setSpacing(true);
		rootLayout.setMargin(true);
		
		rootLayout.addComponent(executor);
		
		rootLayout.addComponent(numbersTable);
		
		final HorizontalLayout controls = new HorizontalLayout();
		controls.setSpacing(true);
		
		controls.addComponent(upperBoundComponent);
		controls.addComponent(computationStarter);
		
		rootLayout.addComponent(controls);
		
		mainWindow.setContent(rootLayout);		
		setMainWindow(mainWindow);
	}

	Table getNumbersTable() {
		return numbersTable;
	}

	Component getUpperBoundComponent() {
		return upperBoundComponent;
	}

	Component getComputationStarter() {
		return computationStarter;
	}

}
