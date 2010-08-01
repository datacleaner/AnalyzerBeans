package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.mock.TransformerMock;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.test.MockDataContextProvider;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class RowProcessingPublisherTest extends TestCase {

	public void testCreateProcessOrderedConsumerListNoConsumers()
			throws Exception {
		List<RowProcessingConsumer> consumerList = RowProcessingPublisher
				.createProcessOrderedConsumerList(new ArrayList<RowProcessingConsumer>());
		assertTrue(consumerList.isEmpty());
	}

	public void testCreateProcessOrderedConsumerListChainedTransformers()
			throws Exception {

		AnalysisJobBuilder ajb = new AnalysisJobBuilder(
				TestHelper.createAnalyzerBeansConfiguration());
		Column physicalColumn = new Column("foo", ColumnType.VARCHAR);
		ajb.addSourceColumn(physicalColumn);

		TransformerJobBuilder tjb1 = ajb.addTransformer(TransformerMock.class)
				.addInputColumn(ajb.getSourceColumns().get(0));
		TransformerJobBuilder tjb2 = ajb.addTransformer(TransformerMock.class)
				.addInputColumn(tjb1.getOutputColumns().get(0));
		TransformerJobBuilder tjb3 = ajb.addTransformer(
				ConvertToStringTransformer.class).addInputColumn(
				tjb2.getOutputColumns().get(0));

		ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(
				ajb.getSourceColumns().get(0));
		ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(
				tjb3.getOutputColumns().get(0));

		ajb.setDataContextProvider(new MockDataContextProvider());

		assertTrue(ajb.isConfigured());
		AnalysisJob analysisJob = ajb.toAnalysisJob();

		List<RowProcessingConsumer> consumers = new ArrayList<RowProcessingConsumer>();
		ArrayList<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>(
				analysisJob.getAnalyzerJobs());
		ArrayList<TransformerJob> transformerJobs = new ArrayList<TransformerJob>(
				analysisJob.getTransformerJobs());

		for (AnalyzerJob analyzerJob : analyzerJobs) {
			RowProcessingConsumer consumer = new AnalyzerConsumer(
					new AnalyzerBeanInstance(analyzerJob.getDescriptor()),
					analyzerJob, analyzerJob.getInput());
			consumers.add(consumer);
		}
		for (TransformerJob transformerJob : transformerJobs) {
			RowProcessingConsumer consumer = new TransformerConsumer(
					new TransformerBeanInstance(transformerJob.getDescriptor()),
					transformerJob, transformerJob.getInput());
			consumers.add(consumer);
		}

		// shuffle the list (it should work regardless of the initial sort
		// order)
		Collections.shuffle(consumers);

		consumers = RowProcessingPublisher
				.createProcessOrderedConsumerList(consumers);

		assertEquals(5, consumers.size());

		// create a list that represents the expected dependent sequence
		Queue<BeanJob<?>> jobDependencies = new LinkedList<BeanJob<?>>();
		jobDependencies.add(transformerJobs.get(0));
		jobDependencies.add(transformerJobs.get(1));
		jobDependencies.add(transformerJobs.get(2));
		jobDependencies.add(analyzerJobs.get(1));
		
		int jobDependenciesFound = 0;
		boolean analyzerJob1found = false;

		BeanJob<?> nextJobDependency = jobDependencies.poll();
		for (RowProcessingConsumer rowProcessingConsumer : consumers) {
			BeanJob<?> job = rowProcessingConsumer.getBeanJob();
			if (job == nextJobDependency) {
				nextJobDependency = jobDependencies.poll();
				jobDependenciesFound++;
			} else if (job == analyzerJobs.get(0)) {
				assertFalse(analyzerJob1found);
				analyzerJob1found = true;
			} else {
				fail("The consumers sort order is wrong! Found: " + job
						+ " but expected: " + nextJobDependency);
			}
		}
		
		assertTrue(analyzerJob1found);
		assertEquals(4, jobDependenciesFound);
	}
}
