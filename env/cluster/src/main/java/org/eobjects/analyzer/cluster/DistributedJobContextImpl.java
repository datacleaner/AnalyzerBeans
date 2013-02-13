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

import org.eobjects.analyzer.job.AnalysisJob;

/**
 * Simple {@link DistributedJobContext} implementation
 */
final class DistributedJobContextImpl implements DistributedJobContext {

    private final AnalysisJob _masterJob;
    private final int _chunkIndex;
    private final int _chunkCount;

    public DistributedJobContextImpl(AnalysisJob masterJob, int chunkIndex, int chunkCount) {
        _masterJob = masterJob;
        _chunkIndex = chunkIndex;
        _chunkCount = chunkCount;
    }

    @Override
    public AnalysisJob getMasterJob() {
        return _masterJob;
    }

    @Override
    public int getJobDivisionCount() {
        return _chunkCount;
    }

    @Override
    public int getJobDivisionIndex() {
        return _chunkIndex;
    }

}
