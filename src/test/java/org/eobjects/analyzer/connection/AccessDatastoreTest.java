package org.eobjects.analyzer.connection;

import java.util.Arrays;

import dk.eobjects.metamodel.DataContext;

import junit.framework.TestCase;

public class AccessDatastoreTest extends TestCase {

	public void testGetDataContextProvider() throws Exception {
		AccessDatastore ds = new AccessDatastore("foobar", "src/test/resources/developers.mdb");
		assertEquals("foobar", ds.getName());

		DataContextProvider dcp = ds.getDataContextProvider();
		DataContext dataContext = dcp.getDataContext();

		assertEquals("[information_schema, developers.mdb]", Arrays.toString(dataContext.getSchemaNames()));
		String[] tableNames = dataContext.getDefaultSchema().getTableNames();
		assertEquals("[developer, product]", Arrays.toString(tableNames));
	}
}
