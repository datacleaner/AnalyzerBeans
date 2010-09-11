package org.eobjects.analyzer.beans;

import junit.framework.TestCase;

import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.MockInputColumn;
import org.eobjects.analyzer.test.MockInputRow;

public class NumberAnalyzerTest extends TestCase {

	private MockInputColumn<Float> col1;
	private MockInputColumn<Long> col2;
	private MockInputColumn<Byte> col3;
	private NumberAnalyzer numberAnalyzer;

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		col1 = new MockInputColumn<Float>("foo", Float.class);
		col2 = new MockInputColumn<Long>("bar", Long.class);
		col3 = new MockInputColumn<Byte>("w00p", Byte.class);

		numberAnalyzer = new NumberAnalyzer(col1, col2, col3);
		numberAnalyzer.init();
	}

	public void testNoRows() throws Exception {
		CrosstabResult result = numberAnalyzer.getResult();
		String[] resultLines = new CrosstabTextRenderer().render(result).split(
				"\n");
		assertEquals(10, resultLines.length);
		assertEquals("                      foo    bar   w00p ", resultLines[0]);
		assertEquals("Highest value      <null> <null> <null> ", resultLines[1]);
		assertEquals("Lowest value       <null> <null> <null> ", resultLines[2]);
		assertEquals("Sum                <null> <null> <null> ", resultLines[3]);
		assertEquals("Mean               <null> <null> <null> ", resultLines[4]);
		assertEquals("Geometric mean     <null> <null> <null> ", resultLines[5]);
		assertEquals("Standard deviation <null> <null> <null> ", resultLines[6]);
		assertEquals("Variance           <null> <null> <null> ", resultLines[7]);
		assertEquals("Null values             0      0      0 ", resultLines[8]);
		assertEquals("Non-null values         0      0      0 ", resultLines[9]);
	}

	public void testOnlyeOneColumnNonNull() throws Exception {
		numberAnalyzer.run(new MockInputRow().put(col2, 1l), 1);
		numberAnalyzer.run(new MockInputRow().put(col2, 2l), 1);
		numberAnalyzer.run(new MockInputRow().put(col2, 3l), 1);
		numberAnalyzer.run(new MockInputRow().put(col2, 4l), 1);
		numberAnalyzer.run(new MockInputRow().put(col2, 5l), 1);

		CrosstabResult result = numberAnalyzer.getResult();
		String[] resultLines = new CrosstabTextRenderer().render(result).split(
				"\n");
		assertEquals(10, resultLines.length);
		assertEquals("                      foo    bar   w00p ", resultLines[0]);
		assertEquals("Highest value      <null>      5 <null> ", resultLines[1]);
		assertEquals("Lowest value       <null>      1 <null> ", resultLines[2]);
		assertEquals("Sum                <null>     15 <null> ", resultLines[3]);
		assertEquals("Mean               <null>      3 <null> ", resultLines[4]);
		assertEquals("Geometric mean     <null>   2,61 <null> ", resultLines[5]);
		assertEquals("Standard deviation <null>   1,58 <null> ", resultLines[6]);
		assertEquals("Variance           <null>    2,5 <null> ", resultLines[7]);
		assertEquals("Null values             5      0      5 ", resultLines[8]);
		assertEquals("Non-null values         0      5      0 ", resultLines[9]);
	}

	public void testSimpleRun() throws Exception {
		numberAnalyzer.run(new MockInputRow().put(col1, 123.4f)
				.put(col2, 1234l).put(col3, (byte) 12), 1);
		numberAnalyzer.run(new MockInputRow().put(col1, 567.8f)
				.put(col2, 5678l).put(col3, (byte) 34), 1);

		CrosstabResult result = numberAnalyzer.getResult();
		String[] resultLines = new CrosstabTextRenderer().render(result).split(
				"\n");
		assertEquals(10, resultLines.length);
		assertEquals("                      foo    bar   w00p ", resultLines[0]);
		assertEquals("Highest value       567,8   5678     34 ", resultLines[1]);
		assertEquals("Lowest value        123,4   1234     12 ", resultLines[2]);
		assertEquals("Sum                 691,2   6912     46 ", resultLines[3]);
		assertEquals("Mean                345,6   3456     23 ", resultLines[4]);
		assertEquals("Geometric mean      264,7 2647,01   20,2 ",
				resultLines[5]);
		assertEquals("Standard deviation 314,24 3142,38  15,56 ",
				resultLines[6]);
		assertEquals("Variance           98745,67 9874568    242 ",
				resultLines[7]);
		assertEquals("Null values             0      0      0 ", resultLines[8]);
		assertEquals("Non-null values         2      2      2 ", resultLines[9]);
	}
}
