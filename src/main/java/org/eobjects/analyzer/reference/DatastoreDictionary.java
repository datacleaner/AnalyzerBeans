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
package org.eobjects.analyzer.reference;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

/**
 * A dictionary backed by a column in a datastore.
 * 
 * Note that even though this datastore <i>is</i> serializable it is not
 * entirely able to gracefully deserialize. The user of the dictionary will have
 * to inject the DatastoreCatalog using the setter method for this.
 * 
 * @author Kasper Sørensen
 * 
 */
public class DatastoreDictionary implements Dictionary {

	private static final long serialVersionUID = 1L;

	private transient DatastoreCatalog _datastoreCatalog;
	private transient ReferenceValues<String> _cachedRefValues;
	private final String _datastoreName;
	private final String _qualifiedColumnName;
	private final String _name;

	public DatastoreDictionary(String name, DatastoreCatalog datastoreCatalog, String datastoreName,
			String qualifiedColumnName) {
		_name = name;
		_datastoreCatalog = datastoreCatalog;
		_datastoreName = datastoreName;
		_qualifiedColumnName = qualifiedColumnName;
	}

	public void setDatastoreCatalog(DatastoreCatalog datastoreCatalog) {
		_datastoreCatalog = datastoreCatalog;
	}

	public DatastoreCatalog getDatastoreCatalog() {
		return _datastoreCatalog;
	}

	public String getDatastoreName() {
		return _datastoreName;
	}

	public String getQualifiedColumnName() {
		return _qualifiedColumnName;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean containsValue(String value) {
		// note that caching IS enabled because the ReferenceValues object
		// returned by getValues() contains a cache!
		return getValues().containsValue(value);
	}

	public ReferenceValues<String> getValues() {
		if (_cachedRefValues == null) {
			synchronized (this) {
				if (_cachedRefValues == null) {
					Datastore datastore = _datastoreCatalog.getDatastore(_datastoreName);
					if (datastore == null) {
						throw new IllegalStateException("Could not resolve datastore " + _datastoreName);
					}

					DataContextProvider dataContextProvider = datastore.getDataContextProvider();
					SchemaNavigator schemaNavigator = dataContextProvider.getSchemaNavigator();
					Column column = schemaNavigator.convertToColumns(new String[] { _qualifiedColumnName })[0];
					if (column == null) {
						throw new IllegalStateException("Could not resolve column " + _qualifiedColumnName);
					}

					_cachedRefValues = new DatastoreReferenceValues(dataContextProvider, column);
				}
			}
		}
		return _cachedRefValues;
	}

}
