package org.eobjects.analyzer.reference;

import java.io.Serializable;

import javax.inject.Inject;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

public class DatastoreDictionary implements Dictionary, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private transient DatastoreCatalog datastoreCatalog;

	private String _datastoreName;
	private String _qualifiedColumnName;
	private String _name;

	public String getDatastoreName() {
		return _datastoreName;
	}

	public void setDatastoreName(String datastoreName) {
		_datastoreName = datastoreName;
	}

	public String getQualifiedColumnName() {
		return _qualifiedColumnName;
	}

	public void setQualifiedColumnName(String qualifiedColumnName) {
		_qualifiedColumnName = qualifiedColumnName;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public ReferenceValues<String> getValues() {
		Datastore datastore = datastoreCatalog.getDatastore(_datastoreName);
		if (datastore == null) {
			throw new IllegalStateException("Could not resolve datastore "
					+ _datastoreName);
		}

		DataContextProvider dataContextProvider = datastore
				.getDataContextProvider();
		SchemaNavigator schemaNavigator = dataContextProvider
				.getSchemaNavigator();
		Column column = schemaNavigator
				.convertToColumns(new String[] { _qualifiedColumnName })[0];
		if (column == null) {
			throw new IllegalStateException("Could not resolve column "
					+ _qualifiedColumnName);
		}

		return new DatastoreReferenceValues(dataContextProvider, column);
	}

}
