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

	private IdGenerator ig = new PrefixedIdGenerator("id");
	private InputColumn<String> c1 = new TransformedInputColumn<String>("greetings", DataTypeFamily.STRING, ig);
	private InputColumn<String> c2 = new TransformedInputColumn<String>("greeters", DataTypeFamily.STRING, ig);
	private StringAnalyzer stringAnalyzer;

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		stringAnalyzer = new StringAnalyzer(c1, c2);
	}

	public void testTypicalExample() throws Exception {
		stringAnalyzer.run(new MockInputRow().put(c1, "Hello").put(c2, "world"), 1);
		stringAnalyzer.run(new MockInputRow().put(c1, "howdy").put(c2, "the universe"), 1);
		stringAnalyzer.run(new MockInputRow().put(c1, "Hey").put(c2, "country"), 1);
		stringAnalyzer.run(new MockInputRow().put(c1, "hi").put(c2, "stranger"), 1);

		CrosstabResult result = stringAnalyzer.getResult();

		assertEquals(Number.class, result.getCrosstab().getValueClass());

		String renderedResult = new CrosstabTextRenderer().render(result);
		String[] resultLines = renderedResult.split("\n");
		assertEquals(21, resultLines.length);

		assertEquals("                                      greetings  greeters ", resultLines[0]);
		assertEquals("Row count                                     4         4 ", resultLines[1]);
		assertEquals("Null count                                    0         0 ", resultLines[2]);
		assertEquals("Entirely uppercase count                      0         0 ", resultLines[3]);
		assertEquals("Entirely lowercase count                      2         4 ", resultLines[4]);
		assertEquals("Total char count                             15        32 ", resultLines[5]);
		assertEquals("Max chars                                     5        12 ", resultLines[6]);
		assertEquals("Min chars                                     2         5 ", resultLines[7]);
		assertEquals("Avg chars                                  3,75         8 ", resultLines[8]);
		assertEquals("Max white spaces                              0         1 ", resultLines[9]);
		assertEquals("Min white spaces                              0         0 ", resultLines[10]);
		assertEquals("Avg white spaces                              0      0,25 ", resultLines[11]);
		assertEquals("Uppercase chars                               2         0 ", resultLines[12]);
		assertEquals("Uppercase chars (excl. first letters)         0         0 ", resultLines[13]);
		assertEquals("Lowercase chars                              13        31 ", resultLines[14]);
		assertEquals("Digit chars                                   0         0 ", resultLines[15]);
		assertEquals("Diacritic chars                               0         0 ", resultLines[16]);
		assertEquals("Non-letter chars                              0         1 ", resultLines[17]);
		assertEquals("Word count                                    4         5 ", resultLines[18]);
		assertEquals("Max words                                     1         2 ", resultLines[19]);
		assertEquals("Min words                                     1         1 ", resultLines[20]);
	}

	public void testOddValuesWithMoreThanOneDistinctCount() throws Exception {
		// notice the non-1 "distinctCount"
		stringAnalyzer.run(new MockInputRow().put(c1, "HELLO").put(c2, "  "), 1);
		stringAnalyzer.run(new MockInputRow().put(c1, "HÖWDY").put(c2, null), 3);
		stringAnalyzer.run(new MockInputRow().put(c1, " HËJSÄN").put(c2, "eobjects.org"), 1);
		stringAnalyzer.run(new MockInputRow().put(c1, "SØREN SEN").put(c2, "- hi"), 4);

		CrosstabResult result = stringAnalyzer.getResult();

		assertEquals(Number.class, result.getCrosstab().getValueClass());

		String renderedResult = new CrosstabTextRenderer().render(result);
		String[] resultLines = renderedResult.split("\n");
		assertEquals(21, resultLines.length);

		assertEquals("                                      greetings  greeters ", resultLines[0]);
		assertEquals("Row count                                     9         9 ", resultLines[1]);
		assertEquals("Null count                                    0         3 ", resultLines[2]);
		assertEquals("Entirely uppercase count                      9         0 ", resultLines[3]);
		assertEquals("Entirely lowercase count                      0         5 ", resultLines[4]);
		assertEquals("Total char count                             63        30 ", resultLines[5]);
		assertEquals("Max chars                                     9        12 ", resultLines[6]);
		assertEquals("Min chars                                     5         2 ", resultLines[7]);
		assertEquals("Avg chars                                   6,5         6 ", resultLines[8]);
		assertEquals("Max white spaces                              1         2 ", resultLines[9]);
		assertEquals("Min white spaces                              0         0 ", resultLines[10]);
		assertEquals("Avg white spaces                            0,5         1 ", resultLines[11]);
		assertEquals("Uppercase chars                              58         0 ", resultLines[12]);
		assertEquals("Uppercase chars (excl. first letters)        49         0 ", resultLines[13]);
		assertEquals("Lowercase chars                               0        19 ", resultLines[14]);
		assertEquals("Digit chars                                   0         0 ", resultLines[15]);
		assertEquals("Diacritic chars                               9         0 ", resultLines[16]);
		assertEquals("Non-letter chars                              5        11 ", resultLines[17]);
		assertEquals("Word count                                   13         9 ", resultLines[18]);
		assertEquals("Max words                                     2         2 ", resultLines[19]);
		assertEquals("Min words                                     1         0 ", resultLines[20]);
	}

	public void testNoRows() throws Exception {
		@SuppressWarnings("unchecked")
		StringAnalyzer stringAnalyzer = new StringAnalyzer(c1, c2);

		CrosstabResult result = stringAnalyzer.getResult();

		assertEquals(Number.class, result.getCrosstab().getValueClass());

		String renderedResult = new CrosstabTextRenderer().render(result);
		String[] resultLines = renderedResult.split("\n");
		assertEquals(21, resultLines.length);

		assertEquals("                                      greetings  greeters ", resultLines[0]);
		assertEquals("Row count                                     0         0 ", resultLines[1]);
		assertEquals("Null count                                    0         0 ", resultLines[2]);
		assertEquals("Entirely uppercase count                      0         0 ", resultLines[3]);
		assertEquals("Entirely lowercase count                      0         0 ", resultLines[4]);
		assertEquals("Total char count                              0         0 ", resultLines[5]);
		assertEquals("Max chars                                <null>    <null> ", resultLines[6]);
		assertEquals("Min chars                                <null>    <null> ", resultLines[7]);
		assertEquals("Avg chars                                <null>    <null> ", resultLines[8]);
		assertEquals("Max white spaces                         <null>    <null> ", resultLines[9]);
		assertEquals("Min white spaces                         <null>    <null> ", resultLines[10]);
		assertEquals("Avg white spaces                         <null>    <null> ", resultLines[11]);
		assertEquals("Uppercase chars                               0         0 ", resultLines[12]);
		assertEquals("Uppercase chars (excl. first letters)         0         0 ", resultLines[13]);
		assertEquals("Lowercase chars                               0         0 ", resultLines[14]);
		assertEquals("Digit chars                                   0         0 ", resultLines[15]);
		assertEquals("Diacritic chars                               0         0 ", resultLines[16]);
		assertEquals("Non-letter chars                              0         0 ", resultLines[17]);
		assertEquals("Word count                                    0         0 ", resultLines[18]);
		assertEquals("Max words                                <null>    <null> ", resultLines[19]);
		assertEquals("Min words                                <null>    <null> ", resultLines[20]);
	}
}
