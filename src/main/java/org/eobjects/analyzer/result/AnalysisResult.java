package org.eobjects.analyzer.result;

import java.io.Serializable;

/**
 * An AnalysisResult represents the result or part of the result of an analysis
 * execution. Hence an @AnalyzerBean object can yield one or more AnalysisResult
 * objects.
 */
public interface AnalysisResult extends Serializable {

	public boolean isSuccesful();

	public Class<?> getAnalyserClass();

	public void setAnalyserClass(Class<?> analyzerClass);
	
}