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
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.StringPattern;

import junit.framework.TestCase;

public class StringPatternMatchFilterTest extends TestCase {

	public void testFilter() throws Exception {
		StringPattern stringPattern = new RegexStringPattern("very simple email pattern", ".+@.+", true);
		MockInputColumn<String> column = new MockInputColumn<String>("my col", String.class);
		StringPatternMatchFilter filter = new StringPatternMatchFilter(column, stringPattern);

		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
	}
}
