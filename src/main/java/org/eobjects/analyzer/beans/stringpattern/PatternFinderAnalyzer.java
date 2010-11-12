package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
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
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.CollectionUtils;

@AnalyzerBean("Pattern finder")
@Description("The Pattern Finder will inspect your String values and generate and match string patterns that suit your data.\nIt can be used for a lot of purposes but is excellent for verifying or getting ideas about the format of the string-values in a column.")
public class PatternFinderAnalyzer implements RowProcessingAnalyzer<PatternFinderResult> {

	private Map<TokenPattern, RowAnnotation> patterns;
	private TokenizerConfiguration configuration;
	private Tokenizer tokenizer;

	@Provided
	RowAnnotationFactory _rowAnnotationFactory;

	@Configured
	InputColumn<String> column;

	@Configured(value = "Predefined token name", required = false)
	String predefinedTokenName;

	@Configured(value = "Predefined token regexes", required = false)
	String[] predefinedTokenPatterns;

	@Configured(required = false)
	@Description("Separate text tokens based on case")
	private Boolean discriminateTextCase = true;

	@Configured(required = false)
	@Description("Separate number tokens based on negativity")
	private Boolean discriminateNegativeNumbers = false;

	@Configured(required = false)
	@Description("Separate number tokens for decimals")
	private Boolean discriminateDecimals = true;

	@Configured(required = false)
	@Description("Use '?'-tokens for mixed text and numbers")
	private Boolean enableMixedTokens = true;

	@Configured(required = false)
	@Description("Ignore whitespace differences")
	private Boolean ignoreRepeatedSpaces = false;

	@Configured(required = false)
	private Character decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

	@Configured(required = false)
	private Character thousandsSeparator = DecimalFormatSymbols.getInstance().getGroupingSeparator();

	@Configured(required = false)
	private Character minusSign = DecimalFormatSymbols.getInstance().getMinusSign();

	@Configured(value = "Upper case patterns expand in size", required = false)
	@Description("Auto-adjust/expand uppercase text tokens")
	private boolean upperCaseExpandable = false;

	@Configured(value = "Lower case patterns expand in size", required = false)
	@Description("Auto-adjust/expand lowercase text tokens")
	private boolean lowerCaseExpandable = true;

	@Initialize
	public void init() {
		patterns = new HashMap<TokenPattern, RowAnnotation>();

		if (enableMixedTokens != null) {
			configuration = new TokenizerConfiguration(enableMixedTokens);
		} else {
			configuration = new TokenizerConfiguration();
		}

		configuration.setUpperCaseExpandable(upperCaseExpandable);
		configuration.setLowerCaseExpandable(lowerCaseExpandable);

		if (discriminateNegativeNumbers != null) {
			configuration.setDiscriminateNegativeNumbers(discriminateNegativeNumbers);
		}

		if (discriminateDecimals != null) {
			configuration.setDiscriminateDecimalNumbers(discriminateDecimals);
		}

		if (discriminateTextCase != null) {
			configuration.setDiscriminateTextCase(discriminateTextCase);
		}

		if (ignoreRepeatedSpaces != null) {
			boolean ignoreSpacesLength = ignoreRepeatedSpaces.booleanValue();
			configuration.setDistriminateTokenLength(TokenType.WHITESPACE, !ignoreSpacesLength);
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
			Set<String> tokenRegexes = CollectionUtils.set(predefinedTokenPatterns);
			configuration.getPredefinedTokens().add(new PredefinedTokenDefinition(predefinedTokenName, tokenRegexes));
		}

		tokenizer = new DefaultTokenizer(configuration);
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		String value = row.getValue(column);
		if (value != null) {
			final List<Token> tokens;
			boolean match = false;
			try {
				tokens = tokenizer.tokenize(value);
			} catch (RuntimeException e) {
				throw new IllegalStateException("Error occurred while tokenizing value: " + value, e);
			}
			Set<Entry<TokenPattern, RowAnnotation>> entries = patterns.entrySet();
			for (Entry<TokenPattern, RowAnnotation> entry : entries) {
				TokenPattern pattern = entry.getKey();
				RowAnnotation annotation = entry.getValue();
				if (pattern.match(tokens)) {
					for (int i = 0; i < distinctCount; i++) {
						_rowAnnotationFactory.annotate(row, distinctCount, annotation);
					}
					match = true;
				}
			}

			if (!match) {
				final TokenPattern pattern;
				try {
					pattern = new TokenPatternImpl(value, tokens, configuration);
				} catch (RuntimeException e) {
					throw new IllegalStateException("Error occurred while creating pattern for: " + tokens, e);
				}

				RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
				for (int i = 0; i < distinctCount; i++) {
					_rowAnnotationFactory.annotate(row, distinctCount, annotation);
				}
				patterns.put(pattern, annotation);
			}
		}
	}

	@Override
	public PatternFinderResult getResult() {
		CrosstabDimension measuresDimension = new CrosstabDimension("Measures");
		measuresDimension.addCategory("Match count");
		CrosstabDimension patternDimension = new CrosstabDimension("Pattern");
		Crosstab<Serializable> crosstab = new Crosstab<Serializable>(Serializable.class, measuresDimension, patternDimension);

		Set<Entry<TokenPattern, RowAnnotation>> entrySet = patterns.entrySet();

		// sort the entries so that the ones with the highest amount of matches
		// are at the top
		Set<Entry<TokenPattern, RowAnnotation>> sortedEntrySet = new TreeSet<Entry<TokenPattern, RowAnnotation>>(
				new Comparator<Entry<TokenPattern, RowAnnotation>>() {
					public int compare(Entry<TokenPattern, RowAnnotation> o1, Entry<TokenPattern, RowAnnotation> o2) {
						int result = o2.getValue().getRowCount() - o1.getValue().getRowCount();
						if (result == 0) {
							result = o1.getKey().toSymbolicString().compareTo(o2.getKey().toSymbolicString());
						}
						return result;
					}
				});
		sortedEntrySet.addAll(entrySet);

		for (Entry<TokenPattern, RowAnnotation> entry : sortedEntrySet) {
			CrosstabNavigator<Serializable> nav = crosstab.where(patternDimension, entry.getKey().toSymbolicString());

			nav.where(measuresDimension, "Match count");
			nav.where(patternDimension, entry.getKey().toSymbolicString());
			RowAnnotation annotation = entry.getValue();
			int size = annotation.getRowCount();
			nav.put(size, true);
			nav.attach(new AnnotatedRowsResult(annotation, _rowAnnotationFactory, column));

			nav.where(measuresDimension, "Sample");
			nav.put(entry.getKey().getSampleString(), true);
		}

		return new PatternFinderResult(column, crosstab);
	}

	// setter methods for unittesting purposes
	public void setRowAnnotationFactory(RowAnnotationFactory rowAnnotationFactory) {
		_rowAnnotationFactory = rowAnnotationFactory;
	}

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

	public void setDiscriminateNegativeNumbers(Boolean discriminateNegativeNumbers) {
		this.discriminateNegativeNumbers = discriminateNegativeNumbers;
	}

	public void setDiscriminateDecimals(Boolean discriminateDecimals) {
		this.discriminateDecimals = discriminateDecimals;
	}

	public void setEnableMixedTokens(Boolean enableMixedTokens) {
		this.enableMixedTokens = enableMixedTokens;
	}

	public void setUpperCaseExpandable(boolean upperCaseExpandable) {
		this.upperCaseExpandable = upperCaseExpandable;
	}

	public void setLowerCaseExpandable(boolean lowerCaseExpandable) {
		this.lowerCaseExpandable = lowerCaseExpandable;
	}
}
