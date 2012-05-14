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

import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public class ConcatenatorTransformerTest extends TestCase {

    public void testConcat() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("str", String.class);
        InputColumn<Boolean> col2 = new MockInputColumn<Boolean>("bool", Boolean.class);

        ConcatenatorTransformer t = new ConcatenatorTransformer(" + ", new InputColumn[] { col1, col2 });

        assertEquals(1, t.getOutputColumns().getColumnCount());
        assertEquals("Concat of str,bool", t.getOutputColumns().getColumnName(0));

        String[] result = t.transform(new MockInputRow().put(col1, "hello").put(col2, true));
        assertEquals(1, result.length);
        assertEquals("hello + true", result[0]);

        result = t.transform(new MockInputRow().put(col1, "hi").put(col2, ""));
        assertEquals(1, result.length);
        assertEquals("hi", result[0]);

        result = t.transform(new MockInputRow().put(col1, "hi").put(col2, null));
        assertEquals(1, result.length);
        assertEquals("hi", result[0]);

        result = t.transform(new MockInputRow().put(col1, null).put(col2, true));
        assertEquals(1, result.length);
        assertEquals("true", result[0]);

        result = t.transform(new MockInputRow().put(col1, null).put(col2, null));
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @SuppressWarnings("deprecation")
    private DataTypeFamily getDataTypeFamily(TransformerBeanDescriptor<?> descriptor) {
        Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredPropertiesForInput();
        assertEquals(1, configuredProperties.size());
        ConfiguredPropertyDescriptor propertyDescriptor = configuredProperties.iterator().next();
        return propertyDescriptor.getInputColumnDataTypeFamily();
    }

    @SuppressWarnings("deprecation")
    public void testDescriptor() throws Exception {
        TransformerBeanDescriptor<?> descriptor = Descriptors.ofTransformer(ConcatenatorTransformer.class);
        assertEquals(DataTypeFamily.UNDEFINED, getDataTypeFamily(descriptor));
        assertEquals(DataTypeFamily.STRING, descriptor.getOutputDataTypeFamily());
    }
}
