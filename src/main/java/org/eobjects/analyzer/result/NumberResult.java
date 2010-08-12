package org.eobjects.analyzer.result;

import org.eobjects.analyzer.beans.Analyzer;

/**
 * Very simple result type for analyzers that simply return a number (maybe a
 * KPI or something like that).
 * 
 * Mostly used for testing purposes.
 * 
 * @author Kasper Sørensen
 */
public class NumberResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Class<? extends Analyzer<?>> analyzerClass;
	private Number number;

	public NumberResult(Class<? extends Analyzer<?>> analyzerClass,
			Number number) {
		super();
		this.analyzerClass = analyzerClass;
		this.number = number;
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return analyzerClass;
	}

	public Number getNumber() {
		return number;
	}
	
	@Override
	public String toString() {
		if (number == null) {
			return "<null>";
		}
		return number.toString();
	}
}
