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
package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.ResultProducer;

public class HtmlCrosstabRendererCallback implements CrosstabRendererCallback<String> {

	private StringBuilder sb;

	public HtmlCrosstabRendererCallback() {
		sb = new StringBuilder();
	}

	@Override
	public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
			List<CrosstabDimension> verticalDimensions) {
		sb.append("<table class=\"crosstabTable\">");
	}

	@Override
	public void endTable() {
		sb.append("</table>");
	}

	@Override
	public void beginRow() {
		sb.append("<tr>");
	}

	@Override
	public void endRow() {
		sb.append("</tr>");
	}

	@Override
	public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
		if (width <= 0) {
			return;
		}
		if (width > 1) {
			sb.append("<td class=\"crosstabHorizontalHeader\" colspan=\"");
			sb.append(width);
			sb.append("\">");
		} else if (width == 1) {
			sb.append("<td>");
		}
		sb.append(category);
		sb.append("</td>");
	}

	@Override
	public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
		if (height <= 0) {
			return;
		}
		if (height > 1) {
			sb.append("<td class=\"crosstabVerticalHeader\" rowspan=\"");
			sb.append(height);
			sb.append("\">");
		} else if (height == 1) {
			sb.append("<td>");
		}
		sb.append(category);
		sb.append("</td>");
	}

	@Override
	public void valueCell(Object value, ResultProducer drillToDetailResultProducer) {
		if (value == null) {
			value = "&lt;null&gt;";
		}
		sb.append("<td>");
		sb.append(value.toString());
		sb.append("</td>");
	}

	@Override
	public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
		sb.append("<td></td>");
	}

	@Override
	public String getResult() {
		return sb.toString();
	}
}
