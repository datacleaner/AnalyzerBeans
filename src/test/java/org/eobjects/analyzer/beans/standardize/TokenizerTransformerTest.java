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
package org.eobjects.analyzer.beans.standardize;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;

import org.eobjects.metamodel.schema.MutableColumn;

public class TokenizerTransformerTest extends TestCase {

	public void testGetOutputColumns() throws Exception {
		InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

		@SuppressWarnings("unchecked")
		TokenizerTransformer transformer = new TokenizerTransformer((InputColumn<String>) col, 2);

		OutputColumns oc = transformer.getOutputColumns();

		assertEquals(2, oc.getColumnCount());
		assertEquals("name (token 1)", oc.getColumnName(0));
		assertEquals("name (token 2)", oc.getColumnName(1));
	}

	public void testTransform() throws Exception {
		InputColumn<?> col = new MetaModelInputColumn(new MutableColumn("name"));

		@SuppressWarnings("unchecked")
		TokenizerTransformer transformer = new TokenizerTransformer((InputColumn<String>) col, 2);

		assertEquals(2, transformer.getOutputColumns().getColumnCount());

		TransformedInputRow row = new TransformedInputRow(null);
		row.addValue(col, "Kasper Sorensen");
		String[] values = transformer.transform(row);
		assertEquals(2, values.length);
		assertEquals("Kasper", values[0]);
		assertEquals("Sorensen", values[1]);

		row = new TransformedInputRow(null);
		row.addValue(col, "Kasper ");
		values = transformer.transform(row);
		assertEquals(2, values.length);
		assertEquals("Kasper", values[0]);
		assertNull(values[1]);
	}
}
