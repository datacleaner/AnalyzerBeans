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
import java.util.Set;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.beans.valuedist.ValueCountList;
import org.eobjects.analyzer.result.ValueDistributionGroupResult;
import org.eobjects.analyzer.result.ValueDistributionResult;

@RendererBean(HtmlRenderingFormat.class)
public class ValueDistributionResultHtmlRenderer extends AbstractRenderer<ValueDistributionResult, String> {

	@Override
	public String render(ValueDistributionResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"valueDistributionResultContainer\">");
		if (result.isGroupingEnabled()) {
			Set<ValueDistributionGroupResult> groupResults = result.getGroupedValueDistributionResults();
			for (ValueDistributionGroupResult groupResult : groupResults) {
				String group = groupResult.getGroupName();
				sb.append("<h3>Value distribution for group: ");
				sb.append(group);
				sb.append("</h3>");
				sb.append(render(groupResult));
			}
		} else {
			ValueDistributionGroupResult groupResult = result.getSingleValueDistributionResult();
			sb.append(render(groupResult));
		}
		sb.append("</div>");
		return sb.toString();
	}

	public String render(ValueDistributionGroupResult groupResult) {
		final ValueCountList topValues = groupResult.getTopValues();
		final ValueCountList bottomValues = groupResult.getBottomValues();
		final int totalCount = groupResult.getTotalCount();
		final int distinctCount = groupResult.getDistinctCount();
		final int uniqueCount = groupResult.getUniqueCount();
		final int nullCount = groupResult.getNullCount();

		final StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"valueDistributionGroupPanel\">");
		if (topValues.getActualSize() + bottomValues.getActualSize() > 0) {
			sb.append("<table class=\"valueDistributionValueTable\">");
			render(topValues, sb);
			render(bottomValues, sb);
			sb.append("</table>");
		}
		sb.append("<table class=\"valueDistributionSummaryTable\">");
		render(sb, "Total count", totalCount);
		render(sb, "Distrinct count", distinctCount);
		render(sb, "Unique count", uniqueCount);
		render(sb, "Null count", nullCount);
		sb.append("</table>");
		sb.append("</div>");
		return sb.toString();
	}

	private void render(ValueCountList list, StringBuilder sb) {
		final List<ValueCount> valueCounts = list.getValueCounts();
		for (ValueCount valueCount : valueCounts) {
			final String value = valueCount.getValue();
			final int count = valueCount.getCount();
			render(sb, value, count);
		}
	}

	private void render(StringBuilder sb, String value, int count) {
		sb.append("<tr><td>");
		sb.append(value);
		sb.append("</td><td>");
		sb.append(count);
		sb.append("</td></tr>");
	}

}
