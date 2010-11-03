package org.eobjects.analyzer.result;

public class CrosstabResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Crosstab<?> crosstab;

	public CrosstabResult(Crosstab<?> crosstab) {
		super();
		this.crosstab = crosstab;
	}

	public Crosstab<?> getCrosstab() {
		return crosstab;
	}

	@Override
	public String toString() {
		return crosstab.toString();
	}
}
