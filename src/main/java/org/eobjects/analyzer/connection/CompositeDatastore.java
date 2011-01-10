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
package org.eobjects.analyzer.connection;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;

public final class CompositeDatastore extends UsageAwareDatastore {

	private static final long serialVersionUID = 1L;

	private final String _name;
	private final List<Datastore> _datastores;

	public CompositeDatastore(String name, List<Datastore> datastores) {
		_name = name;
		_datastores = datastores;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	protected UsageAwareDataContextProvider createDataContextProvider() {
		List<DataContext> dataContexts = new ArrayList<DataContext>(_datastores.size());
		Closeable[] closeables = new Closeable[_datastores.size()];
		for (int i = 0; i < closeables.length; i++) {
			Datastore datastore = _datastores.get(i);
			DataContextProvider dcp = datastore.getDataContextProvider();
			closeables[i] = dcp;
			DataContext dc = dcp.getDataContext();
			dataContexts.add(dc);
		}
		return new SingleDataContextProvider(DataContextFactory.createCompositeDataContext(dataContexts), this, closeables);
	}

}
