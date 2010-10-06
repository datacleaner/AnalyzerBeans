package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.BeanJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;

/**
 * Interface for objects that recieve rows from the RowProcessingPublisher.
 * 
 * @author Kasper Sørensen
 */
interface RowProcessingConsumer {

	public InputColumn<?>[] getRequiredInput();

	public FilterOutcome getRequiredOutcome();

	/**
	 * Main method of the consumer. Recieves the input row, dispatches it to the
	 * bean that needs to process it and returns the row for the next component
	 * in the chain to process (often the same row).
	 * 
	 * @param row
	 * @param distinctCount
	 * @param outcomes
	 * @return
	 */
	public InputRow consume(InputRow row, int distinctCount, FilterOutcomeSink outcomes);

	public AbstractBeanInstance<?> getBeanInstance();

	public BeanJob<?> getBeanJob();
}
