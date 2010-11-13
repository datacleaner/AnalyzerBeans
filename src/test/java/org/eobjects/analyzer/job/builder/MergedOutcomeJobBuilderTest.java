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

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.filter.NotNullFilter;
import org.eobjects.analyzer.beans.filter.SingleWordFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ImmutableFilterOutcome;
import org.eobjects.analyzer.job.MergedOutcome;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.TestHelper;

public class MergedOutcomeJobBuilderTest extends TestCase {

	public void testSimpleBuildNoColumnMerge() throws Exception {
		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
				TestHelper.createAnalyzerBeansConfiguration(TestHelper.createSampleDatabaseDatastore("mydb")));

		analysisJobBuilder.setDatastore("mydb");
		analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.REPORTSTO");
		analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME");

		FilterJobBuilder<NotNullFilter, ValidationCategory> fjb1 = analysisJobBuilder.addFilter(NotNullFilter.class);
		fjb1.addInputColumn(analysisJobBuilder.getSourceColumns().get(0));

		FilterJobBuilder<SingleWordFilter, ValidationCategory> fjb2 = analysisJobBuilder.addFilter(SingleWordFilter.class);
		fjb2.addInputColumn(analysisJobBuilder.getSourceColumns().get(1));

		MergedOutcomeJobBuilder mergedOutcomeJobBuilder = analysisJobBuilder.addMergedOutcomeJobBuilder();

		try {
			mergedOutcomeJobBuilder.toMergedOutcomeJob();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("Merged outcome jobs need at least 2 merged outcomes, none found", e.getMessage());
		}

		mergedOutcomeJobBuilder.addMergedOutcome(fjb1, ValidationCategory.VALID);

		try {
			mergedOutcomeJobBuilder.toMergedOutcomeJob();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("Merged outcome jobs need at least 2 merged outcomes, only 1 found", e.getMessage());
		}

		mergedOutcomeJobBuilder.addMergedOutcome(fjb2, ValidationCategory.VALID);

		MergedOutcomeJob mergedOutcomeJob = mergedOutcomeJobBuilder.toMergedOutcomeJob();

		assertEquals(2, mergedOutcomeJob.getMergeInputs().length);
		assertEquals(0, mergedOutcomeJob.getOutput().length);

		MergedOutcome outcome = mergedOutcomeJob.getOutcome();

		assertTrue(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb1.toFilterJob(), ValidationCategory.VALID)));
		assertFalse(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb1.toFilterJob(), ValidationCategory.INVALID)));
		assertTrue(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb2.toFilterJob(), ValidationCategory.VALID)));
		assertFalse(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb2.toFilterJob(), ValidationCategory.INVALID)));

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		ajb.setRequirement(outcome);
	}

	public void testRunAnalysis() throws Throwable {
		AnalyzerBeansConfiguration conf = TestHelper.createAnalyzerBeansConfiguration(new CsvDatastore("mydb",
				"src/test/resources/employees-missing-values.csv"));
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);

		ajb.setDatastore("mydb");
		ajb.addSourceColumns("name");
		ajb.addSourceColumns("email");

		InputColumn<?> fnCol = ajb.getSourceColumnByName("name");
		assertNotNull(fnCol);
		InputColumn<?> emailCol = ajb.getSourceColumnByName("email");
		assertNotNull(emailCol);

		TransformerJobBuilder<EmailStandardizerTransformer> t = ajb.addTransformer(EmailStandardizerTransformer.class);
		t.addInputColumn(emailCol);

		MutableInputColumn<?> usernameCol = t.getOutputColumnByName("Username");
		assertNotNull(usernameCol);

		FilterJobBuilder<NotNullFilter, ValidationCategory> f = ajb.addFilter(NotNullFilter.class);
		f.addInputColumn(usernameCol);

		MergedOutcomeJobBuilder mojb = ajb.addMergedOutcomeJobBuilder();
		mojb.addMergedOutcome(f, ValidationCategory.VALID).addInputColumn(usernameCol);
		mojb.addMergedOutcome(f, ValidationCategory.INVALID).addInputColumn(fnCol);

		MergedOutcomeJob mergedOutcomeJob = mojb.toMergedOutcomeJob();
		assertNotNull(mergedOutcomeJob);

		InputColumn<?>[] output = mergedOutcomeJob.getOutput();
		assertEquals(1, output.length);
		InputColumn<?> outputColumn = output[0];
		assertTrue(outputColumn.isVirtualColumn());
		assertEquals("Merged column 1", outputColumn.getName());

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> a = ajb.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		a.addInputColumn(outputColumn);

		AnalysisJob analysisJob = ajb.toAnalysisJob();

		AnalysisRunnerImpl runner = new AnalysisRunnerImpl(conf);

		AnalysisResultFuture resultFuture = runner.run(analysisJob);

		if (resultFuture.isErrornous()) {
			List<Throwable> errors = resultFuture.getErrors();
			for (Throwable throwable : errors) {
				throwable.printStackTrace();
			}
			throw errors.get(0);
		}

		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(1, results.size());

		CrosstabResult result = (CrosstabResult) results.get(0);
		String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals("                Match count Sample      ", resultLines[0]);
		assertEquals("aaaa.aaa                  3 john.doe    ", resultLines[1]);
		assertEquals("Aaa. Aaaaaa Aaa           1 Mrs. Foobar Foo ", resultLines[2]);
		assertEquals("Aaaaaaa Aaaaa             1 Asbjørn Leeth ", resultLines[3]);
		assertEquals("aaaaaa                    1 kasper      ", resultLines[4]);
	}
}
