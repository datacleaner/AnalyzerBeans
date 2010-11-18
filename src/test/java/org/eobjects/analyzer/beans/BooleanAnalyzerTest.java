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

		String[] resultLines = new CrosstabTextRenderer().render(ba.getResult().getColumnStatisticsCrosstab()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals("                b1     b2 ", resultLines[0]);
		assertEquals("Row count        7      7 ", resultLines[1]);
		assertEquals("Null count       0      0 ", resultLines[2]);
		assertEquals("True count       5      6 ", resultLines[3]);
		assertEquals("False count      2      1 ", resultLines[4]);

		resultLines = new CrosstabTextRenderer().render(ba.getResult().getValueCombinationCrosstab()).split("\n");
		assertEquals(4, resultLines.length);
		assertEquals("                      b1        b2 Frequency ", resultLines[0]);
		assertEquals("Most frequent          1         1         4 ", resultLines[1]);
		assertEquals("Combination 1          0         1         2 ", resultLines[2]);
		assertEquals("Least frequent         1         0         1 ", resultLines[3]);
	}
}
