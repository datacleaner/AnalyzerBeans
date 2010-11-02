package org.eobjects.analyzer.util;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

/**
 * Helper class for converting objects to and from string representations as
 * used for example in serialized XML jobs.
 * 
 * The string converter currently supports instances and arrays of:
 * <ul>
 * <li>Boolean</li>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Character</li>
 * <li>String</li>
 * <li>java.io.File</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.util.Calendar</li>
 * <li>java.util.regex.Pattern</li>
 * <li>org.eobjects.analyzer.reference.Dictionary</li>
 * <li>org.eobjects.analyzer.reference.SynonymCatalog</li>
 * <li>dk.eobjects.metamodel.schema.Column</li>
 * <li>dk.eobjects.metamodel.schema.Table</li>
 * <li>dk.eobjects.metamodel.schema.Schema</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 */
public final class StringConversionUtils {

	private static final Logger logger = LoggerFactory.getLogger(StringConversionUtils.class);

	private static final String[][] ESCAPE_MAPPING = { { "&amp;", "&" }, { "&#91;", "[" }, { "&#93;", "]" },
			{ "&#44;", "," }, { "&lt;", "<" }, { "&gt;", ">" }, { "&quot;", "\"" }, { "&copy;", "\u00a9" },
			{ "&reg;", "\u00ae" }, { "&euro;", "\u20a0" } };

	// ISO 8601
	private static final String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss S";

	public static final String serialize(Object o) {
		if (o == null) {
			logger.debug("o is null!");
			return "<null>";
		}

		if (ReflectionUtils.isArray(o)) {
			StringBuilder sb = new StringBuilder();
			int length = Array.getLength(o);
			sb.append('[');
			for (int i = 0; i < length; i++) {
				Object obj = Array.get(o, i);
				if (i != 0) {
					sb.append(',');
				}
				sb.append(serialize(obj));
			}
			sb.append(']');
			return sb.toString();
		}

		if (o instanceof Schema) {
			return escape(((Schema) o).getName());
		}
		if (o instanceof Table) {
			return escape(((Table) o).getQualifiedLabel());
		}
		if (o instanceof Column) {
			return escape(((Column) o).getQualifiedLabel());
		}
		if (o instanceof Dictionary) {
			return escape(((Dictionary) o).getName());
		}
		if (o instanceof SynonymCatalog) {
			return escape(((SynonymCatalog) o).getName());
		}
		if (o instanceof Boolean || o instanceof Number || o instanceof String || o instanceof Character) {
			return escape(o.toString());
		}
		if (o instanceof Enum<?>) {
			Enum<?> e = (Enum<?>) o;
			return e.name();
		}
		if (o instanceof java.sql.Date) {
			// will now be picked up by the date conversion
			o = new Date(((java.sql.Date) o).getTime());
		}
		if (o instanceof File) {
			File file = (File) o;
			if (file.isAbsolute()) {
				return file.getAbsolutePath();
			} else {
				return file.getPath();
			}
		}
		if (o instanceof Calendar) {
			// will now be picked up by the date conversion
			o = ((Calendar) o).getTime();
		}
		if (o instanceof Date) {
			return new SimpleDateFormat(dateFormatString).format((Date) o);
		}
		if (o instanceof Pattern) {
			return escape(o.toString());
		}

		logger.warn("Could not convert type: {}", o.getClass().getName());
		return escape(o.toString());
	}

	private static final String escape(String str) {
		for (String[] mapping : ESCAPE_MAPPING) {
			String escapedValue = mapping[1];
			if (str.contains(escapedValue)) {
				str = str.replace(escapedValue, mapping[0]);
			}
		}
		return str;
	}

	private static final String unescape(String str) {
		for (String[] mapping : ESCAPE_MAPPING) {
			String unescapedValue = mapping[0];
			if (str.contains(unescapedValue)) {
				str = str.replaceAll(unescapedValue, mapping[1]);
			}
		}
		return str;
	}

