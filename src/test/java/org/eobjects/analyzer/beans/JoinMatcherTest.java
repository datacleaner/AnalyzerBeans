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
package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.result.DataSetResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Relationship;

public class JoinMatcherTest extends MetaModelTestCase {

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor<JoinMatcher> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(JoinMatcher.class);

		List<ConfiguredPropertyDescriptor> configuredProperties = new ArrayList<ConfiguredPropertyDescriptor>(
				descriptor.getConfiguredProperties());
		assertEquals(4, configuredProperties.size());

		assertEquals("Right table", configuredProperties.get(0).getName());
		assertEquals("Left table join column", configuredProperties.get(1)
				.getName());
		assertEquals("Right table join column", configuredProperties.get(2)
				.getName());
		assertEquals("Left table", configuredProperties.get(3).getName());
	}

	public void testNoMismatch() throws Exception {
		DataContext dc = DataContextFactory
				.createJdbcDataContext(getTestDbConnection());
		Relationship r = dc.getDefaultSchema().getRelationships()[0];
		assertEquals(
				"Relationship[primaryTable=PRODUCTS,primaryColumns=[PRODUCTCODE],foreignTable=ORDERFACT,foreignColumns=[PRODUCTCODE]]",
				r.toString());

		JoinMatcher bean = new JoinMatcher();
		bean.setLeftTable(r.getPrimaryTable());
		bean.setRightTable(r.getForeignTable());
		bean.setLeftTableJoinColumn(r.getPrimaryColumns()[0]);
		bean.setRightTableJoinColumn(r.getForeignColumns()[0]);

		bean.run(dc);

		DataSetResult unmatchedRows = bean.getResult();
		List<Object[]> rowData = unmatchedRows.getDataSet().toObjectArrays();

		// There is a single product registered in the test-database that is not
		// referenced by an order
		assertEquals(1, rowData.size());
		assertEquals(
				"[S18_3233, 1985 Toyota Supra, Classic Cars, 1:18, Highway 66 Mini Classics, This model features soft rubber tires, working steering, rubber mud guards, authentic Ford logos, detailed undercarriage, opening doors and hood, removable split rear gate, full size spare mounted in bed, detailed interior with opening glove box, 7733, 57.01, 107.57, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null]",
				Arrays.toString(rowData.get(0)));
	}
}
