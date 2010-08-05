package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public final class RunExplorerCallback implements AnalyzerLifeCycleCallback {

	private final DataContextProvider dataContextProvider;

	public RunExplorerCallback(DataContextProvider dataContextProvider) {
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringAnalyzer();

		ExploringAnalyzer<?> exploringAnalyzer = (ExploringAnalyzer<?>) analyzerBean;
		exploringAnalyzer.run(dataContextProvider.getDataContext());
	}

}
