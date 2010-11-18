package org.eobjects.analyzer.result;

public class BooleanAnalyzerResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Crosstab<Number> _columnStatisticsCrosstab;
	private Crosstab<Number> _valueCombinationCrosstab;

	public BooleanAnalyzerResult(Crosstab<Number> columnStatisticsCrosstab, Crosstab<Number> valueCombinationCrosstab) {
		_columnStatisticsCrosstab = columnStatisticsCrosstab;
		_valueCombinationCrosstab = valueCombinationCrosstab;
	}

	public Crosstab<Number> getColumnStatisticsCrosstab() {
		return _columnStatisticsCrosstab;
	}

	public Crosstab<Number> getValueCombinationCrosstab() {
		return _valueCombinationCrosstab;
	}
}
