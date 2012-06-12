package de.tobiasdemuth.vaadinworker.tutorial;

import static de.tobiasdemuth.vaadinworker.tutorial.VaadinWorkerTutorialApplication.COUNTER;
import static de.tobiasdemuth.vaadinworker.tutorial.VaadinWorkerTutorialApplication.DATA;

import java.math.BigInteger;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import de.tobiasdemuth.vaadinworker.VaadinWorker;

/**
 *  
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class FibonacciComputer extends VaadinWorker {
	
	private final int numberCount;
	
	private BigInteger secondToLast = BigInteger.ONE;
	
	private BigInteger last = BigInteger.ONE;
	
	private Container container;

	public FibonacciComputer(final VaadinWorkerTutorialApplication application, 
			final int numberCount) {
		super(application);
		
		this.numberCount = numberCount;
		this.container = new IndexedContainer();
		this.container.addContainerProperty(COUNTER, Integer.class, 0);
		this.container.addContainerProperty(DATA, BigInteger.class, BigInteger.ZERO);
	}

	@Override
	public void runInBackground() {
		container.removeAllItems();
		
		int counter = 0;
		Object itemId = container.addItem();
		container.getItem(itemId).getItemProperty(COUNTER).setValue(counter + 1);
		container.getItem(itemId).getItemProperty(DATA).setValue(BigInteger.ONE);
		
		counter = 1;
		itemId = container.addItem();
		container.getItem(itemId).getItemProperty(COUNTER).setValue(counter + 1);
		container.getItem(itemId).getItemProperty(DATA).setValue(BigInteger.ONE);
		
		for(counter = 2; counter < numberCount; counter++) {
			BigInteger newNumber = secondToLast.add(last);
			
			itemId = container.addItem();
			container.getItem(itemId).getItemProperty(COUNTER).setValue(counter + 1);
			container.getItem(itemId).getItemProperty(DATA).setValue(newNumber);
			
			secondToLast = last;
			last = newNumber;
			
			updateProgress((int) ((double) counter / numberCount) * 100, "Computing ...");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	@Override
	public void updateUI() {
		VaadinWorkerTutorialApplication app = (VaadinWorkerTutorialApplication) getApplication();
		
		Table table = app.getNumbersTable();
		table.setContainerDataSource(container);
		
		Component upperBoundComponent = app.getUpperBoundComponent();
		upperBoundComponent.setEnabled(true);
		
		Component computationStarter = app.getComputationStarter();
		computationStarter.setEnabled(true);
		
		app.getMainWindow().showNotification("Computation finished!");
	}

}
