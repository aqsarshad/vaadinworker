package de.tobiasdemuth.vaadinworker.tutorial;

import static de.tobiasdemuth.vaadinworker.tutorial.VaadinWorkerTutorialApplication.COUNTER;
import static de.tobiasdemuth.vaadinworker.tutorial.VaadinWorkerTutorialApplication.DATA;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import de.tobiasdemuth.vaadinworker.VaadinWorker;
import de.tobiasdemuth.vaadinworker.VaadinWorker.ProgressListener;

/**
 * This revised version of the Fibonacci-Computer is able to do in-between-updates of the
 * view, giving a more responsive feeling for the client. In order to solve the problem
 * with the flickering table, we update the table only every 100 items (approximately 
 * once every second).
 *
 * @author Tobias Demuth &lt;mailto:myself@tobias-demuth.de&gt;
 */
public class FibonacciComputer extends VaadinWorker implements ProgressListener {
	
	// How many numbers should we compute?
	private final int numberCount;
	
	// Save the two last computed numbers
	private BigInteger secondToLast = BigInteger.ONE;
	private BigInteger last = BigInteger.ONE;
	
	// Keep the computed last 100 numbers in memory
	private int counter;
	private final List<BigInteger> current;
	
	// the table's datasource
	private Container container;
	
	// flag to control, if we already have done any initialization work on the UI
	private boolean initialized = false;

	public FibonacciComputer(final VaadinWorkerTutorialApplication application, 
			final int numberCount) {
		super(application);
		// For in-between-updates you need a ProgressListener that gets informed
		// whenever updateProgress() gets called
		addListener(this);
		
		this.numberCount = numberCount;
		this.current = new ArrayList<BigInteger>();
	}

	@Override
	public void runInBackground() {
		// First two numbers don't have to be computed, they are simple "1"
		counter = 0;
		current.add(BigInteger.ONE);
		// Now ProgressListener#workProgressed() will get called, which does
		// the actual in-between update of the UI
		updateProgress((int) (((double) counter / numberCount) * 100), "Computing ...");
		
		counter = 1;
		current.add(BigInteger.ONE);
		updateProgress((int) (((double) counter / numberCount) * 100), "Computing ...");
		
		for(counter = 2; counter < numberCount; counter++) {
			BigInteger currentNumber = secondToLast.add(last);
			current.add(currentNumber);
			// Do the update for every computed number
			updateProgress((int) (((double) counter / numberCount) * 100), "Computing ...");
			
			secondToLast = last;
			last = currentNumber;
			
			try {
				// Do a short nap to make it more thrilling ;)
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}
	
	public void workProgressed(int progress, String state, VaadinWorker worker) {
		// If called the very first time, we need to do some initialization-work.
		// We could also do the disabling of the form-controls here, but I wanted 
		// to keep the Application unchanged.
		if(!initialized) {
			this.container = new IndexedContainer();
			this.container.addContainerProperty(COUNTER, Integer.class, 0);
			this.container.addContainerProperty(DATA, BigInteger.class, BigInteger.ZERO);
			
			VaadinWorkerTutorialApplication app = (VaadinWorkerTutorialApplication) getApplication();
			
			Table table = app.getNumbersTable();
			table.setContainerDataSource(container);
			
			initialized = true;
		}
		
		// If at least 100 new numbers have computed, add them to the view
		if(current.size() >= 100) {
			addCurrentNumbersToContainer();
		}
	}

	@Override
	public void updateUI() {
		// Add the rest of the numbers to the container
		addCurrentNumbersToContainer();
		
		// Reenable the disabled form-controls and display the notification
		VaadinWorkerTutorialApplication app = (VaadinWorkerTutorialApplication) getApplication();
		
		Component upperBoundComponent = app.getUpperBoundComponent();
		upperBoundComponent.setEnabled(true);
		
		Component computationStarter = app.getComputationStarter();
		computationStarter.setEnabled(true);
		
		app.getMainWindow().showNotification("Computation finished!");
	}
	
	private void addCurrentNumbersToContainer() {
		System.out.println("addCurrentNumbersToContainer");
		int i = 1;
		for(BigInteger number : current) {
			Object itemId = container.addItem();
			container.getItem(itemId).getItemProperty(COUNTER).setValue(counter - current.size() + i + 1);
			container.getItem(itemId).getItemProperty(DATA).setValue(number);
			i++;
		}
		
		current.clear();
	}

}
