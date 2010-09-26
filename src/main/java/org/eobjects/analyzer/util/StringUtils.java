package org.eobjects.analyzer.util;

public final class StringUtils {

	public static final String DIACRITICS = "æøåâäáàôöóòêëéèûüúùîïíìñńǹḿ";

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isDiacritic(char c) {
		return DIACRITICS.indexOf(Character.toLowerCase(c)) != -1;
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
