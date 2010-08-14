package org.eobjects.analyzer.lifecycle;

import java.io.File;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class RunExplorerCallbackTest extends TestCase {

	private static boolean isAssertEnabled() {
		boolean assertIsEnabledFlag = false;
		assert assertIsEnabledFlag = true;
		return assertIsEnabledFlag;
	}

	public void testOnEvent() throws Exception {
		DataContextProvider dataContextProvider = EasyMock
				.createMock(DataContextProvider.class);
		ExploringAnalyzer<?> analyzer = EasyMock
				.createMock(ExploringAnalyzer.class);
		AnalyzerBeanDescriptor<?> descriptor = EasyMock
				.createMock(AnalyzerBeanDescriptor.class);

		DataContext dataContext = DataContextFactory
				.createCsvDataContext(new File(
						"src/test/resources/RunExplorerCallbackTest-data.csv"));

		if (isAssertEnabled()) {
			EasyMock.expect(descriptor.isExploringAnalyzer()).andReturn(true);
		}

		EasyMock.expect(dataContextProvider.getDataContext()).andReturn(
				dataContext);
		analyzer.run(dataContext);

		EasyMock.replay(dataContextProvider, analyzer, descriptor);

		RunExplorerCallback callback = new RunExplorerCallback(
				dataContextProvider);
		callback.onEvent(LifeCycleState.RUN, analyzer, descriptor);

		EasyMock.verify(dataContextProvider, analyzer, descriptor);
	}
}
