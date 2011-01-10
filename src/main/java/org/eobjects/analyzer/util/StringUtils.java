/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.util;

import java.util.regex.Pattern;

/**
 * Contains various utility methods regarding string handling.
 * 
 * @author Kasper Sørensen
 */
public final class StringUtils {

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\p{Zs}\\p{javaWhitespace}]+");

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

	public static String replaceWhitespaces(String inString, String with) {
		return WHITESPACE_PATTERN.matcher(inString).replaceAll(with);
	}
}
