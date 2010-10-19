package org.eobjects.analyzer.result;

import dk.eobjects.metamodel.DataContext;

public class CrosstabResult implements DataContextAwareAnalyzerResult {

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

	@Override
	public void setDataContext(DataContext dataContext) {
		crosstab.setDataContext(dataContext);
	}
}
