package org.eobjects.analyzer.storage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hsqldb based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class HsqldbStorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	private static final AtomicInteger databaseIndex = new AtomicInteger(0);

	public HsqldbStorageProvider() {
		super("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:ab" + databaseIndex.getAndIncrement(), "SA", "");
	}

	public HsqldbStorageProvider(int inMemoryThreshold) {
		super(inMemoryThreshold, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:ab" + databaseIndex.getAndIncrement(), "SA", "");
	}

	public HsqldbStorageProvider(String directoryPath) {
		super("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + directoryPath, "SA", "");
	}

	public HsqldbStorageProvider(int inMemoryThreshold, String directoryPath) {
		super(inMemoryThreshold, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + directoryPath, "SA", "");
	}
}
