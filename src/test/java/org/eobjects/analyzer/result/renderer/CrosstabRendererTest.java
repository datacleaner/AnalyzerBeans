package org.eobjects.analyzer.result.renderer;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.result.Crosstab;

public class CrosstabRendererTest extends TestCase {

	public void testOneDimension() throws Exception {
		Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Region");
		c.where("Region", "EU").put(1, true);
		c.where("Region", "USA").put(2, true);
		c.where("Region", "Asia").put(3, true);

		CrosstabRenderer crosstabRenderer = new CrosstabRenderer(c);
		String result = crosstabRenderer
				.render(new HtmlCrosstabRendererCallback());
		assertEquals("<table><tr><td>EU</td><td>USA</td><td>Asia</td></tr>"
				+ "<tr><td>1</td><td>2</td><td>3</td></tr></table>",
				result.replaceAll("\"", "'"));

		crosstabRenderer.makeVertical(c.getDimension(0));
		result = crosstabRenderer.render(new HtmlCrosstabRendererCallback());
		assertEquals("<table><tr><td>EU</td><td>1</td></tr>"
				+ "<tr><td>USA</td><td>2</td></tr>"
				+ "<tr><td>Asia</td><td>3</td></tr></table>",
				result.replaceAll("\"", "'"));
	}

	public void testMultipleDimensions() throws Exception {
		// creates a crosstab of some metric (simply iterated for simplicity)
		// based on person characteristica, examplified with Region (EU and
		// USA), Age-group (children, teenagers and adult)
		// and Gender (male and female)

		Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Region",
				"Age-group", "Gender", "Native");
		String[] genderValues = { "Male", "Female" };
		String[] regionValues = { "EU", "USA" };
		String[] ageGroupValues = { "Child", "Teenager", "Adult" };
		String[] nativeValues = { "Yes", "No, immigrant",
				"No, second-generation" };

		int i = 0;
		for (String gender : genderValues) {
			for (String region : regionValues) {
				for (String ageGroup : ageGroupValues) {
					for (String nativeValue : nativeValues) {
						c.where("Region", region).where("Age-group", ageGroup)
								.where("Gender", gender)
								.where("Native", nativeValue).put(i, true);
						i++;
					}
				}
			}
		}

		String[] dimensionNames = c.getDimensionNames();
		assertEquals("[Region, Age-group, Gender, Native]",
				Arrays.toString(dimensionNames));

		CrosstabRenderer crosstabRenderer = new CrosstabRenderer(c);

		// auto-assigned axises
		assertEquals(
				"<table><tr><td></td><td></td><td colspan='3'>EU</td><td colspan='3'>USA</td></tr>"
						+ "<tr><td></td><td></td><td>Child</td><td>Teenager</td><td>Adult</td><td>Child</td><td>Teenager</td><td>Adult</td></tr>"
						+ "<tr><td rowspan='3'>Male</td><td>Yes</td><td>0</td><td>3</td><td>6</td><td>9</td><td>12</td><td>15</td></tr>"
						+ "<tr><td>No, immigrant</td><td>1</td><td>4</td><td>7</td><td>10</td><td>13</td><td>16</td></tr>"
						+ "<tr><td>No, second-generation</td><td>2</td><td>5</td><td>8</td><td>11</td><td>14</td><td>17</td></tr>"
						+ "<tr><td rowspan='3'>Female</td><td>Yes</td><td>18</td><td>21</td><td>24</td><td>27</td><td>30</td><td>33</td></tr>"
						+ "<tr><td>No, immigrant</td><td>19</td><td>22</td><td>25</td><td>28</td><td>31</td><td>34</td></tr>"
						+ "<tr><td>No, second-generation</td><td>20</td><td>23</td><td>26</td><td>29</td><td>32</td><td>35</td></tr></table>",
				crosstabRenderer.render(new HtmlCrosstabRendererCallback())
						.replaceAll("\"", "'"));

		// try all vertical
		crosstabRenderer.makeVertical(c.getDimension(0));
		crosstabRenderer.makeVertical(c.getDimension(1));
		crosstabRenderer.makeVertical(c.getDimension(2));
		crosstabRenderer.makeVertical(c.getDimension(3));
		assertEquals(
				"<table><tr><td rowspan='18'>Male</td><td rowspan='6'>Yes</td><td rowspan='3'>EU</td><td>Child</td><td>0</td></tr>"
						+ "<tr><td>Teenager</td><td>3</td></tr>"
						+ "<tr><td>Adult</td><td>6</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>9</td></tr>"
						+ "<tr><td>Teenager</td><td>12</td></tr>"
						+ "<tr><td>Adult</td><td>15</td></tr>"
						+ "<tr><td rowspan='6'>No, immigrant</td><td rowspan='3'>EU</td><td>Child</td><td>1</td></tr>"
						+ "<tr><td>Teenager</td><td>4</td></tr>"
						+ "<tr><td>Adult</td><td>7</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>10</td></tr>"
						+ "<tr><td>Teenager</td><td>13</td></tr>"
						+ "<tr><td>Adult</td><td>16</td></tr>"
						+ "<tr><td rowspan='6'>No, second-generation</td><td rowspan='3'>EU</td><td>Child</td><td>2</td></tr>"
						+ "<tr><td>Teenager</td><td>5</td></tr>"
						+ "<tr><td>Adult</td><td>8</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>11</td></tr>"
						+ "<tr><td>Teenager</td><td>14</td></tr>"
						+ "<tr><td>Adult</td><td>17</td></tr>"
						+ "<tr><td rowspan='18'>Female</td><td rowspan='6'>Yes</td><td rowspan='3'>EU</td><td>Child</td><td>18</td></tr>"
						+ "<tr><td>Teenager</td><td>21</td></tr>"
						+ "<tr><td>Adult</td><td>24</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>27</td></tr>"
						+ "<tr><td>Teenager</td><td>30</td></tr>"
						+ "<tr><td>Adult</td><td>33</td></tr>"
						+ "<tr><td rowspan='6'>No, immigrant</td><td rowspan='3'>EU</td><td>Child</td><td>19</td></tr>"
						+ "<tr><td>Teenager</td><td>22</td></tr>"
						+ "<tr><td>Adult</td><td>25</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>28</td></tr>"
						+ "<tr><td>Teenager</td><td>31</td></tr>"
						+ "<tr><td>Adult</td><td>34</td></tr>"
						+ "<tr><td rowspan='6'>No, second-generation</td><td rowspan='3'>EU</td><td>Child</td><td>20</td></tr>"
						+ "<tr><td>Teenager</td><td>23</td></tr>"
						+ "<tr><td>Adult</td><td>26</td></tr>"
						+ "<tr><td rowspan='3'>USA</td><td>Child</td><td>29</td></tr>"
						+ "<tr><td>Teenager</td><td>32</td></tr>"
						+ "<tr><td>Adult</td><td>35</td></tr></table>",
				crosstabRenderer.render(new HtmlCrosstabRendererCallback())
						.replaceAll("\"", "'"));
	}
}
