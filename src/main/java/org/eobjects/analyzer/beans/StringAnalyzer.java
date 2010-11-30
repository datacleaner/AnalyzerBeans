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

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.StringAnalyzerResult;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.AverageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An analyzer for various typical String measures.
 * 
 * @author Kasper Sørensen
 */
@AnalyzerBean("String analyzer")
@Description("The String analyzer is used to collect a variety of typical metrics on string values.\nMetrics include statistics on character case, words, diacritics, white-spaces and more...")
@Concurrent(true)
public class StringAnalyzer implements RowProcessingAnalyzer<StringAnalyzerResult> {

	private static final Logger logger = LoggerFactory.getLogger(StringAnalyzer.class);

	private Map<InputColumn<String>, StringAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<String>, StringAnalyzerColumnDelegate>();

	@Configured
	InputColumn<String>[] _columns;

	@Provided
	RowAnnotationFactory _annotationFactory;

	public StringAnalyzer() {
	}

	public StringAnalyzer(InputColumn<String>... columns) {
		_columns = columns;
		_annotationFactory = new InMemoryRowAnnotationFactory();
		init();
	}

	@Initialize
	public void init() {
		for (InputColumn<String> column : _columns) {
			_columnDelegates.put(column, new StringAnalyzerColumnDelegate(_annotationFactory));
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<String> column : _columns) {
			String value = row.getValue(column);

			StringAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
			delegate.run(row, value, distinctCount);
		}
	}

	@Override
	public StringAnalyzerResult getResult() {
		logger.info("getResult()");
		CrosstabDimension measureDimension = new CrosstabDimension("Measures");
		measureDimension.addCategory("Row count");
		measureDimension.addCategory("Null count");
		measureDimension.addCategory("Entirely uppercase count");
		measureDimension.addCategory("Entirely lowercase count");
		measureDimension.addCategory("Total char count");
		measureDimension.addCategory("Max chars");
		measureDimension.addCategory("Min chars");
		measureDimension.addCategory("Avg chars");
		measureDimension.addCategory("Max white spaces");
		measureDimension.addCategory("Min white spaces");
		measureDimension.addCategory("Avg white spaces");
		measureDimension.addCategory("Uppercase chars");
		measureDimension.addCategory("Uppercase chars (excl. first letters)");
		measureDimension.addCategory("Lowercase chars");
		measureDimension.addCategory("Digit chars");
		measureDimension.addCategory("Diacritic chars");
		measureDimension.addCategory("Non-letter chars");
		measureDimension.addCategory("Word count");
		measureDimension.addCategory("Max words");
		measureDimension.addCategory("Min words");

		CrosstabDimension columnDimension = new CrosstabDimension("Column");

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);

