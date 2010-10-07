package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;

public final class RunExplorerCallback implements AnalyzerLifeCycleCallback {

	private final AnalysisJob _job;
	private final AnalyzerJob _analyzerJob;
	private final DataContextProvider _dataContextProvider;
	private final AnalysisListener _analysisListener;

	public RunExplorerCallback(AnalysisJob job, AnalyzerJob analyzerJob, DataContextProvider dataContextProvider,
			AnalysisListener analysisListener) {
		_job = job;
		_analyzerJob = analyzerJob;
		_dataContextProvider = dataContextProvider;
		_analysisListener = analysisListener;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean, AnalyzerBeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringAnalyzer();

		ExploringAnalyzer<?> exploringAnalyzer = (ExploringAnalyzer<?>) analyzerBean;
		if (_analysisListener != null) {
			_analysisListener.analyzerBegin(_job, _analyzerJob);
		}
		exploringAnalyzer.run(_dataContextProvider.getDataContext());
	}

}
