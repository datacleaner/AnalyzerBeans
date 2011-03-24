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
package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;
import java.util.Set;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.test.TestHelper;

import junit.framework.TestCase;

public class InitializeMethodDescriptorImplTest extends TestCase {

	private boolean executed;

	public void testInitialize() throws Exception {
		executed = false;
		Method m = getClass().getDeclaredMethod("doInitialize");
		InitializeMethodDescriptorImpl initializeMethodDescriptorImpl = new InitializeMethodDescriptorImpl(m);
		initializeMethodDescriptorImpl.initialize(this, null, null);

		assertTrue(executed);

		assertEquals("InitializeMethodDescriptorImpl[method=doInitialize]", initializeMethodDescriptorImpl.toString());
	}

	public void doInitialize() {
		executed = true;
	}

	public void testInitWithCatalogs() throws Exception {
		SimpleComponentDescriptor<DatastoreDictionary> desc = SimpleComponentDescriptor.create(DatastoreDictionary.class);
		Set<InitializeMethodDescriptor> initializeMethods = desc.getInitializeMethods();
		assertEquals(1, initializeMethods.size());

		InitializeMethodDescriptorImpl methodDescriptor = (InitializeMethodDescriptorImpl) initializeMethods.iterator()
				.next();
		Class<?>[] parameterTypes = methodDescriptor.getParameterTypes();

		assertEquals(1, parameterTypes.length);
		assertEquals(DatastoreCatalog.class, parameterTypes[0]);

		DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("mydb"));

		DatastoreDictionary dictionary = new DatastoreDictionary("foo", "mydb", "PUBLIC.EMPLOYEES.FIRSTNAME");
		assertNull(dictionary.getDatastoreCatalog());

		methodDescriptor.initialize(dictionary, datastoreCatalog, null);

		assertNotNull(dictionary.getDatastoreCatalog());
	}
}
