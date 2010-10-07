package org.eobjects.analyzer.lifecycle;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.result.AnalyzerResult;

public final class ReturnResultsCallback implements AnalyzerLifeCycleCallback {

	private final AnalysisJob _job;
	private final AnalyzerJob _analyzerJob;
	private final Collection<AnalyzerResult> _results;
	private final AnalysisListener _analysisListener;

	public ReturnResultsCallback(AnalysisJob job, AnalyzerJob analyzerJob, Collection<AnalyzerResult> results,
			AnalysisListener analysisListener) {
		_job = job;
		_analyzerJob = analyzerJob;
		_results = results;
		_analysisListener = analysisListener;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean, AnalyzerBeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.RETURN_RESULTS;

		AnalyzerResult result = analyzerBean.getResult();
		if (result == null) {
			throw new IllegalStateException("Analyzer (" + analyzerBean + ") returned null as a result");
		}
		_analysisListener.analyzerSuccess(_job, _analyzerJob, result);
		_results.add(result);
	}
}
