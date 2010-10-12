package org.eobjects.analyzer.result;

/**
 * Very simple result type for analyzers that simply return a number (maybe a
 * KPI or something like that).
 * 
 * Mostly used for testing purposes.
 * 
 * @author Kasper Sørensen
 */
public class NumberResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;
	private Number number;

	public NumberResult(Number number) {
		this.number = number;
	}

	public Number getNumber() {
		return number;
	}

	@Override
	public String toString() {
		if (number == null) {
			return "<null>";
		}
		return number.toString();
	}
}
