package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.util.CollectionUtils;

public class DefaultTokenizerTest extends TestCase {

	public void testPreliminaryTokenizeAndMixedTokens() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer
				.preliminaryTokenize("hi \t123there - yay -10");
		assertEquals(8, tokens.size());
		assertEquals("Token['hi' (TEXT)]", tokens.get(0).toString());
		assertEquals("Token[' \t' (DELIM)]", tokens.get(1).toString());
		assertEquals("Token['123' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token['there' (TEXT)]", tokens.get(3).toString());
		assertEquals("Token[' - ' (DELIM)]", tokens.get(4).toString());
		assertEquals("Token['yay' (TEXT)]", tokens.get(5).toString());
		assertEquals("Token[' ' (DELIM)]", tokens.get(6).toString());
		assertEquals("Token['-10' (NUMBER)]", tokens.get(7).toString());

		tokens = DefaultTokenizer.flattenMixedTokens(tokens);
		assertEquals(7, tokens.size());
		assertEquals("Token['123there' (MIXED)]", tokens.get(2).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("w00p");
		assertEquals(3, tokens.size());
		assertEquals("Token['w' (TEXT)]", tokens.get(0).toString());
		assertEquals("Token['00' (NUMBER)]", tokens.get(1).toString());
		assertEquals("Token['p' (TEXT)]", tokens.get(2).toString());

		tokens = DefaultTokenizer.flattenMixedTokens(tokens);
		assertEquals(1, tokens.size());
		assertEquals("Token['w00p' (MIXED)]", tokens.get(0).toString());
	}

	public void testDecimals() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer
				.preliminaryTokenize("yay 10.1 whut 20,632. hmm");
		assertEquals(13, tokens.size());

		tokens = DefaultTokenizer.flattenDecimalNumberTokens(tokens,
				CollectionUtils.set(',', '.'));
		assertEquals(9, tokens.size());

		assertEquals("Token['10.1' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token['20,632' (NUMBER)]", tokens.get(6).toString());
		assertEquals("Token['. ' (DELIM)]", tokens.get(7).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632.20213");
		tokens = DefaultTokenizer.flattenDecimalNumberTokens(tokens,
				CollectionUtils.set(',', '.'));
		assertEquals(1, tokens.size());
		assertEquals("Token['20,632.20213' (MIXED)]", tokens.get(0).toString());
	}
}
