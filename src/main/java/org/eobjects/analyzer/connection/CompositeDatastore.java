package org.eobjects.analyzer.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

public class CompositeDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(CompositeDatastore.class);

	private String _name;
	private List<Datastore> _datastores;

	public CompositeDatastore(String name, List<Datastore> datastores) {
		_name = name;
		_datastores = datastores;
	}

	@Override
	public void close() throws IOException {
		for (Datastore datastore : _datastores) {
			try {
				datastore.close();
			} catch (IOException e) {
				logger.warn("Error closing child datastore: " + datastore.getName(), e);
			}
		}
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		List<DataContext> dataContexts = new ArrayList<DataContext>(_datastores.size());
		for (Datastore datastore : _datastores) {
			DataContext dc = datastore.getDataContextProvider().getDataContext();
			dataContexts.add(dc);
		}
		return new SingleDataContextProvider(DataContextFactory.createCompositeDataContext(dataContexts), this);
	}

}
