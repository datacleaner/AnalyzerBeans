package org.eobjects.analyzer.job;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.samples.SimpleCounter;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;

public class AnalysisRunnerTest extends MetaModelTestCase {

	public void testRun() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dc = DataContextFactory.createJdbcDataContext(connection);

		AnalysisJob job = new AnalysisJob();
		job.setAnalyzerClass(SimpleCounter.class);
		Map<String, String[]> tableProperties = new HashMap<String, String[]>();
		tableProperties.put("Table to count", new String[] { dc
				.getDefaultSchema().getTableByName("EMPLOYEES")
				.getQualifiedLabel() });
		job.setTableProperties(tableProperties);

		AnalysisRunner runner = new AnalysisRunner();
		runner.addJob(job);
		runner.run(dc);

		assertEquals(1, runner.getResult().getAnalyzerBeanResults().size());
	}
}
