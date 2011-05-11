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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.FilterItem;
import org.eobjects.metamodel.query.OperatorType;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatastoreSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatastoreSynonymCatalog.class);

	private transient Map<String, String> _masterTermCache;
	private transient DatastoreCatalog _datastoreCatalog;
	private transient BlockingQueue<DataContextProvider> _dataContextProviders = new LinkedBlockingQueue<DataContextProvider>();
	private final String _datastoreName;
	private final String _masterTermColumnPath;
	private final String[] _synonymColumnPaths;

	public DatastoreSynonymCatalog(String name, String datastoreName, String masterTermColumnPath,
			String[] synonymColumnPaths) {
		super(name);
		_datastoreName = datastoreName;
		_masterTermColumnPath = masterTermColumnPath;
		_synonymColumnPaths = synonymColumnPaths;
	}

	public void setDatastoreCatalog(DatastoreCatalog datastoreCatalog) {
		_datastoreCatalog = datastoreCatalog;
	}

	/**
	 * Initializes a DataContextProvider, which will keep the connection open
	 */
	@Initialize
	public void init(DatastoreCatalog datastoreCatalog) {
		logger.info("Initializing dictionary: {}", this);
		setDatastoreCatalog(datastoreCatalog);
		Datastore datastore = getDatastore();
		DataContextProvider dataContextProvider = datastore.getDataContextProvider();
		getDataContextProviders().add(dataContextProvider);
	}

	/**
	 * Closes a DataContextProvider, potentially closing the connection (if no
	 * other DataContextProviders are open).
	 */
	@Close
	public void close() {
		DataContextProvider dataContextProvider = getDataContextProviders().poll();
		if (dataContextProvider != null) {
			logger.info("Closing dictionary: {}", this);
			dataContextProvider.close();
		}
	}

	private Map<String, String> getMasterTermCache() {
		if (_masterTermCache == null) {
			synchronized (this) {
				if (_masterTermCache == null) {
					_masterTermCache = CollectionUtils.createCacheMap();
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

	public String getDatastoreName() {
		return _datastoreName;
	}

	public String getMasterTermColumnPath() {
		return _masterTermColumnPath;
	}

	public String[] getSynonymColumnPaths() {
		return Arrays.copyOf(_synonymColumnPaths, _synonymColumnPaths.length);
	}

	@Override
	public Collection<Synonym> getSynonyms() {

		Datastore datastore = getDatastore();

		DataContextProvider dataContextProvider = datastore.getDataContextProvider();
		DataContext dataContext = dataContextProvider.getDataContext();

		SchemaNavigator schemaNavigator = dataContextProvider.getSchemaNavigator();

		Column masterTermColumn = schemaNavigator.convertToColumn(_masterTermColumnPath);
		Column[] columns = schemaNavigator.convertToColumns(_synonymColumnPaths);

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
		if (StringUtils.isNullOrEmpty(term)) {
			return null;
		}

		final Map<String, String> cache = getMasterTermCache();

		String result;

		synchronized (cache) {

			result = cache.get(term);

			if (result == null) {
				Datastore datastore = getDatastore();

				DataContextProvider dataContextProvider = datastore.getDataContextProvider();
				try {

					SchemaNavigator schemaNavigator = dataContextProvider.getSchemaNavigator();

					Column masterTermColumn = schemaNavigator.convertToColumn(_masterTermColumnPath);
					Column[] columns = schemaNavigator.convertToColumns(_synonymColumnPaths);

					DataContext dataContext = dataContextProvider.getDataContext();
					Table table = masterTermColumn.getTable();

					// create a query that gets the master term where any of the
					// synonym columns are equal to the synonym
					Query query = dataContext.query().from(table.getName()).select(masterTermColumn).toQuery();
					List<FilterItem> filterItems = new ArrayList<FilterItem>();
					for (int i = 0; i < columns.length; i++) {
						Column column = columns[i];
						if (column.getType().isNumber()) {
							Number numberValue = ConvertToNumberTransformer.transformValue(term);
							if (numberValue != null) {
								filterItems.add(new FilterItem(new SelectItem(column), OperatorType.EQUALS_TO, numberValue));
							}
						} else {
							filterItems.add(new FilterItem(new SelectItem(column), OperatorType.EQUALS_TO, term));
						}
					}
					if (filterItems.isEmpty()) {
						result = "";
					} else {
						query.where(new FilterItem(filterItems.toArray(new FilterItem[0])));

						DataSet dataSet = dataContext.executeQuery(query);

						if (dataSet.next()) {
							Row row = dataSet.getRow();
							result = getMasterTerm(row, masterTermColumn);
						} else {
							result = "";
						}
						dataSet.close();
					}
				} finally {
					dataContextProvider.close();
				}

				cache.put(term, result);
			}
		}

		if ("".equals(result)) {
			result = null;
		}

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
