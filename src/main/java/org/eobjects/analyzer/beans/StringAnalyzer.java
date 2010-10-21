package org.eobjects.analyzer.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.QueryResultProducer;
import org.eobjects.analyzer.result.SerializableRowFilter;
import org.eobjects.analyzer.util.AverageBuilder;
import org.eobjects.analyzer.util.CharIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

/**
 * An analyzer for various typical String measures.
 * 
 * @author Kasper Sørensen
 */
@AnalyzerBean("String analyzer")
public class StringAnalyzer implements RowProcessingAnalyzer<CrosstabResult> {

	private static final Logger logger = LoggerFactory.getLogger(StringAnalyzer.class);

	private static final short INDEX_NUM_ROWS = 0;
	private static final short INDEX_NUM_NULL = 1;
	private static final short INDEX_NUM_ALL_UPPERCASE = 2;
	private static final short INDEX_NUM_ALL_LOWERCASE = 3;
	private static final short INDEX_NUM_CHARS = 4;
	private static final short INDEX_MAX_CHARS = 5;
	private static final short INDEX_MIN_CHARS = 6;
	private static final short INDEX_MAX_BLANKS = 7;
	private static final short INDEX_MIN_BLANKS = 8;
	private static final short INDEX_NUM_UPPERCASE = 9;
	private static final short INDEX_NUM_LOWERCASE = 10;
	private static final short INDEX_NUM_DIGIT = 11;
	private static final short INDEX_NUM_DIACRITICS = 12;
	private static final short INDEX_NUM_NONLETTER = 13;
	private static final short INDEX_NUM_WORDS = 14;
	private static final short INDEX_MAX_WORDS = 15;
	private static final short INDEX_MIN_WORDS = 16;
	private static final short INDEX_MAX_WHITE_SPACES = 17;
	private static final short INDEX_MIN_WHITE_SPACES = 18;

	private Map<InputColumn<String>, Integer[]> counts = new HashMap<InputColumn<String>, Integer[]>();
	private Map<InputColumn<String>, AverageBuilder> charAverages = new HashMap<InputColumn<String>, AverageBuilder>();
	private Map<InputColumn<String>, AverageBuilder> blanksAverages = new HashMap<InputColumn<String>, AverageBuilder>();

	@Configured
	InputColumn<String>[] columns;

	public StringAnalyzer() {
	}

