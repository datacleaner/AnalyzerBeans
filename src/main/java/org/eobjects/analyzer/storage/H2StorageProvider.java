package org.eobjects.analyzer.storage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * H2 based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class H2StorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	private static final AtomicInteger databaseIndex = new AtomicInteger(0);

	public H2StorageProvider() {
		super("org.h2.Driver", "jdbc:h2:mem:ab" + databaseIndex.getAndIncrement());
	}
}
