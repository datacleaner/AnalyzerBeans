package org.eobjects.analyzer.result;

public class CrosstabResult implements AnalyzerBeanResult {

	private static final long serialVersionUID = 1L;
	private Class<?> analyzerClass;
	private Crosstab<?> crosstab;

	public CrosstabResult(Class<?> analyzerClass, Crosstab<?> crosstab) {
		super();
		this.analyzerClass = analyzerClass;
		this.crosstab = crosstab;
	}

	@Override
	public Class<?> getAnalyzerClass() {
		return analyzerClass;
	}
	
	public Crosstab<?> getCrosstab() {
		return crosstab;
	}
}
