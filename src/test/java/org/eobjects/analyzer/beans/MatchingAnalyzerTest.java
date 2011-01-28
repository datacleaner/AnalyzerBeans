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
package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;

import junit.framework.TestCase;

public class MatchingAnalyzerTest extends TestCase {

	private final MockInputColumn<String> column2 = new MockInputColumn<String>("PERSON", String.class);
	private final MockInputColumn<String> column1 = new MockInputColumn<String>("GREETING", String.class);
	private final Dictionary dict1 = new SimpleDictionary("Greetings", "Hi", "Hello", "Howdy");
	private final Dictionary dict2 = new SimpleDictionary("Male names", "John", "Joe");
	private final Dictionary dict3 = new SimpleDictionary("Female names", "Barbara", "Jane");
	private final StringPattern sp1 = new SimpleStringPattern("Correct case word", "Aaaaaa");
	private final StringPattern sp2 = new SimpleStringPattern("Lowercase word", "aaaaaa");

	public void testMultipleColumnsDictionariesStringPatterns() throws Exception {
		final InputColumn<?>[] columns = { column1, column2 };
		final Dictionary[] dictionaries = { dict1, dict2, dict3 };
		final StringPattern[] stringPatterns = { sp1, sp2 };
		final MatchingAnalyzer analyzer = new MatchingAnalyzer(columns, dictionaries, stringPatterns);

		analyzer.init();

		analyzer.run(new MockInputRow().put(column1, "Hey").put(column2, "Joe"), 1);
		analyzer.run(new MockInputRow().put(column1, "Hi").put(column2, "John"), 1);
		analyzer.run(new MockInputRow().put(column1, "Hello").put(column2, "World"), 1);
		analyzer.run(new MockInputRow().put(column1, "Hello").put(column2, "Jane"), 1);

		BooleanAnalyzerResult result = analyzer.getResult();

		String[] resultLines = new CrosstabTextRenderer().render(result.getColumnStatisticsCrosstab()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals(
				"                 GREETING in 'Greetings'     GREETING in 'Male names'   GREETING in 'Female names' GREETING 'Correct case word'    GREETING 'Lowercase word'        PERSON in 'Greetings'       PERSON in 'Male names'     PERSON in 'Female names'   PERSON 'Correct case word'      PERSON 'Lowercase word' ",
				resultLines[0]);
		assertEquals(
				"Row count                              4                            4                            4                            4                            4                            4                            4                            4                            4                            4 ",
				resultLines[1]);
		assertEquals(
				"Null count                             0                            0                            0                            0                            0                            0                            0                            0                            0                            0 ",
				resultLines[2]);
		assertEquals(
				"True count                             3                            0                            0                            4                            0                            0                            2                            1                            4                            0 ",
				resultLines[3]);
		assertEquals(
				"False count                            1                            4                            4                            0                            4                            4                            2                            3                            0                            4 ",
				resultLines[4]);
	}
}
