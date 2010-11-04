package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class SqlDatabaseStorageProviderTest extends TestCase {

	private HsqldbStorageProvider sp = new HsqldbStorageProvider();

	public void testCreateList() throws Exception {
		List<String> list = sp.createList(String.class);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());

		list.add("hello");
		list.add("world");
		assertEquals(2, list.size());

		assertEquals("world", list.get(1));

		assertEquals("[hello, world]", Arrays.toString(list.toArray()));

		list.remove(1);

		assertEquals("[hello]", Arrays.toString(list.toArray()));

		list.remove("foobar");
		list.remove("hello");

		assertEquals("[]", Arrays.toString(list.toArray()));
	}

	public void testCreateMap() throws Exception {
		Map<Integer, String> map = sp.createMap(Integer.class, String.class);

		map.put(1, "hello");
		map.put(2, "world");
		map.put(5, "foo");

		assertEquals("world", map.get(2));
		assertNull(map.get(3));

		assertEquals(3, map.size());

		// override 5
		map.put(5, "bar");

		assertEquals(3, map.size());
	}

	public void testCreateSet() throws Exception {
		Set<Long> set = sp.createSet(Long.class);

		assertTrue(set.isEmpty());
		assertEquals(0, set.size());

		set.add(1l);

		assertEquals(1, set.size());

		set.add(2l);
		set.add(3l);

		assertEquals(3, set.size());

		set.add(3l);

		assertEquals(3, set.size());

		Iterator<Long> it = set.iterator();
		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertFalse(it.hasNext());

		it = set.iterator();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		// remove 1
		it.remove();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertEquals("[2, 3]", Arrays.toString(set.toArray()));
	}

	public void testFinalize() throws Exception {
		Connection connectionMock = EasyMock.createMock(Connection.class);
		Statement statementMock = EasyMock.createMock(Statement.class);

		EasyMock.expect(connectionMock.createStatement()).andReturn(statementMock);
		EasyMock.expect(statementMock.executeUpdate("CREATE TABLE MY_TABLE (set_value VARCHAR PRIMARY KEY)")).andReturn(0);
		statementMock.close();
		EasyMock.expect(connectionMock.prepareCall("SELECT set_value FROM MY_TABLE")).andReturn(null);
		EasyMock.expect(connectionMock.prepareCall("SELECT COUNT(*) FROM MY_TABLE WHERE set_value=?")).andReturn(null);
		EasyMock.expect(connectionMock.prepareCall("INSERT INTO MY_TABLE VALUES(?)")).andReturn(null);
		EasyMock.expect(connectionMock.prepareCall("DELETE FROM MY_TABLE WHERE set_value=?")).andReturn(null);

		EasyMock.expect(connectionMock.createStatement()).andReturn(statementMock);
		EasyMock.expect(statementMock.executeUpdate("DROP TABLE MY_TABLE")).andReturn(0);
		statementMock.close();

		EasyMock.replay(statementMock, connectionMock);

		SqlDatabaseSet<String> set = new SqlDatabaseSet<String>(connectionMock, "MY_TABLE", "VARCHAR");
		assertEquals(0, set.size());
		set = null;
		System.gc();
		System.runFinalization();

		EasyMock.verify(statementMock, connectionMock);
	}
}
