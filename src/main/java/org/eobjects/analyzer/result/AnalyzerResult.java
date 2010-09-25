package org.eobjects.analyzer.result;

import java.io.Serializable;

import org.eobjects.analyzer.beans.Analyzer;

/**
 * An AnalysisResult represents the result of an analysis execution.
 */
public interface AnalyzerResult extends Serializable {

	/**
	 * @return The Analyzer class that have produced this result.
	 */
	public Class<? extends Analyzer<?>> getProducerClass();
}