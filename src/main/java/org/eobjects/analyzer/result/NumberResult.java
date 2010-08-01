package org.eobjects.analyzer.result;

import org.eobjects.analyzer.beans.Analyzer;

public class NumberResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Class<? extends Analyzer> analyzerClass;
	private Number number;

	public NumberResult(Class<? extends Analyzer> analyzerClass, Number number) {
		super();
		this.analyzerClass = analyzerClass;
		this.number = number;
	}

	@Override
	public Class<? extends Analyzer> getProducerClass() {
		return analyzerClass;
	}

	public Number getNumber() {
		return number;
	}
}
