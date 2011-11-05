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

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.mongodb.MongoDbDataContext;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDbDatastore extends UsageAwareDatastore<UpdateableDataContext> implements UpdateableDatastore {

	private static final long serialVersionUID = 1L;

	private final String _hostname;
	private final int _port;
	private final String _databaseName;

	public MongoDbDatastore(String name, String databaseName) {
		this(name, null, null, databaseName);
	}

	public MongoDbDatastore(String name, String hostname, Integer port, String databaseName) {
		super(name);
		if (StringUtils.isNullOrEmpty(databaseName)) {
			throw new IllegalArgumentException("Database name cannot be null");
		}
		if (StringUtils.isNullOrEmpty(hostname)) {
			// default Mongo host
			hostname = "localhost";
		}
		if (port == null) {
			// default Mongo port
			port = 27017;
		}
		_hostname = hostname;
		_port = port;
		_databaseName = databaseName;
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(true);
	}

	@Override
	protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
		try {
			Mongo mongo = new Mongo(_hostname, _port);
			DB mongoDb = mongo.getDB(_databaseName);
			UpdateableDataContext dataContext = new MongoDbDataContext(mongoDb);
			return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dataContext, this);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new IllegalStateException("Failed to connect to MongoDB instance: " + e.getMessage(), e);
		}
	}

	@Override
	public UpdateableDatastoreConnection openConnection() {
		DatastoreConnection connection = super.openConnection();
		return (UpdateableDatastoreConnection) connection;
	}

	public String getHostname() {
		return _hostname;
	}

	public int getPort() {
		return _port;
	}

	public String getDatabaseName() {
		return _databaseName;
	}
}
