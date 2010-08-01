package org.eobjects.analyzer.result;

import java.io.Serializable;

import org.eobjects.analyzer.beans.Analyzer;

/**
 * An AnalysisResult represents the result or part of the result of an analysis
 * execution. Hence an @AnalyzerBean object can yield one or more AnalysisResult
 * objects.
 */
public interface AnalyzerResult extends Serializable {

	public Class<? extends Analyzer> getProducerClass();
}