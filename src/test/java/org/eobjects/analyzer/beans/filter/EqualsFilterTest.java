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
package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.data.MockInputColumn;

import junit.framework.TestCase;

public class EqualsFilterTest extends TestCase {

	public void testString() throws Exception {
		EqualsFilter f = new EqualsFilter("hello", new MockInputColumn<String>("col", String.class));
		assertEquals(ValidationCategory.VALID, f.filter("hello"));
		assertEquals(ValidationCategory.INVALID, f.filter("Hello"));
		assertEquals(ValidationCategory.INVALID, f.filter(""));
		assertEquals(ValidationCategory.INVALID, f.filter(null));
	}

	public void testNumber() throws Exception {
		EqualsFilter f = new EqualsFilter("1234", new MockInputColumn<Number>("col", Number.class));
		assertEquals(ValidationCategory.VALID, f.filter(1234));
		assertEquals(ValidationCategory.VALID, f.filter(1234.0));
		assertEquals(ValidationCategory.INVALID, f.filter(2));
		assertEquals(ValidationCategory.INVALID, f.filter(-2));
	}
}
