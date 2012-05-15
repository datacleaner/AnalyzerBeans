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
package org.eobjects.analyzer.beans.valuedist;

import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.metamodel.schema.MutableColumn;

public class ValueDistributionAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor<?> desc = Descriptors
				.ofAnalyzer(ValueDistributionAnalyzer.class);
		assertEquals(0, desc.getInitializeMethods().size());
		assertEquals(6, desc.getConfiguredProperties().size());
		assertEquals(2, desc.getProvidedProperties().size());
		assertEquals("Value distribution", desc.getDisplayName());
	}

	public void testGetCounts() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
				new MetaModelInputColumn(new MutableColumn("col")), true, null,
				null);

		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getNullCount());
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), "hello", 1);
		assertEquals(1, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(1, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(1, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), "world", 1);
		assertEquals(2, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(2, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(2, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), "foobar", 2);
		assertEquals(2, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(3, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(4, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), "world", 1);
		assertEquals(1, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(3, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(5, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), "hello", 3);
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(3, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(8, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), null, 1);
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(1, vd.getResult().getSingleValueDistributionResult()
				.getNullCount());
		assertEquals(4, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(9, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());

		vd.runInternal(new MockInputRow(), null, 3);
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());
		assertEquals(4, vd.getResult().getSingleValueDistributionResult()
				.getNullCount());
		assertEquals(4, vd.getResult().getSingleValueDistributionResult()
				.getDistinctCount());
		assertEquals(12, vd.getResult().getSingleValueDistributionResult()
				.getTotalCount());
	}

	public void testGetValueDistribution() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
				new MetaModelInputColumn(new MutableColumn("col")), true, null,
				null);

		vd.runInternal(new MockInputRow(), "hello", 1);
		vd.runInternal(new MockInputRow(), "hello", 1);
		vd.runInternal(new MockInputRow(), "world", 3);

		ValueCountList topValues = vd.getResult()
				.getSingleValueDistributionResult().getTopValues();
		assertEquals(2, topValues.getActualSize());
		assertEquals("[world->3]", topValues.getValueCounts().get(0).toString());
		assertEquals("[hello->2]", topValues.getValueCounts().get(1).toString());

		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getNullCount());
		assertEquals(0, vd.getResult().getSingleValueDistributionResult()
				.getUniqueCount());

		String[] resultLines = vd.getResult().toString().split("\n");
		assertEquals(6, resultLines.length);
		assertEquals("Value distribution for column: col", resultLines[0]);
		assertEquals("Top values:", resultLines[1]);
		assertEquals(" - world: 3", resultLines[2]);
		assertEquals(" - hello: 2", resultLines[3]);
		assertEquals("Null count: 0", resultLines[4]);
		assertEquals("Unique values: 0", resultLines[5]);
	}

	public void testGroupedRun() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
				new MockInputColumn<String>("foo", String.class),
				new MockInputColumn<String>("bar", String.class), true, null,
				null);

		vd.runInternal(new MockInputRow(), "Copenhagen N", "2200", 3);
		vd.runInternal(new MockInputRow(), "Copenhagen E", "2100", 2);
		vd.runInternal(new MockInputRow(), "Copenhagen", "1732", 4);
		vd.runInternal(new MockInputRow(), "Coppenhagen", "1732", 3);

		ValueDistributionResult result = vd.getResult();
		assertTrue(result.isGroupingEnabled());

		String resultString = result.toString();
		System.out.println(resultString);
		String[] resultLines = resultString.split("\n");
		assertEquals(20, resultLines.length);

		assertEquals("Value distribution for column: foo", resultLines[0]);
		assertEquals("", resultLines[1]);
		assertEquals("Group: 1732", resultLines[2]);
		assertEquals("Top values:", resultLines[3]);
		assertEquals(" - Copenhagen: 4", resultLines[4]);
		assertEquals(" - Coppenhagen: 3", resultLines[5]);
		assertEquals("Null count: 0", resultLines[6]);
		assertEquals("Unique values: 0", resultLines[7]);
		assertEquals("", resultLines[8]);
		assertEquals("Group: 2100", resultLines[9]);
		assertEquals("Top values:", resultLines[10]);
		assertEquals(" - Copenhagen E: 2", resultLines[11]);
		assertEquals("Null count: 0", resultLines[12]);
		assertEquals("Unique values: 0", resultLines[13]);
		assertEquals("", resultLines[14]);
		assertEquals("Group: 2200", resultLines[15]);
		assertEquals("Top values:", resultLines[16]);
		assertEquals(" - Copenhagen N: 3", resultLines[17]);
		assertEquals("Null count: 0", resultLines[18]);
		assertEquals("Unique values: 0", resultLines[19]);
	}
	
	 @SuppressWarnings("deprecation")
    public void testDataTypeFamilyDescriptor() throws Exception {
	        AnalyzerBeanDescriptor<?> descriptor = Descriptors.ofAnalyzer(ValueDistributionAnalyzer.class);
	        Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredPropertiesForInput(false);
	        assertEquals(1, configuredProperties.size());
	        ConfiguredPropertyDescriptor propertyDescriptor = configuredProperties.iterator().next();
	        assertEquals(org.eobjects.analyzer.data.DataTypeFamily.UNDEFINED, propertyDescriptor.getInputColumnDataTypeFamily());
	    }
}
