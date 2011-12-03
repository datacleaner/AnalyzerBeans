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
import java.util.Arrays;
import java.util.List;

import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

import junit.framework.TestCase;

public class SasDatastoreTest extends TestCase {

	// This is an integration test. it is pretty nescesary since release cycles
	// of MetaModel and SassyReader are not synced.
	public void testConnectAndExplore() throws Exception {
		SasDatastore ds = new SasDatastore("my sas ds", new File("src/test/resources/sas"));
		DatastoreConnection con = ds.openConnection();
		try {
			Schema schema = con.getSchemaNavigator().getDefaultSchema();
			assertEquals("[dummy1, dummy2, pizza]", Arrays.toString(schema.getTableNames()));

			Table table = schema.getTableByName("pizza");
			assertEquals("[id, mois, prot, fat, ash, sodium, carb, cal, brand]", Arrays.toString(table.getColumnNames()));

			Column col = table.getColumnByName("brand");

			Query q = con.getDataContext().query().from(table).select(col).orderBy(col).toQuery();
			q.getSelectClause().setDistinct(true);

			List<Object[]> objectArrays = con.getDataContext().executeQuery(q).toObjectArrays();
			assertEquals(10, objectArrays.size());
			assertEquals("a", objectArrays.get(0)[0]);
			assertEquals("b", objectArrays.get(1)[0]);
		} finally {
			con.close();
		}
	}
}
