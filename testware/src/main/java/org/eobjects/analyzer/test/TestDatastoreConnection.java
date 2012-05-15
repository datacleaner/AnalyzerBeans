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
package org.eobjects.analyzer.test;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;

final class TestDatastoreConnection implements DatastoreConnection {

    private final DataContext _dataContext;
    private final Datastore _datastore;

    public TestDatastoreConnection(TestDatastore datastore) throws Exception {
        _datastore = datastore;
        _dataContext = DataContextFactory.createJdbcDataContext(datastore.getDataSource());
    }

    @Override
    public DataContext getDataContext() {
        return _dataContext;
    }

    @Override
    public SchemaNavigator getSchemaNavigator() {
        return new SchemaNavigator(getDataContext());
    }

    @Override
    public Datastore getDatastore() {
        return _datastore;
    }

    @Override
    public void close() {
    }

}
