package org.eobjects.analyzer.result;

public class NumberResult implements AnalyzerBeanResult {

	private static final long serialVersionUID = 1L;
	private Class<?> analyzerClass;
	private Number number;

	public NumberResult(Class<?> analyzerClass, Number number) {
		super();
		this.analyzerClass = analyzerClass;
		this.number = number;
	}

	@Override
	public Class<?> getAnalyzerClass() {
		return analyzerClass;
	}

	@Override
	public boolean isSuccesful() {
		return true;
	}

	public Number getNumber() {
		return number;
	}
}
