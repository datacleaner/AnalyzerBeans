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
package org.eobjects.analyzer.beans.api;

import java.util.Date;

import org.eobjects.analyzer.data.DataTypeFamily;

import junit.framework.TestCase;

public class DataTypeFamilyTest extends TestCase {

	public void testEnumAccess() throws Exception {
		assertEquals("BOOLEAN", DataTypeFamily.BOOLEAN.name());
	}

	public void testValueOfJavaClass() throws Exception {
		assertEquals(DataTypeFamily.STRING, DataTypeFamily.valueOf(String.class));
		assertEquals(DataTypeFamily.BOOLEAN, DataTypeFamily.valueOf(boolean.class));
		assertEquals(DataTypeFamily.DATE, DataTypeFamily.valueOf(Date.class));
		assertEquals(DataTypeFamily.NUMBER, DataTypeFamily.valueOf(Integer.class));
		assertEquals(DataTypeFamily.UNDEFINED, DataTypeFamily.valueOf(FileProperty.class));
	}

	public void testGetJavaType() throws Exception {
		assertEquals(String.class, DataTypeFamily.STRING.getJavaType());
		assertEquals(Boolean.class, DataTypeFamily.BOOLEAN.getJavaType());
		assertEquals(Date.class, DataTypeFamily.DATE.getJavaType());
		assertEquals(Number.class, DataTypeFamily.NUMBER.getJavaType());
		assertEquals(Object.class, DataTypeFamily.UNDEFINED.getJavaType());
	}
}
