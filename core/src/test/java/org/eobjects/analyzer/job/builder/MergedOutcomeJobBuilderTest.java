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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.test.MockAnalyzer;
import org.eobjects.analyzer.test.MockFilter;
import org.eobjects.analyzer.test.MockFilter.Category;

import junit.framework.TestCase;

public class MergedOutcomeJobBuilderTest extends TestCase {

    public void testScenario() throws Exception {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());

        ajb.setDatastore(new CsvDatastore("ds", "src/test/resources/employees.csv"));
        ajb.addSourceColumns("name", "email");

        FilterJobBuilder<MockFilter, Category> filter1 = ajb.addFilter(MockFilter.class);
        filter1.setConfiguredProperty("Some enum", MockFilter.Category.VALID);
        filter1.setConfiguredProperty("Some file", new File("."));
        filter1.addInputColumn(ajb.getSourceColumnByName("name"));

        FilterJobBuilder<MockFilter, Category> filter2 = ajb.addFilter(MockFilter.class);
        filter2.setConfiguredProperty("Some enum", MockFilter.Category.INVALID);
        filter2.setConfiguredProperty("Some file", new File("."));
        filter2.addInputColumn(ajb.getSourceColumnByName("email"));

        MergedOutcomeJobBuilder merge = ajb.addMergedOutcomeJobBuilder();
        merge.addMergedOutcome(filter1.getOutcome(MockFilter.Category.VALID)).addInputColumn(filter1.getInput()[0]);
        merge.addMergedOutcome(filter2.getOutcome(MockFilter.Category.INVALID)).addInputColumn(filter2.getInput()[0]);

        AnalyzerJobBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
        analyzer.setRequirement(merge.getOutcomes()[0]);
        analyzer.addInputColumn(merge.getOutputColumns().get(0));

        AnalysisJob job = ajb.toAnalysisJob();
        ajb.close();

        assertEquals(1, job.getMergedOutcomeJobs().size());
        MergedOutcomeJob mergedOutcomeJob = job.getMergedOutcomeJobs().iterator().next();
        assertEquals(
                "[ImmutableMergeInput[FilterOutcome[category=VALID]], ImmutableMergeInput[FilterOutcome[category=INVALID]]]",
                Arrays.toString(mergedOutcomeJob.getMergeInputs()));
        assertEquals("[FilterOutcome[category=VALID], FilterOutcome[category=INVALID]]",
                Arrays.toString(mergedOutcomeJob.getRequirements()));
        assertEquals("[MergedOutcome[output=[TransformedInputColumn[id=merged-0001,name=Merged column 1]]]]",
                Arrays.toString(mergedOutcomeJob.getOutcomes()));
        assertEquals("[TransformedInputColumn[id=merged-0001,name=Merged column 1]]",
                Arrays.toString(mergedOutcomeJob.getOutput()));
        
        ArrayList<FilterJob> filters = new ArrayList<FilterJob>(job.getFilterJobs());
        assertEquals(2, filters.size());
        
        assertTrue(mergedOutcomeJob.getOutcome().satisfiesRequirement(filters.get(0).getOutcomes()[0]));
        assertFalse(mergedOutcomeJob.getOutcome().satisfiesRequirement(filters.get(0).getOutcomes()[1]));
        assertFalse(mergedOutcomeJob.getOutcome().satisfiesRequirement(filters.get(1).getOutcomes()[0]));
        assertTrue(mergedOutcomeJob.getOutcome().satisfiesRequirement(filters.get(1).getOutcomes()[1]));
    }
}
