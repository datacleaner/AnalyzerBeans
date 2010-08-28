package org.eobjects.analyzer.connection;

import javax.sql.DataSource;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class DataSourceDataContextProvider implements DataContextProvider {

	private final DataContext dataContext;
	private final SchemaNavigator schemaNavigator;

	public DataSourceDataContextProvider(DataSource ds) {
		this.dataContext = DataContextFactory.createJdbcDataContext(ds);
		this.schemaNavigator = new SchemaNavigator(dataContext);
	}

	@Override
	public DataContext getDataContext() {
		return dataContext;
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return schemaNavigator;
	}

}
