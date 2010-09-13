package org.eobjects.analyzer.beans.stringpattern;

public class UndefinedToken implements Token {

	private String _string;

	public UndefinedToken(String string) {
		_string = string;
	}

	public String getString() {
		return _string;
	}

	@Override
	public TokenType getType() {
		return TokenType.UNDEFINED;
	}

	@Override
	public String toString() {
		return "UndefinedToken['" + _string + "']";
	}

	@Override
	public int length() {
		return _string.length();
	}

	@Override
	public char charAt(int index) {
		return _string.charAt(index);
	}
}
