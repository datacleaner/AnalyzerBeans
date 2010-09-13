package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;

import junit.framework.TestCase;

public class TokenPatternTest extends TestCase {

	private TokenizerConfiguration configuration = new TokenizerConfiguration(
			true, '.', ',', '-');

	public void testSimpleMatching() throws Exception {
		DefaultTokenizer tokenizer = new DefaultTokenizer(configuration);
		List<Token> tokens;

		tokens = tokenizer.tokenize("hello world");

		TokenPattern tp1 = new TokenPattern(tokens, configuration);
		assertEquals("aaaaa aaaaa", tp1.toSymbolicString());

		tokens = tokenizer.tokenize("hello pinnochio");
		assertTrue(tp1.matches(tokens));
		assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());
		
		tokens = tokenizer.tokenize("hello you");
		assertTrue(tp1.matches(tokens));
		assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());
		
		tokens = tokenizer.tokenize("hello Mr. FanDango");
		assertFalse(tp1.matches(tokens));
		assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());
		
		configuration.setDiscriminateTextCase(true);
		tokens = tokenizer.tokenize("hello Mr. FanDango");
		TokenPattern tp2 = new TokenPattern(tokens, configuration);
		assertEquals("aaaaa Aa. AaaAaaaa", tp2.toSymbolicString());
	}
}
