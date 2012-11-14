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
package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.HasAnalyzerResult;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Abstract implementation of the {@link HasAnalyzerResultBeanDescriptor}
 * interface. Convenient for implementing it's subclasses.
 * 
 * @author Kasper Sørensen
 * 
 * @param <B>
 */
abstract class AbstractHasAnalyzerResultBeanDescriptor<B extends HasAnalyzerResult<?>> extends
        AbstractBeanDescriptor<B> implements HasAnalyzerResultBeanDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private final Class<? extends AnalyzerResult> _resultClass;
    private final Set<MetricDescriptor> _metrics;

    public AbstractHasAnalyzerResultBeanDescriptor(Class<B> beanClass, boolean requireInputColumns) {
        super(beanClass, requireInputColumns);

        Class<?> typeParameter = ReflectionUtils.getTypeParameter(getComponentClass(), HasAnalyzerResult.class, 0);

        @SuppressWarnings("unchecked")
        Class<? extends AnalyzerResult> resultClass = (Class<? extends AnalyzerResult>) typeParameter;
        _resultClass = resultClass;

        Method[] metricMethods = ReflectionUtils.getMethods(resultClass, Metric.class);
        _metrics = new TreeSet<MetricDescriptor>();
        for (Method method : metricMethods) {
            MetricDescriptor metric = new MetricDescriptorImpl(resultClass, method);
            _metrics.add(metric);
        }
    }

    @Override
    public Class<? extends AnalyzerResult> getResultClass() {
        return _resultClass;
    }
    
    @Override
    public MetricDescriptor getResultMetric(String name) {
        if (name == null) {
            return null;
        }
        
        for (MetricDescriptor metric : _metrics) {
            if (name.equals(metric.getName())) {
                return metric;
            }
        }
        
        // second try - case insensitive
        for (MetricDescriptor metric : _metrics) {
            if (name.equalsIgnoreCase(metric.getName())) {
                return metric;
            }
        }
        return null;
    }

    @Override
    public Set<MetricDescriptor> getResultMetrics() {
        if (_metrics == null) {
            // can happen with deserialized instances only
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(_metrics);
    }
}
