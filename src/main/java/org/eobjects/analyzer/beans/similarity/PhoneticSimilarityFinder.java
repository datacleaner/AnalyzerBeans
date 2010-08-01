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
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Phonetic similarity finder")
public class PhoneticSimilarityFinder implements
		RowProcessingAnalyzer<SimilarityResult> {

	private final static Logger logger = LoggerFactory
			.getLogger(PhoneticSimilarityFinder.class);

	// everything that is ~75% similar will be included
	private final static double SIMILARITY_THRESHOLD = 3d / 4;

	@Inject
	@Configured
	InputColumn<String> column;

	@Inject
	@Provided
	Map<String, Integer> values;

	@Override
	public void run(InputRow row, int distinctCount) {
		String value = row.getValue(column);
		run(value, distinctCount);
	}

	protected void run(String value, int distinctCount) {
		if (value != null) {
			value = value.trim().toLowerCase();
			if (!"".equals(value)) {
				Integer count = values.get(value);
				if (count == null) {
					values.put(value, distinctCount);
				} else {
					values.put(value, count + distinctCount);
				}
			}
		}
	}

	@Override
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
	public void setColumn(InputColumn<String> column) {
		this.column = column;
	}
}