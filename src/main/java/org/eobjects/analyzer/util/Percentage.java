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
package org.eobjects.analyzer.util;

public class Percentage extends Number {

	private static final long serialVersionUID = 1L;

	private short percentage;

	public Percentage(int percentage) {
		this((short) percentage);
	}

	public Percentage(short percentage) {
		if (percentage < 0 || percentage > 100) {
			throw new IllegalArgumentException("Percentage has to be between 0 and 100. Invalid: " + percentage);
		}
		this.percentage = percentage;
	}

	@Override
	public int intValue() {
		return percentage / 100;
	}

	@Override
	public long longValue() {
		return percentage / 100;
	}

	@Override
	public float floatValue() {
		return percentage / 100.0f;
	}

	@Override
	public double doubleValue() {
		return percentage / 100.0d;
	}

	@Override
	public String toString() {
		return percentage + "%";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + percentage;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Percentage other = (Percentage) obj;
		if (percentage != other.percentage)
			return false;
		return true;
	}

	public static Percentage parsePercentage(String str) throws NumberFormatException {
		if (str == null) {
			throw new NumberFormatException("cannot parse null");
		}
		str = str.trim();

		if (str.length() == 0 || str.length() > 4) {
			throw new NumberFormatException(str);
		}

		char percentageSymbol = str.charAt(str.length() - 1);
		if (percentageSymbol != '%') {
			throw new NumberFormatException(str);
		}

		str = str.substring(0, str.length() - 1);
		short p = Short.parseShort(str);

		return new Percentage(p);
	}
}
