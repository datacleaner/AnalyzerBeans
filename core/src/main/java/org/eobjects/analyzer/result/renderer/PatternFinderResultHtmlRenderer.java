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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.PatternFinderResult;

@RendererBean(HtmlRenderingFormat.class)
public class PatternFinderResultHtmlRenderer extends AbstractRenderer<PatternFinderResult, String> {

	@Override
	public String render(PatternFinderResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"patternFinderResultContainer\">");
		final CrosstabHtmlRenderer crosstabHtmlRenderer = new CrosstabHtmlRenderer();
		if (result.isGroupingEnabled()) {
			Map<String, Crosstab<?>> crosstabs = result.getGroupedCrosstabs();
			if (crosstabs.isEmpty()) {
				return "<p>No patterns found</p>";
			}
			Set<Entry<String, Crosstab<?>>> crosstabEntries = crosstabs.entrySet();
			for (Entry<String, Crosstab<?>> entry : crosstabEntries) {
				String group = entry.getKey();
				Crosstab<?> crosstab = entry.getValue();
				if (sb.length() != 0) {
					sb.append("\n");
				}

				sb.append("<h3>Patterns for group: ");
				sb.append(group);
				sb.append("</h3>");
				sb.append("<div class=\"patternFinderResultPanel\">");
				sb.append(crosstabHtmlRenderer.render(crosstab));
				sb.append("</div>");
			}
		} else {
			Crosstab<?> crosstab = result.getSingleCrosstab();
			sb.append("<div class=\"patternFinderResultPanel\">");
			sb.append(crosstabHtmlRenderer.render(crosstab));
			sb.append("</div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

}
