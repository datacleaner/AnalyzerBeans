package org.eobjects.analyzer.beans.similarity;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

@AnalyzerBean("Phonetic similarity finder")
public class PhoneticSimilarityFinder implements RowProcessingAnalyzer {

	private final static Logger logger = LoggerFactory
			.getLogger(PhoneticSimilarityFinder.class);

	// everything that is ~75% similar will be included
	private final static double SIMILARITY_THRESHOLD = 3d / 4;

	@Inject
	@Configured
	Column column;

	@Inject
	@Provided
	Map<String, Integer> values;

	@Override
	public void run(Row row, int distinctCount) {
		Object value = row.getValue(column);
		run(value, distinctCount);
	}

	protected void run(Object value, int distinctCount) {
		if (value != null) {
			String stringValue = value.toString();
			stringValue = stringValue.trim().toLowerCase();
			if (!"".equals(stringValue)) {
				Integer count = values.get(stringValue);
				if (count == null) {
					values.put(stringValue, distinctCount);
				} else {
					values.put(stringValue, count + distinctCount);
				}
			}
		}
	}

	@Result
	public SimilarityResult getResult() {
		Set<SimilarValues> similarValues = new TreeSet<SimilarValues>();

		Soundex soundex = new Soundex();
		RefinedSoundex refinedSoundex = new RefinedSoundex();
		Metaphone metaphone = new Metaphone();

		int soundexThreshold = (int) Math.round(SIMILARITY_THRESHOLD * 4);

		Set<String> keys = values.keySet();
		for (String s1 : keys) {
			for (String s2 : keys) {
				if (!s1.equals(s2)) {
					Integer soundexDiff = null;
					try {
						soundexDiff = soundex.difference(s1, s2);
					} catch (EncoderException e) {
						logger.error("Could not determine soundex difference",
								e);
					}

					Integer refinedSoundexDiff = null;
					try {
						refinedSoundexDiff = refinedSoundex.difference(s1, s2);
					} catch (EncoderException e) {
						logger.error(
								"Could not determine refined soundex difference",
								e);
					}

					boolean metaphoneEquals = metaphone
							.isMetaphoneEqual(s1, s2);

					int refinedSoundexThreshold = (int) Math
							.round(SIMILARITY_THRESHOLD
									* Math.min(s1.length(), s2.length()));
					if (metaphoneEquals || soundexDiff >= soundexThreshold
							|| refinedSoundexDiff >= refinedSoundexThreshold) {
						// we have a similarity match
						similarValues.add(new SimilarValues(s1, s2));
					}
				}
			}
		}

		return new SimilarityResult(getClass(), similarValues);
	}

	// setter for test-purposes
	public void setValues(Map<String, Integer> values) {
		this.values = values;
	}

	// setter for test-purposes
	public void setColumn(Column column) {
		this.column = column;
	}
}