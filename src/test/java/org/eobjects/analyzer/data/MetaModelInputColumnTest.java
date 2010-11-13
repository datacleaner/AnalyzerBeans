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
package org.eobjects.analyzer.data;

import junit.framework.TestCase;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.MutableColumn;

public class MetaModelInputColumnTest extends TestCase {

	public void testGetDataTypeFamily() throws Exception {
		assertEquals(DataTypeFamily.STRING,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.VARCHAR)).getDataTypeFamily());

		assertEquals(DataTypeFamily.STRING,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.CHAR)).getDataTypeFamily());

		assertEquals(DataTypeFamily.UNDEFINED,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.BLOB)).getDataTypeFamily());

		assertEquals(DataTypeFamily.NUMBER,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.INTEGER)).getDataTypeFamily());

		assertEquals(DataTypeFamily.NUMBER,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.FLOAT)).getDataTypeFamily());

		assertEquals(DataTypeFamily.DATE,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.DATE)).getDataTypeFamily());

		assertEquals(DataTypeFamily.DATE,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.TIMESTAMP)).getDataTypeFamily());

		assertEquals(DataTypeFamily.BOOLEAN,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.BIT)).getDataTypeFamily());

		assertEquals(DataTypeFamily.UNDEFINED,
				new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.JAVA_OBJECT)).getDataTypeFamily());
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
