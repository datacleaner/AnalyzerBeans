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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.metamodel.query.Query;

/**
 * Optimizer that will apply possible optimizations coming from
 * {@link QueryOptimizedFilter} instances in the job.
 * 
 * @author Kasper Sørensen
 */
public class RowProcessingQueryOptimizer {

	private final Query _baseQuery;
	private final List<RowProcessingConsumer> _consumers;
	private final Map<FilterConsumer, FilterOutcome> _optimizedFilters;

	public RowProcessingQueryOptimizer(List<RowProcessingConsumer> consumers, Query baseQuery) {
		_consumers = consumers;
		_baseQuery = baseQuery;
		_optimizedFilters = new HashMap<FilterConsumer, FilterOutcome>();

		init();
	}

	private void init() {
		int consumerIndex = 0;
		for (RowProcessingConsumer consumer : _consumers) {
			if (consumer instanceof FilterConsumer) {
				FilterConsumer filterConsumer = (FilterConsumer) consumer;
				FilterOutcome[] outcomes = filterConsumer.getComponentJob().getOutcomes();
				for (FilterOutcome outcome : outcomes) {
					boolean optimizable = isOptimizable(filterConsumer, outcome, consumerIndex);
					if (optimizable) {
						_optimizedFilters.put(filterConsumer, outcome);
					} else {
						break;
					}
				}
			}
		}
	}

	private boolean isOptimizable(final FilterConsumer filterConsumer, final FilterOutcome filterOutcome,
			final int consumerIndex) {
		if (!filterConsumer.isQueryOptimizable(filterOutcome)) {
			return false;
		}

		Set<InputColumn<?>> satisfiedColumns = new HashSet<InputColumn<?>>();
		Set<Outcome> satisfiedRequirements = new HashSet<Outcome>();
		satisfiedRequirements.add(filterOutcome);

		for (int i = consumerIndex + 1; i < _consumers.size(); i++) {
			boolean independentComponent = true;

			RowProcessingConsumer nextConsumer = _consumers.get(i);
			ComponentJob componentJob = nextConsumer.getComponentJob();
			if (componentJob instanceof OutcomeSinkJob) {
				Outcome[] requirements = ((OutcomeSinkJob) componentJob).getRequirements();
				for (Outcome requirement : requirements) {
					if (!satisfiedRequirements.contains(requirement)) {
						return false;
					} else {
						independentComponent = false;
					}
				}
			}

			if (componentJob instanceof InputColumnSinkJob) {
				InputColumn<?>[] requiredColumns = ((InputColumnSinkJob) componentJob).getInput();
				for (InputColumn<?> column : requiredColumns) {
					if (column.isVirtualColumn()) {
						if (!satisfiedColumns.contains(column)) {
							return false;
						} else {
							independentComponent = false;
						}
					}
				}
			}

			if (independentComponent) {
				// totally independent components prohibit optimization
				return false;
			}

			// this component is accepted now, add it's outcomes to the
			// satisfied requirements
			if (componentJob instanceof OutcomeSourceJob) {
				Outcome[] outcomes = ((OutcomeSourceJob) componentJob).getOutcomes();
				for (Outcome outcome : outcomes) {
					satisfiedRequirements.add(outcome);
				}
			}

			if (componentJob instanceof InputColumnSourceJob) {
				InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();
				for (InputColumn<?> column : output) {
					satisfiedColumns.add(column);
				}
			}
		}

		return true;
	}

	public Query getOptimizedQuery() {
		// create a copy/clone of the original query
		Query q = _baseQuery.clone();

		Set<Entry<FilterConsumer, FilterOutcome>> entries = _optimizedFilters.entrySet();
		for (Entry<FilterConsumer, FilterOutcome> entry : entries) {

			FilterConsumer consumer = entry.getKey();
			FilterOutcome outcome = entry.getValue();

			Filter<?> filter = consumer.getBeanInstance().getBean();
			@SuppressWarnings("rawtypes")
			QueryOptimizedFilter queryOptimizedFilter = (QueryOptimizedFilter) filter;

			@SuppressWarnings("unchecked")
			Query newQuery = queryOptimizedFilter.optimizeQuery(q, outcome.getCategory());
			q = newQuery;
		}
		return q;
	}

	public List<RowProcessingConsumer> getOptimizedConsumers() {
		List<RowProcessingConsumer> result = new ArrayList<RowProcessingConsumer>(_consumers);
		for (FilterConsumer filterConsumer : _optimizedFilters.keySet()) {
			result.remove(filterConsumer);
		}
		return result;
	}

	public Collection<? extends Outcome> getOptimizedAvailableOutcomes() {
		return _optimizedFilters.values();
	}

	public boolean isOptimizable() {
		return !_optimizedFilters.isEmpty();
	}

}
