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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract reducer class for {@link CrosstabResult}s that are two dimensional
 * and the dimensions are the same on all slave results. This scenario is quite
 * common since a lot of analyzers produce crosstabs with measures on one
 * dimension and column names on another.
 */
public abstract class AbstractCrosstabResultReducer<R extends CrosstabResult> implements AnalyzerResultReducer<R> {

    @Override
    public R reduce(Collection<? extends R> results) {
        // just use the first result to define the crosstab structure
        final R firstResult = results.iterator().next();

        assert firstResult.getCrosstab().getDimensionCount() == 2;

        final Class<?> valueClass = firstResult.getCrosstab().getValueClass();
        final CrosstabDimension dimension1 = firstResult.getCrosstab().getDimension(0);
        final CrosstabDimension dimension2 = firstResult.getCrosstab().getDimension(1);

        final Crosstab<Serializable> masterCrosstab = createCrosstab(valueClass, dimension1, dimension2);

        final CrosstabNavigator<Serializable> masterNav = masterCrosstab.navigate();
        for (String category1 : dimension1) {
            masterNav.where(dimension1.getName(), category1);
            for (String category2 : dimension2) {
                masterNav.where(dimension2.getName(), category2);

                final String[] categories = new String[] { category1, category2 };

                final List<ResultProducer> slaveResultProducers = new ArrayList<ResultProducer>();
                final List<Object> slaveValues = new ArrayList<Object>(results.size());
                for (R result : results) {
                    final Crosstab<?> slaveCrosstab = result.getCrosstab();
                    final Object slaveValue = slaveCrosstab.getValue(categories);
                    slaveValues.add(slaveValue);

                    final ResultProducer resultProducer = slaveCrosstab.explore(categories);
                    if (resultProducer != null) {
                        slaveResultProducers.add(resultProducer);
                    }
                }

                final Serializable masterValue = reduceValues(slaveValues, category1, category2, valueClass);
                masterNav.put(masterValue);

                if (!slaveResultProducers.isEmpty()) {
                    final ResultProducer masterResultProducer = reduceResultProducers(slaveResultProducers, category1,
                            category2, valueClass, masterValue);
                    if (masterResultProducer != null) {
                        masterNav.attach(masterResultProducer);
                    }
                }
            }
        }

        return buildResult(masterCrosstab, results);
    }

    protected ResultProducer reduceResultProducers(List<ResultProducer> slaveResultProducers, String category1,
            String category2, Class<?> valueClass, Serializable masterValue) {
        for (ResultProducer resultProducer : slaveResultProducers) {
            AnalyzerResult result = resultProducer.getResult();
            if (result instanceof AnnotatedRowsResult) {
                if (((AnnotatedRowsResult) result).getAnnotatedRowCount() > 0) {
                    // just return the first annotated rows result - these are
                    // anyways "just" samples
                    return resultProducer;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Crosstab<Serializable> createCrosstab(Class<?> valueClass, CrosstabDimension dimension1,
            CrosstabDimension dimension2) {
        return new Crosstab(valueClass, dimension1, dimension2);
    }

    protected abstract Serializable reduceValues(List<Object> slaveValues, String category1, String category2,
            Class<?> valueClass);

    protected abstract R buildResult(final Crosstab<?> crosstab, final Collection<? extends R> results);
}
