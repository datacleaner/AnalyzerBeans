package org.eobjects.analyzer.beans.similarity;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class SimilarityResultTest extends TestCase {

	public void testGetValues() throws Exception {
		Set<SimilarValues> similarValues = new HashSet<SimilarValues>();
		similarValues.add(new SimilarValues("hello", "world"));
		similarValues.add(new SimilarValues("foo", "bar"));
		similarValues.add(new SimilarValues("foo", "foobar"));
		similarValues.add(new SimilarValues("foo", "world"));
		SimilarityResult result = new SimilarityResult(
				PhoneticSimilarityFinder.class, similarValues);
		
		assertEquals("[hello, foobar, foo, bar, world]", result.getValues()
				.toString());
		assertEquals(PhoneticSimilarityFinder.class, result.getProducerClass());
	}
}
