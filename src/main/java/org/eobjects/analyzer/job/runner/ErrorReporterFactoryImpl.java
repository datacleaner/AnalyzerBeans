package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.concurrent.ErrorReporter;

final class ErrorReporterFactoryImpl implements ErrorReporterFactory {

	private AnalysisListener _analysisListener;

	public ErrorReporterFactoryImpl(AnalysisListener analysisListener) {
		_analysisListener = analysisListener;
	}

	@Override
	public ErrorReporter analyzerErrorReporter(final AnalysisJob job, final AnalyzerJob analyzerJob) {
		return new ErrorReporter() {
			@Override
			public void reportError(Throwable throwable) {
				_analysisListener.errorInAnalyzer(job, analyzerJob, throwable);
			}
		};
	}

	@Override
	public ErrorReporter unknownErrorReporter(final AnalysisJob job) {
		return new ErrorReporter() {
			@Override
			public void reportError(Throwable throwable) {
				_analysisListener.errorUknown(job, throwable);
			}
		};
	}

}
