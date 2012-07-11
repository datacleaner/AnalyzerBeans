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
package org.eobjects.analyzer.job;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.dategap.DateGapAnalyzer;
import org.eobjects.analyzer.beans.filter.NullCheckFilter;
import org.eobjects.analyzer.beans.filter.NullCheckFilter.NullCheckCategory;
import org.eobjects.analyzer.beans.filter.SingleWordFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.util.FileHelper;

public class JaxbJobWriterTest extends TestCase {

	// mock metadata factory used in this test case because we will otherwise
	// have time-dependent dates in the metadata which will make it difficult to
	// compare results
	private JaxbJobMetadataFactory _metadataFactory;
	private JaxbJobWriter _writer;

	protected void setUp() throws Exception {
		_metadataFactory = new JaxbJobMetadataFactory() {
			@Override
			public JobMetadataType create(AnalysisJob analysisJob)
					throws Exception {
				JobMetadataType jobMetadata = new JobMetadataType();
				jobMetadata.setAuthor("John Doe");
				jobMetadata.setJobVersion("2.0");
				jobMetadata
						.setCreatedDate(DatatypeFactory.newInstance()
								.newXMLGregorianCalendar(2010, 11, 12, 13, 48,
										0, 0, 0));
				return jobMetadata;
			}
		};
		_writer = new JaxbJobWriter(new AnalyzerBeansConfigurationImpl(),
				_metadataFactory);
	};

