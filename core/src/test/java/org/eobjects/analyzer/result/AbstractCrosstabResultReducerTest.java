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
package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

public class AbstractCrosstabResultReducerTest extends TestCase {

    public void testReduceCrosstabs() throws Exception {
        final Crosstab<Number> crosstab1 = new Crosstab<Number>(Number.class, "Measure", "Column");
        crosstab1.where("Column", "col1").where("Measure", "Null count").put(3, true);
        crosstab1.where("Column", "col2").where("Measure", "Null count").put(2, true);
        crosstab1.where("Column", "col1").where("Measure", "Valid count").put(10, true);
        crosstab1.where("Column", "col2").where("Measure", "Valid count").put(11, true);

        final Crosstab<Number> crosstab2 = new Crosstab<Number>(Number.class, "Measure", "Column");
        crosstab2.where("Column", "col1").where("Measure", "Null count").put(5, true);
        crosstab2.where("Column", "col2").where("Measure", "Null count").put(2, true);
        crosstab2.where("Column", "col1").where("Measure", "Valid count").put(20, true);
        crosstab2.where("Column", "col2").where("Measure", "Valid count").put(21, true);

        final AbstractCrosstabResultReducer<CrosstabResult> reducer = new AbstractCrosstabResultReducer<CrosstabResult>() {

            @Override
            protected Serializable reduceValues(List<Object> slaveValues, String category1, String category2, Class<?> valueClass) {
                int sum = 0;
                for (Object slaveValue : slaveValues) {
                    sum += ((Number)slaveValue).intValue();
                }
                return sum;
            }

            @Override
            protected CrosstabResult buildResult(Crosstab<?> crosstab, Collection<? extends CrosstabResult> results) {
                return new CrosstabResult(crosstab);
            }
        };

        final CrosstabResult result1 = new CrosstabResult(crosstab1);
        final CrosstabResult result2 = new CrosstabResult(crosstab2);

        final CrosstabResult masterResult = reducer.reduce(Arrays.asList(result1, result2));
        final Crosstab<?> crosstab = masterResult.getCrosstab();

        assertEquals(8, crosstab.where("Column", "col1").where("Measure", "Null count").get());
        assertEquals(4, crosstab.where("Column", "col2").where("Measure", "Null count").get());
        assertEquals(30, crosstab.where("Column", "col1").where("Measure", "Valid count").get());
        assertEquals(32, crosstab.where("Column", "col2").where("Measure", "Valid count").get());
    }
}
