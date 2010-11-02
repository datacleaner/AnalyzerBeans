package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.ImmutableFilterOutcome;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;

final class FilterConsumer extends ConfigurableBeanJobRowProcessingConsumer implements RowProcessingConsumer {

	private final AnalysisJob _job;
	private final FilterBeanInstance _filterBeanInstance;
	private final FilterJob _filterJob;
	private final InputColumn<?>[] _inputColumns;
	private final AnalysisListener _analysisListener;

	public FilterConsumer(AnalysisJob job, FilterBeanInstance filterBeanInstance, FilterJob filterJob,
			InputColumn<?>[] inputColumns, AnalysisListener analysisListener) {
		super(filterJob);
		_filterBeanInstance = filterBeanInstance;
		_filterJob = filterJob;
		_inputColumns = inputColumns;
		_job = job;
		_analysisListener = analysisListener;
	}

	@Override
	public InputColumn<?>[] getRequiredInput() {
		return _inputColumns;
	}

	@Override
	public InputRow consume(InputRow row, int distinctCount, OutcomeSink outcomes) {
		Filter<?> filter = _filterBeanInstance.getBean();
		try {
			Enum<?> category = filter.categorize(row);
			FilterOutcome outcome = new ImmutableFilterOutcome(_filterJob, category);
			outcomes.add(outcome);
		} catch (RuntimeException e) {
			_analysisListener.errorInFilter(_job, _filterJob, e);
		}
		return row;
	}

	@Override
	public FilterBeanInstance getBeanInstance() {
		return _filterBeanInstance;
	}

	@Override
	public FilterJob getComponentJob() {
		return _filterJob;
	}

	@Override
	public String toString() {
		return "FilterConsumer[" + _filterBeanInstance + "]";
	}
}
