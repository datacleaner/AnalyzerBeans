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
package org.eobjects.analyzer.beans.coalesce;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;

public class CoalesceUnitConverterTest extends TestCase {
    
    private final CoalesceUnitConverter converter = new CoalesceUnitConverter();

    public void testGetOutputDataType() throws Exception {
        MockInputColumn<?> numberCol1 = new MockInputColumn<Number>("num1", Number.class);
        MockInputColumn<?> numberCol2 = new MockInputColumn<Number>("num1", Number.class);
        MockInputColumn<?> integerCol1 = new MockInputColumn<Integer>("int1", Integer.class);
        MockInputColumn<?> integerCol2 = new MockInputColumn<Integer>("int2", Integer.class);
        MockInputColumn<?> stringCol1 = new MockInputColumn<String>("str1", String.class);
        MockInputColumn<?> stringCol2 = new MockInputColumn<String>("str2", String.class);
        MockInputColumn<?> objCol1 = new MockInputColumn<Object>("obj1", Object.class);

        InputColumn<?>[] allColumns = new InputColumn[] { numberCol1, numberCol2, integerCol1, integerCol2, stringCol1,
                stringCol2, objCol1 };

        
        CoalesceUnit unit1 = new CoalesceUnit(stringCol1, stringCol2);
        String str = converter.toString(unit1);
        assertEquals("[str1,str2]", str);
        
        CoalesceUnit unit2 = converter.fromString(CoalesceUnit.class, str);
        assertEquals("[str1, str2]", Arrays.toString(unit2.getInputColumnNames()));
        assertEquals(String.class, unit2.getOutputDataType(allColumns));
    }
}
