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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.builder.SatisfiedWhereBuilder;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class DatastoreSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatastoreSynonymCatalog.class);

	private transient WeakHashMap<String, String> _masterTermCache = new WeakHashMap<String, String>();
	private transient DatastoreCatalog _datastoreCatalog;
	private transient BlockingQueue<DataContextProvider> _dataContextProviders = new LinkedBlockingQueue<DataContextProvider>();
	private final String _name;
	private final String _datastoreName;
	private final String _masterTermQualifiedColumnName;
	private final String[] _synonymQualifiedColumnNames;

	public DatastoreSynonymCatalog(String name, DatastoreCatalog datastoreCatalog, String datastoreName,
			String masterTermQualifiedColumnName, String[] synonymQualifiedColumnNames) {
		_name = name;
		_datastoreCatalog = datastoreCatalog;
		_datastoreName = datastoreName;
		_masterTermQualifiedColumnName = masterTermQualifiedColumnName;
		_synonymQualifiedColumnNames = synonymQualifiedColumnNames;
	}

	public void setDatastoreCatalog(DatastoreCatalog datastoreCatalog) {
		_datastoreCatalog = datastoreCatalog;
	}

	/**
	 * Initializes a DataContextProvider, which will keep the connection open
	 */
	@Initialize
	public void init() {
		logger.info("Initializing dictionary: {}", this);
		Datastore datastore = getDatastore();
		DataContextProvider dcp = datastore.getDataContextProvider();
		getDataContextProviders().add(dcp);
	}

	/**
	 * Closes a DataContextProvider, potentially closing the connection (if no
	 * other DataContextProviders are open).
	 */
	@Close
	public void close() {
		DataContextProvider dcp = getDataContextProviders().poll();
		if (dcp != null) {
			logger.info("Closing dictionary: {}", this);
			dcp.close();
		}
	}

	private WeakHashMap<String, String> getMasterTermCache() {
		if (_masterTermCache == null) {
			synchronized (this) {
				if (_masterTermCache == null) {
					_masterTermCache = new WeakHashMap<String, String>();
				}
			}
		}
		return _masterTermCache;
	}

	private BlockingQueue<DataContextProvider> getDataContextProviders() {
		if (_dataContextProviders == null) {
			synchronized (this) {
				if (_dataContextProviders == null) {
					_dataContextProviders = new LinkedBlockingQueue<DataContextProvider>();
				}
			}
		}
		return _dataContextProviders;
	}

	private Datastore getDatastore() {
		Datastore datastore = _datastoreCatalog.getDatastore(_datastoreName);
		if (datastore == null) {
			throw new IllegalStateException("Could not resolve datastore " + _datastoreName);
		}
		return datastore;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Collection<Synonym> getSynonyms() {

		Datastore datastore = getDatastore();

		DataContextProvider dataContextProvider = datastore.getDataContextProvider();
		DataContext dataContext = dataContextProvider.getDataContext();

		SchemaNavigator sn = dataContextProvider.getSchemaNavigator();

		Column masterTermColumn = sn.convertToColumn(_masterTermQualifiedColumnName);
		Column[] columns = sn.convertToColumns(_synonymQualifiedColumnNames);

		Table table = masterTermColumn.getTable();

		Query query = dataContext.query().from(table.getName()).select(masterTermColumn).select(columns).toQuery();
		DataSet results = dataContext.executeQuery(query);

		List<Synonym> synonyms = new ArrayList<Synonym>();

		while (results.next()) {
			Row row = results.getRow();
			synonyms.add(new SimpleSynonym(getMasterTerm(row, masterTermColumn), getSynonyms(row, table.getColumns())));
		}
		dataContextProvider.close();
		return synonyms;
	}

	@Override
	public String getMasterTerm(String term) {
		if (term == null) {
			return null;
		}

		WeakHashMap<String, String> cache = getMasterTermCache();
		if (cache.containsKey(term)) {
			return cache.get(term);
		}

		Datastore datastore = getDatastore();

		DataContextProvider dataContextProvider = datastore.getDataContextProvider();

		SchemaNavigator sn = dataContextProvider.getSchemaNavigator();

		Column masterTermColumn = sn.convertToColumn(_masterTermQualifiedColumnName);
		Column[] columns = sn.convertToColumns(_synonymQualifiedColumnNames);

		DataContext dataContext = dataContextProvider.getDataContext();
		Table table = masterTermColumn.getTable();

		// create a query that gets the master term where any of the synonym
		// columns are equal to the synonym
		SatisfiedWhereBuilder<?> qb = dataContext.query().from(table.getName()).select(masterTermColumn).where(columns[0])
				.equals(term);
		for (int i = 1; i < columns.length; i++) {
			qb = qb.or(columns[i]).equals(term);
		}
		Query query = qb.toQuery();
		DataSet dataSet = dataContext.executeQuery(query);
		String result = null;

		if (dataSet.next()) {
			Row row = dataSet.getRow();
			result = getMasterTerm(row, masterTermColumn);
		}
		dataSet.close();
		dataContextProvider.close();

		cache.put(term, result);
		return result;
	}

	private String getMasterTerm(Row row, Column column) {
		return (String) row.getValue(column);
	}

	private String[] getSynonyms(Row row, Column[] columns) {
		List<String> synonyms = new ArrayList<String>();
		for (Column synonymColumn : columns) {
			String value = (String) row.getValue(synonymColumn);
			synonyms.add(value);
		}
		return synonyms.toArray(new String[0]);
	}

}
