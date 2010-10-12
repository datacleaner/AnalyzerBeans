package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

public final class AnalyzerJobResult {

	private final AnalyzerJob _job;
	private final AnalyzerResult _result;

	public AnalyzerJobResult(AnalyzerResult result, AnalyzerJob job) {
		_job = job;
		_result = result;
	}

	public AnalyzerJob getJob() {
		return _job;
	}

	public AnalyzerResult getResult() {
		return _result;
	}
}
