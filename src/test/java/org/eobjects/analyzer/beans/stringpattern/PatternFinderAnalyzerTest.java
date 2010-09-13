package org.eobjects.analyzer.beans.stringpattern;

import junit.framework.TestCase;

import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.MockInputColumn;
import org.eobjects.analyzer.test.MockInputRow;

public class PatternFinderAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		// simply test that the analyzer is valid
		AnnotationBasedAnalyzerBeanDescriptor<PatternFinderAnalyzer> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(PatternFinderAnalyzer.class);
		assertEquals("Pattern finder", descriptor.getDisplayName());
	}

	public void testEmployeeTitles() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title",
				String.class);

		pf.setColumn(column);
		pf.setDiscriminateTextCase(true);

		pf.init();

		pf.run(new MockInputRow().put(column, "Sales director"), 1);

		String[] resultLines;
		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split(
				"\n");
		assertEquals(2, resultLines.length);
		assertEquals("               Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaa aaaaaaaa           1 Sales director ",
				resultLines[1]);

		pf.run(new MockInputRow().put(column, "Key account manager"), 1);
		pf.run(new MockInputRow().put(column, "Account manager"), 1);
		pf.run(new MockInputRow().put(column, "Sales manager (EMEA)"), 1);

		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split(
				"\n");
		assertEquals(4, resultLines.length);
		assertEquals("                     Match count Sample      ",
				resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa               2 Sales director ",
				resultLines[1]);
		assertEquals("Aaa aaaaaaa aaaaaaa            1 Key account manager ",
				resultLines[2]);
		assertEquals("Aaaaa aaaaaaa (AAAA)           1 Sales manager (EMEA) ",
				resultLines[3]);

		pf.run(new MockInputRow().put(column, "Sales Manager, USA"), 1);
		pf.run(new MockInputRow().put(column, "Account Manager (USA)"), 1);
		pf.run(new MockInputRow().put(column, "1st on the phone"), 1);

		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split(
				"\n");
		assertEquals(7, resultLines.length);
		assertEquals("                      Match count Sample      ",
				resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa                2 Sales director ",
				resultLines[1]);
		assertEquals("1st aa aaa aaaaa                1 1st on the phone ",
				resultLines[2]);
		assertEquals("Aaa aaaaaaa aaaaaaa             1 Key account manager ",
				resultLines[3]);
		assertEquals("Aaaaa Aaaaaaa, AAA              1 Sales Manager, USA ",
				resultLines[4]);
		assertEquals("Aaaaa aaaaaaa (AAAA)            1 Sales manager (EMEA) ",
				resultLines[5]);
		assertEquals(
				"Aaaaaaa Aaaaaaa (AAA)           1 Account Manager (USA) ",
				resultLines[6]);
	}
}
