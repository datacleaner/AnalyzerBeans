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
package org.eobjects.analyzer.configuration;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.connection.MongoDbDatastore;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.storage.BerkeleyDbStorageProvider;
import org.eobjects.analyzer.storage.CombinedStorageProvider;
import org.eobjects.analyzer.storage.HsqldbStorageProvider;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.metamodel.DataContext;
import org.junit.Assert;

public class JaxbConfigurationReaderTest extends TestCase {

	private final JaxbConfigurationReader reader = new JaxbConfigurationReader();
	private DatastoreCatalog _datastoreCatalog;

	public void testValidConfiguration() throws Exception {
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-valid.xml"));

		DatastoreCatalog datastoreCatalog = getDataStoreCatalog(configuration);
		assertEquals("[composite_datastore, my database, mydb_jndi, persons_csv]",
				Arrays.toString(datastoreCatalog.getDatastoreNames()));

		assertTrue(configuration.getTaskRunner() instanceof SingleThreadedTaskRunner);
	}

	public void testCombinedStorage() throws Exception {
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-combined-storage.xml"));
		StorageProvider storageProvider = configuration.getStorageProvider();

		assertEquals(CombinedStorageProvider.class, storageProvider.getClass());

		CombinedStorageProvider csp = (CombinedStorageProvider) storageProvider;
		assertEquals(BerkeleyDbStorageProvider.class, csp.getCollectionsStorageProvider().getClass());
		assertEquals(HsqldbStorageProvider.class, csp.getRowAnnotationsStorageProvider().getClass());
	}

	public void testAllDatastoreTypes() throws Exception {
		DatastoreCatalog datastoreCatalog = getDataStoreCatalog(getConfiguration());
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();
		assertEquals(
				"[my mongo, my_access, my_composite, my_csv, my_custom, my_dbase, my_excel_2003, my_fixed_width_1, my_fixed_width_2, my_jdbc_connection, my_jdbc_datasource, my_odb, my_sas, my_xml]",
				Arrays.toString(datastoreNames));

		assertEquals("a mongo db based datastore", datastoreCatalog.getDatastore("my mongo").getDescription());
		assertEquals("jdbc_con", datastoreCatalog.getDatastore("my_jdbc_connection").getDescription());
		assertEquals("jdbc_ds", datastoreCatalog.getDatastore("my_jdbc_datasource").getDescription());
		assertEquals("dbf", datastoreCatalog.getDatastore("my_dbase").getDescription());
		assertEquals("csv", datastoreCatalog.getDatastore("my_csv").getDescription());
		assertEquals("xml", datastoreCatalog.getDatastore("my_xml").getDescription());
		assertEquals("custom", datastoreCatalog.getDatastore("my_custom").getDescription());
		assertEquals("odb", datastoreCatalog.getDatastore("my_odb").getDescription());
		assertEquals("xls", datastoreCatalog.getDatastore("my_excel_2003").getDescription());
		assertEquals("comp", datastoreCatalog.getDatastore("my_composite").getDescription());
		assertEquals("mdb", datastoreCatalog.getDatastore("my_access").getDescription());
		assertEquals("folder of sas7bdat files", datastoreCatalog.getDatastore("my_sas").getDescription());
		
		MongoDbDatastore mongoDbDatastore = (MongoDbDatastore) datastoreCatalog.getDatastore("my mongo");
		assertEquals("analyzerbeans_test", mongoDbDatastore.getDatabaseName());
		assertEquals("localhost", mongoDbDatastore.getHostname());
		assertEquals(27017, mongoDbDatastore.getPort());

		FixedWidthDatastore ds = (FixedWidthDatastore) datastoreCatalog.getDatastore("my_fixed_width_1");
		assertEquals(19, ds.getFixedValueWidth());
		assertEquals("[]", Arrays.toString(ds.getValueWidths()));

		ds = (FixedWidthDatastore) datastoreCatalog.getDatastore("my_fixed_width_2");
		assertEquals(-1, ds.getFixedValueWidth());
		assertEquals("[4, 17, 19]", Arrays.toString(ds.getValueWidths()));

		for (String name : datastoreNames) {
			// test that all connections, except the JNDI-based on will work
			if (!"my_jdbc_datasource".equals(name)) {
				Datastore datastore = datastoreCatalog.getDatastore(name);
				DataContext dc = datastore.openConnection().getDataContext();
				assertNotNull(dc);
			}
		}

		Datastore compositeDatastore = datastoreCatalog.getDatastore("my_composite");
		DatastoreConnection con = compositeDatastore.openConnection();
		DataContext dataContext = con.getDataContext();
		String[] schemaNames = dataContext.getSchemaNames();
		assertEquals("[INFORMATION_SCHEMA, PUBLIC, Spreadsheet2003.xls, developers.mdb, employees.csv, information_schema]",
				Arrays.toString(schemaNames));
		con.close();
	}

	private AnalyzerBeansConfiguration getConfiguration() {
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-datastore-types.xml"));
		return configuration;
	}

	private DatastoreCatalog getDataStoreCatalog(AnalyzerBeansConfiguration configuration) {
		_datastoreCatalog = configuration.getDatastoreCatalog();
		return _datastoreCatalog;
	}

