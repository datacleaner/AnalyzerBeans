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
package org.eobjects.analyzer.util;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.util.EqualsBuilder;

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

		// this is needed to make sure the unittest is runnable in all locales.
		TimeZone timeZone = TimeZone.getDefault();
		int localeOffset = timeZone.getRawOffset();

		runTests(new Date(1234 - localeOffset), "1970-01-01T00:00:01 234");
		runTests(Calendar.getInstance(), null);
		runTests(new java.sql.Date(1234 - localeOffset), "1970-01-01T00:00:01 234");
	}

	public static class MySerializable implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String myString;
		private final int myInt;

		public MySerializable(String myString, int myInt) {
			this.myString = myString;
			this.myInt = myInt;
		}

		public int getMyInt() {
			return myInt;
		}

		public String getMyString() {
			return myString;
		}
	}

	public void testConvertSerializable() throws Exception {
		String serialized = StringConversionUtils.serialize(new MySerializable("foobar", 1337));
		assertEquals(
				"&#91;-84&#44;-19&#44;0&#44;5&#44;115&#44;114&#44;0&#44;67&#44;111&#44;114&#44;103&#44;46&#44;101&#44;111&#44;98&#44;106&#44;101&#44;99&#44;116&#44;115&#44;46&#44;97&#44;110&#44;97&#44;108&#44;121&#44;122&#44;101&#44;114&#44;46&#44;117&#44;116&#44;105&#44;108&#44;46&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;67&#44;111&#44;110&#44;118&#44;101&#44;114&#44;115&#44;105&#44;111&#44;110&#44;85&#44;116&#44;105&#44;108&#44;115&#44;84&#44;101&#44;115&#44;116&#44;36&#44;77&#44;121&#44;83&#44;101&#44;114&#44;105&#44;97&#44;108&#44;105&#44;122&#44;97&#44;98&#44;108&#44;101&#44;0&#44;0&#44;0&#44;0&#44;0&#44;0&#44;0&#44;1&#44;2&#44;0&#44;2&#44;73&#44;0&#44;5&#44;109&#44;121&#44;73&#44;110&#44;116&#44;76&#44;0&#44;8&#44;109&#44;121&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;116&#44;0&#44;18&#44;76&#44;106&#44;97&#44;118&#44;97&#44;47&#44;108&#44;97&#44;110&#44;103&#44;47&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;59&#44;120&#44;112&#44;0&#44;0&#44;5&#44;57&#44;116&#44;0&#44;6&#44;102&#44;111&#44;111&#44;98&#44;97&#44;114&#93;",
				serialized);

		MySerializable deserialized = StringConversionUtils.deserialize(serialized, MySerializable.class, null, null, null);
		assertEquals("foobar", deserialized.getMyString());
		assertEquals(1337, deserialized.getMyInt());
	}

	public void testAbstractNumber() throws Exception {
		Number n = StringConversionUtils.deserialize("1", Number.class, null, null, null);
		assertTrue(n instanceof Long);
		assertEquals(1, n.intValue());

		n = StringConversionUtils.deserialize("1.01", Number.class, null, null, null);
		assertTrue(n instanceof Double);
		assertEquals(1.01, n.doubleValue());
		assertEquals(1, n.intValue());
	}

	public void testEnum() throws Exception {
		String serialized = StringConversionUtils.serialize(ValidationCategory.VALID);
		assertEquals("VALID", serialized);

		Object deserialized = StringConversionUtils.deserialize(serialized, ValidationCategory.class, null, null, null);
		assertEquals(ValidationCategory.VALID, deserialized);

		ValidationCategory[] array = new ValidationCategory[] { ValidationCategory.VALID, ValidationCategory.INVALID };
		serialized = StringConversionUtils.serialize(array);
		assertEquals("[VALID,INVALID]", serialized);

		deserialized = StringConversionUtils.deserialize(serialized, ValidationCategory[].class, null, null, null);
		assertTrue(EqualsBuilder.equals(array, deserialized));
	}

	public void testFile() throws Exception {
		File file1 = new File("pom.xml");
		File fileAbs = file1.getAbsoluteFile();
		File dir1 = new File("src");

		String serialized = StringConversionUtils.serialize(file1);
		assertEquals("pom.xml", serialized);

		Object deserialized = StringConversionUtils.deserialize(serialized, File.class, null, null, null);
		assertTrue(EqualsBuilder.equals(file1, deserialized));

		serialized = StringConversionUtils.serialize(fileAbs);
		assertEquals(fileAbs.getAbsolutePath(), serialized);

		File[] arr = new File[] { file1, dir1 };

		serialized = StringConversionUtils.serialize(arr);
		assertEquals("[pom.xml,src]", serialized);

		deserialized = StringConversionUtils.deserialize(serialized, File[].class, null, null, null);
		assertTrue(EqualsBuilder.equals(arr, deserialized));
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

		Collection<StringPattern> stringPatterns = new ArrayList<StringPattern>();

		ReferenceDataCatalogImpl referenceDataCatalog = new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs,
				stringPatterns);

		Dictionary dictionaryResult = StringConversionUtils.deserialize("my dict", Dictionary.class, null,
				referenceDataCatalog, null);
		assertSame(dictionaryResult, dictionary);

		dictionaryResult = StringConversionUtils.deserialize("foo", Dictionary.class, null, referenceDataCatalog, null);
		assertNull(dictionaryResult);

		SynonymCatalog synonymCatalogResult = StringConversionUtils.deserialize("my synonyms", SynonymCatalog.class, null,
				referenceDataCatalog, null);
		assertSame(synonymCatalogResult, synonymCatalog);
		synonymCatalogResult = StringConversionUtils.deserialize("bar", SynonymCatalog.class, null, referenceDataCatalog,
				null);
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
		assertNull(StringConversionUtils.deserialize(s, String.class, null, null, null));
		assertNull(StringConversionUtils.deserialize(s, Integer.class, null, null, null));
		assertNull(StringConversionUtils.deserialize(s, Date.class, null, null, null));
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

		Long[] result = StringConversionUtils.deserialize("123", Long[].class, null, null, null);
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
		Object o2 = StringConversionUtils.deserialize(s, o.getClass(), null, null, null);
		if (ReflectionUtils.isArray(o)) {
			boolean equals = EqualsBuilder.equals(o, o2);
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
