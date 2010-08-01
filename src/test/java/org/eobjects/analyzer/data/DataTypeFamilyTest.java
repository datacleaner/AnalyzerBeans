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
