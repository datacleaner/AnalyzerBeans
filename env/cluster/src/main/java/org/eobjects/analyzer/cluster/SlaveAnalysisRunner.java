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
package org.eobjects.analyzer.cluster;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;

/**
 * An {@link AnalysisRunner} which is appropriate for use on slave nodes, since
 * it will honor non-distributed tasks like {@link Initialize} and {@link Close}
 * methods that are marked with distributed=false.
 */
public class SlaveAnalysisRunner extends AnalysisRunnerImpl {

    public SlaveAnalysisRunner(AnalyzerBeansConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected boolean isNonDistributedTasksIncluded() {
        return false;
    }
}
