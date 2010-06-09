package org.eobjects.analyzer.connection;

public interface DatastoreCatalog {

	public String[] getDatastoreNames();

	public Datastore getDatastore(String name);
}
