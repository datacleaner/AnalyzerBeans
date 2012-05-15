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

import java.sql.Connection;
import java.sql.DriverManager;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.util.FileHelper;

public class TestDatastoreConnection implements DatastoreConnection {

    private final Connection _connection;
    private final UpdateableDataContext _dataContext;
    private final Datastore _datastore;

    public TestDatastoreConnection(Datastore datastore) throws Exception {
        _datastore = datastore;
        Class.forName("org.hsqldb.jdbcDriver");
        _connection = DriverManager.getConnection("jdbc:hsqldb:res:orderdb;readonly=true");
        _dataContext = DataContextFactory.createJdbcDataContext(_connection);
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
        FileHelper.safeClose(_connection);
    }

}