	public StringAnalyzer(InputColumn<String>... columns) {
		this.columns = columns;
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<String> column : columns) {
			String value = row.getValue(column);
			Integer[] counters = counts.get(column);
			AverageBuilder charAverageBuilder = charAverages.get(column);
			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);
			if (counters == null) {
				counters = new Integer[19];
				counters[INDEX_NUM_ROWS] = 0;
				counters[INDEX_NUM_NULL] = 0;
				counters[INDEX_NUM_ALL_UPPERCASE] = 0;
				counters[INDEX_NUM_ALL_LOWERCASE] = 0;
				counters[INDEX_NUM_CHARS] = 0;
				counters[INDEX_MIN_CHARS] = null;
				counters[INDEX_MAX_CHARS] = null;
				counters[INDEX_MAX_BLANKS] = null;
				counters[INDEX_MIN_BLANKS] = null;
				counters[INDEX_NUM_UPPERCASE] = 0;
				counters[INDEX_NUM_LOWERCASE] = 0;
				counters[INDEX_NUM_DIGIT] = 0;
				counters[INDEX_NUM_DIACRITICS] = 0;
				counters[INDEX_NUM_NONLETTER] = 0;
				counters[INDEX_NUM_WORDS] = 0;
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

			counters[INDEX_NUM_ROWS] = counters[INDEX_NUM_ROWS] + distinctCount;

			if (value == null) {
				counters[INDEX_NUM_NULL] = counters[INDEX_NUM_NULL] + distinctCount;
			} else {
				int numChars = value.length();
				int totalChars = numChars * distinctCount;
				int numWords = new StringTokenizer(value).countTokens();
				int totalWords = numWords * distinctCount;

				int numBlanks = 0;
				int numDigits = 0;
				int numDiacritics = 0;
				int numLetters = 0;
				int numNonLetters = 0;
				int numUppercase = 0;
				int numLowercase = 0;
				CharIterator it = new CharIterator(value);
				while (it.hasNext()) {
					it.next();
					if (it.isLetter()) {
						numLetters += distinctCount;
						if (it.isUpperCase()) {
							numUppercase += distinctCount;
						} else {
							numLowercase += distinctCount;
						}
						if (it.isDiacritic()) {
							numDiacritics += distinctCount;
						}
					} else {
						numNonLetters += distinctCount;
						if (it.isDigit()) {
							numDigits += distinctCount;
						}
						if (it.isWhitespace()) {
							numBlanks++;
						}
					}
				}

				counters[INDEX_NUM_UPPERCASE] = counters[INDEX_NUM_UPPERCASE] + numUppercase;
				counters[INDEX_NUM_LOWERCASE] = counters[INDEX_NUM_LOWERCASE] + numLowercase;
				counters[INDEX_NUM_NONLETTER] = counters[INDEX_NUM_NONLETTER] + numNonLetters;

				if (counters[INDEX_MIN_CHARS] == null) {
					// This is the first time we encounter a non-null value, so
					// we just set all counters
					counters[INDEX_MAX_CHARS] = numChars;
					counters[INDEX_MIN_CHARS] = numChars;
					counters[INDEX_MIN_WORDS] = numWords;
					counters[INDEX_MAX_WORDS] = numWords;
					counters[INDEX_MIN_BLANKS] = numBlanks;
					counters[INDEX_MAX_BLANKS] = numBlanks;
				}

				counters[INDEX_NUM_CHARS] = counters[INDEX_NUM_CHARS] + totalChars;
				counters[INDEX_NUM_WORDS] = counters[INDEX_NUM_WORDS] + totalWords;

				if (numDiacritics > 0) {
					counters[INDEX_NUM_DIACRITICS] = counters[INDEX_NUM_DIACRITICS] + numDiacritics;
				}

				if (numDigits > 0) {
					counters[INDEX_NUM_DIGIT] = counters[INDEX_NUM_DIGIT] + numDigits;
				}

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

				if (numLetters > 0) {
					if (value.equals(value.toUpperCase())) {
						counters[INDEX_NUM_ALL_UPPERCASE] = counters[INDEX_NUM_ALL_UPPERCASE] + distinctCount;
					}

					if (value.equals(value.toLowerCase())) {
						counters[INDEX_NUM_ALL_LOWERCASE] = counters[INDEX_NUM_ALL_LOWERCASE] + distinctCount;
					}
				}

				charAverageBuilder.addValue(numChars);
				blanksAverageBuilder.addValue(numBlanks);
			}
		}
	}

	@Override
	public CrosstabResult getResult() {
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
		measureDimension.addCategory("Lowercase chars");
		measureDimension.addCategory("Digit chars");
		measureDimension.addCategory("Diacritic chars");
		measureDimension.addCategory("Non-letter chars");
		measureDimension.addCategory("Word count");
		measureDimension.addCategory("Max words");
		measureDimension.addCategory("Min words");

		CrosstabDimension columnDimension = new CrosstabDimension("Column");

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);

		for (InputColumn<String> column : columns) {
			String columnName = column.getName();

			columnDimension.addCategory(columnName);

			Integer[] columnCounts = this.counts.get(column);
			final Integer numRows;
			final Integer numNull;
			final Integer numAllUppercase;
			final Integer numAllLowercase;
			final Integer numChars;
			final Integer maxChars;
			final Integer minChars;
			final Integer numWords;
			final Integer maxWords;
			final Integer minWords;
			final Integer maxBlanks;
			final Integer minBlanks;
			final Integer numUppercase;
			final Integer numLowercase;
			final Integer numDigits;
			final Integer numDiacritics;
			final Integer numNonLetter;

			if (columnCounts != null) {
				numRows = columnCounts[INDEX_NUM_ROWS];
				numNull = columnCounts[INDEX_NUM_NULL];
				numAllUppercase = columnCounts[INDEX_NUM_ALL_UPPERCASE];
				numAllLowercase = columnCounts[INDEX_NUM_ALL_LOWERCASE];
				numChars = columnCounts[INDEX_NUM_CHARS];
				maxChars = columnCounts[INDEX_MAX_CHARS];
				minChars = columnCounts[INDEX_MIN_CHARS];
				numWords = columnCounts[INDEX_NUM_WORDS];
				maxWords = columnCounts[INDEX_MAX_WORDS];
				minWords = columnCounts[INDEX_MIN_WORDS];
				maxBlanks = columnCounts[INDEX_MAX_BLANKS];
				minBlanks = columnCounts[INDEX_MIN_BLANKS];
				numUppercase = columnCounts[INDEX_NUM_UPPERCASE];
				numLowercase = columnCounts[INDEX_NUM_LOWERCASE];
				numDigits = columnCounts[INDEX_NUM_DIGIT];
				numDiacritics = columnCounts[INDEX_NUM_DIACRITICS];
				numNonLetter = columnCounts[INDEX_NUM_NONLETTER];
			} else {
				numRows = 0;
				numNull = 0;
				numAllUppercase = 0;
				numAllLowercase = 0;
				numChars = 0;
				maxChars = null;
				minChars = null;
				numWords = 0;
				maxWords = null;
				minWords = null;
				maxBlanks = null;
				minBlanks = null;
				numUppercase = 0;
				numLowercase = 0;
				numDigits = 0;
				numDiacritics = 0;
				numNonLetter = 0;
			}

			AverageBuilder charAverageBuilder = charAverages.get(column);
			if (charAverageBuilder == null) {
				charAverageBuilder = new AverageBuilder();
			}

			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);
			if (blanksAverageBuilder == null) {
				blanksAverageBuilder = new AverageBuilder();
			}

			Double avgChars = null;
			if (charAverageBuilder.getNumValues() > 0) {
				avgChars = charAverageBuilder.getAverage();
			}
			Double avgBlanks = null;
			if (blanksAverageBuilder.getNumValues() > 0) {
				avgBlanks = blanksAverageBuilder.getAverage();
			}

			boolean queryable;
			if (columnCounts == null) {
				queryable = false;
			} else {
				queryable = column.isPhysicalColumn();
			}

			// base query for exploration data result producers
			Query baseQuery = null;
			QueryResultProducer resultProducer;
			if (queryable) {
				baseQuery = getBaseQuery(column.getPhysicalColumn());
			}

			// begin entering numbers into the crosstab
			CrosstabNavigator<Number> nav = crosstab.where(columnDimension, columnName);

			nav.where(measureDimension, "Row count").put(numRows);

			nav.where(measureDimension, "Null count").put(numNull);

			nav.where(measureDimension, "Entirely uppercase count").put(numAllUppercase);
			nav.where(measureDimension, "Entirely lowercase count").put(numAllLowercase);

			nav.where(measureDimension, "Total char count").put(numChars);

			nav.where(measureDimension, "Max chars").put(maxChars);
			if (queryable) {
				resultProducer = new QueryResultProducer(baseQuery);
				resultProducer.addFilter(new CharRowFilter(column.getPhysicalColumn(), maxChars));
				nav.attach(resultProducer);
			}

			nav.where(measureDimension, "Min chars").put(minChars);
			if (queryable) {
				resultProducer = new QueryResultProducer(baseQuery);
				resultProducer.addFilter(new CharRowFilter(column.getPhysicalColumn(), minChars));
				nav.attach(resultProducer);
			}

			nav.where(measureDimension, "Avg chars").put(avgChars);
			nav.where(measureDimension, "Max white spaces").put(maxBlanks);
			nav.where(measureDimension, "Min white spaces").put(minBlanks);
			nav.where(measureDimension, "Avg white spaces").put(avgBlanks);
			nav.where(measureDimension, "Uppercase chars").put(numUppercase);
			nav.where(measureDimension, "Lowercase chars").put(numLowercase);
			nav.where(measureDimension, "Digit chars").put(numDigits);
			nav.where(measureDimension, "Diacritic chars").put(numDiacritics);
			nav.where(measureDimension, "Non-letter chars").put(numNonLetter);
			nav.where(measureDimension, "Word count").put(numWords);

			nav.where(measureDimension, "Max words").put(maxWords);
			if (queryable) {
				resultProducer = new QueryResultProducer(baseQuery);
				resultProducer.addFilter(new WordRowFilter(column.getPhysicalColumn(), maxWords));
				nav.attach(resultProducer);
			}

			nav.where(measureDimension, "Min words").put(minWords);
			if (queryable) {
				resultProducer = new QueryResultProducer(baseQuery);
				resultProducer.addFilter(new WordRowFilter(column.getPhysicalColumn(), minWords));
				nav.attach(resultProducer);
			}

		}

		return new CrosstabResult(crosstab);
	}

	private Query getBaseQuery(Column column) {
		return new Query().from(column.getTable()).select(new SelectItem(column)).selectCount().groupBy(column);
	}

	static class CharRowFilter implements SerializableRowFilter {
		private static final long serialVersionUID = 1L;

		private Column column;
		private Integer numChars;

		public CharRowFilter(Column column, Integer numChars) {
			this.column = column;
			this.numChars = numChars;
		}

		@Override
		public boolean accept(Row row) {
			Object value = row.getValue(column);
			if (value != null && value.toString().length() == numChars) {
				return true;
			}
			return false;
		}

	}

	static class WordRowFilter implements SerializableRowFilter {
		private static final long serialVersionUID = 1L;

		private Column column;
		private Integer numWords;

		public WordRowFilter(Column column, Integer numWords) {
			this.column = column;
			this.numWords = numWords;
		}

		@Override
		public boolean accept(Row row) {
			Object value = row.getValue(column);
			if (value != null && new StringTokenizer(value.toString()).countTokens() == numWords) {
				return true;
			}
			return false;
		}
	}
}
