package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;

/**
 * Represents a symbol/element in a token pattern. If for example the pattern is
 * "aaa@aaa.aa", then there will be 5 symbols:
 * <ul>
 * <li>aaa</li>
 * <li>@</li>
 * <li>aaa</li>
 * <li>.</li>
 * <li>aa</li>
 * </ul>
 * 
 * The token pattern symbol is different from a pattern in the way that it is
 * more abstract. A symbol will not retain the concrete values of most tokens.
 * Thus the information stored in a symbol will often be limited to:
 * 
 * <ul>
 * <li>The TokenType</li>
 * <li>The length of the symbol</li>
 * <li>Metadata about the symbol such as: Is it a negativ number, is it
 * uppercase, does it contain decimals etc.</li>
 * </ul>
 * 
 * @see Token
 * 
 * @author Kasper Sørensen
 */
public interface TokenPatternSymbol extends Serializable {

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
