package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.Analyzer;

public interface AnalyzerJobBuilder<A extends Analyzer<?>> {

	public AnalyzerJob toAnalyzerJob() throws IllegalStateException;

	public boolean isConfigured();
}
