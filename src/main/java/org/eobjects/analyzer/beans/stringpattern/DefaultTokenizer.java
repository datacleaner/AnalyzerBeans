package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class DefaultTokenizer implements Serializable, Tokenizer {

	private static final long serialVersionUID = 1L;
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
		List<SimpleToken> tokens = preliminaryTokenize(string);

		if (_configuration.isTokenTypeEnabled(TokenType.MIXED)) {
			tokens = flattenMixedTokens(tokens);
		}

		Set<Character> decimalSeparatorCharacters = _configuration
				.getDecimalSeparatorCharacters();
		if (!decimalSeparatorCharacters.isEmpty()) {
			tokens = flattenDecimalNumberTokens(tokens,
					decimalSeparatorCharacters);
		}

		return tokens;
	}

	protected static List<SimpleToken> preliminaryTokenize(String string) {
		LinkedList<SimpleToken> result = new LinkedList<SimpleToken>();
		SimpleToken lastToken = null;

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if ('-' == c) {
				// the meaning of '-' is dependent on the next token (maybe it's
				// a negative number operator)
				lastToken = registerChar(result, lastToken, c,
						TokenType.UNDEFINED);
			} else if (Character.isDigit(c)) {
				lastToken = registerChar(result, lastToken, c, TokenType.NUMBER);
			} else if (Character.isLetter(c)) {
				lastToken = registerChar(result, lastToken, c, TokenType.TEXT);
			} else if (Character.isWhitespace(c)) {
				lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
			} else {
				lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
			}
		}

		for (ListIterator<SimpleToken> it = result.listIterator(); it.hasNext();) {
			SimpleToken token = it.next();
			if (token.getType() == TokenType.UNDEFINED) {
				if ("-".equals(token.getString())) {
					int nextIndex = it.nextIndex();
					if (nextIndex != -1) {
						SimpleToken nextToken = result.get(nextIndex);
						if (nextToken.getType() == TokenType.NUMBER) {
							nextToken.prependChar('-');
							it.remove();
							continue;
						}
					}
					token.setType(TokenType.DELIM);
				}
			}
		}

		SimpleToken previousToken = null;
		// concat similar typed tokens
		for (ListIterator<SimpleToken> it = result.listIterator(); it.hasNext();) {
			SimpleToken token = it.next();
			if (previousToken == null
					|| token.getType() != previousToken.getType()) {
				// move on
				previousToken = token;
			} else {
				// concat
				previousToken.appendString(token.getString());
				it.remove();
			}
		}

		return result;
	}

	private static SimpleToken registerChar(LinkedList<SimpleToken> result,
			SimpleToken lastToken, char c, TokenType tokenType) {
		if (lastToken == null) {
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		} else if (lastToken.getType() == tokenType) {
			lastToken.appendChar(c);
		} else {
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		}
		return lastToken;
	}

	public static List<SimpleToken> flattenDecimalNumberTokens(
			List<SimpleToken> tokens, Set<Character> decimalSeparatorCharacters) {
		SimpleToken previousToken = null;
		for (ListIterator<SimpleToken> it = tokens.listIterator(); it.hasNext();) {
			SimpleToken token = it.next();
			if (previousToken == null) {
				previousToken = token;
			} else {
				if (token.getType() == TokenType.DELIM
						&& previousToken.getType() == TokenType.NUMBER) {
					if (token.getString().length() == 1) {
						char tokenChar = token.getString().charAt(0);
						if (decimalSeparatorCharacters.contains(tokenChar)) {
							if (it.hasNext()) {
								SimpleToken nextToken = it.next();
								if (nextToken.getType() == TokenType.NUMBER) {
									if (!isInteger(previousToken)) {
										previousToken.setType(TokenType.MIXED);
									}
									previousToken.appendChar(tokenChar);
									previousToken.appendString(nextToken
											.getString());
									it.remove();
									it.previous();
									it.remove();
									continue;
								}
							}
						}
					}
				}
				previousToken = token;
			}
		}
		return tokens;
	}

	private static boolean isInteger(Token token) {
		assert token.getType() == TokenType.NUMBER;
		String string = token.getString();
		for (int i = 0; i < string.length(); i++) {
			if (!Character.isDigit(string.charAt(i))) {
				return false;
			}
		}
		return true;
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