	public void testReferenceDataCatalog() throws Exception {
		AnalyzerBeansConfiguration conf = getConfigurationFromXMLFile();
		ReferenceDataCatalog referenceDataCatalog = conf.getReferenceDataCatalog();
		String[] dictionaryNames = referenceDataCatalog.getDictionaryNames();
		assertEquals("[custom_dict, datastore_dict, textfile_dict, valuelist_dict]", Arrays.toString(dictionaryNames));

		LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(conf.getInjectionManagerFactory().getInjectionManager(null));

		Dictionary d = referenceDataCatalog.getDictionary("datastore_dict");
		assertEquals("dict_ds", d.getDescription());
		lifeCycleHelper.initialize(d);
		assertTrue(d.containsValue("Patterson"));
		assertTrue(d.containsValue("Murphy"));
		assertFalse(d.containsValue("Gates"));

		d = referenceDataCatalog.getDictionary("textfile_dict");
		assertEquals("dict_txt", d.getDescription());
		lifeCycleHelper.initialize(d);
		assertTrue(d.containsValue("Patterson"));
		assertFalse(d.containsValue("Murphy"));
		assertTrue(d.containsValue("Gates"));

		d = referenceDataCatalog.getDictionary("valuelist_dict");
		assertEquals("dict_simple", d.getDescription());
		lifeCycleHelper.initialize(d);
		assertFalse(d.containsValue("Patterson"));
		assertFalse(d.containsValue("Murphy"));
		assertTrue(d.containsValue("greetings"));

		d = referenceDataCatalog.getDictionary("custom_dict");
		assertEquals("dict_custom", d.getDescription());
		lifeCycleHelper.initialize(d);
		assertFalse(d.containsValue("Patterson"));
		assertFalse(d.containsValue("Murphy"));
		assertFalse(d.containsValue("Gates"));
		assertTrue(d.containsValue("value0"));
		assertTrue(d.containsValue("value1"));
		assertTrue(d.containsValue("value2"));
		assertTrue(d.containsValue("value3"));
		assertTrue(d.containsValue("value4"));
		assertFalse(d.containsValue("value5"));

		String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();
		assertEquals("[custom_syn, datastore_syn, textfile_syn]", Arrays.toString(synonymCatalogNames));

		SynonymCatalog s = referenceDataCatalog.getSynonymCatalog("textfile_syn");
		assertEquals("syn_txt", s.getDescription());
		lifeCycleHelper.initialize(s);
		assertEquals("DNK", s.getMasterTerm("Denmark"));
		assertEquals("DNK", s.getMasterTerm("Danmark"));
		assertEquals("DNK", s.getMasterTerm("DK"));
		assertEquals("ALB", s.getMasterTerm("Albania"));
		assertEquals(null, s.getMasterTerm("Netherlands"));

		s = referenceDataCatalog.getSynonymCatalog("datastore_syn");
		assertEquals("syn_ds", s.getDescription());
		lifeCycleHelper.initialize(s);

		// lookup by id
		assertEquals("La Rochelle Gifts", s.getMasterTerm("119"));
		// lookup by phone number (string)
		assertEquals("Danish Wholesale Imports", s.getMasterTerm("31 12 3555"));
		assertEquals(null, s.getMasterTerm("foobar"));

		s = referenceDataCatalog.getSynonymCatalog("custom_syn");
		assertEquals("syn_custom", s.getDescription());
		lifeCycleHelper.initialize(s);
		assertEquals("DNK", s.getMasterTerm("Denmark"));
		assertEquals("DNK", s.getMasterTerm("Danmark"));
		assertEquals(null, s.getMasterTerm("DK"));
		assertEquals(null, s.getMasterTerm("Albania"));
		assertEquals("NLD", s.getMasterTerm("Netherlands"));

		String[] stringPatternNames = referenceDataCatalog.getStringPatternNames();
		assertEquals("[regex danish email, simple email]", Arrays.toString(stringPatternNames));

		StringPattern pattern = referenceDataCatalog.getStringPattern("regex danish email");
		assertEquals("pattern_reg", pattern.getDescription());
		lifeCycleHelper.initialize(pattern);
		assertEquals("RegexStringPattern[name=regex danish email, expression=[a-z]+@[a-z]+\\.dk, matchEntireString=true]",
				pattern.toString());
		assertTrue(pattern.matches("kasper@eobjects.dk"));
		assertFalse(pattern.matches("kasper@eobjects.org"));
		assertFalse(pattern.matches(" kasper@eobjects.dk"));

		pattern = referenceDataCatalog.getStringPattern("simple email");
		assertEquals("pattern_simple", pattern.getDescription());
		lifeCycleHelper.initialize(pattern);
		assertEquals("SimpleStringPattern[name=simple email, expression=aaaa@aaaaa.aa]", pattern.toString());
		assertTrue(pattern.matches("kasper@eobjects.dk"));
		assertTrue(pattern.matches("kasper@eobjects.org"));
		assertFalse(pattern.matches(" kasper@eobjects.dk"));
	}

	public void testCustomDictionaryWithInjectedDatastore() {
		AnalyzerBeansConfiguration configuration = getConfigurationFromXMLFile();
		ReferenceDataCatalog referenceDataCatalog = configuration.getReferenceDataCatalog();
		SampleCustomDictionary sampleCustomDictionary = (SampleCustomDictionary) referenceDataCatalog
				.getDictionary("custom_dict");
		Assert.assertEquals("my_jdbc_connection", sampleCustomDictionary.datastore.getName());
	}

	private AnalyzerBeansConfiguration getConfigurationFromXMLFile() {
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-reference-data-types.xml"));
		return configuration;
	}
}
