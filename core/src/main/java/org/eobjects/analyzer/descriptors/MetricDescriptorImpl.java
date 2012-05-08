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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.util.ReflectionUtils;

final class MetricDescriptorImpl implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    private final Method _method;

    public MetricDescriptorImpl(Method method) {
        _method = method;
        _method.setAccessible(true);
    }

    public Method getMethod() {
        return _method;
    }

    @Override
    public String getName() {
        Metric metric = getAnnotation(Metric.class);
        return metric.value();
    }

    @Override
    public int compareTo(MetricDescriptor o) {
        Metric metric1 = getAnnotation(Metric.class);
        final int order1 = metric1.order();
        Metric metric2 = o.getAnnotation(Metric.class);
        final int order2;
        if (metric2 == null) {
            order2 = Integer.MAX_VALUE;
        } else {
            order2 = metric2.order();
        }
        int diff = order1 - order2;
        if (diff == 0) {
            return getName().compareTo(o.getName());
        }
        return diff;
    }

    @Override
    public Number getValue(AnalyzerResult result, MetricParameters metricParameters) {
        Object[] methodParameters = createMethodParameters(metricParameters);
        try {
            Object returnValue = _method.invoke(result, methodParameters);
            return (Number) returnValue;
        } catch (Exception e) {
            throw new IllegalStateException("Could not invoke metric getter " + _method, e);
        }
    }

    private Object[] createMethodParameters(MetricParameters metricParameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        Annotation[] annotations = getMethod().getAnnotations();
        return new HashSet<Annotation>(Arrays.asList(annotations));
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(_method, annotationClass);
    }

    @Override
    public String getDescription() {
        Description desc = getAnnotation(Description.class);
        if (desc == null) {
            return null;
        }
        return desc.value();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_method == null) ? 0 : _method.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricDescriptorImpl other = (MetricDescriptorImpl) obj;
        if (_method == null) {
            if (other._method != null)
                return false;
        } else if (!_method.equals(other._method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + "]";
    }

}
