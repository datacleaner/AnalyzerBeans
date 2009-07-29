package org.eobjects.analyzer.result;

public class NumberResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Class<?> analyzerClass;
	private Number number;

	public NumberResult(Class<?> analyzerClass, Number number) {
		super();
		this.analyzerClass = analyzerClass;
		this.number = number;
	}

	@Override
	public Class<?> getProducerClass() {
		return analyzerClass;
	}

	public Number getNumber() {
		return number;
	}
}
