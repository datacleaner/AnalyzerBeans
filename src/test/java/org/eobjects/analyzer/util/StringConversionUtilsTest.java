package org.eobjects.analyzer.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class StringConversionUtilsTest extends TestCase {

	public void testConvertSimpleTypes() throws Exception {
		runTests("hello, [world]", "hello&#44; &#91;world&#93;");
		runTests("hello", "hello");
		runTests(1337, "1337");
		runTests(12l, "12");
		runTests('a', "a");
		runTests(true, "true");
		runTests(false, "false");
		runTests((short) 12, "12");
		runTests((byte) 12, "12");
		runTests(1337.0, "1337.0");
		runTests(1337.0f, "1337.0");
		runTests(new Date(1234), "1970-01-01T01:00:01 234");
		runTests(Calendar.getInstance(), null);
		runTests(new java.sql.Date(1234), "1970-01-01T01:00:01 234");
	}
	
	public void testSerializeUnknownTypes() throws Exception {
		String result = StringConversionUtils.serialize(new Percentage(50));
		assertEquals("50%", result);
	}
	
	public void testSerializeSchemaElements() throws Exception {
		Schema schema = new Schema("s1");
		assertEquals("s1", StringConversionUtils.serialize(schema));
		
		Table table = new Table("t1");
		table.setSchema(schema);
		assertEquals("s1.t1", StringConversionUtils.serialize(table));
		
		Column column = new Column("c1");
		column.setTable(table);
		assertEquals("s1.t1.c1", StringConversionUtils.serialize(column));
	}

	public void testNullArgument() throws Exception {
		String s = StringConversionUtils.serialize(null);
		assertEquals("<null>", s);
		assertNull(StringConversionUtils.deserialize(s, String.class, null));
		assertNull(StringConversionUtils.deserialize(s, Integer.class, null));
		assertNull(StringConversionUtils.deserialize(s, Date.class, null));
	}

	public void testArrays() throws Exception {
		runTests(new String[] { "hello,world" }, "[hello&#44;world]");
		runTests(new String[] { "hello", "world" }, "[hello,world]");
		runTests(new String[] { "hello", "[world]" }, "[hello,&#91;world&#93;]");
		runTests(new String[] { "hello, there", "[world]" }, null);
		runTests(new String[] { "hello, there [y0!]", "w00p" }, null);
		runTests(new Double[] { 123.4, 567.8 }, "[123.4,567.8]");
		runTests(new String[0], "[]");
		runTests(new String[3], "[<null>,<null>,<null>]");

		Long[] result = StringConversionUtils.deserialize("123", Long[].class,
				null);
		assertEquals(1, result.length);
		assertEquals(123l, result[0].longValue());
	}

	public void testDoubleSidedArray() throws Exception {
		runTests(new String[][] { { "hello", "world" }, { "hi", "there" } },
				"[[hello,world],[hi,there]]");
		runTests(new String[][] { { "hello", "world" }, { "howdy" },
				{ "hi", "there partner", "yiiioowy" } },
		"[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
		runTests(new String[][] { { "hello", "world" }, { "howdy" },
				{ "hi", "there partner", "yiiioowy" } },
		"[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
	}

	public void testDeepArray() throws Exception {
		runTests(
				new Integer[][][][] { { { { 1, 2 }, { 3, 4 } }, { { 5, 6 } } } },
				"[[[[1,2],[3,4]],[[5,6]]]]");
	}

	private void runTests(final Object o, String expectedStringRepresentation) {
		String s = StringConversionUtils.serialize(o);
		if (expectedStringRepresentation != null) {
			assertEquals(expectedStringRepresentation, s);
		}
		Object o2 = StringConversionUtils.deserialize(s, o.getClass(), null);
		if (ReflectionUtils.isArray(o)) {
			boolean equals = ArrayUtils.isEquals(o, o2);
			if (!equals) {
				System.out.println("Not equals:");
				System.out.println(" expected: " + o + ": "
						+ ArrayUtils.toString(o));
				System.out.println(" actual:   " + o2 + ": "
						+ ArrayUtils.toString(o2));
			}
			assertTrue(equals);
		} else {
			assertEquals(o, o2);
		}
	}
}
