package org.eobjects.analyzer.beans;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.descriptors.ResultDescriptor;
import org.eobjects.analyzer.result.DataSetAnalyzerBeanResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Relationship;

public class JoinMatcherTest extends MetaModelTestCase {

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				JoinMatcher.class);

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		assertEquals(4, configuredDescriptors.size());
		assertEquals("Left table", configuredDescriptors.get(0).getName());
		assertEquals("Right table", configuredDescriptors.get(1).getName());
		assertEquals("Left table join column", configuredDescriptors.get(2)
				.getName());
		assertEquals("Right table join column", configuredDescriptors.get(3)
				.getName());

		List<ResultDescriptor> resultDescriptors = descriptor
				.getResultDescriptors();
		assertEquals(1, resultDescriptors.size());
		assertEquals("Unmatched rows", resultDescriptors.get(0).getName());
	}

	public void testNoMismatch() throws Exception {
		DataContext dc = DataContextFactory
				.createJdbcDataContext(getTestDbConnection());
		Relationship r = dc.getDefaultSchema().getRelationships()[0];
		assertEquals(
				"Relationship[primaryTable=PRODUCTS,primaryColumns={PRODUCTCODE},foreignTable=ORDERFACT,foreignColumns={PRODUCTCODE}]",
				r.toString());

		JoinMatcher bean = new JoinMatcher();
		bean.setLeftTable(r.getPrimaryTable());
		bean.setRightTable(r.getForeignTable());
		bean.setLeftTableJoinColumn(r.getPrimaryColumns()[0]);
		bean.setRightTableJoinColumn(r.getForeignColumns()[0]);

		bean.run(dc);

		DataSetAnalyzerBeanResult unmatchedRows = bean.getUnmatchedRows();
		List<Object[]> rowData = unmatchedRows.getDataSet().toObjectArrays();

		// There is a single product registered in the test-database that is not
		// referenced by an order
		assertEquals(1, rowData.size());
		assertEquals(
				"{S18_3233,1985 Toyota Supra,Classic Cars,1:18,Highway 66 Mini Classics,This model features soft rubber tires, working steering, rubber mud guards, authentic Ford logos, detailed undercarriage, opening doors and hood, removable split rear gate, full size spare mounted in bed, detailed interior with opening glove box,7733,57.01,107.57,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>}",
				ArrayUtils.toString(rowData.get(0)));
	}
}
