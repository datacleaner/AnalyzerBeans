package org.eobjects.analyzer.job.runner;

import java.util.Collection;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;

/**
 * Interface for objects that recieve rows from the RowProcessingPublisher.
 * 
 * @author Kasper Sørensen
 */
public interface RowProcessingConsumer {

	/**
	 * @return the required input columns for this consumer
	 */
	public InputColumn<?>[] getRequiredInput();

	/**
	 * @param availableOutcomesInFlow
	 *            a collection of all outcomes that <i>can</i> be available to
	 *            the consumer given the proposed flow order.
	 * @return whether or not the requirements (in terms of required outcomes)
	 *         are sufficient for adding this consumer into the execution flow.
	 *         If false the ordering mechanism will try to move the consumer to
	 *         a later stage in the flow.
	 */
	public boolean satisfiedForFlowOrdering(Collection<Outcome> availableOutcomesInFlow);

	/**
	 * @param outcomes
	 *            the current available outcomes in the processing of the
	 *            particular row.
	 * @return whether or not the requirements (in terms of required outcomes)
	 *         are sufficient for including this component for a particular
	 *         row's processing. If false, this component will be skipped.
	 */
	public boolean satisfiedForConsume(Outcome[] outcomes);

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
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes);

	/**
	 * @return the bean instance or null if none is available
	 */
	public AbstractBeanInstance<?> getBeanInstance();

	/**
	 * @return the componbent job
	 */
	public ComponentJob getComponentJob();
}
