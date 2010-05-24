package org.eobjects.analyzer.beans.stringpattern;

public class PredefinedToken implements Token {

	private PredefinedTokenDefinition _predefinedTokenDefintion;
	private String _string;

	public PredefinedToken(PredefinedTokenDefinition tokenDefinition,
			String string) {
		_predefinedTokenDefintion = tokenDefinition;
		_string = string;
	}

	public PredefinedTokenDefinition getPredefinedTokenDefintion() {
		return _predefinedTokenDefintion;
	}

	@Override
	public String getString() {
		return _string;
	}

	@Override
	public TokenType getType() {
		return TokenType.PREDEFINED;
	}

	@Override
	public String toString() {
		return "Token[" + _string + " (PREDEFINED "
				+ _predefinedTokenDefintion.getName() + ")]";
	}
}
