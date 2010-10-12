package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.util.CollectionUtils;

@AnalyzerBean("Pattern finder")
public class PatternFinderAnalyzer implements
		RowProcessingAnalyzer<CrosstabResult> {

	private Map<TokenPattern, List<String>> patterns;
	private TokenizerConfiguration configuration;
	private Tokenizer tokenizer;

	@Configured
	InputColumn<String> column;

	@Configured(value = "Predefined token name", required = false)
	String predefinedTokenName;

	@Configured(value = "Predefined token regexes", required = false)
	String[] predefinedTokenPatterns;

	@Configured(required = false)
	private Boolean discriminateTextCase;

	@Configured(required = false)
	private Boolean discriminateNegativeNumbers;

	@Configured(required = false)
	private Boolean discriminateDecimals;

	@Configured(required = false)
	private Boolean enableMixedTokens;

	@Configured(required = false)
	private Boolean ignoreRepeatedSpaces;

	@Configured(required = false)
	private Character decimalSeparator;

	@Configured(required = false)
	private Character thousandsSeparator;

	@Configured(required = false)
	private Character minusSign;

	@Initialize
	public void init() {
		patterns = new HashMap<TokenPattern, List<String>>();

		if (enableMixedTokens != null) {
			configuration = new TokenizerConfiguration(enableMixedTokens);
		} else {
			configuration = new TokenizerConfiguration();
		}

		if (discriminateNegativeNumbers != null) {
			configuration
					.setDiscriminateNegativeNumbers(discriminateNegativeNumbers);
		}

		if (discriminateDecimals != null) {
			configuration.setDiscriminateDecimalNumbers(discriminateDecimals);
		}

		if (discriminateTextCase != null) {
			configuration.setDiscriminateTextCase(discriminateTextCase);
		}

		if (ignoreRepeatedSpaces != null) {
			boolean ignoreSpacesLength = ignoreRepeatedSpaces.booleanValue();
			configuration.setDistriminateTokenLength(TokenType.WHITESPACE,
					!ignoreSpacesLength);
		}

		if (decimalSeparator != null) {
			configuration.setDecimalSeparator(decimalSeparator);
		}

		if (thousandsSeparator != null) {
			configuration.setThousandsSeparator(thousandsSeparator);
		}

		if (minusSign != null) {
			configuration.setMinusSign(minusSign);
		}

		if (predefinedTokenName != null && predefinedTokenPatterns != null) {
			Set<String> tokenRegexes = CollectionUtils
					.set(predefinedTokenPatterns);
			configuration.getPredefinedTokens().add(
					new PredefinedTokenDefinition(predefinedTokenName,
							tokenRegexes));
		}

		tokenizer = new DefaultTokenizer(configuration);
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		String value = row.getValue(column);
		if (value != null) {
			boolean match = false;
			List<Token> tokens = tokenizer.tokenize(value);
			Set<Entry<TokenPattern, List<String>>> entries = patterns
					.entrySet();
			for (Entry<TokenPattern, List<String>> entry : entries) {
				TokenPattern pattern = entry.getKey();
				List<String> matchingValues = entry.getValue();
				if (pattern.match(tokens)) {
					for (int i = 0; i < distinctCount; i++) {
						matchingValues.add(value);
					}
					match = true;
				}
			}

			if (!match) {
				TokenPattern pattern = new TokenPatternImpl(tokens,
						configuration);
				List<String> matchingValues = new ArrayList<String>(
						distinctCount);
				for (int i = 0; i < distinctCount; i++) {
					matchingValues.add(value);
				}
				patterns.put(pattern, matchingValues);
			}
		}
	}

	@Override
	public CrosstabResult getResult() {
		CrosstabDimension measuresDimension = new CrosstabDimension("Measures");
		measuresDimension.addCategory("Match count");
		CrosstabDimension patternDimension = new CrosstabDimension("Pattern");
		Crosstab<Serializable> crosstab = new Crosstab<Serializable>(
				Serializable.class, measuresDimension, patternDimension);

		Set<Entry<TokenPattern, List<String>>> entrySet = patterns.entrySet();

		// sort the entries so that the ones with the highest amount of matches
		// are at the top
		Set<Entry<TokenPattern, List<String>>> sortedEntrySet = new TreeSet<Entry<TokenPattern, List<String>>>(
				new Comparator<Entry<TokenPattern, List<String>>>() {
					public int compare(Entry<TokenPattern, List<String>> o1,
							Entry<TokenPattern, List<String>> o2) {
						int result = o2.getValue().size()
								- o1.getValue().size();
						if (result == 0) {
							result = o1.getKey().toSymbolicString()
									.compareTo(o2.getKey().toSymbolicString());
						}
						return result;
					}
				});
		sortedEntrySet.addAll(entrySet);

		for (Entry<TokenPattern, List<String>> entry : sortedEntrySet) {
			CrosstabNavigator<Serializable> nav = crosstab.where(
					patternDimension, entry.getKey().toSymbolicString());

			nav.where(measuresDimension, "Match count");
			nav.where(patternDimension, entry.getKey().toSymbolicString());
			List<String> matches = entry.getValue();
			int size = matches.size();
			nav.put(size, true);
			nav.attach(new ListResult<String>(matches));

			nav.where(measuresDimension, "Sample");
			nav.put(matches.get(0), true);
		}

		return new CrosstabResult(crosstab);
	}

	// setter methods for unittesting purposes

	public void setColumn(InputColumn<String> column) {
		this.column = column;
	}

	public void setPredefinedTokenName(String predefinedTokenName) {
		this.predefinedTokenName = predefinedTokenName;
	}

	public void setPredefinedTokenPatterns(String[] predefinedTokenPatterns) {
		this.predefinedTokenPatterns = predefinedTokenPatterns;
	}

	public void setDiscriminateTextCase(Boolean discriminateTextCase) {
		this.discriminateTextCase = discriminateTextCase;
	}

	public void setDiscriminateNegativeNumbers(
			Boolean discriminateNegativeNumbers) {
		this.discriminateNegativeNumbers = discriminateNegativeNumbers;
	}

	public void setDiscriminateDecimals(Boolean discriminateDecimals) {
		this.discriminateDecimals = discriminateDecimals;
	}

	public void setEnableMixedTokens(Boolean enableMixedTokens) {
		this.enableMixedTokens = enableMixedTokens;
	}
}
