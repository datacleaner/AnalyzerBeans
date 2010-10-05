package org.eobjects.analyzer.beans;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.transform.DictionaryMatcherTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.JaxbJobFactory;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.test.TestHelper;

public class DictionaryMatcherTransformerTest extends TestCase {

	public void testParseAndAssignDictionaries() throws Exception {
		Datastore datastore = new CsvDatastore("my database",
				"src/test/resources/projects.csv");
		AnalyzerBeansConfiguration configuration = TestHelper
				.createAnalyzerBeansConfiguration(datastore);
		AnalysisJobBuilder job = new JaxbJobFactory(configuration)
				.create(new File(
						"src/test/resources/example-job-dictionary.xml"));
		assertTrue(job.isConfigured());

		AnalysisJob analysisJob = job.toAnalysisJob();
		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(
				configuration).run(analysisJob);
		List<AnalyzerResult> results = resultFuture.getResults();

		assertEquals(4, results.size());
		ValueDistributionResult res = (ValueDistributionResult) results.get(0);
		assertEquals("eobjects match", res.getColumnName());
		assertEquals(8, res.getCount("true").intValue());
		assertEquals(4, res.getCount("false").intValue());

		res = (ValueDistributionResult) results.get(1);
		assertEquals("apache match", res.getColumnName());
		assertEquals(2, res.getCount("true").intValue());
		assertEquals(10, res.getCount("false").intValue());

		res = (ValueDistributionResult) results.get(2);
		assertEquals("logging match", res.getColumnName());
		assertEquals(3, res.getCount("true").intValue());
		assertEquals(9, res.getCount("false").intValue());

		res = (ValueDistributionResult) results.get(3);
		assertEquals("logging match -> number", res.getColumnName());
		assertEquals(3, res.getCount("1").intValue());
		assertEquals(9, res.getCount("0").intValue());
	}

	public void testTransform() throws Exception {
		Dictionary[] dictionaries = new Dictionary[] {
				new SimpleDictionary("danish male names", "kasper", "kim",
						"asbjørn"),
				new SimpleDictionary("danish female names", "trine", "kim",
						"lene") };
		DictionaryMatcherTransformer transformer = new DictionaryMatcherTransformer(
				dictionaries);
		assertEquals("[true, false]", Arrays.toString(transformer.transform("kasper")));
		assertEquals("[false, false]", Arrays.toString(transformer.transform("foobar")));
		assertEquals("[false, true]", Arrays.toString(transformer.transform("trine")));
		assertEquals("[true, true]", Arrays.toString(transformer.transform("kim")));
	}
}
