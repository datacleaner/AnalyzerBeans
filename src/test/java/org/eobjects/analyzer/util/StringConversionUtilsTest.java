package org.eobjects.analyzer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.SynonymCatalog;

import dk.eobjects.metamodel.schema.MutableColumn;
import dk.eobjects.metamodel.schema.MutableSchema;
import dk.eobjects.metamodel.schema.MutableTable;
import dk.eobjects.metamodel.schema.Schema;

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

	public void testEnum() throws Exception {
		String serialized = StringConversionUtils.serialize(ValidationCategory.VALID);
		assertEquals("VALID", serialized);
		
		Object deserialized = StringConversionUtils.deserialize(serialized, ValidationCategory.class, null, null);
		assertEquals(ValidationCategory.VALID, deserialized);

		ValidationCategory[] array = new ValidationCategory[] { ValidationCategory.VALID,
				ValidationCategory.INVALID };
		serialized = StringConversionUtils.serialize(array);
		assertEquals("[VALID,INVALID]", serialized);
		
		deserialized = StringConversionUtils.deserialize(serialized, ValidationCategory[].class, null, null);
		assertTrue(CompareUtils.equals(array, deserialized));
	}
	
	public void testFile() throws Exception {
		File file1 = new File("pom.xml");
		File fileAbs = file1.getAbsoluteFile();
		File dir1 = new File("src");
		
		String serialized = StringConversionUtils.serialize(file1);
		assertEquals("pom.xml", serialized);
		
		Object deserialized = StringConversionUtils.deserialize(serialized, File.class, null, null);
		assertTrue(CompareUtils.equals(file1, deserialized));
		
		serialized = StringConversionUtils.serialize(fileAbs);
		assertEquals(fileAbs.getAbsolutePath(), serialized);
		
		File[] arr = new File[] {file1, dir1};
		
		serialized = StringConversionUtils.serialize(arr);
		assertEquals("[pom.xml,src]", serialized);
		
		deserialized = StringConversionUtils.deserialize(serialized, File[].class, null, null);
		assertTrue(CompareUtils.equals(arr, deserialized));
	}

	public void testReferenceDataSerialization() throws Exception {
		Dictionary dictionary = new SimpleDictionary("my dict");
		SynonymCatalog synonymCatalog = new SimpleSynonymCatalog("my synonyms");

		assertEquals("my dict", StringConversionUtils.serialize(dictionary));
		assertEquals("my synonyms", StringConversionUtils.serialize(synonymCatalog));

		Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
		dictionaries.add(dictionary);

		Collection<SynonymCatalog> synonymCatalogs = new ArrayList<SynonymCatalog>();
		synonymCatalogs.add(synonymCatalog);

		ReferenceDataCatalogImpl referenceDataCatalog = new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs);

		Dictionary dictionaryResult = StringConversionUtils.deserialize("my dict", Dictionary.class, null,
				referenceDataCatalog);
		assertSame(dictionaryResult, dictionary);

		dictionaryResult = StringConversionUtils.deserialize("foo", Dictionary.class, null, referenceDataCatalog);
		assertNull(dictionaryResult);

		SynonymCatalog synonymCatalogResult = StringConversionUtils.deserialize("my synonyms", SynonymCatalog.class, null,
				referenceDataCatalog);
		assertSame(synonymCatalogResult, synonymCatalog);
		synonymCatalogResult = StringConversionUtils.deserialize("bar", SynonymCatalog.class, null, referenceDataCatalog);
		assertNull(synonymCatalogResult);
	}

	public void testSerializeUnknownTypes() throws Exception {
		String result = StringConversionUtils.serialize(new Percentage(50));
		assertEquals("50%", result);
	}

	public void testSerializeSchemaElements() throws Exception {
		Schema schema = new MutableSchema("s1");
		assertEquals("s1", StringConversionUtils.serialize(schema));

		MutableTable table = new MutableTable("t1");
		table.setSchema(schema);
		assertEquals("s1.t1", StringConversionUtils.serialize(table));

		MutableColumn column = new MutableColumn("c1");
		column.setTable(table);
		assertEquals("s1.t1.c1", StringConversionUtils.serialize(column));
	}

	public void testNullArgument() throws Exception {
		String s = StringConversionUtils.serialize(null);
		assertEquals("<null>", s);
		assertNull(StringConversionUtils.deserialize(s, String.class, null, null));
		assertNull(StringConversionUtils.deserialize(s, Integer.class, null, null));
		assertNull(StringConversionUtils.deserialize(s, Date.class, null, null));
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

		Long[] result = StringConversionUtils.deserialize("123", Long[].class, null, null);
		assertEquals(1, result.length);
		assertEquals(123l, result[0].longValue());
	}

	public void testDoubleSidedArray() throws Exception {
		runTests(new String[][] { { "hello", "world" }, { "hi", "there" } }, "[[hello,world],[hi,there]]");
		runTests(new String[][] { { "hello", "world" }, { "howdy" }, { "hi", "there partner", "yiiioowy" } },
				"[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
		runTests(new String[][] { { "hello", "world" }, { "howdy" }, { "hi", "there partner", "yiiioowy" } },
				"[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
	}

	public void testDeepArray() throws Exception {
		runTests(new Integer[][][][] { { { { 1, 2 }, { 3, 4 } }, { { 5, 6 } } } }, "[[[[1,2],[3,4]],[[5,6]]]]");
	}

	private void runTests(final Object o, String expectedStringRepresentation) {
		String s = StringConversionUtils.serialize(o);
		if (expectedStringRepresentation != null) {
			assertEquals(expectedStringRepresentation, s);
		}
		Object o2 = StringConversionUtils.deserialize(s, o.getClass(), null, null);
		if (ReflectionUtils.isArray(o)) {
			boolean equals = CompareUtils.equals(o, o2);
			if (!equals) {
				StringBuilder sb = new StringBuilder();
				sb.append("Not equals!");
				sb.append("\n expected: " + o + ": " + Arrays.toString((Object[]) o));
				sb.append("\n actual:   " + o2 + ": " + Arrays.toString((Object[]) o2));
				fail(sb.toString());
			}
		} else {
			assertEquals(o, o2);
		}
	}
}
