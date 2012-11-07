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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ELFormulaMetricDescriptorTest extends TestCase {

    private final Map<String, MetricDescriptor> _variables = new HashMap<String, MetricDescriptor>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _variables.put("foo", new MockMetricDescriptor(100));
        _variables.put("bar", new MockMetricDescriptor(13));
        _variables.put("baz", new MockMetricDescriptor(456));
    }

    public void testGetValueSingleVariable() throws Exception {
        ELFormulaMetricDescriptor formulaMetricDescriptor = new ELFormulaMetricDescriptor("#{foo}", _variables);
        Number result = formulaMetricDescriptor.getValue(null, null);
        assertEquals(100, result.intValue());
    }

    public void testGetValueMultipleVariables() throws Exception {
        ELFormulaMetricDescriptor formulaMetricDescriptor = new ELFormulaMetricDescriptor("#{foo * 100 / bar}",
                _variables);
        Number result = formulaMetricDescriptor.getValue(null, null);
        assertEquals(769, result.intValue());
    }

    public void testGetValueVariablesAndConstants() throws Exception {
        ELFormulaMetricDescriptor formulaMetricDescriptor = new ELFormulaMetricDescriptor("baz + 100", _variables);
        Number result = formulaMetricDescriptor.getValue(null, null);
        assertEquals(556, result.intValue());
    }

    public void testGetValueNoCurlyBrackets() throws Exception {
        ELFormulaMetricDescriptor formulaMetricDescriptor = new ELFormulaMetricDescriptor("foo * 100 / bar", _variables);
        Number result = formulaMetricDescriptor.getValue(null, null);
        assertEquals(769, result.intValue());
    }
}
