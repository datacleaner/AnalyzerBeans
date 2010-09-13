package org.eobjects.analyzer.util;

public final class StringUtils {

	public static final String DIACRITICS = "æøåâäáàôöóòêëéèûüúùîïíìñńǹḿ";

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isDiacritic(char c) {
		return DIACRITICS.indexOf(Character.toLowerCase(c)) != -1;
	}

}
