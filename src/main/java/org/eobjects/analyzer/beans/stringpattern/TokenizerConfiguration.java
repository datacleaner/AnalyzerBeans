package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class TokenizerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Which token types are enabled
	 */
	private EnumSet<TokenType> _tokenTypes;

	/**
	 * Should tokens be discriminated (when matching) based on length. For
	 * example, if "hello" and "hi" should be matched, then length
	 * discrimination should be false. If only "hello" and "world", but not "hi"
	 * should be matched then length discrimination should be true.
	 */
	private EnumMap<TokenType, Boolean> _discriminateTokenLength;

	/**
	 * Discriminate the case of characters in TEXT tokens
	 */
	private boolean _discriminateTextCase = true;

	/**
	 * Discriminate the type of whitespaces (space, tab etc.)
	 */
	private boolean _discriminateWhiteSpaces;

	/**
	 * Discriminate decimal numbers from integers
	 */
	private boolean _discriminateDecimalNumbers;

	/**
	 * Discriminate negative numbers from positive numbers
	 */
	private boolean _discriminateNegativeNumbers = true;

	/**
	 * Characters to use for thousand separator in numbers (typically ',')
	 */
	private Character _thousandSeparator;

	/**
	 * Characters to use for decimal separation in numbers (typically '.')
	 */
	private Character _decimalSeparator;

	/**
	 * Character to use for minus sign in numbers (typically '-')
	 */
	private Character _minusSign;

	private List<PredefinedTokenDefinition> _predefinedTokens = new LinkedList<PredefinedTokenDefinition>();

	public TokenizerConfiguration(boolean enableMixed, char decimalSeparator,
			char thousandSeparator, char minusSign) {
		_tokenTypes = EnumSet.allOf(TokenType.class);
		if (!enableMixed) {
			_tokenTypes.remove(TokenType.MIXED);
		}

		// set default values;
		_discriminateTokenLength = new EnumMap<TokenType, Boolean>(
				TokenType.class);
		for (TokenType tokenType : TokenType.values()) {
			_discriminateTokenLength.put(tokenType, false);
		}

		_discriminateTokenLength.put(TokenType.WHITESPACE, true);

		_discriminateTextCase = true;
		_discriminateWhiteSpaces = true;

		_discriminateDecimalNumbers = true;
		_discriminateNegativeNumbers = false;

		_decimalSeparator = decimalSeparator;
		_thousandSeparator = thousandSeparator;
		_minusSign = minusSign;
	}

	public TokenizerConfiguration() {
		this(true, DecimalFormatSymbols.getInstance().getDecimalSeparator(),
				DecimalFormatSymbols.getInstance().getGroupingSeparator(),
				DecimalFormatSymbols.getInstance().getMinusSign());
	}

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
		_discriminateTokenLength.put(tokenType,
				Boolean.valueOf(discriminateTokenLength));
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

	public void setPredefinedTokens(
			List<PredefinedTokenDefinition> predefinedTokens) {
		_predefinedTokens = predefinedTokens;
	}

	public boolean isDiscriminateDecimalNumbers() {
		return _discriminateDecimalNumbers;
	}

	public void setDiscriminateDecimalNumbers(boolean discriminateDecimalNumbers) {
		_discriminateDecimalNumbers = discriminateDecimalNumbers;
	}

	public Character getThousandSeparator() {
		return _thousandSeparator;
	}

	public void setThousandSeparator(Character thousandSeparator) {
		_thousandSeparator = thousandSeparator;
	}

	public Character getDecimalSeparator() {
		return _decimalSeparator;
	}

	public void setDecimalSeparator(Character decimalSeparator) {
		_decimalSeparator = decimalSeparator;
	}

	public Character getMinusSign() {
		return _minusSign;
	}

	public void setMinusSign(Character minusSign) {
		_minusSign = minusSign;
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
