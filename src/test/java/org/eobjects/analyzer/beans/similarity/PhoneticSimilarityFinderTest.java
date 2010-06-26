package org.eobjects.analyzer.beans.similarity;

import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class PhoneticSimilarityFinderTest extends TestCase {

	public void testGetResult() throws Exception {
		PhoneticSimilarityFinder analyzer = new PhoneticSimilarityFinder();
		analyzer.setValues(new HashMap<String, Integer>());

		// 4 similar sounding kasper's
		analyzer.run("kasper", 1);
		analyzer.run("gasper", 1);
		analyzer.run("qasper", 1);
		analyzer.run("kaspar", 1);

		// 3 similar sounding hello's
		analyzer.run("hello", 1);
		analyzer.run("hallo", 1);
		analyzer.run("hellow", 1);

		// something without similarities
		analyzer.run("wowsers", 1);

		SimilarityResult result = analyzer.getResult();

		assertEquals(3, result.getSimilarValues("kasper").size());
		assertEquals("{kaspar,gasper,qasper}", ArrayUtils.toString(result
				.getSimilarValues("kasper").toArray()));
		assertEquals(3, result.getSimilarValues("gasper").size());
		assertEquals(3, result.getSimilarValues("qasper").size());
		assertEquals(3, result.getSimilarValues("kaspar").size());

		assertEquals("[hellow, hallo]",
				ArrayUtils.toString(result.getSimilarValues("hello")));
		assertEquals(2, result.getSimilarValues("hallo").size());
		assertEquals(2, result.getSimilarValues("hellow").size());
		assertEquals(0, result.getSimilarValues("wowsers").size());

		assertEquals(9, result.getSimilarValues().size());
	}
}
