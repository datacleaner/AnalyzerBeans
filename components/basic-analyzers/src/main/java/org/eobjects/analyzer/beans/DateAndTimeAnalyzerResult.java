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
package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.Metric;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Represents the result of a Date and Time Analyzer.
 * 
 * @author Kasper Sørensen
 */
public class DateAndTimeAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    public DateAndTimeAnalyzerResult(Crosstab<?> crosstab) {
        super(crosstab);
    }

    @Metric(order = 1, value = DateAndTimeAnalyzer.MEASURE_ROW_COUNT)
    public int getRowCount(InputColumn<?> col) {
        Number n = (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_ROW_COUNT).get();
        return n.intValue();
    }

    @Metric(order = 2, value = DateAndTimeAnalyzer.MEASURE_NULL_COUNT)
    public int getNullCount(InputColumn<?> col) {
        Number n = (Number) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_NULL_COUNT).get();
        return n.intValue();
    }

    @Metric(order = 3, value = DateAndTimeAnalyzer.MEASURE_HIGHEST_DATE)
    @Description("The highest date value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getHighestDate(InputColumn<?> col) {
        String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_HIGHEST_DATE).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    @Metric(order = 3, value = DateAndTimeAnalyzer.MEASURE_LOWEST_DATE)
    @Description("The lowest date value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getLowestDate(InputColumn<?> col) {
        String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_LOWEST_DATE).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }
    
    @Metric(order = 4, value = DateAndTimeAnalyzer.MEASURE_AVERAGE_DATE)
    @Description("The average date value for the given column. The value is measured in number of days since 1970-01-01.")
    public Number getAverageDate(InputColumn<?> col) {
        String s = (String) getCrosstab().where(DateAndTimeAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(DateAndTimeAnalyzer.DIMENSION_MEASURE, DateAndTimeAnalyzer.MEASURE_AVERAGE_DATE).safeGet(null);
        return convertToDaysSinceEpoch(s);
    }

    protected static Number convertToDaysSinceEpoch(String s) {
        if (s == null) {
            return null;
        }

        final LocalDate epoch = new LocalDate(1970, 1, 1);

        final LocalDate date = LocalDate.parse(s);
        int days = Days.daysBetween(epoch, date).getDays();

        return days;
    }
}
