package org.eobjects.analyzer.connection;

import java.io.Serializable;

public interface DatastoreCatalog extends Serializable {

	public String[] getDatastoreNames();

	public Datastore getDatastore(String name);
}