		for (InputColumn<String> column : _columns) {
			String columnName = column.getName();

			StringAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

			columnDimension.addCategory(columnName);

			final Integer numRows = delegate.getNumRows();
			final Integer numNull = delegate.getNumNull();
			final Integer numEntirelyUppercase = delegate.getNumEntirelyUppercase();
			final Integer numEntirelyLowercase = delegate.getNumEntirelyLowercase();
			final Integer numChars = delegate.getNumChars();
			final Integer maxChars = delegate.getMaxChars();
			final Integer minChars = delegate.getMinChars();
			final Integer numWords = delegate.getNumWords();
			final Integer maxWords = delegate.getMaxWords();
			final Integer minWords = delegate.getMinWords();
			final Integer maxWhitespace = delegate.getMaxWhitespace();
			final Integer minWhitespace = delegate.getMinWhitespace();
			final Integer numUppercase = delegate.getNumUppercase();
			final Integer numUppercaseExclFirstLetter = delegate.getNumUppercaseExclFirstLetter();
			final Integer numLowercase = delegate.getNumLowercase();
			final Integer numDigits = delegate.getNumDigit();
			final Integer numDiacritics = delegate.getNumDiacritics();
			final Integer numNonLetter = delegate.getNumNonLetter();
			final AverageBuilder charAverageBuilder = delegate.getCharAverageBuilder();
			final AverageBuilder blanksAverageBuilder = delegate.getWhitespaceAverageBuilder();

			Double avgChars = null;
			if (charAverageBuilder.getNumValues() > 0) {
				avgChars = charAverageBuilder.getAverage();
			}
			Double avgBlanks = null;
			if (blanksAverageBuilder.getNumValues() > 0) {
				avgBlanks = blanksAverageBuilder.getAverage();
			}

			// begin entering numbers into the crosstab
			CrosstabNavigator<Number> nav = crosstab.where(columnDimension, columnName);

			nav.where(measureDimension, "Row count").put(numRows);

			nav.where(measureDimension, "Null count").put(numNull);
			if (numNull > 0) {
				addAttachment(nav, delegate.getNullAnnotation(), column);
			}

			nav.where(measureDimension, "Entirely uppercase count").put(numEntirelyUppercase);
			if (numEntirelyUppercase > 0) {
				addAttachment(nav, delegate.getEntirelyUppercaseAnnotation(), column);
			}

			nav.where(measureDimension, "Entirely lowercase count").put(numEntirelyLowercase);
			if (numEntirelyLowercase > 0) {
				addAttachment(nav, delegate.getEntirelyLowercaseAnnotation(), column);
			}

			nav.where(measureDimension, "Total char count").put(numChars);

			nav.where(measureDimension, "Max chars").put(maxChars);
			if (maxChars != null) {
				addAttachment(nav, delegate.getMaxCharsAnnotation(), column);
			}

			nav.where(measureDimension, "Min chars").put(minChars);
			if (minChars != null) {
				addAttachment(nav, delegate.getMinCharsAnnotation(), column);
			}

			nav.where(measureDimension, "Avg chars").put(avgChars);
			nav.where(measureDimension, "Max white spaces").put(maxWhitespace);
			if (maxWhitespace != null) {
				addAttachment(nav, delegate.getMaxWhitespaceAnnotation(), column);
			}

			nav.where(measureDimension, "Min white spaces").put(minWhitespace);
			if (minWhitespace != null) {
				addAttachment(nav, delegate.getMinWhitespaceAnnotation(), column);
			}

			nav.where(measureDimension, "Avg white spaces").put(avgBlanks);
			nav.where(measureDimension, "Uppercase chars").put(numUppercase);
			nav.where(measureDimension, "Uppercase chars (excl. first letters)").put(numUppercaseExclFirstLetter);
			if (numUppercaseExclFirstLetter > 0) {
				addAttachment(nav, delegate.getUppercaseExclFirstLetterAnnotation(), column);
			}

			nav.where(measureDimension, "Lowercase chars").put(numLowercase);
			nav.where(measureDimension, "Digit chars").put(numDigits);
			if (numDigits > 0) {
				addAttachment(nav, delegate.getDigitAnnotation(), column);
			}

			nav.where(measureDimension, "Diacritic chars").put(numDiacritics);
			if (numDiacritics > 0) {
				addAttachment(nav, delegate.getDiacriticAnnotation(), column);
			}

			nav.where(measureDimension, "Non-letter chars").put(numNonLetter);
			nav.where(measureDimension, "Word count").put(numWords);

			nav.where(measureDimension, "Max words").put(maxWords);
			if (maxWords != null) {
				addAttachment(nav, delegate.getMaxWordsAnnotation(), column);
			}

			nav.where(measureDimension, "Min words").put(minWords);
			if (minWords != null) {
				addAttachment(nav, delegate.getMinWordsAnnotation(), column);
			}
		}

		return new StringAnalyzerResult(_columns, crosstab);
	}

	private void addAttachment(CrosstabNavigator<Number> nav, RowAnnotation annotation, InputColumn<?> column) {
		nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
	}
}
