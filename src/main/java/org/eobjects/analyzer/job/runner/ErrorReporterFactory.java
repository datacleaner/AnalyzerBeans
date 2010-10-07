package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.concurrent.ErrorReporter;

public interface ErrorReporterFactory {

	public ErrorReporter analyzerErrorReporter(final AnalysisJob job, final AnalyzerJob analyzerJob);

	public ErrorReporter unknownErrorReporter(final AnalysisJob job);
}
