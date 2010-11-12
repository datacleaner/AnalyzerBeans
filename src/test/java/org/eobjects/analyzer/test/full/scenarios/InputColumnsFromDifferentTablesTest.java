package org.eobjects.analyzer.test.full.scenarios;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.test.TestHelper;

/**
 * Ticket #383: Error handling when a job has been errornously configured - the
 * input columns of a transformer originate from different tables
 * 
 * @author Kasper Sørensen
 */
public class InputColumnsFromDifferentTablesTest extends TestCase {

	public void testScenario() throws Exception {
		AnalyzerBeansConfiguration conf = TestHelper.createAnalyzerBeansConfiguration(TestHelper
				.createSampleDatabaseDatastore("my database"));

		AnalysisRunner runner = new AnalysisRunnerImpl(conf);

		AnalysisJobBuilder jobBuilder = new JaxbJobReader(conf).create(new File(
				"src/test/resources/example-job-input-columns-from-different-tables.xml"));

		try {
			runner.run(jobBuilder.toAnalysisJob());
			fail("exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Input columns in ImmutableTransformerJob[transformer=Concatenator] originate from different tables",
					e.getMessage());
		}

	}
}
