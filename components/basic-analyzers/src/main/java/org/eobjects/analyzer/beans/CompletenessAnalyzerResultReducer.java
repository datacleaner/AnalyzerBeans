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
package org.eobjects.analyzer.beans;

import java.util.Collection;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

public class CompletenessAnalyzerResultReducer implements AnalyzerResultReducer<CompletenessAnalyzerResult> {

    @Override
    public CompletenessAnalyzerResult reduce(Collection<? extends CompletenessAnalyzerResult> results) {
        final CompletenessAnalyzerResult firstResult = results.iterator().next();

        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();
        final RowAnnotation annotation = annotationFactory.createAnnotation();
        final InputColumn<?>[] highlightedColumns = firstResult.getHighlightedColumns();

        int totalRowCount = 0;
        for (CompletenessAnalyzerResult result : results) {
            final InputRow[] rows = result.getRows();
            final int invalidRowCount = result.getInvalidRowCount();
            if (invalidRowCount == rows.length) {
                // if the rows are included for preview/sampling - then
                // re-annotate them in the master result
                annotationFactory.annotate(rows, annotation);
            } else {
                // else we just transfer annotation counts
                annotationFactory.transferAnnotations(result.getAnnotation(), annotation);
            }

            totalRowCount += result.getTotalRowCount();
        }

        return new CompletenessAnalyzerResult(totalRowCount, annotation, annotationFactory, highlightedColumns);
    }

}
