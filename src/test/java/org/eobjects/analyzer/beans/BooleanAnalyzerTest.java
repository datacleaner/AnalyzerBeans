package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;

import junit.framework.TestCase;

public class BooleanAnalyzerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		@SuppressWarnings("unchecked")
		InputColumn<Boolean>[] c = new InputColumn[2];
		c[0] = new MockInputColumn<Boolean>("b1", Boolean.class);
		c[1] = new MockInputColumn<Boolean>("b2", Boolean.class);

		BooleanAnalyzer ba = new BooleanAnalyzer(c);
		ba.init();

		ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 3);
		ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 1);
		ba.run(new MockInputRow().put(c[0], true).put(c[1], false), 1);
		ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);
		ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);

		String[] resultLines = new CrosstabTextRenderer().render(ba.getResult()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals("                b1     b2 ", resultLines[0]);
		assertEquals("Row count        7      7 ", resultLines[1]);
		assertEquals("Null count       0      0 ", resultLines[2]);
		assertEquals("True count       5      6 ", resultLines[3]);
		assertEquals("False count      2      1 ", resultLines[4]);
	}
}
