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
package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class StringLengthTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("foobar", String.class);
		StringLengthTransformer transformer = new StringLengthTransformer(col);
		assertEquals(1, transformer.getOutputColumns().getColumnCount());
		Number[] result;
		
		result = transformer.transform(new MockInputRow().put(col, "hello"));
		assertEquals(1, result.length);
		assertEquals(5, result[0]);
		
		result = transformer.transform(new MockInputRow().put(col, null));
		assertEquals(1, result.length);
		assertNull(result[0]);
		
		result = transformer.transform(new MockInputRow().put(col, ""));
		assertEquals(1, result.length);
		assertEquals(0, result[0]);
	}
}
