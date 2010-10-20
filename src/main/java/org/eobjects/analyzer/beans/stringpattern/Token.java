package org.eobjects.analyzer.beans.stringpattern;

public interface Token {

	public TokenType getType();

	public String getString();

	public char charAt(int index);

	public int length();
}
