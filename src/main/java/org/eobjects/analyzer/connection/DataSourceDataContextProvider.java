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

import javax.sql.DataSource;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class DataSourceDataContextProvider implements DataContextProvider {

	private final DataContext _dataContext;
	private final SchemaNavigator _schemaNavigator;
	private final Datastore _datastore;

	public DataSourceDataContextProvider(DataSource ds, Datastore datastore) {
		this._dataContext = DataContextFactory.createJdbcDataContext(ds);
		this._schemaNavigator = new SchemaNavigator(_dataContext);
		this._datastore = datastore;
	}

	@Override
	public DataContext getDataContext() {
		return _dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return _schemaNavigator;
	}

	@Override
	public Datastore getDatastore() {
		return _datastore;
	}

}
