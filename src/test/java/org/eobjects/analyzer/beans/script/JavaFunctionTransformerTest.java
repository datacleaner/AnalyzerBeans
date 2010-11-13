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
package org.eobjects.analyzer.beans.script;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.Function;

import junit.framework.TestCase;

public class JavaFunctionTransformerTest extends TestCase {

	/**
	 * Creates a function that returns the length of the incoming string
	 * 
	 * @throws Exception
	 */
	public void testRunWithSimpleFunction() throws Exception {
		String javaCode = "package foo; import "
				+ Function.class.getName()
				+ "; public class MyFunction implements Function<String,String> { public String run(String str) { return \"\" + str.length(); } }";
		InputColumn<String> column = new MockInputColumn<String>("foo", String.class);
		JavaFunctionTransformer t = new JavaFunctionTransformer(javaCode, column);
		assertEquals("5", t.transform(new MockInputRow().put(column, "hello"))[0]);
		assertEquals("11", t.transform(new MockInputRow().put(column, "hello there"))[0]);
	}
}
