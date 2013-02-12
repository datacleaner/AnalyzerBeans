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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisJobFailedException;
import org.eobjects.analyzer.job.runner.JobStatus;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * {@link AnalysisResultFuture} implementation for clustered/distributed
 * set-ups.
 */
public final class DistributedAnalysisResultFuture implements AnalysisResultFuture {

    private final List<AnalysisResultFuture> _results;
    private volatile Date _creationDate;
    private final Map<ComponentJob, AnalyzerResult> _resultMap;

    public DistributedAnalysisResultFuture(List<AnalysisResultFuture> results) {
        _results = results;
        _resultMap = new HashMap<ComponentJob, AnalyzerResult>();
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Date getCreationDate() {
        if (!isDone()) {
            return null;
        }
        return _creationDate;
    }

    @Override
    public boolean isDone() {
        for (AnalysisResultFuture result : _results) {
            if (!result.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void await() {
        for (AnalysisResultFuture result : _results) {
            result.await();
        }
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit) {
        // TODO: Timeout not yet implemented
        await();
    }

    @Override
    public boolean isErrornous() {
        return !isSuccessful();
    }

    @Override
    public boolean isSuccessful() {
        await();
        for (AnalysisResultFuture result : _results) {
            if (result.isErrornous()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JobStatus getStatus() {
        JobStatus status = JobStatus.SUCCESSFUL;
        for (AnalysisResultFuture result : _results) {
            JobStatus slaveStatus = result.getStatus();
            switch (slaveStatus) {
            case ERRORNOUS:
                return JobStatus.ERRORNOUS;
            case NOT_FINISHED:
                status = JobStatus.NOT_FINISHED;
                break;
            }
        }

        return status;
    }

    @Override
    public List<AnalyzerResult> getResults() throws AnalysisJobFailedException {
        await();
        if (isErrornous()) {
            throw new AnalysisJobFailedException(getErrors());
        }

        final Collection<AnalyzerResult> values = _resultMap.values();
        return new ArrayList<AnalyzerResult>(values);
    }

    @Override
    public AnalyzerResult getResult(ComponentJob componentJob) throws AnalysisJobFailedException {
        await();
        if (isErrornous()) {
            throw new AnalysisJobFailedException(getErrors());
        }

        return _resultMap.get(componentJob);
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() throws AnalysisJobFailedException {
        await();
        if (isErrornous()) {
            throw new AnalysisJobFailedException(getErrors());
        }

        return Collections.unmodifiableMap(_resultMap);
    }

    @Override
    public List<Throwable> getErrors() {
        final List<Throwable> errors = new ArrayList<Throwable>();
        for (AnalysisResultFuture result : _results) {
            final List<Throwable> slaveErrors = result.getErrors();
            if (slaveErrors != null) {
                errors.addAll(slaveErrors);
            }
        }
        return errors;
    }

}
