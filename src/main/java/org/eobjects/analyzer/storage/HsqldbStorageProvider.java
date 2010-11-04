package org.eobjects.analyzer.storage;


/**
 * Hsqldb based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class HsqldbStorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	public HsqldbStorageProvider() {
		super("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:analyzerbeans", "SA", "");
	}
}
