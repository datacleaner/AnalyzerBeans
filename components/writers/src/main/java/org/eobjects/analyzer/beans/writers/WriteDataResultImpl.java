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
package org.eobjects.analyzer.beans.writers;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Func;

/**
 * Default implementation of {@link WriteDataResult}.
 * 
 * @author Kasper Sørensen
 */
public final class WriteDataResultImpl implements WriteDataResult {

	private static final long serialVersionUID = 1L;

	private final int _writtenRowCount;
	private final Func<DatastoreCatalog, Datastore> _datastoreFunc;
	private final String _schemaName;
	private final String _tableName;

	public WriteDataResultImpl(final int writtenRowCount,
			final Datastore datastore, final String schemaName,
			final String tableName) {
		_writtenRowCount = writtenRowCount;
		_schemaName = schemaName;
		_tableName = tableName;
		_datastoreFunc = new Func<DatastoreCatalog, Datastore>() {
			@Override
			public Datastore eval(DatastoreCatalog catalog) {
				return datastore;
			}
		};
	}

	public WriteDataResultImpl(final int writtenRowCount,
			final String datastoreName, final String schemaName,
			final String tableName) {
		_writtenRowCount = writtenRowCount;
		_schemaName = schemaName;
		_tableName = tableName;
		_datastoreFunc = new Func<DatastoreCatalog, Datastore>() {
			@Override
			public Datastore eval(DatastoreCatalog catalog) {
				return catalog.getDatastore(datastoreName);
			}
		};
	}

	@Override
	public int getWrittenRowCount() {
		return _writtenRowCount;
	}

	@Override
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
		return _datastoreFunc.eval(datastoreCatalog);
	}

	@Override
	public Table getPreviewTable(Datastore datastore) {
		DatastoreConnection con = datastore.openConnection();
		try {
			return con.getSchemaNavigator().convertToTable(_schemaName,
					_tableName);
		} finally {
			con.close();
		}
	}

	@Override
	public String toString() {
		return _writtenRowCount + " records written to table";
	}
}
