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
package org.eobjects.analyzer.beans.stringpattern;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

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
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.CollectionUtils;

@AnalyzerBean("Pattern finder")
@Description("The Pattern Finder will inspect your String values and generate and match string patterns that suit your data.\nIt can be used for a lot of purposes but is excellent for verifying or getting ideas about the format of the string-values in a column.")
@Concurrent(true)
public class PatternFinderAnalyzer implements RowProcessingAnalyzer<PatternFinderResult> {

	private DefaultPatternFinder _patternFinder;

	@Provided
	RowAnnotationFactory _rowAnnotationFactory;

	@Configured(order = 1)
	InputColumn<String> column;

	@Configured(required = false, order = 2)
	@Description("Separate text tokens based on case")
	Boolean discriminateTextCase = true;

	@Configured(required = false, order = 3)
	@Description("Separate number tokens based on negativity")
	Boolean discriminateNegativeNumbers = false;

	@Configured(required = false, order = 4)
	@Description("Separate number tokens for decimals")
	Boolean discriminateDecimals = true;

	@Configured(required = false, order = 5)
	@Description("Use '?'-tokens for mixed text and numbers")
	Boolean enableMixedTokens = true;

	@Configured(required = false, order = 6)
	@Description("Ignore whitespace differences")
	Boolean ignoreRepeatedSpaces = false;

	@Configured(value = "Upper case patterns expand in size", required = false, order = 7)
	@Description("Auto-adjust/expand uppercase text tokens")
	boolean upperCaseExpandable = false;

	@Configured(value = "Lower case patterns expand in size", required = false, order = 8)
	@Description("Auto-adjust/expand lowercase text tokens")
	boolean lowerCaseExpandable = true;

	@Configured(value = "Predefined token name", required = false, order = 9)
	String predefinedTokenName;

	@Configured(value = "Predefined token regexes", required = false, order = 10)
	String[] predefinedTokenPatterns;

	@Configured(required = false, order = 11)
	Character decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

	@Configured(required = false, order = 12)
	Character thousandsSeparator = DecimalFormatSymbols.getInstance().getGroupingSeparator();

	@Configured(required = false, order = 13)
	Character minusSign = DecimalFormatSymbols.getInstance().getMinusSign();

	@Initialize
	public void init() {
		TokenizerConfiguration configuration;
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

		_patternFinder = new DefaultPatternFinder(configuration, _rowAnnotationFactory);
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		String value = row.getValue(column);
		_patternFinder.run(row, value, distinctCount);
	}

	@Override
	public PatternFinderResult getResult() {
		CrosstabDimension measuresDimension = new CrosstabDimension("Measures");
		measuresDimension.addCategory("Match count");
		CrosstabDimension patternDimension = new CrosstabDimension("Pattern");
		Crosstab<Serializable> crosstab = new Crosstab<Serializable>(Serializable.class, measuresDimension, patternDimension);

		Set<Entry<TokenPattern, RowAnnotation>> entrySet = _patternFinder.getAnnotations().entrySet();

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

	public void setDecimalSeparator(Character decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public void setIgnoreRepeatedSpaces(Boolean ignoreRepeatedSpaces) {
		this.ignoreRepeatedSpaces = ignoreRepeatedSpaces;
	}

	public void setMinusSign(Character minusSign) {
		this.minusSign = minusSign;
	}

	public void setThousandsSeparator(Character thousandsSeparator) {
		this.thousandsSeparator = thousandsSeparator;
	}
}
