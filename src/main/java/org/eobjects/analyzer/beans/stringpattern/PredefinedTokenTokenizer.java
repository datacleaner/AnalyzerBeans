package org.eobjects.analyzer.beans.stringpattern;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredefinedTokenTokenizer implements Tokenizer {

	private List<PredefinedTokenDefinition> _predefinedTokenDefitions;

	public PredefinedTokenTokenizer(PredefinedTokenDefinition... predefinedTokenDefinitions) {
		_predefinedTokenDefitions = new LinkedList<PredefinedTokenDefinition>();
		for (PredefinedTokenDefinition predefinedToken : predefinedTokenDefinitions) {
			_predefinedTokenDefitions.add(predefinedToken);
		}
	}

	public PredefinedTokenTokenizer(List<PredefinedTokenDefinition> predefinedTokenDefinitions) {
		_predefinedTokenDefitions = predefinedTokenDefinitions;
	}

	/**
	 * Will only return either tokens with type PREDEFINED or UNDEFINED
	 */
	@Override
	public List<Token> tokenize(String s) {
		LinkedList<Token> result = new LinkedList<Token>();
		result.add(new UndefinedToken(s));

		for (PredefinedTokenDefinition predefinedTokenDefinition : _predefinedTokenDefitions) {
			Set<Pattern> patterns = predefinedTokenDefinition.getTokenRegexPatterns();
			for (Pattern pattern : patterns) {
				for (ListIterator<Token> it = result.listIterator(); it.hasNext();) {
					Token token = it.next();
					if (token instanceof UndefinedToken) {
						List<Token> replacementTokens = tokenizeInternal(token.getString(), predefinedTokenDefinition,
								pattern);
						if (replacementTokens.size() > 1) {
							it.remove();
							for (Token newToken : replacementTokens) {
								it.add(newToken);
							}
						}
					}
				}
			}
		}

		return result;
	}

	protected static List<Token> tokenizeInternal(String string, PredefinedTokenDefinition predefinedTokenDefinition,
			Pattern pattern) {
		LinkedList<Token> result = new LinkedList<Token>();
		result.add(new UndefinedToken(string));

		for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {

			int start = matcher.start();
			int end = matcher.end();

			result.removeLast();

			if (start > 0) {
				result.add(new UndefinedToken(string.substring(0, start)));
			}
			result.add(new PredefinedToken(predefinedTokenDefinition, string.substring(start, end)));

			if (end == string.length()) {
				break;
			}

			string = string.substring(end);
			result.add(new UndefinedToken(string));
		}

		return result;
	}
}
