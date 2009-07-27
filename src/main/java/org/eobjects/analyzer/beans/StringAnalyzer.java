package org.eobjects.analyzer.beans;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.util.AverageBuilder;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FormatHelper;

@AnalyzerBean("String analyzer")
public class StringAnalyzer implements RowProcessingAnalyzer {

	private static final short INDEX_NUM_CHARS = 0;
	private static final short INDEX_MAX_CHARS = 1;
	private static final short INDEX_MIN_CHARS = 2;
	private static final short INDEX_MAX_BLANKS = 3;
	private static final short INDEX_MIN_BLANKS = 4;
	private static final short INDEX_NUM_UPPERCASE = 5;
	private static final short INDEX_NUM_LOWERCASE = 6;
	private static final short INDEX_NUM_NONLETTER = 7;
	private static final short INDEX_NUM_WORDS = 8;
	private static final short INDEX_MAX_WORDS = 9;
	private static final short INDEX_MIN_WORDS = 10;
	private static final short INDEX_MAX_WHITE_SPACES = 11;
	private static final short INDEX_MIN_WHITE_SPACES = 12;

	private NumberFormat numberFormat = FormatHelper.getUiNumberFormat();
	private Map<Column, Long[]> counts = new HashMap<Column, Long[]>();
	private Map<Column, AverageBuilder> charAverages = new HashMap<Column, AverageBuilder>();
	private Map<Column, AverageBuilder> blanksAverages = new HashMap<Column, AverageBuilder>();

	@Configured
	Column[] columns;

	@Initialize
	public void init() {
		for (Column column : columns) {
			if (!column.getType().isLiteral()) {
				throw new IllegalArgumentException(
						"Column is not of literal type: " + column);
			}
		}
	}

