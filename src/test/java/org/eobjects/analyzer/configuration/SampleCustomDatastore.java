package org.eobjects.analyzer.configuration;

import java.io.File;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.junit.Ignore;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

@Ignore
public class SampleCustomDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	File xmlFile;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		DataContext dc = DataContextFactory.createXmlDataContext(xmlFile, false, false);
		return new SingleDataContextProvider(dc, this);
	}

	@Override
	public void close() {
	}

}
