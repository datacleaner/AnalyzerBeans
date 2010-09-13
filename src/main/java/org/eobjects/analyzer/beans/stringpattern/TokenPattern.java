package org.eobjects.analyzer.beans.stringpattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenPattern {

	private TokenizerConfiguration _configuration;
	private List<TokenPatternSymbol> _symbols;

	public TokenPattern(List<Token> tokens, TokenizerConfiguration configuration) {
		if (tokens == null) {
			throw new IllegalArgumentException("tokens cannot be null");
		}
		_symbols = new ArrayList<TokenPatternSymbol>(tokens.size());
		for (Token token : tokens) {
			_symbols.add(new TokenPatternSymbolImpl(token, configuration));
		}
		_configuration = configuration;
	}

	public boolean matches(List<Token> tokens) {
		if (_symbols.size() != tokens.size()) {
			return false;
		}

		Iterator<TokenPatternSymbol> it1 = _symbols.iterator();
		Iterator<Token> it2 = tokens.iterator();
		while (it1.hasNext()) {
			TokenPatternSymbol tokenSymbol = it1.next();
			Token token = it2.next();
			if (!tokenSymbol.matches(token, _configuration)) {
				return false;
			}
		}

		// it's a match. now expand sizes of tokens if needed

		it1 = _symbols.iterator();
		it2 = tokens.iterator();
		while (it1.hasNext()) {
			TokenPatternSymbol tokenSymbol = it1.next();
			Token token2 = it2.next();
			if (tokenSymbol.isExpandable()) {
				int length1 = tokenSymbol.length();
				int length2 = token2.length();
				if (length1 < length2) {
					int diff = length2 - length1;
					tokenSymbol.expandLenght(diff);
				}
			}
		}

		return true;
	}

	public String toSymbolicString() {
		StringBuilder sb = new StringBuilder();
		for (TokenPatternSymbol symbol : _symbols) {
			sb.append(symbol.toSymbolicString());
		}
		return sb.toString();
	}
}
