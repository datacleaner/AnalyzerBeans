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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.NotNullFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class AnalysisJobBuilderTest extends TestCase {

	private AnalysisJobBuilder analysisJobBuilder;
	private AnalyzerBeansConfigurationImpl configuration;
	private JdbcDatastore datastore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Collection<Datastore> datastores = new LinkedList<Datastore>();

		datastore = TestHelper.createSampleDatabaseDatastore("my db");
		datastores.add(datastore);

		configuration = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(datastores));

		analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDatastore("my db");
	}

	public void testToString() throws Exception {
		RowProcessingAnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		TransformerJobBuilder<ConvertToStringTransformer> tjb = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		assertEquals("RowProcessingAnalyzerJobBuilder[analyzer=String analyzer,inputColumns=[]]", ajb.toString());
		assertEquals("TransformerJobBuilder[transformer=Convert to string,inputColumns=[]]", tjb.toString());
	}

	public void testToAnalysisJob() throws Exception {
		Table employeeTable = datastore.getDataContextProvider().getDataContext().getDefaultSchema()
				.getTableByName("EMPLOYEES");
		assertNotNull(employeeTable);

		Column emailColumn = employeeTable.getColumnByName("EMAIL");
		analysisJobBuilder.addSourceColumns(employeeTable.getColumnByName("EMPLOYEENUMBER"),
				employeeTable.getColumnByName("FIRSTNAME"), emailColumn);

		assertTrue(analysisJobBuilder.containsSourceColumn(emailColumn));
		assertFalse(analysisJobBuilder.containsSourceColumn(null));
		assertFalse(analysisJobBuilder.containsSourceColumn(employeeTable.getColumnByName("LASTNAME")));

		TransformerJobBuilder<ConvertToStringTransformer> transformerJobBuilder = analysisJobBuilder
				.addTransformer(ConvertToStringTransformer.class);

		Collection<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.NUMBER);
		assertEquals(1, numberColumns.size());
		assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]", Arrays.toString(numberColumns.toArray()));

		transformerJobBuilder.addInputColumn(numberColumns.iterator().next());
		assertTrue(transformerJobBuilder.isConfigured());

		// the AnalyzerJob has no Analyzers yet, so it is not "configured".
		assertFalse(analysisJobBuilder.isConfigured());

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> analyzerJobBuilder = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);

		List<InputColumn<?>> stringInputColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.STRING);
		Set<String> columnNames = new TreeSet<String>();
		for (InputColumn<?> inputColumn : stringInputColumns) {
			columnNames.add(inputColumn.getName());
		}
		assertEquals("[EMAIL, EMPLOYEENUMBER (as string), FIRSTNAME]", columnNames.toString());

		analyzerJobBuilder.addInputColumns(stringInputColumns);
		assertTrue(analyzerJobBuilder.isConfigured());

		// now there is: source columns, configured analyzers and configured
		// transformers.
		assertTrue(analysisJobBuilder.isConfigured());

		AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();

		assertEquals(
				"ImmutableAnalysisJob[sourceColumns=3,filterJobs=0,transformerJobs=1,analyzerJobs=1,mergedOutcomeJobs=0]",
				analysisJob.toString());

		// test hashcode and equals
		assertNotSame(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob(), analysisJob);
		assertEquals(analysisJobBuilder.toAnalysisJob().hashCode(), analysisJob.hashCode());

		Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
		assertEquals(3, sourceColumns.size());

		try {
			sourceColumns.add(new MockInputColumn<Boolean>("bla", Boolean.class));
			fail("Exception expected");
		} catch (UnsupportedOperationException e) {
			// do nothing
		}

		Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
		assertEquals(1, transformerJobs.size());

		TransformerJob transformerJob = transformerJobs.iterator().next();
		assertEquals("ImmutableTransformerJob[name=null,transformer=Convert to string]", transformerJob.toString());

		assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]", Arrays.toString(transformerJob.getInput()));

		Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
		assertEquals(1, analyzerJobs.size());

		AnalyzerJob analyzerJob = analyzerJobs.iterator().next();
		assertEquals("ImmutableAnalyzerJob[name=null,analyzer=String analyzer]", analyzerJob.toString());
	}

	public void testGetAvailableUnfilteredBeans() throws Exception {
		Table customersTable = datastore.getDataContextProvider().getDataContext().getDefaultSchema()
				.getTableByName("CUSTOMERS");
		assertNotNull(customersTable);

		analysisJobBuilder.addSourceColumns(customersTable.getColumnByName("ADDRESSLINE1"),
				customersTable.getColumnByName("ADDRESSLINE2"));

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> saAjb = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		saAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

		FilterJobBuilder<NotNullFilter, ValidationCategory> fjb = analysisJobBuilder.addFilter(NotNullFilter.class);
		fjb.addInputColumn(analysisJobBuilder.getSourceColumns().get(0));

		List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(1, result.size());
		assertEquals(result.get(0), saAjb);

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> pfAjb = analysisJobBuilder
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		pfAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

		result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(2, result.size());
		assertEquals(result.get(0), saAjb);
		assertEquals(result.get(1), pfAjb);

		pfAjb.setRequirement(fjb, ValidationCategory.VALID);

		result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
		assertEquals(1, result.size());
		assertEquals(result.get(0), saAjb);
	}

	public void testRemoveFilter() throws Exception {
		DataContextProvider dcp = datastore.getDataContextProvider();

		FilterJobBuilder<MaxRowsFilter, ValidationCategory> maxRowsFilter = analysisJobBuilder
				.addFilter(MaxRowsFilter.class);
		analysisJobBuilder.setDefaultRequirement(maxRowsFilter, ValidationCategory.VALID);

		TransformerJobBuilder<EmailStandardizerTransformer> emailStdTransformer = analysisJobBuilder
				.addTransformer(EmailStandardizerTransformer.class);
		assertSame(maxRowsFilter.getOutcome(ValidationCategory.VALID), emailStdTransformer.getRequirement());

		FilterJobBuilder<NotNullFilter, ValidationCategory> notNullFilter = analysisJobBuilder
				.addFilter(NotNullFilter.class);
		notNullFilter.setRequirement(null);
		maxRowsFilter.setRequirement(notNullFilter.getOutcome(ValidationCategory.VALID));

		assertNull(notNullFilter.getRequirement());

		analysisJobBuilder.addSourceColumn(dcp.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL"));
		emailStdTransformer.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> stringAnalyzer = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		stringAnalyzer.addInputColumns(emailStdTransformer.getOutputColumns());

		assertSame(maxRowsFilter.getOutcome(ValidationCategory.VALID), stringAnalyzer.getRequirement());

		analysisJobBuilder.removeFilter(maxRowsFilter);

		assertNull(analysisJobBuilder.getDefaultRequirement());
		assertSame(notNullFilter.getOutcome(ValidationCategory.VALID), stringAnalyzer.getRequirement());
		assertSame(notNullFilter.getOutcome(ValidationCategory.VALID), emailStdTransformer.getRequirement());

		dcp.close();
	}

	public void testSourceColumnListeners() throws Exception {
		JdbcDatastore datastore = TestHelper.createSampleDatabaseDatastore("mydb");
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
		ajb.setDatastore(datastore);

		SourceColumnChangeListener listener1 = EasyMock.createMock(SourceColumnChangeListener.class);
		ajb.getSourceColumnListeners().add(listener1);

		Column column = ajb.getDataContextProvider().getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);

		// scene 1: add source column
		listener1.onAdd(inputColumn);
		listener1.onRemove(inputColumn);
		listener1.onAdd(inputColumn);

		EasyMock.replay(listener1);

		ajb.addSourceColumns(inputColumn);
		ajb.removeSourceColumn(column);
		ajb.addSourceColumn(inputColumn);

		EasyMock.verify(listener1);
		EasyMock.reset(listener1);

		// scene 2: add transformer
		TransformerChangeListener listener2 = EasyMock.createMock(TransformerChangeListener.class);
		ajb.getTransformerChangeListeners().add(listener2);

		final TransformerBeanDescriptor<EmailStandardizerTransformer> descriptor = Descriptors
				.ofTransformer(EmailStandardizerTransformer.class);
		IArgumentMatcher tjbMatcher = new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) argument;
				return tjb.getDescriptor() == descriptor;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("transformer job builder");
			}
		};
		EasyMock.reportMatcher(tjbMatcher);
		listener2.onAdd(null);

		// output updated
		EasyMock.reportMatcher(tjbMatcher);
		EasyMock.reportMatcher(new IArgumentMatcher() {

			@Override
			public boolean matches(Object argument) {
				@SuppressWarnings("unchecked")
				List<MutableInputColumn<?>> list = (List<MutableInputColumn<?>>) argument;
				if (list.size() == 2) {
					if (list.get(0).getName().equals("Username")) {
						if (list.get(1).getName().equals("Domain")) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("list of output columns");
			}
		});
		listener2.onOutputChanged(null, null);

		// configuration updated
		EasyMock.reportMatcher(tjbMatcher);
		listener2.onConfigurationChanged(null);

		// remove transformer
		EasyMock.reportMatcher(tjbMatcher);
		EasyMock.reportMatcher(new IArgumentMatcher() {

			@Override
			public boolean matches(Object argument) {
				@SuppressWarnings("unchecked")
				List<MutableInputColumn<?>> list = (List<MutableInputColumn<?>>) argument;
				return list.isEmpty();
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append("empty list of output columns");
			}
		});
		listener2.onOutputChanged(null, null);

		EasyMock.reportMatcher(tjbMatcher);
		listener2.onRemove(null);

		listener1.onRemove(inputColumn);

		EasyMock.replay(listener1, listener2);

		ajb.addTransformer(descriptor).addInputColumn(inputColumn);
		ajb.reset();

		EasyMock.verify(listener1, listener2);
	}
}
