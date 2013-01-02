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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.Metric;

public class NumberAnalyzerResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<? extends Number>[] _columns;

    public NumberAnalyzerResult(InputColumn<? extends Number>[] columns, Crosstab<?> crosstab) {
        super(crosstab);
        _columns = columns;
    }

    public InputColumn<? extends Number>[] getColumns() {
        return _columns;
    }

    @Metric(NumberAnalyzer.MEASURE_ROW_COUNT)
    public Number getRowCount(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_ROW_COUNT).get();
    }
    
    @Metric(NumberAnalyzer.MEASURE_NULL_COUNT)
    public Number getNullCount(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_NULL_COUNT).get();
    }
    
    @Metric(NumberAnalyzer.MEASURE_HIGHEST_VALUE)
    public Number getHighestValue(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_HIGHEST_VALUE).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_LOWEST_VALUE)
    public Number getLowestValue(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_LOWEST_VALUE).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_SUM)
    public Number getSum(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_SUM).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_MEAN)
    public Number getMean(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_MEAN).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_GEOMETRIC_MEAN)
    public Number getGeometricMean(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_GEOMETRIC_MEAN).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_STANDARD_DEVIATION)
    public Number getStandardDeviation(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_STANDARD_DEVIATION).safeGet(null);
    }
    
    @Metric(NumberAnalyzer.MEASURE_VARIANCE)
    public Number getVariance(InputColumn<?> col) {
        return (Number) getCrosstab().where(NumberAnalyzer.DIMENSION_COLUMN, col.getName())
                .where(NumberAnalyzer.DIMENSION_MEASURE, NumberAnalyzer.MEASURE_VARIANCE).safeGet(null);
    }
}
