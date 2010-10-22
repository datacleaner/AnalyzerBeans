package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.job.AnalyzerJob;

public interface AnalyzerJobBuilder<A extends Analyzer<?>> {
	
	public AnalyzerJob toAnalyzerJob() throws IllegalStateException;

	public AnalyzerJob[] toAnalyzerJobs() throws IllegalStateException;
	
	public boolean isConfigured(boolean throwException);

	public boolean isConfigured();
}
