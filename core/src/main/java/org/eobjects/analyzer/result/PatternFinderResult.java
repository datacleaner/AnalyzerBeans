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

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.data.InputColumn;

/**
 * Represents the result of the {@link PatternFinderAnalyzer}.
 * 
 * A pattern finder result has two basic forms: Grouped or ungrouped. To find
 * out which type a particular instance has, use the
 * {@link #isGroupingEnabled()} method.
 * 
 * Ungrouped results only contain a single/global crosstab. A grouped result
 * contain multiple crosstabs, based on groups.
 * 
 * @author Kasper Sørensen
 */
public class PatternFinderResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<String> _column;
    private final InputColumn<String> _groupColumn;
    private final Map<String, Crosstab<?>> _crosstabs;

    public PatternFinderResult(InputColumn<String> column, Crosstab<?> crosstab) {
        _column = column;
        _groupColumn = null;
        _crosstabs = new HashMap<String, Crosstab<?>>();
        _crosstabs.put(null, crosstab);
    }

    public PatternFinderResult(InputColumn<String> column, InputColumn<String> groupColumn,
            Map<String, Crosstab<?>> crosstabs) {
        _column = column;
        _groupColumn = groupColumn;
        _crosstabs = crosstabs;
    }

    public InputColumn<String> getColumn() {
        return _column;
    }

    public InputColumn<String> getGroupColumn() {
        return _groupColumn;
    }

    public Map<String, Crosstab<?>> getGroupedCrosstabs() {
        if (!isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a grouped crosstab based Pattern Finder result");
        }
        return _crosstabs;
    }

    public Crosstab<?> getSingleCrosstab() {
        if (isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a single crosstab based Pattern Finder result");
        }
        return _crosstabs.get(null);
    }

    public boolean isGroupingEnabled() {
        return _groupColumn != null;
    }

    @Metric("Pattern count")
    public int getPatternCount() {
        return getSingleCrosstab().getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN).getCategoryCount();
    }
}
