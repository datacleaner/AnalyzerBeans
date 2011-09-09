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
package org.eobjects.analyzer.beans.api;

import junit.framework.TestCase;

public class OutputColumnsTest extends TestCase {

	public void testTooFewColumnsInt() throws Exception {
		try {
			new OutputColumns(0);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("columns must be 1 or higher", e.getMessage());
		}
	}
	
	public void testNullColumns() throws Exception {
		try {
			new OutputColumns(null);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("arguments cannot be null", e.getMessage());
		}
	}
	
	public void testTooFewColumnsStringArray() throws Exception {
		try {
			new OutputColumns(new String[0]);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals("column names length must be 1 or higher", e.getMessage());
		}
	}
}
