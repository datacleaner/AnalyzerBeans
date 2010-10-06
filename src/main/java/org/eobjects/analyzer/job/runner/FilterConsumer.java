package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.BeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.ImmutableFilterOutcome;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;

final class FilterConsumer implements RowProcessingConsumer {

	private final FilterBeanInstance _filterBeanInstance;
	private final FilterJob _filterJob;
	private final InputColumn<?>[] _inputColumns;

	public FilterConsumer(FilterBeanInstance filterBeanInstance, FilterJob filterJob, InputColumn<?>[] inputColumns) {
		_filterBeanInstance = filterBeanInstance;
		_filterJob = filterJob;
		_inputColumns = inputColumns;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, FilterOutcomeSink outcomes) {
		Filter<?> filter = _filterBeanInstance.getBean();
		Enum<?> category = filter.categorize(row);
		FilterOutcome outcome = new ImmutableFilterOutcome(_filterJob, category);
		outcomes.add(outcome);
		return row;
	}

	@Override
	public AbstractBeanInstance<?> getBeanInstance() {
		return _filterBeanInstance;
	}

	@Override
	public BeanJob<?> getBeanJob() {
		return _filterJob;
	}

	@Override
	public FilterOutcome getRequiredOutcome() {
		return _filterJob.getRequirement();
	}

}
