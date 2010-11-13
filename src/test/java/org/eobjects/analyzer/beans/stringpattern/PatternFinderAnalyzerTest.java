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
package org.eobjects.analyzer.beans.stringpattern;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;

public class PatternFinderAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		// simply test that the analyzer is valid
		AnnotationBasedAnalyzerBeanDescriptor<PatternFinderAnalyzer> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(PatternFinderAnalyzer.class);
		assertEquals("Pattern finder", descriptor.getDisplayName());
	}

	public void testSingleToken() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);

		pf.init();

		pf.run(new MockInputRow().put(column, "blabla"), 1);

		assertEquals("Crosstab:\nMatch count,aaaaaa: 1\nSample,aaaaaa: blabla", pf.getResult().getCrosstab().toString());
	}

	public void testEmployeeTitles() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);
		pf.setDiscriminateTextCase(true);

		pf.init();

		pf.run(new MockInputRow().put(column, "Sales director"), 1);

		String[] resultLines;
		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(2, resultLines.length);
		assertEquals("               Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaa aaaaaaaa           1 Sales director ", resultLines[1]);

		pf.run(new MockInputRow().put(column, "Key account manager"), 1);
		pf.run(new MockInputRow().put(column, "Account manager"), 1);
		pf.run(new MockInputRow().put(column, "Sales manager (EMEA)"), 1);

		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(4, resultLines.length);
		assertEquals("                     Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa               2 Sales director ", resultLines[1]);
		assertEquals("Aaa aaaaaaa aaaaaaa            1 Key account manager ", resultLines[2]);
		assertEquals("Aaaaa aaaaaaa (AAAA)           1 Sales manager (EMEA) ", resultLines[3]);

		pf.run(new MockInputRow().put(column, "Sales Manager, USA"), 1);
		pf.run(new MockInputRow().put(column, "Account Manager (USA)"), 1);
		pf.run(new MockInputRow().put(column, "1st on the phone"), 1);

		resultLines = new CrosstabTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(7, resultLines.length);
		assertEquals("                      Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa                2 Sales director ", resultLines[1]);
		assertEquals("1st aa aaa aaaaa                1 1st on the phone ", resultLines[2]);
		assertEquals("Aaa aaaaaaa aaaaaaa             1 Key account manager ", resultLines[3]);
		assertEquals("Aaaaa Aaaaaaa, AAA              1 Sales Manager, USA ", resultLines[4]);
		assertEquals("Aaaaa aaaaaaa (AAAA)            1 Sales manager (EMEA) ", resultLines[5]);
		assertEquals("Aaaaaaa Aaaaaaa (AAA)           1 Account Manager (USA) ", resultLines[6]);
	}

	public void testEmailAddresses() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);
		pf.setDiscriminateTextCase(true);

		pf.init();

		pf.run(new MockInputRow().put(column, "kasper@eobjects.dk"), 1);
		pf.run(new MockInputRow().put(column, "kasper.sorensen@eobjects.dk"), 1);
		pf.run(new MockInputRow().put(column, "john@doe.com"), 1);
		pf.run(new MockInputRow().put(column, "john.doe@company.com"), 1);

		String[] resultLines = new CrosstabTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(3, resultLines.length);
		assertEquals("                             Match count Sample      ", resultLines[0]);
		assertEquals("aaaaaa.aaaaaaaa@aaaaaaaa.aaa           2 kasper.sorensen@eobjects.dk ", resultLines[1]);
		assertEquals("aaaaaa@aaaaaaaa.aaa                    2 kasper@eobjects.dk ", resultLines[2]);
	}
}
