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
package org.eobjects.analyzer.job.builder;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Table;

public class RowProcessingAnalyzerJobBuilderTest extends TestCase {

	private AnalysisJobBuilder ajb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ajb = new AnalysisJobBuilder(TestHelper.createAnalyzerBeansConfiguration());
	}

	public void testBuildMultipleJobsForSingleInputAnalyzer() throws Exception {
		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> jobBuilder = ajb
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);

		assertFalse(jobBuilder.isConfigured());

		Table table = new MutableTable("table");
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("foo", ColumnType.VARCHAR, table, 0, true)));
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("bar", ColumnType.VARCHAR, table, 1, true)));

		// change a property
		ConfiguredPropertyDescriptor property = jobBuilder.getDescriptor().getConfiguredProperty(
				"Discriminate negative numbers");
		jobBuilder.setConfiguredProperty(property, false);

		try {
			// cannot create a single job, since there will be two
			jobBuilder.toAnalyzerJob();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("This builder generates 2 jobs, but a single job was requested", e.getMessage());
		}

		assertTrue(jobBuilder.isConfigured());
		AnalyzerJob[] analyzerJobs = jobBuilder.toAnalyzerJobs();
		assertEquals(2, analyzerJobs.length);

		assertEquals(1, analyzerJobs[0].getInput().length);
		assertEquals("foo", analyzerJobs[0].getInput()[0].getName());
		assertEquals(false, analyzerJobs[0].getConfiguration().getProperty(property));

		assertEquals(1, analyzerJobs[1].getInput().length);
		assertEquals("bar", analyzerJobs[1].getInput()[0].getName());
		assertEquals(false, analyzerJobs[1].getConfiguration().getProperty(property));
	}

	public void testNoOriginatingTableBecauseOfMockColumns() throws Exception {
		RowProcessingAnalyzerJobBuilder<StringAnalyzer> jobBuilder = ajb.addRowProcessingAnalyzer(StringAnalyzer.class);
		jobBuilder.addInputColumn(new MockInputColumn<String>("foo", String.class));
		jobBuilder.addInputColumn(new MockInputColumn<String>("bar", String.class));

		try {
			jobBuilder.toAnalyzerJob();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals(
					"Could not determine source for analyzer 'RowProcessingAnalyzerJobBuilder[analyzer=String analyzer,inputColumns=[MockInputColumn[name=foo], MockInputColumn[name=bar]]]'",
					e.getMessage());
		}
	}

	public void testBuildMultipleJobsForEachTable() throws Exception {
		RowProcessingAnalyzerJobBuilder<StringAnalyzer> jobBuilder = ajb.addRowProcessingAnalyzer(StringAnalyzer.class);

		Table table1 = new MutableTable("table1");
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("foo", ColumnType.VARCHAR, table1, 0, true)));
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("bar", ColumnType.VARCHAR, table1, 1, true)));

		Table table2 = new MutableTable("table2");
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("w00p", ColumnType.VARCHAR, table2, 0, true)));
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("weee", ColumnType.VARCHAR, table2, 1, true)));
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("wohoo", ColumnType.VARCHAR, table2, 2, true)));

		AnalyzerJob[] analyzerJobs = jobBuilder.toAnalyzerJobs();
		assertEquals(2, analyzerJobs.length);

		assertEquals(2, analyzerJobs[0].getInput().length);
		assertEquals("foo", analyzerJobs[0].getInput()[0].getName());
		assertEquals("bar", analyzerJobs[0].getInput()[1].getName());

		assertEquals(3, analyzerJobs[1].getInput().length);
		assertEquals("w00p", analyzerJobs[1].getInput()[0].getName());
		assertEquals("weee", analyzerJobs[1].getInput()[1].getName());
		assertEquals("wohoo", analyzerJobs[1].getInput()[2].getName());
	}
}
