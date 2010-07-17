package org.eobjects.analyzer.job.runner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.result.AnalyzerResult;

public interface AnalysisResultFuture {

	public boolean isDone();
	
	public void await();
	
	public void await(long timeout, TimeUnit timeUnit);
	
	public List<AnalyzerResult> getResults();
}
