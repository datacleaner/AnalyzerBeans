package org.eobjects.analyzer.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hsqldb based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class HsqldbStorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	private static final AtomicInteger databaseIndex = new AtomicInteger(0);
	private static final String DATABASE_FILENAME = "hsqldb-storage";

	public HsqldbStorageProvider() {
		super("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:ab" + databaseIndex.getAndIncrement(), "SA", "");
	}

	public HsqldbStorageProvider(int inMemoryThreshold) {
		super(inMemoryThreshold, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:ab" + databaseIndex.getAndIncrement(), "SA", "");
	}

	public HsqldbStorageProvider(String directoryPath) {

		super("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + cleanDirectory(directoryPath) + File.separatorChar
				+ DATABASE_FILENAME, "SA", "");
	}

	public HsqldbStorageProvider(int inMemoryThreshold, String directoryPath) {
		super(inMemoryThreshold, "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + cleanDirectory(directoryPath)
				+ File.separatorChar + DATABASE_FILENAME, "SA", "");
	}

	private static String cleanDirectory(String directoryPath) {
		File file = new File(directoryPath);
		if (file.exists() && file.isDirectory()) {
			// remove all files from previous uncleansed database
			File[] dbFiles = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(DATABASE_FILENAME);
				}
			});
			for (File f : dbFiles) {
				f.delete();
			}
		}
		return directoryPath;
	}
}
