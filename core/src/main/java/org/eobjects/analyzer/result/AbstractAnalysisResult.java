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
package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Abstract/base implementation of {@link AnalysisResult}
 */
public abstract class AbstractAnalysisResult implements AnalysisResult {

    @SuppressWarnings("unchecked")
    @Override
    public <R extends AnalyzerResult> List<? extends R> getResults(Class<R> resultClass) {
        final List<R> list = new ArrayList<R>();

        final List<AnalyzerResult> results = getResults();
        for (AnalyzerResult analyzerResult : results) {
            if (ReflectionUtils.is(analyzerResult.getClass(), resultClass)) {
                list.add((R) analyzerResult);
            }
        }
        return list;
    }
    
    @Override
    public AnalyzerResult getResult(ComponentJob componentJob) {
        AnalyzerResult analyzerResult = getResultMap().get(componentJob);
        return analyzerResult;
    }
}
