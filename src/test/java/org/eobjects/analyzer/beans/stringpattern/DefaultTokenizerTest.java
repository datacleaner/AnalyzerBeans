package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;

import junit.framework.TestCase;

public class DefaultTokenizerTest extends TestCase {

	public void testTokenizeInternal() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer
				.tokenizeInternal("hi \t123there - yay -10");
		assertEquals(8, tokens.size());
		assertEquals("Token[hi (TEXT)]", tokens.get(0).toString());
		assertEquals("Token[ \t (DELIM)]", tokens.get(1).toString());
		assertEquals("Token[123 (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token[there (TEXT)]", tokens.get(3).toString());
		assertEquals("Token[ -  (DELIM)]", tokens.get(4).toString());
		assertEquals("Token[yay (TEXT)]", tokens.get(5).toString());
		assertEquals("Token[  (DELIM)]", tokens.get(6).toString());
		assertEquals("Token[-10 (NUMBER)]", tokens.get(7).toString());
		
		tokens = DefaultTokenizer
		.tokenizeInternal("w00p");
	}
}
