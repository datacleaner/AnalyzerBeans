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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;

import junit.framework.TestCase;

public class DictionaryLookupFilterTest extends TestCase {

	public void testSimpleLookups() throws Exception {
		InputColumn<String> column = new MockInputColumn<String>("col", String.class);
		Dictionary dictionary = new SimpleDictionary("my dictionary", "foo", "bar", "baz");
		
		DictionaryFilter filter = new DictionaryFilter(column, dictionary);
		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "foo")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "foo ")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "foo bar")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "foobar")));
		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "bar")));
		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "baz")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, null)));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "")));
	}
}
