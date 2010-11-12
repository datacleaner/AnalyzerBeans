package org.eobjects.analyzer.beans.similarity;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.result.SimilarityResult;

public class SimilarityResultTest extends TestCase {

	public void testGetValues() throws Exception {
		List<SimilarityGroup> similarValues = new ArrayList<SimilarityGroup>();
		similarValues.add(new SimilarityGroup("hello", "world"));
		similarValues.add(new SimilarityGroup("foo", "bar"));
		similarValues.add(new SimilarityGroup("foo", "foobar"));
		similarValues.add(new SimilarityGroup("foo", "world"));
		SimilarityResult result = new SimilarityResult(similarValues);

		assertEquals("[hello, foobar, foo, bar, world]", result.getValues().toString());
	}
}
