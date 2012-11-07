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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.eobjects.analyzer.result.AnalyzerResult;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * A {@link MetricDescriptor} which is based on an EL formula as well as a set
 * of child metric descriptors.
 */
public class ELFormulaMetricDescriptor extends AbstractMetricDescriptor implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    private final String _formula;
    private final Map<String, MetricDescriptor> _children;

    /**
     * Constructs an {@link ELFormulaMetricDescriptor} based on a formula and a
     * map of variable names and metrics to use for variable value resolution.
     * 
     * @param formula
     *            a formula to use, eg. "#{duplicates} / #{rowCount}"
     * @param children
     *            the variables
     */
    public ELFormulaMetricDescriptor(String formula, Map<String, MetricDescriptor> children) {
        if (formula == null) {
            throw new IllegalArgumentException("Formula cannot be null");
        }
        _formula = formula;
        _children = children;
    }

    @Override
    public String getName() {
        return _formula;
    }

    @Override
    public Number getValue(AnalyzerResult result, MetricParameters metricParameters) {
        final ExpressionFactory factory = createExpressionFactory();
        final ELContext context = createContext(factory);

        for (Map.Entry<String, MetricDescriptor> child : _children.entrySet()) {
            final String childVariableName = child.getKey();
            final MetricDescriptor childMetric = child.getValue();
            final Number childValue = childMetric.getValue(result, metricParameters);
            context.getELResolver().setValue(context, null, childVariableName, childValue);
        }

        final String formula;
        if (_formula.indexOf("#{") == -1) {
            formula = "#{" + _formula + "}";
        } else {
            formula = _formula;
        }

        final ValueExpression valueExpression = factory.createValueExpression(context, formula, Integer.class);
        return (Number) valueExpression.getValue(context);
    }

    /**
     * Creates the {@link ELContext} to use for evaluating the formula. Override
     * this method if JUEL is not the desired backend EL engine.
     * 
     * @param factory
     *            the expression factory.
     * @return
     */
    private ELContext createContext(ExpressionFactory factory) {
        return new SimpleContext();
    }

    /**
     * Creates the {@link ExpressionFactory} to use for evaluating the formula.
     * Override this method if JUEL is not the desired backend EL engine.
     * 
     * @return
     */
    protected ExpressionFactory createExpressionFactory() {
        return new ExpressionFactoryImpl();
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(AnalyzerResult result) {
        return Collections.emptyList();
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public boolean isParameterizedByInputColumn() {
        return false;
    }

    @Override
    public boolean isParameterizedByString() {
        return false;
    }

}
