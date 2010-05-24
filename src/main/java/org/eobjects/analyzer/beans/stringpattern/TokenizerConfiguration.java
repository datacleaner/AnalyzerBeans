package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TokenizerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private EnumSet<TokenType> _tokenTypes = EnumSet.allOf(TokenType.class);
	private EnumMap<TokenType, Boolean> _discriminateTokenLength = new EnumMap<TokenType, Boolean>(
			TokenType.class);
	private boolean _discriminateTextCase = true;
	private boolean _discriminateWhiteSpaces = true;
	private boolean _discriminateDecimalNumbers = true;
	private boolean _discriminateNegativeNumbers = true;
	private Set<String> _decimalSeparatorTokens = new HashSet<String>();
	private List<PredefinedTokenDefinition> _predefinedTokens = new LinkedList<PredefinedTokenDefinition>();

	public void setTokenTypes(EnumSet<TokenType> tokenTypes) {
		_tokenTypes = tokenTypes;
	}

	public EnumSet<TokenType> getTokenTypes() {
		return _tokenTypes;
	}

	public EnumMap<TokenType, Boolean> getDiscriminateTokenLength() {
		return _discriminateTokenLength;
	}

	public boolean isDistriminateTokenLength(TokenType tokenType) {
		Boolean discriminateTokenLength = _discriminateTokenLength
				.get(tokenType);
		if (discriminateTokenLength == null) {
			return false;
		}
		return discriminateTokenLength.booleanValue();
	}

	public void setDiscriminateTokenLength(
			EnumMap<TokenType, Boolean> discriminateTokenLength) {
		_discriminateTokenLength = discriminateTokenLength;
	}

	public void setDistriminateTokenLength(TokenType tokenType,
			boolean discriminateTokenLength) {
		_discriminateTokenLength.put(tokenType, Boolean
				.valueOf(discriminateTokenLength));
	}

	public boolean isDiscriminateTextCase() {
		return _discriminateTextCase;
	}

	public void setDiscriminateTextCase(boolean discriminateTextCase) {
		_discriminateTextCase = discriminateTextCase;
	}

	public boolean isDiscriminateWhiteSpaces() {
		return _discriminateWhiteSpaces;
	}

	public void setDiscriminateWhiteSpaces(boolean discriminateWhiteSpaces) {
		_discriminateWhiteSpaces = discriminateWhiteSpaces;
	}

	public List<PredefinedTokenDefinition> getPredefinedTokens() {
		return _predefinedTokens;
	}

	public void setPredefinedTokens(List<PredefinedTokenDefinition> predefinedTokens) {
		_predefinedTokens = predefinedTokens;
	}

	public boolean isDiscriminateDecimalNumbers() {
		return _discriminateDecimalNumbers;
	}

	public void setDiscriminateDecimalNumbers(boolean discriminateDecimalNumbers) {
		_discriminateDecimalNumbers = discriminateDecimalNumbers;
	}

	public Set<String> getDecimalSeparatorTokens() {
		return _decimalSeparatorTokens;
	}

	public void setDecimalSeparatorTokens(Set<String> decimalSeparatorTokens) {
		_decimalSeparatorTokens = decimalSeparatorTokens;
	}

	public void addDecimalSeparatorToken(String decimalSeparatorToken) {
		_decimalSeparatorTokens.add(decimalSeparatorToken);
	}

	public void removeDecimalSeparatorToken(String decimalSeparatorToken) {
		_decimalSeparatorTokens.remove(decimalSeparatorToken);
	}

	public boolean isDiscriminateNegativeNumbers() {
		return _discriminateNegativeNumbers;
	}

	public void setDiscriminateNegativeNumbers(
			boolean discriminateNegativeNumbers) {
		_discriminateNegativeNumbers = discriminateNegativeNumbers;
	}
	
	public boolean isTokenTypeEnabled(TokenType tokenType) {
		return _tokenTypes.contains(tokenType);
	}
}
