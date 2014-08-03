/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnyOutcome;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSourceJob;

/**
 * Helping class for the row processing publisher, that will help sort the
 * consumers correctly
 * 
 * 
 */
class RowProcessingConsumerSorter {

	private final Collection<? extends RowProcessingConsumer> _consumers;

	public RowProcessingConsumerSorter(Collection<? extends RowProcessingConsumer> consumers) {
		_consumers = consumers;
	}

	public List<RowProcessingConsumer> createProcessOrderedConsumerList() {
		List<RowProcessingConsumer> orderedConsumers = new ArrayList<RowProcessingConsumer>();
		Collection<RowProcessingConsumer> remainingConsumers = new LinkedList<RowProcessingConsumer>(_consumers);
		Set<InputColumn<?>> availableVirtualColumns = new HashSet<InputColumn<?>>();
		Set<Outcome> availableOutcomes = new HashSet<Outcome>();

		while (!remainingConsumers.isEmpty()) {
			boolean changed = false;
			for (Iterator<RowProcessingConsumer> it = remainingConsumers.iterator(); it.hasNext();) {
				RowProcessingConsumer consumer = it.next();

				boolean accepted = true;

				// make sure that any dependent filter outcome is evaluated
				// before this component
				accepted = consumer.satisfiedForFlowOrdering(availableOutcomes);

				// make sure that all the required colums are present
				if (accepted) {
					InputColumn<?>[] requiredInput = consumer.getRequiredInput();
					if (requiredInput != null) {
						for (InputColumn<?> inputColumn : requiredInput) {
							if (!inputColumn.isPhysicalColumn()) {
								if (!(inputColumn instanceof ExpressionBasedInputColumn)) {
									if (!availableVirtualColumns.contains(inputColumn)) {
										accepted = false;
										break;
									}
								}
							}
						}
					}
				}

				if (accepted) {
					orderedConsumers.add(consumer);
					it.remove();
					changed = true;

					ComponentJob componentJob = consumer.getComponentJob();

					InputColumn<?>[] requiredInput = consumer.getRequiredInput();
					for (InputColumn<?> inputColumn : requiredInput) {
						if (inputColumn instanceof ExpressionBasedInputColumn) {
							availableVirtualColumns.add(inputColumn);
						}
					}

					if (componentJob instanceof InputColumnSourceJob) {
						InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();
						for (InputColumn<?> col : output) {
							availableVirtualColumns.add(col);
						}
					}

					if (componentJob instanceof OutcomeSourceJob) {
						Outcome[] outcomes = ((OutcomeSourceJob) componentJob).getOutcomes();
						for (Outcome outcome : outcomes) {
							availableOutcomes.add(outcome);
						}
					}
				}
			}

			if (!changed) {
				// should never happen, but if a bug enters the
				// algorithm this exception will quickly expose it
				throw new IllegalStateException("Could not detect next consumer in processing order");
			}
		}
		return orderedConsumers;
	}

}
