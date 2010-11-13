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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class CompositeDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(CompositeDatastore.class);

	private String _name;
	private List<Datastore> _datastores;

	public CompositeDatastore(String name, List<Datastore> datastores) {
		_name = name;
		_datastores = datastores;
	}

	@Override
	public void close() {
		for (Datastore datastore : _datastores) {
			try {
				datastore.close();
			} catch (Throwable t) {
				logger.warn("Error closing child datastore: " + datastore.getName(), t);
			}
		}
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		List<DataContext> dataContexts = new ArrayList<DataContext>(_datastores.size());
		for (Datastore datastore : _datastores) {
			DataContext dc = datastore.getDataContextProvider().getDataContext();
			dataContexts.add(dc);
		}
		return new SingleDataContextProvider(DataContextFactory.createCompositeDataContext(dataContexts), this);
	}

}
