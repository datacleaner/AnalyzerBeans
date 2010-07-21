package org.eobjects.analyzer.beans;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.QueryResultProducer;
import org.eobjects.analyzer.result.SerializableRowFilter;
import org.eobjects.analyzer.util.AverageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FormatHelper;

@AnalyzerBean("String analyzer")
public class StringAnalyzer implements RowProcessingAnalyzer {

	private static final Logger logger = LoggerFactory
			.getLogger(StringAnalyzer.class);

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
	private Map<InputColumn<String>, Integer[]> counts = new HashMap<InputColumn<String>, Integer[]>();
	private Map<InputColumn<String>, AverageBuilder> charAverages = new HashMap<InputColumn<String>, AverageBuilder>();
	private Map<InputColumn<String>, AverageBuilder> blanksAverages = new HashMap<InputColumn<String>, AverageBuilder>();

	@Configured
	InputColumn<String>[] columns;

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<String> column : columns) {
			String value = row.getValue(column);
			Integer[] counters = counts.get(column);
			AverageBuilder charAverageBuilder = charAverages.get(column);
			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);
			if (counters == null) {
				counters = new Integer[13];
				counters[INDEX_NUM_CHARS] = 0;
				counters[INDEX_MIN_CHARS] = null;
				counters[INDEX_MAX_CHARS] = null;
				counters[INDEX_MAX_BLANKS] = null;
				counters[INDEX_MIN_BLANKS] = null;
				counters[INDEX_NUM_UPPERCASE] = 0;
				counters[INDEX_NUM_LOWERCASE] = 0;
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
			if (value != null) {
				int numChars = value.length();
				int numWords = new StringTokenizer(value).countTokens();
				int numBlanks = countBlanks(value);

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
					char c = value.charAt(i);
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

	public static int countBlanks(String str) {
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
		logger.info("getResult()");
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

		for (InputColumn<String> column : columns) {
			String columnName = column.getName();

			columnDimension.addCategory(columnName);
			CrosstabNavigator<String> nav = crosstab.where(columnDimension,
					columnName);

			Integer[] columnCounts = this.counts.get(column);
			final Integer numChars;
			final Integer maxChars;
			final Integer minChars;
			final Integer numWords;
			final Integer maxWords;
			final Integer minWords;
			final Integer maxBlanks;
			final Integer minBlanks;
			if (columnCounts != null) {
				numChars = columnCounts[INDEX_NUM_CHARS];
				maxChars = columnCounts[INDEX_MAX_CHARS];
				minChars = columnCounts[INDEX_MIN_CHARS];
				numWords = columnCounts[INDEX_NUM_WORDS];
				maxWords = columnCounts[INDEX_MAX_WORDS];
				minWords = columnCounts[INDEX_MIN_WORDS];
				maxBlanks = columnCounts[INDEX_MAX_BLANKS];
				minBlanks = columnCounts[INDEX_MIN_BLANKS];
			} else {
				numChars = null;
				maxChars = null;
				minChars = null;
				numWords = null;
				maxWords = null;
				minWords = null;
				maxBlanks = null;
				minBlanks = null;
			}

			AverageBuilder charAverageBuilder = charAverages.get(column);
			if (charAverageBuilder == null) {
				charAverageBuilder = new AverageBuilder();
			}

			AverageBuilder blanksAverageBuilder = blanksAverages.get(column);
			if (blanksAverageBuilder == null) {
				blanksAverageBuilder = new AverageBuilder();
			}

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
			if (numChars != null && numChars > 0) {
				numUppercase = (columnCounts[INDEX_NUM_UPPERCASE] * 100 / numChars)
						+ "%";
				numLowercase = (columnCounts[INDEX_NUM_LOWERCASE] * 100 / numChars)
						+ "%";
				numNonletter = (columnCounts[INDEX_NUM_NONLETTER] * 100 / numChars)
						+ "%";
			}

			boolean isPhysicalColumn = column.isPhysicalColumn();

			// base query for exploration data result producers
			Query baseQuery = null;
			QueryResultProducer resultProducer;
			if (isPhysicalColumn) {
				baseQuery = getBaseQuery(column.getPhysicalColumn());
			}

			if (numChars != null) {
				nav.where(measureDimension, "Char count").put(
						Long.toString(numChars));
			}
			if (maxChars != null) {
				nav.where(measureDimension, "Max chars").put(
						Long.toString(maxChars));

				if (isPhysicalColumn) {
					resultProducer = new QueryResultProducer(baseQuery);
					resultProducer.addFilter(new CharRowFilter(column
							.getPhysicalColumn(), maxChars));
					nav.attach(resultProducer);
				}
			}

			if (minChars != null) {
				nav.where(measureDimension, "Min chars").put(
						Long.toString(minChars));
				if (isPhysicalColumn) {
					resultProducer = new QueryResultProducer(baseQuery);
					resultProducer.addFilter(new CharRowFilter(column
							.getPhysicalColumn(), minChars));
					nav.attach(resultProducer);
				}
			}

			if (columnCounts != null) {
				nav.where(measureDimension, "Avg chars").put(avgChars);
				nav.where(measureDimension, "Max white spaces").put(
						Long.toString(maxBlanks));
				nav.where(measureDimension, "Min white spaces").put(
						Long.toString(minBlanks));
				nav.where(measureDimension, "Avg white spaces").put(avgBlanks);
				nav.where(measureDimension, "Uppercase chars")
						.put(numUppercase);
				nav.where(measureDimension, "Lowercase chars")
						.put(numLowercase);
				nav.where(measureDimension, "Non-letter chars").put(
						numNonletter);
				nav.where(measureDimension, "Word count").put(
						Long.toString(numWords));
			}

			if (maxWords != null) {
				nav.where(measureDimension, "Max words").put(
						Long.toString(maxWords));
				if (isPhysicalColumn) {
					resultProducer = new QueryResultProducer(baseQuery);
					resultProducer.addFilter(new WordRowFilter(column
							.getPhysicalColumn(), maxWords));
					nav.attach(resultProducer);
				}
			}

			if (minWords != null) {
				nav.where(measureDimension, "Min words").put(
						Long.toString(minWords));
				if (isPhysicalColumn) {
					resultProducer = new QueryResultProducer(baseQuery);
					resultProducer.addFilter(new WordRowFilter(column
							.getPhysicalColumn(), minWords));
					nav.attach(resultProducer);
				}
			}
		}

		return new CrosstabResult(getClass(), crosstab);
	}

	private Query getBaseQuery(Column column) {
		return new Query().from(column.getTable())
				.select(new SelectItem(column)).selectCount().groupBy(column);
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
			if (value != null
					&& new StringTokenizer(value.toString()).countTokens() == numWords) {
				return true;
			}
			return false;
		}
	}
}
