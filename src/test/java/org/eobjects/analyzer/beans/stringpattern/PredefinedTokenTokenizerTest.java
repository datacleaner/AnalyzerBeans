package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class PredefinedTokenTokenizerTest extends TestCase {

	public void testOverlappingPatterns() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition(
				"greeting", "hello .*", "hi .*");
		
		Set<Pattern> patterns = pt.getTokenRegexPatterns();
		assertEquals(2, patterns.size());
		for (Pattern pattern : patterns) {
			// both patterns can find a match here
			assertTrue(pattern.matcher("hello hi there").find());
		}
		
		List<Token> tokens = new PredefinedTokenTokenizer(pt).tokenize("hello hi there");
		assertEquals(2, tokens.size());
		assertEquals("UndefinedToken['hello ']", tokens.get(0).toString());
		assertEquals("Token['hi there' (PREDEFINED greeting)]", tokens.get(1).toString());
	}

	public void testTokenizeInternal() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition(
				"greeting", "hello");
		List<Token> tokens = PredefinedTokenTokenizer.tokenizeInternal(
				"hello there hello world", pt, pt.getTokenRegexPatterns()
						.iterator().next());
		assertEquals(4, tokens.size());

		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(0)
				.toString());
		assertEquals("UndefinedToken[' there ']", tokens.get(1).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(2)
				.toString());
		assertEquals("UndefinedToken[' world']", tokens.get(3).toString());

		tokens = PredefinedTokenTokenizer.tokenizeInternal("world hello", pt,
				pt.getTokenRegexPatterns().iterator().next());
		assertEquals(2, tokens.size());
		assertEquals("UndefinedToken['world ']", tokens.get(0).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(1)
				.toString());
	}

	public void testSimpleTokenSeparation() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition(
				"greeting", "hi", "hello", "howdy");

		PredefinedTokenTokenizer tokenizer = new PredefinedTokenTokenizer(pt);

		List<Token> tokens = tokenizer.tokenize("Well hello there world");
		assertEquals(3, tokens.size());

		assertEquals("UndefinedToken['Well ']", tokens.get(0).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(1)
				.toString());
		assertEquals("UndefinedToken[' there world']", tokens.get(2).toString());

		tokens = tokenizer.tokenize("howdy Well hello there hi world hi");
		assertEquals(7, tokens.size());
		assertEquals("Token['howdy' (PREDEFINED greeting)]", tokens.get(0)
				.toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(2)
				.toString());
		assertEquals("Token['hi' (PREDEFINED greeting)]", tokens.get(4)
				.toString());
		assertEquals("UndefinedToken[' world ']", tokens.get(5).toString());
		assertEquals("Token['hi' (PREDEFINED greeting)]", tokens.get(6)
				.toString());
	}
}
