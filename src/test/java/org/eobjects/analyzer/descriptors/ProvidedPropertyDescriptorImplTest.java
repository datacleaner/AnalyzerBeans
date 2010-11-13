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

import java.lang.reflect.Field;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Provided;

public class ProvidedPropertyDescriptorImplTest extends TestCase {

	@Provided
	Map<String, Boolean> stringMap;

	@Provided
	Map<String, Integer> intMap;

	public void testGenericTypes() throws Exception {
		Field stringMapField = getClass().getDeclaredField("stringMap");
		ProvidedPropertyDescriptorImpl descriptor = new ProvidedPropertyDescriptorImpl(
				stringMapField, null);

		assertEquals(
				"ProvidedPropertyDescriptorImpl[field=stringMap,baseType=interface java.util.Map]",
				descriptor.toString());
		
		assertEquals(2, descriptor.getTypeArgumentCount());
		assertEquals(String.class, descriptor.getTypeArgument(0));
		assertEquals(Boolean.class, descriptor.getTypeArgument(1));

		Field intMapField = getClass().getDeclaredField("intMap");
		descriptor = new ProvidedPropertyDescriptorImpl(intMapField, null);
		assertEquals(
				"ProvidedPropertyDescriptorImpl[field=intMap,baseType=interface java.util.Map]",
				descriptor.toString());
		
		assertEquals(2, descriptor.getTypeArgumentCount());
		assertEquals(String.class, descriptor.getTypeArgument(0));
		assertEquals(Integer.class, descriptor.getTypeArgument(1));
	}
}
