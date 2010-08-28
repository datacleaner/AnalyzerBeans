package org.eobjects.analyzer.configuration;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;

public class JaxbConfigurationFactoryTest extends TestCase {

	public void testValidConfiguration() throws Exception {
		AnalyzerBeansConfiguration configuration = new JaxbConfigurationFactory()
				.create(new File(
						"src/test/resources/example-configuration-valid.xml"));

		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		assertEquals(
				"[mydb_jndi, persons_csv, composite_datastore, my database]",
				Arrays.toString(datastoreCatalog.getDatastoreNames()));

		assertTrue(configuration.getTaskRunner() instanceof SingleThreadedTaskRunner);
	}
}
