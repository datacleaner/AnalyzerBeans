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

import org.eobjects.analyzer.job.PrefixedIdGenerator;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.MutableColumn;
import junit.framework.TestCase;

public class TransformedInputRowTest extends TestCase {

	public void testGetValue() throws Exception {
		Column col1 = new MutableColumn("foo");
		Column col2 = new MutableColumn("bar");
		InputColumn<?> inputColumn1 = new MetaModelInputColumn(col1);
		InputColumn<?> inputColumn2 = new MetaModelInputColumn(col2);
		MutableInputColumn<String> inputColumn3 = new TransformedInputColumn<String>(
				"bar", DataTypeFamily.STRING, new PrefixedIdGenerator("test"));
		assertEquals("test-1", inputColumn3.getId());

		TransformedInputRow row1 = new TransformedInputRow(null);
		row1.addValue(inputColumn1, "f");
		row1.addValue(inputColumn2, "b");
		assertEquals("f", row1.getValue(inputColumn1));
		assertEquals("b", row1.getValue(inputColumn2));
		assertNull(row1.getValue(inputColumn3));
		assertNull(row1.getValue(null));

		TransformedInputRow row2 = new TransformedInputRow(row1);
		assertEquals("f", row2.getValue(inputColumn1));
		assertEquals("b", row2.getValue(inputColumn2));

		row2.addValue(inputColumn3, "w");

		assertNull(row1.getValue(inputColumn3));
		assertEquals("w", row2.getValue(inputColumn3));
	}
}
