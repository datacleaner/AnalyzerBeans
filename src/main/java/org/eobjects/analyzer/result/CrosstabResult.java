package org.eobjects.analyzer.result;

import org.eobjects.analyzer.beans.Analyzer;

public class CrosstabResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Class<? extends Analyzer<?>> analyzerClass;
	private Crosstab<?> crosstab;

	public CrosstabResult(Class<? extends Analyzer<?>> analyzerClass, Crosstab<?> crosstab) {
		super();
		this.analyzerClass = analyzerClass;
		this.crosstab = crosstab;
	}

	@Override
	public Class<? extends Analyzer<?>> getProducerClass() {
		return analyzerClass;
	}

	public Crosstab<?> getCrosstab() {
		return crosstab;
	}
}
