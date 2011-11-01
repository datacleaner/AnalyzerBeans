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

public class UsageAwareDataContextProviderTest extends TestCase {

	public void testGetUsageCount() throws Exception {
		CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
		assertFalse(ds.isDatastoreConnectionOpen());

		UsageAwareDatastoreConnection dcp1 = (UsageAwareDatastoreConnection) ds.getDataContextProvider();
		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(1, dcp1.getUsageCount());

		DataContextProvider dcp2 = ds.getDataContextProvider();
		assertEquals(2, dcp1.getUsageCount());

		DataContextProvider dcp3 = ds.getDataContextProvider();
		assertEquals(3, dcp1.getUsageCount());

		assertSame(dcp1, dcp2);
		assertSame(dcp1, dcp3);

		dcp3.close();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(2, dcp1.getUsageCount());

		dcp2.close();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertEquals(1, dcp1.getUsageCount());

		dcp1.close();

		assertFalse(ds.isDatastoreConnectionOpen());
	}

	public void testCloseByGarbageCollection() throws Exception {
		CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
		assertFalse(ds.isDatastoreConnectionOpen());

		DataContextProvider dcp1 = ds.getDataContextProvider();
		DataContextProvider dcp2 = ds.getDataContextProvider();

		assertTrue(ds.isDatastoreConnectionOpen());
		assertSame(dcp1, dcp2);

		dcp1 = null;
		dcp2 = null;

		// invoke GC
		System.gc();
		System.runFinalization();

		assertFalse(ds.isDatastoreConnectionOpen());
	}
}
