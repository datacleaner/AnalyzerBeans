package org.eobjects.analyzer.beans.stringpattern;

import junit.framework.TestCase;

public class TokenPatternSymbolImplTest extends TestCase {

	private TokenizerConfiguration configuration = new TokenizerConfiguration(
			true, '.', ',', '-');

	public void testMatchText() throws Exception {
		configuration.setDiscriminateTextCase(false);
		configuration.setDistriminateTokenLength(TokenType.TEXT, true);

		TokenPatternSymbol t1 = new TokenPatternSymbolImpl(new SimpleToken(
				TokenType.TEXT, "hello"), configuration);
		SimpleToken t2;

		t2 = new SimpleToken(TokenType.TEXT, "WORLD");
		assertTrue(t1.matches(t2, configuration));

		t2 = new SimpleToken(TokenType.TEXT, "hi");
		assertFalse(t1.matches(t2, configuration));

		configuration.setDistriminateTokenLength(TokenType.TEXT, false);

		assertTrue(t1.matches(t2, configuration));

		configuration.setDiscriminateTextCase(true);

		t2 = new SimpleToken(TokenType.TEXT, "WORLD");
		assertFalse(t1.matches(t2, configuration));
	}

	public void testMatchNumber() throws Exception {
		configuration.setDiscriminateDecimalNumbers(true);
		configuration.setDiscriminateNegativeNumbers(true);

		TokenPatternSymbol t1 = new TokenPatternSymbolImpl(new SimpleToken(
				TokenType.NUMBER, "123"), configuration);
		SimpleToken t2;

		t2 = new SimpleToken(TokenType.NUMBER, "45678");
		assertTrue(t1.matches(t2, configuration));

		t2 = new SimpleToken(TokenType.NUMBER, "-45678");
		assertFalse(t1.matches(t2, configuration));

		t2 = new SimpleToken(TokenType.NUMBER, "45678.2");
		assertFalse(t1.matches(t2, configuration));

		configuration.setDiscriminateDecimalNumbers(false);
		assertTrue(t1.matches(t2, configuration));
		t2 = new SimpleToken(TokenType.NUMBER, "-45678");
		assertFalse(t1.matches(t2, configuration));

		configuration.setDiscriminateNegativeNumbers(false);
		assertTrue(t1.matches(t2, configuration));
		t2 = new SimpleToken(TokenType.NUMBER, "-45.678.4324");
		assertTrue(t1.matches(t2, configuration));
	}

	public void testMatchDifferentTypes() throws Exception {
		TokenPatternSymbol t1 = new TokenPatternSymbolImpl(new SimpleToken(
				TokenType.TEXT, "hello"), configuration);
		SimpleToken t2 = new SimpleToken(TokenType.NUMBER, "123");

		assertFalse(t1.matches(t2, null));
	}

	public void testMatchDelim() throws Exception {
		TokenPatternSymbol t1 = new TokenPatternSymbolImpl(new SimpleToken(
				TokenType.DELIM, ",-"), configuration);

		SimpleToken t2;
		t2 = new SimpleToken(TokenType.DELIM, ",-");

		assertTrue(t1.matches(t2, configuration));

		t2 = new SimpleToken(TokenType.DELIM, "-,");

		assertFalse(t1.matches(t2, configuration));
	}
}
