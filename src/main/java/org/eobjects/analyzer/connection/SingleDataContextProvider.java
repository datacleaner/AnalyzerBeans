package org.eobjects.analyzer.connection;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public class SingleDataContextProvider implements DataContextProvider {

	private final DataContext dataContext;
	private final SchemaNavigator schemaNavigator;

	public SingleDataContextProvider(DataContext dataContext) {
		this.dataContext = dataContext;
		this.schemaNavigator = new SchemaNavigator(dataContext);
	}

	@Override
	public DataContext getDataContext() {
		return this.dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return this.schemaNavigator;
	}

}
