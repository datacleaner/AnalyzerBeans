package org.eobjects.analyzer.result;

public class CrosstabResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Class<?> analyzerClass;
	private Crosstab<?> crosstab;

	public CrosstabResult(Class<?> analyzerClass, Crosstab<?> crosstab) {
		super();
		this.analyzerClass = analyzerClass;
		this.crosstab = crosstab;
	}

	@Override
	public Class<?> getProducerClass() {
		return analyzerClass;
	}
	
	public Crosstab<?> getCrosstab() {
		return crosstab;
	}
}
