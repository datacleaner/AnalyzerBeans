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
package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.StringPattern;

@FilterBean("String pattern match")
@Description("Filters values that matches and does not match a string pattern")
public class StringPatternMatchFilter implements Filter<ValidationCategory> {

	@Configured
	InputColumn<String> column;

	@Configured
	StringPattern stringPattern;

	public StringPatternMatchFilter(InputColumn<String> column, StringPattern stringPattern) {
		this();
		this.column = column;
		this.stringPattern = stringPattern;
	}

	public StringPatternMatchFilter() {
	}

	@Override
	public ValidationCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(column);
		if (value != null) {
			if (stringPattern.matches(value)) {
				return ValidationCategory.VALID;
			}
		}
		return ValidationCategory.INVALID;
	}

}
