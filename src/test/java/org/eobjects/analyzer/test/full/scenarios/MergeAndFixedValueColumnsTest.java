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

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.test.TestHelper;

public class MergeAndFixedValueColumnsTest extends TestCase {

	public void testScenario() throws Throwable {
		CsvDatastore datastore = new CsvDatastore("my database", "src/test/resources/example-name-lengths.csv");
		AnalyzerBeansConfiguration configuration = TestHelper.createAnalyzerBeansConfiguration(datastore);
		AnalysisJob job = new JaxbJobReader(configuration).read(new FileInputStream(
				"src/test/resources/example-job-merged-fixed-values.xml"));

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
		AnalysisResultFuture resultFuture = runner.run(job);

		if (!resultFuture.isSuccessful()) {
			throw resultFuture.getErrors().get(0);
		}

		ValueDistributionResult result = (ValueDistributionResult) resultFuture.getResults().get(0);
		assertEquals(7, result.getCount("REGULAR NAME").intValue());
		assertEquals(1, result.getCount("NO NAME").intValue());
		assertEquals(1, result.getCount("LONG NAME").intValue());
		assertEquals(4, result.getCount("SHORT NAME").intValue());
	}
}
