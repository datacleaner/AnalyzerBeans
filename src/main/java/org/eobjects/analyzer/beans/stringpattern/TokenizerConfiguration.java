package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class TokenizerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private EnumSet<TokenType> _tokenTypes;
	private EnumMap<TokenType, Boolean> _discriminateTokenLength;

	private boolean _discriminateTextCase;
	private boolean _discriminateWhiteSpaces;
	private boolean _discriminateDecimalNumbers;
	private boolean _discriminateNegativeNumbers;
	private Character _thousandsSeparator;
	private Character _decimalSeparator;
	private Character _minusSign;

	private List<PredefinedTokenDefinition> _predefinedTokens = new LinkedList<PredefinedTokenDefinition>();

	public TokenizerConfiguration() {
		this(true);
	}

	public TokenizerConfiguration(boolean enableMixedTokens) {
		this(enableMixedTokens, DecimalFormatSymbols.getInstance().getDecimalSeparator(), DecimalFormatSymbols.getInstance()
				.getGroupingSeparator(), DecimalFormatSymbols.getInstance().getMinusSign());
	}

	public TokenizerConfiguration(boolean enableMixed, Character decimalSeparator, Character thousandsSeparator,
			Character minusSign) {
		_tokenTypes = EnumSet.allOf(TokenType.class);
		if (!enableMixed) {
			_tokenTypes.remove(TokenType.MIXED);
		}

		// set default values;
		_discriminateTokenLength = new EnumMap<TokenType, Boolean>(TokenType.class);
		_discriminateTokenLength.put(TokenType.TEXT, false);
		_discriminateTokenLength.put(TokenType.NUMBER, false);
		_discriminateTokenLength.put(TokenType.MIXED, false);
		_discriminateTokenLength.put(TokenType.PREDEFINED, false);
		_discriminateTokenLength.put(TokenType.WHITESPACE, true);
		_discriminateTokenLength.put(TokenType.DELIM, true);

		_discriminateTextCase = true;
		_discriminateWhiteSpaces = true;
		_discriminateDecimalNumbers = true;
		_discriminateNegativeNumbers = false;

		_decimalSeparator = decimalSeparator;
		_thousandsSeparator = thousandsSeparator;
		_minusSign = minusSign;
	}

	/**
	 * Sets which token types are enabled
	 */
	public void setTokenTypes(EnumSet<TokenType> tokenTypes) {
		_tokenTypes = tokenTypes;
	}

	/**
	 * Which token types are enabled
	 */
	public EnumSet<TokenType> getTokenTypes() {
		return _tokenTypes;
	}

	/**
	 * Should tokens be discriminated (when matching) based on length. For
	 * example, if "hello" and "hi" should be matched, then length
	 * discrimination should be false. If only "hello" and "world", but not "hi"
	 * should be matched then length discrimination should be true.
	 */
	public EnumMap<TokenType, Boolean> getDiscriminateTokenLength() {
		return _discriminateTokenLength;
	}

	/**
	 * Should tokens be discriminated (when matching) based on length. For
	 * example, if "hello" and "hi" should be matched, then length
	 * discrimination should be false. If only "hello" and "world", but not "hi"
	 * should be matched then length discrimination should be true.
	 */
	public boolean isDistriminateTokenLength(TokenType tokenType) {
		Boolean discriminateTokenLength = _discriminateTokenLength.get(tokenType);
		if (discriminateTokenLength == null) {
			return false;
		}
		return discriminateTokenLength.booleanValue();
	}

	/**
	 * Sets which tokens should be discriminated (when matching) based on
	 * length. For example, if "hello" and "hi" should be matched, then length
	 * discrimination should be false. If only "hello" and "world", but not "hi"
	 * should be matched then length discrimination should be true.
	 */
	public void setDistriminateTokenLength(EnumMap<TokenType, Boolean> discriminateTokenLength) {
		_discriminateTokenLength = discriminateTokenLength;
	}

	/**
	 * Sets which tokens should be discriminated (when matching) based on
	 * length. For example, if "hello" and "hi" should be matched, then length
	 * discrimination should be false. If only "hello" and "world", but not "hi"
	 * should be matched then length discrimination should be true.
	 */
	public void setDistriminateTokenLength(TokenType tokenType, boolean discriminateTokenLength) {
		_discriminateTokenLength.put(tokenType, Boolean.valueOf(discriminateTokenLength));
	}

	/**
	 * Discriminate the case of characters in TEXT tokens
	 */
	public boolean isDiscriminateTextCase() {
		return _discriminateTextCase;
	}

	/**
	 * Sets whether to discriminate the case of characters in TEXT tokens
	 */
	public void setDiscriminateTextCase(boolean discriminateTextCase) {
		_discriminateTextCase = discriminateTextCase;
	}

	/**
	 * Discriminate the type of whitespaces (space, tab etc.)
	 */
	public boolean isDiscriminateWhiteSpaces() {
		return _discriminateWhiteSpaces;
	}

	/**
	 * Sets whether to discriminate the type of whitespaces (space, tab etc.)
	 */
	public void setDiscriminateWhiteSpaces(boolean discriminateWhiteSpaces) {
		_discriminateWhiteSpaces = discriminateWhiteSpaces;
	}

	public List<PredefinedTokenDefinition> getPredefinedTokens() {
		return _predefinedTokens;
	}

	public void setPredefinedTokens(List<PredefinedTokenDefinition> predefinedTokens) {
		_predefinedTokens = predefinedTokens;
	}

	/**
	 * Discriminate decimal numbers from integers when matching
	 */
	public boolean isDiscriminateDecimalNumbers() {
		return _discriminateDecimalNumbers;
	}

	/**
	 * Sets whether to discriminate decimal numbers from integers when matching
	 */
	public void setDiscriminateDecimalNumbers(boolean discriminateDecimalNumbers) {
		_discriminateDecimalNumbers = discriminateDecimalNumbers;
	}

	/**
	 * Characters to use for thousands separator in numbers (typically ',')
	 */
	public Character getThousandsSeparator() {
		return _thousandsSeparator;
	}

	/**
	 * Sets the characters to use for thousands separator in numbers (typically
	 * ',')
	 */
	public void setThousandsSeparator(Character thousandSeparator) {
		_thousandsSeparator = thousandSeparator;
	}

	/**
	 * Characters to use for decimal separation in numbers (typically '.')
	 */
	public Character getDecimalSeparator() {
		return _decimalSeparator;
	}

	/**
	 * Sets the characters to use for decimal separation in numbers (typically
	 * '.')
	 */
	public void setDecimalSeparator(Character decimalSeparator) {
		_decimalSeparator = decimalSeparator;
	}

	/**
	 * Character to use for minus sign in numbers (typically '-')
	 */
	public Character getMinusSign() {
		return _minusSign;
	}

	/**
	 * Sets the character to use for minus sign in numbers (typically '-')
	 */
	public void setMinusSign(Character minusSign) {
		_minusSign = minusSign;
	}

	/**
	 * Discriminate negative numbers from positive numbers
	 */
	public boolean isDiscriminateNegativeNumbers() {
		return _discriminateNegativeNumbers;
	}

	/**
	 * Sets whether to discriminate negative numbers from positive numbers
	 */
	public void setDiscriminateNegativeNumbers(boolean discriminateNegativeNumbers) {
		_discriminateNegativeNumbers = discriminateNegativeNumbers;
	}

	public boolean isTokenTypeEnabled(TokenType tokenType) {
		return _tokenTypes.contains(tokenType);
	}
}
