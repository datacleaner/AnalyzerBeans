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
package org.eobjects.analyzer.connection;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;

public class UsageAwareDatastoreTest extends TestCase {

	public void testSerializationAndDeserializationOfAllDatastoreTypes() throws Exception {
		JaxbConfigurationReader reader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-datastore-types.xml"));
		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();

		String[] datastoreNames = datastoreCatalog.getDatastoreNames();
		for (String name : datastoreNames) {
			Datastore ds = datastoreCatalog.getDatastore(name);

			if (ds instanceof UsageAwareDatastore) {
				System.out.println("Cloning datastore: " + ds);
				Object clone = SerializationUtils.clone(ds);
				assertEquals(ds, clone);
			}
		}
	}
}
