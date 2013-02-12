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
package org.eobjects.analyzer.cluster.virtual;

import org.eobjects.analyzer.cluster.ClusterNode;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;

/**
 * A virtual cluster node
 * 
 * @see VirtualClusterManager
 */
final class VirtualClusterNode implements ClusterNode {

    private final String _name;
    private final AnalysisRunner _analysisRunner;

    public VirtualClusterNode(String name, AnalyzerBeansConfiguration configuration) {
        _name = name;
        _analysisRunner = new AnalysisRunnerImpl(configuration);
    }

    public AnalysisRunner getAnalysisRunner() {
        return _analysisRunner;
    }

    @Override
    public String getName() {
        return _name;
    }

}
