package org.eobjects.analyzer.beans;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.PrefixedIdGenerator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.MockInputRow;

public class StringAnalyzerTest extends TestCase {

	public void testTypicalExample() throws Exception {
		IdGenerator ig = new PrefixedIdGenerator("id");
		InputColumn<String> c1 = new TransformedInputColumn<String>(
				"greetings", DataTypeFamily.STRING, ig);
		InputColumn<String> c2 = new TransformedInputColumn<String>("greeters",
				DataTypeFamily.STRING, ig);

		@SuppressWarnings("unchecked")
		StringAnalyzer stringAnalyzer = new StringAnalyzer(c1, c2);
		stringAnalyzer.run(
				new MockInputRow().put(c1, "Hello").put(c2, "world"), 1);
		stringAnalyzer.run(
				new MockInputRow().put(c1, "howdy").put(c2, "the universe"), 1);
		stringAnalyzer.run(
				new MockInputRow().put(c1, "Hey").put(c2, "country"), 1);
		stringAnalyzer.run(
				new MockInputRow().put(c1, "hi").put(c2, "stranger"), 1);

		CrosstabResult result = stringAnalyzer.getResult();

		assertEquals(Number.class, result.getCrosstab().getValueClass());

		String renderedResult = new CrosstabTextRenderer().render(result);
		String[] resultLines = renderedResult.split("\n");
		assertEquals(14, resultLines.length);

		assertEquals("                 greetings  greeters ", resultLines[0]);
		assertEquals("Char count              15        32 ", resultLines[1]);
		assertEquals("Max chars                5        12 ", resultLines[2]);
		assertEquals("Min chars                2         5 ", resultLines[3]);
		assertEquals("Avg chars             3,75         8 ", resultLines[4]);
		assertEquals("Max white spaces         0         1 ", resultLines[5]);
		assertEquals("Min white spaces         0         0 ", resultLines[6]);
		assertEquals("Avg white spaces         0      0,25 ", resultLines[7]);
		assertEquals("Uppercase chars        13%        0% ", resultLines[8]);
		assertEquals("Lowercase chars        86%       96% ", resultLines[9]);
		assertEquals("Non-letter chars        0%        3% ", resultLines[10]);
		assertEquals("Word count               4         5 ", resultLines[11]);
		assertEquals("Max words                1         2 ", resultLines[12]);
		assertEquals("Min words                1         1 ", resultLines[13]);
	}
}
