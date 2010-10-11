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

		String s = new CrosstabTextRenderer().render(new CrosstabResult(null, c));
		assertEquals("      Male Female \nEU       1      3 \nUSA      2      4 \n", s);
	}
}
