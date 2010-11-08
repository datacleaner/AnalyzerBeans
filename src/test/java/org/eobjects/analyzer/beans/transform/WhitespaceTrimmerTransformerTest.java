package org.eobjects.analyzer.beans.transform;

import junit.framework.TestCase;

public class WhitespaceTrimmerTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		WhitespaceTrimmerTransformer t = new WhitespaceTrimmerTransformer(false, true, false);
		assertEquals(" hello  world", t.transform(" hello  world "));
		assertNull(t.transform((String) null));

		t = new WhitespaceTrimmerTransformer(true, false, false);
		assertEquals("hello  world ", t.transform(" hello  world "));

		t = new WhitespaceTrimmerTransformer(true, true, true);
		assertEquals("hello world", t.transform(" hello  world "));
		assertEquals("hello world", t.transform(" hello\t\tworld "));
		assertEquals("hello world", t.transform(" hello\tworld "));
	}
}
