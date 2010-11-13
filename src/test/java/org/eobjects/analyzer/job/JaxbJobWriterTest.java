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

import javax.xml.datatype.DatatypeFactory;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.filter.NotNullFilter;
import org.eobjects.analyzer.beans.filter.SingleWordFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.util.FileHelper;

public class JaxbJobWriterTest extends TestCase {

	// mock metadata factory used in this test case because we will otherwise
	// have time-dependent dates in the metadata which will make it difficult to
	// compare results
	private JaxbJobMetadataFactory _metadataFactory;
	private JaxbJobWriter _writer = new JaxbJobWriter();

	protected void setUp() throws Exception {
		_metadataFactory = new JaxbJobMetadataFactory() {
			@Override
			public JobMetadataType create(AnalysisJob analysisJob) throws Exception {
				JobMetadataType jobMetadata = new JobMetadataType();
				jobMetadata.setAuthor("John Doe");
				jobMetadata.setJobVersion("2.0");
				jobMetadata.setCreatedDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(2010, 11, 12, 13, 48, 0, 0,
						0));
				return jobMetadata;
			}
		};
		_writer.setJobMetadataFactory(_metadataFactory);
	};

	public void testEmptyJobEnvelope() throws Exception {
		AnalysisJob job = EasyMock.createMock(AnalysisJob.class);
		DataContextProvider dcp = EasyMock.createMock(DataContextProvider.class);

		EasyMock.expect(job.getDataContextProvider()).andReturn(dcp);
		EasyMock.expect(job.getSourceColumns()).andReturn(new ArrayList<InputColumn<?>>());
		EasyMock.expect(job.getTransformerJobs()).andReturn(new ArrayList<TransformerJob>());
		EasyMock.expect(job.getFilterJobs()).andReturn(new ArrayList<FilterJob>());
		EasyMock.expect(job.getMergedOutcomeJobs()).andReturn(new ArrayList<MergedOutcomeJob>());
		EasyMock.expect(job.getAnalyzerJobs()).andReturn(new ArrayList<AnalyzerJob>());

		EasyMock.replay(job);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		_writer.write(job, baos);

		String str = new String(baos.toByteArray());
		str = str.replaceAll("\"", "_");
		String[] lines = str.split("\n");
		assertEquals(13, lines.length);

		assertEquals("<?xml version=_1.0_ encoding=_UTF-8_ standalone=_yes_?>", lines[0]);
		assertEquals("<job xmlns=_http://eobjects.org/analyzerbeans/job/1.0_>", lines[1]);
		assertEquals("    <job-metadata>", lines[2]);
		assertEquals("        <job-version>2.0</job-version>", lines[3]);
		assertEquals("        <author>John Doe</author>", lines[4]);
		assertEquals("        <created-date>2010-11-12Z</created-date>", lines[5]);
		assertEquals("    </job-metadata>", lines[6]);
		assertEquals("    <source>", lines[7]);
		assertEquals("        <columns/>", lines[8]);
		assertEquals("    </source>", lines[9]);
		assertEquals("    <transformation/>", lines[10]);
		assertEquals("    <analysis/>", lines[11]);
		assertEquals("</job>", lines[12]);

		EasyMock.verify(job);
	}

	public void testCompareWithBenchmarkFiles() throws Exception {
		JdbcDatastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(TestHelper.createAnalyzerBeansConfiguration(datastore));

		ajb.setDatastore("my db");

		ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

		InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
		InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
		InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> strAnalyzer = ajb.addRowProcessingAnalyzer(StringAnalyzer.class);
		strAnalyzer.addInputColumns(fnCol, lnCol);

		assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file1.xml");

		TransformerJobBuilder<EmailStandardizerTransformer> tjb = ajb.addTransformer(EmailStandardizerTransformer.class);
		tjb.addInputColumn(emailCol);
		strAnalyzer.addInputColumns(tjb.getOutputColumns());

		assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file2.xml");

		FilterJobBuilder<NotNullFilter, ValidationCategory> fjb1 = ajb.addFilter(NotNullFilter.class);
		fjb1.addInputColumn(fnCol);
		strAnalyzer.setRequirement(fjb1, "VALID");

		assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file3.xml");

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> patternFinder1 = ajb
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		MutableInputColumn<?> usernameColumn = tjb.getOutputColumnByName("Username");
		patternFinder1.addInputColumn(fnCol).addInputColumn(usernameColumn).getConfigurableBean()
				.setEnableMixedTokens(false);

		assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file4.xml");

		FilterJobBuilder<SingleWordFilter, ValidationCategory> fjb2 = ajb.addFilter(SingleWordFilter.class);
		fjb2.addInputColumn(usernameColumn);

		MergedOutcomeJobBuilder mergedOutcome = ajb.addMergedOutcomeJobBuilder();
		mergedOutcome.addMergedOutcome(fjb1, ValidationCategory.INVALID).addInputColumn(fnCol);
		mergedOutcome.addMergedOutcome(fjb2, ValidationCategory.INVALID).addInputColumn(usernameColumn);
		MutableInputColumn<?> mergedColumn = mergedOutcome.getOutputColumns().get(0);
		mergedColumn.setName("Merged output column (fn or username)");

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> patternFinder2 = ajb
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		patternFinder2.addInputColumn(mergedColumn);

		assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file5.xml");
	}

	private void assertMatchesBenchmark(AnalysisJob analysisJob, String filename) throws Exception {
		final File outputFolder = new File("target/test-output/");
		if (!outputFolder.exists()) {
			assertTrue("Could not create output folder!", outputFolder.mkdirs());
		}

		final File benchmarkFolder = new File("src/test/resources/");

		File outputFile = new File(outputFolder, filename);

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
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
