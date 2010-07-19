package org.eobjects.analyzer.job;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.mock.ExploringBeanMock;
import org.eobjects.analyzer.beans.mock.RowProcessingBeanMock;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.SimpleAnalysisRunner;
import org.eobjects.analyzer.job.runner.SimpleAnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.ProvidedList;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.test.TestHelper;

import com.sleepycat.collections.StoredMap;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class SimpleAnalysisRunnerImplTest extends MetaModelTestCase {

	private static final int NUM_EMPLOYEES = 23;
	private static final int NUM_CUSTOMERS = 122;
	private static final Integer CONFIGURED2_VALUE = 1337;
	private static final String CONFIGURED1_VALUE = "configString1";
	private DataContext dc;
	private DataContextProvider dcp;
	private Table employeesTable;
	private Table customersTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (dc == null) {
			Connection connection = getTestDbConnection();
			dc = DataContextFactory.createJdbcDataContext(connection);
			dcp = new SingleDataContextProvider(dc);
			employeesTable = dc.getDefaultSchema().getTableByName("EMPLOYEES");
			customersTable = dc.getDefaultSchema().getTableByName("CUSTOMERS");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ExploringBeanMock.clearInstances();
		RowProcessingBeanMock.clearInstances();
	}

	public void testExploringBeans() throws Exception {
		SimpleAnalyzerJob job1 = new SimpleAnalyzerJob(ExploringBeanMock.class);
		setReusableMockProperties(job1);

		SimpleAnalyzerJob job2 = new SimpleAnalyzerJob(ExploringBeanMock.class);
		setReusableMockProperties(job2);

		SimpleAnalysisRunner runner = new SimpleAnalysisRunnerImpl(
				TestHelper.createAnalyzerBeansConfiguration());
		AnalysisResultFuture resultFuture = runner.run(dcp, job1, job2);

		List<ExploringBeanMock> beans = ExploringBeanMock.getInstances();
		assertEquals(2, beans.size());

		ExploringBeanMock bean = beans.get(0);
		performReusableMockTests(bean);

		bean = beans.get(1);
		performReusableMockTests(bean);

		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(2, results.size());
	}

	public void testRowProcessingBeans() throws Exception {
		SimpleAnalyzerJob job = new SimpleAnalyzerJob(RowProcessingBeanMock.class);
		setReusableMockProperties(job);
		Column[] employeeColumns = employeesTable.getColumns();
		Column[] customerColumns = customersTable.getColumns();
		job.putColumnProperty("Columns", employeeColumns[0],
				employeeColumns[1], customerColumns[0]);

		SimpleAnalysisRunner runner = new SimpleAnalysisRunnerImpl(
				TestHelper.createAnalyzerBeansConfiguration());
		AnalysisResultFuture resultFuture = runner.run(dcp, job);

		List<RowProcessingBeanMock> beans = RowProcessingBeanMock
				.getInstances();
		assertEquals(2, beans.size());

		RowProcessingBeanMock bean = beans.get(0);
		performReusableMockTests(bean);
		assertEquals(NUM_EMPLOYEES, bean.getRowCount());
		assertEquals(NUM_EMPLOYEES, bean.getRunCount());
		InputColumn<?>[] columns = bean.getColumns();
		assertEquals(2, columns.length);
		assertEquals(employeeColumns[0], columns[0].getPhysicalColumn());
		assertEquals(employeeColumns[1], columns[1].getPhysicalColumn());

		bean = beans.get(1);
		performReusableMockTests(bean);
		assertEquals(NUM_CUSTOMERS, bean.getRowCount());
		assertEquals(NUM_CUSTOMERS, bean.getRunCount());
		columns = bean.getColumns();
		assertEquals(1, columns.length);
		assertEquals(customerColumns[0], columns[0].getPhysicalColumn());

		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(4, results.size());
	}

	public void testCombination() throws Exception {
		SimpleAnalyzerJob job1 = new SimpleAnalyzerJob(ExploringBeanMock.class);
		setReusableMockProperties(job1);

		SimpleAnalyzerJob job2 = new SimpleAnalyzerJob(ExploringBeanMock.class);
		setReusableMockProperties(job2);

		SimpleAnalyzerJob job3 = new SimpleAnalyzerJob(RowProcessingBeanMock.class);
		setReusableMockProperties(job3);
		Column[] employeeColumns = employeesTable.getColumns();
		Column[] customerColumns = customersTable.getColumns();
		job3.putColumnProperty("Columns", employeeColumns[0],
				employeeColumns[1], customerColumns[0]);

		SimpleAnalyzerJob job4 = new SimpleAnalyzerJob(RowProcessingBeanMock.class);
		setReusableMockProperties(job4);
		job4.putColumnProperty("Columns", employeeColumns[0],
				employeeColumns[1]);

		SimpleAnalysisRunnerImpl runner = new SimpleAnalysisRunnerImpl(
				TestHelper.createAnalyzerBeansConfiguration());
		AnalysisResultFuture resultFuture = runner.run(dcp, job1,job2,job3,job4);

		List<RowProcessingBeanMock> rowProcessors = RowProcessingBeanMock
				.getInstances();
		assertEquals(3, rowProcessors.size());
		for (RowProcessingBeanMock bean : rowProcessors) {
			performReusableMockTests(bean);
		}
		assertEquals(NUM_EMPLOYEES, rowProcessors.get(0).getRowCount());
		assertEquals(NUM_CUSTOMERS, rowProcessors.get(1).getRowCount());
		assertEquals(NUM_EMPLOYEES, rowProcessors.get(2).getRowCount());
		assertEquals(2, ExploringBeanMock.getInstances().size());

		resultFuture.await();
		
		// Assert that only two queries have been run even though 3 row
		// processor instances exist
		assertEquals(new Integer(2), runner.getRowProcessorCount());
	}

	private void performReusableMockTests(RowProcessingBeanMock bean) {
		assertTrue(bean.isClose1());
		assertTrue(bean.isClose2());
		assertTrue(bean.isInit1());
		assertTrue(bean.isInit2());
		assertTrue(bean.isResult1());
		assertTrue(bean.isResult2());
		assertEquals(CONFIGURED1_VALUE, bean.getConfigured1());
		assertEquals(CONFIGURED2_VALUE, bean.getConfigured2());
		List<Boolean> providedList = bean.getProvidedList();
		assertNotNull(providedList);
		assertTrue(providedList instanceof ProvidedList<?>);

		Map<String, Long> providedMap = bean.getProvidedMap();
		assertNotNull(providedMap);
		assertTrue(providedMap instanceof StoredMap);
	}

	private void performReusableMockTests(ExploringBeanMock bean) {
		assertTrue(bean.isClose1());
		assertTrue(bean.isClose2());
		assertTrue(bean.isInit1());
		assertTrue(bean.isInit2());
		assertTrue(bean.isResult());
		assertEquals(CONFIGURED1_VALUE, bean.getConfigured1());
		assertEquals(CONFIGURED2_VALUE, bean.getConfigured2());
		List<Boolean> providedList = bean.getProvidedList();
		assertNotNull(providedList);
		assertTrue(providedList instanceof ProvidedList<?>);

		Map<String, Long> providedMap = bean.getProvidedMap();
		assertNotNull(providedMap);
		assertTrue(providedMap instanceof StoredMap);

		assertEquals(1, bean.getRunCount());
	}

	private void setReusableMockProperties(SimpleAnalyzerJob job) {
		job.putStringProperty("Configured1", CONFIGURED1_VALUE);
		job.putIntegerProperty("Configured2", CONFIGURED2_VALUE);
	}
}