	/**
	 * Deserializes a string to a java object.
	 * 
	 * @param <E>
	 * @param str
	 * @param type
	 * @param schemaNavigator
	 *            schema navigator to use when type is Column, Table or Schema.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <E> E deserialize(String str, Class<E> type, SchemaNavigator schemaNavigator,
			ReferenceDataCatalog referenceDataCatalog) {
		logger.debug("deserialize(\"{}\", {})", str, type);
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}

		if ("<null>".equals(str)) {
			return null;
		}

		if (type.isArray()) {
			return (E) deserializeArray(str, type, schemaNavigator, referenceDataCatalog);
		}
		if (ReflectionUtils.isString(type)) {
			return (E) unescape(str);
		}
		if (ReflectionUtils.isBoolean(type)) {
			return (E) Boolean.valueOf(str);
		}
		if (ReflectionUtils.isCharacter(type)) {
			return (E) Character.valueOf(str.charAt(0));
		}
		if (ReflectionUtils.isInteger(type)) {
			return (E) Integer.valueOf(str);
		}
		if (ReflectionUtils.isLong(type)) {
			return (E) Long.valueOf(str);
		}
		if (ReflectionUtils.isByte(type)) {
			return (E) Byte.valueOf(str);
		}
		if (ReflectionUtils.isShort(type)) {
			return (E) Short.valueOf(str);
		}
		if (ReflectionUtils.isDouble(type)) {
			return (E) Double.valueOf(str);
		}
		if (ReflectionUtils.isFloat(type)) {
			return (E) Float.valueOf(str);
		}
		if (type.isEnum()) {
			try {
				E[] enumConstants = type.getEnumConstants();
				Method nameMethod = Enum.class.getMethod("name");
				for (E e : enumConstants) {
					String name = (String) nameMethod.invoke(e);
					if (name.equals(str)) {
						return e;
					}
				}
			} catch (Exception e) {
				throw new IllegalStateException("Unexpected error occurred while examining enum", e);
			}
			throw new IllegalArgumentException("No such enum '" + str + "' in enum class: " + type.getName());
		}
		if (ReflectionUtils.isDate(type)) {
			return (E) toDate(str);
		}
		if (ReflectionUtils.is(type, File.class)) {
			return (E) new File(str);
		}
		if (ReflectionUtils.is(type, Calendar.class)) {
			Date date = toDate(str);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			return (E) c;
		}
		if (ReflectionUtils.is(type, Pattern.class)) {
			try {
				return (E) Pattern.compile(str);
			} catch (PatternSyntaxException e) {
				throw new IllegalArgumentException("Invalid regular expression syntax in '" + str + "'.", e);
			}
		}
		if (ReflectionUtils.is(type, java.sql.Date.class)) {
			Date date = toDate(str);
			return (E) new java.sql.Date(date.getTime());
		}
		if (ReflectionUtils.isColumn(type)) {
			Column column = schemaNavigator.convertToColumn(str);
			if (column == null) {
				logger.warn("Column not found: {}", str);
			}
			return (E) column;
		}
		if (ReflectionUtils.isTable(type)) {
			Table table = schemaNavigator.convertToTable(str);
			if (table == null) {
				logger.warn("Table not found: {}", str);
			}
			return (E) table;
		}
		if (ReflectionUtils.isSchema(type)) {
			Schema schema = schemaNavigator.convertToSchema(str);
			if (schema == null) {
				logger.warn("Schema not found: {}", str);
			}
			return (E) schema;
		}
		if (ReflectionUtils.is(type, Dictionary.class)) {
			Dictionary dictionary = referenceDataCatalog.getDictionary(str);
			if (dictionary == null) {
				logger.warn("Dictionary not found: {}", str);
			}
			return (E) dictionary;
		}
		if (ReflectionUtils.is(type, SynonymCatalog.class)) {
			SynonymCatalog synonymCatalog = referenceDataCatalog.getSynonymCatalog(str);
			if (synonymCatalog == null) {
				logger.warn("SynonymCatalog not found: {}", str);
			}
			return (E) synonymCatalog;
		}

		throw new IllegalArgumentException("Could not convert to type: " + type.getName());
	}

	private static final Date toDate(String str) {
		try {
			return (Date) new SimpleDateFormat(dateFormatString).parse(str);
		} catch (ParseException e) {
			logger.error("Could not parse date: " + str, e);
			throw new IllegalArgumentException(e);
		}
	}

	private static final Object deserializeArray(final String str, Class<?> type, SchemaNavigator schemaNavigator,
			ReferenceDataCatalog referenceDataCatalog) {
		assert type.isArray();

		Class<?> componentType = type.getComponentType();

		if ("[]".equals(str)) {
			logger.debug("found [], returning empty array");
			return Array.newInstance(componentType, 0);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("deserializeArray(\"{}\")", str);
			logger.debug("component type is: {}", componentType);

			int beginningBrackets = 0;
			int endingBrackets = 0;

			CharIterator it = new CharIterator(str);
			while (it.hasNext()) {
				it.next();
				if (it.is('[')) {
					beginningBrackets++;
				} else if (it.is(']')) {
					endingBrackets++;
				}
			}
			it.reset();
			logger.debug("brackets statistics: beginning={}, ending={}", beginningBrackets, endingBrackets);
			if (beginningBrackets != endingBrackets) {
				logger.warn("Unbalanced beginning and ending brackets!");
			}
		}

		if (!str.startsWith("[") || !str.endsWith("]")) {
			if (str.indexOf(',') == -1) {
				Object result = Array.newInstance(componentType, 1);
				Array.set(result, 0, deserialize(str, componentType, schemaNavigator, referenceDataCatalog));
				return result;
			}
			throw new IllegalArgumentException(
					"Cannot parse string as array, bracket encapsulation and comma delimitors expected. Found: " + str);
		}

		final String innerString = str.substring(1, str.length() - 1);
		logger.debug("innerString: {}", innerString);

		List<Object> objects = new ArrayList<Object>();
		int offset = 0;
		while (offset < innerString.length()) {
			logger.debug("offset: {}", offset);
			final int commaIndex = innerString.indexOf(',', offset);
			logger.debug("commaIndex: {}", commaIndex);
			final int bracketBeginIndex = innerString.indexOf('[', offset);
			logger.debug("bracketBeginIndex: {}", bracketBeginIndex);

			if (commaIndex == -1) {
				logger.debug("no comma found");
				String s = innerString.substring(offset);
				objects.add(deserialize(s, componentType, schemaNavigator, referenceDataCatalog));
				offset = innerString.length();
			} else if (bracketBeginIndex == -1 || commaIndex < bracketBeginIndex) {
				String s = innerString.substring(offset, commaIndex);
				if ("".equals(s)) {
					offset++;
				} else {
					logger.debug("no brackets in next element: \"{}\"", s);
					objects.add(deserialize(s, componentType, schemaNavigator, referenceDataCatalog));
					offset = commaIndex + 1;
				}
			} else {

				String s = innerString.substring(bracketBeginIndex);
				int nextBracket = 0;
				int depth = 1;
				logger.debug("substring with nested array: {}", s);

				while (depth > 0) {
					final int searchOffset = nextBracket + 1;
					int nextEndBracket = s.indexOf(']', searchOffset);
					if (nextEndBracket == -1) {
						throw new IllegalStateException("No ending bracket in array string: " + s.substring(searchOffset));
					}
					int nextBeginBracket = s.indexOf('[', searchOffset);
					if (nextBeginBracket == -1) {
						nextBeginBracket = s.length();
					}

					nextBracket = Math.min(nextEndBracket, nextBeginBracket);
					char c = s.charAt(nextBracket);
					logger.debug("nextBracket: {} ({})", nextBracket, c);

					if (c == '[') {
						depth++;
					} else if (c == ']') {
						depth--;
					} else {
						throw new IllegalStateException("Unexpected char: " + c);
					}
					logger.debug("depth: {}", depth);
					if (depth == 0) {
						s = s.substring(0, nextBracket + 1);
						logger.debug("identified array: {}", s);
					}
				}

				logger.debug("recursing to nested array: {}", s);

				logger.debug("inner array string: " + s);
				objects.add(deserializeArray(s, componentType, schemaNavigator, referenceDataCatalog));

				offset = bracketBeginIndex + s.length();
			}
		}

		Object result = Array.newInstance(componentType, objects.size());
		for (int i = 0; i < objects.size(); i++) {
			Array.set(result, i, objects.get(i));
		}
		return result;
	}
}