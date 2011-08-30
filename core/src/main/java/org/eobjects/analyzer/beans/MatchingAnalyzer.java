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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.transform.DictionaryMatcherTransformer;
import org.eobjects.analyzer.beans.transform.StringPatternMatcherTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.metamodel.util.CollectionUtils;

@AnalyzerBean("Matching analyzer")
@Description("Provides a handy shortcut for doing matching with dictionaries and string patterns as well as retrieving matching matrices for all matches.")
public class MatchingAnalyzer implements RowProcessingAnalyzer<BooleanAnalyzerResult> {

	@Configured(order = 1)
	InputColumn<?>[] columns;

	@Configured(order = 2, required = false)
	Dictionary[] dictionaries;

	@Configured(order = 3, required = false)
	StringPattern[] stringPatterns;

	private BooleanAnalyzer _booleanAnalyzer;
	private DictionaryMatcherTransformer[] _dictionaryMatchers;
	private StringPatternMatcherTransformer[] _stringPatternMatchers;
	private List<InputColumn<Boolean>> _matchColumns;

	public MatchingAnalyzer(InputColumn<?>[] columns, Dictionary[] dictionaries, StringPattern[] stringPatterns) {
		this();
		this.columns = columns;
		this.dictionaries = dictionaries;
		this.stringPatterns = stringPatterns;
	}

	public MatchingAnalyzer() {
	}

	@Initialize
	public void init() {
		if (CollectionUtils.isNullOrEmpty(dictionaries) && CollectionUtils.isNullOrEmpty(stringPatterns)) {
			throw new IllegalStateException("No dictionaries or string patterns selected");
		}

		_dictionaryMatchers = new DictionaryMatcherTransformer[columns.length];
		_stringPatternMatchers = new StringPatternMatcherTransformer[columns.length];
		_matchColumns = new ArrayList<InputColumn<Boolean>>();

		OutputColumns outputColumns;
		for (int i = 0; i < columns.length; i++) {
			if (isDictionaryMatchingEnabled()) {
				// create matcher for dictionaries
				DictionaryMatcherTransformer dictionaryMatcher = new DictionaryMatcherTransformer(columns[i], dictionaries);
				outputColumns = dictionaryMatcher.getOutputColumns();
				addMatchColumns(outputColumns);
				_dictionaryMatchers[i] = dictionaryMatcher;
			}

			if (isStringPatternMatchingEnabled()) {
				// create matcher for string patterns
				StringPatternMatcherTransformer stringPatternMatcher = new StringPatternMatcherTransformer(columns[i],
						stringPatterns);
				outputColumns = stringPatternMatcher.getOutputColumns();
				addMatchColumns(outputColumns);
				_stringPatternMatchers[i] = stringPatternMatcher;
			}
		}

		@SuppressWarnings("unchecked")
		InputColumn<Boolean>[] columnArray = _matchColumns.toArray(new InputColumn[_matchColumns.size()]);
		_booleanAnalyzer = new BooleanAnalyzer(columnArray);
		_booleanAnalyzer.init();
	}

	private boolean isStringPatternMatchingEnabled() {
		return stringPatterns != null && stringPatterns.length > 0;
	}

	private boolean isDictionaryMatchingEnabled() {
		return dictionaries != null && dictionaries.length > 0;
	}

	private void addMatchColumns(OutputColumns outputColumns) {
		int count = outputColumns.getColumnCount();
		for (int i = 0; i < count; i++) {
			String columnName = outputColumns.getColumnName(i);
			InputColumn<Boolean> col = new MockInputColumn<Boolean>(columnName, Boolean.class);
			_matchColumns.add(col);
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		MockInputRow mockInputRow = new MockInputRow();

		int matchColumnIndex = 0;
		for (int i = 0; i < columns.length; i++) {
			final Object value = row.getValue(columns[i]);
			mockInputRow.put(columns[i], value);

			if (isDictionaryMatchingEnabled()) {
				Boolean[] matches = _dictionaryMatchers[i].transform(row);
				for (Boolean match : matches) {
					InputColumn<Boolean> matchColumn = _matchColumns.get(matchColumnIndex);
					matchColumnIndex++;
					mockInputRow.put(matchColumn, match);
				}
			}
			if (isStringPatternMatchingEnabled()) {
				Boolean[] matches = _stringPatternMatchers[i].transform(row);
				for (Boolean match : matches) {
					InputColumn<Boolean> matchColumn = _matchColumns.get(matchColumnIndex);
					matchColumnIndex++;
					mockInputRow.put(matchColumn, match);
				}
			}
		}

		_booleanAnalyzer.run(mockInputRow, distinctCount);
	}

	@Override
	public BooleanAnalyzerResult getResult() {
		return _booleanAnalyzer.getResult();
	}
}
