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

import junit.framework.TestCase;

public class UsageAwareDatastoreConnectionTest extends TestCase {

	public void testGetUsageCount() throws Exception {
		CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
		assertFalse(ds.isDatastoreConnectionOpen());

		UsageAwareDatastoreConnection<?> con1 = (UsageAwareDatastoreConnection<?>) ds.openConnection();
		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(1, con1.getUsageCount());

		DatastoreConnection con2 = ds.openConnection();
		assertEquals(2, con1.getUsageCount());

		DatastoreConnection con3 = ds.openConnection();
		assertEquals(3, con1.getUsageCount());

		assertSame(con1, con2);
		assertSame(con1, con3);

		con3.close();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(2, con1.getUsageCount());

		con2.close();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(1, con1.getUsageCount());

		con1.close();

		assertFalse(ds.isDatastoreConnectionOpen());
	}

	public void testCloseByGarbageCollection() throws Exception {
		CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
		assertFalse(ds.isDatastoreConnectionOpen());

		DatastoreConnection con1 = ds.openConnection();
		DatastoreConnection con2 = ds.openConnection();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertSame(con1, con2);

		con1 = null;
		con2 = null;

		// invoke GC
		System.gc();
		System.runFinalization();

		assertFalse(ds.isDatastoreConnectionOpen());
	}
}
