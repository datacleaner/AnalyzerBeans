package org.eobjects.analyzer.configuration;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;

import dk.eobjects.metamodel.DataContext;

public class JaxbConfigurationFactoryTest extends TestCase {

	private JaxbConfigurationFactory factory = new JaxbConfigurationFactory();

	public void testValidConfiguration() throws Exception {
		AnalyzerBeansConfiguration configuration = factory.create(new File(
				"src/test/resources/example-configuration-valid.xml"));

		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		assertEquals("[mydb_jndi, persons_csv, composite_datastore, my database]",
				Arrays.toString(datastoreCatalog.getDatastoreNames()));

		assertTrue(configuration.getTaskRunner() instanceof SingleThreadedTaskRunner);
	}

	public void testAllDatastoreTypes() throws Exception {
		AnalyzerBeansConfiguration configuration = factory.create(new File(
				"src/test/resources/example-configuration-all-datastore-types.xml"));
		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();
		assertEquals("[my_jdbc_connection, my_csv, my_jdbc_datasource, my_excel_2003, my_composite, my_access]",
				Arrays.toString(datastoreNames));

		for (String name : datastoreNames) {
			// test that all connections, except the JNDI-based on will work
			if (!"my_jdbc_datasource".equals(name)) {
				Datastore datastore = datastoreCatalog.getDatastore(name);
				DataContext dc = datastore.getDataContextProvider().getDataContext();
				assertNotNull(dc);
			}
		}

		Datastore compositeDatastore = datastoreCatalog.getDatastore("my_composite");
		DataContext dataContext = compositeDatastore.getDataContextProvider().getDataContext();
		assertEquals("[INFORMATION_SCHEMA, PUBLIC, Spreadsheet2003.xls, developers.mdb, employees.csv, information_schema]",
				Arrays.toString(dataContext.getSchemaNames()));
	}
}
