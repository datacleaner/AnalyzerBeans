package org.eobjects.analyzer.test.full.scenarios;

import java.util.List;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisRunner;
import org.eobjects.analyzer.job.AnalysisRunnerImpl;
import org.eobjects.analyzer.job.MultiThreadedRunnableConsumer;
import org.eobjects.analyzer.job.RunnableConsumer;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ValueDistributionAndStringAnalysisTest extends MetaModelTestCase {

	public void testScenario() throws Exception {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.beans", true);
		RunnableConsumer runnableConsumer = new MultiThreadedRunnableConsumer(5);
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();

		AnalysisRunner runner = new AnalysisRunnerImpl(descriptorProvider,
				runnableConsumer, collectionProvider);

		DataContext dc = DataContextFactory
				.createJdbcDataContext(getTestDbConnection());

		Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		assertNotNull(table);

		Column[] columns = table.getColumns();

		for (Column column : columns) {
			AnalysisJob vdJob = new AnalysisJob(ValueDistributionAnalyzer.class);
			vdJob.putColumnProperty("Column", column.getQualifiedLabel());
			runner.addJob(vdJob);
		}

		columns = table.getLiteralColumns();
		
		AnalysisJob saJob = new AnalysisJob(StringAnalyzer.class);
		saJob.putColumnProperty("Columns", columns);
		runner.addJob(saJob);

		List<AnalyzerResult> results = runner.run(dc);
		assertEquals(1, results.size());
	}
}
