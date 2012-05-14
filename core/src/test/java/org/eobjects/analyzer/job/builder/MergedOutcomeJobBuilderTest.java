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

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ImmutableFilterOutcome;
import org.eobjects.analyzer.job.MergedOutcome;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.result.renderer.PatternFinderResultTextRenderer;
import org.eobjects.analyzer.test.TestHelper;

public class MergedOutcomeJobBuilderTest extends TestCase {

    public void testSimpleBuildNoColumnMerge() throws Exception {
        JdbcDatastore ds = TestHelper.createSampleDatabaseDatastore("mydb");
        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
                new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(ds)));

        analysisJobBuilder.setDatastore("mydb");
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.REPORTSTO");
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME");

        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb1 = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);

        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb2 = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);

        MergedOutcomeJobBuilder mergedOutcomeJobBuilder = analysisJobBuilder.addMergedOutcomeJobBuilder();

        try {
            mergedOutcomeJobBuilder.toMergedOutcomeJob();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            assertEquals("Merged outcome jobs need at least 2 merged outcomes, none found", e.getMessage());
        }

        mergedOutcomeJobBuilder.addMergedOutcome(fjb1, MaxRowsFilter.Category.VALID);

        try {
            mergedOutcomeJobBuilder.toMergedOutcomeJob();
            fail("Exception expected");
        } catch (IllegalStateException e) {
            assertEquals("Merged outcome jobs need at least 2 merged outcomes, only 1 found", e.getMessage());
        }

        mergedOutcomeJobBuilder.addMergedOutcome(fjb2, MaxRowsFilter.Category.VALID);

        MergedOutcomeJob mergedOutcomeJob = mergedOutcomeJobBuilder.toMergedOutcomeJob();

        assertEquals(2, mergedOutcomeJob.getMergeInputs().length);
        assertEquals(0, mergedOutcomeJob.getOutput().length);

        MergedOutcome outcome = mergedOutcomeJob.getOutcome();

        assertTrue(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb1.toFilterJob(),
                MaxRowsFilter.Category.VALID)));
        assertFalse(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb1.toFilterJob(),
                MaxRowsFilter.Category.INVALID)));
        assertTrue(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb2.toFilterJob(),
                MaxRowsFilter.Category.VALID)));
        assertFalse(outcome.satisfiesRequirement(new ImmutableFilterOutcome(fjb2.toFilterJob(),
                MaxRowsFilter.Category.INVALID)));

        AnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder.addAnalyzer(StringAnalyzer.class);
        ajb.setRequirement(outcome);
    }

    public void testRunAnalysis() throws Throwable {
        CsvDatastore ds = new CsvDatastore("mydb", "src/test/resources/employees-missing-values.csv");
        AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(ds));
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);

        ajb.setDatastore("mydb");
        ajb.addSourceColumns("name");
        ajb.addSourceColumns("email");

        InputColumn<?> fnCol = ajb.getSourceColumnByName("name");
        assertNotNull(fnCol);
        InputColumn<?> emailCol = ajb.getSourceColumnByName("email");
        assertNotNull(emailCol);

        TransformerJobBuilder<EmailStandardizerTransformer> t = ajb.addTransformer(EmailStandardizerTransformer.class);
        t.addInputColumn(emailCol);

        MutableInputColumn<?> usernameCol = t.getOutputColumnByName("Username");
        assertNotNull(usernameCol);

        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> f = ajb.addFilter(MaxRowsFilter.class);
        f.setConfiguredProperty("Max rows", 3);

        MergedOutcomeJobBuilder mojb = ajb.addMergedOutcomeJobBuilder();
        mojb.addMergedOutcome(f, MaxRowsFilter.Category.VALID).addInputColumn(usernameCol);
        mojb.addMergedOutcome(f, MaxRowsFilter.Category.INVALID).addInputColumn(fnCol);

        MergedOutcomeJob mergedOutcomeJob = mojb.toMergedOutcomeJob();
        assertNotNull(mergedOutcomeJob);

        InputColumn<?>[] output = mergedOutcomeJob.getOutput();
        assertEquals(1, output.length);
        InputColumn<?> outputColumn = output[0];
        assertTrue(outputColumn.isVirtualColumn());
        assertEquals("Merged column 1", outputColumn.getName());

        AnalyzerJobBuilder<PatternFinderAnalyzer> a = ajb.addAnalyzer(PatternFinderAnalyzer.class);
        a.addInputColumn(outputColumn);

        AnalysisJob analysisJob = ajb.toAnalysisJob();

        AnalysisRunnerImpl runner = new AnalysisRunnerImpl(conf);

        AnalysisResultFuture resultFuture = runner.run(analysisJob);

        if (resultFuture.isErrornous()) {
            List<Throwable> errors = resultFuture.getErrors();
            for (Throwable throwable : errors) {
                throwable.printStackTrace();
            }
            throw errors.get(0);
        }

        List<AnalyzerResult> results = resultFuture.getResults();
        assertEquals(1, results.size());

        PatternFinderResult result = (PatternFinderResult) results.get(0);
        String[] resultLines = new PatternFinderResultTextRenderer().render(result).split("\n");
        assertEquals("                 Match count Sample      ", resultLines[0]);
        assertEquals("aaaa.aaa                   3 john.doe    ", resultLines[1]);
        assertEquals("Aaa. Aaaaaa Aaa            1 Mrs. Foobar Foo ", resultLines[2]);
        assertEquals("Aaaaaaa Aaaaa              1 Asbjørn Leeth ", resultLines[3]);
        assertEquals("Aaaaaaaa, Aaaaaa           1 Sørensen, Kasper ", resultLines[4]);
        assertEquals(5, resultLines.length);
    }
}
