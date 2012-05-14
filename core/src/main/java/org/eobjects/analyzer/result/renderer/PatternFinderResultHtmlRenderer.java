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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.result.html.BodyElement;
import org.eobjects.analyzer.result.html.HeadElement;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.html.SimpleHtmlFragment;

@RendererBean(HtmlRenderingFormat.class)
public class PatternFinderResultHtmlRenderer extends AbstractRenderer<PatternFinderResult, HtmlFragment> {

    @Inject
    DescriptorProvider descriptorProvider;

    public PatternFinderResultHtmlRenderer(DescriptorProvider descriptorProvider) {
        this.descriptorProvider = descriptorProvider;
    }

    @Override
    public HtmlFragment render(PatternFinderResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"patternFinderResultContainer\">");
        final SimpleHtmlFragment htmlFragment = new SimpleHtmlFragment();
        if (result.isGroupingEnabled()) {
            Map<String, Crosstab<?>> crosstabs = result.getGroupedCrosstabs();
            if (crosstabs.isEmpty()) {
                htmlFragment.addBodyElement("<p>No patterns found</p>");
                return htmlFragment;
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
                append(sb, htmlFragment, crosstab);
                sb.append("</div>");
            }
        } else {
            Crosstab<?> crosstab = result.getSingleCrosstab();
            sb.append("<div class=\"patternFinderResultPanel\">");
            append(sb, htmlFragment, crosstab);
            sb.append("</div>");
        }
        sb.append("</div>");
        htmlFragment.addBodyElement(sb.toString());
        return htmlFragment;
    }

    private void append(StringBuilder sb, SimpleHtmlFragment htmlFragment, Crosstab<?> crosstab) {
        final CrosstabHtmlRenderer crosstabHtmlRenderer = new CrosstabHtmlRenderer(descriptorProvider);

        final HtmlFragment renderedResult = crosstabHtmlRenderer.render(crosstab);

        final List<BodyElement> bodyElements = renderedResult.getBodyElements();
        assert 1 == bodyElements.size();
        for (BodyElement bodyElement : bodyElements) {
            sb.append(bodyElement.toHtml());
        }

        final List<HeadElement> headElements = renderedResult.getHeadElements();
        assert headElements.isEmpty();
        for (HeadElement headElement : headElements) {
            htmlFragment.addHeadElement(headElement);
        }
    }

}
