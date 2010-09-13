package org.eobjects.analyzer.beans.stringpattern;

public interface TokenPatternSymbol {

	public String toSymbolicString();
	
	public TokenType getTokenType();
	
	public boolean isUpperCaseOnly();
	
	public boolean isLowerCaseOnly();
	
	public boolean isDecimal();
	
	public boolean isNegative();
	
	public boolean matches(Token token, TokenizerConfiguration configuration);

	public int length();

	public void expandLenght(int amount);

	public boolean isExpandable();
}
