package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import junit.framework.TestCase;

public class MetaModelInputColumnTest extends TestCase {

	public void testGetDataTypeFamily() throws Exception {
		assertEquals(DataTypeFamily.STRING, new MetaModelInputColumn(
				new Column("foobar", ColumnType.VARCHAR)).getDataTypeFamily());

		assertEquals(DataTypeFamily.STRING, new MetaModelInputColumn(
				new Column("foobar", ColumnType.CHAR)).getDataTypeFamily());

		assertEquals(DataTypeFamily.UNDEFINED, new MetaModelInputColumn(
				new Column("foobar", ColumnType.BLOB)).getDataTypeFamily());

		assertEquals(DataTypeFamily.NUMBER, new MetaModelInputColumn(
				new Column("foobar", ColumnType.INTEGER)).getDataTypeFamily());

		assertEquals(DataTypeFamily.NUMBER, new MetaModelInputColumn(
				new Column("foobar", ColumnType.FLOAT)).getDataTypeFamily());

		assertEquals(DataTypeFamily.DATE, new MetaModelInputColumn(new Column(
				"foobar", ColumnType.DATE)).getDataTypeFamily());

		assertEquals(DataTypeFamily.DATE, new MetaModelInputColumn(new Column(
				"foobar", ColumnType.TIMESTAMP)).getDataTypeFamily());

		assertEquals(DataTypeFamily.BOOLEAN, new MetaModelInputColumn(
				new Column("foobar", ColumnType.BIT)).getDataTypeFamily());

		assertEquals(DataTypeFamily.UNDEFINED,
				new MetaModelInputColumn(new Column("foobar",
						ColumnType.JAVA_OBJECT)).getDataTypeFamily());
	}

	public void testConstructorArgRequired() throws Exception {
		try {
			new MetaModelInputColumn(null);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("column cannot be null", e.getMessage());
		}
	}
}
