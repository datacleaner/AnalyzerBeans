package org.eobjects.analyzer.beans.standardize;

import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;

import junit.framework.TestCase;

public class EmailStandardizerTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		EmailStandardizerTransformer transformer = new EmailStandardizerTransformer();

		String[] result = transformer.transform("kasper@eobjects.dk");
		assertEquals(2, result.length);
		assertEquals("kasper", result[0]);
		assertEquals("eobjects.dk", result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.dk");
		assertEquals(2, result.length);
		assertEquals("kasper.sorensen", result[0]);
		assertEquals("eobjects.dk", result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.d");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);

		result = transformer.transform("@eobjects.dk");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.organization");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);
	}
}
