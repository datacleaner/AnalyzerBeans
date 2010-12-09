/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.lifecycle;

import java.io.File;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
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
		Datastore datastore = EasyMock.createMock(Datastore.class);
		DataContextProvider dataContextProvider = EasyMock.createMock(DataContextProvider.class);
		ExploringAnalyzer<?> analyzer = EasyMock.createMock(ExploringAnalyzer.class);
		AnalyzerBeanDescriptor<?> descriptor = EasyMock.createMock(AnalyzerBeanDescriptor.class);

		DataContext dataContext = DataContextFactory.createCsvDataContext(new File(
				"src/test/resources/RunExplorerCallbackTest-data.csv"));

		if (isAssertEnabled()) {
			EasyMock.expect(descriptor.isExploringAnalyzer()).andReturn(true);
		}

		EasyMock.expect(datastore.getDataContextProvider()).andReturn(dataContextProvider);
		EasyMock.expect(dataContextProvider.getDataContext()).andReturn(dataContext);
		dataContextProvider.close();

		analyzer.run(dataContext);

		EasyMock.replay(datastore, dataContextProvider, analyzer, descriptor);

		RunExplorerCallback callback = new RunExplorerCallback(null, null, datastore, null);
		callback.onEvent(LifeCycleState.RUN, analyzer, descriptor);

		EasyMock.verify(datastore, dataContextProvider, analyzer, descriptor);
	}
}
