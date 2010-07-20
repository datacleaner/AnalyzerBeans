package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;

public interface AnalysisRunner {

	public AnalysisResultFuture run(AnalysisJob job);
}
