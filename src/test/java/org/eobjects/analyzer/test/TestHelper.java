/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleSynonym;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.analyzer.storage.StorageProvider;
import org.junit.Ignore;

@Ignore
public final class TestHelper {

	private static final DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
			"org.eobjects.analyzer.beans", true).scanPackage("org.eobjects.analyzer.result.renderer", true);

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration(Datastore datastore) {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		return createAnalyzerBeansConfiguration(taskRunner, datastore);
	}

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration() {
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		StorageProvider storageProvider = createStorageProvider();
		return new AnalyzerBeansConfigurationImpl(createDatastoreCatalog(), createReferenceDataCatalog(),
				descriptorProvider, taskRunner, storageProvider);
	}

	public static StorageProvider createStorageProvider() {
		return new InMemoryStorageProvider();
	}

	public static AnalyzerBeansConfiguration createAnalyzerBeansConfiguration(TaskRunner taskRunner, Datastore datastore) {
		StorageProvider storageProvider = createStorageProvider();

		List<Datastore> datastores = new LinkedList<Datastore>();
		datastores.add(datastore);

		return new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(datastores), createReferenceDataCatalog(),
				descriptorProvider, taskRunner, storageProvider);
	}

	public static ReferenceDataCatalog createReferenceDataCatalog() {
		Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
		dictionaries.add(new SimpleDictionary("eobjects.org products", "MetaModel", "DataCleaner", "AnalyzerBeans"));
		dictionaries.add(new SimpleDictionary("apache products", "commons-lang", "commons-math", "commons-codec",
				"commons-logging"));
		dictionaries.add(new SimpleDictionary("logging products", "commons-logging", "log4j", "slf4j", "java.util.Logging"));

		Collection<SynonymCatalog> synonymCatalogs = new ArrayList<SynonymCatalog>();
		synonymCatalogs.add(new SimpleSynonymCatalog("translated terms", new SimpleSynonym("hello", "howdy", "hi", "yo",
				"hey"), new SimpleSynonym("goodbye", "bye", "see you", "hey")));

		Collection<StringPattern> stringPatterns = new ArrayList<StringPattern>();

		return new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs, stringPatterns);
	}

	public static DatastoreCatalog createDatastoreCatalog() {
		List<Datastore> datastores = new LinkedList<Datastore>();
		return new DatastoreCatalogImpl(datastores);
	}

	public static JdbcDatastore createSampleDatabaseDatastore(String name) {
		return new JdbcDatastore(name, "jdbc:hsqldb:res:metamodel", "org.hsqldb.jdbcDriver");
	}
}
