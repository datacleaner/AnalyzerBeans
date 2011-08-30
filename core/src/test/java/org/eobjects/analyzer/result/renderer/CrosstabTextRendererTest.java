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
package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;

import junit.framework.TestCase;

public class CrosstabTextRendererTest extends TestCase {

	public void testSimpleCrosstab() throws Exception {
		Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Gender", "Region");
		c.where("Gender", "Male").where("Region", "EU").put(1, true);
		c.where("Gender", "Male").where("Region", "USA").put(2, true);
		c.where("Gender", "Female").where("Region", "EU").put(3, true);
		c.where("Gender", "Female").where("Region", "USA").put(4, true);

		String s = new CrosstabTextRenderer().render(new CrosstabResult(c));
		assertEquals("      Male Female \nEU       1      3 \nUSA      2      4 \n", s);
	}
}
