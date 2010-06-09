package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class RunExplorerCallback implements LifeCycleCallback {

	private DataContextProvider dataContextProvider;

	public RunExplorerCallback(DataContextProvider dataContextProvider) {
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringAnalyzer();

		ExploringAnalyzer exploringAnalyzer = (ExploringAnalyzer) analyzerBean;
		exploringAnalyzer.run(dataContextProvider.getDataContext());
	}

}
