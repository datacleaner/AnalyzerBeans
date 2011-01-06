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
					"Input columns in ImmutableTransformerJob[name=null,transformer=Concatenator] originate from different tables",
					e.getMessage());
		}

	}
}
