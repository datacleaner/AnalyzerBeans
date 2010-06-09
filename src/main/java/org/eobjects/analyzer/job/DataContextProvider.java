package org.eobjects.analyzer.job;

import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.DataContext;

public interface DataContextProvider {

	public DataContext getDataContext();

	public SchemaNavigator getSchemaNavigator();
}
