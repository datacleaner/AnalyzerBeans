package org.eobjects.analyzer.result;

import java.io.Serializable;

import dk.eobjects.metamodel.DataContext;

public class DefaultResultProducer implements Serializable, ResultProducer {

	private static final long serialVersionUID = 1L;
	private AnalyzerResult result;

	public DefaultResultProducer(AnalyzerResult result) {
		this.result = result;
	}

	@Override
	public AnalyzerResult getResult() {
		return result;
	}

	@Override
	public void setDataContext(DataContext dataContext) {
		// do nothing
	}
}
