package org.eobjects.analyzer.connection;

import java.util.Collection;

public class DatastoreCatalogImpl implements DatastoreCatalog {

	private Collection<Datastore> _datastores;

	public DatastoreCatalogImpl(Collection<Datastore> datastores) {
		_datastores = datastores;
	}

	@Override
	public String[] getDatastoreNames() {
		String[] names = new String[_datastores.size()];
		int i = 0;
		for (Datastore ds : _datastores) {
			names[i] = ds.getName();
		}
		return names;
	}

	@Override
	public Datastore getDatastore(String name) {
		if (name != null) {
			for (Datastore ds : _datastores) {
				if (name.equals(ds.getName())) {
					return ds;
				}
			}
		}
		return null;
	}
}
