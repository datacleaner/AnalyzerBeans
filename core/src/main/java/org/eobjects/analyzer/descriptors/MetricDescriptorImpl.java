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
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.util.ReflectionUtils;

final class MetricDescriptorImpl implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    private final transient Method _method;

    private final String _name;
    private final Class<? extends AnalyzerResult> _resultClass;
    private final String _methodName;

    public MetricDescriptorImpl(Class<? extends AnalyzerResult> resultClass, Method method) {
        _resultClass = resultClass;
        _method = method;
        _method.setAccessible(true);

        _name = ReflectionUtils.getAnnotation(_method, Metric.class).value();
        _methodName = _method.getName();
    }

    public Method getMethod() {
        if (_method == null) {
            return ReflectionUtils.getMethod(_resultClass, _methodName);
        }
        return _method;
    }

    @Override
    public String getName() {
        return _name;
    }

    public Class<? extends AnalyzerResult> getResultClass() {
        return _resultClass;
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
        if (result == null) {
            throw new IllegalArgumentException("AnalyzerResult cannot be null");
        }
        Method method = getMethod();
        Object[] methodParameters = createMethodParameters(method, metricParameters);
        try {
            Object returnValue = method.invoke(result, methodParameters);
            return (Number) returnValue;
        } catch (Exception e) {
            throw new IllegalStateException("Could not invoke metric getter " + _method, e);
        }
    }

    private Object[] createMethodParameters(Method method, MetricParameters metricParameters) {
        final Class<?>[] parameterTypes = _method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return null;
        }

        final Object[] result = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (String.class == parameterTypes[i]) {
                result[i] = metricParameters.getQueryString();
            } else if (InputColumn.class == parameterTypes[i]) {
                result[i] = metricParameters.getQueryInputColumn();
            } else {
                throw new IllegalStateException("Unsupported metric parameter type: " + parameterTypes[i]);
            }
        }

        return result;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        Annotation[] annotations = getMethod().getAnnotations();
        return new HashSet<Annotation>(Arrays.asList(annotations));
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(getMethod(), annotationClass);
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
        result = prime * result + ((_resultClass == null) ? 0 : _resultClass.hashCode());
        result = prime * result + ((_methodName == null) ? 0 : _methodName.hashCode());
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
        if (_resultClass == null) {
            if (other._resultClass != null)
                return false;
        } else if (!_resultClass.equals(other._resultClass))
            return false;
        if (_methodName == null) {
            if (other._methodName != null)
                return false;
        } else if (!_methodName.equals(other._methodName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + "]";
    }

}
