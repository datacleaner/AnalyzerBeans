package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eobjects.analyzer.util.CharIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTokenizer implements Serializable, Tokenizer {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultTokenizer.class);

	private final TokenizerConfiguration _configuration;

	public DefaultTokenizer() {
		this(new TokenizerConfiguration());
	}

	public DefaultTokenizer(TokenizerConfiguration configuration) {
		if (configuration == null) {
			throw new NullPointerException(
					"configuration argument cannot be null");
		}
		_configuration = configuration;
	}

	public List<Token> tokenize(String string) {
		if (string == null) {
			return null;
		}

		List<Token> tokens;

		if (_configuration.isTokenTypeEnabled(TokenType.PREDEFINED)) {
			PredefinedTokenTokenizer tokenizer = new PredefinedTokenTokenizer(
					_configuration.getPredefinedTokens());
			tokens = tokenizer.tokenize(string);
			for (ListIterator<Token> it = tokens.listIterator(); it.hasNext();) {
				Token token = it.next();
				if (token.getType() == TokenType.UNDEFINED) {
					List<SimpleToken> replacementTokens = tokenizeInternal(token
							.getString());
					if (replacementTokens.size() > 1) {
						it.remove();
						for (SimpleToken replacementToken : replacementTokens) {
							it.add(replacementToken);
						}
					}
				}
			}
		} else {
			tokens = new LinkedList<Token>();
			tokens.addAll(tokenizeInternal(string));
		}

		return tokens;
	}

	private List<SimpleToken> tokenizeInternal(String string) {
		List<SimpleToken> tokens = preliminaryTokenize(string, _configuration);

		if (_configuration.isTokenTypeEnabled(TokenType.MIXED)) {
			tokens = flattenMixedTokens(tokens);
		}

		return tokens;
	}

	protected static List<SimpleToken> preliminaryTokenize(final String string,
			final TokenizerConfiguration configuration) {
		LinkedList<SimpleToken> result = new LinkedList<SimpleToken>();
		SimpleToken lastToken = null;

		CharIterator ci = new CharIterator(string);
		while (ci.hasNext()) {
			char c = ci.next();

			if (ci.is(configuration.getThousandSeparator())
					|| ci.is(configuration.getDecimalSeparator())) {
				boolean treatAsSeparator = false;
				if (lastToken != null
						&& lastToken.getType() == TokenType.NUMBER) {
					// there's a previous NUMBER token
					
					if (ci.hasNext()) {
						char next = ci.next();
						if (ci.isDigit()) {
							// the next token is also a NUMBER
							
							// now we're ready to assume that this is a
							// separator
							treatAsSeparator = true;
							lastToken = registerChar(result, lastToken, c,
									TokenType.NUMBER);
							lastToken = registerChar(result, lastToken, next,
									TokenType.NUMBER);
						} else {
							ci.previous();
						}
					}
				}

				if (!treatAsSeparator) {
					// the thousand separator is treated as a delim
					lastToken = registerChar(result, lastToken, c,
							TokenType.DELIM);
				}
			} else if (ci.is(configuration.getMinusSign())) {
				// the meaning of minus sign is dependent on the next token
				// (maybe it's the negative number operator)
				boolean treatAsOperator = false;
				if (ci.hasNext()) {
					char next = ci.next();
					if (ci.isDigit()) {
						// the minus sign was the number operator
						treatAsOperator = true;
						lastToken = registerChar(result, null, c,
								TokenType.NUMBER);
						lastToken = registerChar(result, lastToken, next,
								TokenType.NUMBER);
					} else {
						ci.previous();
					}
				}

				if (!treatAsOperator) {
					// the minus sign is treated as a delim
					lastToken = registerChar(result, lastToken, c,
							TokenType.DELIM);
				}
			} else if (ci.isDigit()) {
				lastToken = registerChar(result, lastToken, c, TokenType.NUMBER);
			} else if (ci.isLetter()) {
				lastToken = registerChar(result, lastToken, c, TokenType.TEXT);
			} else if (ci.isWhitespace()) {
				lastToken = registerChar(result, lastToken, c,
						TokenType.WHITESPACE);
			} else {
				lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
			}
		}

		return result;
	}

	private static SimpleToken registerChar(List<SimpleToken> result,
			SimpleToken lastToken, char c, TokenType tokenType) {
		if (lastToken == null) {
			logger.debug("Creating new {} token", tokenType);
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		} else if (lastToken.getType() == tokenType) {
			logger.debug("Appending to previous token", tokenType);
			lastToken.appendChar(c);
		} else {
			logger.debug("Creating new {} token", tokenType);
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		}
		logger.debug("{} registered as {}", c, tokenType);
		return lastToken;
	}

	private static boolean isInteger(Token token, Character thousandSeparator) {
		assert token.getType() == TokenType.NUMBER;
		CharIterator it = new CharIterator(token.getString());
		while (it.hasNext()) {
			it.next();
			if (!it.isDigit()) {
				if (!it.is(thousandSeparator)) {
					return false;
				}
			}
		}
		return false;
	}

	public static List<SimpleToken> flattenMixedTokens(List<SimpleToken> tokens) {
		SimpleToken previousToken = null;
		for (ListIterator<SimpleToken> it = tokens.listIterator(); it.hasNext();) {
			SimpleToken token = it.next();
			if (previousToken == null) {
				previousToken = token;
			} else {
				TokenType previousType = previousToken.getType();
				TokenType currentType = token.getType();
				if (isMixedCandidate(previousType)
						&& isMixedCandidate(currentType)) {
					previousToken.appendString(token.getString());
					previousToken.setType(TokenType.MIXED);
					it.remove();
				} else {
					previousToken = token;
				}
			}
		}
		return tokens;
	}

	private static boolean isMixedCandidate(TokenType type) {
		return type == TokenType.MIXED || type == TokenType.NUMBER
				|| type == TokenType.TEXT;
	}
}
