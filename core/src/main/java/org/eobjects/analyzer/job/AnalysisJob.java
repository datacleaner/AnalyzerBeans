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

import java.util.Collection;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.metamodel.schema.Column;

/**
 * Defines a job to be executed by AnalyzerBeans.
 * 
 * A {@link AnalysisJob} contains a set of components.
 * 
 * A {@link AnalysisJob} references a source {@link Datastore} and some
 * {@link Column}s (represented via {@link InputColumn}s) of this datastore.
 * 
 * Building jobs is usually done using the {@link AnalysisJobBuilder} class.
 * 
 * Executing jobs is usually done using the {@link AnalysisRunner} interface.
 */
public interface AnalysisJob {

    /**
     * Gets the {@link Datastore} that this job uses as it's source.
     * 
     * @return
     */
    public Datastore getDatastore();

    /**
     * Gets the source columns of the {@link Datastore} (see
     * {@link #getDatastore()}) referenced by this job.
     * 
     * @return
     */
    public Collection<InputColumn<?>> getSourceColumns();

    /**
     * Gets all {@link TransformerJob}s contained in this job.
     * 
     * @return
     */
    public Collection<TransformerJob> getTransformerJobs();

    /**
     * Gets all {@link FilterJob}s contained in this job.
     * 
     * @return
     */
    public Collection<FilterJob> getFilterJobs();

    /**
     * Gets all {@link MergedOutcomeJob}s contained in this job.
     * 
     * @return
     */
    public Collection<MergedOutcomeJob> getMergedOutcomeJobs();

    /**
     * Gets all {@link AnalyzerJob}s contained in this job.
     * 
     * @return
     */
    public Collection<AnalyzerJob> getAnalyzerJobs();

    /**
     * Gets all {@link ExplorerJob}s contained in this job.
     * 
     * @return
     */
    public Collection<ExplorerJob> getExplorerJobs();
}