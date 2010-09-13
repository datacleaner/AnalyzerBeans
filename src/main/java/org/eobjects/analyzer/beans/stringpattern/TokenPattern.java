package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a pattern of token symbols that are matchable against a series of
 * concrete tokens.
 * 
 * @author Kasper Sørensen
 */
public interface TokenPattern extends Serializable {

	/**
	 * Attempts to match a list of tokens against this pattern. If it succeeds,
	 * true will be returned.
	 * 
	 * @param tokens
	 * @return true if the match was succesful, false otherwise.
	 */
	public boolean match(List<Token> tokens);

	public List<TokenPatternSymbol> getSymbols();

	public String toSymbolicString();
}