	@SuppressWarnings("unchecked")
	public void testNullColumnProperty() throws Exception {
		Datastore ds = TestHelper.createSampleDatabaseDatastore("db");
		AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl()
				.replace(new DatastoreCatalogImpl(ds));
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
		ajb.setDatastore(ds);

		DateGapAnalyzer dga = ajb.addAnalyzer(DateGapAnalyzer.class)
				.getConfigurableBean();
		Column orderDateColumn = ds.openConnection()
				.getSchemaNavigator()
				.convertToColumn("PUBLIC.ORDERS.ORDERDATE");
		Column shippedDateColumn = ds.openConnection()
				.getSchemaNavigator()
				.convertToColumn("PUBLIC.ORDERS.SHIPPEDDATE");

		ajb.addSourceColumns(orderDateColumn, shippedDateColumn);

		dga.setFromColumn((InputColumn<Date>) ajb
				.getSourceColumnByName("ORDERDATE"));
		dga.setToColumn((InputColumn<Date>) ajb
				.getSourceColumnByName("SHIPPEDDATE"));
		dga.setSingleDateOverlaps(true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		_writer.write(ajb.toAnalysisJob(), baos);

		String str = new String(baos.toByteArray());
		str = str.replaceAll("\"", "_");

		String[] lines = str.split("\n");
		assertEquals(27, lines.length);

		assertEquals("<?xml version=_1.0_ encoding=_UTF-8_ standalone=_yes_?>",
				lines[0]);
		assertEquals("<job xmlns=_http://eobjects.org/analyzerbeans/job/1.0_>",
				lines[1]);
		assertEquals("    <job-metadata>", lines[2]);
		assertEquals("        <job-version>2.0</job-version>", lines[3]);
		assertEquals("        <author>John Doe</author>", lines[4]);
		assertEquals("        <created-date>2010-11-12Z</created-date>",
				lines[5]);
		assertEquals("    </job-metadata>", lines[6]);
		assertEquals("    <source>", lines[7]);
		assertEquals("        <data-context ref=_db_/>", lines[8]);
		assertEquals("        <columns>", lines[9]);
		assertEquals(
				"            <column type=_TIMESTAMP_ path=_PUBLIC.ORDERS.ORDERDATE_ id=_col_0_/>",
				lines[10]);
		assertEquals(
				"            <column type=_TIMESTAMP_ path=_PUBLIC.ORDERS.SHIPPEDDATE_ id=_col_1_/>",
				lines[11]);
		assertEquals("        </columns>", lines[12]);
		assertEquals("    </source>", lines[13]);
		assertEquals("    <transformation/>", lines[14]);
		assertEquals("    <analysis>", lines[15]);
		assertEquals("        <analyzer>", lines[16]);
		assertEquals("            <descriptor ref=_Date gap analyzer_/>",
				lines[17]);
		assertEquals("            <properties>", lines[18]);
		assertEquals(
				"                <property value=_true_ name=_Count intersecting from and to dates as overlaps_/>",
				lines[19]);
		assertEquals(
				"                <property value=_true_ name=_Fault tolerant switch from/to dates_/>",
				lines[20]);
		assertEquals("            </properties>", lines[21]);
		assertEquals("            <input name=_From column_ ref=_col_0_/>",
				lines[22]);
		assertEquals("            <input name=_To column_ ref=_col_1_/>",
				lines[23]);
		assertEquals("        </analyzer>", lines[24]);
		assertEquals("    </analysis>", lines[25]);
		assertEquals("</job>", lines[26]);
	}

	public void testEmptyJobEnvelope() throws Exception {
		AnalysisJob job = EasyMock.createMock(AnalysisJob.class);
		Datastore ds = EasyMock.createMock(Datastore.class);

		EasyMock.expect(job.getDatastore()).andReturn(ds);

		EasyMock.expect(ds.getName()).andReturn("myds");

		EasyMock.expect(job.getSourceColumns()).andReturn(
				new ArrayList<InputColumn<?>>());
		EasyMock.expect(job.getTransformerJobs()).andReturn(
				new ArrayList<TransformerJob>());
		EasyMock.expect(job.getFilterJobs()).andReturn(
				new ArrayList<FilterJob>());
		EasyMock.expect(job.getMergedOutcomeJobs()).andReturn(
				new ArrayList<MergedOutcomeJob>());
		EasyMock.expect(job.getAnalyzerJobs()).andReturn(
				new ArrayList<AnalyzerJob>());
		EasyMock.expect(job.getExplorerJobs()).andReturn(
				new ArrayList<ExplorerJob>());

		EasyMock.replay(job, ds);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		_writer.write(job, baos);

		String str = new String(baos.toByteArray());
		str = str.replaceAll("\"", "_");
		String[] lines = str.split("\n");
		assertEquals(14, lines.length);

		assertEquals("<?xml version=_1.0_ encoding=_UTF-8_ standalone=_yes_?>",
				lines[0]);
		assertEquals("<job xmlns=_http://eobjects.org/analyzerbeans/job/1.0_>",
				lines[1]);
		assertEquals("    <job-metadata>", lines[2]);
		assertEquals("        <job-version>2.0</job-version>", lines[3]);
		assertEquals("        <author>John Doe</author>", lines[4]);
		assertEquals("        <created-date>2010-11-12Z</created-date>",
				lines[5]);
		assertEquals("    </job-metadata>", lines[6]);
		assertEquals("    <source>", lines[7]);
		assertEquals("        <data-context ref=_myds_/>", lines[8]);
		assertEquals("        <columns/>", lines[9]);
		assertEquals("    </source>", lines[10]);
		assertEquals("    <transformation/>", lines[11]);
		assertEquals("    <analysis/>", lines[12]);
		assertEquals("</job>", lines[13]);

		EasyMock.verify(job, ds);
	}

	public void testCompareWithBenchmarkFiles() throws Exception {
		Datastore datastore = TestHelper
				.createSampleDatabaseDatastore("my db");
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(
				new AnalyzerBeansConfigurationImpl()
						.replace(new DatastoreCatalogImpl(datastore)));

		ajb.setDatastore("my db");

		ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME",
				"PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

		InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
		InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
		InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");

		AnalyzerJobBuilder<StringAnalyzer> strAnalyzer = ajb
				.addAnalyzer(StringAnalyzer.class);
		strAnalyzer.addInputColumns(fnCol, lnCol);

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file1.xml");

		TransformerJobBuilder<EmailStandardizerTransformer> tjb = ajb
				.addTransformer(EmailStandardizerTransformer.class);
		tjb.addInputColumn(emailCol);
		strAnalyzer.addInputColumns(tjb.getOutputColumns());

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file2.xml");

		FilterJobBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> fjb1 = ajb
				.addFilter(NullCheckFilter.class);
		fjb1.addInputColumn(fnCol);
		strAnalyzer.setRequirement(fjb1, "NOT_NULL");

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file3.xml");

		AnalyzerJobBuilder<PatternFinderAnalyzer> patternFinder1 = ajb
				.addAnalyzer(PatternFinderAnalyzer.class);
		makeCrossPlatformCompatible(patternFinder1);
		MutableInputColumn<?> usernameColumn = tjb
				.getOutputColumnByName("Username");
		patternFinder1.addInputColumn(fnCol).addInputColumn(usernameColumn)
				.getConfigurableBean().setEnableMixedTokens(false);

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file4.xml");

		FilterJobBuilder<SingleWordFilter, ValidationCategory> fjb2 = ajb
				.addFilter(SingleWordFilter.class);
		fjb2.addInputColumn(usernameColumn);

		MergedOutcomeJobBuilder mergedOutcome = ajb
				.addMergedOutcomeJobBuilder();
		mergedOutcome.addMergedOutcome(fjb1, NullCheckCategory.NULL)
				.addInputColumn(fnCol);
		mergedOutcome.addMergedOutcome(fjb2, ValidationCategory.INVALID)
				.addInputColumn(usernameColumn);
		MutableInputColumn<?> mergedColumn = mergedOutcome.getOutputColumns()
				.get(0);
		mergedColumn.setName("Merged output column (fn or username)");

		AnalyzerJobBuilder<PatternFinderAnalyzer> patternFinder2 = ajb
				.addAnalyzer(PatternFinderAnalyzer.class);
		makeCrossPlatformCompatible(patternFinder2);
		patternFinder2.addInputColumn(mergedColumn);

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file5.xml");

		mergedOutcome.setName("merge1");
		tjb.setName("trans1");
		fjb1.setName("fjb1");
		fjb2.setName("fjb2");
		patternFinder1.setName("pf 1");
		patternFinder2.setName("pf 2");

		assertMatchesBenchmark(ajb.toAnalysisJob(),
				"JaxbJobWriterTest-file6.xml");
	}

	/**
	 * Helper method to make sure that some of the locale-dependent settings of
	 * the pattern finder are standardized in order to make the test
	 * cross-platform compatible.
	 * 
	 * @param pfb
	 */
	private void makeCrossPlatformCompatible(
			AnalyzerJobBuilder<PatternFinderAnalyzer> pfb) {
		PatternFinderAnalyzer pf = pfb.getConfigurableBean();
		pf.setDecimalSeparator('.');
		pf.setMinusSign('-');
		pf.setThousandsSeparator(',');
	}

	private void assertMatchesBenchmark(AnalysisJob analysisJob, String filename)
			throws Exception {
		final File outputFolder = new File("target/test-output/");
		if (!outputFolder.exists()) {
			assertTrue("Could not create output folder!", outputFolder.mkdirs());
		}

		final File benchmarkFolder = new File("src/test/resources/");

		File outputFile = new File(outputFolder, filename);

		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(outputFile));
		_writer.write(analysisJob, bos);
		bos.flush();
		bos.close();
		String output = FileHelper.readFileAsString(outputFile);

		File benchmarkFile = new File(benchmarkFolder, filename);
		if (!benchmarkFile.exists()) {
			assertEquals("No benchmark file '" + filename + "' exists!", output);
		}
		String benchmark = FileHelper.readFileAsString(benchmarkFile);
		assertEquals(benchmark, output);
	}
}
