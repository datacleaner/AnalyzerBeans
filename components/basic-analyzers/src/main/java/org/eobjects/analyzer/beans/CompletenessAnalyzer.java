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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.categories.ValidationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.util.HasName;

@AnalyzerBean("Completeness analyzer")
@Description("Asserts the completeness of your data by ensuring that all required fields are filled.")
@Categorized(ValidationCategory.class)
public class CompletenessAnalyzer implements Analyzer<CompletenessAnalyzerResult> {

    public static enum Condition implements HasName {
        NOT_BLANK_OR_NULL("Not <blank> or <null>"), NOT_NULL("Not <null>");
        
        private final String _name;

        private Condition(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Inject
    @Configured("Values")
    @Description("Values to check for completeness")
    InputColumn<?>[] _valueColumns;

    @Inject
    @Configured("Conditions")
    @Description("The conditions of which a value is determined to be filled or not")
    Condition[] _conditions;

    @Inject
    @Provided
    RowAnnotation _invalidRecords;

    @Inject
    @Provided
    RowAnnotationFactory _annotationFactory;

    private final AtomicInteger _rowCount;

    public CompletenessAnalyzer() {
        _rowCount = new AtomicInteger();
    }

    @Initialize
    public void init() {
        _rowCount.set(0);
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        _rowCount.addAndGet(distinctCount);
        for (int i = 0; i < _valueColumns.length; i++) {
            final Object value = row.getValue(_valueColumns[i]);
            final boolean valid;
            if (value instanceof String && _conditions[i] == Condition.NOT_BLANK_OR_NULL) {
                valid = !StringUtils.isNullOrEmpty((String) value);
            } else {
                valid = value != null;
            }
            if (!valid) {
                _annotationFactory.annotate(row, distinctCount, _invalidRecords);
                return;
            }
        }
    }

    @Override
    public CompletenessAnalyzerResult getResult() {
        return new CompletenessAnalyzerResult(_rowCount.get(), _invalidRecords, _annotationFactory, _valueColumns);
    }

}
