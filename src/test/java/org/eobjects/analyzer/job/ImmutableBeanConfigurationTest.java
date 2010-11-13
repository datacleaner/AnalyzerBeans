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
package org.eobjects.analyzer.job;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptorImpl;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

import junit.framework.TestCase;

public class ImmutableBeanConfigurationTest extends TestCase {

	@Configured
	String[] conf;

	public void testEqualsArrayValue() throws Exception {
		Field field = getClass().getDeclaredField("conf");
		ConfiguredPropertyDescriptorImpl descriptor = new ConfiguredPropertyDescriptorImpl(
				field, null);

		Map<ConfiguredPropertyDescriptor, Object> properties1 = new HashMap<ConfiguredPropertyDescriptor, Object>();
		properties1.put(descriptor, new String[] { "hello", "world" });

		Map<ConfiguredPropertyDescriptor, Object> properties2 = new HashMap<ConfiguredPropertyDescriptor, Object>();
		properties2.put(descriptor, new String[] { "hello", "world" });

		assertEquals(new ImmutableBeanConfiguration(properties1),
				new ImmutableBeanConfiguration(properties2));
	}
}
