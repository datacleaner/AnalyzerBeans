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
package org.eobjects.analyzer.beans.convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Attempts to convert anything to a Date value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to date")
@Description("Converts anything to a date (or null).")
public class ConvertToDateTransformer implements Transformer<Date> {

	private static final List<DateTimeFormatter> dateTimeFormatters;

	static {
		dateTimeFormatters = new ArrayList<DateTimeFormatter>();
		String[] prototypePatterns = { "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy" };
		for (String string : prototypePatterns) {
			dateTimeFormatters.add(DateTimeFormat.forPattern(string));
			string = string.replaceAll("\\-", "\\.");
			dateTimeFormatters.add(DateTimeFormat.forPattern(string));
			string = string.replaceAll("\\.", "\\/");
			dateTimeFormatters.add(DateTimeFormat.forPattern(string));
		}
	}

	private static final DateTimeFormatter NUMBER_BASED_DATE_FORMAT_LONG = DateTimeFormat.forPattern("yyyyMMdd");
	private static final DateTimeFormatter NUMBER_BASED_DATE_FORMAT_SHORT = DateTimeFormat.forPattern("yyMMdd");

	@Inject
	@Configured
	InputColumn<?> input;

	@Configured(required = false)
	Date nullReplacement;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(input.getName() + " (as date)");
	}

	@Override
	public Date[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Date d = transformValue(value);
		if (d == null) {
			d = nullReplacement;
		}
		return new Date[] { d };
	}

	public static Date transformValue(Object value) {
		Date d = null;
		if (value != null) {
			if (value instanceof Date) {
				d = (Date) value;
			} else if (value instanceof java.sql.Date) {
				java.sql.Date sqlDate = (java.sql.Date) value;
				d = new Date(sqlDate.getTime());
			} else if (value instanceof String) {
				d = convertFromString((String) value);
			} else if (value instanceof Number) {
				d = convertFromNumber((Number) value);
			}
		}
		return d;
	}

	protected static Date convertFromString(String value) {
		try {
			long longValue = Long.parseLong(value);
			return convertFromNumber(longValue);
		} catch (NumberFormatException e) {
			// do nothing, proceed to dateFormat parsing
		}

		for (DateTimeFormatter formatter : dateTimeFormatters) {
			try {
				return formatter.parseDateTime(value).toDate();
			} catch (Exception e) {
				// proceed to next formatter
			}
		}

		return null;
	}

	protected static Date convertFromNumber(Number value) {
		Number numberValue = (Number) value;
		long longValue = numberValue.longValue();

		String stringValue = Long.toString(longValue);
		// test if the number is actually a format of the type yyyyMMdd
		if (stringValue.length() == 8 && (stringValue.startsWith("1") || stringValue.startsWith("2"))) {
			try {
				return NUMBER_BASED_DATE_FORMAT_LONG.parseDateTime(stringValue).toDate();
			} catch (Exception e) {
				// do nothing, proceed to next method of conversion
			}
		}

		// test if the number is actually a format of the type yyMMdd
		if (stringValue.length() == 6) {
			try {
				return NUMBER_BASED_DATE_FORMAT_SHORT.parseDateTime(stringValue).toDate();
			} catch (Exception e) {
				// do nothing, proceed to next method of conversion
			}
		}

		if (longValue > 5000000) {
			// this number is most probably amount of milliseconds since
			// 1970
			return new Date(longValue);
		} else {
			// this number is most probably the amount of days since
			// 1970
			return new Date(longValue * 1000 * 60 * 60 * 24);
		}
	}

}
