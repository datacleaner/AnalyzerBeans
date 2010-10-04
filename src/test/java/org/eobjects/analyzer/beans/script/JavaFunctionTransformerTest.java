package org.eobjects.analyzer.beans.script;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.reference.Function;
import org.eobjects.analyzer.test.MockInputColumn;
import org.eobjects.analyzer.test.MockInputRow;

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
