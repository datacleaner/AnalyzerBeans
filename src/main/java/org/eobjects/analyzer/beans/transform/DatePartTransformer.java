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
package org.eobjects.analyzer.beans.transform;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Extract date part")
@Description("Extract the parts of a date (year, month, day etc.)")
public class DatePartTransformer implements Transformer<Number> {

	@Configured(order = 1)
	InputColumn<Date> column;

	@Configured(order = 2)
	boolean year = true;

	@Configured(order = 3)
	boolean month = true;

	@Configured(order = 4)
	boolean dayOfMonth = true;

	@Configured(order = 5)
	boolean hour = false;

	@Configured(order = 6)
	boolean minute = false;

	@Configured(order = 7)
	boolean second = false;

	@Override
	public OutputColumns getOutputColumns() {
		final List<String> columnNames = new ArrayList<String>();
		final String columnName = column.getName();

		if (year) {
			columnNames.add(columnName + " (year)");
		}
		if (month) {
			columnNames.add(columnName + " (month)");
		}
		if (dayOfMonth) {
			columnNames.add(columnName + " (day of month)");
		}
		if (hour) {
			columnNames.add(columnName + " (hour)");
		}
		if (minute) {
			columnNames.add(columnName + " (minute)");
		}
		if (second) {
			columnNames.add(columnName + " (second)");
		}

		if (columnNames.isEmpty()) {
			columnNames.add(columnName + " (year)");
		}

		return new OutputColumns(columnNames.toArray(new String[columnNames.size()]));
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		final Date value = inputRow.getValue(column);
		return transform(value);
	}

	public Number[] transform(Date date) {
		final Calendar cal;
		if (date == null) {
			cal = null;
		} else {
			cal = Calendar.getInstance();
			cal.setTime(date);
		}

		final List<Number> result = new ArrayList<Number>();

		if (year) {
			result.add(getYear(cal));
		}
		if (month) {
			result.add(getMonth(cal));
		}
		if (dayOfMonth) {
			result.add(getDayOfMonth(cal));
		}
		if (hour) {
			result.add(getHour(cal));
		}
		if (minute) {
			result.add(getMinute(cal));
		}
		if (second) {
			result.add(getSecond(cal));
		}

		if (result.isEmpty()) {
			result.add(getYear(cal));
		}
		return result.toArray(new Number[result.size()]);
	}

	private Number getSecond(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.SECOND);
	}

	private Number getMinute(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.MINUTE);
	}

	private Number getHour(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private Number getDayOfMonth(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	private Number getMonth(Calendar cal) {
		if (cal == null) {
			return null;
		}
		// add 1 to the month, to make it 1-based (January = 1)
		return cal.get(Calendar.MONTH) + 1;
	}

	private Number getYear(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.YEAR);
	}
}
