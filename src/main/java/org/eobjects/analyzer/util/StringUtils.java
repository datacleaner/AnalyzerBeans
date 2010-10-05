package org.eobjects.analyzer.util;

public final class StringUtils {

	public static final String LATIN_CHARACTERS = "";

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isDiacritic(char c) {
		if (Character.isLetter(c)) {
			return !isLatin(c);
		}
		return false;
	}

	public static boolean isLatin(char c) {
		return c >= 'A' && c <= 'z';
	}

	public static String leftTrim(String str) {
		int i = 0;
		while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
			i++;
		}
		return str.substring(i);
	}

	public static String rightTrim(String str) {
		int i = str.length() - 1;
		while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
			i--;
		}
		return str.substring(0, i + 1);
	}
}
