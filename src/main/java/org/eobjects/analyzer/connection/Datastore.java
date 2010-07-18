package org.eobjects.analyzer.connection;

import java.io.Closeable;
import java.io.Serializable;

public interface Datastore extends Closeable, Serializable {

	public String getName();
	
	public DataContextProvider getDataContextProvider();
}
