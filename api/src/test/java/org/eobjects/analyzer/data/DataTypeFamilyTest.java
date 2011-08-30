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
package org.eobjects.analyzer.data;

import java.util.Date;

import junit.framework.TestCase;

public class DataTypeFamilyTest extends TestCase {

	public void testGetJavaType() throws Exception {
		assertEquals(Boolean.class, DataTypeFamily.BOOLEAN.getJavaType());
		assertEquals(Date.class, DataTypeFamily.DATE.getJavaType());
		assertEquals(Number.class, DataTypeFamily.NUMBER.getJavaType());
		assertEquals(String.class, DataTypeFamily.STRING.getJavaType());
		assertEquals(Object.class, DataTypeFamily.UNDEFINED.getJavaType());
	}

	public void testValueOfClass() throws Exception {
		assertEquals(DataTypeFamily.BOOLEAN,
				DataTypeFamily.valueOf(Boolean.class));
		assertEquals(DataTypeFamily.DATE, DataTypeFamily.valueOf(Date.class));
		assertEquals(DataTypeFamily.NUMBER,
				DataTypeFamily.valueOf(Number.class));
		assertEquals(DataTypeFamily.NUMBER,
				DataTypeFamily.valueOf(Integer.class));
		assertEquals(DataTypeFamily.NUMBER,
				DataTypeFamily.valueOf(Double.class));
		assertEquals(DataTypeFamily.STRING,
				DataTypeFamily.valueOf(String.class));
		assertEquals(DataTypeFamily.UNDEFINED,
				DataTypeFamily.valueOf(Object.class));
		assertEquals(DataTypeFamily.UNDEFINED,
				DataTypeFamily.valueOf(Byte[].class));
	}
}
