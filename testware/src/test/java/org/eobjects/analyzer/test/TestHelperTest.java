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

import java.util.Arrays;

import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;

import junit.framework.TestCase;

public class TestHelperTest extends TestCase {

    public void testCreateSampleDatabaseDatastore() throws Exception {
        JdbcDatastore ds = TestHelper.createSampleDatabaseDatastore("foo");
        assertEquals("foo", ds.getName());

        UpdateableDatastoreConnection con = ds.openConnection();
        String[] tableNames = con.getSchemaNavigator().getDefaultSchema().getTableNames();
        assertEquals(
                "[CUSTOMERS, CUSTOMER_W_TER, DEPARTMENT_MANAGERS, DIM_TIME, EMPLOYEES, "
                        + "OFFICES, ORDERDETAILS, ORDERFACT, ORDERS, PAYMENTS, PRODUCTS, "
                        + "QUADRANT_ACTUALS, TRIAL_BALANCE]", Arrays.toString(tableNames));

        con.close();
    }
}
