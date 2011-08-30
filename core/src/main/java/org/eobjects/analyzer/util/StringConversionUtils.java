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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
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

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <li>org.eobjects.analyzer.reference.StringPattern</li>
 * <li>org.eobjects.analyzer.connection.Datastore</li>
 * <li>org.eobjects.metamodel.schema.Column</li>
 * <li>org.eobjects.metamodel.schema.Table</li>
 * <li>org.eobjects.metamodel.schema.Schema</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 * @author Nancy Sharma
 */
public final class StringConversionUtils {

	private static final String[][] ESCAPE_MAPPING = { { "&amp;", "&" }, { "&#91;", "[" }, { "&#93;", "]" },
			{ "&#44;", "," }, { "&lt;", "<" }, { "&gt;", ">" }, { "&quot;", "\"" }, { "&copy;", "\u00a9" },
			{ "&reg;", "\u00ae" }, { "&euro;", "\u20a0" } };

	private static final Logger logger = LoggerFactory.getLogger(StringConversionUtils.class);

	// ISO 8601
	private static final String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss S";

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

		if (o instanceof Calendar) {
			// will now be picked up by the date conversion
			o = ((Calendar) o).getTime();
		}

		final String result;
		if (o instanceof Boolean || o instanceof Number || o instanceof String || o instanceof Character) {
			result = o.toString();
		} else if (o instanceof Schema) {
			result = ((Schema) o).getName();
		} else if (o instanceof Table) {
			result = ((Table) o).getQualifiedLabel();
		} else if (o instanceof Column) {
			result = ((Column) o).getQualifiedLabel();
		} else if (o instanceof Dictionary) {
			result = ((Dictionary) o).getName();
		} else if (o instanceof SynonymCatalog) {
			result = ((SynonymCatalog) o).getName();
		} else if (o instanceof StringPattern) {
			result = ((StringPattern) o).getName();
		} else if (o instanceof Datastore) {
			result = ((Datastore) o).getName();
		} else if (o instanceof Enum<?>) {
			Enum<?> e = (Enum<?>) o;
			result = e.name();
		} else if (o instanceof File) {
			File file = (File) o;
			if (file.isAbsolute()) {
				result = file.getAbsolutePath();
			} else {
				result = file.getPath();
			}
		} else if (o instanceof Date) {
			result = new SimpleDateFormat(dateFormatString).format((Date) o);
		} else if (o instanceof Pattern) {
			result = o.toString();
		} else if (o instanceof Serializable) {
			logger.info("No built-in handling of type: {}, using serialization.", o.getClass().getName());
			byte[] bytes = SerializationUtils.serialize((Serializable) o);
			result = serialize(bytes);
		} else {
			logger.warn("Could not convert type: {}", o.getClass().getName());
			result = o.toString();
		}

		return escape(result);
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
			ReferenceDataCatalog referenceDataCatalog, DatastoreCatalog datastoreCatalog) {
		logger.debug("deserialize(\"{}\", {})", str, type);
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}

		if ("<null>".equals(str)) {
			return null;
		}

		if (type.isArray()) {
			return (E) deserializeArray(str, type, schemaNavigator, referenceDataCatalog, datastoreCatalog);
		}

		str = unescape(str);
		if (ReflectionUtils.isString(type)) {
			return (E) str;
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
				throw new IllegalArgumentException("Column not found: " + str);
			}
			return (E) column;
		}
		if (ReflectionUtils.isTable(type)) {
			Table table = schemaNavigator.convertToTable(str);
			if (table == null) {
				throw new IllegalArgumentException("Table not found: " + str);
			}
			return (E) table;
		}
		if (ReflectionUtils.isSchema(type)) {
			Schema schema = schemaNavigator.convertToSchema(str);
			if (schema == null) {
				throw new IllegalArgumentException("Schema not found: " + str);
			}
			return (E) schema;
		}
		if (ReflectionUtils.is(type, Dictionary.class)) {
			Dictionary dictionary = referenceDataCatalog.getDictionary(str);
			if (dictionary == null) {
				throw new IllegalArgumentException("Dictionary not found: " + str);
			}
			return (E) dictionary;
		}
		if (ReflectionUtils.is(type, SynonymCatalog.class)) {
			SynonymCatalog synonymCatalog = referenceDataCatalog.getSynonymCatalog(str);
			if (synonymCatalog == null) {
				throw new IllegalArgumentException("Synonym catalog not found: " + str);
			}
			return (E) synonymCatalog;
		}
		if (ReflectionUtils.is(type, StringPattern.class)) {
			StringPattern stringPattern = referenceDataCatalog.getStringPattern(str);
			if (stringPattern == null) {
				throw new IllegalArgumentException("String pattern not found: " + str);
			}
			return (E) stringPattern;
		}
		if (ReflectionUtils.is(type, Datastore.class)) {
			if (null != datastoreCatalog) {
				Datastore datastore = datastoreCatalog.getDatastore(str);
				if (datastore == null) {
					throw new IllegalArgumentException("Datastore not found: " + str);
				}
				return (E) datastore;
			}
		}
		if (ReflectionUtils.isNumber(type)) {
			return (E) ConvertToNumberTransformer.transformValue(str);
		}
		if (ReflectionUtils.is(type, Serializable.class)) {
			logger.warn("No built-in handling of type: {}, using deserialization", type.getName());
			byte[] bytes = deserialize(str, byte[].class, schemaNavigator, referenceDataCatalog, datastoreCatalog);
			try {
				ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(new ByteArrayInputStream(
						bytes));
				objectInputStream.addClassLoader(type.getClassLoader());
				Object obj = objectInputStream.readObject();
				return (E) obj;
			} catch (Exception e) {
				throw new IllegalStateException("Could not deserialize to " + type + ".", e);
			}
		}

		throw new IllegalArgumentException("Could not convert to type: " + type.getName());
	}

	private static final Date toDate(String str) {
		try {
			return new SimpleDateFormat(dateFormatString).parse(str);
		} catch (ParseException e) {

			Date date = ConvertToDateTransformer.getInternalInstance().transformValue(str);
			if (date == null) {
				logger.error("Could not parse date: " + str, e);
				throw new IllegalArgumentException(e);
			} else {
				return date;
			}
		}
	}

	private static final Object deserializeArray(final String str, Class<?> type, SchemaNavigator schemaNavigator,
			ReferenceDataCatalog referenceDataCatalog, DatastoreCatalog datastoreCatalog) {
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
				Array.set(result, 0,
						deserialize(str, componentType, schemaNavigator, referenceDataCatalog, datastoreCatalog));
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
				objects.add(deserialize(s, componentType, schemaNavigator, referenceDataCatalog, datastoreCatalog));
				offset = innerString.length();
			} else if (bracketBeginIndex == -1 || commaIndex < bracketBeginIndex) {
				String s = innerString.substring(offset, commaIndex);
				if ("".equals(s)) {
					offset++;
				} else {
					logger.debug("no brackets in next element: \"{}\"", s);
					objects.add(deserialize(s, componentType, schemaNavigator, referenceDataCatalog, datastoreCatalog));
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
				objects.add(deserializeArray(s, componentType, schemaNavigator, referenceDataCatalog, datastoreCatalog));

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