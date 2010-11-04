package org.eobjects.analyzer.storage;


/**
 * H2 based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class H2StorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	public H2StorageProvider() {
		super("org.h2.Driver", "jdbc:h2:mem:");
	}
}