	@Override
	public void run(Row row, long distinctCount) {
		for (Column column : columns) {
			Object value = row.getValue(column);
			Long[] counters = counts.get(column);
			AverageBuilder charAverageBuilder = charAverages.get(column);
			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);
			if (counters == null) {
				counters = new Long[13];
				counters[INDEX_NUM_CHARS] = 0l;
				counters[INDEX_MIN_CHARS] = null;
				counters[INDEX_MAX_CHARS] = null;
				counters[INDEX_MAX_BLANKS] = null;
				counters[INDEX_MIN_BLANKS] = null;
				counters[INDEX_NUM_UPPERCASE] = 0l;
				counters[INDEX_NUM_LOWERCASE] = 0l;
				counters[INDEX_NUM_NONLETTER] = 0l;
				counters[INDEX_NUM_WORDS] = 0l;
				counters[INDEX_MIN_WORDS] = null;
				counters[INDEX_MAX_WORDS] = null;
				counters[INDEX_MIN_WHITE_SPACES] = null;
				counters[INDEX_MAX_WHITE_SPACES] = null;
				counts.put(column, counters);

				charAverageBuilder = new AverageBuilder();
				charAverages.put(column, charAverageBuilder);

				blanksAverageBuilder = new AverageBuilder();
				blanksAverages.put(column, blanksAverageBuilder);
			}
			if (value != null) {
				String string = value.toString();
				long numChars = string.length();
				long numWords = new StringTokenizer(string).countTokens();
				long numBlanks = countBlanks(string);

				if (counters[INDEX_MIN_CHARS] == null) {
					// This is the first time we encounter a non-null value, so
					// we
					// just set all counters
					counters[INDEX_MAX_CHARS] = numChars;
					counters[INDEX_MIN_CHARS] = numChars;
					counters[INDEX_MIN_WORDS] = numWords;
					counters[INDEX_MAX_WORDS] = numWords;
					counters[INDEX_MIN_BLANKS] = numBlanks;
					counters[INDEX_MAX_BLANKS] = numBlanks;
				}

				counters[INDEX_NUM_CHARS] = counters[INDEX_NUM_CHARS]
						+ numChars;
				counters[INDEX_NUM_WORDS] = counters[INDEX_NUM_WORDS]
						+ numWords;

				if (counters[INDEX_MAX_CHARS] < numChars) {
					counters[INDEX_MAX_CHARS] = numChars;
				}
				if (counters[INDEX_MIN_CHARS] > numChars) {
					counters[INDEX_MIN_CHARS] = numChars;
				}
				if (counters[INDEX_MAX_WORDS] < numWords) {
					counters[INDEX_MAX_WORDS] = numWords;
				}
				if (counters[INDEX_MIN_WORDS] > numWords) {
					counters[INDEX_MIN_WORDS] = numWords;
				}
				if (counters[INDEX_MAX_BLANKS] < numBlanks) {
					counters[INDEX_MAX_BLANKS] = numBlanks;
				}
				if (counters[INDEX_MIN_BLANKS] > numBlanks) {
					counters[INDEX_MIN_BLANKS] = numBlanks;
				}

				for (int i = 0; i < numChars; i++) {
					char c = string.charAt(i);
					if (Character.isLetter(c)) {
						if (Character.isUpperCase(c)) {
							counters[INDEX_NUM_UPPERCASE] = counters[INDEX_NUM_UPPERCASE] + 1;
						} else {
							counters[INDEX_NUM_LOWERCASE] = counters[INDEX_NUM_LOWERCASE] + 1;
						}
					} else {
						counters[INDEX_NUM_NONLETTER] = counters[INDEX_NUM_NONLETTER] + 1;
					}
				}

				charAverageBuilder.addValue(numChars);
				blanksAverageBuilder.addValue(numBlanks);
			}
		}
	}

	public static long countBlanks(String str) {
		int count = 0;
		char[] chars = str.toCharArray();
		for (char c : chars) {
			if (Character.isWhitespace(c)) {
				count++;
			}
		}
		return count;
	}

	@Result
	public CrosstabResult result() {
		CrosstabDimension measureDimension = new CrosstabDimension("measure");
		measureDimension.addCategory("Char count");
		measureDimension.addCategory("Max chars");
		measureDimension.addCategory("Min chars");
		measureDimension.addCategory("Avg chars");
		measureDimension.addCategory("Max white spaces");
		measureDimension.addCategory("Min white spaces");
		measureDimension.addCategory("Avg white spaces");
		measureDimension.addCategory("Uppercase chars");
		measureDimension.addCategory("Lowercase chars");
		measureDimension.addCategory("Non-letter chars");
		measureDimension.addCategory("Word count");
		measureDimension.addCategory("Max words");
		measureDimension.addCategory("Min words");

		CrosstabDimension columnDimension = new CrosstabDimension("column");

		Crosstab<String> crosstab = new Crosstab<String>(String.class,
				columnDimension, measureDimension);

		for (Column column : columns) {
			String columnName = column.getName();

			columnDimension.addCategory(columnName);
			CrosstabNavigator<String> nav = crosstab.where(columnDimension,
					columnName);

			Long[] columnCounts = this.counts.get(column);
			AverageBuilder charAverageBuilder = charAverages.get(column);
			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);

			Long numChars = columnCounts[INDEX_NUM_CHARS];
			final Long maxChars = columnCounts[INDEX_MAX_CHARS];
			final Long minChars = columnCounts[INDEX_MIN_CHARS];
			String avgChars = null;
			if (charAverageBuilder.getNumValues() > 0) {
				avgChars = numberFormat.format(charAverageBuilder.getAverage());
			}
			String avgBlanks = null;
			if (blanksAverageBuilder.getNumValues() > 0) {
				avgBlanks = numberFormat.format(blanksAverageBuilder
						.getAverage());
			}
			String numUppercase = "0%";
			String numLowercase = "0%";
			String numNonletter = "0%";
			if (numChars > 0) {
				numUppercase = (columnCounts[INDEX_NUM_UPPERCASE] * 100 / numChars)
						+ "%";
				numLowercase = (columnCounts[INDEX_NUM_LOWERCASE] * 100 / numChars)
						+ "%";
				numNonletter = (columnCounts[INDEX_NUM_NONLETTER] * 100 / numChars)
						+ "%";
			}
			final Long numWords = columnCounts[INDEX_NUM_WORDS];
			final Long maxWords = columnCounts[INDEX_MAX_WORDS];
			final Long minWords = columnCounts[INDEX_MIN_WORDS];
			final Long maxBlanks = columnCounts[INDEX_MAX_BLANKS];
			final Long minBlanks = columnCounts[INDEX_MIN_BLANKS];

			nav.where(measureDimension, "Char count").put(
					Long.toString(numChars));
			nav.where(measureDimension, "Max chars").put(
					Long.toString(maxChars));
			nav.where(measureDimension, "Min chars").put(
					Long.toString(minChars));
			nav.where(measureDimension, "Avg chars").put(avgChars);
			nav.where(measureDimension, "Max white spaces").put(
					Long.toString(maxBlanks));
			nav.where(measureDimension, "Min white spaces").put(
					Long.toString(minBlanks));
			nav.where(measureDimension, "Avg white spaces").put(avgBlanks);
			nav.where(measureDimension, "Uppercase chars").put(numUppercase);
			nav.where(measureDimension, "Lowercase chars").put(numLowercase);
			nav.where(measureDimension, "Non-letter chars").put(numNonletter);
			nav.where(measureDimension, "Word count").put(
					Long.toString(numWords));
			nav.where(measureDimension, "Max words").put(
					Long.toString(maxWords));
			nav.where(measureDimension, "Min words").put(
					Long.toString(minWords));

			// TODO: Attach queries / drill to detail sources / action
			// listeners?
			// if (maxChars != null) {
			// MatrixValue mv = matrixValues[1];
			// mv.setDetailSource(getBaseQuery(column));
			// mv.addDetailRowFilter(getCharFilter(column, maxChars));
			// }
			//
			// if (minChars != null) {
			// MatrixValue mv = matrixValues[2];
			// mv.setDetailSource(getBaseQuery(column));
			// mv.addDetailRowFilter(getCharFilter(column, minChars));
			// }
			//
			// if (maxWords != null) {
			// MatrixValue mv = matrixValues[11];
			// mv.setDetailSource(getBaseQuery(column));
			// mv.addDetailRowFilter(getWordFilter(column, maxWords));
			// }
			//
			// if (minWords != null) {
			// MatrixValue mv = matrixValues[12];
			// mv.setDetailSource(getBaseQuery(column));
			// mv.addDetailRowFilter(getWordFilter(column, minWords));
			// }
		}

		return new CrosstabResult(getClass(), crosstab);
	}
}
